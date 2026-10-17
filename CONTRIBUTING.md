# Contributing

Thank you for helping improve the Java & Software Architecture Knowledge Hub.
Corrections, clearer explanations, better examples and new articles are all welcome.

## Ways to contribute

- **Report an error.** Use the "open an issue" link at the bottom of any article, or
  open an issue describing the page, the problem and, ideally, a source.
- **Fix a small issue.** Use the "Edit it on GitHub" link on the page and open a pull
  request.
- **Write a new article.** Pick a topic from [CONTENT-STATUS.md](CONTENT-STATUS.md) or
  from a section's `planned:` list, and open an issue first so that work is not
  duplicated.

## Writing a new article

1. **Create it from an archetype**

   ```bash
   hugo new --kind pattern design-patterns/creational/singleton.md
   hugo new --kind architecture software-architecture/clean-architecture.md
   hugo new --kind case-study system-design/case-studies/url-shortener.md
   hugo new spring/data/transaction-propagation.md
   ```

2. **Write the example code first** (for code-heavy topics).
   - Plain Java: add a package under `examples/java/src/hub/...` with a `*Demo` class,
     run `python scripts/verify_java.py --update`, and review the generated file in
     `examples/java/expected/`.
   - Spring: extend `examples/spring-hexagonal-orders` or add a new Maven project
     under `examples/` and a CI step for it.
   - Include code in the article with the `code` shortcode. Do not paste code that CI
     does not compile, except for short illustrative snippets.

3. **Write the article** following the archetype's headings. Keep it accessible but
   precise, and prefer one well-chosen example to several shallow ones.

4. **Draw diagrams in Mermaid**, and explain each one in the text. Class and sequence
   diagrams must match the code.

5. **Publish:** set `status: complete`, remove `draft: true`, set `lastReviewed`,
   then update `CONTENT-STATUS.md`, the section's `planned:` list and any learning
   path step that covers the topic.

## Quality checklist

Before you open a pull request, confirm that the article:

- [ ] solves a real problem and says when **not** to use the technique;
- [ ] has a simple definition, the problem, a diagram, an example, trade-offs, common
      mistakes, interview Q&A, a summary, related topics and references;
- [ ] uses Java 21 and labels anything version-specific with a `[!VERSION]` callout;
- [ ] distinguishes precisely between similar ideas (for example, GoF Factory Method
      versus a simple factory; "Spring implements X" versus "Spring resembles X");
- [ ] labels assumptions and shows calculations (system design);
- [ ] uses original wording and examples, and cites sources for facts and quotations;
- [ ] contains no credentials, tokens, internal URLs or personal information.

And that the checks pass:

```bash
python scripts/verify_java.py
(cd examples/spring-hexagonal-orders && mvn verify)    # if you touched it
hugo --gc --minify
python scripts/check_links.py public
node scripts/check_render.mjs http://localhost:1313/ --width 390   # optional, needs Chrome
```

## Style

- British or American spelling is fine; be consistent within an article.
- Short sentences and active voice. Define a term before using it.
- Use tables for comparisons and trade-offs, callouts for the key idea, interview
  tips and common mistakes.
- Link to other articles by content path (`/design-patterns/behavioral/strategy`);
  the build checks them.
- Quote sparingly and attribute every quotation.

## Licensing of contributions

By contributing you agree that your written content is licensed under CC BY 4.0 and
your code under the MIT License, as described in [LICENSE](LICENSE). Only contribute
material you have the right to license this way; do not copy text, code or images from
other sources unless their licence allows it and you attribute them.
