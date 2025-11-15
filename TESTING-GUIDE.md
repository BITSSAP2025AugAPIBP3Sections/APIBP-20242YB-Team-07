# Testing Guide for Cooknect Kubernetes Deployment

This guide provides step-by-step instructions for testing your containerized Cooknect application deployment.

## 🧪 Testing Overview

We'll test the deployment in these phases:

1. **Prerequisites Check** - Verify all tools are installed
2. **Docker Testing** - Test container builds locally
3. **Minikube Setup** - Prepare Kubernetes cluster
4. **Deployment Testing** - Deploy and verify services
5. **Functional Testing** - Test application functionality
6. **Load Testing** - Verify scalability

## 📋 Phase 1: Prerequisites Check

### Check Required Tools

```bash
# Navigate to project directory
cd /Users/I528935/Desktop/APIBP-20242YB-Team-07

# Check Docker
docker --version
docker info

# Check Minikube
minikube version

# Check kubectl
kubectl version --client

# Check if Minikube is running
minikube status
```

### Expected Output

```
Docker version 24.0.0+
Minikube version: v1.32.0+
kubectl version should show client version
```

## 🐳 Phase 2: Docker Testing

### Test 1: Build Demo Container

```bash
# Test build process with demo script
./demo-build.sh
```

**Expected Results:**

- ✅ Script runs without errors
- ✅ Docker image `cooknect/recipe-service:demo` is created
- ✅ Image size should be reasonable (< 500MB)

### Test 2: Manual Container Build

```bash
# Test building another service manually
cd backend/user-service
docker build -t test/user-service .

# Check if build succeeded
docker images | grep test/user-service
```

### Test 3: Container Runtime Test

```bash
# Run a container to test if it starts properly
docker run -d --name test-recipe \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=test \
  cooknect/recipe-service:demo

# Wait a moment and check if it's running
sleep 30
docker ps | grep test-recipe

# Check logs
docker logs test-recipe

# Cleanup
docker stop test-recipe
docker rm test-recipe
```

## ⚙️ Phase 3: Minikube Setup

### Test 1: Start Minikube

```bash
# Stop any existing Minikube
minikube stop

# Start fresh Minikube with adequate resources
minikube start --memory=8192 --cpus=4 --driver=docker

# Verify startup
minikube status
```

### Test 2: Enable Required Addons

```bash
# Enable necessary addons
minikube addons enable ingress
minikube addons enable metrics-server

# List enabled addons
minikube addons list
```

### Test 3: Verify Kubernetes Cluster

```bash
# Check cluster info
kubectl cluster-info

# Check nodes
kubectl get nodes

# Check system pods
kubectl get pods -n kube-system
```

## 🚀 Phase 4: Deployment Testing

### Test 1: Deploy Infrastructure

```bash
cd k8s

# Deploy namespace and storage
kubectl apply -f namespace-and-storage.yaml

# Verify namespace creation
kubectl get namespaces | grep cooknect

# Check PVC status
kubectl get pvc -n cooknect
```

### Test 2: Deploy Kafka

```bash
# Deploy Kafka
kubectl apply -f kafka.yaml

# Monitor Kafka deployment
kubectl get pods -n cooknect -w
# Press Ctrl+C when kafka pod shows "Running"

# Check Kafka service
kubectl get services -n cooknect
```

### Test 3: Full Deployment

```bash
# Deploy everything using the script
./deploy.sh

# Monitor all pods coming online (this may take 5-10 minutes)
kubectl get pods -n cooknect -w
```

### Test 4: Verify Deployment Status

```bash
# Check all resources
kubectl get all -n cooknect

# Check pod details
kubectl describe pods -n cooknect

# Check for any failed pods
kubectl get pods -n cooknect | grep -v Running
```

## 🔍 Phase 5: Service Connectivity Testing

### Test 1: Internal Service Communication

```bash
# Test recipe service health endpoint
kubectl exec -n cooknect deployment/recipe-service -- \
  curl -s http://localhost:8080/actuator/health

# Test service-to-service communication
kubectl exec -n cooknect deployment/user-service -- \
  curl -s http://recipe-service:8080/actuator/health
```

### Test 2: External Access Testing

```bash
# Get Minikube IP
MINIKUBE_IP=$(minikube ip)
echo "Minikube IP: $MINIKUBE_IP"

# Access frontend service
minikube service frontend-service -n cooknect --url

# Access gateway service
minikube service gateway-service -n cooknect --url

# Access Kafka UI
minikube service kafka-ui-service -n cooknect --url
```

### Test 3: Load Balancer Testing

```bash
# Test multiple requests to see load balancing
for i in {1..5}; do
  kubectl exec -n cooknect deployment/user-service -- \
    curl -s http://recipe-service:8080/actuator/info | grep hostname
  sleep 1
done
```

## 📊 Phase 6: Application Functionality Testing

### Test 1: API Gateway Testing

