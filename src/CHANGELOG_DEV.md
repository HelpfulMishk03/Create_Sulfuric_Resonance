# Development Changelog

This file retains implementation details and development fixes that are too granular for player-facing release notes. The public changelog remains in `CHANGELOG.md`.

## 0.7.0 — Moving With Purpose

### Added

- Added `CSR_070_Moving_Contraption_Demo.nbt` as a compact Create Mechanical Bearing rotation layout carrying a Scraper, Block Caddy, and Perforated Spritzer; kept a separate piston-based full CSR block payload test. Added a beginner guide and a registry-complete compatibility matrix.
- Audited Create's `Contraption` capture, `MovementContext`, mounted-storage, and movement-setting APIs. Create already serializes block-entity NBT and transforms block states during disassembly, so no CSR-wide relocation mixin was added. Active CSR machine simulation on moving structures remains unsupported and is itemized by machine.
- Added Rotor Weather Shrouding attachment state to the Molten Rotor Furnace, rain protection, and rain runoff particles at the canopy eaves.
- Added stationary Caddy cargo rendering on Depots, with cargo retained while parked.
- Added Deployer loading for Caddies moving underneath in Use mode, alongside direct block loading by right-clicking a belt-carried Caddy.
- Added direct block loading for empty Block Caddies placed on the ground; one held block is stored per interaction.
- Added empty and filled Block Caddies as placeable decorative blocks, preserving and displaying cargo when placed or picked back up.
- Added the 3D belt-carried Block Caddy, placed directly onto belt segments and rendered as a fixed model with cargo fitted inside its rails.
- Added the IPI recipe using iron bars and Create iron sheets; use a block on the belt segment carrying an empty Caddy to load it.
- Added the Scraper machine, its Hopper + dried kelp + Cut Metal crafting recipe, and the Create sawing recipe that yields two Cut Metals from an iron sheet.
- Added the data-driven Caddy processing recipe type and per-carrier belt hold state for Create Press, Saw, Spout, Encased Fan, and Combustion Belt processing. A carrier pauses independently while its belt keeps moving, completes one block operation, then resumes. Stacked Caddies split at a valid station so one timer transforms one carrier and the remainder queue behind it.
- Added processing recipes for Stone → Stone Bricks, Copper Block → Cut Copper, Oak Log → Stripped Oak Log, Concrete Powder → Concrete by Spout or fan washing, Clay → Terracotta, Stone → Smooth Stone, Wet Sponge → Sponge, and Ashesil → Tempered Ashesil. Spout recipes reserve their water requirement and consume it only when the cargo transformation completes; matching block-state properties are copied to compatible outputs.
- Added Scraper output handling for empty and loaded Caddies. Loaded cargo goes into adjacent item storage when it can accept the full stack; otherwise the Caddy waits on the belt.
- Added the hidden Redline advancement and overload: at 2000°C, inserting TNT while a linked Thermal Relay Switch is active starts a five-second fuse and forces the overload even when the normal TNT explosion option is disabled.
- Precision Spritzer now sprays selected cargo blocks carried inside belt-mounted Block Caddies; it applies the registered sulfuric acid conversion when one exists.

### Changed

- Corrected the Caddy item render cargo frame: ItemRenderer's display transform already applies the centered -0.5 model shift, so the cargo renderer now draws in block-local coordinates. This addresses the repeated side-offset report for GUI and belt-rendered Caddies.
- Processing now holds matching Caddies in a per-station retry queue instead of letting them pass during Create's trigger window. Queued carriers serialize behind the active one using Create's front-to-back belt inventory order, avoiding float-position ties when several Caddies stop at the same station. The press recipe completes on the server tick Create's pressing animation ends. Saw and Spout Create feedback remain enabled, while fan rotors and Combustion Belts retain native motion and receive matching processing particles.
- Reworked Caddy Ponder timelines with explicit placed block examples and short non-overlapping captions. Added a Scraper scene showing intact storage cargo and ordinary cargo separation.
- Corrected the Scraper scoop collision transform using the X/Y/Z pivot origins from `models/block/scraper.json`; previous outline math interpreted the origin coordinates at the wrong indices.

- Lowered belt Caddies so their grips seat into the belt, aligned cargo flush with the support feet, centered them across belt width without item side-drift, and narrowed the carrier slightly to prevent adjacent Caddies overlapping.
- Centered block cargo using its baked model bounds in placed Caddies, while retaining item-model rendering for fluid pipes and valve handles.
- Corrected the ItemRenderer cargo frame: nested Caddy cargo now shares the centered coordinate origin used by its fixed item model, fixing pipes and other cargo models appearing in a rear corner.
- Retextured the Caddy atlas with a gunmetal, steel, and Create-copper palette while preserving the existing 32x32 UV and transparency layout.
- Reworked the Caddy tooltip with Create's standard hold-Shift hint and expanded belt, Depot, and Deployer instructions; raised its GUI model by one pixel.
- Render cargo with its saved block-state properties, including shaft orientation, and use current belt lighting in belt, held, and inventory views.
- Adjusted first- and third-person held Caddy scale and cargo display size across belt-carried, parked, decorative, and inventory models.
- Reorganized Caddy and Resonant Bar recipes under `recipe/crafting`, and removed unused Scraper height variants in favor of the updated model.
- Updated the Scraper model and made its rear connector extend into inset chest fronts while remaining flush against full-width storage blocks such as vaults.
- Matched the Weather Shrouding back and left exterior UV islands to the right-side gunmetal shade and removed warm copper, yellow, and orange atlas pixels. Synced the updated atlas into the Blockbench model without changing UV coordinates.
- Reduced the Scraper flap angle to 80% of Create's funnel curve to keep the rubber strips clear of the rear wall; lowered the scoop collision outline by one model pixel; staged empty Caddy and oak plank item examples on the hand-loading Ponder floor.

