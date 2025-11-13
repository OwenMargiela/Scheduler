@echo off
REM Launcher script for scheduler-cli on Windows

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

java -jar "%~dp0scheduler-cli.jar" %*
pause