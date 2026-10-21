# CSR 0.7.0 Create contraption compatibility

**Source baseline:** Minecraft 1.21.1, NeoForge 21.1.259, Create 6.0.10-281.  
**Registry audit:** 58 unique block registrations parsed from `AllModBlocks.java`; 57 are non-fluid blocks.  
**Evidence boundary:** The Create movement and schematic source APIs were inspected. The schematic files are structurally generated as Create 6.0.10 vanilla `StructureTemplate` NBT. No Minecraft/Create game session was available for assembling, rendering, saving, or moving these test structures, so every `Tested` value is **No**. Static/API support is not presented as gameplay proof.

## How to read the matrix

- **Moves** means Create's normal structure capture can collect the block type, and its state is not intentionally marked unmovable by CSR. It is an API/source expectation, not a runtime result.
- **Renders** means a block state or captured block-entity snapshot is available to Create's contraption renderer. Custom renderer output still needs in-game confirmation.
- **Data preserved** means Create captures the block entity's saved NBT and reconstructs it on disassembly. `State only` means there is no CSR block entity payload. Links containing world positions or external network identities are called out separately.
- **Operates while moving** is deliberately strict. Ordinary block-entity ticking is not run as though the moving structure were at each machine's original world position. `No` means the machine can be carried with its data, but its active processing or network behavior is not implemented for a mounted context.
- Create's Mechanical Bearing and Mechanical Piston layouts are both included, but neither has been assembled in game. Gantry, Rope Pulley, and Train systems still need runtime checks; no controller type is claimed as tested.

## Complete registered-block matrix

| Registered CSR block | Moves | Renders | Data preserved | Operates while moving | Tested |
|---|---|---|---|---|---|
| `molten_rotor_furnace` | Expected yes | Expected; renderer has a contraption path | Yes: heat, fuel, settings, attachment, identities | No: furnace ticking and rain exposure need a mounted-world implementation | No |
| `thermochemical_link_drive` | Expected yes | Expected | Yes: saved thermochemical data | No: kinetic/thermal graph does not run as a mounted graph | No |
| `perforated_spritzer` | Expected yes | Expected | Yes: fluid tank and settings | No: existing block-entity processing/capability path is stationary | No |
| `scraper` | Expected yes | Expected; moving flap animation is not active | Yes: block-entity snapshot and flap value | No: scraper searches a stationary belt and world inventory | No |
| `block_caddy` | Expected yes | Expected; cargo comes from captured block-entity NBT | Yes: cargo ItemStack and its block data | No: belt carrier processing is stationary-belt logic | No |
| `ash_ceramic_crucible` | Expected yes | Expected | Yes: item and fluid state | No: inventory/fluid processing uses normal world capabilities | No |
| `thermochemical_cogwheel` | Expected yes | Expected | Yes: heat data and block state | No: kinetic and thermal networks are not rebuilt as mounted networks | No |
| `large_thermochemical_cogwheel` | Expected yes | Expected | Yes: heat data and block state | No: kinetic and thermal networks are not rebuilt as mounted networks | No |
| `thermochemical_conduit` | Expected yes | Expected | Yes: heat data and block state | No: adjacency-based heat graph is stationary | No |
| `thermochemical_shaft` | Expected yes | Expected | Yes: heat data and block state | No: kinetic and thermal networks are not rebuilt as mounted networks | No |
| `thermochemical_boiler_interface` | Expected yes | Expected | Yes: heat data and block state | No: boiler/thermal adjacency is stationary | No |
| `thermochemical_clutch` | Expected yes | Expected | Yes: heat data and block state | No: Create kinetic state and thermal graph are not mounted integrations | No |
| `thermochemical_gearbox` | Expected yes | Expected | Yes: heat data and block state | No: Create kinetic state and thermal graph are not mounted integrations | No |
| `parallel_thermochemical_gearbox` | Expected yes | Expected | Yes: heat data and block state | No: Create kinetic state and thermal graph are not mounted integrations | No |
| `living_ember_lamp` | Expected yes | Expected | Yes: link identity and lamp state | No: link tracking and animation tick are stationary | No |
| `thermal_relay_switch` | Expected yes | Expected | Yes: modes, thresholds, UUID/link data | No: relay tick resolves the stationary thermal network; mounted links are unverified | No |
| `thermal_battery` | Expected yes | Expected | Yes: stored heat and output settings | No: rotational output and thermal network supply are not mounted | No |
| `thermal_gauge` | Expected yes | Expected | Yes: gauge/link settings | No: readings depend on the stationary network resolver | No |
| `thermal_warning_alarm` | Expected yes | Expected | Yes: thresholds and link identity | No: alarm tick depends on the stationary network resolver | No |
| `process_monitor` | Expected yes | Expected | Yes: identity and saved channel targets | No: target polling is stationary; targets may refer to old world coordinates | No |
| `process_gauge` | Expected yes | Expected | Yes: monitor/link references | No: readings depend on stationary monitor targets | No |
| `sulfur_burner` | Expected yes | Expected | Yes: fuel inventory and heat state | No: fuel ticking and item capabilities are not mounted | No |
| `resonant_heat_injector` | Expected yes | Expected | Yes: thermochemical heat data | No: network update is adjacency based | No |
| `catalyst_bed` | Expected yes | Expected | State only, including connection state | N/A: passive structural block | No |
| `sulfuric_resonance_chamber` | Expected yes | Expected; moving block model is captured | Yes: inventory, fluid, recipe and thermal data | No: reaction progression must not tick against an invalid virtual/world position | No |
| `rotary_leacher` | Expected yes as a paired two-block structure | Expected | Yes: both halves' inventories, fluid, and progress snapshots | No: recipe processing and fluid access are not mounted integrations | No |
| `resonant_iron_bars` | Expected yes | Expected | State only | N/A: passive block | No |
| `resonant_copper_bars` | Expected yes | Expected | State only | N/A: passive block | No |
| `resonant_gold_bars` | Expected yes | Expected | State only | N/A: passive block | No |
| `rubber_padding` | Expected yes | Expected | State only | N/A: passive block | No |
| `sulfur_block` | Expected yes | Expected | State only | N/A: passive block | No |
| `carbon_deposit_block` | Expected yes | Expected | State only | N/A: passive block | No |
| `infernal_carbon_deposit_block` | Expected yes | Expected | State only | N/A: passive block | No |
| `ash_brick_block` | Expected yes | Expected | State only | N/A: passive block | No |
| `ash_brick_slab` | Expected yes | Expected | State only | N/A: passive block | No |
| `ash_brick_stairs` | Expected yes | Expected | State only | N/A: passive block | No |
| `ash_brick_pillar` | Expected yes | Expected | State only | N/A: passive block | No |
| `ash_brick_wall` | Expected yes | Expected | State only | N/A: passive block | No |
| `mineral_concrete` | Expected yes | Expected | State only | N/A: passive block | No |
| `mineral_concrete_mix` | Expected yes | Expected | State only | N/A: passive block | No |
| `mineral_concrete_slab` | Expected yes | Expected | State only | N/A: passive block | No |
| `mineral_concrete_stairs` | Expected yes | Expected | State only | N/A: passive block | No |
| `mineral_concrete_pillar` | Expected yes | Expected | State only | N/A: passive block | No |
| `mineral_concrete_wall` | Expected yes | Expected | State only | N/A: passive block | No |
| `weathered_mineral_concrete` | Expected yes | Expected | State only | N/A: passive block | No |
| `weathered_mineral_concrete_slab` | Expected yes | Expected | State only | N/A: passive block | No |
| `weathered_mineral_concrete_stairs` | Expected yes | Expected | State only | N/A: passive block | No |
| `weathered_mineral_concrete_pillar` | Expected yes | Expected | State only | N/A: passive block | No |
| `weathered_mineral_concrete_wall` | Expected yes | Expected | State only | N/A: passive block | No |
| `ashesil` | Expected yes | Expected | State only | N/A: passive block | No |
| `framed_ashesil` | Expected yes | Expected | State only | N/A: passive block | No |
| `ashesil_pane` | Expected yes | Expected | State only | N/A: passive block | No |
| `framed_ashesil_pane` | Expected yes | Expected | State only | N/A: passive block | No |
| `tempered_ashesil` | Expected yes | Expected | State only | N/A: passive block | No |
| `framed_tempered_ashesil` | Expected yes | Expected | State only | N/A: passive block | No |
| `tempered_ashesil_pane` | Expected yes | Expected | State only | N/A: passive block | No |
| `framed_tempered_ashesil_pane` | Expected yes | Expected | State only | N/A: passive block | No |
| `sulfuric_acid` | No: block uses `PushReaction.DESTROY` | Expected | No BE data | N/A: fluid block is not a carried machine | No |

