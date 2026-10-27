# Development Changelog

Implementation details and verification records for Create: Sulfuric Resonance. Player-facing release notes are in `CHANGELOG.md`.

## 0.7.0 — Moving With Purpose — Finalized 2026-10-27

- Decoupled Thermal Battery kinetic output and fault status from thermochemical shaft presence. Batteries can now run standalone or drive ordinary Create shafts; the thermochemical interface remains responsible for heat-network connections.

### Release acceptance

- The player confirmed that the final two outstanding issues are working: mobile Living Ember Lamp illumination on SABLE airships and the Combustion Belt heat overlay. The player accepted 0.7.0 as complete.
- Earlier player confirmations cover natural Caddy movement, processor cycle holds, Scraper pickup, Thermal Battery shaft placement, Deployer loading, the fourth Caddy Ponder, SABLE advancement triggers, and the Thermochemical Boiler Interface.
- The older investigation notes below are retained as history. Their pending or unresolved labels describe those earlier builds and do not reopen the accepted release.

### Block Caddies and cargo

- Added reusable belt carriers, direct loading of a clicked Caddy, Use-mode Deployer loading, Depot parking, and decorative placement of empty or filled Caddies. Decorative displays support blocks and ordinary items.
- Retained the approved full-size carrier geometry, centered GUI presentation, cargo alignment, and peg feet seated into the belt.
- Cargo rendering preserves placed block state and orientation, multipart block halves, saved block-entity data, dynamic kinetic details, and supported special renderers. Corrected item-icon substitution for placeable cargo, offset or duplicated geometry, and missing GUI and belt cargo in modded render paths.
- Cargo outline and collision calculations use saved block state and block-entity data. Cached results reduce repeated complex geometry work; SABLE's synthetic collision queries use the synchronized shape ID in Caddy block state.
- Guarded nested Caddy cargo against recursive shape evaluation and stack overflow. Retained carried-block break sounds and added the optional Carry On blacklist to prevent unsupported pickup from stripping the cargo renderer.
- Supported storage cargo uses preserved inventory NBT, sneak-right-click access, opening and closing sounds, and lid animations interpolated with render partial ticks.

### Processing and extraction

- Added data-driven Caddy processing for Create Presses, Saws, Spouts, Fans, and Deployers, plus CSR Combustion Belts and Precision Spritzers.
- Normal belt transport moves the Caddy into a station. Removed custom machine-center movement, transfer corrections, and client stop prediction that caused snapping or apparent acceleration.
- Per-station queues hold carriers through the full processing animation. Pressed cargo changes at contact and remains held until the Press returns home. Spritzers count actual recipe spray contacts before transforming cargo and finish their spray cycle before release.
- Spout processing waits for the required fluid. Recipe transformations preserve the carrier and update its cargo rather than replacing the Caddy with an output item. Combustion treatment continues with ordinary belt travel.
- Scraper pickup uses the scoop on the segment it owns, captures moving carriers reliably, and separates ordinary cargo from empty Caddies. Loaded storage cargo retains its contents; insufficient output capacity holds the carrier.

### SABLE, trains, and machinery

- Added optional sublevel actor support and plot-aware links for CSR thermal systems, controls, monitoring, and processing machinery.
- Mobile Ember Lamps project their existing heat-derived light into the parent world and each nearby receiving SABLE plot. Sources use the stored plot coordinates with SABLE's pose transforms, which already account for the rotation pivot.
- Temporary projected sources are restricted to the client light engine. Movement and tier changes refresh them; removing a lamp restores native ambient lighting. Source placement does not create missing chunks or write persistent auxiliary lights.
- Fixed belt heat deserialization by injecting at every RETURN from Create `BeltBlockEntity.read`. The previous TAIL hook skipped normal full-NBT loads and unchanged-casing update packets. Serialized heat is retained during loading while server processing continues to calculate live heat.
- Belt goggles report Unheated, Heated, Superheated, or Combustion from received heat data. Player confirmation closes the remaining SABLE tooltip report.
- Retained Rotor Furnace heat and fuel across assembly and reinitialized generated rotation after disassembly. Living Ember Lamp functionality on moving Create trains was player-confirmed.
- Thermal Battery shaft clicks allow placement without sneaking; the top and underside present matching geometry.
- Corrected Boiler Interface north-center port selection and array refresh behavior. The player confirmed the boiler system across SABLE conditions.
- Occupational Hazard and What Are You Doing resolve the player's transformed position in the appropriate SABLE block view. The player confirmed advancement triggering on an airship.
- Fixed Process Monitor targeting and arming for sublevel-stored machinery. Its guidance identifies the Resonance Chamber and Rotary Leacher as supported process targets.
- Restored Resonance Chamber JEI/EMI platform animation, completion-audio toggle handling, and shift-click ingredient insertion. Corrected Combustion Belt recipe-display tooltip layering.
- Removed the incompatible development Netty override.

