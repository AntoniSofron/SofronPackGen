# 26.3 format and licensing audit

Verified against Mojang's Java 26.3 release notes and Fabric's 26.3 migration article:
- `material_rule` replaces `surface_rule`.
- `aquifers` is an object; `surface_level` is a density function.
- `noise_router.chunk_surface_level` replaces `preliminary_surface_level`.
- Noise registry: `base_octave`, `octave_count`, `base_amplitude`, `amplitude_modifiers`.
- The new project uses none of the removed `configured_feature` paths.
- No third-party mod source files or assets were copied. Original design influences are not a claim of source-code compatibility or licensing permission.

Important limitations:
- No automated Minecraft registry decoding or Gradle build has been run.
- The `minecraft:overworld` material rule and `minecraft:overworld/temperature`/vegetation density functions must resolve in a 26.3 runtime.
- A density function's type being syntactically valid does not establish desired terrain shape.
- The user-approved configuration is a design specification; implemented parameters are documented in README.
