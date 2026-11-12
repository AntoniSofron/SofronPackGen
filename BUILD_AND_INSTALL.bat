@echo off
setlocal
cd /d "%~dp0"
echo ===================================================
echo  SOFRON TERRA LAB - BUILD AND INSTALL
echo ===================================================
if "%~1"=="" (
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build.ps1" -Install
) else (
  powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build.ps1" -Install -ModsDir "%~1"
)
set "result=%ERRORLEVEL%"
if not "%result%"=="0" echo Build or install failed with exit code %result%.
echo.
pause
exit /b %result%
