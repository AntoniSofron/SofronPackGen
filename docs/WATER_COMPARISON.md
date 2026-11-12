# Water-generation comparison: vanilla, Tectonic, Terralith, Sofron Pack Gen

## Evidence

Minecraft Java 26.3 official notes: https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3
Aquifer mechanics: https://minecraft.wiki/w/Noise_cave
Tectonic config reference: https://github-wiki-see.page/m/Apollounknowndev/tectonic/wiki/Config
Terralith older noise settings (1.20; **not 26.3 compatible**): https://github.com/Stardust-Labs-MC/Terralith/blob/1.20/data/minecraft/worldgen/noise_settings/overworld.json

## Findings

- **Vanilla**: The global fluid rule places water up to sea level for empty terrain, while aquifers can override underground fluid status. Ocean aquifer cells are classified by comparing their position with sampled *local preliminary surface heights*. Aquifers do not themselves sculpt oceans or rivers.
- **Tectonic**: Its public configuration separates ocean/deep-ocean depth and continental terrain settings from aquifer/cave settings. This supports the architectural principle of sculpting the basin first, then letting the fluid rule fill it. It does not establish the exact 26.3 implementation.
- **Terralith**: Its publicly accessible 1.20 noise settings use vanilla-compatible density/noise-router architecture. This is historical comparative evidence, not a line-by-line verification of current 26.3 code.
- **Sofron Pack Gen**: `sea_level=64`, `default_fluid=minecraft:water`, `aquifers.exclusion=0`, but `aquifers.surface_level` was a constant 64 while `noise_router.chunk_surface_level` used `terrain/chunk_surface_level`. This split is suspicious: the aquifer sea-classification algorithm needs a local terrain-height estimate. The branch now uses the same local function for both fields.

## Caveats

The existing `terrain/chunk_surface_level` is only a rough height estimate (`80+24*offset`) and may not track river/lake carving, karst or final density. Consequently, this patch is an **unverified hypothesis**, not proof of correct oceans. Further work may require deriving a more accurate preliminary height from the actual density field.

## Runtime A/B protocol

1. Test **new worlds** with the same seed, without Distant Horizons.
2. Compare a dry open-sky ocean floor at Y=53. Record actual block at Y=54 and Y=63 and F3 biome.
3. If dry after this patch, perform a separate *diagnostic only* run with aquifers object omitted (global fluid rule). If water appears, the issue is definitely aquifer selection; if not, inspect generator version, density/sea level, chunk persistence, and other mods. Do **not** ship aquifers disabled as a final solution: it floods underground caves.
4. Re-enable aquifers after diagnosis and improve local surface estimate. Preserve underwater caves and dry inland caves.
