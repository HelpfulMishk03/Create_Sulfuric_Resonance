# Molten Rotor custom fuels — datapacks and KubeJS

CSR 0.5.0 exposes Molten Rotor fuel definitions as a normal recipe type:

`type: sulfuricresonance:molten_rotor_fuel`

A matching data-driven recipe is resolved **before** CSR's built-in fuel tags,
item-specific fuels, and vanilla furnace-fuel fallback. This means a pack can
add a new fuel, override an existing fuel, or explicitly disable an existing
fuel without modifying CSR.

## Datapack example

Place a JSON file anywhere under `data/<namespace>/recipe/`:

```json
{
  "type": "sulfuricresonance:molten_rotor_fuel",
  "ingredient": { "item": "example:compressed_peat" },
  "burn_time_ticks": 1800.0,
  "heating_rate": 18.0,
  "maximum_temperature": 800.0,
  "maximum_units": 12,
  "behavior": "generic_medium",
  "priority": 100
}
```

`ingredient` uses the normal Minecraft Ingredient format, so tags work too:

```json
{
  "ingredient": { "tag": "c:fuels/peat" }
}
```

## KubeJS example

No CSR-specific KubeJS plugin is required. KubeJS can emit the standard custom
recipe directly:

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'sulfuricresonance:molten_rotor_fuel',
    ingredient: { item: 'example:compressed_peat' },
    burn_time_ticks: 1800.0,
    heating_rate: 18.0,
    maximum_temperature: 800.0,
    maximum_units: 12,
    behavior: 'generic_medium',
    priority: 100
  }).id('example:molten_rotor_fuels/compressed_peat')
})
```

A normal `/reload` reloads the recipe together with the rest of the datapack.
Queued fuels are resolved against the current recipe manager when they start.
A fuel that is already burning keeps the burn/heating values it started with.

## Overriding an existing CSR fuel

Use an ingredient that matches the existing item and a priority above other
matching definitions. Data-driven definitions take precedence over CSR's
hardcoded fallback regardless, while priority decides between multiple
matching data-driven definitions.

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'sulfuricresonance:molten_rotor_fuel',
    ingredient: { item: 'minecraft:coal' },
    burn_time_ticks: 500.0,
    heating_rate: 11.0,
    maximum_temperature: 525.0,
    maximum_units: 24,
    behavior: 'coal',
    priority: 1000
  }).id('example:molten_rotor_fuels/coal_rebalance')
})
```

## Disabling a fuel

An `enabled: false` definition suppresses all built-in fallback for matching
items. Stats are optional on disabled definitions.

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'sulfuricresonance:molten_rotor_fuel',
    ingredient: { item: 'minecraft:tnt' },
    enabled: false,
    priority: 1000
  }).id('example:molten_rotor_fuels/disable_tnt')
})
```

## Fields

- `ingredient` — required Minecraft ingredient (item or tag).
- `enabled` — optional, default `true`. `false` blocks fallback for matches.
- `burn_time_ticks` — burn duration in ticks. 20 ticks = 1 second.
- `heating_rate` — Molten Rotor heating rate in °C/s, using CSR's existing
  fuel semantics.
- `maximum_temperature` — highest temperature this fuel can drive the rotor to.
- `maximum_units` — how many units of this fuel definition may be active/queued.
- `behavior` — optional built-in behavior family, default `generic_medium`.
- `priority` — optional integer, default `0`; higher values win when multiple
  custom definitions match the same stack. Equal priority is resolved by
  recipe id for deterministic behavior.

Supported `behavior` values are the existing CSR fuel behavior ids:
`stick`, `log`, `coal`, `charcoal`, `coal_block`, `kelp_block`, `generic_low`,
`generic_medium`, `generic_high`, `tnt`, `blaze_cake`,
`soul_fired_blaze_cake`, `molten_ember_pellet`, `coke`, `infernal_coke`,
`carbon_deposit_block`, and `infernal_carbon_deposit_block`.

Behavior preserves special Rotor semantics where applicable. For example,
`stick` remains the log-boost behavior and the Blaze Cake behaviors retain
CSR's special queue/heat requirements. Merely using `tnt` behavior does not
make a non-TNT item explode the furnace; the existing explosion interaction
continues to check the actual TNT item.