### Assets, tutorials, and localization

- Added Rotor Weather Shrouding, rain protection, runoff particles, and the final gunmetal texture pass.
- Added Cut Metal and the Scraper recipes, including dried kelp in the Scraper recipe and two Cut Metals from a sawn iron sheet.
- Added the hidden Redline challenge and overload sequence at 2000 degrees C with TNT and an active linked Thermal Relay Switch.
- Populated Caddy and Scraper Ponder NBT with the signature floors, demonstration blocks, cargo, and block-entity data. Scenes reveal saved blocks without replacement break particles or duplicate floors.
- Five Caddy chapters include Precision Spritzer conversion of Exposed Copper cargo into a Copper Block and a Saw/Spout processing demonstration. The final message in the factory-loading scene has a longer display time.
- Added a second Scraper chapter showing ordinary belt items intentionally passing through without collection. Both scenes use saved Ponder structures and translated captions.
- Added Polish localization and expanded Ponder, tooltip, and Process Monitor translations. Final key-parity check: all ten locale files contain the same 987 keys as `en_us.json`, with no missing or extra keys.
- Aligned carried-block collision and selection bounds to the same inset and scaling used by the rendered cargo, including two-block-tall halves.
- Replaced deprecated block-renderer, baked-model, item-light, recipe-count, and explosion-resistance APIs; removed a redundant renderer cast. `compileJava` passes with deprecation lint clean. Full `-Xlint:all` still identifies intentional constructor-time references used by Minecraft block/entity/menu initialization.

### Final verification record

- Ran copied-world development clients using Minecraft 1.21.1, NeoForge 21.1.259, Create 6.0.10, and SABLE 2.0.6. The original development world was not modified.
- Reproduced the belt failure before changing the read hook. Afterwards, client and server heat agreed at RADIANT -> SMOULDERING -> RADIANT. Full-NBT reconstruction and unchanged-casing update packets preserved each tier. The visible tooltip displayed Combustion.
- Verified the mobile lamp and separate receiving airship plot at light 13 -> 7 -> 13, with neighboring light 12 -> 6 -> 12. Removing the lamp returned the recipient plot to its native light level of 6. Captured in-game screenshots and passed 16 recorded runtime checks.
- The normal final `jar` build succeeded. Packaged classes matched the compiled outputs, the temporary playground harness was absent, and the build included revision `scene-light-4-full-nbt`.
- The player subsequently confirmed both final fixes working in their own run. This acceptance closes the two outstanding release reports.
- The copied-world checks exercised the development environment, not a new ATM10 launch or every historical compatibility scenario. Earlier source-only and archival schematic audits retain their original evidence level.

## Historical 0.7.0 development record

The following sections preserve intermediate implementation and investigation notes. Statements such as "pending", "unresolved", or "unverified" refer to the build being investigated at the time. The final release acceptance and verification record above are authoritative for the completed work.

## 0.7.0 — Moving With Purpose & Contraption Compatibility with Sable

### Added

