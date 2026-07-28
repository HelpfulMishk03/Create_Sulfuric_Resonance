# Create: Sulfuric Resonance

**Create: Sulfuric Resonance** is an independently developed NeoForge addon for Create that expands Minecraft's combustion, heat generation, sulfur chemistry, sulfuric acid, rubber processing, kinetic machinery, and industrial automation systems.

> **Beta notice:** Version **0.0.2.3-betaA** is a public-beta candidate. Core systems are functional and extensively tested, but balancing, recipes, visuals, and compatibility may still change before a stable release.

## Supported Versions

- Minecraft **1.21.1**
- NeoForge **21.1.243 or newer** within the supported Minecraft 1.21.1 range
- Create **6.0.10 or newer**
- Java **21**
- JEI **19.39.0.372 or newer** — optional, client-side

> **JEI compatibility notice:** JEI **19.39.0.379 and newer** may display blank fluid textures in some Combustion Mixing recipe slots. The recipes and fluid amounts remain present, but the visual fluid tile may not render correctly. This issue is still under investigation.

The current beta has also been tested with:

- NeoForge **21.1.244**
- Create **6.0.10**
- JEI **19.42.0.384**

## Development Status

> **Public beta:** Version **0.0.2.3-betaA** is feature-complete enough for public testing, but it is not a final stable release. Core mechanics are working and have been extensively tested; balancing, recipes, visuals, compatibility, names, and implementation details may still change as the project matures.

Create: Sulfuric Resonance is my first major Minecraft mod project. Development has taken approximately eight months of active work, not including an extended break caused by unforeseen life emergencies. Feedback and clear bug reports are appreciated, but updates may take time.

Release-by-release additions and fixes are documented in [CHANGELOG.md](src/main/resources/CHANGELOG.md).

## Testing Status

The 0.0.2.3-betaA pre-release test pass completed:

- **416 / 416 checks completed**
- **412 passed**
- **0 failed**
- **4 marked for final review**

The review items covered dropped-item model positioning, furnace side-click behavior, insertion sounds, and JEI fluid-slot rendering. Side-click behavior and insertion sounds have since been updated.

## Current Features

### Molten Rotor Furnace

The Molten Rotor Furnace is a kinetic heat source and combustion-processing block that converts temperature into rotational power.

Major capabilities include:

- temperature-based RPM and stress generation;
- multiple heat tiers;
- visible and queued fuel handling;
- manual and automated fuel insertion;
- Create-goggle diagnostics;
- rain-sensitive temperature behavior;
- creative heat-tier cycling;
- high-tier fuel boosts;
- custom block-entity rendering;
- world-reload persistence.

### Combustion Mixing

Combustion Mixing extends Create's Basin processing with recipes that require the Molten Rotor Furnace's higher heat tiers.

### Sulfuric Acid

Sulfuric Acid is a hazardous industrial fluid with custom effects, particles, fluid reactions, bucket support, and processing uses.

### Rubber and Mechanical Components

The mod includes a developing rubber-production chain alongside kinetic components, bouncing Rubber Padding, the Perforated Spritzer, impeller parts, gaskets, and related industrial materials.

### Ponder

Ponder scenes are included for supported machinery and mechanics, including Perforated Spritzer content. 
The second scene for the perforated spritzer is not yet complete.

## Installation

1. Install Minecraft **1.21.1**.
2. Install NeoForge **21.1.243 or newer** for Minecraft 1.21.1.
3. Install Create **6.0.10 or newer**.
4. Optionally install JEI **19.39.0.372 or newer** for recipe viewing.
5. Download Create: Sulfuric Resonance from curseforge or modrinth.

Do not install development source folders or an embedded Create dependency. Create must remain a separate required mod.


## Known Beta Limitations

- JEI is optional and client-side only.
- In the current development build, fluid names and amounts are recognized in Combustion Mixing.
- The Molten Rotor and Perforated Spritzer dropped-item model positions may still receive final visual adjustment.
- Fuel compatibility is tag-aware but is not yet completely data-driven for every modded furnace fuel.
- Some modded wooden fuels may require explicit tag or compatibility support.
- Recipe balance and heat values may change during beta testing.
- Additional Ponder scenes and recipe explanations are still planned.

## Planned Work

- Expand data-driven and tag-based fuel profiles.
- Add broader compatibility with Create addons and modded fuels.
- Continue Molten Rotor visual and balance polish.
- Expand Lye, latex, rubber, and vulcanization progression.
- Add more combustion and Sulfuric Acid recipes.
- Expand Ponder coverage.
- Continue dedicated-server and large-modpack compatibility testing.
- Support for EMI

## License and Distribution

Create: Sulfuric Resonance is proprietary software.

**Copyright © 2026 Hxney, Ł. All Rights Reserved.**

Compiled official releases may be used for personal, non-commercial gameplay. Unauthorized redistribution, re-uploading, modified releases, forks, ports, source-code reuse, asset reuse, impersonation, or claims of ownership are prohibited except where the included license expressly permits them.

Modpack inclusion is permitted only under the terms in [LICENSE.md](src/main/resources/LICENSE.md), using an official Create: Sulfuric Resonance distribution page when required.

This license applies only to original Create: Sulfuric Resonance material. Create, Minecraft, NeoForge, JEI, and all other third-party projects remain owned and licensed by their respective creators.

Create: Sulfuric Resonance is an unofficial addon and is not affiliated with or endorsed by Mojang Studios, Microsoft, NeoForged, or the Creators of Create.

## Official Author

**Hxney**