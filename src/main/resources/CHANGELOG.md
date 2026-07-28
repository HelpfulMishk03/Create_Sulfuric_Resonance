# Changelog
**Copyright © 2026 Hxney, Ł. All Rights Reserved.**
Hxney and Ł are separate public creator identities used by the same developer and copyright owner.

All notable changes to **Create: Sulfuric Resonance** are documented here.

This project is still in beta. Older entries are reconstructed from surviving builds, development notes, testing records, and the restored source history. They describe the major development progression rather than claiming a perfectly complete commit-by-commit record.

## [0.0.2.3-betaA] - Public Beta Candidate

### Added

#### Molten Rotor Furnace

- Restored and substantially expanded the Molten Rotor Furnace as the mod's central kinetic heat source.
- Added temperature-driven rotational generation and stress-capacity output.
- Added multiple heat tiers spanning unheated, normal combustion, superheated, extreme, and radiant operation.
- Added smooth heat-up, cooldown, pre-spin, shaft, and impeller transitions.
- Added Create-goggle information for temperature, heat tier, RPM, stress output, active fuel, remaining burn time, cooling time, and queued fuel.
- Added direct empty-hand status readouts for important operating information.
- Added persistent serialization for active fuel, queued fuel, temperature, heat state, creative mode, and inserted fuel-item models.
- Added fuel queues so excess accepted fuel is preserved instead of silently deleted.
- Added queued-fuel drops when the furnace is broken.
- Added exact inserted-item rendering so compatible modded fuel items retain their own appearance.
- Added distinct visible layouts for coal-like fuels, logs, wooden rods, TNT, Blaze Cakes, and Soul-Fired Blaze Cakes.
- Added heat-dependent flame, smoke, kindling, radiant, shaft, impeller, and heat-gauge presentation.
- Added rain-sensitive heating and cooling behavior.
- Added a low-volume universal insertion sound for successful manual insertion.
- Added invalid-item interaction blocking so side clicks no longer activate or place blocks through the furnace.

#### Fuel and heat systems

- Added centralized fuel resolution through compatibility classes instead of scattering fuel rules throughout the block entity.
- Added support for Minecraft coal tags, common wooden rods, ATM10 tiny coals, and common coal coke.
- Added or expanded support for sticks, wooden rods, logs, planks and compatible wood fuels, coal, charcoal, coal blocks, dried kelp blocks, and modded fuel profiles.
- Preserved EvilCraft reinforced-undead wood compatibility while explicitly rejecting invalid dark-stick behavior.
- Added TNT as an unstable high-energy fuel with an optional explosion mechanic.
- Added Dragon's Breath as a radiant heat-duration boost.
- Added Nether Stars as an extended ultimate heat-duration boost.
- Added Blaze Cake and Soul-Fired Blaze Cake progression.
- Added creative Blaze Cake heat-tier cycling without ordinary fuel consumption.
- Prevented normal fuel insertion while creative heat mode is active.

#### Automation

- Added an insertion-only NeoForge item capability for the Molten Rotor Furnace.
- Added hopper, funnel, chute, pipe, and generic item-handler insertion support.
- Added Create Mechanical Arm deposit support.
- Routed manual and automated insertion through the same validation, capacity, and queue rules.
- Prevented automated extraction from the furnace.
- Added Mechanical Arm interaction support for Rubber Padding.

#### Combustion Mixing and JEI

- Added a dedicated Combustion Mixing recipe category built on Create Basin processing.
- Added Molten Rotor heat requirements for heated, superheated, and combustion-level recipes.
- Added Mechanical Mixer, Basin, Molten Rotor Furnace, and Soul-Fired Blaze Cake catalysts where appropriate.
- Added item and fluid inputs and outputs.
- Added stochastic output-chance tooltips.
- Added heat-condition labels and corrected non-superheated label colors.
- Restored the missing item-output loop.
- Removed duplicate fluid-output slot registration.
- Added an animated Molten Rotor category display.

#### Sulfur chemistry

- Added Sulfur and Sulfur Blocks.
- Added Sulfuric Acid fluid, flowing fluid, block, and bucket.
- Added Acid Burn for living entities exposed to Sulfuric Acid.
- Added acid fog and drip-particle behavior.
- Added water-contact smoke and extinguishing reactions.
- Added lava reactions that convert source lava to obsidian and flowing lava to stone.
- Added combustion-based Sulfuric Acid processing recipes.

#### Rubber processing

- Added Latex Clumps.
- Added Lye.
- Added Netherwood Dust washing/splashing progression for producing Lye.
- Added Unrefined Rubber and Vulcanized Rubber processing stages.
- Added Molded Rubber Gaskets.
- Added Rubber Padding.
- Added Obsidian Fiber and Obsidian Fiber Molds.
- Added Sheathed Impeller Blades and Infernal Impellers.
- Added configurable Rubber Padding bounce behavior for entities and items.
- Added reduced fall damage, progressive item settling, particles, sounds, and Mechanical Arm interaction for Rubber Padding.

#### Perforated Spritzer

- Restored the Perforated Spritzer block and block entity.
- Added fluid capability registration.
- Added custom block rendering, shape, interaction behavior, particles, and sounds.
- Added Perforated Spritzer Ponder content.

#### Pyroclast content

- Added Pyroclastic Powder.
- Added the Pyroclast Bomb.
- Added a custom projectile entity and explosion behavior.
- Added Create Potato Cannon integration.
- Added Ember Catalyst, Spent Ash, Blaze Shards, Embersol, Reinforced Cinder Compound, and Flameborne Core.
- Added related high-temperature processing and washing recipes.

#### Presentation and project identity

- Added a dedicated creative tab.
- Added a custom project logo and mod-list branding.
- Added item models, blockstates, partial models, particles, sounds, names, and tooltips for restored and newly added content.
- Added copyright, authorship, licensing, and redistribution notices.
- Added a public-facing README, changelog, and release documentation.

### Changed

- Reworked visual RPM behavior so the Molten Rotor begins at ambient temperature and scales predictably with furnace temperature.
- Reworked inserted-stick rendering into paired leaning wooden rods.
- Reworked visible fuel limits independently from internal queue capacity.
- Reworked Blaze Cake placement, flame height, smoke behavior, radiant presentation, and high-temperature visuals.
- Reworked interaction messages so unnecessary insertion messages were removed while useful rejection feedback remains.
- Reworked item model transforms for dropped Molten Rotor and Perforated Spritzer presentation.
- Reworked client registration to keep rendering classes out of dedicated-server initialization.
- Reworked compatibility code into dedicated compatibility packages.
- Updated the development environment to Java 21, Minecraft 1.21.1, NeoForge 21.1.243+, Create 6.0.10, Flywheel 1.0.6, Ponder 1.0.x, and supported JEI 19.x builds.
- Updated metadata to version `0.0.2.3-betaA` and author `Hxney`.

### Fixed

- Fixed dedicated-server crashes caused by loading client-only classes.
- Fixed missing generated Mixin refmap packaging.
- Fixed Molten Rotor fuel and impeller visuals disappearing around 300°C.
- Fixed incorrect shaft/impeller facing and rotation behavior across block orientations.
- Fixed fuel queue restrictions that prevented compatible fuels from being queued behind an active fuel.
- Fixed creative mode still accepting ordinary fuel.
- Fixed Blaze Cake and Soul-Fired Blaze Cake floating or clipping in the renderer.
- Fixed stick orientation and placement on both sides of the furnace.
- Fixed excess fire height and particle density at several heat levels.
- Fixed missing or altered smoke-slit behavior.
- Fixed dropped-block model positioning issues under review.
- Fixed furnace side-click pass-through behavior.
- Fixed log-only insertion audio by replacing it with a universal insertion sound.
- Fixed missing Combustion Mixing item outputs.
- Fixed duplicate Combustion Mixing fluid-output slots.
- Fixed incorrect non-superheated JEI heat-label colors.
- Fixed multiple raw types, redundant casts, unused declarations, duplicated code blocks, and registration warnings.
- Fixed multiple block, entity, item, capability, and recipe registration errors encountered during source restoration.

### Testing

- Completed a 416-item interactive test plan.
- Recorded 412 passes, 0 failures, and 4 final-review items during the principal beta validation pass.
- Tested in a large NeoForge modded environment containing Create and numerous Create addons.
- Tested manual insertion, automated insertion, world reloads, breaking behavior, high heat, rain behavior, particles, sounds, JEI, Ponder, client launch, and dedicated-server startup.

### Known issues

- Some JEI 19.39.0.379+ environments may show blank fluid textures in Combustion Mixing slots even though the recipe, tooltip, and fluid amount are registered correctly.
- Balancing, recipe progression, visuals, naming, and broad mod compatibility may change during beta.
- Tag-based support is broader than before but does not guarantee compatibility with every modded fuel.

## [0.0.2.1-betaA through 0.0.2.1-betaE] - Source Restoration Baseline

### Added and restored

- Reconstructed the active development source from the author's surviving compiled release and project materials after an extended hiatus.
- Restored core registrations for blocks, items, block entities, fluids, effects, entities, sounds, particles, recipes, creative tabs, and Ponder.
- Restored the Molten Rotor Furnace block, block entity, renderer, and heat behavior.
- Restored the Perforated Spritzer, Rubber Padding, Sulfuric Acid, and Pyroclast systems.
- Restored Create, Flywheel, Ponder, Registrate, and JEI development integration.

### Fixed

- Fixed Java 21 toolchain configuration.
- Fixed Ponder plugin registration syntax and dependency versions.
- Fixed NeoForge event-bus registration and deprecations.
- Fixed dedicated-server initialization failures.
- Fixed block-entity capability registration errors.
- Fixed entity-renderer generic mismatches.
- Fixed recipe serializer and recipe-type generic issues.
- Fixed numerous renderer regressions and restored a stable visual baseline.

## [0.0.1.9-beta] - Earlier Surviving Beta

- Earlier compiled beta used as one source of reference during restoration.
- Included the original foundation of the Molten Rotor Furnace, sulfur chemistry, rubber content, Perforated Spritzer, and Pyroclast systems.
- The modern source is not a byte-for-byte decompilation of this build; it has been repaired, reorganized, expanded, and independently maintained by the original author.

## Project history note

Create: Sulfuric Resonance is developed and maintained by **Hxney**. Approximately eight months of active development led to the `0.0.2.3-betaA` public-beta candidate, excluding an approximately one-year interruption caused by unforeseen life emergencies.
