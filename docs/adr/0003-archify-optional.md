# ADR 0003: Archify as an optional, offline authoring tool

Status: Accepted (2026-10-17)

## Context

The project brief suggested Archify for system-design diagrams, provided its identity,
licence and capabilities were verified and the site never depended on it.

Verification (2026-10-17):

- **Product:** Archify, an open-source command-line tool at
  <https://github.com/tt-a1i/archify> (version 2.17 inspected), derived from
  Cocoon-AI's architecture-diagram-generator.
- **Licence:** MIT for both projects (LICENSE file inspected); third-party notices
  are shipped with the tool.
- **How it works:** a Node.js CLI (`node bin/archify.mjs`) validates a typed JSON
  specification (architecture, workflow, sequence, data-flow or lifecycle) and renders
  a **self-contained HTML file** with inline SVG, light and dark themes, and SVG/PNG
  export. It can also import Mermaid flowcharts, sequence and state diagrams.
- **Hosting/embedding:** it is not a hosted service — there is no account, API key,
  embedding service or paid tier involved. Output is a static file.

## Decision

- Mermaid remains the primary diagram format (ADR 0002). Every article must be
  complete with its Mermaid diagrams alone.
- Archify may be used **locally, by authors**, for complex architecture diagrams that
  benefit from an interactive standalone view.
- The JSON source is committed under `diagrams/archify/` and the generated HTML under
  `static/diagrams/`. Articles link to it with the `interactive-diagram` shortcode,
  which fails the build if either file is missing.
- CI does not run Archify. Published output is the committed HTML, so the site builds
  without Node.js and keeps working if the tool disappears.

## Consequences

+ Richer diagrams where they add value, with no runtime or build dependency.
+ Diagram sources stay editable and reviewable as JSON.
- Generated HTML must be regenerated manually when the JSON source changes; reviewers
  should check that both files change together.
- Authors who use it need Node.js 18+ locally.
