# Hydrology implementation status

## Implemented in this branch

- Independent tributary axis with secondary meander noise.
- Tributary valley carving coupled to distance from the primary river axis, to reduce overlap at junctions.
- Erosion- and climate-masked lake basin depressions.
- Connected all of these density functions to the Overworld surface-density chain.
- Static CI checks for density-function references and wiring.

## Not implemented or validated

These are **topographic approximations**, not a real hydrology simulation. There is no flow direction, watershed accumulation, downhill continuity, explicit surface water fill, water level for lakes above sea level, waterfalls, underground water transport, river source/sink constraints, or verified joining tributaries. Aquifers do not provide those guarantees.

No claim of a fully navigable river system or 100% settings coverage. Runtime terrain tests are still necessary. Avoid merging if CI fails.
