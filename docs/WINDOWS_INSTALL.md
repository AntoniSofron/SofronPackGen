# Windows BUILD_AND_INSTALL

Run `BUILD_AND_INSTALL.bat` from the repository root after installing **Java JDK 25** and **Gradle 9.5.0**. The repository currently does not include Gradle wrapper scripts, so a local Gradle installation is required.

Default installation target: `%APPDATA%\\.minecraft\\mods`.

To install into another Minecraft profile, pass its mods directory as the first argument:

```bat
BUILD_AND_INSTALL.bat "E:\\.minecraft\\mods"
```

The script runs `gradle --no-daemon clean build`, verifies the expected JAR and `fabric.mod.json` mod ID, backs up old Sofron Pack JARs into `sofron_pack_previous_jars` and copies the new JAR. If build or JAR verification fails, installed mods are not touched. If backup or copy fails, the script stops with an error.

**Caution:** This is a convenience installer, not a worldgen runtime test. Always back up worlds before running a changed generator. Keep Minecraft closed during installation. The default JAR name is `Sofron-Pack-Gen-2-2.0.0-alpha.1.jar`; update the script when the project version changes.
