#!/bin/bash

# Virtual Bank - Docker Startup Fix Script
# This script fixes common issues with MySQL and Elasticsearch containers

echo "=========================================="
echo "🔧 Fixing Docker Startup Issues"
echo "=========================================="
echo ""

# Function to check if running on Linux
is_linux() {
    [[ "$OSTYPE" == "linux-gnu"* ]]
}

# Function to check if running on macOS
is_macos() {
    [[ "$OSTYPE" == "darwin"* ]]
}

# Function to check if running on Windows/WSL
is_wsl() {
    grep -qi microsoft /proc/version 2>/dev/null
}

# 1. Fix Elasticsearch vm.max_map_count (Linux/WSL only)
echo "Step 1: Checking Elasticsearch requirements..."
if is_linux || is_wsl; then
    current_map_count=$(sysctl vm.max_map_count 2>/dev/null | awk '{print $3}')
    required_map_count=262144

    if [ -z "$current_map_count" ] || [ "$current_map_count" -lt "$required_map_count" ]; then
        echo "⚠️  vm.max_map_count is too low: $current_map_count (required: $required_map_count)"
        echo "   Attempting to fix..."

        # Try to set it temporarily
        if sudo sysctl -w vm.max_map_count=$required_map_count 2>/dev/null; then
            echo "✅ Temporarily set vm.max_map_count to $required_map_count"
            echo "   To make it permanent, run:"
            echo "   echo 'vm.max_map_count=262144' | sudo tee -a /etc/sysctl.conf"
        else
            echo "❌ Failed to set vm.max_map_count (requires sudo)"
            echo "   Please run manually:"
            echo "   sudo sysctl -w vm.max_map_count=262144"
            echo "   To make it permanent:"
            echo "   echo 'vm.max_map_count=262144' | sudo tee -a /etc/sysctl.conf"
            echo ""
            read -p "Press Enter to continue anyway, or Ctrl+C to exit..."
        fi
    else
        echo "✅ vm.max_map_count is correctly set: $current_map_count"
    fi
else
    echo "ℹ️  Skipping vm.max_map_count check (not on Linux/WSL)"
fi
echo ""

# 2. Stop all running containers
echo "Step 2: Stopping all running containers..."
docker compose down 2>/dev/null
echo "✅ Containers stopped"
echo ""

# 3. Remove potentially corrupted volumes
echo "Step 3: Cleaning up potentially corrupted volumes..."
echo "⚠️  This will delete all data in MySQL and Elasticsearch volumes!"
read -p "Do you want to remove volumes? (y/N): " remove_volumes
if [[ "$remove_volumes" =~ ^[Yy]$ ]]; then
    docker compose down -v
    echo "✅ Volumes removed"
else
    echo "ℹ️  Keeping existing volumes"
fi
echo ""

# 4. Check for port conflicts
echo "Step 4: Checking for port conflicts..."
ports_to_check=(3306 8080 80 4201 9090 3000 9200 9300 5000 9600 5601 1025 8025)
port_conflicts=false

for port in "${ports_to_check[@]}"; do
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 || netstat -tuln 2>/dev/null | grep -q ":$port "; then
        echo "⚠️  Port $port is already in use"
        port_conflicts=true
    fi
done

if [ "$port_conflicts" = true ]; then
    echo ""
    echo "❌ Some ports are already in use. Please stop the conflicting services."
    echo "   You can find processes using ports with: sudo lsof -i :<port>"
    echo ""
    read -p "Continue anyway? (y/N): " continue_anyway
    if [[ ! "$continue_anyway" =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo "✅ No port conflicts detected"
fi
echo ""

# 5. Pull latest images
echo "Step 5: Pulling latest images..."
docker compose pull mysql elasticsearch
echo "✅ Images pulled"
echo ""

# 6. Start services in stages
echo "Step 6: Starting services in stages..."
echo ""

echo "Starting MySQL and Elasticsearch first..."
docker compose up -d mysql elasticsearch
echo ""
echo "Waiting 30 seconds for MySQL and Elasticsearch to initialize..."
sleep 30
echo ""

# Check if MySQL and Elasticsearch are healthy
echo "Checking service health..."
mysql_status=$(docker compose ps mysql --format json 2>/dev/null | grep -o '"State":"[^"]*"' | cut -d'"' -f4)
es_status=$(docker compose ps elasticsearch --format json 2>/dev/null | grep -o '"State":"[^"]*"' | cut -d'"' -f4)

echo "MySQL status: $mysql_status"
echo "Elasticsearch status: $es_status"
echo ""

if [ "$mysql_status" != "running" ]; then
    echo "❌ MySQL failed to start. Checking logs..."
    echo "--- MySQL Logs ---"
    docker compose logs --tail=50 mysql
    echo "--- End MySQL Logs ---"
    echo ""
fi

if [ "$es_status" != "running" ]; then
    echo "❌ Elasticsearch failed to start. Checking logs..."
    echo "--- Elasticsearch Logs ---"
    docker compose logs --tail=50 elasticsearch
    echo "--- End Elasticsearch Logs ---"
    echo ""
fi

if [ "$mysql_status" = "running" ] && [ "$es_status" = "running" ]; then
    echo "✅ MySQL and Elasticsearch started successfully!"
    echo ""
    echo "Starting remaining services..."
    docker compose up -d
    echo ""
    echo "Waiting 20 seconds for services to initialize..."
    sleep 20
    echo ""
    echo "=========================================="
    echo "✅ All services started!"
    echo "=========================================="
    echo ""
    echo "📊 Service Status:"
    docker compose ps
    echo ""
    echo "🌐 Access the application:"
    echo "   Frontend:       http://localhost"
    echo "   Admin Portal:   http://localhost:4201"
    echo "   Backend API:    http://localhost:8080/api"
    echo "   Health Check:   http://localhost:8080/actuator/health"
    echo "   Prometheus:     http://localhost:9090"
    echo "   Grafana:        http://localhost:3000 (admin/admin)"
    echo "   Kibana:         http://localhost:5601"
    echo "   MailHog:        http://localhost:8025"
    echo ""
    echo "📝 View logs: docker compose logs -f"
    echo "🛑 Stop all:  docker compose down"
else
    echo "❌ Failed to start core services. Please check the logs above."
    echo ""
    echo "Common solutions:"
    echo "1. For Elasticsearch on Linux/WSL:"
    echo "   sudo sysctl -w vm.max_map_count=262144"
    echo ""
    echo "2. Check Docker has enough memory allocated (recommended: 4GB+)"
    echo ""
    echo "3. View detailed logs:"
    echo "   docker compose logs mysql"
    echo "   docker compose logs elasticsearch"
    echo ""
    echo "4. Try removing all containers and volumes:"
    echo "   docker compose down -v"
    echo "   Then run this script again"
    exit 1
fi
