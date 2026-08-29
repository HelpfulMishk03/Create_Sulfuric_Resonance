# Changelog
**Copyright © 2026 Hxney, Ł. All Rights Reserved.**
Hxney and Ł are separate public creator identities used by the same developer and copyright owner.

All notable changes to **Create: Sulfuric Resonance** are documented here.

Older entries are reconstructed from surviving builds, development notes, testing records, and the restored source history. They describe the major development progression rather than claiming a perfectly complete commit-by-commit record.

# Create: Sulfuric Resonance 0.4.0 -- Reactive Tools

0.4.0 expands CSR's field equipment and advanced resonance infrastructure with three completed throwable-item passes and the new Catalyst Bed for the Sulfuric Resonance Chamber.

## Added

### Catalyst Bed

* Added the Catalyst Bed as a physical upgrade placed directly beneath a Sulfuric Resonance Chamber.
* An installed Catalyst Bed advances live Chamber processing at 1.5× normal speed without changing recipe files, displayed recipe durations, ingredients, heat requirements, RPM requirements, acid use, automation, or operating modes.
* Only the Catalyst Bed immediately beneath a Chamber applies; additional Beds cannot stack the acceleration.
* Installing or removing the Bed during a reaction changes the processing rate without resetting completed progress.
* The Chamber continues to operate normally without a Catalyst Bed.
* Added disconnected and connected model states using the finished Catalyst Bed atlas.
* The raised connector appears only while a valid Chamber is directly above and remains visually compatible with the Chamber's moving reaction platform.
* Collision and interaction geometry remain entirely inside the Catalyst Bed's own block space.
* Added Engineer's Goggle status for active and missing Catalyst Beds.
* Added a shaped crafting recipe using Resonant Iron Sheets, Iron Bars, and an Activated Sulfur Catalyst.
* Added block loot, pickaxe and tool-tier tags, an item model, creative-tab placement, and the **Resonant Foundation** advancement.
* Added a dedicated Ponder scene covering placement, connection visuals, acceleration, live removal, and non-stacking behavior.

### Cinder Flare

* Added a staged two-second ignition action using Flint and Steel in the off hand.
* Added synchronized ignition sounds, sparks, flame feedback, and dedicated lit and unlit item states.
* Added throwable lit flares with persistent flame presentation and multiplayer-aware entity behavior.

### Sulfuric Acid Flask

* Added throwable Sulfuric Acid Flasks with dedicated projectile rendering and synchronized impact behavior.
* Flask impacts apply Acid Burn and remove one oxidation stage from up to three copper blocks.
* Added water interaction, impact cleanup, localized tooltips, and entity presentation.

### Pyroclast Bomb Overhaul

* Rebuilt hand throwing around a deliberate short wind-up and arcing projectile trajectory.
* Added controlled impact demolition of up to five blocks, moderate combat damage, restrained knockback, and a limited ignition chance.
* Water now extinguishes a Pyroclast Bomb so it can be recovered.
* Added dedicated detonation sound and subtitle coverage, pyroclastic fragments, smoke, flame, flash, and debris feedback.
* Added Create Potato Cannon ammunition support alongside normal hand throwing.

### Localization

* Added complete Catalyst Bed names, tooltips, advancement text, Goggle status, and Ponder documentation across all eight supported locales.

## Changed

### Item Textures

* Updated the Sulfuric Acid Bucket texture.

## Fixed

### Catalyst Bed Presentation

* Replaced the static connected-state sleeve with a rendered connector that extends and retracts with the Sulfuric Resonance Chamber platform.
* Corrected the render-pass order so Sulfuric Acid blends over the sleeve instead of depth-hiding it during processing.
* Corrected first-person, third-person, GUI, dropped-item, and item-frame transforms for the Catalyst Bed item model.
* Rebuilt the hover outline from the finished model's individual voxel bounds instead of using a broad box through the machine.
* Switched the binary-alpha Catalyst Bed model from translucent blending to cutout rendering so selection highlighting cannot reveal the floor through opaque machinery.

### Thermochemical Boiler Output

* Fixed connected Thermochemical Link Drives inheriting one three-block heat span when every Link Drive was directly attached to its own active Steam Engine powered shaft.
* Each direct boiler-powered shaft connection now acts as a local thermochemical heat source for its attached Link Drive.


----------------


# Create: Sulfuric Resonance 0.3.1 — Industrial Pulse

**Industrial Pulse** is a focused machinery-feedback and presentation update.

Rather than introducing another major progression system, 0.3.1 brings existing machinery to life through stronger mechanical animation, responsive audio, state-driven lighting, restrained particles, smoother transitions, and clearer physical feedback.

Alongside this machinery-feedback pass, Industrial Pulse introduces the **Thermochemical Boiler Interface**, allowing CSR thermochemical networks to directly heat Create steam boilers through automatically connected heater arrays ranging from a single Interface to a complete 3×3 installation.

## Added

### Thermochemical Boiler Interface

