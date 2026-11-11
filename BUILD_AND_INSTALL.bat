@echo off
setlocal EnableExtensions
cd /d "%~dp0"
echo === SOFRON PACK - BUILD AND INSTALL ===
echo Uses project Gradle wrapper (9.7.0), not system Gradle.
echo Installation target: E:\.minecraft\mods
call "%~dp0gradlew.bat" --no-daemon clean build
if errorlevel 1 (
 echo.
 echo BUILD FAILED. Nothing was changed in Minecraft mods.
 pause
 exit /b 1
)
set "JAR=%~dp0build\libs\26.3-Sofron-Pack-Gen-v0.1.jar"
if not exist "%JAR%" (
 echo ERROR: Built JAR not found: "%JAR%"
 pause
 exit /b 2
)
set "MODS=E:\.minecraft\mods"
if not exist "%MODS%" mkdir "%MODS%"
echo.
echo Checking generated JAR...
powershell -NoProfile -ExecutionPolicy Bypass -Command "try { Add-Type -AssemblyName System.IO.Compression.FileSystem -ErrorAction Stop; $z=[System.IO.Compression.ZipFile]::OpenRead($env:JAR); try { $bad=@($z.Entries | Where-Object { $_.FullName -match 'density_function/experimental/ridge_(detail|mask)\.json$' }); if($bad.Count -gt 0) { Write-Host 'ERROR: old experimental ridge JSON files found'; exit 3 }; Write-Host 'OK: generated JAR passed validation' } finally { if($null -ne $z) { $z.Dispose() } } } catch { Write-Host ('JAR validation error: ' + $_.Exception.Message); exit 3 }"
if errorlevel 1 (
 echo JAR validation failed. Nothing installed.
 pause
 exit /b 3
)
echo Backing up previous Sofron Pack jars...
set "BACKUP=%MODS%\sofron_pack_previous_jars"
if not exist "%BACKUP%" mkdir "%BACKUP%"
for %%F in ("%MODS%\sofron-pack*.jar" "%MODS%\sofron_pack-*.jar" "%MODS%\26.3-Sofron-Pack-Gen-*.jar") do (
 if exist "%%~fF" move /Y "%%~fF" "%BACKUP%\" >nul
)
copy /Y "%JAR%" "%MODS%\" >nul
if errorlevel 1 (
 echo Copy failed. Check Minecraft is closed.
 pause
 exit /b 4
)
echo.
echo SUCCESS: Installed "%MODS%\26.3-Sofron-Pack-Gen-v0.1.jar"
echo Minecraft should be closed before building. Start Minecraft 26.3 now.
pause
