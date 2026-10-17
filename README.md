# Java & Software Architecture Knowledge Hub

> Understand the concepts. Explore the diagrams. Build better software.

An open, static handbook on Java, the 23 GoF design patterns, SOLID, Spring,
software architecture, Hexagonal Architecture, Domain-Driven Design, microservices
and system design. It serves two purposes: a personal knowledge base for interview
preparation, and a public reference that developers can find through search engines.

Every article explains the problem, the idea, a diagram, a practical example,
trade-offs, common mistakes and interview questions. Code on the site is included
from source files that CI compiles, runs and tests.

## Contents

- [Site structure](#site-structure)
- [Technology stack](#technology-stack)
- [Local setup](#local-setup)
- [Hugo commands](#hugo-commands)
- [Authoring content](#authoring-content)
- [Diagram conventions](#diagram-conventions)
- [Code example conventions](#code-example-conventions)
- [Build and validation](#build-and-validation)
- [Deploying to GitHub Pages](#deploying-to-github-pages)
- [Contributing](#contributing)
- [License](#license)

## Site structure

```text
content/
  _index.md                      home page (featured articles)
  learning-paths/                six ordered learning paths
  design-patterns/               SOLID, GoF overview, creational/structural/behavioral
  java/                          fundamentals/, advanced/
  spring/                        core/, web/, data/, security/, production/, patterns-in-spring/
  software-architecture/
  hexagonal-architecture/
  domain-driven-design/          strategic/, tactical/, advanced/
  microservices/                 fundamentals/, communication/, reliability/,
                                 data-consistency/, operations/, case-studies/
  system-design/                 introduction, diagram building blocks, case-studies/
  comparisons/                   pattern and architecture comparisons
  glossary.md, about.md, search.md
layouts/                         site templates (no third-party theme)
  _partials/  _shortcodes/  _markup/   learning-paths/
assets/css, assets/js            styles and small vanilla JS (theme, nav, search, TOC, diagrams)
static/js/vendor/                vendored Mermaid 11.17.2 + licence
static/diagrams/                 optional standalone interactive diagrams (generated)
diagrams/archify/                sources for those diagrams
examples/java/                   plain Java 21 examples + expected output
examples/spring-hexagonal-orders/  Spring Boot 4.1 hexagonal example (Maven)
archetypes/                      templates: default, pattern, architecture, case-study
scripts/                         verify_java.py, check_links.py, check_render.mjs
docs/adr/                        architecture decision records for this project
CONTENT-STATUS.md                what is published and what is planned
```

Each section's `_index.md` lists its unwritten topics under `planned:`. They are shown
as plain text, never as links, so no page looks finished when it is not.

## Technology stack

| Concern | Choice | Notes |
|---|---|---|
| Static site generator | [Hugo](https://gohugo.io/) **v0.167.0** (extended) | Custom templates; see [ADR 0001](docs/adr/0001-hugo-static-site.md) |
| Content | Markdown | Raw HTML disabled; callouts use `> [!KEY]` syntax |
| Diagrams | [Mermaid](https://mermaid.js.org/) **11.17.2**, vendored | [ADR 0002](docs/adr/0002-mermaid-vendored.md); optional [Archify](https://github.com/tt-a1i/archify) exports, [ADR 0003](docs/adr/0003-archify-optional.md) |
| Search | Static JSON index + a small vanilla JS function | No backend, no library |
| Examples | Java **21**, Spring Boot **4.1.1**, Maven | [ADR 0004](docs/adr/0004-versions-and-verified-examples.md) |
| Hosting | GitHub Pages via GitHub Actions | `.github/workflows/deploy.yml` |

The published site needs no database, server, Node.js, Docker or paid service.

## Local setup

Requirements:

- **Hugo extended v0.167.0** or newer ([installation](https://gohugo.io/installation/)).
  The version is pinned in `.github/workflows/deploy.yml`.
- **JDK 21** and **Maven 3.9+** — only to verify the code examples.
- **Python 3.10+** — for `scripts/verify_java.py` and `scripts/check_links.py`.
- Optional: **Node.js 22+ and Chrome** — for `scripts/check_render.mjs`.

```bash
git clone https://github.com/<owner>/<repo>.git
cd <repo>
hugo server            # http://localhost:1313/
```

## Hugo commands

| Command | Purpose |
|---|---|
| `hugo server` | Live-reloading local preview |
| `hugo server -D` | Preview including drafts |
| `hugo --gc --minify` | Production build into `public/` |
| `hugo --gc --minify --baseURL https://<owner>.github.io/<repo>/` | Build exactly as GitHub Pages will serve a project site |
| `hugo new --kind pattern design-patterns/creational/singleton.md` | New pattern article |
| `hugo new --kind architecture software-architecture/clean-architecture.md` | New architecture article |
| `hugo new --kind case-study system-design/case-studies/url-shortener.md` | New case study |
| `hugo new java/fundamentals/generics.md` | Any other article (default archetype) |

To show repository links ("Edit on GitHub", "View source") locally, set
`HUGO_PARAMS_REPOURL=https://github.com/<owner>/<repo>`. CI sets it automatically.

## Authoring content

1. Create the file from an archetype (see above). New files start as `draft: true`.
2. Follow the section structure in the archetype. Articles are expected to include a
   simple definition, the problem, a diagram, a practical example, trade-offs, common
   mistakes, interview Q&A, a summary, related topics and references.
3. Front matter:

   | Field | Meaning |
   |---|---|
   | `title`, `linkTitle`, `description` | Page title, short navigation title, meta description (≤ 160 characters is ideal) |
   | `weight` | Order within the section |
   | `difficulty` | `beginner`, `intermediate` or `advanced` |
   | `status` / `draft` | Set `status: complete` and remove `draft` when publishing |
   | `tags`, `keywords` | Taxonomy and related-article matching |
   | `prerequisites`, `related` | Content paths; the build fails if they do not exist |
   | `lastReviewed` | Date of last technical review |
   | `javaVersion` | Shown in the article header for code-heavy pages |

4. Link to other pages by **content path**: `[Strategy](/design-patterns/behavioral/strategy)`
   or with a heading anchor: `(/design-patterns/solid-principles#d--dependency-inversion-principle)`.
   The link render hook converts these to correct URLs under any base path and fails
   the build if the page does not exist.
5. Callouts:

   ```markdown
   > [!KEY] Optional title
   > Text of the callout.
   ```

   Types: `KEY`, `INTERVIEW`, `MISTAKE`, `TRADEOFF`, `VERSION`, `TIP`, `NOTE`,
   `WARNING`, `IMPORTANT`, `CAUTION`.
6. When publishing, update `CONTENT-STATUS.md`, remove the topic from its section's
   `planned:` list, and link it from any learning path step that covers it.

## Diagram conventions

- Write diagrams as fenced ```` ```mermaid {title="…"} ```` blocks. The title becomes
  the figure caption and accessible label.
- Explain every box and arrow in the surrounding text.
- UML class and sequence diagrams must use the same class and method names as the
  example code.
- Supported and used here: `flowchart`, `sequenceDiagram`, `classDiagram`,
  `stateDiagram-v2`, `erDiagram`. Check new syntax against Mermaid 11.17.
- Reusable system-design shapes are documented in `/system-design/diagram-patterns`.
- Optional interactive diagrams (Archify): commit the JSON under `diagrams/archify/`
  and the generated HTML under `static/diagrams/`, then link it:

  ```markdown
  {{< interactive-diagram file="order-management.html" title="Order management" source="diagrams/archify/order-management.json" tool="Archify" >}}
  ```

## Code example conventions

- Plain Java examples live in `examples/java/src/hub/...`, one package per topic,
  targeting **Java 21**. Each runnable example has a `*Demo` class with `main`.
- The expected output of each demo is stored in `examples/java/expected/<main class>.txt`.
  `{{any}}` matches nondeterministic text such as UUIDs.
- Include code in articles with the `code` shortcode, never by copy-paste:

  ```markdown
  {{< code file="examples/java/src/hub/patterns/strategy/Checkout.java" >}}
  {{< code file="examples/java/expected/hub.patterns.strategy.StrategyDemo.txt" lang="text" title="Output" >}}
  ```

  `region="name"` includes only the lines between `// region name` and `// endregion`.
- Keep examples small, deterministic and dependency-free. Inject `Clock` and ID
  generators instead of calling `Instant.now()` or `UUID.randomUUID()` in logic.
- Short illustrative snippets (for example, "before" code) may be inline in Markdown.

## Build and validation

Run these before opening a pull request. CI runs the first four on every push and
pull request.

```bash
python scripts/verify_java.py                          # compile (-Werror), run, compare output
(cd examples/spring-hexagonal-orders && mvn verify)    # 17 unit, slice and end-to-end tests
hugo --gc --minify                                     # fails on broken internal links
python scripts/check_links.py public                   # verifies #anchors and asset paths

# Optional, with `hugo server` running: render every page in headless Chrome
node scripts/check_render.mjs http://localhost:1313/ --width 390    # mobile layout + diagrams
node scripts/check_render.mjs http://localhost:1313/ --width 1440   # desktop
```

After changing an example's behaviour on purpose, regenerate its expected output with
`python scripts/verify_java.py --update` and review the diff.

## Deploying to GitHub Pages

The workflow `.github/workflows/deploy.yml`:

- runs on pushes to `main`, on pull requests (build and checks only) and manually
  (**Actions → Build and deploy → Run workflow**);
- verifies the Java and Spring examples;
- builds with Hugo using the base URL that GitHub Pages reports, so both
  `https://<owner>.github.io/<repo>/` project sites and custom domains work;
- checks links and anchors in the built site;
- deploys with the official `actions/upload-pages-artifact` and `actions/deploy-pages`
  actions, using only the permissions each job needs. No secrets are required.

One-time setup:

1. Push the repository to GitHub with `main` as the default branch.
2. In the repository, open **Settings → Pages**.
3. Under **Build and deployment → Source**, choose **GitHub Actions**.
4. Open **Settings → Actions → General** and make sure actions are allowed for the
   repository.
5. Push to `main` (or run the workflow manually). The deploy job prints the site URL,
   which also appears under **Settings → Pages**.

For a custom domain, set it under **Settings → Pages → Custom domain**; the workflow
picks up the new base URL automatically on the next run.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). In short: open an issue for larger changes,
follow the article structure, include code through the `code` shortcode, and run the
validation commands above.

## License

Written content is licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/);
code is licensed under the MIT License. See [LICENSE](LICENSE). Mermaid is included
under its MIT License.