- Added `CSR_070_Moving_Contraption_Demo.nbt` as a compact Create Mechanical Bearing rotation layout carrying a Scraper, Block Caddy, and Perforated Spritzer; kept a separate piston-based full CSR block payload test. Added a beginner guide and a registry-complete compatibility matrix.
- Audited Create's `Contraption` capture, `MovementContext`, mounted-storage, and movement-setting APIs. Create already serializes block-entity NBT and transforms block states during disassembly, so no CSR-wide relocation mixin was added. Active CSR machine simulation on moving structures remains unsupported and is itemized by machine.
- Added Rotor Weather Shrouding attachment state to the Molten Rotor Furnace, rain protection, and rain runoff particles at the canopy eaves.
- Added stationary Caddy cargo rendering on Depots, with cargo retained while parked.
- Added Deployer loading for Caddies moving underneath in Use mode, alongside direct block loading by right-clicking a belt-carried Caddy.
- Added direct block loading for empty Block Caddies placed on the ground; one held block is stored per interaction.
- Added empty and filled Block Caddies as placeable decorative blocks, preserving and displaying cargo when placed or picked back up.
- Caddy cargo rendering now draws the stored placed block state directly, captures a placement-facing state when loaded by hand, and renders double-block halves together. Removing the item-model and unconditional block-entity-renderer overlay prevents off-center duplicate cargo such as Mekanism Generators windmills. Standard Caddy collision now derives from the carried block's collision shape, including the upper half of double-block cargo. Added the optional-safe `carryon` block blacklist tag for Caddies.
- Added the 3D belt-carried Block Caddy, placed directly onto belt segments and rendered as a fixed model with cargo fitted inside its rails.
- Added the IPI recipe using iron bars and Create iron sheets; use a block on the belt segment carrying an empty Caddy to load it.
- Added the Scraper machine, its Hopper + dried kelp + Cut Metal crafting recipe, and the Create sawing recipe that yields two Cut Metals from an iron sheet.
- Added the data-driven Caddy processing recipe type and per-carrier belt hold state for Create Press, Saw, Spout, Encased Fan, and Combustion Belt processing. A carrier pauses independently while its belt keeps moving, completes one block operation, then resumes. Stacked Caddies split at a valid station so one timer transforms one carrier and the remainder queue behind it.
- Added processing recipes for Stone → Stone Bricks, Copper Block → Cut Copper, Oak Log → Stripped Oak Log, Concrete Powder → Concrete by Spout or fan washing, Clay → Terracotta, Stone → Smooth Stone, Wet Sponge → Sponge, and Ashesil → Tempered Ashesil. Spout recipes reserve their water requirement and consume it only when the cargo transformation completes; matching block-state properties are copied to compatible outputs.
- Added Scraper output handling for empty and loaded Caddies. Loaded cargo goes into adjacent item storage when it can accept the full stack; otherwise the Caddy waits on the belt.
- Added the hidden Redline advancement and overload: at 2000°C, inserting TNT while a linked Thermal Relay Switch is active starts a five-second fuse and forces the overload even when the normal TNT explosion option is disabled.
- Precision Spritzer now sprays selected cargo blocks carried inside belt-mounted Block Caddies; it applies the registered sulfuric acid conversion when one exists. Added a fourth Caddy Ponder scene for Exposed Copper becoming a Copper Block.
- Added Sable to the development compile classpath while retaining its runtime launch dependency alongside Create Aeronautics.

### Changed

- Fixed Combustion Belt heat deserialization on every return from Create `BeltBlockEntity.read`, including full chunk NBT and unchanged-casing update packets. The prior TAIL injection skipped both paths, leaving the client at NONE while the server was RADIANT. Retain serialized heat on full NBT loads; server processing continues to resolve live heat each tick.
- Reworked mobile Ember Lamp illumination to project each heat-derived source into ordinary world coordinates and every nearby SABLE receiver plot. Use stored plot coordinates directly with SABLE pose transforms, which already subtract their rotation pivot. Temporary sources are confined to the client light engine, update when the lamp moves or changes tier, and are removed when the lamp disappears. Avoid auxiliary-light writes and never create missing chunks.

