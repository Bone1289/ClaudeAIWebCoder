#!/bin/bash

# Virtual Bank - Backend Startup Script
# This script starts the Spring Boot backend with MySQL (local profile)

echo "========================================"
echo "Starting Virtual Bank Backend..."
echo "========================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed or not in PATH"
    echo "Please install Java 21 and try again"
    exit 1
fi

# Display Java version
echo "Java version:"
java -version
echo ""

# Navigate to backend directory
cd backend || exit 1

# Check if we need to build
if [ ! -f "build/libs/demo-0.0.1-SNAPSHOT.jar" ]; then
    echo "Building the application..."
    ./gradlew clean build -x test
    echo ""
fi

# Ask user which profile to use
echo "Select database profile:"
echo "1) dev (H2 in-memory - no MySQL needed)"
echo "2) local (MySQL - requires MySQL running)"
echo "3) prod (Production MySQL)"
read -p "Enter choice [1-3] (default: 1): " choice
choice=${choice:-1}

case $choice in
    1)
        PROFILE="dev"
        ;;
    2)
        PROFILE="local"
        ;;
    3)
        PROFILE="prod"
        ;;
    *)
        echo "Invalid choice, using 'dev' profile"
        PROFILE="dev"
        ;;
esac

echo ""
echo "========================================"
echo "Starting with profile: $PROFILE"
echo "========================================"
echo ""

# Start the application
./gradlew bootRun --args="--spring.profiles.active=$PROFILE"
