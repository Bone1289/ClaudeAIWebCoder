#!/bin/bash

# Virtual Bank - Frontend Startup Script
# This script starts the Angular frontend

echo "========================================"
echo "Starting Virtual Bank Frontend..."
echo "========================================"
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Error: Node.js is not installed or not in PATH"
    echo "Please install Node.js 18+ and try again"
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ Error: npm is not installed or not in PATH"
    echo "Please install npm and try again"
    exit 1
fi

# Display versions
echo "Node version:"
node --version
echo ""
echo "npm version:"
npm --version
echo ""

# Navigate to frontend directory
cd frontend || exit 1

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
    echo ""
fi

echo "========================================"
echo "Starting Angular Development Server..."
echo "========================================"
echo ""
echo "The app will open at: http://localhost:4200"
echo "Press Ctrl+C to stop"
echo ""

# Start the Angular dev server
npm start