### Fixed

- Remapped the Block Caddy's game model UVs to the current Blockbench atlas and rendered belt Caddies at their segment position without item spin or stack jitter.
- Smoothly interpolated Caddies across belt transfers and rendered them at their current position when a belt stops, preventing transfer snaps and stopped-belt shaking.
- Synced the painted Block Caddy texture into game resources so the runtime model uses the supplied atlas.
- Restricted Block Caddy placement to one per belt segment and fixed cargo loading or Precision Spritzer conversion removing the Caddy from the belt.
- Fixed Deployer loading by handling belt-carried Caddies in Create's belt-deployer callbacks, before the recipe-only processing check; the Deployer consumes one held block in Use mode.
- Fixed the Scraper's invisible placed model by explicitly enabling its baked block model.
- Shifting and right-clicking a shrouded Molten Rotor now returns the shrouding item in both Survival and Creative; a full inventory drops the returned item nearby.
- Precision Spritzer recognizes selected Caddy cargo as a valid target even without a conversion, and updates the cargo item when a registered conversion exists.
- Resonance Chamber Audio OFF now suppresses the completion chime while leaving completion particles enabled.
- Restored the moving reaction platform in the JEI and EMI Resonance Chamber recipe previews.
- Fixed Resonance Chamber shift-click insertion for valid recipe ingredients.
- Removed the incompatible Netty version override from development runs, restoring single-player world connections with NeoForge's platform-provided networking libraries.

### Development verification notes

- The registry-driven generator found 58 unique CSR block registrations. Both schematic NBTs passed local structural validation (dimensions, palette references, unique in-bounds positions, controller blocks, and DataVersion 3955); the full test includes the 57 non-fluid registrations. Sulfuric Acid is excluded from the moving payload because its block uses `PushReaction.DESTROY`.
- Copied both NBTs into the development run's `schematics` directory. Schematic Table discovery/upload, bearing/motor assembly and rotation, piston assembly/power, rendering during motion, save/reload, disassembly, multiplayer synchronization, and machine state behavior remain **not verified in game**. Both controller layouts still need a Create UI run before calling either schematic functional.
- The current Create dependency is 6.0.10-281. Compatibility with the stated Create 6.0.7 minimum was not runtime-tested. The demo contains a Mechanical Bearing layout and the full test contains a Mechanical Piston layout; neither was exercised in a game session. Gantry, Rope Pulley, and Train movement paths were source-audited only.

- `compileJava --offline --no-daemon` succeeded after the station queue, press-animation completion, Ponder scene, and English-language updates. This confirms compilation and JSON parsing only; repeat-line behavior and Ponder visuals still need in-game confirmation.
- The supplied client log reported missing Ponder structures for `block_caddy/hand_loading.nbt` and `block_caddy/chest_cargo.nbt`, plus an invalid `minecraft:air` ItemStack. Added dedicated 5x5 Ponder templates for hand loading and chest cargo, a separate Deployer and Belt template for factory loading, and translations for every Caddy scene. This fixes the missing-resource cause in source; runtime loading remains unverified.
- The supplied build log reported a missing `Player` import in `MoltenRotorBlockEntity`; the import was added. A follow-up Gradle attempt could not initialize `native-platform.dll` and stopped before project compilation, so compilation remains unverified.
- The current source does not register Block Caddies as Mechanical Arm interaction points. Mechanical Arm support for Caddies is therefore not claimed in the public notes.
- Ponder registration, chapter visibility, and scene behavior require runtime validation. Do not claim these as verified from source or resource processing alone.
- Scraper output behavior with no adjacent item-handler capability is intentional in current source: it ejects ordinary cargo and an empty Caddy as world items, then removes the belt entry. When a handler exists but cannot fit the complete output, the Caddy stays locked and waits. Confirm this distinction with Sol before release documentation or behavior changes.
- `models/block/thermal_clutch_geometry.json` contains faces with an undefined `#0` texture variable, but a source search found no references to this geometry file. Treat it as a possibly unused template; verify whether the model loader ever selects it before calling this a visible defect or changing it.
- Release hardening still needs gameplay checks for Weather Shrouding save/load and removal, cargo preservation/duplication across belt and Depot transfers, dedicated-server initialization, and mixin compatibility. A source or JAR review alone cannot prove those runtime paths.
- Create's native structure capture can carry CSR block states and saved block-entity data, but the current source has no CSR-specific mounted behavior for Caddies or active machine simulation on moving structures. The rotating demo only exercises Create's native movement and saved-block carrying once tested; it does not establish mobile CSR processing.
- Caddy processor integration, per-block timing, output state preservation, and Spout fluid handling are implemented in source and recipe data but have not been verified in a running game. In particular, confirm single-carrier pausing/spacing, machine animation feedback, saved progress across world reloads, fan airflow matching, and heated-belt behavior in game.
