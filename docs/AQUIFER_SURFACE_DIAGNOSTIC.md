# Local surface height for aquifer diagnostics

The previous noise router used a constant `chunk_surface_level = 64`, regardless of the local terrain shape. This change routes the value to `sofron_pack:terrain/chunk_surface_level`, an existing approximation defined as `80 + 24 × terrain/offset`.

The approximation follows the zero-crossing of the base vertical gradient (from +6 at Y=-64 to -10 at Y=320, slope -1/24). **It is not an exact heightmap**: the final terrain also contains river, lake, karst and other modifiers. Aquifer `surface_level` remains 64, the intended sea level, so the local terrain estimate does not accidentally move the global fluid reference.

## Verification needed

Test with a **new world** and Distant Horizons disabled. At an open-sky basin with floor Y=53, check actual block types at Y=54 and Y=63, and F3 biome and coordinates. If air persists, this patch alone does not fix the missing water; investigate the aquifer floodedness/spread and any 26.3 codec semantics. Do not claim runtime success without this check.

This change does not implement complete river/lake filling or correct surface material placement.
