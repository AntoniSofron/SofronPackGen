@echo off
setlocal EnableExtensions DisableDelayedExpansion
cd /d "%~dp0"
echo =========================================================
echo   SOFRON PACK GEN 2.0 - BUILD AND INSTALL
echo =========================================================
echo.
set "MODS_DIR=%APPDATA%\.minecraft\mods"
if not "%~1"=="" set "MODS_DIR=%~1"
set "GRADLE_VERSION=9.5.0"
set "GRADLE_HOME=%CD%\.build-tools\gradle-%GRADLE_VERSION%"
set "GRADLE_EXE=%GRADLE_HOME%\bin\gradle.bat"
set "JAR_NAME=Sofron-Pack-Gen-2-2.0.0-alpha.1.jar"
set "JAR_PATH=%CD%\build\libs\%JAR_NAME%"
echo Target: "%MODS_DIR%"
echo.
where java >nul 2>&1
if errorlevel 1 (
  echo ERROR: Java 25 JDK not found. Install Java 25.
  goto :fail
)
java -version
echo.
if exist "gradlew.bat" (
  echo Using project Gradle wrapper...
  call "%CD%\gradlew.bat" --no-daemon clean build
  if errorlevel 1 goto :build_fail
  goto :verify
)
if not exist "%GRADLE_EXE%" (
  echo Gradle %GRADLE_VERSION% not cached. Downloading official distribution...
  powershell -NoProfile -ExecutionPolicy Bypass -File "%CD%\scripts\install_gradle.ps1" -Version "%GRADLE_VERSION%" -Destination "%CD%\.build-tools"
  if errorlevel 1 (
    echo ERROR: Unable to install Gradle automatically.
    goto :fail
  )
)
if not exist "%GRADLE_EXE%" (
  echo ERROR: Gradle executable not found after installation.
  goto :fail
)
echo Building with Gradle %GRADLE_VERSION%...
call "%GRADLE_EXE%" --no-daemon clean build
if errorlevel 1 goto :build_fail
goto :verify

:build_fail
echo ERROR: Build failed. Installed mods were not changed.
goto :fail

:verify
if not exist "%JAR_PATH%" (
  echo ERROR: Expected JAR missing: "%JAR_PATH%"
  dir /b "build\libs\*.jar" 2>nul
  goto :fail
)
echo.
echo Verifying Fabric metadata...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Add-Type -AssemblyName System.IO.Compression.FileSystem; $z=[IO.Compression.ZipFile]::OpenRead($env:JAR_PATH); try { $e=$z.GetEntry('fabric.mod.json'); if(-not $e){throw 'fabric.mod.json missing'}; $r=New-Object IO.StreamReader($e.Open()); try{$m=$r.ReadToEnd() | ConvertFrom-Json}finally{$r.Dispose()}; if($m.id -ne 'sofron_pack'){throw 'Wrong mod id'}; Write-Host ('Verified mod id: ' + $m.id) } finally { $z.Dispose() }"
if errorlevel 1 (
  echo ERROR: JAR validation failed. Installed mods were not changed.
  goto :fail
)
if not exist "%MODS_DIR%" mkdir "%MODS_DIR%"
if not exist "%MODS_DIR%" goto :fail
set "BACKUP_DIR=%MODS_DIR%\sofron_pack_previous_jars"
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
if not exist "%BACKUP_DIR%" goto :fail
echo Backing up previous Sofron Pack JARs...
for %%F in ("%MODS_DIR%\Sofron-Pack-Gen-2-*.jar" "%MODS_DIR%\26.3-Sofron-Pack-Gen-*.jar" "%MODS_DIR%\sofron-pack*.jar" "%MODS_DIR%\sofron_pack-*.jar") do (
  if exist "%%~fF" (
    move /Y "%%~fF" "%BACKUP_DIR%\" >nul
    if errorlevel 1 goto :fail
  )
)
copy /Y "%JAR_PATH%" "%MODS_DIR%\%JAR_NAME%" >nul
if errorlevel 1 goto :fail
echo.
echo SUCCESS: "%MODS_DIR%\%JAR_NAME%"
echo Start Minecraft 26.3 with Fabric Loader.
pause
exit /b 0

:fail
echo.
echo BUILD / INSTALL FAILED.
pause
exit /b 1
