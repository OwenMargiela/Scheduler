@echo off
REM Launcher script for scheduler-cli on Windows
REM Checks for Java and keeps window open to show errors

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo.
    echo Error: Java is not installed or not in PATH
    echo.
    echo Please download and install Java 11+ from:
    echo https://adoptium.net/
    echo.
    echo Make sure to check "Add to PATH" during installation.
    echo.
    pause
    exit /b 1
)

java -jar "%~dp0scheduler-cli.jar" %*
pause
