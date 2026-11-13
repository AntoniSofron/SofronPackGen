# Sofron Terra Lab — active terrain implementation (Minecraft 26.3)

## Contract
- Uses the owner's unmodified 84-parameter approval in `config/approved-worldgen-spec.json`.
- Vanilla biomes and Vanilla structures **only**. No custom biome or structure registries.
- Native Minecraft 26.3 climate channels, aquifers and cave density (see router in `overworld.json`). The world ceiling is Y=320, floor Y=-64, sea level Y=64.
- Original additive landforms layered on top of native final density. No reused old SofronPackGen terrain functions.
- Continent/biome source remains Vanilla to preserve ocean classification and underground-biome safety. Land mask is zero in deep ocean.

## Connected terrain modules
| System | Active density function | Implementation | Limit |
|---|---|---|---|
| Mountain belts | terrain/mountain_ranges | erosion/ridge and regional-noise gating | Peak Y=256 is target, not enforced maximum |
| Classic valleys | terrain/classic_valleys | soft density depression away from oceans | Not a flow-directed drainage model |
| Beta cliffs / overhangs | terrain/beta_overhangs | inland 3D warped relief limited by altitude | Shape needs screenshots |
| Karst columns | terrain/karst_spires | original bounded noise footprint, region clustering, proximity to existing ground | Not yet BOP feature-level pillars |
| Alpha-like coastal landforms | terrain/alpha_coastal_islets | localized shaped relief at vanilla coasts | Not independent oceanic islands; avoid ocean biome mismatches |
| Ocean trenches | terrain/ocean_trenches | original offshore sea-floor deepening | Must be water-checked in 26.3 runtime |
| Microrelief | terrain/micro_hills | bounded 2D small-scale relief | Calibrate against terrain screenshots |

## Not completed
Land fraction 60%, explicit continentScale=20, exact Y256 peak limit, watershed algorithm, navigable rivers, tributaries and lake networks, waterfalls, underground river paths, terrain-aware villages, custom river beaches, sky islands, and quantitative LOD tests **are recorded requirements not yet implemented**. River water at sea level uses native Vanilla routing. Please do not equate a density noise depression with a flowing river.

## Acceptance gates
1. Gradle compiles Minecraft 26.3 Fabric API Java 25.
2. Server loads `sofron_terra_lab` datapack and vanilla biome/structure registries.
3. At least 5 verified ocean biome positions contain actual water at Y54 and Y63.
4. No exposed Deep Dark in sampled surface positions.
5. Compare fresh world, 4+ seeds, land coverage, ocean depth, height profile, cliffs, karst, and structure integrity.

Runtime tests can reveal codec/geometry problems despite successful JSON parsing. Keep implementation PR as draft until world tests pass.

## Measured CI result (2026-11-13)

The actual Minecraft 26.3 Fabric server started successfully on seeds `3880636525852909587`, `123456789` and `-987654321` with the `sofron_terra_lab` data pack active. All three test jobs found real water blocks at Y54 and Y63 in sampled `minecraft:deep_ocean` positions, and did not find Deep Dark biome high in the air at the sampled coordinates. Compilation and JAR resource tests also passed.

**Important:** this is a limited automated smoke test, not a full visual test or verification of every one of the 84 choices. Images from the configurator remain conceptual illustrations. Hydrology implementation is specifically pending. See `docs/PARAMETER_COVERAGE.md`.
