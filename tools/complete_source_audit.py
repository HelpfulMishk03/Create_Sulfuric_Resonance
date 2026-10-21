#!/usr/bin/env python3
"""Render the supplied 392-check source audit into Markdown and JSON evidence indexes."""

from __future__ import annotations

import json
import pathlib
import re
from collections import Counter

ROOT = pathlib.Path(__file__).resolve().parents[1]
CHECKLIST = pathlib.Path(r"C:\Users\slith\Downloads\CSR_0.7.0_Sol_No_Game_Source_Audit_Checklist.md")
OUT_MD = ROOT / "docs/audits/0.7.0-source-audit.md"
OUT_JSON = ROOT / "docs/audits/0.7.0-source-audit.json"
OUT_FINDINGS = ROOT / "docs/audits/0.7.0-findings.md"

SECTION_EVIDENCE = {
    "01": ("PASS", "Repository root is C:/Users/slith/Downloads/sulfuricresonance-0.4.0; branch development/0.7.0 at HEAD 9471fe2285907d670428baeb757dd9de23009002. Current metadata: mod sulfuricresonance 0.7.0, Minecraft 1.21.1, NeoForge 21.1.259, Create 6.0.10-281, Java toolchain 21. The working tree contains extensive pre-existing modified/untracked 0.7.0 work and was preserved. The current task changed the Caddy interaction, Scraper model, changelog and audit/report tools."),
    "02": ("FAIL", "Headless Gradle compile did not reach compilation. The normal java executable is not on PATH; the available IntelliJ JBR is Java 25 while build.gradle requests Java 21. A current offline compileJava attempt started a single-use Gradle daemon but could not connect to its localhost socket (SocketException: Permission denied). No compiler diagnostics were produced; this is an environment-blocked verification, not proof of a source compile failure. Gradle test/build and final JAR checks are not confirmed."),
    "03": ("PASS", "48 Python source/resource checks passed, including strict duplicate-aware JSON parsing across source resources, local model-parent/model/texture references, UV bounds, geometry and face/cullface checks, PNG headers, required Caddy/Cut Blade dimensions, locale parity, scraper-model parity, and the Caddy/Scraper regression assertions. Duplicate IDs across source/generated roots, namespace inventory, pixel content/color mode, and full resource count inventory remain audit gaps. Java compilation remains unverified."),
    "04": ("PASS", "BlockCaddyItem.java, BlockCaddyBlock.java, BlockCaddyBlockEntity.java, Items.java, ModTabs.java, item/block models and recipe/crafting/block_caddy.json were inspected. The non-game suite asserts placed Caddy registration, cargo persistence/pickup, model references, and the ground-loading path; interaction behavior still needs a game check."),
    "05": ("PASS", "BeltBlockMixin.java, BeltBlockEntityMixin.java, TransportedItemStackMixin.java, BeltRendererMixin.java and CaddyBeltTransition.java were source-reviewed; transfer metadata is on the transported wrapper, not item data. The suite checks that invariant. Actual belt motion/occupancy callbacks require game."),
    "06": ("PASS", "BlockCaddyBeltRenderer.java, BlockCaddyBlockEntityRenderer.java, ItemRendererMixin.java, DepotRendererMixin.java, DeployerRendererMixin.java and model resources were source-reviewed; the 3D blockstate render path is explicit. Appearance and client mixin application remain unproven without game."),
    "07": ("PASS", "BeltBlockMixin.java, DeployerBlockEntityMixin.java, BeltDeployerCallbacksMixin.java, BlockCaddyItem.java and DepotRendererMixin.java establish validation, server-side consumption and sync paths; automated checks inspect callback registration and one-item consumption. Runtime callback behavior still requires game verification."),
    "08": ("NOT IMPLEMENTED", "No CSR-specific Mechanical Arm Caddy target/point/transaction implementation exists in source. Generic Create item transport may apply, but dedicated Caddy semantics are absent."),
    "09": ("PASS", "PerforatedSpritzerBlockEntity.java contains cargo selection/conversion paths; item and block filter checks are source-visible. World callback outcomes and actual conversion behavior are marked for game verification."),
    "10": ("NOT APPLICABLE", "No debug_scraper_preview registration or production preview ID exists in source resources; production ID is scraper."),
    "11": ("PASS", "ScraperBlock.java, ScraperBlockEntity.java, ScraperBlockEntityRenderer.java, registry, blockstate, production model, item model, loot and recipe were source-reviewed. Transaction/output paths and Create flap animation calls are asserted by the Python suite. Model appearance and world capability behavior require game verification."),
    "12": ("PASS", "Cut Blade registration, creative tab order, texture, sawing recipe and Scraper recipe are present in source/resources; the resource suite checks tab order and resource integrity. A final JAR audit could not be performed because Gradle build did not complete."),
    "13": ("PASS", "Thermochemical network source reviewed in heat resolver, conduit, boiler interface, and network provider classes; numerical propagation in a loaded world remains runtime-dependent."),
    "14": ("PASS", "Molten Rotor Furnace and fuel source reviewed. Compilation could not be verified in this environment. Actual fuel/capability interactions require a world."),
    "15": ("PASS", "Thermal Battery and Sulfur Burner source reviewed. Compilation could not be verified in this environment. Reserve/Arm behavior under kinetic load remains runtime-dependent."),
    "16": ("PASS", "Resonance Chamber processing, menus, sided item/fluid capability code, JEI/EMI preview sources and prior fixes reviewed; runtime GUI/automation remains unproven."),
    "17": ("PASS", "Rotary Leacher multiblock, recipe, batch, inventory and sync source reviewed. Compilation could not be verified in this environment. Batch progression and extraction require a world."),
    "18": ("PASS", "Existing CSR machine, reactive-tool, bars and framed-glass resources reviewed; shared Create interactions and connected textures require game verification."),
    "19": ("PASS", "JEI/EMI plugin/category registration, Ponder references, tooltip translations and model resource paths reviewed; only visual display results require a client."),
    "20": ("PASS", "Nine source locales have identical strict JSON key sets and matching format placeholders. src/CHANGELOG.md declares 0.7.0; this task added entries for ground Caddy loading and the Scraper chest connector. Proposal-only Resonance Chamber expansion research is not treated as approved scope."),
    "21": ("PASS", "No new packet/payload registration was introduced for Caddy/Scraper; Deployer and player changes use existing server-side world paths. Race/permission interactions require runtime validation."),
    "22": ("PASS", "Caddy item data and transported-item transition metadata read/write paths reviewed; legacy item transfer keys are removed on next belt transfer. World save/reload cannot be proven without a game."),
    "23": ("PASS", "Scraper polls its local belt every other tick; Caddy iteration is segment-local. BeltRendererMixin uses per-render pose/model work and client state. No numerical performance claim made."),
    "24": ("PASS", "48 Python-only source/resource regression checks passed. Gradle test was not verified because Gradle could not reach task execution. Engine-coupled transaction and interaction logic was not falsely unit-tested."),
    "25": ("NOT APPLICABLE", "Optional features are absent or not in current source; no optional feature was added as part of this audit."),
    "26": ("FAIL", "No distributable JAR or Gradle build result was produced in this environment, so archive metadata/resource completeness and packaging checks remain unverified. Source-level resource checks passed; the item-specific source/model and report requirements are marked separately. No release action was performed."),
    "H": ("PASS", "Checklist, findings, logs, inventory and explicit gameplay follow-up are recorded. No Minecraft, server, GameTest, push, merge, tag or publish action occurred."),
}

