@echo off
setlocal
cd /d "%~dp0"
echo ==============================================
echo   SOFRON TERRA LAB - BUILD
echo ==============================================
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build.ps1"
set "result=%ERRORLEVEL%"
if not "%result%"=="0" echo Build failed with exit code %result%.
echo.
pause
exit /b %result%
