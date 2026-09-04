# Create: Sulfuric Resonance

**Create: Sulfuric Resonance** is a NeoForge addon for [Create](https://github.com/Creators-of-Create/Create) centered around thermochemical heat, sulfur chemistry, industrial processing, and factory automation.

CSR adds its own heat network, machines, materials, sulfur processing, sulfuric acid production, resonance processing, and automation systems while keeping Create's mechanical factory style at the center of progression.

Not every recipe or progression step is documented here. JEI/EMI, advancements, tooltips, and Ponder are intended to handle most in-game documentation.

---

## Current Version

### **0.4.1 — Adjusted Equilibrium**

0.4.1 builds on Reactive Tools with additional automation support and final Cinder Flare polish.

Cinder Flares can now be ignited automatically on moving belts using a **Deployer holding Flint and Steel**.

Dispensers can also launch **Lit Cinder Flares**, **Sulfuric Acid Flasks**, and **Pyroclast Bombs**, allowing the Reactive Tools introduced in 0.4.0 to be incorporated directly into automated Create factories.

The update also adds dedicated Ponder scenes for Reactive Tool automation, localized automation hints, an updated Cinder Flare recipe, and improved Cinder Flare ignition presentation.

---

## 0.4.1 Highlights

### Cinder Flare Automation

Cinder Flares can now be ignited automatically while moving on belts.

A **Deployer holding Flint and Steel** can strike and ignite an unlit Cinder Flare as part of a production line.

### Reactive Tool Dispenser Support

Dispensers can now launch:

* Lit Cinder Flares
* Sulfuric Acid Flasks
* Pyroclast Bombs

This allows Reactive Tools to be triggered using ordinary redstone and factory automation.

### Cinder Flare Recipe

The Cinder Flare recipe has been updated to include a **Slimeball** and use a more coherent crafting layout.

### Cinder Flare Polish

The Cinder Flare ignition animation has been corrected so the flare is presented and struck at the proper end.

Third-person presentation has also been adjusted to better match the intended ignition sequence.

### Ponder and Tooltips

0.4.1 adds dedicated Ponder scenes covering:

* Cinder Flare ignition automation
* Reactive Tool launching

Reactive Tools also include localized automation hints across CSR's supported languages.

---

## Reactive Tools

### Catalyst Bed

The Catalyst Bed is a physical Chamber upgrade.

It accelerates active Chamber recipes by **1.5×** when placed directly underneath the machine.

The effect changes live if the Bed is added or removed during processing, does not reset progress, and cannot stack with additional Beds.

### Cinder Flare

Cinder Flares are lit with Flint and Steel in the off hand through a staged striking sequence.

Once lit, they can be thrown and continue burning where they land.

Cinder Flares can also be ignited automatically on moving belts using a Deployer holding Flint and Steel.

### Sulfuric Acid Flask

Sulfuric Acid Flasks are filled with **250 mB** of Sulfuric Acid and thrown to create a localized corrosive impact.

They apply Acid Burn and can remove one oxidation stage from nearby copper blocks.

### Pyroclast Bomb

The Pyroclast Bomb has a short wind-up and an arcing throw.

On impact, it deals controlled damage, restrained knockback, limited ignition, and can destroy up to five eligible blocks.

Water extinguishes the Bomb before detonation, allowing it to be recovered.

Pyroclast Bombs can also be fired from Create's Potato Cannon or launched from Dispensers.

---

## Requirements

### Required

* Minecraft **1.21.1**
* NeoForge **21.1.238+**
* Create **6.0.7+**
* Java **21**

### Optional

* JEI **19.42.0.387+**
* EMI **1.1.24+**

---

## Main Systems

### Thermochemical Heat

CSR has its own heat network separate from Create's kinetic network.

Thermochemical heat can be generated, transmitted, redirected, monitored, and consumed by CSR machinery.

The network includes dedicated shafts, cogwheels, gearboxes, control blocks, gauges, alarms, and other components.

### Sulfur Chemistry

Sulfur is used throughout CSR for fuels, chemical processing, machine components, and advanced materials.

Current sulfur-related processing includes:

* Sulfur
* Sulfuric Acid
* Sulfur-based fuels
* High-temperature processing
* Acid treatment
* Fertilizer production
* Resonant materials

### Sulfuric Resonance Chamber

The **Sulfuric Resonance Chamber** is one of CSR's main processing machines.

Recipes can require a combination of:

* Sulfuric Acid
* Thermochemical heat
* Rotational speed
* Input materials
* Catalysts
* Auxiliary ingredients

The Chamber has its own recipe type and recipe-viewer integration.

### Intelligent Industry

CSR machines can expose process states such as:

* Idle
* Ready
* Processing
* Blocked

Those states can be used by monitoring equipment, gauges, alarms, and redstone systems.

Current automation equipment includes the **Process Monitor**, **Process Gauge**, **Thermal Warning Alarm**, **Thermochemical Clutch**, and related network systems.

---

## Other Machinery and Components

CSR currently includes systems such as:

* Sulfur Burner
* Catalyst Bed
* Thermochemical Shaft
* Thermochemical Cogwheel
* Large Thermochemical Cogwheel
* Parallel Thermochemical Gearbox
* Thermochemical Link Drive
* Thermochemical Clutch
* Thermochemical Boiler Interface
* Thermal Gauge
* Process Monitor
* Thermal Warning Alarm
* Resonant Heat Injector
* Relay Switch
* Cinder Flare
* Sulfuric Acid Flask
* Pyroclast Bomb

Along with additional materials, components, fuels, tools, and processing items, plus more not mentioned here.

---

## Create Integration

CSR machinery is intended to work inside normal Create factories.

Depending on the machine, CSR supports interaction with systems including:

* Funnels
* Belts
* Deployers
* Mechanical Arms
* Fluid Pipes
* Spouts
* Basins
* Steam Boilers
* Steam Engines
* Potato Cannons
* Dispensers
* Redstone
* Wrenches
* Engineer's Goggles
* Kinetic networks

CSR's thermochemical heat network remains separate from Create's kinetic network unless a specific machine provides an interaction between the two systems.

---

## Ponder

CSR includes custom **Ponder** scenes for its major machines and systems.

These cover machine setup, thermochemical connections, processing requirements, automation, controls, and other important behavior.

Current Ponder documentation also includes:

* Catalyst Bed operation
* Cinder Flare ignition
* Deployer-based Cinder Flare automation
* Reactive Tool launching

---

## Recipe Viewer Support

CSR supports:

* **JEI**
* **EMI**

Both are optional.

CSR includes custom recipe categories for its processing systems, including the Sulfuric Resonance Chamber.

---

## Advancements

CSR includes its own advancement tree covering machinery, processing, materials, progression, and selected challenges.

---

## Localization

0.4.1 includes localization for:

* English (US)
* English (UK)
* German
* Spanish
* French
* Portuguese (Brazil)
* Russian
* Simplified Chinese

Localization covers major machinery, items, tooltips, GUIs, subtitles, advancements, Ponder scenes, and automation hints.

---

## Compatibility

```text
Minecraft 1.21.1
NeoForge 21.1.238+
Create 6.0.7+
Java 21

JEI 19.42.0.387+ (optional)
EMI 1.1.24+ (optional)
```

