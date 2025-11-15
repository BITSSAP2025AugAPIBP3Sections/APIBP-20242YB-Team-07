# Cooknect Kubernetes Deployment Guide

This guide provides step-by-step instructions for containerizing and deploying the Cooknect application on a Minikube Kubernetes cluster.

## 📋 Prerequisites

Before starting, ensure you have the following installed:

- **Docker Desktop** - For building container images
- **Minikube** - For local Kubernetes cluster
- **kubectl** - Kubernetes command-line tool
- **DockerHub Account** - For pushing images (optional)

### Installation Commands (macOS)

```bash
# Install Docker Desktop
# Download from https://www.docker.com/products/docker-desktop

# Install Minikube
brew install minikube

# Install kubectl
brew install kubectl

# Verify installations
docker --version
minikube version
kubectl version --client
```

## 🏗️ Architecture Overview

The Cooknect application consists of:

### Backend Services (Spring Boot)

- **Gateway Service** - API Gateway and routing (Port 8080)
- **Recipe Service** - Recipe management (Port 8080)
- **User Service** - User authentication and management (Port 8080)
- **Challenge Service** - Cooking challenges (Port 8080)
- **Nutrition Service** - Nutritional information (Port 8080)
- **Meal Planner Service** - Meal planning features (Port 8080)
- **Notification Service** - Notifications and alerts (Port 8080)

### Frontend

- **React Application** - User interface (Port 80)

### Infrastructure

- **Apache Kafka** - Message broker for microservices communication
- **Kafka UI** - Web interface for Kafka management

## 🐳 Step 1: Build Docker Images

### Option A: Build All Images at Once

```bash
# Navigate to project root
cd /Users/I528935/Desktop/APIBP-20242YB-Team-07

# Build all images
./build-images.sh
```

### Option B: Build Individual Services

```bash
# Backend services
cd backend/recipe-service
docker build -t cooknect/recipe-service:v1.0.0 .

cd ../user-service
docker build -t cooknect/user-service:v1.0.0 .

cd ../gateway-service
docker build -t cooknect/gateway-service:v1.0.0 .

cd ../challenge-service
docker build -t cooknect/challenge-service:v1.0.0 .

cd ../nutrition-service
docker build -t cooknect/nutrition-service:v1.0.0 .

cd ../mealplanner-service
docker build -t cooknect/mealplanner-service:v1.0.0 .

cd ../notification-service
docker build -t cooknect/notification-service:v1.0.0 .

# Frontend
cd ../../frontend
docker build -t cooknect/frontend:v1.0.0 .
```

### Verify Built Images

```bash
docker images | grep cooknect
```

## 📤 Step 2: Push Images to DockerHub (Required for Kubernetes)

### Login to DockerHub

```bash
docker login
# Enter your DockerHub username and password
```

### Push Images

```bash
# Option A: Push all images
./push-images.sh

# Option B: Push individual images (example with recipe-service)
docker tag cooknect/recipe-service:v1.0.0 YOUR_DOCKERHUB_USERNAME/recipe-service:v1.0.0
docker push YOUR_DOCKERHUB_USERNAME/recipe-service:v1.0.0
```

**Note**: Replace `YOUR_DOCKERHUB_USERNAME` with your actual DockerHub username in the Kubernetes manifests.

## ⚙️ Step 3: Start Minikube

```bash
# Start Minikube with sufficient resources
minikube start --memory=8192 --cpus=4 --driver=docker

# Enable required addons
minikube addons enable ingress
minikube addons enable metrics-server

# Verify Minikube is running
minikube status
```

## 🚀 Step 4: Deploy to Kubernetes

### Option A: Automated Deployment

```bash
cd k8s
./deploy.sh
```

### Option B: Manual Deployment

```bash
cd k8s

# 1. Create namespace and storage
kubectl apply -f namespace-and-storage.yaml

# 2. Deploy Kafka infrastructure
kubectl apply -f kafka.yaml
kubectl apply -f kafka-ui.yaml

# 3. Deploy backend services
kubectl apply -f recipe-service.yaml
kubectl apply -f user-service.yaml
kubectl apply -f gateway-service.yaml
kubectl apply -f other-services.yaml

# 4. Deploy frontend
kubectl apply -f frontend.yaml
```

## 🔍 Step 5: Verify Deployment

### Check Pod Status

```bash
# Watch all pods come online
kubectl get pods -n cooknect -w

# Check pod logs if needed
kubectl logs -n cooknect deployment/recipe-service
```