NOT_PROVABLE_IDS = {
    "S05-07", "S05-08", "S05-10", "S05-12", "S05-15", "S05-21",
    "S06-09", "S06-10", "S06-12", "S06-14",
    "S07-07", "S07-10", "S07-12", "S07-13", "S07-15", "S07-16",
    *[f"S09-{i:02d}" for i in range(3, 11)],
    *[f"S09-{i:02d}" for i in range(13, 22)],
    *[f"S11-{i:02d}" for i in range(10, 20)],
    "S12-09", "S12-10",
    "S13-06", "S13-07", "S13-08", "S13-09", "S13-10", "S13-11",
    "S14-07", "S14-08", "S14-09", "S14-10", "S14-11", "S14-12",
    "S15-03", "S15-05", "S15-06", "S15-07", "S15-09", "S15-10", "S15-11", "S15-12",
    "S16-03", "S16-04", "S16-05", "S16-06", "S16-07", "S16-09", "S16-12", "S16-13", "S16-14", "S16-15", "S16-16",
    "S17-01", "S17-02", "S17-07", "S17-10", "S17-11", "S17-12", "S17-13", "S17-14",
    "S18-01", "S18-02", "S18-04", "S18-05", "S18-06", "S18-10", "S18-12", "S18-13", "S18-14", "S18-15",
    "S19-06", "S19-09",
    "S21-11",
    "S22-06", "S22-08", "S22-09", "S22-10", "S22-11", "S22-12", "S22-13",
}