```bash
# Get gateway URL
GATEWAY_URL=$(minikube service gateway-service -n cooknect --url)

# Test gateway health
curl $GATEWAY_URL/actuator/health

# Test routing (if endpoints are configured)
curl $GATEWAY_URL/api/recipes/health
curl $GATEWAY_URL/api/users/health
```

### Test 2: Frontend Testing

```bash
# Get frontend URL
FRONTEND_URL=$(minikube service frontend-service -n cooknect --url)

# Test frontend health endpoint
curl $FRONTEND_URL/health

# Open in browser for manual testing
open $FRONTEND_URL
```

### Test 3: Kafka Testing

```bash
# Access Kafka UI
KAFKA_UI_URL=$(minikube service kafka-ui-service -n cooknect --url)
echo "Open Kafka UI at: $KAFKA_UI_URL"

# Test Kafka connectivity from application
kubectl exec -n cooknect deployment/recipe-service -- \
  curl -s http://kafka-service:9092
```

## 🔧 Phase 7: Scaling and Performance Testing

### Test 1: Manual Scaling

```bash
# Scale recipe service to 3 replicas
kubectl scale deployment recipe-service --replicas=3 -n cooknect

# Watch scaling happen
kubectl get pods -n cooknect -w | grep recipe-service

# Verify all replicas are running
kubectl get deployment recipe-service -n cooknect
```

### Test 2: Rolling Update Test

```bash
# Trigger a rolling update by changing an environment variable
kubectl patch deployment recipe-service -n cooknect \
  -p '{"spec":{"template":{"spec":{"containers":[{"name":"recipe-service","env":[{"name":"TEST_VAR","value":"updated"}]}]}}}}'

# Watch the rolling update
kubectl rollout status deployment/recipe-service -n cooknect
```

### Test 3: Load Testing

```bash
# Simple load test using kubectl exec
for i in {1..20}; do
  kubectl exec -n cooknect deployment/user-service -- \
    curl -s http://recipe-service:8080/actuator/health &
done
wait

echo "Load test completed"
```

## 🚨 Troubleshooting Tests

### Test 1: Pod Failure Simulation

```bash
# Delete a pod to test recovery
kubectl delete pod -n cooknect -l app=recipe-service --timeout=0s --grace-period=0

# Watch new pod come up
kubectl get pods -n cooknect -w | grep recipe-service
```

### Test 2: Resource Monitoring

```bash
# Check resource usage
kubectl top nodes
kubectl top pods -n cooknect

# Check pod logs for errors
kubectl logs -n cooknect deployment/recipe-service --tail=50
```

### Test 3: Network Policy Testing

```bash
# Test DNS resolution
kubectl exec -n cooknect deployment/recipe-service -- \
  nslookup kafka-service.cooknect.svc.cluster.local

# Test port connectivity
kubectl exec -n cooknect deployment/recipe-service -- \
  nc -zv kafka-service 9092
```

## 📝 Test Results Checklist

### ✅ Docker Tests

- [ ] Demo build script runs successfully
- [ ] Manual container builds work
- [ ] Containers start and respond to health checks

### ✅ Kubernetes Tests

- [ ] Minikube starts with adequate resources
- [ ] All pods reach "Running" status
- [ ] Services are accessible internally and externally
- [ ] Load balancing works across replicas

### ✅ Application Tests

- [ ] Health endpoints respond correctly
- [ ] Service-to-service communication works
- [ ] Frontend loads in browser
- [ ] Kafka UI is accessible

### ✅ Performance Tests

- [ ] Scaling up/down works correctly
- [ ] Rolling updates complete successfully
- [ ] System handles basic load testing

## 🎯 Quick Professor Demo

For a quick demonstration to your professor:

```bash
# 1. Show the deployment
cd /Users/I528935/Desktop/APIBP-20242YB-Team-07
kubectl get all -n cooknect

# 2. Show services are accessible
minikube service list -n cooknect

# 3. Test a service
kubectl exec -n cooknect deployment/recipe-service -- \
  curl -s http://localhost:8080/actuator/health

# 4. Show scaling
kubectl scale deployment recipe-service --replicas=3 -n cooknect
kubectl get pods -n cooknect | grep recipe-service

# 5. Access the frontend
minikube service frontend-service -n cooknect
```

## 📞 Getting Help

If tests fail, check:

1. **Logs**: `kubectl logs -n cooknect deployment/<service-name>`
2. **Events**: `kubectl get events -n cooknect`
3. **Describe**: `kubectl describe pod <pod-name> -n cooknect`
4. **Resources**: `kubectl top pods -n cooknect`

## 📊 Expected Test Timeline

- **Prerequisites Check**: 5 minutes
- **Docker Testing**: 10 minutes
- **Minikube Setup**: 5 minutes
- **Deployment Testing**: 15 minutes
- **Service Testing**: 10 minutes
- **Performance Testing**: 10 minutes

**Total testing time: ~55 minutes**