* Added the Thermochemical Boiler Interface for powering Create steam boilers from CSR thermochemical networks.
* Supports automatically connected arrays of 1–9 Interfaces within Create’s 3×3 boiler-heating footprint.
* Touching Interfaces automatically merge into one controllerless shared array without a GUI or linking tool.
* One thermochemical shaft connection can supply heat to the entire connected array.
* Thermochemical Boiler Interfaces draw thermochemical heat from the connected network without consuming or transmitting kinetic Stress Units.
* Array size scales boiler output with diminishing returns while retaining Create’s normal boiler size, water-supply, and Steam Engine limits.
* Higher thermochemical heat tiers provide increasingly powerful boiler heating beyond the normal Blaze Burner heating ceiling.
* Multiple thermochemical shaft connections on the same array cannot stack heat or duplicate boiler output.
* Added automatic connected-side manifold models that close internal faces between adjacent Interfaces.
* Added one configurable shaft-interface face for each array.
* Standalone Interfaces begin with a visible north-facing shaft port.
* Smaller arrays support any valid exposed shaft-interface face.
* Complete 3×3 arrays restrict the shaft interface to the center position of an outer edge.
* Wrenching an exposed face moves the array’s shaft interface to that face.
* Wrenching the currently selected face removes the shaft interface.
* Added protection against a boiler-fed thermochemical network recursively supplying heat back into the same boiler.
* Added Engineer’s Goggle information for array size, validity, heat tier, temperature, selected input, boiler connection, and target output.
* Added Steam Engine Goggle information for thermochemical heat, temperature, RPM, and generated Stress Units.
* Added a dedicated Ponder scene covering Interface arrays, shaft-port selection, boiler heating, Steam Engine assembly, and boiler-fed thermochemical output.
* Added the **Boiling Point** advancement for crafting a Thermochemical Boiler Interface.
* Added a shaped crafting recipe using Iron Sheets, a Copper Sheet, Thermochemical Casings, and a Thermochemical Shaft.

### Thermochemical Clutch

* Added a dedicated animated mechanical locking assembly.
* Added smooth engagement and release movement.
* Added dedicated engagement and release sounds.
* Added directional lock placement so the mechanism appears on the actual interrupted or output side of the Clutch.
* Added separate physical timing for lock movement and mechanical impact feedback.

### Sulfur Burner

* Added a complete combustion-feedback sequence across the existing five-second warmup period.
* Added a dedicated ignition whoosh when a cold Burner accepts fuel.
* Added unstable combustion cracking during early warmup.
* Added continuous industrial combustion ambience while operating.
* Added a dedicated fuel-depletion extinguish effect.
* Added staged sulfur flame, cinder, smoke, and combustion particles.
* Added progressive Brimstone Core illumination during warmup.
* Added an ignition flash when combustion begins.
* Added a stabilization pulse when full heat is reached.
* Added a lingering Brimstone Core afterglow after shutdown.

### Sulfuric Resonance Chamber

* Added a physically animated reaction platform.
* The platform now rests below its operating position and rises as the Chamber becomes engaged.
* Added distinct platform positions for insufficient heat and insufficient rotational speed.
* Completed output keeps the reaction assembly physically latched until the output is removed.
* Added READY-state ring movement and restrained standby illumination.
* Added state-driven ring behavior for insufficient heat and insufficient speed.
* Added dedicated Chamber startup audio with different behavior for normal and Resonance-class reactions.
* Added dedicated READY, lock-in, release, insufficient-speed strain, and missing-acid feedback sounds.
* Added a per-machine **Audio** toggle to the Chamber interface.
* Added startup and release particle pulses.
* Added restrained resonance particles during active processing.
* Added subtle READY-state particles.
* Added completion-pulse feedback.
* Added smooth visual cooldown after processing instead of immediately returning the Chamber to a cold state.

### Localization

* Added complete Russian (`ru_ru`) localization.

* Added complete Simplified Chinese (`zh_cn`) localization.

* Expanded CSR to **8 fully supported languages**:

  * American English (`en_us`)
  * British English (`en_gb`)
  * German (`de_de`)
  * Spanish (`es_es`)
  * French (`fr_fr`)
  * Brazilian Portuguese (`pt_br`)
  * Russian (`ru_ru`)
  * Simplified Chinese (`zh_cn`)

* Updated every supported locale for the complete 0.3.1 text set.

* Added localized subtitles for all new Industrial Pulse audio.

* Preserved formatting placeholders, numerical values, technical identifiers, registry tags, and Create-specific terminology across translations.

* Reviewed Russian and Simplified Chinese translations for semantic accuracy rather than relying on overly literal wording.

## Changed

### Thermochemical Clutch

* Redstone continues to interrupt rotation and thermochemical heat immediately while the physical lock follows the state transition visually.
* Refined lock geometry and positioning for proper clearance between moving and stationary components.
* Rebalanced engagement and release audio so engagement reads as the heavier mechanical action.
* Improved powered-state readability without changing the Clutch’s mechanics, balance, or automation behavior.

