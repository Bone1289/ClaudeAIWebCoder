@echo off
REM Virtual Bank - Backend Startup Script (Windows)
REM This script starts the Spring Boot backend with MySQL (local profile)

echo ========================================
echo Starting Virtual Bank Backend...
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 21 and try again
    pause
    exit /b 1
)

REM Display Java version
echo Java version:
java -version
echo.

REM Navigate to backend directory
cd backend
if errorlevel 1 (
    echo Error: Cannot find backend directory
    pause
    exit /b 1
)

REM Check if we need to build
if not exist "build\libs\demo-0.0.1-SNAPSHOT.jar" (
    echo Building the application...
    gradlew.bat clean build -x test
    echo.
)

REM Ask user which profile to use
echo Select database profile:
echo 1) dev (H2 in-memory - no MySQL needed)
echo 2) local (MySQL - requires MySQL running)
echo 3) prod (Production MySQL)
set /p choice="Enter choice [1-3] (default: 1): "

if "%choice%"=="" set choice=1

if "%choice%"=="1" (
    set PROFILE=dev
) else if "%choice%"=="2" (
    set PROFILE=local
) else if "%choice%"=="3" (
    set PROFILE=prod
) else (
    echo Invalid choice, using 'dev' profile
    set PROFILE=dev
)

echo.
echo ========================================
echo Starting with profile: %PROFILE%
echo ========================================
echo.

REM Start the application
gradlew.bat bootRun --args="--spring.profiles.active=%PROFILE%"

pause
