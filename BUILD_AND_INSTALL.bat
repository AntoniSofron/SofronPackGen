@echo off
setlocal EnableExtensions DisableDelayedExpansion
cd /d "%~dp0"
echo =========================================================
echo   SOFRON PACK GEN 2.0 - BUILD AND INSTALL
echo =========================================================
echo.
set "MODS_DIR=%APPDATA%\.minecraft\mods"
if not "%~1"=="" set "MODS_DIR=%~1"
set "JAR_NAME=Sofron-Pack-Gen-2-2.0.0-alpha.1.jar"
set "JAR_PATH=%CD%\build\libs\%JAR_NAME%"
echo Target: "%MODS_DIR%"
echo.
where java >nul 2>&1
if errorlevel 1 (
  echo ERROR: Java not found on PATH. Install Java JDK 25.
  goto :fail
)
java -version
echo.
if exist "gradlew.bat" (
  echo Using project Gradle wrapper...
  call "%CD%\gradlew.bat" --no-daemon clean build
) else (
  where gradle >nul 2>&1
  if errorlevel 1 (
    echo ERROR: Gradle not found. Install Gradle 9.5.0 and add it to PATH.
    echo This repository does not currently include the Gradle wrapper.
    goto :fail
  )
  echo Using system Gradle. Required version: 9.5.0
  for /f "tokens=2" %%V in ('gradle --version ^| findstr /b /c:"Gradle 9.5.0"') do set "GRADLE_VER=%%V"
  if not "%GRADLE_VER%"=="9.5.0" (
    echo ERROR: Expected Gradle 9.5.0. Run gradle --version to check.
    goto :fail
  )
  gradle --no-daemon clean build
)
if errorlevel 1 (
  echo ERROR: Build failed. No existing mod files were changed.
  goto :fail
)
if not exist "%JAR_PATH%" (
  echo ERROR: Expected JAR missing: "%JAR_PATH%"
  echo Files in build\libs:
  dir /b "build\libs\*.jar" 2>nul
  goto :fail
)
echo.
echo Verifying JAR archive and Fabric metadata...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Add-Type -AssemblyName System.IO.Compression.FileSystem; $z=[IO.Compression.ZipFile]::OpenRead($env:JAR_PATH); try { $e=$z.GetEntry('fabric.mod.json'); if(-not $e){throw 'fabric.mod.json missing'}; $r=New-Object IO.StreamReader($e.Open()); try{$m=$r.ReadToEnd() | ConvertFrom-Json}finally{$r.Dispose()}; if($m.id -ne 'sofron_pack'){throw 'Wrong mod id'}; Write-Host ('Verified mod id: ' + $m.id) } finally { $z.Dispose() }"
if errorlevel 1 (
  echo ERROR: JAR validation failed. No mods were changed.
  goto :fail
)
if not exist "%MODS_DIR%" mkdir "%MODS_DIR%"
if not exist "%MODS_DIR%" (
  echo ERROR: Could not create target mods directory.
  goto :fail
)
set "BACKUP_DIR=%MODS_DIR%\sofron_pack_previous_jars"
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
if not exist "%BACKUP_DIR%" (
  echo ERROR: Could not create backup directory.
  goto :fail
)
echo.
echo Backing up existing Sofron Pack JARs...
for %%F in ("%MODS_DIR%\Sofron-Pack-Gen-2-*.jar" "%MODS_DIR%\26.3-Sofron-Pack-Gen-*.jar" "%MODS_DIR%\sofron-pack*.jar" "%MODS_DIR%\sofron_pack-*.jar") do (
  if exist "%%~fF" (
    move /Y "%%~fF" "%BACKUP_DIR%\" >nul
    if errorlevel 1 (
      echo ERROR: Backup failed for "%%~fF". Installation aborted.
      goto :fail
    )
  )
)
copy /Y "%JAR_PATH%" "%MODS_DIR%\%JAR_NAME%" >nul
if errorlevel 1 (
  echo ERROR: Copy failed. Check that Minecraft is closed.
  goto :fail
)
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