### Sulfur Burner

* Reworked the KINDLED → SEETHING warmup so it reads as a developing combustion process.
* Early warmup behaves as unstable combustion before gradually becoming a controlled industrial burn.
* Combustion cracking becomes less prominent as the Burner stabilizes.
* Main Burner ambience builds alongside warmup progress.
* Fully heated combustion is calmer and more stable than the startup phase.
* Continuous queued fuel no longer repeatedly triggers the ignition sequence between individual fuel items.
* Reloading an already-burning Sulfur Burner no longer produces a false ignition event.
* Shutdown feedback occurs only when the Burner actually exhausts its available fuel.

### Sulfuric Resonance Chamber

* Reworked Chamber state presentation so its operating condition is substantially more readable without relying entirely on the GUI.
* Normal reactions now use a deliberate startup envelope before reaching full visual intensity.
* Resonance-class reactions use a longer, heavier startup profile with distinct ring-speed behavior.
* Ring speed develops alongside reaction progress instead of switching directly to full operation.
* READY, insufficient-heat, insufficient-speed, processing, completed, and output-blocked states now have distinct physical behavior.
* Insufficient rotational speed produces periodic mechanical strain feedback.
* Missing sulfuric acid produces a dry mechanical response.
* Completion and shutdown behavior settle progressively instead of visually snapping back to idle.
* Existing Automatic and Manual operating mechanics remain unchanged.

### Molten Rotor Furnace

* Replaced the previous harsh persistent machinery sound with a dedicated industrial rotor rumble.
* Rebalanced Rotor rumble volume across heat tiers.
* Increased its audible presence at normal Minecraft volume settings.
* Preserved temperature-dependent pitch and volume progression.
* Preserved smooth audio fade-in and fade-out as the Furnace heats and cools.
* Existing Rotor visual and mechanical behavior otherwise remains unchanged.

## Fixed

### Thermochemical Clutch

* Fixed visible Z-fighting in the locking assembly.
* Fixed the locking component appearing on the incorrect side in some orientations.
* Fixed overlapping surfaces in the moving lock geometry.

### Sulfur Burner

* Fixed normal operating combustion audio being too quiet.
* Fixed short hiss audio repeatedly restarting during continuous operation.
* Fixed the fuel-depletion sound behaving like persistent ambience instead of a one-shot shutdown effect.
* Fixed false ignition feedback when loading a Burner that was already operating.

### Sulfuric Resonance Chamber

* Prevented Chamber startup and completion effects from falsely replaying when loading an already-running machine.
* Improved synchronization between logical process state and client-side animation state.
* Prevented startup audio from continuing after processing stops or the Chamber is no longer present.
* Improved transition handling between processing, completion, blocked output, READY, and idle states.
* Preserved the per-machine Audio setting across world saves and reloads.

## Industrial Pulse Consistency Pass

* Audited machinery feedback across every completed 0.3.1 system.
* Normalized transition behavior so visual feedback follows real machine state rather than operating independently from it.
* Kept persistent particles restrained to prevent normal factories from becoming visually excessive.
* Kept startup, shutdown, and failure effects event-driven where appropriate.
* Checked feedback behavior across world reloads, chunk reconstruction, and client reconnection to prevent false machine events.
* Audited registered blocks, items, blockstates, models, textures, recipes, loot tables, advancements, sounds, Ponder structures, mixins, metadata, and localization resources.
* Verified complete localization-key parity and formatting-placeholder consistency across all eight supported languages.
* Preserved existing redstone, processing, thermochemical-network, and automation mechanics throughout the polish pass.


----------------



### 0.3.0 — Intelligent Industry

0.3.0 turns thermochemical information into factory behavior.

This update focuses on:
- process monitoring
- intelligent control
- precision spraying
- utility chemistry
- tighter Create-style automation
- major polish and fixes across existing CSR systems

---

## Added

### Intelligent Industry

* Shared process-state system:
  * `IDLE`
  * `READY`
  * `PROCESSING`
  * `BLOCKED`
* Persistent process identities for linked machines.
* Process Monitor with five independent machine channels.
* Process Gauge with selectable Monitor channels.
* Process Gauge redstone output:
  * `READY` = 5
  * `BLOCKED` = 15
  * all other states = 0
* Logic Bank item.
* Shared Intelligent Industry logic framework.
* Thermal Warning Alarm with:
  * striker animation
  * alarm sound
  * network status indication
  * redstone output
* Cross-dimensional thermochemical network resolution for supported linked devices.

### Thermochemical Clutch

* Added the Thermochemical Clutch.
* Redstone controls both:
  * rotational transmission
  * thermochemical heat transmission
* Independent shaft halves allow the driven side to continue rotating while the output side is stopped.

### Precision Spraying

