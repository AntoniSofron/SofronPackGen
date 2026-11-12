# Sofron Pack Gen 2.0 — Minecraft 26.3 / Fabric

**Requires Fabric API 0.166.2+26.3 and Fabric Loader 0.20.1 or newer.**\n\nExperimental world-generation rewrite. **This branch replaces the active terrain, climate, cave and water pipeline after field tests showed dry ocean floors, stone deserts and cave biomes on the surface.** It is not yet the complete 2.0 feature set.

## Active generation architecture

- **Vanilla 26.3 climate router:** continentalness, erosion, ridges, temperature, vegetation **and depth all use the same vanilla inputs**. This restores consistency between surface biomes, cave biomes, coastline and material rules.
- **Vanilla 26.3 caves and natural aquifers:** the final density begins with `minecraft:overworld/final_density`, restoring vanilla cave handling; aquifer surface uses `minecraft:overworld/chunk_surface_level` with the same exclusion conditions as Vanilla 26.3.
- **Sea level Y=64:** ocean water selection is kept separate from terrain sculpting.
- **Original land-only relief:** on continental interiors, an erosion/ridge-weighted mountain profile and gentle 2D hills are added to the vanilla final density. The uplift mask is exactly zero over oceans/coasts and tapers to zero above and below the normal terrain range. Custom noise does not replace the biome inputs.
- **Vanilla surface materials and Vanilla biomes**; no copied Tectonic/Terralith code or assets.

The former experimental density-function files are retained for reference but are **not referenced by the active Overworld router**. These include previous cave, karst, alpha/classic, river/lake and volcano approximations; do not treat their presence as implementation.

## Build and install on Windows

Double-click `BUILD_AND_INSTALL.bat`. Requires Java 25 and internet access for initial Gradle/dependency downloads. The script installs into `%APPDATA%\\.minecraft\\mods` or the mods folder supplied as its first argument.

After installing, create **a new world**, not an existing save with previously generated terrain. For a controlled test, disable Distant Horizons and other world-generation mods, leaving Fabric Loader/API and Sofron Pack Gen.

## Test status

CI checks the JSON graph, Fabric JAR content and active router invariants **and starts a real Minecraft 26.3 Fabric server** with the mod data pack enabled. A server smoke test passed on seed `3880636525852909587`: a deep-ocean column contained actual `minecraft:water` at Y54, Y62 and Y63, and air at Y64. See `docs/CORE_REWRITE_26_3.md` and `docs/RUNTIME_TEST_RESULTS.md`. This is evidence for the tested area, **not a guarantee for all seeds, rivers or custom biomes**.

## Remaining requested features

The approved plan lives in `docs/approved-settings.json`. Real connected river networks, water-filled inland lakes above sea level, waterfalls, true under-river routes, dramatic BOP-style karst spires, Alpha/Classic regions, volcanoes, structure changes, targeted land ratio and exact mountain maxima remain future work. They must be built on the stable core and verified in-game before advertised.

## Research background

- Minecraft Java Edition 26.3 notes: https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3
- Tectonic (approach to ocean depth / terrain-scale separation): https://github-wiki-see.page/m/Apollounknowndev/tectonic/wiki/Config
- Terralith historical public worldgen reference (not reused as code): https://github.com/Stardust-Labs-MC/Terralith/blob/1.20/data/minecraft/worldgen/noise_settings/overworld.json
