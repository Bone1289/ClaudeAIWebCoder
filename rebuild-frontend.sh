#!/bin/bash

# Script to rebuild the frontend with clean cache

echo "Stopping containers..."
docker-compose down

echo "Removing frontend build cache..."
cd frontend
rm -rf node_modules/.cache .angular dist

echo "Rebuilding Docker containers..."
cd ..
docker-compose build --no-cache frontend

echo "Starting containers..."
docker-compose up -d

echo "Done! The frontend should rebuild cleanly now."
echo "Access the application at http://localhost:4200"