* Added the Precision Spritzer as an upgrade to the Perforated Spritzer.
* Item filtering.
* Entity filtering.
* Searchable registry-backed filter lists.
* Scrollable entries.
* Multiple simultaneous selections.
* Persistent filter selections.
* Selected Only view.
* Item and Entity filters can operate simultaneously.
* Modded entity types are supported by the registry-backed filter system.
* Dedicated Precision Spraying JEI category.
* Dedicated Precision Spraying EMI support.

### Chemistry

* Added Superphosphate Fertilizer.
* Create Mixing recipe support.
* Broad crop compatibility through `BonemealableBlock`.
* Added sulfuric-acid copper deoxidation:
  * Oxidized Copper → Weathered Copper
  * Weathered Copper → Exposed Copper
  * Exposed Copper → Copper
* Each oxidation-stage reduction requires three successful sulfuric-acid contacts.

### Sulfuric Resonance Chamber

* Added Manual and Automatic operating modes.
* Chamber defaults to Automatic.
* Added Manual start control.

### Ponder

* Added or expanded Ponder coverage for:
  * Process Monitor
  * Process Gauge
  * Thermal Warning Alarm
  * Thermochemical Clutch
  * Precision Spritzer
  * Rubber Padding
  * Sulfuric Acid
  * Intelligent Industry
  * Fluid Handling
  * Sulfur Chemistry

---

## Changed

### Process Monitoring

* Process Monitor now owns all machine-channel assignments.
* Process Gauge reads one of five channels from a linked Process Monitor.
* Gauge channel switching no longer changes Monitor bindings.
* Process Monitor and Process Gauge floor placement and orientation were improved.

### Redstone and Control

* Process Gauge now outputs:
  * `READY` = 5
  * `BLOCKED` = 15
  * all remaining states = 0
* Thermal Relay Switch modes are now fully separated:
  * Custom Heat
  * Low Fuel
* Low Fuel scope now controls both active-fuel and final-cooldown warnings.

### Create Integration

* Thermal Gauges can share a Factory Gauge panel block with Create Factory Gauges in separate quadrants.
* Mixed Factory Gauge / Thermal Gauge hosts now preserve their complete panel state across:
  * world save and reload
  * client reconnect
  * chunk unload and reload
* Mixed gauge hosts now reconstruct their Thermal Gauge client state when a chunk is sent back to the player.
* Ordinary standalone Create Factory Gauges retain Create's native block-entity behavior and are no longer globally replaced by CSR gauge hosts.
* Thermal Gauge placement beside existing Factory Gauges now uses the shared panel host without replacing unrelated gauge data.
* Thermochemical Gearbox can now be rotated with a Wrench.
* Parallel Thermochemical Gearbox can now be rotated with a Wrench.
* Sneak-right-clicking a Thermal Gauge with a Wrench now picks it up while preserving its stored thermochemical connection.

### Precision Spritzer

* Empty-hand right-click opens the filter interface.
* Held items and blocks retain normal placement/use behavior.
* Item and Entity tabs now choose which filter list is being edited.
* Sulfuric acid is only consumed when a valid selected target exists.
* Copper deoxidation now affects only the directly exposed block beneath the Spritzer.

### Thermochemical Network

* Living Ember Lamp and related network behavior now support cross-dimensional thermochemical-network resolution.
* Relevant Ponder documentation was updated accordingly.

### Organization and Compatibility

* Creative-tab ordering was regrouped around:
  * machinery
  * thermochemical infrastructure
  * Intelligent Industry
  * chemistry
  * fuels
  * materials
  * rubber
  * ceramics
  * construction
* Sulfuric Resonance Chamber geometry now remains fully inside the standard `16×16×16` block volume.
* NeoForge minimum requirement lowered from `21.1.247+` to `21.1.238+`.
* Minecraft compatibility metadata now correctly targets Minecraft `1.21.1` only.

---

## Fixed

### Process Monitor / Gauge

* Linked process machines now enter persistent `ERR` when broken or replaced.
* Replacement machines at the same position no longer silently inherit old bindings.
* Gauge channel switching fixed.
* Gauge drum interpolation fixed.
* READY/BLOCKED redstone output fixed.
* Wall and floor placement fixed.
* Selector orientation fixed.
* Monitor linking fixed in both Survival and Creative.
* Process Gauge advancement renamed to **Condition to Signal** to avoid overlap with **Heat, Delivered**.

### Thermal Gauge / Factory Gauge Integration

* Fixed mixed Thermal Gauge / Factory Gauge panels becoming invisible after leaving and re-entering a world.
* Fixed mixed gauge hosts losing their client-side Thermal Gauge representation after chunk reload.
* Fixed Thermal Gauges remaining stored in the world but failing to render after reconnecting.
* Fixed client-side shared-host conversion using an unattached block entity.
* Fixed a client synchronization error that could throw:
  * `this.level is null`
  * `thermal_gauge_host` payload processing failures
