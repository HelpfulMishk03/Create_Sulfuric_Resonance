## License
Create: Sulfuric Resonance is proprietary software. Copyright © 2026 Hxney. All Rights Reserved.

Compiled releases may be used for personal, non-commercial gameplay. Redistribution, modified releases, source-code reuse, asset reuse, and unauthorized uploads are prohibited. See [LICENSE](LICENSE) for the complete terms.

Create: Sulfuric Resonance is a NeoForge addon for Create that expands Minecraft's heat, combustion, sulfur, acid, rubber, and mechanical-processing systems.

# Create: Sulfuric Resonance

The current stable development version is **0.0.2.1-betaA** for:

- Minecraft **1.21.1**
- NeoForge **21.1.243**
- Create **6.0.10-281**
- JEI **19.39.0.371** (optional, client-side)

## Current Features

### Molten Rotor Furnace

The Molten Rotor Furnace is a kinetic heat source and combustion-processing block.

Current functionality includes:

- Multiple heat tiers
- Kinetic RPM generation
- Stress-capacity output
- Fuel stacking and queued fuel
- Exact active-fuel names in Create goggles
- Queued-fuel count and item breakdown in Create goggles
- Remaining burn-time display
- Cooling-time display
- Rain-sensitive heating and cooling
- Creative Blaze Cake mode
- Blaze Cake and Soul-Fired Blaze Cake support
- Mechanical Arm fuel insertion
- Custom rendering for the impeller, shafts, heat gauge, and radiant particles
- Fuel persistence across world reloads

Supported built-in fuel profiles currently include:

- Sticks
- Logs
- Coal
- Charcoal
- Coal Blocks
- Dried Kelp Blocks
- TNT
- Blaze Cakes
- Soul-Fired Blaze Cakes

### Combustion Mixing

Adds a custom basin-processing category for recipes requiring the Molten Rotor's higher heat tiers.

JEI integration includes:

- A dedicated Combustion Mixing category
- An animated Molten Rotor display
- Mechanical Mixer, Basin, and Molten Rotor catalysts
- Heat requirement indicators
- Soul-Fired Blaze Cake catalyst display for maximum-heat recipes

### Sulfuric Acid

Includes sulfuric-acid content and supporting systems such as:

- Sulfuric Acid fluid and block
- Acid Burn effect
- Acid fog handling
- Acid drip particles
- Sulfuric Acid Bucket
- Combustion-based acid processing

### Rubber and Mechanical Components

Includes additional materials and components such as:

- Rubber Padding
- Molded Rubber Gaskets
- Sheathed Impeller Blades
- Perforated Spritzer
- Rubber-related processing recipes
- Mechanical Arm interaction support for applicable blocks

### Pyroclast Bomb

Includes Create Potato Cannon integration for the Pyroclast Bomb, including its custom projectile type and explosion behavior.

### Ponder

Includes Ponder integration for supported machinery, including Perforated Spritzer scenes.

## Installation

1. Install Minecraft 1.21.1.
2. Install NeoForge 21.1.243 or newer within the supported 1.21.1 range.
3. Install Create 6.0.10.
4. Optionally install JEI 19.39.0.371 or newer for recipe viewing.
5. Place the Create: Sulfuric Resonance JAR in the Minecraft `mods` folder.

## Building from Source

This project uses Java 21 and the Gradle wrapper.

On Windows PowerShell:

```powershell
.\gradlew clean build
```

The built mod JAR will be placed in:

```text
build/libs/
```

## Development Setup

The project expects:

```properties
minecraft_version=1.21.1
neo_version=21.1.243
create_version=6.0.10-281
jei_version=19.39.0.371
```

Refresh dependencies when versions change:

```powershell
.\gradlew --refresh-dependencies clean build
```

## Repository Notes

This project was reconstructed completely by the original author, Hxney, from a previously released compiled build after a development hiatus. The current source has since been repaired, updated, expanded, and restored as the official development source for Create: Sulfuric Resonance. Hxney remains the only one actively maintaining and updating.

The current `0.0.2.1` source should be treated as the new stable development baseline rather than a byte-for-byte decompilation of the older archive.

Generated development folders are not part of the source repository, including:

```text
.gradle/
.idea/
build/
run/
runs/
```

## Known Limitations

- The current fuel system uses defined fuel profiles rather than automatically supporting every furnace fuel from every addon.
- Broader tag-based and data-driven fuel compatibility is planned.
- JEI support is optional and client-side.
- This is still an active development project, so recipe balance and mechanics may change.

## Planned Work

Current planned improvements include:

- Tag-based fuel categories
- More efficient compatibility with Create addons and modded fuels
- Data-driven fuel profiles
- Additional combustion recipes
- Continued Molten Rotor polish
- Expanded Ponder scenes
- Further sulfur, acid, rubber, and industrial-processing content

## License

Create: Sulfuric Resonance Proprietary License
Copyright © 2026 Hxney. All Rights Reserved.

## Author

Hxney
