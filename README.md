# Create: Sulfuric Resonance

**Create: Sulfuric Resonance** is a NeoForge addon for [Create](https://github.com/Creators-of-Create/Create) centered around thermochemical heat, sulfur chemistry, industrial processing, and factory automation.

CSR adds its own heat network, machines, materials, sulfur processing, sulfuric acid production, resonance processing, and automation systems while keeping Create's mechanical factory style at the center of progression.

Not every recipe or progression step is documented here. JEI/EMI, advancements, tooltips, and Ponder are intended to handle most in-game documentation.

---

## Current Version

### **0.3.1 — Industrial Pulse**

0.3.1 is mainly a machinery and polish update built on the systems introduced in 0.3.0.

The main addition is the **Thermochemical Boiler Interface**, which allows CSR thermochemical heat networks to heat Create steam boilers.

The update also includes major animation, sound, lighting, particle, rendering, and feedback improvements across existing machines.

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

---

### Sulfur Chemistry

Sulfur is used throughout CSR for fuels, chemical processing, machine components, and advanced materials.

Current sulfur-related processing includes systems involving:

* Sulfur
* Sulfuric Acid
* Sulfur-based fuels
* High-temperature processing
* Acid treatment
* Fertilizer production
* Resonant materials

---

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

---

### Intelligent Industry

The 0.3.x releases added factory monitoring and control systems.

Machines can expose process states such as:

* Idle
* Ready
* Processing
* Blocked

Those states can be used by monitoring equipment, gauges, alarms, and redstone systems.

Current automation equipment includes the **Process Monitor**, **Process Gauge**, **Thermal Warning Alarm**, **Thermochemical Clutch**, and related network systems.

---

## 0.3.1 Highlights

### Thermochemical Boiler Interface

The Thermochemical Boiler Interface connects CSR heat networks to Create steam boilers.

Interfaces can form arrays from **1 to 9 blocks**, supporting boiler heater layouts up to Create's normal 3×3 limit.

A connected array shares one thermochemical heat input.

The Interface does not transmit kinetic rotation and does not add kinetic stress to the Create network.

Smaller layouts can have their shaft connection changed with a wrench, while larger 3×3 layouts use the appropriate middle faces.

---

### Sulfur Burner

The Sulfur Burner received a larger ignition and shutdown sequence with:

* Warmup behavior
* New sounds
* Improved particles
* Ignition lighting
* Stabilization effects
* Afterglow
* Improved fuel-state handling

---

### Sulfuric Resonance Chamber

The Chamber received a substantial presentation pass.

Changes include:

* Animated platform movement
* Different positions for operating states
* Startup and shutdown effects
* New machine sounds
* READY-state feedback
* Processing particles
* Completion effects
* Improved cooldown behavior
* Audio controls

---

### Thermochemical Clutch

The Thermochemical Clutch now has improved engagement and release behavior, including:

* Animated locking
* Smoother transitions
* Directional behavior
* New mechanical sounds

---

### Create Boiler Integration

CSR thermochemical networks can now contribute heat directly to Create steam boiler setups through the Boiler Interface.

Steam Engine output is still determined by Create's boiler system, water supply, boiler size, and attached engines.

---

## Other Machinery and Components

CSR currently includes systems such as:

* Sulfur Burner
* Sulfuric Resonance Chamber
* Thermochemical Shaft
* Thermochemical Cogwheel
* Large Thermochemical Cogwheel
* Parallel Thermochemical Gearbox
* Thermochemical Link Drive
* Thermochemical Clutch
* Thermochemical Boiler Interface
* Thermal Gauge
* Process Monitor
* Process Gauge
* Thermal Warning Alarm
* Precision Spritzer
* Resonant Heat Injector
* Relay Switch
* Ember Lamp

Along with additional materials, components, fuels, tools, and processing items.

---

## Create Integration

CSR machinery is intended to work inside normal Create factories.

Depending on the machine, CSR supports interaction with systems including:

* Funnels
* Mechanical Arms
* Fluid Pipes
* Spouts
* Basins
* Steam Boilers
* Steam Engines
* Redstone
* Wrenches
* Engineer's Goggles
* Kinetic networks

CSR's thermochemical heat network remains separate from Create's kinetic network unless a specific machine provides an interaction between the two systems.

---

## Ponder

CSR includes custom **Ponder** scenes for its major machines and systems.

These cover machine setup, thermochemical connections, processing requirements, automation, controls, and other important behavior.

0.3.1 includes **28 Ponder scenes**.

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

0.3.1 includes localization for:

* English (US)
* English (UK)
* German
* Spanish
* French
* Portuguese (Brazil)
* Russian
* Simplified Chinese

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

---

## Version

Current release:

**0.3.1 — Industrial Pulse**