* Fixed synchronization ordering when converting a Create Factory Gauge host into a mixed CSR gauge host.
* Fixed mixed-gauge state synchronization so the client receives the complete Factory + Thermal panel state.
* Fixed mixed hosts failing to reconstruct correctly when chunks are sent to the client.
* Fixed world-safety issues caused by globally replacing Create Factory Gauge block-entity types.
* Existing tuned Create Factory Gauges are now preserved when CSR Thermal Gauges are installed alongside them.
* Mixed gauge panels now survive:
  * save and reload
  * full client restart
  * chunk unload and reload
  * repeated placement and removal
* Gauge items stored in mixed hosts are preserved correctly when the supporting panel block is removed.

### Thermal Warning Alarm

* Network behavior cleaned up.
* Display behavior corrected.
* Final-cooldown warnings corrected.
* Ponder timing and explanations improved.

### Thermal Relay Switch

* Custom Heat and Low Fuel no longer execute simultaneously.
* Low Fuel behavior now follows its intended scope correctly.

### Thermochemical Clutch

* Live shaft rendering fixed.
* Shaft lighting fixed.
* Powered Ponder-side shaft rotation behavior corrected.

### Precision Spritzer

* Scrollbar behavior fixed.
* Search behavior fixed.
* Multi-selection fixed.
* Filter persistence fixed.
* Inventory-key typing no longer closes the selector.
* Item presentation fixed.
* Dedicated texture and model behavior corrected.
* Drop and pick-block behavior corrected.
* Precision Spraying JEI and EMI layouts cleaned up.
* Copper processing no longer affects whole vertical stacks.
* Copper processing now requires three real acid contacts per oxidation stage.

### Rendering and Models

* 3D machine-item glass transparency fixed with Continuity installed.
* Sulfuric Resonance Chamber upper geometry corrected.
* Chamber player-foot collision behavior corrected.
* Mixed Thermal Gauge / Factory Gauge rendering now restores correctly after world and chunk reloads.

### Ponder

* Missing translations repaired.
* Missing category hookups repaired.
* Scene structure coverage corrected.
* Outdated explanations updated.
* Living Ember Lamp cross-dimensional behavior corrected.
* Sulfur Burner Heated → warmup → Superheated explanation corrected.
* Chamber Automatic / Manual documentation added.

### Code Cleanup

* Large warning-cleanup pass completed.
* Deprecated API usages cleaned up where applicable.
* Nullability annotations corrected.
* Redundant local variables removed.
* Redundant inspection suppressions removed.
* Unused Thermal Gauge code removed.
* Duplicate code removed.
* Unused code removed.
* Redundant logic removed.

---

## Compatibility

### Required

* Minecraft **1.21.1**
* NeoForge **21.1.238+**
* Create **6.0.7+**
* Java **21**

### Optional

* JEI **19.42.0.387+**
* EMI **1.1.24+**

---

## Localization

Full localization-key parity is maintained for:

* English (US)
* English (UK)
* Spanish
* French
* German
* Portuguese (Brazil)


----------------



### 0.2.9-beta — Resonance

0.2.9 gives CSR's thermochemical infrastructure an advanced purpose by combining heat, rotation, sulfuric acid, specialized reagents, and automation into a new resonance-processing stage.

#### Added

* Sulfuric Resonance Chamber with a dedicated multi-input processing system.
* Custom Chamber recipes supporting:

  * Substrate
  * Optional Catalyst
  * Optional Auxiliary
  * Sulfuric Acid
  * Minimum heat
  * Minimum RPM
  * Processing time
  * Dedicated output
* Resonant Copper Ingot.
* Resonant Iron Ingot.
* Resonant Gold Ingot.
* Resonant Copper Sheet.
* Resonant Iron Sheet.
* Resonant Gold Sheet.
* Activated Sulfur Catalyst with a custom animated texture.
* Unfinished Thermal Matrix and Thermal Matrix progression.
* Resonant Heat Injector.
* Thermochemical Link Drive.
* Parallel Thermochemical Gearbox.
* Thermal Gauge with wrench interaction and linked-network information.
* Active Sulfuric Resonance Chamber rendering with progressive ring illumination, reaction-dependent ring acceleration, startup sequencing, completion peak, and smooth cooldown.
* Dedicated Sulfuric Resonance Chamber GUI with integrated recipe browsing.
* Full Sulfuric Resonance Chamber JEI category.
* Full Sulfuric Resonance Chamber EMI category.
* Animated EMI displays for:

  * Molten Rotor Fuels
  * Combustion Mixing
  * Combustion Belt
  * Sulfuric Resonance Chamber
* Sulfuric Resonance Chamber Ponder scene.
* Resonant Heat Injector Ponder scene.
* Thermal Gauge Ponder scene.
* Thermochemical Link Drive Ponder scene.
* Thermochemical Cogwheel Ponder scene.
* Large Thermochemical Cogwheel Ponder scene.
* Parallel Thermochemical Gearbox Ponder scene.
* Ash Ceramic Crucible Ponder scene.
* **Thermochemical Telemetry** advancement.
* **Material in Tune** advancement for producing a Resonant Metal.

#### Changed

