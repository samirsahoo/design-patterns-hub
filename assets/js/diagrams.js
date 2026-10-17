// Renders ```mermaid code blocks with the vendored Mermaid build and
// re-renders them when the colour theme changes. A block that fails to parse
// keeps its source visible and is marked with .diagram--error, and a
// "[diagram-error]" message is logged so broken diagrams are easy to find.
(function () {
  "use strict";
  if (!window.mermaid) return;

  var blocks = Array.prototype.slice.call(document.querySelectorAll("pre.mermaid"));
  blocks.forEach(function (b) { b.dataset.source = b.textContent; });
  var generation = 0;

  function render() {
    var gen = ++generation;
    var dark = (window.hubEffectiveTheme ? window.hubEffectiveTheme() : "light") === "dark";
    window.mermaid.initialize({
      startOnLoad: false,
      securityLevel: "strict",
      theme: dark ? "dark" : "neutral",
      fontFamily: "inherit",
      flowchart: { htmlLabels: true, curve: "basis" },
      sequence: { mirrorActors: false }
    });
    blocks.reduce(function (chain, b, i) {
      return chain.then(function () {
        if (gen !== generation) return;
        return window.mermaid.render("mmd-" + gen + "-" + i, b.dataset.source).then(function (out) {
          if (gen !== generation) return;
          b.innerHTML = out.svg;
          b.classList.add("is-rendered");
          b.parentNode.classList.remove("diagram--error");
          if (out.bindFunctions) out.bindFunctions(b);
        }).catch(function (err) {
          b.textContent = b.dataset.source;
          b.parentNode.classList.add("diagram--error");
          if (window.console) console.error("[diagram-error] " + (b.parentNode.getAttribute("aria-label") || "block " + i) + ": " + (err && err.message ? err.message : err));
        });
      });
    }, Promise.resolve()).then(function () {
      document.documentElement.setAttribute("data-diagrams", "done");
    });
  }

  render();
  document.addEventListener("themechange", render);
})();
