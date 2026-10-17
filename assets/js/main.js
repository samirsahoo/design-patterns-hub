// Site behaviour: theme toggle, mobile navigation, static search, TOC
// highlighting and copy-to-clipboard for code blocks. No dependencies.
(function () {
  "use strict";

  var root = document.documentElement;

  // ---------------------------------------------------------------- theme
  function storedTheme() {
    try { return localStorage.getItem("theme"); } catch (e) { return null; }
  }
  function effectiveTheme() {
    var t = root.getAttribute("data-theme");
    if (t === "light" || t === "dark") return t;
    return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  }
  function setTheme(t) {
    root.setAttribute("data-theme", t);
    try { localStorage.setItem("theme", t); } catch (e) { /* storage unavailable */ }
    document.dispatchEvent(new CustomEvent("themechange", { detail: { theme: t } }));
  }
  var toggle = document.querySelector(".theme-toggle");
  if (toggle) {
    toggle.addEventListener("click", function () {
      setTheme(effectiveTheme() === "dark" ? "light" : "dark");
    });
  }
  if (!storedTheme() && window.matchMedia) {
    window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", function () {
      document.dispatchEvent(new CustomEvent("themechange", { detail: { theme: effectiveTheme() } }));
    });
  }
  window.hubEffectiveTheme = effectiveTheme;

  // ------------------------------------------------------- mobile sidebar
  var navToggle = document.querySelector(".nav-toggle");
  var sidebar = document.getElementById("sidebar");
  var backdrop = document.querySelector(".sidebar-backdrop");
  function setNav(open) {
    document.body.classList.toggle("nav-open", open);
    if (navToggle) {
      navToggle.setAttribute("aria-expanded", String(open));
      navToggle.setAttribute("aria-label", open ? "Close navigation" : "Open navigation");
    }
    if (backdrop) backdrop.hidden = !open;
    if (open && sidebar) {
      var current = sidebar.querySelector('[aria-current="page"]') || sidebar.querySelector("a");
      if (current) current.focus();
    }
  }
  if (navToggle && sidebar) {
    navToggle.addEventListener("click", function () {
      setNav(!document.body.classList.contains("nav-open"));
    });
    if (backdrop) backdrop.addEventListener("click", function () { setNav(false); });
    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape" && document.body.classList.contains("nav-open")) {
        setNav(false);
        navToggle.focus();
      }
    });
  }
  // Keep the active sidebar entry in view on long trees.
  if (sidebar) {
    var active = sidebar.querySelector('[aria-current="page"]');
    if (active && active.scrollIntoView) active.scrollIntoView({ block: "center" });
  }

  // --------------------------------------------------------------- search
  var indexPromise = null;
  function loadIndex(url) {
    if (!indexPromise) {
      indexPromise = fetch(url, { credentials: "same-origin" })
        .then(function (r) { if (!r.ok) throw new Error(r.status); return r.json(); })
        .then(function (docs) {
          docs.forEach(function (d) {
            d._t = normalize(d.t);
            d._h = normalize((d.h || []).join(" "));
            d._g = normalize((d.g || []).join(" "));
            d._d = normalize(d.d);
            d._b = normalize(d.b);
          });
          return docs;
        });
    }
    return indexPromise;
  }
  function normalize(s) {
    return (s || "").toLowerCase().normalize("NFKD").replace(/[̀-ͯ]/g, "");
  }
  function terms(q) {
    return normalize(q).split(/[^a-z0-9+#]+/).filter(function (t) { return t.length > 1; });
  }
  // Every term must match somewhere; fields are weighted.
  function search(docs, q) {
    var ts = terms(q);
    if (!ts.length) return [];
    var out = [];
    docs.forEach(function (d) {
      var score = 0;
      for (var i = 0; i < ts.length; i++) {
        var t = ts[i], s = 0;
        if (d._t.indexOf(t) !== -1) s += d._t.split(/\s+/).indexOf(t) !== -1 ? 12 : 8;
        if (d._g.indexOf(t) !== -1) s += 5;
        if (d._h.indexOf(t) !== -1) s += 4;
        if (d._d.indexOf(t) !== -1) s += 3;
        if (d._b.indexOf(t) !== -1) s += 1;
        if (!s) return;
        score += s;
      }
      out.push({ doc: d, score: score });
    });
    out.sort(function (a, b) { return b.score - a.score; });
    return out;
  }
  function snippet(d, q) {
    var ts = terms(q);
    var body = d.b || "";
    var lower = normalize(body);
    var pos = -1;
    for (var i = 0; i < ts.length && pos === -1; i++) pos = lower.indexOf(ts[i]);
    if (pos === -1) return d.d || body.slice(0, 140);
    var start = Math.max(0, pos - 60);
    return (start > 0 ? "…" : "") + body.slice(start, start + 160) + "…";
  }
  function el(tag, cls, text) {
    var e = document.createElement(tag);
    if (cls) e.className = cls;
    if (text != null) e.textContent = text;
    return e;
  }

  var form = document.querySelector("form.search");
  if (form) {
    var input = form.querySelector(".search__input");
    var box = form.querySelector(".search__results");
    var selected = -1;
    var timer = null;

    function close() {
      box.hidden = true;
      input.setAttribute("aria-expanded", "false");
      input.removeAttribute("aria-activedescendant");
      selected = -1;
    }
    function highlight(i) {
      var items = box.querySelectorAll(".search__item");
      items.forEach(function (it, n) { it.setAttribute("aria-selected", String(n === i)); });
      selected = i;
      if (items[i]) {
        input.setAttribute("aria-activedescendant", items[i].id);
        items[i].scrollIntoView({ block: "nearest" });
      }
    }
    function render(q) {
      loadIndex(form.dataset.index).then(function (docs) {
        var results = search(docs, q).slice(0, 8);
        box.textContent = "";
        if (!results.length) {
          box.appendChild(el("p", "search__empty", "No results for “" + q + "”."));
        } else {
          results.forEach(function (r, i) {
            var a = el("a", "search__item");
            a.href = r.doc.u;
            a.id = "search-item-" + i;
            a.setAttribute("role", "option");
            a.appendChild(el("span", "search__section", r.doc.s));
            a.appendChild(el("span", "search__title", r.doc.t));
            a.appendChild(el("span", "search__snippet", snippet(r.doc, q)));
            box.appendChild(a);
          });
        }
        box.hidden = false;
        input.setAttribute("aria-expanded", "true");
        selected = -1;
      }).catch(function () {
        box.textContent = "";
        box.appendChild(el("p", "search__empty", "Search index could not be loaded."));
        box.hidden = false;
      });
    }
    input.addEventListener("focus", function () { loadIndex(form.dataset.index).catch(function () {}); });
    input.addEventListener("input", function () {
      clearTimeout(timer);
      var q = input.value.trim();
      if (q.length < 2) { close(); return; }
      timer = setTimeout(function () { render(q); }, 120);
    });
    input.addEventListener("keydown", function (e) {
      var items = box.querySelectorAll(".search__item");
      if (e.key === "ArrowDown" && items.length) { e.preventDefault(); highlight((selected + 1) % items.length); }
      else if (e.key === "ArrowUp" && items.length) { e.preventDefault(); highlight((selected - 1 + items.length) % items.length); }
      else if (e.key === "Enter" && selected >= 0 && items[selected]) { e.preventDefault(); window.location.href = items[selected].href; }
      else if (e.key === "Escape") { close(); }
    });
    document.addEventListener("click", function (e) { if (!form.contains(e.target)) close(); });
    document.addEventListener("keydown", function (e) {
      var tag = (document.activeElement && document.activeElement.tagName) || "";
      if (e.key === "/" && !/INPUT|TEXTAREA|SELECT/.test(tag)) { e.preventDefault(); input.focus(); }
    });
  }

  // Full results page (/search/?q=…)
  var page = document.querySelector("form.search-page");
  if (page) {
    var pInput = page.querySelector("input");
    var status = document.querySelector(".search-page__status");
    var list = document.querySelector(".search-page__results");
    var q0 = new URLSearchParams(window.location.search).get("q") || "";
    pInput.value = q0;
    function run(q) {
      list.textContent = "";
      if (!q.trim()) { status.textContent = "Type a query to search."; return; }
      status.textContent = "Searching…";
      loadIndex(page.dataset.index).then(function (docs) {
        var results = search(docs, q);
        status.textContent = results.length + (results.length === 1 ? " result" : " results") + " for “" + q + "”.";
        results.forEach(function (r) {
          var li = el("li", "search-page__result");
          var a = el("a", null, r.doc.t);
          a.href = r.doc.u;
          li.appendChild(el("span", "search__section", r.doc.s));
          li.appendChild(a);
          li.appendChild(el("p", "search__snippet", snippet(r.doc, q)));
          list.appendChild(li);
        });
      }).catch(function () { status.textContent = "Search index could not be loaded."; });
    }
    page.addEventListener("submit", function (e) {
      e.preventDefault();
      var q = pInput.value;
      history.replaceState(null, "", "?q=" + encodeURIComponent(q));
      run(q);
    });
    run(q0);
  }

  // ------------------------------------------------------ TOC highlight
  var tocLinks = document.querySelectorAll(".toc--side a[href^='#']");
  if (tocLinks.length && "IntersectionObserver" in window) {
    var map = {};
    tocLinks.forEach(function (a) { map[decodeURIComponent(a.getAttribute("href").slice(1))] = a; });
    var headings = Object.keys(map).map(function (id) { return document.getElementById(id); }).filter(Boolean);
    var visible = {};
    var obs = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) { visible[en.target.id] = en.isIntersecting; });
      var firstVisible = headings.find(function (h) { return visible[h.id]; });
      if (firstVisible) {
        tocLinks.forEach(function (a) { a.classList.remove("is-active"); });
        map[firstVisible.id].classList.add("is-active");
      }
    }, { rootMargin: "-64px 0px -60% 0px" });
    headings.forEach(function (h) { obs.observe(h); });
  }

  // ------------------------------------------------------- copy buttons
  if (navigator.clipboard) {
    document.querySelectorAll(".prose .highlight").forEach(function (block) {
      var btn = el("button", "copy-btn", "Copy");
      btn.type = "button";
      btn.setAttribute("aria-label", "Copy code to clipboard");
      btn.addEventListener("click", function () {
        var code = block.querySelector("code");
        navigator.clipboard.writeText(code ? code.innerText : "").then(function () {
          btn.textContent = "Copied";
          setTimeout(function () { btn.textContent = "Copy"; }, 1500);
        });
      });
      block.appendChild(btn);
    });
  }
})();
