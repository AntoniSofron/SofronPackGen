# 26.3 Core Rewrite — water, biomes and terrain

## Reproduced failures

User screenshots (2026-11-12) show at least one open-sky basin at Y=53 with no standing water, despite sea_level=64; gigantic open stone basins and arches, and sculk/Deep Dark vegetation at the surface. These are actual worldgen problems, not just Distant Horizons LOD.

## Root design error

Previous active configuration independently replaced `continents`, `erosion`, `ridges`, `depth` and `final_density` while keeping the vanilla biome preset and surface material rules. The resulting climate/density maps were not calibrated to Vanilla's ocean/rivers/deep-dark selection. A below-sea-level depression does not necessarily receive ocean fluid if aquifer classification regards it as an inland cave or an area above its *estimated preliminary surface*.

Additional problems:
- The old estimated surface `80+24*terrain/offset` ignored river/lake depressions and other offsets from actual final density.
- A constant biome depth of 0 suppressed genuine underground biome selection and never fixed the water logic.
- Custom cave min-composition cut huge openings; disabling it improved terrain but erased all intended caves.
- River and lake density masks carved depressions with no hydrology/shoreline classification.
- Changing aquifer `surface_level` alone did not fix the mismatch in the source screenshot.

## Lessons from Tectonic and Terralith (design principles, not copied implementations)

Tectonic's documented settings distinguish **ocean/deep-ocean depth** from continent, erosion and ridge scaling. Its underground river feature is separate. Terralith's publicly available older datapack uses the game's coherent density/biome systems. Neither justifies independently combining arbitrary climate noise and uncalibrated caves.

Reference sources:
- https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3
- https://github.com/Apollounknowndev/tectonic
- https://github-wiki-see.page/m/Apollounknowndev/tectonic/wiki/Config
- https://github.com/Stardust-Labs-MC/Terralith/blob/1.20/data/minecraft/worldgen/noise_settings/overworld.json

Licensing: no mod code, configuration or assets were copied. Vanilla Minecraft references are the game's built-in registry keys.

## New active pipeline

| Responsibility | Source |
|---|---|
| All vanilla climate channels + underground depth | minecraft:overworld/* |
| Solid terrain and caves | minecraft:overworld/final_density plus restricted Sofron landform |
| Aquifer surface and preliminary chunk surface | minecraft:overworld/chunk_surface_level |
| Aquifer exclusion | Vanilla 26.3 erosion/depth conditions |
| Ocean depth and biome alignment | Vanilla 26.3 |
| Surface soils, snow, ore placement | minecraft:overworld material rule |
| Original additional relief | soffron original land-only mask, mountain profile and micro-hills |
| Sea level | 64 |

**Critical invariant:** new relief is exactly zero at continentalness <= -0.12 (open ocean), so custom terrain cannot cut giant ocean basins disconnected from ocean climate. It also decreases to zero outside the occupied terrain height range to avoid filling the sky or deep caves.

## Test plan (mandatory before a stable claim)

1. Build and confirm Minecraft 26.3 can load the registry, start a new world and generate chunks.
2. Seed `3880636525852909587`; check with F3 over a true deep ocean. At ocean XYZ, Y63 should be water where the sea floor is below it. Test Y54 in deep ocean.
3. Verify oceans are continuous, including along chunk boundaries. No large air-filled regions below sea level under the sky in ocean biomes.
4. Check normal grass surface and cave biome selection, including Deep Dark at depth (not on surface).
5. Check mountains, erosion, beaches and rivers on at least 4 different seeds. Check generation speed.
6. Test with and without Distant Horizons, and only *after* baseline passes, with the full mod setup.

## Known limitations

This solves architecture first; the earlier karst/river/lake/etc functions are disconnected. It does **not** claim all requested terrain settings have been implemented. Surface fluid level Y64 does not automatically fill every inland basin. Full inland river and lake networks require a coherent separate system and tests.
