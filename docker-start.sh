#!/bin/bash

# Virtual Bank - Docker Startup Script
# This script starts the entire application stack using Docker Compose

echo "=========================================="
echo "🐳 Starting Virtual Bank with Docker"
echo "=========================================="
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Error: Docker is not installed or not in PATH"
    echo "Please install Docker Desktop and try again"
    echo "Download from: https://www.docker.com/products/docker-desktop"
    exit 1
fi

# Check if Docker Compose is available
if ! docker compose version &> /dev/null; then
    echo "❌ Error: Docker Compose is not available"
    echo "Please install Docker Compose and try again"
    exit 1
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    echo "❌ Error: Docker daemon is not running"
    echo "Please start Docker Desktop and try again"
    exit 1
fi

echo "✅ Docker is installed and running"
echo ""

# Ask user what to do
echo "Select an option:"
echo "1) Start all services (MySQL + Backend + Frontend)"
echo "2) Stop all services"
echo "3) Restart all services"
echo "4) View logs"
echo "5) Clean up (remove containers and volumes)"
read -p "Enter choice [1-5]: " choice

case $choice in
    1)
        echo ""
        echo "🚀 Starting all services..."
        echo "This may take a few minutes on first run (building images)..."
        echo ""
        docker compose up -d --build
        echo ""
        echo "✅ Services started successfully!"
        echo ""
        echo "📊 Service Status:"
        docker compose ps
        echo ""
        echo "🌐 Access the application:"
        echo "   Frontend:  http://localhost"
        echo "   Backend:   http://localhost:8080/api"
        echo "   Health:    http://localhost:8080/actuator/health"
        echo "   MySQL:     localhost:3306 (user: root, password: root)"
        echo ""
        echo "📝 View logs with: docker compose logs -f"
        echo "🛑 Stop services with: docker compose down"
        ;;
    2)
        echo ""
        echo "🛑 Stopping all services..."
        docker compose down
        echo ""
        echo "✅ All services stopped"
        ;;
    3)
        echo ""
        echo "♻️  Restarting all services..."
        docker compose restart
        echo ""
        echo "✅ All services restarted"
        ;;
    4)
        echo ""
        echo "📝 Showing logs (Ctrl+C to exit)..."
        echo ""
        docker compose logs -f
        ;;
    5)
        echo ""
        read -p "⚠️  This will remove all containers and data. Continue? (y/N): " confirm
        if [[ $confirm == [yY] ]]; then
            echo ""
            echo "🧹 Cleaning up..."
            docker compose down -v
            echo ""
            echo "✅ Cleanup complete"
        else
            echo "Cancelled"
        fi
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac
