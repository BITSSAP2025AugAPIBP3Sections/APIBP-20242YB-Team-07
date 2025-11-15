#!/bin/bash

# Build script for all Docker images
set -e

# Configuration
DOCKER_REGISTRY="cooknect"  # Change this to your DockerHub username
VERSION="v1.0.0"

echo "🐳 Building Docker images for Cooknect application..."
echo "Registry: $DOCKER_REGISTRY"
echo "Version: $VERSION"
echo ""

# Function to build a service
build_service() {
    local service_name=$1
    local service_path=$2
    
    echo "🔨 Building $service_name..."
    cd "$service_path"
    
    # Build the Docker image
    docker build -t "$DOCKER_REGISTRY/$service_name:$VERSION" .
    docker tag "$DOCKER_REGISTRY/$service_name:$VERSION" "$DOCKER_REGISTRY/$service_name:latest"
    
    echo "✅ Built $service_name successfully"
    cd - > /dev/null
}

# Build backend services
echo "🔧 Building backend services..."
build_service "recipe-service" "./backend/recipe-service"
build_service "user-service" "./backend/user-service"
build_service "challenge-service" "./backend/challenge-service"
build_service "gateway-service" "./backend/gateway-service"
build_service "mealplanner-service" "./backend/mealplanner-service"
build_service "notification-service" "./backend/notification-service"
build_service "nutrition-service" "./backend/nutrition-service"

# Build frontend
echo "🌐 Building frontend..."
build_service "frontend" "./frontend"

echo ""
echo "✅ All images built successfully!"
echo ""

# List built images
echo "📋 Built images:"
docker images | grep "$DOCKER_REGISTRY"

echo ""
echo "🚀 To push images to registry, run:"
echo "   docker push $DOCKER_REGISTRY/recipe-service:$VERSION"
echo "   docker push $DOCKER_REGISTRY/user-service:$VERSION"
echo "   # ... and so on for all services"
echo ""
echo "Or push all at once:"
echo "   ./push-images.sh"