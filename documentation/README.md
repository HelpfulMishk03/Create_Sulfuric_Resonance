# CSR developer documentation

This directory is developer documentation for Create: Sulfuric Resonance. It describes ownership, integration boundaries, and release checks. It is not a player wiki.

| Document | Use it when |
| --- | --- |
| [Architecture](architecture.md) | You need to find the owner of a rule or understand the mod's top-level shape. |
| [Thermochemical systems](thermochemical-systems.md) | You are modifying heat, rotation, the Molten Rotor, networks, or the Thermal Battery. |
| [Machines and processing](machines-and-processing.md) | You are modifying recipes, machines, fluids, items, or progression. |


## Documentation rules

- Update the relevant document in the same pull request as an ownership change, new invariant, new persistent field, or integration boundary.
- Keep player instructions in Ponder, tooltips, the future wiki, and public release pages. Keep implementation contracts here.
- Prefer a concise invariant over prose that describes an accidental implementation.
- A successful Gradle build proves compilation and resource processing. It does not prove a client, server, render, network, datapack reload, or migration path.
