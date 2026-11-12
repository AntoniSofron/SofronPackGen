# Surface biome-depth isolation experiment

The Overworld noise router previously assigned `depth` to `sofron_pack:terrain/base`. This is a **solid density field**, not the climate depth coordinate expected by vanilla biome selection. Using it can map the wrong depth value to biomes at the terrain surface.

This branch sets `noise_router.depth` to constant `0` as a diagnostic. It is deliberately **not the final solution**: it also suppresses genuine underground biome selection. A correct future implementation must calculate a normalized vertical climate depth relative to local surface height and restore underground biomes at suitable depths.

To compare, generate a brand-new world with the same seed and only Sofron Pack Gen + required Fabric dependencies, with Distant Horizons disabled. Check whether surface Deep Dark/sculk disappears. Also record F3 coordinates, actual water blocks and screenshots. This change does **not** fix sea filling, the surface material rule or exposed stone. Do not present this as full worldgen completion.
