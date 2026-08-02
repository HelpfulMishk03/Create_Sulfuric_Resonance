## Create: Sulfuric Resonance

**Create: Sulfuric Resonance** is an independently developed NeoForge addon for Create that expands combustion, kinetic heat generation, Sulfur chemistry, Sulfuric Acid processing, rubber engineering, ash-based materials, and industrial automation.

> **Beta notice:** Version **0.0.2.6-beta — Ashen Resonance** is a public beta release. Core systems are functional and tested, but balancing, recipes, visuals, names, and compatibility may still change before a stable release. Back up important worlds before updating beta versions.

## Supported Versions

- Minecraft **1.21.1**
- NeoForge **21.1.247**
- Create **6.0.10 or newer**
- Java **21**
- JEI **19.39.0.372 or newer** — optional and client-side

> **JEI compatibility notice:** Some JEI builds may render blank fluid textures in Combustion Mixing recipes. The recipe, tooltip, and fluid amount can still be registered correctly.

## Development Status

> **Public beta:** `0.0.2.6-beta` expands the mod with the **Ashen Resonance** material family, new Create-compatible tools, a custom crucible, heat-responsive lighting, additional building blocks, custom sounds, a rebuilt advancement tree, and expanded localization.

Create: Sulfuric Resonance is a solo project. Feedback and clear bug reports are appreciated, though updates may take time.

Release-by-release additions and fixes are documented in [CHANGELOG.md](src/main/resources/CHANGELOG.md).

## Current Features

### Molten Rotor Furnace

The Molten Rotor Furnace is a kinetic heat source and combustion-processing block that converts temperature into rotational power.

Major capabilities include:

- temperature-based RPM and stress generation;
- multiple heat tiers;
- manual and automated fuel insertion;
- Create Mechanical Arm support;
- Create-goggle diagnostics;
- rain-sensitive temperature behaviour;
- creative heat-tier cycling;
- custom block-entity rendering and persistence;
- custom operating sounds;
- a detailed Ponder scene covering heat tiers, RPM, stress output, the gauge, and cooldown behaviour.

### Combustion Mixing

Combustion Mixing extends Create Basin processing with recipes that require the Molten Rotor Furnace's highest heat tiers.

The **Ceramic Crucible** expands the mod's heat-resistant processing identity and provides a foundation for specialized combustion recipes.

### Ashen Resonance Materials

Version `0.0.2.6-beta` introduces a new family of ash-derived materials and components:

- **Ash Brick**
- **Ash Ceramic**
- **Ash Brick Block**
- **Ash Brick Stairs**
- **Ash Brick Slab**
- **Ash Brick Wall**
- **Ash Brick Pillar**
- **Ceramic Crucible**
- **Ashesil Glass**
- **Ashesil Panes**

These materials support construction, decoration, industrial crafting, and future high-temperature processing systems.

### Living Ember Lamp

The **Living Ember Lamp** is a heat-responsive industrial light linked to a Molten Rotor Furnace.

Its brightness changes according to the linked furnace's heat state, providing both functional lighting and immediate visual feedback for active machinery.

### Cinder Fuel Briquette

The **Cinder Fuel Briquette** is a compact industrial fuel made from Reinforced Cinder Compound and Charcoal.

- Burns longer than ordinary Charcoal.
- Supports manual insertion and Create-based automation.
- Can be handled by Mechanical Arms and other supported item-transfer systems.

### Cinder Sandpaper

The **Cinder Sandpaper** is a Create-compatible polishing tool made from Reinforced Cinder Compound and Paper.

- Functions as sandpaper in Create processing.
- Has twice the durability of Red Sandpaper.
- Supports automated polishing workflows.

### Sulfur and Cross-Mod Compatibility

Sulfuric Resonance uses common tags to recognise compatible processed Sulfur from other mods.

- Supported sulfur-processing inputs include compatible `c:sulfur` items and `c:dusts/sulfur` dusts.
- Sulfuric Acid uses shared common fluid and bucket tags.
- Sulfur storage blocks use common storage-block tags.
- Raw ores, raw chunks, and storage blocks are intentionally not treated as processed Sulfur inputs.
- Materials using private or missing tags may need dedicated compatibility support.

Verified compatibility includes:

- TFMG Sulfur Dust;
- TFMG Sulfuric Acid;
- Butchery Sulfur;
- Railcraft Reborn Sulfur Dust.