* Molten Rotor Furnace automation now accepts funnels only from the top and back.
* Thermal Relay Switch, Living Ember Lamp, and Thermal Gauge now share the same linked thermochemical network system.
* Linked network information can be viewed while holding a connected network item or by hovering with a wrench.
* Thermochemical Conduit mining requirement reduced from an iron-tier pickaxe to a stone-tier pickaxe.
* Thermochemical Gearbox texture substantially updated.
* Parallel Thermochemical Gearbox provides its intended phase-reversed output behavior.
* Sulfuric Resonance Chamber automatically routes valid ingredients into their appropriate substrate, catalyst, and auxiliary slots.
* Chamber input automation now preserves recipe validity and rejects incompatible batch changes.
* Chamber inputs lock during active processing while still allowing manual removal to abort a reaction.
* Chamber recipe selection remains locked to the active batch during processing.
* Chamber GUI can be opened while holding ordinary non-placeable items instead of requiring an empty hand.
* Living Ember Lamp advancement description updated to distinguish remote network-state indication from Thermal Gauge telemetry.
* JEI and EMI recipe layouts received a complete presentation and alignment pass.

#### Fixed

* Fixed Thermochemical Gearbox rotation propagation.
* Fixed Parallel Thermochemical Gearbox rotation presentation.
* Fixed Thermochemical Gearbox shaft alignment and rendering.
* Fixed Thermochemical Conduit item model so its shaft is correctly baked into the inventory model.
* Fixed Sulfuric Resonance Chamber GUI access after Mechanical Arm insertion.
* Fixed Chamber recipe-viewer switching and contextual recipe locking.
* Fixed Chamber automated insertion and extraction rules.
* Fixed Chamber funnel-side restrictions.
* Fixed Chamber Mechanical Arm deposit and take behavior.
* Fixed Chamber item-slot validation and smart routing.
* Fixed Chamber processing state persistence and recipe locking.
* Fixed Chamber ring visuals snapping immediately back to idle after processing.
* Fixed Chamber ring startup, acceleration, peak-speed, and cooldown behavior.
* Fixed Chamber NORMAL and RESONANCE visual-speed distinction.
* Fixed Chamber rendering issues that could obscure internal machinery.
* Fixed Ashesil and Tempered Ashesil inventory transparency.
* Fixed missing and incorrect Ponder structures and scene rendering.
* Fixed Parallel Thermochemical Gearbox Ponder rotation directions.
* Fixed Thermochemical Cogwheel models not appearing in Ponder scenes.
* Fixed Ash Ceramic Crucible content not appearing correctly in its Ponder scene.
* Fixed untranslated shared EMI tags.
* Fixed JEI and EMI rendering, alignment, and recipe-display issues.
* Updated all advancement translations.
* Updated all supported language files and restored exact localization-key parity.

#### Compatibility

* Minecraft **1.21.1**
* NeoForge **21.1.247+**
* Create **6.0.7+**
* JEI **19.42.0.387**
* EMI **1.1.24+1.21.1**
* Java **21**

#### Localization

Complete localization coverage is maintained for:

* English (US)
* English (UK)
* Spanish
* French
* German
* Portuguese (Brazil)

#### Summary

Resonance completes the next major stage of CSR's thermochemical progression:

**Generate heat → transmit it → combine heat, rotation, acid, and reagents → resonate advanced materials → apply thermochemical heat directly to processing machinery.**

0.2.9 also completes a broad presentation and compatibility pass across JEI, EMI, Ponder, advancements, automation, localization, models, and Chamber rendering.



----------------




### 0.2.8-beta — Sulfurous Capabilities

0.2.8 expands the thermochemical infrastructure introduced in 0.2.7 with new sulfur-based fuels, heat sources, materials, and dedicated kinetic components.

#### Added

* Brimstone Core
* Sulfurous Fuel Compound
* Sulfur Fuel Briquette
* Tempered Ashesil
* Tempered Ashesil Pane
* Thermochemical Cogwheel
* Large Thermochemical Cogwheel
* Sulfur Burner
* Sulfur Burner Ponder scene

#### Changed

* Standard Create cogwheels no longer transmit thermochemical heat.
* Thermochemical heat transmission through cogwheels now requires dedicated Thermochemical Cogwheels.
* Minimum supported Create version is now **6.0.7+**.
* JEI support updated to **19.42.0.387**.

#### Fixed

* Updated language entries.
* Fixed JEI rendering issues.
* Additional compatibility and presentation fixes.



----------------


## [0.2.7-beta] — Reactive Heat

### Summary

Reactive Heat introduces a complete thermochemical transmission system centered around the Molten Rotor Furnace. Heat can now be generated, transmitted, routed, monitored, and consumed across an industrial network while expanding combustion progression, recipe integration, automation, and compatibility.

**CHANGED VERSIONING TO PROPER 0.2.7**

### Added

#### Thermochemical Network

