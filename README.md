# Create: Sulfuric Resonance
## 0.6.0 — Second Yield

**Second Yield** expands Create: Sulfuric Resonance into ore refining, chemical leaching, and industrial construction.

At the center of the update is the new **Rotary Leacher**, a two-block processing machine that uses Sulfuric Acid to refine crushed ores into **Purified Metal Chunks**, producing **Mineral Tailings** as a secondary output.

Alongside the new processing line, 0.6.0 introduces an entire mineral-concrete construction set, expands Create automation support, improves recipe-viewer integration, and carries several quality-of-life changes forward from 0.5.1.

---

## Compatibility

- **Minecraft:** 1.21.1
- **Mod Loader:** NeoForge 21.1.252+
- **Create:** 6.0.10-281+
- **Java:** 21


## Links & Support

Found a bug, want the latest release, or want to support continued development?

- **Issues & Bug Reports** — [Report an issue](https://github.com/HelpfulMishk03/Create_Sulfuric_Resonance/issues)
- **Modrinth** — [Download on Modrinth](https://modrinth.com/mod/create-sulfuric-resonance)
- **CurseForge** — [Download on CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-sulfuric-resonance)
- **Ko-fi** — [Support Create: Sulfuric Resonance](https://ko-fi.com/hxneyl)

Bug reports and reproducible compatibility issues are always appreciated.  
If you enjoy the mod and would like to support its continued development, you can do so through Ko-fi.


Recipe-viewer support is available for both **JEI** and **EMI**.

---

# Rotary Leaching

## The Rotary Leacher

0.6.0 introduces the **Rotary Leacher**, a two-block chemical processing machine designed around Sulfuric Acid and Create-style automation.

The Leacher can process:

- Raw Iron Ore
- Raw Gold Ore
- Raw Copper Ore
- Raw Zinc Ore

Each recipe converts crushed ore into a corresponding **Purified Metal Chunk** while also producing **Mineral Tailings**.

Purified Metal Chunks can then be processed through normal smelting or blasting routes.

The result is an additional ore-processing path built around chemical refinement rather than simply increasing the number of mechanical processing stages.

---

## Sulfuric Acid Processing

The Rotary Leacher stores and consumes **Sulfuric Acid** as part of its processing cycle.

Its visible acid surface changes with the amount of fluid stored inside the machine, making the tank level readable directly from the world.

The **Sulfuric Resonance Chamber** now follows the same behavior, keeping its visible fluid level synchronized with its internal Sulfuric Acid storage.

---

# Create Automation

The Rotary Leacher is designed to function as part of a full Create production line rather than as an isolated machine.

It supports:

- Manual item insertion and extraction
- Item-handler based automation
- Sulfuric Acid input
- Automated recipe outputs
- Create Mechanical Arm interaction
- Dedicated Mechanical Arm deposit points
- Dedicated Mechanical Arm extraction points
- Machine status readouts

Mechanical Arms can therefore be used to directly feed and unload the Leacher as part of larger factories.

A dedicated **Ponder scene** walks through the basic leaching process and automation layout in-game.

---

# Purified Metal Chunks

Second Yield adds purified forms of the four metals currently supported by Rotary Leaching:

- Purified Iron Chunk
- Purified Gold Chunk
- Purified Copper Chunk
- Purified Zinc Chunk

Both **smelting** and **blasting** recipes are provided.

---

# Mineral Tailings

Leaching produces **Mineral Tailings** alongside the purified metal output.

Rather than existing only as a processing byproduct, Mineral Tailings can be reused as part of the new construction-material system introduced in this update.

---

# Mineral Concrete & Weathered Mineral Concrete

0.6.0 adds 2 new industrial construction families.


---

# Recipe Viewer Support

Rotary Leaching is integrated into the mod's recipe-viewer support.

### JEI

Rotary Leaching recipes have their own displays and workstation integration.

The 0.6.0 update also corrects layout issues present during development.

### EMI

EMI support is enabled in the default development environment and Rotary Leaching recipes are integrated into the existing category and workstation system.

---

# Sulfur Compatibility

0.6.0 adds compatibility for the vanilla-backport:

- Sulfur
- Potent Sulfur

When those optional items are available, Sulfuric Resonance automatically exposes the corresponding compatible recipes.

No separate compatibility addon is required.

---

# Sulfur Changes

The previous **Sulfur Block** has been renamed:

> **Block of Raw Sulfur**

The new name better reflects its role as a raw-material storage block.

Localization keys and creative-tab organization have been updated accordingly.

---

# Additional Improvements

Several internal and visual improvements were made while building the new processing system.


# 0.5.1 Features Included in 0.6.0

Several changes first introduced during the 0.5.1 development cycle are also included in the 0.6.0 release.

## Direct Chamber Filling

Sulfuric Acid buckets can now be used directly on the **Sulfuric Resonance Chamber** to fill its internal tank.

This provides a straightforward manual alternative to fluid piping during setup or smaller-scale production.

---

## Precision Spritzer Filtering

The **Precision Spritzer** now treats its item-list filter independently from transformation recipes.

If a block matches the configured item filter, the Spritzer can spray it even when no block-transformation recipe exists.

Transformation itself remains recipe-controlled.

In other words:

- **Filter match:** determines whether the Spritzer may target the block.
- **Transformation recipe:** determines whether spraying changes that block into something else.

Entity-targeting behavior is unchanged.