#!/bin/bash

# Script to reset Kafka topics and clear old messages

echo "Resetting Kafka topics to clear old messages..."

# Stop the application
echo "Stopping containers..."
docker-compose down

# Remove Kafka data volumes (this will delete all messages)
echo "Removing Kafka data..."
docker volume rm ClaudeAIWebCoder_kafka_data 2>/dev/null || echo "Kafka volume already removed"

# Restart the application
echo "Starting containers..."
docker-compose up -d

echo "Done! Kafka topics have been reset."
echo "Old messages have been cleared."
echo ""
echo "The notification system should now work without deserialization errors."
echo "Access the application at http://localhost:4200"
