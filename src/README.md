# Create: Sulfuric Resonance

[CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-sulfuric-resonance)

[Modrinth](https://modrinth.com/mod/create-sulfuric-resonance)

[Issue Tracker](https://github.com/HelpfulMishk03/Create_Sulfuric_Resonance/issues)

**Create: Sulfuric Resonance**, or **CSR**, is a NeoForge addon for [Create](https://github.com/Creators-of-Create/Create) built around sulfur chemistry, thermochemical heat, advanced materials, and factory automation.

It adds another side to Create's progression: furnaces that need more than rotational power, heat networks that run through the factory, sulfuric acid production, reactive materials, and machinery that can report what it is doing.

CSR is meant to feel industrial, with a chemical appeal. Everything is designed around automation, extensibility, and extending an existing Create factory. Most machines use physical inputs, shafts, pipes, funnels, Mechanical Arms, redstone, and Engineer's Goggles rather than existing as isolated menu blocks or GUIs.

## Current release: 0.5.1

0.5.1 is a stabilization and balance update for **0.5.0 — Afterburn**.

It keeps the systems introduced in Afterburn while correcting several issues found through extended gameplay and compatibility testing.

### 0.5.1 changes

* Fixed the **Thermal Battery** being able to provide effectively unlimited Stress Units while generating rotation.
* Thermal Battery rotational output now has finite stress capacity:
    * **Heated:** 64 RPM / 2048 SU.
    * **Superheated:** 128 RPM / 4098 SU.
* Added Thermal Battery Engineer's Goggle information for generated RPM and stress capacity.
* Fixed missing Thermal Battery goggle translations across all supported languages.
* Fixed **Thermochemical Clutch** shaft lighting appearing excessively bright in some environments.
* Thermochemical Clutch shaft halves now use local world lighting instead of full-bright rendering.
* Changed the **Thermal Warning Alarm** recipe to use regular Minecraft Glass instead of Ashesil Glass.
* Continued dependency, compatibility, and general release cleanup.

## 0.5.0 — Afterburn

0.5.0 expanded the Molten Rotor Furnace and introduced a way to store thermochemical heat for later use.

### Afterburn highlights

* **Afterburn** — push the Molten Rotor Furnace beyond the normal Radiant ceiling of 1599 °C and up to 2000 °C with compatible fuels.
* **Thermite Charge** — a high-temperature fuel made for driving the Molten Rotor Furnace into Afterburn.
* **Brimstone Briquette** — a Molten Rotor Furnace fuel capable of reaching Radiant heat.
* **Thermal Battery** — stores Heated and Superheated thermochemical energy and releases it back into the network when needed.
* **Custom Molten Rotor fuels** — datapacks and KubeJS can add fuels with their own burn time, heating rate, maximum temperature, behavior, and priority.
* Expanded Molten Rotor Furnace fire effects, sound, Ponder scenes, tooltips, localization, and general polish.

Afterburn is a Molten Rotor Furnace state, not a new thermochemical heat tier. Radiant remains the highest normal network tier.

While above 1599 °C, the Furnace gains additional stress capacity and cools more slowly, but does not gain additional RPM or processing speed.

## Requirements

### Required

* Minecraft **1.21.1**
* NeoForge **21.1.238+**
* Create **6.0.7+**
* Java **21**

### Optional

* JEI **19.42.0.387+**
* EMI **1.1.24+**

JEI and EMI can be used separately or together.

## What you can build

### Thermochemical heat networks

Generate heat, move it through dedicated shafts and conduits, split or interrupt it, store it, monitor it, and use it alongside Create's kinetic network.

Thermochemical heat is part of the factory layout. Where it comes from and how it reaches a machine matters.

### Sulfur processing

Sulfur runs through much of CSR's progression: specialized fuels, Sulfuric Acid, high-temperature processing, ceramics, rubber, machine components, and advanced materials.

### Molten Rotor Furnace

The **Molten Rotor Furnace** acts as CSR's main thermochemical heat and rotational power source.

Different fuels determine how hot it can run, from lower thermochemical temperatures through Radiant and, with compatible fuels, into Afterburn.

As temperature rises, the Furnace produces progressively higher rotational output and stress capacity.

### Thermal storage

The **Thermal Battery** stores thermochemical heat instead of requiring every machine to remain connected to a live source at all times.

It connects through a single thermochemical shaft interface, stores Heated and Superheated reserves, responds to redstone, and keeps its stored heat when moved.

When powered and sufficiently charged, the Thermal Battery can also temporarily act as a kinetic source:

* **Heated:** 64 RPM / 2048 SU.
* **Superheated:** 128 RPM / 4098 SU.

The Battery only outputs Heated or Superheated thermochemical heat. It does not output Radiant or Afterburn heat.

### Resonance machinery

The **Sulfuric Resonance Chamber** combines heat, rotation, Sulfuric Acid, and specialized materials in one processing system.

The **Catalyst Bed** installs directly beneath the Chamber and speeds up live processing by 1.5× without replacing its existing recipes or automation.

### Reactive equipment

CSR includes several tools and materials that can be used directly or worked into Create automation.

* **Cinder Flare**
* **Sulfuric Acid Flask**
* **Pyroclast Bomb**
* **Thermite Charge**

### Smarter factories

Process Monitors, Process Gauges, Thermal Warning Alarms, Thermochemical Clutches, linked displays, and redstone output let a factory react to machine state instead of relying only on timers.

### Steam boiler integration

The Thermochemical Boiler Interface lets CSR heat feed Create steam boilers through connected heater arrays.

Steam Engine outputs can also act as local thermochemical sources through directly connected Link Drives.

## Automation

CSR machinery works with the tools already used in Create builds, including:

* Funnels and belts
* Deployers
* Mechanical Arms
* Fluid pipes and tanks
* Redstone
* Wrenches
* Engineer's Goggles
* Linked monitoring equipment
* Dispensers
* Potato Cannons where supported

The machines are made to sit inside real production lines, not beside a manual crafting station.

## Custom Molten Rotor fuels

Molten Rotor Furnace fuels can be extended through datapacks or KubeJS using CSR's:

`sulfuricresonance:molten_rotor_fuel`

Fuel definitions can control:

* Burn time
* Heating rate
* Maximum temperature
* Maximum queued units
* Fuel behavior
* Priority

This allows modpacks and custom datapacks to integrate their own materials into CSR without requiring direct code support.

## Learning the mod

Major machines and systems have in-game **Ponder** scenes covering setup, heat flow, controls, and automation.

Advancements provide a loose progression path, while Engineer's Goggles expose useful machine and network information. JEI and EMI include CSR's custom processing categories and animated machinery where appropriate.

Not every recipe or material chain is listed here on purpose. Some of CSR is better discovered by building it.

## Languages

CSR currently includes:

* English (US)
* English (UK)
* Spanish
* French
* German
* Portuguese (Brazil)
* Russian
* Simplified Chinese

Localization covers major machinery, tooltips, GUIs, advancements, Ponder scenes, and recipe-viewer text.

## Reporting issues

Reproducible bugs and compatibility problems can be reported through the [Issue Tracker](https://github.com/HelpfulMishk03/Create_Sulfuric_Resonance/issues).

When reporting an issue, include:

* CSR version
* Minecraft version
* NeoForge version
* Create version
* JEI or EMI version if relevant
* Relevant logs or crash reports
* Steps to reproduce the problem

---

Create: Sulfuric Resonance is independently developed and is not an official Create project.