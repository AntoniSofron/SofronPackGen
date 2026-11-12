# Local build archive audit — 2026-11-12

The user-provided `SofronPackGen-main.zip` contains a Gradle 9.5.0 distribution, `.gradle/` caches, compiled Java classes, resources and `build/libs/Sofron-Pack-Gen-2-2.0.0-alpha.1.jar` (27,105 bytes). Its source scripts correspond to the current main branch after PR #14.

**Action:** Keep downloaded Gradle, caches, build outputs and Minecraft saves out of source control via `.gitignore`. CI now verifies the distributable JAR contains `fabric.mod.json`, the Java initializer class and Overworld noise settings, and uploads the compiled JAR as a downloadable GitHub Actions artifact.

The uploaded JAR's presence is evidence of a local build output, **not** evidence of successful mod loading, terrain generation, world creation, correct fluids or complete settings implementation. No generated binary or Gradle distribution has been committed to the repository.
