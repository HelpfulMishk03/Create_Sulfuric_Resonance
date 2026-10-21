#!/usr/bin/env python3
"""Inspect the assembled CSR jar without loading Minecraft."""

from __future__ import annotations

import json
import pathlib
import re
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
JAR = ROOT / "build/libs/sulfuricresonance-0.7.0.jar"
OUT = ROOT / "docs/audits/0.7.0-jar-inventory.json"


def main() -> int:
    if not JAR.is_file():
        raise SystemExit(f"missing built jar: {JAR}")

    with zipfile.ZipFile(JAR) as archive:
        bad_entry = archive.testzip()
        if bad_entry:
            raise SystemExit(f"corrupt zip entry: {bad_entry}")
        names = archive.namelist()
        if len(names) != len(set(names)):
            raise SystemExit("duplicate paths in jar")
        entries = set(names)
        metadata = archive.read("META-INF/neoforge.mods.toml").decode("utf-8")
        manifest = archive.read("META-INF/MANIFEST.MF").decode("utf-8")
        required = [
            "sulfuricresonance.mixins.json",
            "META-INF/LICENSE.md",
            "assets/sulfuricresonance/models/block/block_caddy.json",
            "assets/sulfuricresonance/models/block/scraper.json",
            "assets/sulfuricresonance/models/item/block_caddy.json",
            "assets/sulfuricresonance/textures/block/block_caddy.png",
            "assets/sulfuricresonance/textures/block/scraper.png",
            "assets/sulfuricresonance/textures/item/cut_blade.png",
            "assets/sulfuricresonance/lang/en_us.json",
            "data/sulfuricresonance/recipe/crafting/block_caddy.json",
            "data/sulfuricresonance/recipe/crafting/scraper.json",
            "data/sulfuricresonance/recipe/crafting/resonant_iron_bars.json",
        ]
        missing = [name for name in required if name not in entries]
        if missing:
            raise SystemExit(f"missing required jar entries: {missing}")

        class_entries = [n for n in names if n.endswith(".class")]
        majors = sorted({int.from_bytes(archive.read(n)[6:8], "big") for n in class_entries})
        if any(major != 65 for major in majors):
            raise SystemExit(f"unexpected Java class major version(s): {majors}")
        suspicious = [n for n in names if re.search(r"(?:src\.zip|\.log$|\.wld$|backup|debug_scraper_preview)", n, re.I)]
        if suspicious:
            raise SystemExit(f"unexpected development artifacts: {suspicious}")

        payloads = [archive.read(n) for n in names if n.lower().endswith((".json", ".toml", ".mf", ".properties"))]
        joined = b"\n".join(payloads).lower()
        private_path = re.search(rb"[a-z]:\\users\\[^\\]+\\(?:downloads|documents|desktop)\\", joined)
        if private_path:
            raise SystemExit(f"developer-local Windows path found in metadata/resources: {private_path.group(0)!r}")

        version = re.search(r'^version\s*=\s*"([^"]+)"', metadata, re.M)
        mod_id = re.search(r'^modId\s*=\s*"([^"]+)"', metadata, re.M)
        if not version or version.group(1) != "0.7.0" or not mod_id or mod_id.group(1) != "sulfuricresonance":
            raise SystemExit("mod identity/version mismatch")

        counts = {
            "entries": len(names),
            "classes": len(class_entries),
            "class_major_versions": majors,
            "source_resource_files": sum(1 for p in (ROOT / "src/main/resources").rglob("*") if p.is_file()),
            "source_resource_json": sum(1 for p in (ROOT / "src/main/resources").rglob("*.json") if p.is_file()),
            "jar_json": sum(n.endswith(".json") for n in names),
            "jar_png": sum(n.endswith(".png") for n in names),
            "recipes": sum(n.startswith("data/") and "/recipe/" in n and n.endswith(".json") for n in names),
            "block_models": sum("/models/block/" in n and n.endswith(".json") for n in names),
            "item_models": sum("/models/item/" in n and n.endswith(".json") for n in names),
            "blockstates": sum("/blockstates/" in n and n.endswith(".json") for n in names),
            "textures": sum("/textures/" in n and n.endswith(".png") for n in names),
            "locales": sum("/lang/" in n and n.endswith(".json") for n in names),
            "loot_tables": sum("/loot_table/" in n and n.endswith(".json") for n in names),
            "tags": sum("/tags/" in n and n.endswith(".json") for n in names),
            "required_paths_present": required,
            "archive_integrity": "passed",
            "mod_id": mod_id.group(1),
            "version": version.group(1),
            "manifest": manifest,
            "scraper_preview_ids": [n for n in names if "debug_scraper_preview" in n.lower()],
            "developer_local_paths_in_metadata_resources": False,
        }

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(counts, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(counts, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
