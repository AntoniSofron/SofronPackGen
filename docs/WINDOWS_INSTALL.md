# Windows BUILD_AND_INSTALL

Run `BUILD_AND_INSTALL.bat` by double-clicking it from the extracted repository directory.

## Requirements

- **Java JDK 25** available on PATH.
- **Fabric API 0.166.2+26.3** installed in the target Minecraft profile (alongside Fabric Loader 0.20.1 or newer). The installer builds Sofron Pack Gen but does not install Fabric API.
- Internet connection for the first build (Gradle distribution, Fabric and Minecraft dependencies).
- No manual Gradle installation is necessary.

If a Gradle wrapper is present, the script uses it. Otherwise it downloads **Gradle 9.5.0** from the official Gradle distribution service into `.build-tools`, checks its published SHA-256 digest, and reuses it on subsequent runs.

Default installation target: `%APPDATA%\\.minecraft\\mods`.

To choose a different instance:

```bat
BUILD_AND_INSTALL.bat "E:\\Minecraft\\MyInstance\\mods"
```

The script runs a clean build, verifies the expected JAR and its `fabric.mod.json` ID, backs up matching old Sofron Pack JARs in `sofron_pack_previous_jars`, then copies the new JAR.

**Important:** Keep Minecraft closed, back up existing worlds, and use Minecraft 26.3 with Fabric Loader. This installer has not yet been run on a Windows test machine. A successful build does not establish that all worldgen features work at runtime.
