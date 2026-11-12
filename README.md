# Sofron Pack Gen 2.0 — clean-room experimental rewrite

**Minecraft Java 26.3 / Fabric / Java 25.** This branch replaces the repository tree rather than layering on legacy SofronPack density functions. The only `minecraft:` worldgen override is `worldgen/noise_settings/overworld.json`. Vanilla biomes and vanilla material rules remain; there are no SofronPack biomes, structures or old karst resources.

## Current implementation
- New continent, archipelago, erosion, ridge, mountain and valley noises, using 26.3 `base_octave`, `octave_count`, `base_amplitude` and `amplitude_modifiers`.
- Continuous spline profiles without hard `range_choice` cliffs; Y=64 sea level; 384-block vertical range.
- Broad mountain ridges, coastal gradients, valleys, macro/micro relief, cave worm/chamber density and aquifers.
- Vanilla biome selection is **retained**, but climate inputs are replaced with custom continentalness, erosion and ridge functions.
- All worldgen functions are under `sofron_pack:` and referenced from the Overworld noise router.
- No third-party code, assets, custom biomes or old Sofron karst.

## Experimental / not yet implemented
This is an **alpha foundation, not the completed approved configuration**. The requested Biomes O' Plenty-inspired spires, Alpha Islands, Classic Valley regions, 100% underground river networks, navigable surface rivers, waterfalls, biome-specific terrain weighting, volcanoes, adjusted village frequency and 75% land target are **not yet implemented or verified**. They are recorded in `docs/approved-settings.json` for the next iterations. Do not interpret the slider percentages as native Minecraft parameters.

## Build
1. Install **JDK 25** and set `JAVA_HOME`.
2. Ensure Gradle wrapper is present (this branch intentionally contains no legacy wrapper binary; if absent use `gradle wrapper --gradle-version 9.7` with compatible Gradle installed).
3. Run `gradlew.bat build` on Windows or `./gradlew build` on Linux/macOS.
4. Place the produced JAR from `build/libs` in the Fabric mods folder.
5. **Create a new world** to compare results. Seed: `3880636525852909587`.

## Validation status
Static JSON structural checks and GitHub resource reference review only; no Minecraft runtime, registry codec or Gradle compilation has been performed. Expect additional integration work. In particular verify `noise_router.depth`, aquifer surface levels, cave thresholds and vanilla biome source compatibility before treating this as release-ready.

## Sources / compatibility
- Minecraft Java Edition 26.3 release notes: https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3
- Fabric 26.3 migration notes: https://www.fabricmc.net/2026/09/15/263.html