- Combustion Belt display resolution permits a connected heat-chain lookup on the client, with synchronized heat data remaining the primary display source.
- Occupation Hazard and What Are You Doing now resolve the player's transformed position into Sable's embedded block view before checking crucibles, furnace heat, and combustion belt survival.
- Sable builds collider geometry from a synthetic state-only getter with no block-entity data. Caddies now encode an ID for the computed cargo collision shape in block state, allowing Sable's state-keyed collider cache to retain each cargo shape. If the shape table is exhausted or has not rebound yet, cargo uses a full-block collision fallback. Normal-world collision continues using the exact saved block-state shape, with authored outlines for modded blocks lacking a collision shape and both halves of double blocks.
- Corrected the Caddy item render cargo frame: ItemRenderer's display transform already applies the centered -0.5 model shift, so the cargo renderer now draws in block-local coordinates. This addresses the repeated side-offset report for GUI and belt-rendered Caddies.
- Processing arrival now records a matching station before movement, skips Create's built-in processing callback for that Caddy, and lets BeltInventory apply its normal movement step. The Caddy is captured on the next tick at its actual belt position; CSR no longer assigns a machine-center position during arrival. Per-station queueing, press contact timing, and holding through the machine animation remain in place.
- Reworked the Scraper scene around its saved checkerboard template and aligned belt travel with the displayed direction. Caddy and Scraper scenes reveal serialized structure blocks and cargo directly, avoiding setup-time block replacement and its break particles. Added a Precision Spritzer structure with exposed and clean copper examples, Caddy cargo NBT, acid, and belt block-entity data.
- Added the missing English translation for Process Monitor Ponder operation text 10 and documented the currently supported machines in the scene.
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

- Ran isolated copied-world clients with Create 6.0.10, NeoForge 21.1.259, and SABLE 2.0.6. Reproduced the belt failure before the RETURN hook correction, then verified client/server tiers agree at RADIANT -> SMOULDERING -> RADIANT. Full-NBT reconstruction and unchanged-casing update reads preserve each tier. Verified the loose mobile lamp and the separate airship receiver scene both follow light 13 -> 7 -> 13, with neighboring light 12 -> 6 -> 12. Source removal restores the recipient scene to its native ambient light. The original development save was not modified; ATM10 was not launched in this check.

- The registry-driven generator found 58 unique CSR block registrations. Both schematic NBTs passed local structural validation (dimensions, palette references, unique in-bounds positions, controller blocks, and DataVersion 3955); the full test includes the 57 non-fluid registrations. Sulfuric Acid is excluded from the moving payload because its block uses `PushReaction.DESTROY`.
- Copied both NBTs into the development run's `schematics` directory. Schematic Table discovery/upload, bearing/motor assembly and rotation, piston assembly/power, rendering during motion, save/reload, disassembly, multiplayer synchronization, and machine state behavior remain **not verified in game**. Both controller layouts still need a Create UI run before calling either schematic functional.
- The current Create dependency is 6.0.10-281. Compatibility with the stated Create 6.0.7 minimum was not runtime-tested. The demo contains a Mechanical Bearing layout and the full test contains a Mechanical Piston layout; neither was exercised in a game session. Gantry, Rope Pulley, and Train movement paths were source-audited only.

