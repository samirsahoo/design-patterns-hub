# ADR 0004: Java 21, Spring Boot 4.1, and code included from compiled sources

Status: Accepted (2026-10-17)

## Context

Readers copy code from articles. Code pasted into Markdown drifts from reality and
silently stops compiling. Examples also need one documented language and framework
version.

## Decision

- **Java 21 (LTS)** for all examples. Features finalised after Java 21 are labelled
  with their release. Java 25 is the newer LTS; articles call out where it changes
  recommended practice.
- **Spring Boot 4.1.1** (Spring Framework 7) for Spring examples.
- Code shown in articles is included from files under `examples/` with the `code`
  shortcode, which fails the build if the file or region is missing.
- `scripts/verify_java.py` compiles every plain-Java example with
  `--release 21 -Xlint:all,-serial -Werror`, runs each demo, and compares stdout with
  `examples/java/expected/<main class>.txt` (`{{any}}` matches nondeterministic text).
- The Spring Boot example is built and tested with `mvn verify` in CI.

## Consequences

+ Every code listing on the site compiles, and every printed output is real.
+ Upgrading Java or Spring is a deliberate, testable change.
- Small illustrative snippets that are not full programs (for example, "before"
  versions in an article) are written inline and are not compiled; keep them short.
