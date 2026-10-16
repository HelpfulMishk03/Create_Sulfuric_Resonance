# Machines, processing, and content

## Processing systems

| System | Main owner(s) | Integration contract |
| --- | --- | --- |
| Molten Rotor Furnace | `content/blocks/moltenrotor/` | fuel, heat, rotation, visual/audio feedback |
| Sulfur Burner | `content/blocks/sulfurburner/` | compact heat source with staged operating feedback |
| Combustion Belt | `content/blocks/combustionbelt/`, Belt mixins | belt movement plus resolved heat/time/distance requirements |
| Ash Ceramic Crucible | `content/blocks/crucible/` | Create basin mixing with CSR heat rules |
| Sulfuric Resonance Chamber | `content/blocks/sulfuricresonancechamber/` | inventory, acid, RPM, heat, process state, output and presentation |
| Rotary Leacher | `content/blocks/rotaryleacher/`, `content/recipes/rotaryleaching/` | batched acid processing, primary/byproduct outputs, process state and automation |
| Catalyst Bed | `content/blocks/catalystbed/` | direct Chamber upgrade; changes processing rate without stacking |
| Resonant Heat Injector | `content/blocks/resonantheatinjector/` | specialized Chamber heat interaction |
| Spritzers | `content/fluids/spritzer/` | tanked fluid behavior, world/entity/item interaction and optional precision filtering |

For each machine, keep these ownership layers distinct:

1. Block: placement, state properties, interaction surface, and shape.
2. Block entity: authoritative ticking, inventory/fluid state, processing, persistence, synchronization.
3. Menu: server-validated player interaction and data slots.
4. Screen/renderer: client-only interpretation of synchronized data.
5. Resources: recipe, loot, tags, models, language, sounds, particles, Ponder, advancement.

## Process state and automation

Process monitoring uses the `content/process/` package and Process Monitor/Gauge content. Process state exists so normal Create-style automation can respond to machine state rather than relying on timers.

The monitor's target contract currently covers the Sulfuric Resonance Chamber and Rotary Leacher: both expose discrete batch states that can change downstream factory behavior. Thermal infrastructure and fuel appliances keep their purpose-specific goggle, gauge, redstone, and indicator feedback; do not add them as monitor targets just for parity. Add another provider when its process state gives automation a meaningful action.

When adding a state, define all of the following together: the authoritative condition, serialization/sync behavior, Goggle/Jade text, redstone behavior, automation interactions, screen/renderer presentation, localization, and Ponder explanation. Do not infer a process state from renderer animation.

## Fluids, effects, and reactive tools

Sulfuric Acid content spans fluid registration, block/world behavior, effects, client fog/particles, filling/emptying recipes, spritzer behavior, and throwable flask behavior. Treat acid changes as cross-cutting: a fluid change can affect recipes, entity damage, copper treatment, world interactions, UI, particles, and localization.

Reactive tools include Cinder Flares, Sulfuric Acid Flasks, and Pyroclast Bombs. Their automation bridges live under `compat/automation/` and `compat/arm/`; client hand/throw animations are client code. When changing an item model or animation, check first person, third-person left/right hands, deployed/dispensed behavior, server multiplayer sync, and Ponder.

## Data and resources

Shipped content belongs in `src/main/resources`:

```text
assets/sulfuricresonance/   models, textures, lang, sounds, blockstates
data/sulfuricresonance/     recipes, loot, tags, advancements
META-INF/                   NeoForge metadata, mixin declarations, packaged license
```

All locales must contain the same translation keys as `en_us.json`; localized text still needs human review for meaning, line length, placeholders, and terminology.

## New-content definition of done

A new block/item is not complete when it compiles. Before calling it complete, check registration, creative tab, recipe, tags/tool requirements, loot, models/blockstate, particles/sounds where relevant, all locale keys, advancement, Ponder, Goggle/Jade status, JEI/EMI, automation, dedicated server, and actual in-game appearance.

### Framed Ashesil

The Construction tab includes Framed Ashesil, Framed Ashesil Pane, Framed Tempered Ashesil, and Framed Tempered Ashesil Pane. Surround one Obsidian Fiber with four corresponding glass blocks to frame four blocks, or with eight corresponding panes to frame eight panes. These retain their source glass properties, including tempered blast resistance and Silk Touch drops. The frame uses a narrow obsidian rim around the existing glass.

#### Leaching recipe batching contract

Rotary Leacher batches use sulfuric acid and the documented fixed batch acid/time curve. Custom recipe `fluid`, `fluid_amount`, and `processing_time` fields do not override that curve in the current implementation. Ingredient, input count, result, byproduct, and minimum speed remain recipe driven.

Sulfur Burners are direct deposit-only Mechanical Arm targets. Arm insertion uses the existing fuel handler, including its accepted-fuel rules and queue limit. Fuel extraction remains unavailable.
