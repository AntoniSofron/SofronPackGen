# Sofron Terra Lab — active terrain implementation (Minecraft 26.3)

## Contract
- Uses the owner's unmodified 84-parameter approval in `config/approved-worldgen-spec.json`.
- Vanilla biomes and Vanilla structures **only**. No custom biome or structure registries.
- Native Minecraft 26.3 climate channels, aquifers and cave density (see router in `overworld.json`). The world ceiling is Y=320, floor Y=-64, sea level Y=63.
- Original inland uplift layered on Vanilla final density, combined using max with an anchored solid mountain core. No reused old SofronPackGen terrain functions.
- Continent/biome source remains Vanilla to preserve ocean classification and underground-biome safety. Land mask is zero in deep ocean.

## Connected terrain modules
| System | Active density function | Implementation | Limit |
|---|---|---|---|
| Mountain belts | terrain/mountain_ranges | erosion/ridge and regional-noise gating; monotonic solid column anchored from Y~33, target summit Y256 | Peak Y=256 is target for strong ridge cores, not a hard Vanilla height cap |
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
3. At least 5 verified ocean biome positions contain actual water at Y54 and Y62, air at Y63.
4. No exposed Deep Dark in sampled surface positions.
5. Compare fresh world, 4+ seeds, land coverage, ocean depth, height profile, cliffs, karst, and structure integrity.

Runtime tests can reveal codec/geometry problems despite successful JSON parsing. Keep implementation PR as draft until world tests pass.

## Measured CI result (2026-11-13)

The actual Minecraft 26.3 Fabric server started successfully on seeds `3880636525852909587`, `123456789` and `-987654321` with the `sofron_terra_lab` data pack active. All three test jobs found real water blocks at Y54 and Y63 in sampled `minecraft:deep_ocean` positions, and did not find Deep Dark biome high in the air at the sampled coordinates. Compilation and JAR resource tests also passed.

**Important:** this is a limited automated smoke test, not a full visual test or verification of every one of the 84 choices. Images from the configurator remain conceptual illustrations. Hydrology implementation is specifically pending. See `docs/PARAMETER_COVERAGE.md`.

## 2026-11-13 — connected peak and sea level redesign

- A screenshot exposed a broad forested floating landmass around the user's perceived Y256 summit zone. The old `mountain_ranges` was simply added as a positive Y-band, allowing a separate density island instead of a mountain connected to the ground.
- New `mountain_strength` is **purely XZ** (Vanilla continentalness/erosion/ridge + the Sofron regional mask). New `mountain_ranges` forms a solid core from the ground upward: `min(gradient(Y80=-0.04,Y320=-1.24) + 0.92*mountain_strength, anchor_gradient(Y24=-0.4,Y64=1.4))`.
- The connected core is combined as `max(vanilla_final_density + all_other_relief, mountain_ranges)`, not by adding a pulse. At mountain_strength=1, the core's positive density ends at Y256. There is no positive high-altitude mountain density above this point; Vanilla terrain itself is not clipped.
- `upland_lift` adds a broader, coast-masked +0.20 density uplift near the surface (fades out by Y224) to make non-mountain inland terrain less flat.
- Beta three-dimensional overhang detail fades out completely by Y170. Original karst and surface bumps are also bounded below Y215. Intentional sky islands remain unimplemented; this accidental forested floating mass is not intended.
- Ocean level lowered from 64 to 63, so on natural deep ocean blocks Y54 and Y62 must be water and Y63 should be air.
- Important trade-off: anchored mountain mass fills some naturally occurring upper-level caves within mountain cores. Deep Vanilla caves and caves outside these cores are preserved. More precise cave reintroduction can follow after geometry is verified.
- Static `scripts/check_grounded_mountains.py` proves a continuous Y interval of positive mountain density for representative strength values; actual multi-seed Minecraft server smoke tests check water and loading. Screenshots/measurements are still required for visual acceptance.
