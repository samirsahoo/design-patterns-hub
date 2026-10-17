#!/usr/bin/env python3
"""Check internal links and #fragments in a built Hugo site.

Hugo's render hook already fails the build on links to missing pages; this
script additionally verifies that every #fragment exists on its target page
and that asset references (CSS, JS, images) resolve to files.

Usage:
  python scripts/check_links.py public [--base-path /repo-name/]

--base-path is the URL path prefix the site was built with (for GitHub Pages
project sites, "/<repo>/"). Defaults to "/".
"""
from __future__ import annotations

import sys
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import unquote, urlsplit


class Collector(HTMLParser):
    def __init__(self) -> None:
        super().__init__()
        self.ids: set[str] = set()
        self.refs: list[str] = []

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if a.get("id"):
            self.ids.add(a["id"])
        if tag == "a" and a.get("name"):
            self.ids.add(a["name"])
        for attr in ("href", "src"):
            if a.get(attr) and not (tag == "link" and a.get("rel") in ("canonical", "alternate")):
                self.refs.append(a[attr])


def parse(path: Path) -> Collector:
    c = Collector()
    c.feed(path.read_text(encoding="utf-8"))
    return c


def resolve(root: Path, url_path: str) -> Path | None:
    p = root / unquote(url_path).lstrip("/")
    if p.is_file():
        return p
    if (p / "index.html").is_file():
        return p / "index.html"
    return None


def main() -> int:
    if len(sys.argv) < 2:
        print(__doc__)
        return 2
    root = Path(sys.argv[1])
    base = "/"
    if "--base-path" in sys.argv:
        base = "/" + sys.argv[sys.argv.index("--base-path") + 1].strip("/") + "/"
        base = base.replace("//", "/")

    pages = {p: parse(p) for p in root.rglob("*.html")}
    errors = 0
    checked = 0
    for page, data in pages.items():
        for ref in data.refs:
            parts = urlsplit(ref)
            if parts.scheme or ref.startswith(("mailto:", "//", "data:", "javascript:")):
                continue
            checked += 1
            if not parts.path:
                target = page
            else:
                if not parts.path.startswith("/"):
                    url_path = (page.parent.relative_to(root).as_posix() + "/" + parts.path)
                else:
                    if not parts.path.startswith(base):
                        print(f"{page.relative_to(root)}: {ref} is outside base path {base}")
                        errors += 1
                        continue
                    url_path = parts.path[len(base):]
                target = resolve(root, url_path)
                if target is None:
                    print(f"{page.relative_to(root)}: broken link {ref}")
                    errors += 1
                    continue
            if parts.fragment and target.suffix == ".html":
                ids = pages[target].ids if target in pages else parse(target).ids
                if unquote(parts.fragment) not in ids:
                    print(f"{page.relative_to(root)}: missing anchor {ref}")
                    errors += 1
    print(f"Checked {checked} internal references in {len(pages)} pages: {errors} problem(s)")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