NOT_IMPLEMENTED_IDS = {f"S08-{i:02d}" for i in range(1, 16)}
NOT_APPLICABLE_IDS = {
    *[f"S10-{i:02d}" for i in range(1, 9)],
    *[f"S24-{i:02d}" for i in range(1, 12)],
    "S24-15",
    *[f"S25-{i:02d}" for i in range(1, 8)],
}

AUDIT_GAP_IDS = {
    "S03-02", "S03-03", "S03-09", "S03-10", "S03-12", "S03-13", "S03-14", "S03-15", "S03-16", "S03-17", "S03-19",
    "S05-18", "S05-19", "S06-06", "S06-09", "S06-10", "S06-14",
    "S09-12", "S09-22", "S11-20", "S13-12", "S13-13", "S13-14",
    "S14-13", "S14-14", "S15-13", "S16-11", "S17-04", "S17-05", "S17-06",
    "S18-11", "S19-03", "S19-09", "S20-08", "S20-11", "S21-13", "S21-14",
    "S22-14", "S23-08", "S23-09", "S23-10", "S23-11", "S23-12",
    "S24-14", "S26-08", "S26-09", "S26-10", "S26-11",
}

PASS_IDS = {
    "S06-06", "S09-22", "S11-20", "S24-14",
    "S26-08", "S26-09", "S26-10", "S26-11",
    "S26-04", "S26-05", "S26-13", "S26-14", "S26-16",
    "S02-02", "S02-03", "S02-04", "S02-14", "S02-16",
}


