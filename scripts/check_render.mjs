#!/usr/bin/env node
// Loads every page of a running site in headless Chrome and reports:
//   - Mermaid diagrams that failed to render (.diagram--error)
//   - horizontal overflow at the given viewport width (layout bugs)
//
// Local tool only (Node 22+ and Chrome/Chromium required); the site itself
// needs neither. Typical use, with `hugo server` running:
//
//   node scripts/check_render.mjs http://localhost:1313/ --width 390
//   node scripts/check_render.mjs http://localhost:1313/ --width 1440 --only-diagrams
//
// Set CHROME_PATH if Chrome is not found automatically.
import { spawn } from "node:child_process";
import { existsSync, mkdtempSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";

const args = process.argv.slice(2);
const base = (args.find((a) => /^https?:/.test(a)) || "http://localhost:1313/").replace(/\/?$/, "/");
const width = Number(args[args.indexOf("--width") + 1] || 1280) || 1280;
const onlyDiagrams = args.includes("--only-diagrams");

const candidates = [
  process.env.CHROME_PATH,
  "C:/Program Files/Google/Chrome/Application/chrome.exe",
  "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
  "/usr/bin/google-chrome", "/usr/bin/chromium", "/usr/bin/chromium-browser",
].filter(Boolean);
const chrome = candidates.find((p) => existsSync(p));
if (!chrome) { console.error("Chrome not found; set CHROME_PATH"); process.exit(2); }

const sitemap = await (await fetch(new URL("sitemap.xml", base))).text();
let urls = [...sitemap.matchAll(/<loc>([^<]+)<\/loc>/g)].map((m) => m[1]);
// The sitemap uses the configured baseURL; point every URL at the server being tested.
urls = urls.map((u) => new URL(new URL(u).pathname.replace(new URL(base).pathname, "/").replace(/^\/+/, ""), base).href);

const port = 9300 + Math.floor(Math.random() * 500);
const proc = spawn(chrome, ["--headless=new", "--disable-gpu", `--remote-debugging-port=${port}`,
  `--user-data-dir=${mkdtempSync(join(tmpdir(), "render-check-"))}`, "about:blank"], { stdio: "ignore" });
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
let targets;
for (let i = 0; i < 50 && !targets; i++) {
  try { targets = await (await fetch(`http://127.0.0.1:${port}/json`)).json(); } catch { await sleep(200); }
}
const ws = new WebSocket(targets.find((t) => t.type === "page").webSocketDebuggerUrl);
await new Promise((r) => ws.addEventListener("open", r));
let seq = 0; const pending = new Map();
ws.addEventListener("message", (e) => {
  const m = JSON.parse(e.data);
  if (m.id && pending.has(m.id)) { pending.get(m.id)(m); pending.delete(m.id); }
});
const send = (method, params = {}) => new Promise((r) => { const i = ++seq; pending.set(i, r); ws.send(JSON.stringify({ id: i, method, params })); });
const evaluate = async (expression) =>
  (await send("Runtime.evaluate", { expression, returnByValue: true, awaitPromise: true })).result?.result?.value;

await send("Page.enable");
await send("Emulation.setDeviceMetricsOverride", { width, height: 900, deviceScaleFactor: 1, mobile: width < 700 });

let problems = 0; let diagrams = 0; let pages = 0;
for (const url of urls) {
  await send("Page.navigate", { url });
  for (let i = 0; i < 80; i++) {
    await sleep(250);
    if (await evaluate(`document.readyState === "complete" && (!document.querySelector("pre.mermaid") || document.documentElement.dataset.diagrams === "done")`)) break;
  }
  const r = await evaluate(`({
    total: document.querySelectorAll("pre.mermaid").length,
    failed: [...document.querySelectorAll(".diagram--error")].map(f => f.getAttribute("aria-label") || "untitled"),
    overflow: document.documentElement.scrollWidth - document.documentElement.clientWidth
  })`);
  if (!r) { console.log(`FAIL ${url}: page did not load`); problems++; continue; }
  if (onlyDiagrams && r.total === 0) continue;
  pages++; diagrams += r.total;
  for (const f of r.failed) { console.log(`FAIL ${url}: diagram "${f}" did not render`); problems++; }
  if (!onlyDiagrams && r.overflow > 0) { console.log(`FAIL ${url}: ${r.overflow}px horizontal overflow at ${width}px`); problems++; }
}
console.log(`Checked ${pages} pages, ${diagrams} diagrams at ${width}px: ${problems} problem(s)`);
ws.close(); proc.kill();
process.exit(problems ? 1 : 0);
