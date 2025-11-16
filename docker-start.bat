@echo off
REM Virtual Bank - Docker Startup Script (Windows)
REM This script starts the entire application stack using Docker Compose

echo ==========================================
echo Starting Virtual Bank with Docker
echo ==========================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo Error: Docker is not installed or not in PATH
    echo Please install Docker Desktop and try again
    echo Download from: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

REM Check if Docker daemon is running
docker info >nul 2>&1
if errorlevel 1 (
    echo Error: Docker daemon is not running
    echo Please start Docker Desktop and try again
    pause
    exit /b 1
)

echo Docker is installed and running
echo.

REM Ask user what to do
echo Select an option:
echo 1) Start all services (MySQL + Backend + Frontend)
echo 2) Stop all services
echo 3) Restart all services
echo 4) View logs
echo 5) Clean up (remove containers and volumes)
set /p choice="Enter choice [1-5]: "

if "%choice%"=="1" (
    echo.
    echo Starting all services...
    echo This may take a few minutes on first run (building images)...
    echo.
    docker compose up -d --build
    echo.
    echo Services started successfully!
    echo.
    echo Service Status:
    docker compose ps
    echo.
    echo Access the application:
    echo    Frontend:  http://localhost
    echo    Backend:   http://localhost:8080/api
    echo    Health:    http://localhost:8080/actuator/health
    echo    MySQL:     localhost:3306 (user: root, password: root)
    echo.
    echo View logs with: docker compose logs -f
    echo Stop services with: docker compose down
) else if "%choice%"=="2" (
    echo.
    echo Stopping all services...
    docker compose down
    echo.
    echo All services stopped
) else if "%choice%"=="3" (
    echo.
    echo Restarting all services...
    docker compose restart
    echo.
    echo All services restarted
) else if "%choice%"=="4" (
    echo.
    echo Showing logs (Ctrl+C to exit)...
    echo.
    docker compose logs -f
) else if "%choice%"=="5" (
    echo.
    set /p confirm="This will remove all containers and data. Continue? (y/N): "
    if /i "%confirm%"=="y" (
        echo.
        echo Cleaning up...
        docker compose down -v
        echo.
        echo Cleanup complete
    ) else (
        echo Cancelled
    )
) else (
    echo Invalid choice
)

pause
