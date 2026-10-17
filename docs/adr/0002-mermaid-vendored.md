# ADR 0002: Mermaid for diagrams, vendored and rendered in the browser

Status: Accepted (2026-10-17)

## Context

Diagrams must be version-controlled, reviewable in pull requests, and kept close to
the text that explains them. The published site must not depend on a CDN or a
proprietary rendering service, and the build must not need Node.js.

## Decision

- Write diagrams as ```` ```mermaid ```` blocks inside the Markdown articles.
- Vendor **Mermaid 11.17.2** (MIT) at `static/js/vendor/mermaid.min.js` together with
  its licence, and load it only on pages that contain a diagram.
- Render client-side with `securityLevel: "strict"`, re-rendering when the colour
  theme changes. A diagram that fails to parse keeps its source visible, gets the
  `diagram--error` class, and logs `[diagram-error]` to the console.
- Mermaid 11 was chosen over the newer 12 line because it has had a longer
  stabilisation period. Upgrade deliberately, re-checking every diagram.

## Consequences

+ Diagrams are plain text in the repository and render without any build step.
+ Without JavaScript, readers still see the diagram source, which is readable.
- About 3 MB of JavaScript on diagram pages (cached after the first load).
- Syntax errors are found at render time, not at build time. Validation therefore
  loads every page with diagrams in a headless browser and checks for
  `diagram--error` (see README, "Validating diagrams").