### Sulfuric Acid

Sulfuric Acid is a hazardous industrial fluid with custom effects, particles, reactions, bucket support, and processing uses.

- Create filling and emptying recipes support the Sulfuric Acid Bucket.
- Tooltips communicate corrosive handling, fluid and bucket compatibility, industrial uses, and water or lava reactions.

### Rubber and Mechanical Components

The mod includes a developing rubber-production chain alongside kinetic components, bouncing Rubber Padding, the Perforated Spritzer, impeller parts, gaskets, and related industrial materials.

### Perforated Spritzer

The Perforated Spritzer stores and disperses fluids downward for crop, fluid-handling, and mob-automation setups.

Its Ponder coverage includes:

- basic operation and fluid pressure;
- crop hydration and watering;
- Sulfuric Acid, lava, and water interactions;
- mob damage and automatic drop collection.

### Advancements

The advancement tree has been rebuilt and expanded to better guide progression through Sulfuric Resonance.

### Ponder

Ponder scenes are included for:

- Perforated Spritzer operation;
- Perforated Spritzer mob automation;
- Molten Rotor Furnace operation, heat progression, kinetic output, gauge behaviour, and cooldown;
- Cross-Mod Sulfur Compatibility, explaining accepted common tags and processed external Sulfur inputs;
- Living Ember Lamp linking and heat-responsive behaviour.

### Localization

Current localization support includes:

- American English;
- British English;
- Spanish;
- French;
- German.

## User Experience and Presentation

Version `0.0.2.6-beta` includes a broad inventory and visual-consistency pass.

- Stack sizes were reviewed for consistency.
- Creative-tab ordering was reorganized.
- Related intermediates and outputs were grouped together.
- Item, block, and UI contrast were reviewed.
- Outline thickness and material colour language were standardized.
- Tooltips were added or revised across the new material and component chains.

## Installation

1. Install Minecraft **1.21.1**.
2. Install NeoForge **21.1.247** for Minecraft 1.21.1.
3. Install Create **6.0.10 or newer**.
4. Optionally install JEI **19.39.0.372 or newer** for recipe viewing.
5. Download Create: Sulfuric Resonance from CurseForge or Modrinth.
6. Place the official mod JAR in the instance's `mods` folder.

Do not install development source folders or an embedded Create dependency. Create must remain a separate required mod.

## Known Beta Limitations

- JEI is optional and client-side only.
- Some JEI builds may render blank Combustion Mixing fluid slots even when recipe data is present.
- Fuel compatibility is tag-aware but is not completely data-driven for every modded furnace fuel.
- Not every modded log is automatically accepted for latex processing.
- Some modded wooden fuels may require explicit tag or compatibility support.
- Recipe balance and heat values may change during beta testing.
- Minecraft block light is omnidirectional; directional-looking glow effects do not change the underlying light-propagation system.

## Planned Work

- Expand data-driven and tag-based fuel profiles where they make sense.
- Add broader compatibility with Create addons and selected modded fuels.
- Continue Molten Rotor visual and balance polish.
- Expand latex, rubber, and vulcanisation progression.
- Add more Combustion and Sulfuric Acid recipes.
- Expand Ash Ceramic and Ceramic Crucible processing.
- Continue dedicated-server and large-modpack compatibility testing.
- Add EMI support.
- Introduce new machinery and later material tiers.

## License and Distribution

Create: Sulfuric Resonance is proprietary software.

**Copyright © 2026 Hxney, Ł. All Rights Reserved.**

Compiled official releases may be used for personal, non-commercial gameplay. Unauthorized redistribution, re-uploading, modified releases, forks, ports, source-code reuse, asset reuse, impersonation, or claims of ownership are prohibited except where the included license expressly permits them.

Modpack inclusion is permitted only under the terms in [LICENSE.md](src/main/resources/LICENSE.md), using an official Create: Sulfuric Resonance distribution page when required.

This license applies only to original Create: Sulfuric Resonance material. Create, Minecraft, NeoForge, JEI, and all other third-party projects remain owned and licensed by their respective creators.

Create: Sulfuric Resonance is an unofficial addon and is not affiliated with or endorsed by Mojang Studios, Microsoft, NeoForged, or the creators of Create.

## Official Author

**Hxney**
