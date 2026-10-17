#!/usr/bin/env python3
"""Compile every Java example and check each runnable demo's output.

Layout:
  examples/java/src/**.java                 sources (package-structured)
  examples/java/expected/<fully.qualified.Main>.txt
                                            expected stdout of that main class

In expected files, the token {{any}} matches any text within a line.

Usage:
  python scripts/verify_java.py            compile + run + compare
  python scripts/verify_java.py --update   rewrite expected files from actual output
                                           (review the diff before committing!)
Requires a JDK (javac/java) for the release in JAVA_RELEASE.
"""
from __future__ import annotations

import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

JAVA_RELEASE = "21"
ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "examples" / "java" / "src"
EXPECTED = ROOT / "examples" / "java" / "expected"


def run(cmd: list[str], **kw) -> subprocess.CompletedProcess:
    return subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", **kw)


def matches(expected: str, actual: str) -> bool:
    exp_lines = expected.rstrip("\n").splitlines()
    act_lines = actual.rstrip("\n").splitlines()
    if len(exp_lines) != len(act_lines):
        return False
    for e, a in zip(exp_lines, act_lines):
        pattern = "^" + ".*".join(re.escape(part) for part in e.rstrip().split("{{any}}")) + "$"
        if not re.match(pattern, a.rstrip()):
            return False
    return True


def main() -> int:
    update = "--update" in sys.argv
    for tool in ("javac", "java"):
        if not shutil.which(tool):
            print(f"ERROR: {tool} not found on PATH", file=sys.stderr)
            return 2

    sources = sorted(str(p) for p in SRC.rglob("*.java"))
    if not sources:
        print("ERROR: no Java sources found", file=sys.stderr)
        return 2

    with tempfile.TemporaryDirectory() as out:
        argfile = Path(out) / "sources.txt"
        argfile.write_text("\n".join(f'"{s}"' if " " in s else s for s in sources).replace("\\", "/"), encoding="utf-8")
        result = run(["javac", "--release", JAVA_RELEASE, "-Xlint:all,-serial", "-Werror",
                      "-encoding", "UTF-8", "-d", out, f"@{argfile}"])
        if result.returncode != 0:
            print(result.stdout + result.stderr)
            print(f"FAIL: compilation of {len(sources)} files failed")
            return 1
        print(f"OK   compiled {len(sources)} source files with --release {JAVA_RELEASE}")

        mains = sorted(p.stem for p in EXPECTED.glob("*.txt"))
        failures = 0
        for main_class in mains:
            result = run(["java", "-Duser.language=en", "-Duser.country=US", "-cp", out, main_class], timeout=60)
            actual = result.stdout.replace("\r\n", "\n")
            exp_file = EXPECTED / f"{main_class}.txt"
            if result.returncode != 0:
                print(f"FAIL {main_class} exited with {result.returncode}\n{result.stderr}")
                failures += 1
                continue
            if update:
                exp_file.write_text(actual, encoding="utf-8", newline="\n")
                print(f"UPD  {main_class}")
            elif matches(exp_file.read_text(encoding="utf-8"), actual):
                print(f"OK   {main_class}")
            else:
                failures += 1
                print(f"FAIL {main_class}: output differs\n--- expected\n{exp_file.read_text(encoding='utf-8')}--- actual\n{actual}")
        if not mains:
            print("WARN no expected-output files found")
        print(f"{len(mains) - failures}/{len(mains)} examples produced the expected output")
        return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
