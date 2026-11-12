# Generator v2 implementation audit

The alpha/classic/island, river-valley, volcano, biome-weight, karst spire and cave noises are now connected to the Overworld final-density chain. Region masks use continuous splines to avoid hard discontinuities.

## Important: NOT 100% complete
- Surface river water filling, tributary network, lakes, waterfalls and flowing downhill require a coherent hydrology pass, not merely a valley density mask.
- Underground river water and aquifer consistency require an actual fluid-level implementation.
- Biome-specific height profiles need biome-source-aware sampling. The biome-weight noise is only an approximation.
- Volcano cones and lava flows need local structures/surface rules.
- Rebalanced biome source and structures need validated 26.3 registry integration.
- Spires use new original density functions inspired by BOP visual forms, not BOP source code.
- Full Minecraft registry decoding, runtime tests and terrain shape measurements have not been run.

**Do not merge until CI and game tests pass.**