- Added Thermochemical Shaft.
- Added Encased Thermochemical Shaft variants.
- Added Thermochemical Conduit.
- Added Thermochemical Gearbox.
- Added Thermochemical Casing.
- Added Combustion Belt.
- Added Thermal Relay Switch.
- Added complete thermochemical heat transmission, routing, monitoring, and belt-processing systems.
- Added complete Ponder scenes for all new thermochemical machinery.

#### Materials

- Added Obsidian Cloth.
- Added Unfinished Filament.
- Added Cinder Filament.
- Added Acid-Etched Copper Sheet.
- Added Wet Ash Ceramic.
- Added Unfired Ash Brick.
- Added Acid-Resistant Ceramic.
- Expanded ash and ceramic progression.

#### Molten Rotor Fuels

- Added Coke.
- Added Molten Ember Pellet.
- Added Infernal Coke.
- Added Carbon Deposit Block.
- Added Infernal Carbon Deposit Block.
- Added reversible storage recipes.

#### Recipe Viewers

- Added native JEI support.
- Added native EMI support.
- Added combined JEI + EMI compatibility.

#### Progression

- Added new advancements for thermochemical machinery, combustion processing, rubber progression, and material refinement.

### Changed

- Improved Living Ember Lamp behaviour and transitions.
- Improved Thermochemical network rules and routing.
- Improved Combustion Belt propagation.
- Improved Molten Rotor automation and fuel handling.
- Improved JEI and EMI layouts.
- Updated recipes throughout combustion and ceramic progression.
- Updated English (US), English (UK), Spanish, French, and German localization.
- Updated textures, models, and visual consistency.

### Fixed

- Fixed JEI/EMI compatibility issues.
- Fixed thermochemical routing issues.
- Fixed advancement trigger issues.
- Fixed rendering, particle, transparency, and Z-fighting issues.
- Fixed recipe viewer alignment.
- Fixed translation and model issues.
- Fixed Molten Rotor fuel synchronization and save compatibility.


----------------



## [0.2.6-beta] — Ashen Resonance

### Added

#### Ashen Resonance materials

- Added Ash Brick.
- Added Ash Ceramic.
- Added the Ash Brick Block.
- Added Ash Brick Stairs.
- Added the Ash Brick Slab.
- Added the Ash Brick Wall.
- Added the axis-oriented Ash Brick Pillar.
- Added the Ceramic Crucible.
- Added Ashesil Glass.
- Added Ashesil Panes.

#### Living Ember Lamp

- Added the Living Ember Lamp.
- Added furnace-linking behaviour for selecting and connecting a Molten Rotor Furnace.
- Added heat-responsive light output based on the linked furnace's current heat state.
- Added linked-coordinate information to the lamp item.
- Added connection validation so replacing a removed furnace at the same position does not silently inherit an old lamp link.
- Added Ponder guidance for linking and operating the lamp.

#### Create-compatible tools and fuels

- Added the Cinder Fuel Briquette.
- Added manual and automated furnace insertion support for the briquette.
- Added Create Mechanical Arm support and compatibility with the mod's existing supported transfer methods.
- Added the Cinder Sandpaper.
- Added Create-compatible polishing behaviour.
- Set Cinder Sandpaper durability to twice that of Red Sandpaper.

#### Advancements and localization

- Revamped the advancement tree.
- Added French localization.
- Added German localization.
- Updated existing language files for the new content.
- Expanded American English, British English, Spanish, French, and German coverage.

#### Presentation and audio

- Added custom sounds.
- Added tooltips for new and existing materials, components, and intermediate items.
- Added or updated item models, block models, blockstates, recipes, loot tables, tags, and creative-tab entries for the new content.

### Changed

#### Inventory and user experience

- Reviewed stack sizes and removed unintended inventory inconsistencies.
- Reordered creative-tab entries so related materials, intermediates, and outputs appear together.
- Improved JEI and creative inventory grouping for the Ashen Resonance production chains.
- Removed or repositioned entries that did not belong in their previous inventory locations.

#### Visual consistency

- Completed a consistency pass across items, blocks, and interface elements.
- Standardized contrast, outline thickness, and the mod's material colour language.
- Refined mismatched textures and presentation details that made content appear unfinished.

### Fixed

- Fixed Living Ember Lamp links remaining valid after the connected Molten Rotor Furnace was removed.
- Fixed replacement furnaces placed at previously linked coordinates automatically inheriting stale lamp connections.
- Fixed missing or inconsistent creative-tab placement for newly added content.
- Fixed item and block presentation inconsistencies discovered during the final visual pass.


----------------



## [0.2.5-beta] — Compatibility, Guidance, and Progression

### Updated

- Updated NeoForge to `21.1.247`.
- Updated English (US), English (UK), and Spanish language files.
- Refined the Molten Rotor Furnace Ponder scene layout and viewing orientation.

### Cross-Mod Compatibility

- Added the shared Sulfur-processing input tag.
- Added shared Sulfuric Acid fluid and bucket tags.
- Removed unsafe hard references to optional TFMG content.
- Verified compatibility with:
  - TFMG Sulfur Dust
  - TFMG Sulfuric Acid
  - Butchery Sulfur
  - Railcraft Reborn Sulfur Dust