- `compileJava --offline --no-daemon` succeeded after the station queue, press-animation completion, Ponder scene, and English-language updates. This confirms compilation and JSON parsing only; repeat-line behavior and Ponder visuals still need in-game confirmation.
- `compileJava --no-daemon` succeeded after disabling Mixin AP remapping for the client `BlockLightEngine#getEmission` hook, which had no mapping in the current annotation-processor setup. This confirms compilation only; Sable lamp brightness, Caddy belt behavior, Scraper pickup, and Ponder visuals and particles still need in-game confirmation.
- `compileJava --offline --no-daemon` succeeded after adding Sable player-position resolution and the first belt-tooltip/cargo collision fallbacks. Player testing confirmed the Sable advancement fix but reported that the tooltip and cargo collision still failed. The client-only resolver guard and Sable's state-keyed collider input were then traced and corrected; revised overlay and collision changes still need runtime verification.
- Structural inspection found 28 blocks each in the hand-loading and chest-cargo templates (25 floor blocks plus three examples), 36 in factory loading (25 plus 11), 34 in Precision Spritzer (25 plus nine), and 36 in Scraper (25 plus 11). The structures include Caddy cargo NBT, oak-log axis state, chest contents, belt block-entity data, Scraper, and Spritzer. Ponder registration and playback remain unverified in game.
- The supplied build log reported a missing `Player` import in `MoltenRotorBlockEntity`; the import was added. A follow-up Gradle attempt could not initialize `native-platform.dll` and stopped before project compilation, so compilation remains unverified.
- The current source does not register Block Caddies as Mechanical Arm interaction points. Mechanical Arm support for Caddies is therefore not claimed in the public notes.
- Player-reported Sable verification passed for the Molten Rotor Furnace, Rotor Weather Shrouding, Sulfur Burner, Catalyst Bed, Sulfuric Resonance Chamber, Rotary Leacher, Ash Ceramic Crucible, Scraper, Thermal Battery, Thermal Link Drive, Thermal Relay Switch, Heat Gauge, both Gearbox variants, thermochemical network blocks, Process Monitor, Process Gauge, Thermal Alarm, Resonant Heat Injector, and Spritzers.
- The user separately confirmed the Thermochemical Boiler Interface across Sable conditions, including its north-center shaft face on 1x1 and 2x2 arrays. Caddy processing was confirmed to hold cargo through processor and Spritzer animations and to wait for the machine cycle before resuming; the new natural belt-arrival capture still needs in-game confirmation.
- The Scraper candidate search validates that its belt segment is directly below the Scraper and only considers items within that segment's half-block scoop window. It checks the current and preceding belt positions for a scoop crossing; direct pickup behavior remains unverified in game.
- Living Ember Lamp light mapping now detects Sable plot membership from the stored plot chunk, transforms the local block position through the plot pose, and adds the emission at that parent-world position. The previous implementation used hidden storage coordinates. This correction remains unverified in game.
- Ponder templates contain their floors, cargo blocks, and block-entity data; hand-loading and chest-cargo scenes now use the saved blocks instead of replacing them at setup. The Spritzer and Scraper structures are also populated. Registration, chapter visibility, break-particle behavior, captions, and rendered playback remain unverified in game.
- Create Train belt movement, Rotor Furnace goggles on assembled trains, and thermal-block appearance during nighttime train assembly remain open runtime reports; no fix is claimed here.
- Scraper output behavior with no adjacent item-handler capability is intentional in current source: it ejects ordinary cargo and an empty Caddy as world items, then removes the belt entry. When a handler exists but cannot fit the complete output, the Caddy stays locked and waits. Confirm this distinction with Sol before release documentation or behavior changes.
- `models/block/thermal_clutch_geometry.json` contains faces with an undefined `#0` texture variable, but a source search found no references to this geometry file. Treat it as a possibly unused template; verify whether the model loader ever selects it before calling this a visible defect or changing it.
- Release hardening still needs gameplay checks for Weather Shrouding save/load and removal, cargo preservation/duplication across belt and Depot transfers, dedicated-server initialization, and mixin compatibility. A source or JAR review alone cannot prove those runtime paths.
- Create's native structure capture can carry CSR block states and saved block-entity data, but the current source has no CSR-specific mounted behavior for Caddies or active machine simulation on moving structures. The rotating demo only exercises Create's native movement and saved-block carrying once tested; it does not establish mobile CSR processing.
- The user confirmed processor and Spritzer cargo holding through machine animation. For arrival, the belt mixin now cancels only Create's processing callback while returning control to its outer tick, allowing the ordinary movement step to run. Pending Caddies are then captured at their actual position without writing `beltPosition`; this change is not yet in-game verified. Recipe output preservation, Spout fluid persistence, fan airflow matching, heated-belt behavior, and save/reload remain separate checks.

- Release follow-up: bypass native belt processing for every Caddy, including empty and unmatched carriers; Scrapers retain independent position samples across belt ticks. Corrected the Precision Spritzer and Scraper structure size/position tags from integer arrays to integer lists (35 and 37 fields). Registered transformed Ember Lamp emissions with NeoForge auxiliary light managers. compileJava and jar succeeded; gameplay remains unverified.

