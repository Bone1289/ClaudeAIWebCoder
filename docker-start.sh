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
echo "1) Clean up and start fresh (remove containers, volumes, and rebuild) [DEFAULT]"
echo "2) Start all services (MySQL + Backend + Frontend + Admin)"
echo "3) Stop all services"
echo "4) Restart all services"
echo "5) View logs"
echo "6) Rebuild backend only"
echo "7) Rebuild frontend only"
echo "8) Rebuild admin portal only"
echo "9) Rebuild all services"
read -p "Enter choice [1-9] (default: 1): " choice

# Set default to option 1 if no input
choice=${choice:-1}

case $choice in
    1)
        echo ""
        echo "🧹 Cleaning up existing containers and volumes..."
        docker compose down -v
        echo ""
        echo "🚀 Starting fresh with full rebuild..."
        echo "This may take a few minutes (building images)..."
        echo ""
        docker compose up -d --build
        echo ""
        echo "✅ Services started successfully!"
        echo ""
        echo "📊 Service Status:"
        docker compose ps
        echo ""
        echo "🌐 Access the application:"
        echo "   Frontend:      http://localhost"
        echo "   Admin Portal:  http://localhost:4201"
        echo "   Backend:       http://localhost:8080/api"
        echo "   Health:        http://localhost:8080/actuator/health"
        echo "   MySQL:         localhost:3306 (user: root, password: root)"
        echo ""
        echo "📊 Monitoring & Infrastructure:"
        echo "   Prometheus:    http://localhost:9090"
        echo "   Grafana:       http://localhost:3000 (admin/admin)"
        echo "   Kibana:        http://localhost:5601"
        echo "   Kafka UI:      http://localhost:8090"
        echo "   MailHog:       http://localhost:8025"
        echo ""
        echo "👤 Default Credentials:"
        echo "   User:  demo@example.com / password123"
        echo "   Admin: admin / Admin"
        echo ""
        echo "📝 View logs with: docker compose logs -f"
        ;;
    2)
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
        echo "   Frontend:      http://localhost"
        echo "   Admin Portal:  http://localhost:4201"
        echo "   Backend:       http://localhost:8080/api"
        echo "   Health:        http://localhost:8080/actuator/health"
        echo "   MySQL:         localhost:3306 (user: root, password: root)"
        echo ""
        echo "📊 Monitoring & Infrastructure:"
        echo "   Prometheus:    http://localhost:9090"
        echo "   Grafana:       http://localhost:3000 (admin/admin)"
        echo "   Kibana:        http://localhost:5601"
        echo "   Kafka UI:      http://localhost:8090"
        echo "   MailHog:       http://localhost:8025"
        echo ""
        echo "👤 Default Credentials:"
        echo "   User:  demo@example.com / password123"
        echo "   Admin: admin / Admin"
        echo ""
        echo "📝 View logs with: docker compose logs -f"
        echo "🛑 Stop services with: docker compose down"
        ;;
    3)
        echo ""
        echo "🛑 Stopping all services..."
        docker compose down
        echo ""
        echo "✅ All services stopped"
        ;;
    4)
        echo ""
        echo "♻️  Restarting all services..."
        docker compose restart
        echo ""
        echo "✅ All services restarted"
        echo ""
        echo "📊 Service Status:"
        docker compose ps
        ;;
    5)
        echo ""
        echo "📝 Showing logs (Ctrl+C to exit)..."
        echo ""
        docker compose logs -f
        ;;
    6)
        echo ""
        echo "🔨 Rebuilding backend only..."
        echo "Stopping backend service..."
        docker compose stop backend
        echo ""
        echo "Building new backend image..."
        docker compose build backend
        echo ""
        echo "Starting backend service..."
        docker compose up -d backend
        echo ""
        echo "✅ Backend rebuilt and restarted!"
        echo ""
        echo "📊 Backend Status:"
        docker compose ps backend
        echo ""
        echo "📝 View backend logs with: docker compose logs -f backend"
        ;;
    7)
        echo ""
        echo "🔨 Rebuilding frontend only..."
        echo "Stopping frontend service..."
        docker compose stop frontend
        echo ""
        echo "Building new frontend image..."
        docker compose build frontend
        echo ""
        echo "Starting frontend service..."
        docker compose up -d frontend
        echo ""
        echo "✅ Frontend rebuilt and restarted!"
        echo ""
        echo "📊 Frontend Status:"
        docker compose ps frontend
        echo ""
        echo "📝 View frontend logs with: docker compose logs -f frontend"
        ;;
    8)
        echo ""
        echo "🔨 Rebuilding admin portal only..."
        echo "Stopping frontend-admin service..."
        docker compose stop frontend-admin
        echo ""
        echo "Building new admin portal image..."
        docker compose build frontend-admin
        echo ""
        echo "Starting admin portal service..."
        docker compose up -d frontend-admin
        echo ""
        echo "✅ Admin portal rebuilt and restarted!"
        echo ""
        echo "📊 Admin Portal Status:"
        docker compose ps frontend-admin
        echo ""
        echo "🌐 Access at: http://localhost:4201"
        echo "👤 Login with: admin / Admin"
        echo ""
        echo "📝 View admin portal logs with: docker compose logs -f frontend-admin"
        ;;
    9)
        echo ""
        echo "🔨 Rebuilding all services..."
        echo ""
        docker compose up -d --build --force-recreate
        echo ""
        echo "✅ All services rebuilt and restarted!"
        echo ""
        echo "📊 Service Status:"
        docker compose ps
        echo ""
        echo "📝 View logs with: docker compose logs -f"
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac
