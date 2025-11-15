#!/bin/bash

# Push script for Docker images to DockerHub
set -e

# Configuration
DOCKER_REGISTRY="cooknect"  # Change this to your DockerHub username
VERSION="v1.0.0"

echo "📤 Pushing Docker images to registry..."
echo "Registry: $DOCKER_REGISTRY"
echo "Version: $VERSION"
echo ""

# Check if logged in to Docker Hub
if ! docker info | grep -q "Username"; then
    echo "❌ Please login to Docker Hub first:"
    echo "   docker login"
    exit 1
fi

# Function to push a service
push_service() {
    local service_name=$1
    
    echo "📤 Pushing $service_name..."
    docker push "$DOCKER_REGISTRY/$service_name:$VERSION"
    docker push "$DOCKER_REGISTRY/$service_name:latest"
    echo "✅ Pushed $service_name successfully"
}

# Push all services
push_service "recipe-service"
push_service "user-service"
push_service "challenge-service"
push_service "gateway-service"
push_service "mealplanner-service"
push_service "notification-service"
push_service "nutrition-service"
push_service "frontend"

echo ""
echo "✅ All images pushed successfully!"
echo ""
echo "🔗 Your images are now available at:"
echo "   https://hub.docker.com/u/$DOCKER_REGISTRY"