- Follow-up: Sable lamp actor now requests light recomputation from the plot-owned light engine; parent-world light updates alone do not refresh sublevel lighting sections. Battery placement bypass now covers held BlockItems rather than a shaft-class subset. Scraper Ponder stores both extracted cargo and the empty Caddy, triggers its flap animation, and the fourth Caddy scene leaves a gap between spray-point and acid-contact captions. compileJava and jar succeeded. Lamp illumination and battery interaction remain runtime-unverified; early Scraper pickup and machine snapping remain unresolved.

- Player confirmed Thermal Battery shaft placement fixed. Corrected the Scraper final caption in both English locale files; translated Ponder keys override Java fallback text.

- Scoped follow-up: removed custom rendered transfer-position correction and client stop prediction; Scraper pickup now occurs toward the exit edge of its own segment. Lamp resolves a relocated loaded furnace by its preserved identity before treating the original location as unloaded. Added 70 translated Ponder entries across seven locales. All nine language files have identical key sets and zero checked placeholder mismatches. Gameplay validation remains outstanding.


## Archived detailed public notes for 0.7.0

Preserved when the player-facing notes were condensed. These entries record development changes, not additional gameplay verification. The finalized release record above takes precedence over historical fix wording and verification status below.

## 0.7.0 — Moving With Purpose & Contraption Compatibility with Sable

### Added

- Added a Create Mechanical Bearing rotation demo and full CSR block test schematic, plus a first-time placement guide and a compatibility matrix covering all 58 registered CSR blocks.
- Documented which machine state is carried by Create's normal contraption capture and which CSR processing or thermal-network behaviors pause while assembled.
- Added the reusable Block Caddy system: carry placeable blocks on belts, load them by hand or with a Use-mode Deployer, and park them on Depots.
- Added decorative placement for empty and filled Caddies; cargo is preserved when placed and picked back up.
- Added the Scraper and Cut Metal, including crafting and sawing recipes. The Scraper separates ordinary cargo from its Caddy, keeps storage blocks loaded, and waits when connected storage cannot accept its output.
- Added block-only processing for Caddies at Create presses, saws, Spouts, and Encased Fans, plus heat treatment on Combustion Belts. A matching Caddy pauses for one block's recipe time while the belt keeps moving, then continues with its transformed cargo.
- Added Caddy recipes for pressing Stone into Stone Bricks and Copper Blocks into Cut Copper; sawing Oak Logs into stripped logs while preserving axis; washing Concrete Powder into Concrete at Spouts or fans; heating Clay and Stone in fan airflow; drying Wet Sponges; and tempering Ashesil on a sufficiently heated Combustion Belt.
- Added removable Rotor Weather Shrouding for the Molten Rotor Furnace, blocking rain exposure and shedding visible runoff droplets during storms.
- Added Precision Spritzer processing for selected cargo blocks carried inside belt-mounted Caddies when a sulfuric acid conversion is registered.
- Added Ponder walkthroughs for hand loading, Deployer loading on a moving belt, chest storage, and Scraper output handling, plus a fourth Caddy scene showing a Precision Spritzer transforming Exposed Copper cargo into a Copper Block.
- Added the hidden Redline advancement: inserting TNT at 2000°C with an active linked Thermal Relay Switch starts a five-second overload fuse, even when the normal TNT explosion option is disabled.

### Changed

- Retextured the Caddy and refined its belt movement, cargo fit and centering, held and inventory scale, and lighting for consistent presentation.
- Updated Caddy belt rendering to draw the carrier and cargo together in block space, avoiding item display transforms that could misplace or hide cargo in modded render environments such as ATM10.
- Kept belt Caddies at near-full block size by bypassing the small fixed-display item scale; the carrier feet remain seated into the belt surface.
- Added optional Sable sublevel ticking for the Thermal Battery, Thermal Relay Switch, Living Ember Lamp, and Process Monitor, plus plot-aware lookup for Sable-stored process and furnace links.
- Caddy processing now triggers Create's press stroke, saw feedback, and Spout filling animation. Pressed cargo changes at tool contact, and its Caddy stays held until the Press returns home.
- Loaded Caddies can use matching Create Deployer recipes on their carried block; the Caddy stays held through the Deployer cycle and only one block of cargo transforms per activation.
- Matching Caddies keep moving with the belt until they naturally reach a processing station, then stay held through the machine's processing animation.
- Shaft-item clicks on the Thermal Battery now bypass its GUI interaction so normal shaft placement can proceed without sneaking.
- Rotor Furnace runtime kinetic initialization is rebuilt after a saved block entity is restored, preserving its saved heat and fuel state across contraption disassembly.
- Reworked the Caddy Ponder scenes to show the example blocks clearly, with shorter instructions timed to avoid overlap.
- Updated Process Monitor Ponder guidance to identify the currently supported machines: the Sulfuric Resonance Chamber and Rotary Leacher.
- Added the missing English translation for Process Monitor Ponder operation text 10.
- Restricted Scraper pickup to Caddies reaching or crossing the scoop point on the belt segment directly beneath it.
- Mapped Living Ember Lamp emission from Sable plots to each lamp's transformed position in the parent client light engine.
- Clarified Caddy tooltips and instructions.
- Refined Rotor Weather Shrouding with a consistent gunmetal shade across the back, left, and right outer panels, removing copper, yellow, and orange accents.
- Updated the Scraper crafting recipe to use dried kelp.

### Fixed

- Player verified: Thermal Battery shaft placement works without sneaking.

- Fixed cargo alignment in Caddy GUI and belt renders by using the same block-local model coordinates as the carrier.
- Fixed filled Caddy collision shapes being missed by Sable ship physics by synchronizing cargo presence with the Caddy's block state, including when saved Caddies load.
- Added Sable's dedicated sublevel collision-shape hook for filled Caddies so cargo collision follows the carried block during physics simulation.
- Fixed Process Monitor arming lookup for blocks stored in Sable plots and kept normal-world reach checks for ordinary blocks.
- Filled Caddies now use the carried block's break sound when broken.
- Fixed matching Caddies passing a busy processing station by queuing arrivals and advancing them in belt order after the previous operation completes.
- Corrected the Scraper scoop voxel outline to use the model's actual rotation pivot.
- Rebuilt the Caddy and Scraper Ponder structures with their checkerboard floors, demonstration blocks, cargo, and block-entity data. Scenes reveal the saved structures directly so setup does not replace blocks or emit break particles.
- Mirrored the Thermal Battery's complete top assembly onto its underside so either orientation presents the same face.
- Allowed Create shafts to be placed on the Thermal Battery's shaft interface without opening its GUI.
- Kept the Thermochemical Boiler Interface's north-center shaft face visible in Sable physics for 1x1 and 2x2 arrays.
- Restored the Resonance Chamber's moving reaction platform in JEI and EMI previews, and made its Audio OFF setting suppress the completion chime.
- Fixed Resonance Chamber shift-click insertion for valid recipe ingredients.

- Assembly-retention follow-up: lamp NBT now preserves emitted and target light instead of restoring a zero target after relocation. Scraper pickup additionally requires the carrier world center inside the Scraper-owned belt square. Removed the remaining belt-transfer recording hook. compileJava and jar passed; no gameplay verification claimed.

- Loading/contact follow-up: OptionalSableMixinPlugin now gates only mixins whose simple name starts with Sable; ordinary belt and renderer mixins always apply. Optional API detection checks class resources without linking Sable classes during mixin selection. Scraper capture no longer uses offset crossing history or an exit-edge offset: current carrier world position must be within 0.20 blocks along the belt and 0.50 across it from the Scraper-owned segment center. This includes Caddies inserted near the scoop. compileJava and jar passed. These source changes are not gameplay-verified.

