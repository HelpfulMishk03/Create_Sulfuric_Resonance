# Thermochemical systems

## Heat and kinetic flow

CSR introduces a thermochemical graph that shares physical-looking components with Create but must be resolved independently. Rotation may make a machine capable of operating, while heat determines whether it has the required thermal condition. A rotor can generate rotation; a discharging Thermal Battery may also participate in kinetic behavior. Neither fact alone makes a heat source valid.

## Source resolution

The resolver is `content/blocks/thermochemicalconduit/ThermochemicalHeatResolver`. It walks eligible thermochemical nodes and returns a heat tier, source position, temperature, and path information.

```text
target requests heat
  -> find eligible live thermochemical source
  -> return live result when found
  -> otherwise search eligible Thermal Battery fallback
  -> Battery verifies mode, reserve, and interface
  -> return fallback result or NONE
```

The resolver must remain deterministic, terminate in cyclic networks, respect loaded-chunk checks, and never let a Battery create a self-charge loop. Resolver calls that mutate Battery demand are a high-risk seam: preserve that behavior only deliberately and keep it server-side.

## Molten Rotor Furnace

Core owners are `MoltenRotorBlockEntity`, `MoltenRotorTemperatureController`, `MoltenRotorFuelController`, and `MoltenRotorFuelHandler` under `content/blocks/moltenrotor/`.

The Rotor owns its temperature, operating tier, current/queued fuel, cooling, and kinetic output. Afterburn is a Rotor operating state above the ordinary Radiant ceiling, not a new general network heat tier. Its player-facing contract is: Radiant remains the highest normal tier at 1599 C; eligible Thermite-driven operation can reach 2000 C; Afterburn raises stress capacity but does not raise RPM or processing speed.

Any Rotor change must be tested at boundaries below/at/above 1599 C and at the 2000 C cap, including save/reload, rain/cooling, fuel insertion, renderer/audio transition, and recipe consumers.

## Sulfur Burner

The Sulfur Burner is CSR's compact thermochemical source. Unlike the Molten Rotor Furnace, it supplies heat to the thermochemical network without being a kinetic generator. Its implementation lives under `content/blocks/sulfurburner/`, with the authoritative operating state held by `SulfurBurnerBlockEntity` and presentation handled by the block, renderer, particles, and client sound/effect classes in that subsystem.

```text
accepted sulfur fuel
  -> Burner server-side fuel and operating state
  -> warmup / ignition / unstable burn / stabilized burn / shutdown / afterglow
  -> live thermochemical source exposed to the resolver
  -> shafts, conduits, and eligible consumers
```

The Burner owns its own fuel consumption, temperature/tier progression, and persistent operating state. The resolver only discovers the resulting live source; it must not advance the Burner's fuel or manufacture a heat tier. Client flame, light, sound, and particle effects read synchronized state and must never become a second source of truth.

When changing the Sulfur Burner, check all of the following:

- accepted fuel and insertion/automation rules;
- each operating transition, including warmup, ignition, unstable burn, stabilization, shutdown, and afterglow;
- heat tier and temperature seen by a directly attached Thermochemical Shaft and by a remote consumer;
- save/reload while active, at each transition, and during cooldown;
- rain/world interaction where applicable;
- Engineer's Goggles/Jade information, redstone or automation behavior, Ponder, subtitles, particles, light, and sounds;
- dedicated-server startup and a multiplayer client observing the same Burner state.

The key invariant is: **the Sulfur Burner is a live source only while its authoritative server state says it is producing heat.** A running animation, lingering afterglow, or connected shaft alone does not make it a valid heat source.

## Fuel precedence

```text
matching datapack/KubeJS molten_rotor_fuel recipes
  -> highest custom priority; recipe id breaks a tie deterministically
  -> enabled false can suppress fallback
  -> CSR built-in item/tag behavior
  -> ordinary fallback behavior
```

The recipe type is registered by `MoltenRotorFuelRecipeRegistry`; codec and behavior contract live in `MoltenRotorFuelRecipe` and `MoltenRotorFuelRecipeSerializer`; runtime selection is in `compat/fuel/FuelCompatibility`.

`/reload` changes definitions for future resolution. A fuel already burning retains the values it started with. Keep this rule when refactoring so reloads cannot retroactively mutate a running fuel.

For author-facing examples, see the root `MOLTEN_ROTOR_FUEL_DATAPACKS.md`; it should remain linked from the root README.

## Thermal network components

| Component | Architectural role |
| --- | --- |
| Thermochemical Shaft | physical carrier and cached network participant |
| Conduit | network traversal/extension |
| Cogwheels | dedicated heat-transmitting gears; ordinary Create gears do not transmit heat |
| Gearbox / Parallel Gearbox | directional routing/branching |
| Clutch | controlled interruption of thermochemical connection |
| Link Drive | special bridge/interaction point with kinetic/boiler behavior |
| Boiler Interface | converts valid CSR heat into Create boiler heating through its array rules |

Every component must expose matching `IRotate` and `ThermochemicalConnection` behavior. A visual shaft port that does not match the connection predicate is a bug even when the block looks connected.

## Thermal Battery

The Battery (`content/blocks/thermalbattery/`) is a reserve/fallback machine, not a second unrestricted heat graph.

| Concern | Owner |
| --- | --- |
| Placement, single interface face, blockstate indicator | `ThermalBatteryBlock` |
| Stored Heated/Superheated reserve, persistence, mode, demand/drain | `ThermalBatteryBlockEntity` |
| Menu authority | `ThermalBatteryMenu` on the server |
| Menu drawing | `client/screen/ThermalBatteryScreen` |
| World rendering | `client/ThermalBatteryRenderer` |
| Eligibility as fallback | `ThermochemicalHeatResolver` |

Current machine rules to preserve or consciously revise:

- It charges only from a valid live non-Battery thermochemical source.
- It keeps Heated and Superheated energy pools separate.
- Heated mode can draw its own reserve and then Superheated reserve; Superheated mode needs Superheated reserve.
- It becomes a resolver fallback when the live source is absent and demand exists.
- It has a single thermochemical interface face and must not silently connect through other faces.

Before release, exercise: empty/charged states, each output mode, loss and restoration of live source, multiple consumers, blocked kinetic network, save/reload, rotation/placement, and dedicated server menu interaction.
