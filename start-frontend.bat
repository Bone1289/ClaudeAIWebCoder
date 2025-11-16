@echo off
REM Virtual Bank - Frontend Startup Script (Windows)
REM This script starts the Angular frontend

echo ========================================
echo Starting Virtual Bank Frontend...
echo ========================================
echo.

REM Check if Node.js is installed
node --version >nul 2>&1
if errorlevel 1 (
    echo Error: Node.js is not installed or not in PATH
    echo Please install Node.js 18+ and try again
    pause
    exit /b 1
)

REM Check if npm is installed
npm --version >nul 2>&1
if errorlevel 1 (
    echo Error: npm is not installed or not in PATH
    echo Please install npm and try again
    pause
    exit /b 1
)

REM Display versions
echo Node version:
node --version
echo.
echo npm version:
npm --version
echo.

REM Navigate to frontend directory
cd frontend
if errorlevel 1 (
    echo Error: Cannot find frontend directory
    pause
    exit /b 1
)

REM Check if node_modules exists
if not exist "node_modules" (
    echo Installing dependencies...
    npm install
    echo.
)

echo ========================================
echo Starting Angular Development Server...
echo ========================================
echo.
echo The app will open at: http://localhost:4200
echo Press Ctrl+C to stop
echo.

REM Start the Angular dev server
npm start

pause