- Added common Sulfur storage-block tags.
- Added Create filling and emptying recipes for the Sulfuric Acid Bucket.

### Tooltips

- Improved Sulfur and Sulfuric Acid tooltips.
- Added corrosive-handling warnings for Sulfuric Acid.
- Added fluid and bucket compatibility information.
- Added water and lava reaction warnings.
- Added intended industrial-use descriptions.
- Added a warning that improper handling may cause major harm.

### Advancements

- **Sulfurous Beginnings** — Obtain Sulfur.
- **Shattered Power** — Obtain Blaze Shards and craft the Infernal Impeller.
- **I’m Flattered..** — Obtain Spent Ash.
- **You’re on Fire** — Obtain a Flameborne Core.
- **Woah.. How Do I Use This Thing?** — Obtain the Molten Rotor Furnace.
- **Controlled Chemistry** — Process Sulfur through Combustion Mixing.
- **Industrial Allies** — Use compatible Sulfur from another mod.
- **Pyro** — Obtain and fire the Pyroclast Bomb from a Potato Cannon.

### Ponder

- Added an in-game Sulfur Processing Guide.
- Explains accepted common Sulfur tags.
- Explains compatibility with properly tagged external Sulfur dusts.
- Explains why raw ores, chunks, and storage blocks are not accepted as processed Sulfur inputs.


----------------



## [0.2.4-beta1] - Progression Hotfix

### Fixed

- Fixed a progression-blocking recipe loop where the Molten Rotor Furnace required Flameborne Core while Flameborne Core required sulfur obtained through the furnace progression.
- Replaced the Molten Rotor Furnace's Flameborne Core ingredient with Blaze Shard.
- Restored a valid progression path from early Blaze processing to the Molten Rotor Furnace and then into sulfur chemistry.


----------------



## [0.2.4-beta] - Ponder, Projectile, and Build Stability Update

### Added

#### Ponder

- Added a detailed Molten Rotor Furnace Ponder scene covering startup, heat tiers, RPM progression, stress output, the heat gauge, Engineer's Goggles information, and cooldown behavior.
- Added the completed Perforated Spritzer mob-automation Ponder scene.
- Added and synchronized Ponder localization for American English, British English, and Spanish.
- Added the Molten Rotor Furnace to the mod's supported Ponder machinery.

#### Pyroclast Bomb

- Added generated Create Potato Cannon projectile data for the Pyroclast Bomb.
- Added separate balancing for hand-thrown and Potato Cannon launch behavior.

### Changed

- Expanded the Molten Rotor Furnace Ponder sequence from a short overview into a longer heat-and-output tutorial.
- Corrected the displayed Ponder shaft rotation direction.
- Reduced the Pyroclast Bomb's hand-thrown launch velocity for a heavier, shorter arc.
- Reduced the Pyroclast Bomb's Potato Cannon velocity for more controllable range.
- Updated public documentation and release metadata to version `0.2.4-beta`.

### Fixed

- Fixed the Create Potato Cannon no longer recognizing the Pyroclast Bomb as valid ammunition.
- Fixed generated Potato Cannon projectile data not being included in the final mod resources.
- Fixed duplicate-resource build failures caused by the same projectile JSON being supplied from multiple resource locations.
- Fixed missing default-localization entries for newly added Ponder text.
- Fixed the Gradle data-generation configuration so `runData` can generate the required Create projectile data.

### Testing

- Verified the Molten Rotor Furnace Ponder scene in game.
- Verified the expanded heat explanation, gauge sequence, kinetic animation, and corrected shaft direction.
- Verified hand throwing and Create Potato Cannon launching for the Pyroclast Bomb.
- Verified that the Pyroclast Bomb remains registered as valid Potato Cannon ammunition after rebuilding.

### Known limitations

- Fuel compatibility remains intentionally selective; not every modded furnace fuel or log is accepted automatically.
- Furnace lighting is still based on Minecraft's normal block-light behavior and renderer effects rather than truly directional light propagation.
- Additional Ponder coverage and recipe explanations may still be expanded during beta.


----------------



## [0.2.3-betaA] - Pre-Release Public

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
- Updated metadata to version `0.2.3-betaA` and author `Hxney`.

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


----------------



## [0.2.1-betaA through 0.2.1-betaE] - Source Restoration Baseline

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


----------------



## [0.1.9-beta] - Earlier Surviving Beta

- Earlier compiled beta used as one source of reference during restoration.
- Included the original foundation of the Molten Rotor Furnace, sulfur chemistry, rubber content, Perforated Spritzer, and Pyroclast systems.
- The modern source is not a byte-for-byte decompilation of this build; it has been repaired, reorganized, expanded, and independently maintained by the original author.

## Project history note

Create: Sulfuric Resonance is developed and maintained by **Hxney**. Approximately eight months of active development led to the `0.2.4-beta` public-beta candidate, excluding a one-year interruption caused by unforeseen life emergencies.