### Runtime investigation: contact-light-2
- Reproduction log confirms server pickup within 0.044 blocks of the Scraper segment center; no evidence of a remote server pickup in that event. Lamp block state remains at light level 13 after assembly.
- Added a once-per-game-tick server belt guard because the custom Sable actor tick can overlap ordinary plot block-entity ticking. A one-time diagnostic reports a prevented duplicate. This targets server/client movement divergence without changing pickup geometry or approved Caddy models.
- If retained lamp brightness exceeds actual propagated plot light, initialize the plot light section and propagate its sources. Diagnostics now report actual plot block light, rather than only the saved brightness property.
- Gameplay results remain unverified pending another development-client reproduction. No resolved public release claim.
### Ember Lamp client light sections (2026-10-24)
- User verified Caddy motion and Scraper behavior fixed. Preserve those implementations.
- Runtime light diagnostics show expected=13, propagated=13 on the Sable server plot. Remaining visible lighting failure is downstream of server propagation.
- Client lamp sources now allocate block-light storage for otherwise empty projected world sections, enable their source columns, and register both the plot and projected world positions. Removed sources restore the actual section emptiness status.
- Added client propagated-light diagnostics and report optional API lookup failure once. Visual confirmation remains pending; do not mark Ember Lamp Sable lighting verified.
### Mobile lamp projection correction
- Client diagnostics showed projected light coordinates millions of blocks from the actual lamp while both plot engines reported light 13. The manual conversion incorrectly subtracted the plot center before passing the position into a pose that already handles that pivot.
- Replaced manual pose conversion with installed Sable ActiveSableCompanion.projectOutOfSubLevel(Level, Vec3), passing the unchanged stored block center. Verified the helper's installed bytecode uses this convention and returns stationary positions unchanged.
- Caddy and Scraper implementations unchanged. Visual mobile lighting still requires runtime confirmation.
### Train verification and Rotor Furnace restoration
- User verified Living Ember Lamp functionality on a moving Create train. Sable lamp illumination remains unresolved.
- Rotor Furnace kinetic initialization now reapplies generated rotation after attaching and runs on the first server tick if the scheduled initialization was lost during disassembly. Saved heat/fuel are preserved; restarting no longer depends on a heat-tier transition in source.
- Added client lamp neighbor-light and player-position diagnostics to distinguish retained source light from useful nearby illumination. Do not claim the Sable visual issue fixed.
- User observes Create's own belts/cogwheels share the nighttime train darkness; track as shared rendering behavior rather than a confirmed CSR-specific regression.
### Public changelog status refresh (2026-10-24)
- Promoted Caddy snapping and early Scraper pickup to player-confirmed fixes.
- Recorded player-confirmed Living Ember Lamp operation on moving Create trains.
- Kept SABLE lamp illumination unresolved and Rotor Furnace restart after train disassembly awaiting gameplay confirmation.
- Recorded shared nighttime train lighting behavior without claiming a CSR-specific cause or fix. Historical public version entries preserved.
### Living Ember Lamp active core rendering
- User clarified the remaining Sable symptom is the lamp itself appearing dark. Nearby active source diagnostics already report propagated light 13 with neighboring light 12.
- The rotating Embersol renderer previously used the environmental packed light unchanged. It now uses full-bright rendering while LIGHT_LEVEL is above zero, so the active light source does not darken with assembled-structure lighting. Unlit core rendering and the metal frame retain ordinary lighting.
- This is a renderer-only correction. Caddy, Scraper, train furnace, and light propagation behavior were not modified in this pass. Build verification does not replace visual confirmation.
### Ember Lamp heat-dependent core correction
- Replaced the nonzero/full-bright renderer override with packed block light derived from the existing LIGHT_LEVEL. The same renderer serves placed, Create contraption, and Sable modes.
- Existing furnace mapping remains NONE=0, FADING/SMOULDERING=7, KINDLED/SEETHING=11, RADIANT=13. Rendering consumes the resulting state after existing fades and low-fuel/redline pulses, without duplicating furnace logic.
- Ambient block and sky light remain respected. This preserves ordinary environmental shading while providing the heat-derived minimum core light when assembled lighting is dark.
- Corrected the public full-bright note. Runtime visual verification remains pending.

### Follow-up: belt heat sync and SABLE Caddy collision fallback (2026-10-26)
- User reported that the belt heat tooltip and cargo collision were still broken after the earlier changes.
- Belt heat data is now sent on every authoritative 20-tick heat scan, including when the tier is unchanged, so recreated SABLE client belt block entities can recover their server-synced tier.
- When a Caddy collision query has no block entity, the regular collision path now falls back to the cargo shape keyed by the Caddy blockstate, matching SABLE's synthetic getter behavior.
- `compileJava --offline --no-daemon --console=plain` succeeded. Both behaviors still need in-game confirmation; compilation does not establish a runtime fix.
