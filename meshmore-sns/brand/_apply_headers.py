#!/usr/bin/env python3
"""Add the short MIT/SPDX copyright header to every .dart file under
the project's source trees that doesn't already carry one. Idempotent
— rerun safely after adding new files.

Update years/owners by editing HEADER below.
"""
import os
import re

HEADER = (
    "// Copyright (c) 2026 IoTone, Inc.\n"
    "// SPDX-License-Identifier: MIT\n"
)

ROOTS = [
    "meshmore_sns_app/responsive_starter_app/lib",
    "meshmore_sns_app/responsive_starter_app/test",
    "meshmore_sns_app/packages/meshcore/lib",
    "meshmore_sns_app/packages/meshcore/test",
]

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def has_header(text: str) -> bool:
    """True when the file already begins with our SPDX header (any year)."""
    return bool(re.match(
        r"//\s*Copyright \(c\)\s*\d{4}\s+IoTone[^\n]*\n"
        r"//\s*SPDX-License-Identifier:\s*MIT\s*\n",
        text,
    ))


def has_other_copyright(text: str) -> bool:
    """If the file carries some other Copyright line at the top, leave it
    alone — likely third-party code or generated."""
    head = text[:512]
    return bool(
        re.search(r"^//\s*Copyright", head, flags=re.M)
        and "IoTone" not in head
    )


def update_in_place(path: str) -> str:
    """Returns one of: 'added', 'skipped (already has)', 'skipped (other)'."""
    with open(path, encoding="utf-8") as f:
        text = f.read()
    if has_header(text):
        # Refresh year/owner if the same shape but different year.
        new = re.sub(
            r"//\s*Copyright \(c\)\s*\d{4}\s+IoTone[^\n]*\n"
            r"//\s*SPDX-License-Identifier:\s*MIT\s*\n",
            HEADER,
            text,
            count=1,
        )
        if new != text:
            with open(path, "w", encoding="utf-8") as f:
                f.write(new)
            return "refreshed"
        return "skipped (already has)"
    if has_other_copyright(text):
        return "skipped (other copyright)"
    with open(path, "w", encoding="utf-8") as f:
        f.write(HEADER + text)
    return "added"


def walk_dart_files():
    for root in ROOTS:
        abs_root = os.path.join(REPO_ROOT, root)
        if not os.path.isdir(abs_root):
            continue
        for dirpath, _dirs, files in os.walk(abs_root):
            # Skip build/generated dirs.
            if any(part in dirpath for part in (
                "/build/", "/.dart_tool/", "/.git/")):
                continue
            for fn in files:
                if fn.endswith(".dart"):
                    yield os.path.join(dirpath, fn)


def main():
    summary = {"added": 0, "refreshed": 0,
               "skipped (already has)": 0,
               "skipped (other copyright)": 0}
    for p in walk_dart_files():
        result = update_in_place(p)
        summary[result] = summary.get(result, 0) + 1
        if result in ("added", "refreshed"):
            print(f"  {result:>10}  {os.path.relpath(p, REPO_ROOT)}")
    print("\nsummary:")
    for k, v in summary.items():
        print(f"  {k:>30}: {v}")


if __name__ == "__main__":
    main()