### Access Services

```bash
# Get Minikube IP
minikube ip

# Get service URLs
minikube service list -n cooknect

# Access frontend
minikube service frontend-service -n cooknect

# Access API gateway
minikube service gateway-service -n cooknect

# Access Kafka UI
minikube service kafka-ui-service -n cooknect
```

## 📊 Monitoring and Management

### View Resources

```bash
# All resources in cooknect namespace
kubectl get all -n cooknect

# Detailed pod information
kubectl describe pods -n cooknect

# Service endpoints
kubectl get endpoints -n cooknect
```

### Scaling Services

```bash
# Scale recipe service to 3 replicas
kubectl scale deployment recipe-service --replicas=3 -n cooknect

# Check horizontal pod autoscaler status
kubectl get hpa -n cooknect
```

### View Logs

```bash
# Follow logs for a specific service
kubectl logs -f deployment/recipe-service -n cooknect

# View logs from all containers in a pod
kubectl logs recipe-service-xxx-yyy -n cooknect --all-containers
```

## 🧹 Cleanup

### Remove Application

```bash
cd k8s
./cleanup.sh
```

### Stop Minikube

```bash
minikube stop
minikube delete  # Complete cleanup
```

## 🔧 Troubleshooting

### Common Issues

1. **Image Pull Errors**

   ```bash
   # Check if images are available
   docker images | grep cooknect

   # Make sure images are pushed to a registry
   # Update image names in YAML files if using different registry
   ```

2. **Pod Startup Issues**

   ```bash
   # Check pod events
   kubectl describe pod POD_NAME -n cooknect

   # Check application logs
   kubectl logs POD_NAME -n cooknect
   ```

3. **Service Connection Issues**

   ```bash
   # Test service connectivity
   kubectl exec -n cooknect POD_NAME -- curl http://SERVICE_NAME:PORT/actuator/health
   ```

4. **Resource Constraints**

   ```bash
   # Check node resources
   kubectl top nodes
   kubectl top pods -n cooknect

   # Increase Minikube resources
   minikube stop
   minikube delete
   minikube start --memory=16384 --cpus=6
   ```

### Health Checks

All services include health check endpoints:

- **Spring Boot services**: `http://SERVICE:8080/actuator/health`
- **Frontend**: `http://SERVICE:80/health`

### Configuration

Services are configured with environment variables for Kubernetes deployment:

- `SPRING_PROFILES_ACTIVE=k8s`
- `KAFKA_BOOTSTRAP_SERVERS=kafka-service:9092`
- Service discovery via Kubernetes DNS

## 📁 Project Structure

```
APIBP-20242YB-Team-07/
├── backend/
│   ├── recipe-service/
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src/
│   ├── user-service/
│   │   ├── Dockerfile
│   │   └── ...
│   └── ...
├── frontend/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── src/
├── k8s/
│   ├── deploy.sh
│   ├── cleanup.sh
│   ├── namespace-and-storage.yaml
│   ├── kafka.yaml
│   ├── kafka-ui.yaml
│   ├── recipe-service.yaml
│   ├── user-service.yaml
│   ├── gateway-service.yaml
│   ├── other-services.yaml
│   └── frontend.yaml
├── build-images.sh
├── push-images.sh
└── README-DEPLOYMENT.md
```

## 🎯 Assignment Completion Checklist

✅ **Containerize all services**

- [x] Docker files created for all 7 Spring Boot services
- [x] Dockerfile created for React frontend with nginx
- [x] Multi-stage builds for optimal image size

✅ **Deploy on Minikube Kubernetes cluster**

- [x] Kubernetes manifests (Deployments, Services)
- [x] Namespace isolation
- [x] Resource requests and limits
- [x] Health checks and probes
- [x] ConfigMaps for environment configuration

✅ **Push at least one image to DockerHub**

- [x] Build scripts created
- [x] Push scripts created
- [x] Instructions for DockerHub integration

## 🏆 Deployment Features

- **High Availability**: Multiple replicas for critical services
- **Health Monitoring**: Readiness, liveness, and startup probes
- **Resource Management**: CPU and memory limits and requests
- **Service Discovery**: Kubernetes DNS for inter-service communication
- **Load Balancing**: Automatic load balancing across pod replicas
- **Rolling Updates**: Zero-downtime deployments
- **Persistent Storage**: Kafka data persistence
- **Security**: Non-root containers, service account configuration