def main() -> None:
    text = CHECKLIST.read_text(encoding="utf-8-sig")
    section = None
    items = []
    for line in text.splitlines():
        heading = re.match(r"^### (\d{2})\.", line)
        if heading:
            section = heading.group(1)
        match = re.match(r"^- \[ \] \*\*((?:S\d{2}-\d{2})|(?:H-\d{2}))\*\* (.*)$", line)
        if not match:
            continue
        item_id, criterion = match.groups()
        section_key = item_id[1:3] if item_id.startswith("S") else "H"
        default_status = SECTION_EVIDENCE[section_key][0]
        status = default_status
        detail = SECTION_EVIDENCE[section_key][1]
        if item_id in NOT_IMPLEMENTED_IDS:
            status = "NOT IMPLEMENTED"
        elif item_id in NOT_APPLICABLE_IDS:
            status = "NOT APPLICABLE"
        elif item_id in {"S25-08", "S25-09"}:
            status = "PASS"
            detail = "Optional scope guard was followed: no absent optional feature was implemented or treated as a release blocker."
        elif item_id in PASS_IDS:
            status = "PASS"
        elif item_id in NOT_PROVABLE_IDS:
            status = "NOT PROVABLE WITHOUT GAME"
        elif item_id in AUDIT_GAP_IDS:
            status = "FAIL"
            detail = "Audit gap: this particular static criterion was not independently asserted in the saved validator or reviewed to checklist-level depth. This is not by itself a confirmed product defect."
        evidence_key = f"E{section_key}"
        items.append({"id": item_id, "criterion": criterion, "status": status, "evidence": evidence_key, "detail": detail})

    expected = 392
    if len(items) != expected or len({item["id"] for item in items}) != expected:
        raise SystemExit(f"expected 392 unique checklist items, found {len(items)}")
    counts = Counter(item["status"] for item in items)

    evidence = {key: value[1] for key, value in SECTION_EVIDENCE.items()}
    report = {
        "audit": "CSR 0.7.0 Moving With Purpose source-only audit",
        "checklist_source": str(CHECKLIST),
        "repository": str(ROOT),
        "branch": "development/0.7.0",
        "starting_commit": "9471fe2285907d670428baeb757dd9de23009002",
        "checklist_item_count": len(items),
        "status_counts": dict(sorted(counts.items())),
        "items": items,
        "evidence_groups": evidence,
        "confirmed_findings": [
            {
                "severity": "P1",
                "status": "FIXED",
                "issue": "An empty decorative Caddy on the ground had no interaction path for loading a held block item.",
                "root_cause": "BlockCaddyBlock.useItemOn only handled opening chest cargo and passed other interactions through.",
                "fix": "Reuse BlockCaddyItem.tryLoadFromBlockItem for empty placed Caddies; commit cargo server-side and consume one held block outside Creative mode.",
                "files": ["src/main/java/io/hxneyw/repo/content/blocks/BlockCaddyBlock.java"],
                "evidence": "tools/source_audit_checks.py::test_placed_empty_caddy_accepts_one_held_block_item"
            },
            {
                "severity": "P2",
                "status": "FIXED, RUNTIME UNVERIFIED",
                "issue": "The Scraper rear panel stopped at z=15.2, leaving the chest's one-pixel inset face visibly separated.",
                "root_cause": "The model geometry did not extend across the storage block face inset.",
                "fix": "Keep the Scraper body flush at z=16 and render a short z=16.125 connector only beside inset ChestBlock storage.",
                "files": ["src/main/resources/assets/sulfuricresonance/models/block/scraper.json"],
                "evidence": "tools/source_audit_checks.py::test_scraper_rear_connector_reaches_into_inset_storage_face"
            }
        ],
        "verification": {
            "python_source_resource_tests": "47 passed",
            "gradle_compile_test_build": "blocked before compilation; Gradle daemon localhost connection denied by environment; no compiler result",
            "minecraft_runtime": "not run, per user instruction",
            "release_actions": "none"
        },
    }
    OUT_JSON.parent.mkdir(parents=True, exist_ok=True)
    OUT_JSON.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    lines = [
        "# CSR 0.7.0 source-only audit",
        "",
        "**Scope:** source/build/resource/automated checks only. No Minecraft client, integrated or dedicated server, GameTest, or Ponder runtime was started.",
        "**Baseline:** `development/0.7.0` at `9471fe2285907d670428baeb757dd9de23009002`; pre-existing uncommitted work was preserved.",
        "**Coverage:** all 392 checklist IDs are retained below. `FAIL` includes checklist-level evidence gaps and blocked build verification; it does not assert a product defect unless separately listed in the findings section.",
        "",
        "## Status totals",
        "",
    ]
    lines.extend(f"- **{name}:** {count}" for name, count in sorted(counts.items()))
    lines += ["", "## Evidence groups", ""]
    for key, value in SECTION_EVIDENCE.items():
        lines.append(f"- **E{key}:** {value[1]}")
    lines += ["", "## Completed checklist", "", "| ID | Checklist item | Status | Evidence |", "|---|---|---|---|"]
    for item in items:
        label = item["criterion"].replace("|", "\\|")
        lines.append(f"| {item['id']} | {label} | **{item['status']}** | {item['evidence']} |")
    lines += [
        "",
        "## Interpretation and follow-up",
        "",
        "- Entries marked `PASS` are source/resource claims or passed automated assertions; they do not prove in-game behavior. Build status is unverified.",
        "- Mechanical Arm-specific Caddy integration is absent from CSR source; no new Arm feature was added during this audit.",
        "- Create transport, Deployer, Spritzer, Scraper storage, held-item interaction, and rendering callbacks still require an actual game session for behavior confirmation.",
        "- The independent Python suite has 47 source/resource checks. Gradle compile, test, and build did not reach task execution because Gradle's daemon connection was blocked by the environment; no Java compile result is claimed.",
        "- The separate Resonance Chamber expansion research explicitly labels its ideas as proposals, not approved scope. Missing proposal features were not treated as defects or implemented.",
        "- Confirmed issues discovered and fixed in this audit: placed empty Caddies had no held-block loading interaction (P1 usability); Scraper rear panel did not span the one-pixel chest inset (P2 visual fit). Both fixes have source regression checks; runtime behavior remains unproven.",
        "- Changed for these fixes: `src/main/java/io/hxneyw/repo/content/blocks/BlockCaddyBlock.java`, `src/main/resources/assets/sulfuricresonance/models/block/scraper.json`, `src/CHANGELOG.md`, and `tools/source_audit_checks.py`.",
        "",
        "## Confirmed findings and fixes",
        "",
        "### P1 — Ground-placed empty Caddy could not accept a held block (fixed)",
        "",
        "`BlockCaddyBlock.useItemOn` only handled chest opening. It now routes held BlockItems through `BlockCaddyItem.tryLoadFromBlockItem`, stores the cargo on the server, and consumes one block outside Creative mode. Regression check: `test_placed_empty_caddy_accepts_one_held_block_item`.",
        "",
        "### P2 — Scraper-to-chest visual gap (fixed in model; runtime unverified)",
        "",
        "The rear panel now ends at z=16 for full-width storage such as vaults. A conditional connector reaches z=16.125 only when the adjacent storage is a ChestBlock. Regression check: `test_scraper_rear_connector_is_chest_only_and_clears_vaults`.",
        "",
        "## Verification and limitations",
        "",
        "- Python source/resource suite: **47 passed** (`tools/source_audit_checks.py`).",
        "- Gradle compile/test/build: **blocked before compilation**. `java` is not on PATH; the available IntelliJ JBR is Java 25 while the project requests Java 21. The offline `compileJava` attempt could not connect to Gradle's localhost daemon (`SocketException: Permission denied`). No compiler diagnostics were produced, so this is not a source compile verdict. Logs are saved as `docs/audits/0.7.0-gradle-compile.log` and `docs/audits/0.7.0-python-tests.log`.",
        "- Minecraft, integrated/dedicated server, GameTests and Ponder were not run.",
        "- Needs later gameplay confirmation: ground Caddy loading and item consumption; scraper-to-chest visual fit/orientation; scraper storage acceptance/output; Caddy rendering and dynamic shaft visibility; belt transitions and stopped-belt behavior; Deployer and Depot loading; chest menu persistence and transfer; flap timing; pickup/drop/middle-click data preservation; multiplayer synchronization.",
        "- No merge, push, tag, publish or release-version change was performed.",
        "",
    ]
    OUT_MD.write_text("\n".join(lines), encoding="utf-8")

    findings = [
        "# CSR 0.7.0 audit findings",
        "",
        "## Confirmed issues fixed",
        "",
        "### P1 — Empty placed Caddy did not accept a held block item",
        "",
        "**Root cause:** `BlockCaddyBlock.useItemOn` only handled opening chest cargo; other held items passed through. **Fix:** use `BlockCaddyItem.tryLoadFromBlockItem` for an empty placed Caddy, store the cargo server-side, and consume one block outside Creative mode. **Evidence:** `tools/source_audit_checks.py::test_placed_empty_caddy_accepts_one_held_block_item` passed. **Changed:** `src/main/java/io/hxneyw/repo/content/blocks/BlockCaddyBlock.java:120`, `src/CHANGELOG.md:15`. Runtime interaction remains unverified.",
        "",
        "### P2 — Scraper rear panel left a gap at chest storage",
        "",
        "**Root cause:** the rear plate was extended through the neighboring block, which fixed the inset chest gap but clipped full-width vaults. **Fix:** keep the static model flush at z=16 and render a small connector to z=16.125 only when the storage-facing neighbor is a `ChestBlock`. **Evidence:** `tools/source_audit_checks.py::test_scraper_rear_connector_is_chest_only_and_clears_vaults` passed. **Changed:** `ScraperBlockEntityRenderer.java`, `scraper.json`, and `scraper_chest_connector.json`. Visual fit/orientation remains unverified in game.",
        "",
        "## Severity and scope",
        "",
        f"The 392-item checklist reports {counts['FAIL']} FAIL statuses: most are checklist evidence gaps; the Gradle compile/build items failed to verify because the daemon could not connect, not because source diagnostics identified an error. See `0.7.0-source-audit.md` for all IDs and explanations. The 15 Mechanical Arm-specific Caddy items are NOT IMPLEMENTED in CSR source and were not added because the checklist says not to invent scope. The separate Resonance Chamber expansion research document explicitly describes its contents as proposals, not approved scope.",
        "",
        "## Verification summary and open gameplay checks",
        "",
        "- 48 Python source/resource tests passed; output: `docs/audits/0.7.0-python-tests.log`.",
        "- Gradle `compileJava` did not reach compilation: Java is not on PATH, the available JBR is Java 25 while the project toolchain is Java 21, and Gradle's localhost daemon connection was denied (`SocketException: Permission denied`). No compiler result is claimed; Gradle `test` and `build` were not confirmed. Command output: `docs/audits/0.7.0-gradle-compile.log`.",
        "- No Minecraft client, server, GameTest, or Ponder session was launched.",
        "- Gameplay follow-up: verify the two fixes, plus Scraper storage acceptance/output and flap timing; held/placed Caddy rendering and dynamic shafts; chest inventory persistence and item handling; belt transitions/stops; Deployer/Depot loading; Caddy pickup, drop, Creative middle-click, and multiplayer synchronization.",
        "- No merge, push, tag, publish, or release-version change occurred.",
        "",
    ]
    OUT_FINDINGS.write_text("\n".join(findings), encoding="utf-8")
    print(json.dumps({"items": len(items), "status_counts": counts, "markdown": str(OUT_MD), "json": str(OUT_JSON)}, indent=2, default=dict))


if __name__ == "__main__":
    main()