## Implemented boundary and remaining work

Create 6's standard contraption capture serializes block-entity state and applies structure transforms when blocks are reconstructed. CSR's inventories, tanks, temperatures, progress values, and Caddy cargo therefore have a persistence path without a CSR-wide BE relocation mixin. The generated test layouts start with empty machine inventories so the schematic itself cannot duplicate stored items or fluids.

This does **not** implement active machine simulation on a moving structure. The concrete blockers are:

1. CSR machine tickers query their normal `Level` and world positions. Calling them from a mounted actor would read the stationary location or a virtual render world, so it could consume resources or advance recipes against the wrong blocks.
2. Create mounted item/fluid storage is a separate API from CSR's current stationary capability endpoints. It does not automatically make a moving Spritzer, Chamber, Crucible, or Leacher accept transfers from stationary pipes.
3. Thermal relays, batteries, gauges, alarms, and thermochemical components resolve adjacency/UUID networks. There is no mounted thermal graph or position-transform/relink pass yet.
4. Scraper and Caddy processor logic explicitly queries stationary belt segments and Create processing machines. They can be carried, but that does not make those belt interactions operate inside a contraption.
5. Process monitor targets can include world positions; Create transforms block states and the captured block entity but not arbitrary CSR position references without per-entity transform logic.

Real operational support needs machine-specific mounted actors backed by Create's `MovementContext`, portable storage where transfer is intended, safe recipes using contraption-local state, and explicit link/position transforms. That work is not represented as finished in the matrix or changelog.

## Verification performed

- Parsed all 58 unique registrations from `AllModBlocks.java`; the full test layout includes all non-fluid registrations and intentionally omits only the destructive fluid block `sulfuric_acid`.
- Generated both `.nbt` files with 1.21.1 `StructureTemplate` fields and `DataVersion: 3955`; compressed as gzip like Create exports.
- Verified NBT structure, palette entries, dimensions, and block positions with a local NBT reader.
- The project compilation/package build is reported separately from this matrix.
- Not verified in game: Schematic Table discovery/upload, placement, piston assembly, power direction, moving renderers, save/reload mid-motion, disassembly, multiplayer sync, inventories/tanks/heat after movement, or operation while moving.
