# Runtime validation — Minecraft Java 26.3 / Fabric

## Evidence collected on 2026-11-12

**Compile and JSON validation:** PASS with Java 25 / Gradle 9.5.0 / Fabric Loom 1.17.

**Dedicated Minecraft runtime:** PASS in GitHub Actions. Seed `3880636525852909587`, generated **new** world, Fabric API `0.166.2+26.3` and Fabric Loader `0.20.1`.

The game console confirmed the mod entrypoint and enabled resource pack:
```
[sofron_pack (Fabric mod)]
```

A real server command `locate biome minecraft:deep_ocean` found X=640, Z=32. RCON checked **actual blocks**, not distant-horizon LODs:

| X | Z | Y=54 water | Y=63 water | Deep ocean at Y63 |
|---|---|---|---|---|
| 640 | 32 | yes | yes | yes |
| 648 | 40 | yes | yes | yes |
| 632 | 24 | yes | yes | no |
| 656 | 32 | yes | yes | yes |
| 640 | 48 | yes | yes | no |

The complete column at X=640, Z=32 contained water at Y49–Y63, with air at Y64–66.

Passing workflow run:
https://github.com/AntoniSofron/SofronPackGen/actions/runs/43284381442

The test is subsequently strengthened to require water at both Y54 and Y63 **in every location where the Y63 biome is confirmed as `minecraft:deep_ocean`**. CI runs this assertion on changes to generation resources.

## False positives caught

With Fabric Loader but **without Fabric API Resource Loader**, `datapack list enabled` showed only `vanilla (built-in)`, even though the mod initializer ran. The test then saw the **vanilla** water surface ending at Y62. This could not validate Sofron worldgen. Fabric API is now a declared runtime dependency, and CI rejects any run without the `sofron_pack` data pack active.

Fabric Resource Loader documentation: https://maven.fabricmc.net/docs/fabric-api-0.144.3%2B26.1/net/fabricmc/fabric/api/resource/package-summary.html

## What this does *not* prove

- Dry inland basins and river systems, lakes above sea level, waterfalls.
- The precise shape of mountains, karst spires and volcanoes.
- Zero sculk on every possible surface seed, placement or coastline.
- Distant Horizons LOD consistency.
- Compatibility with other terrain-overhaul mods and old world chunks.

For these, new worlds, multiple seeds and cross-mod tests remain necessary. Do not advertise complete Sofron Pack Gen 2.0 feature coverage.
