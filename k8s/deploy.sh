#!/bin/bash

# Deploy script for Cooknect application on Minikube
set -e

echo "🚀 Starting deployment of Cooknect application to Minikube..."

# Check if minikube is running
if ! minikube status > /dev/null 2>&1; then
    echo "❌ Minikube is not running. Please start minikube first:"
    echo "   minikube start"
    exit 1
fi

# Apply namespace and storage
echo "📦 Creating namespace and storage..."
kubectl apply -f namespace-and-storage.yaml

# Wait for PVC to be bound
echo "⏳ Waiting for PVC to be bound..."
kubectl wait --for=condition=Bound pvc/kafka-pvc -n cooknect --timeout=60s

# Deploy Kafka
echo "📡 Deploying Kafka..."
kubectl apply -f kafka.yaml

# Wait for Kafka to be ready
echo "⏳ Waiting for Kafka to be ready..."
kubectl wait --for=condition=Available deployment/kafka -n cooknect --timeout=180s

# Deploy Kafka UI
echo "🖥️ Deploying Kafka UI..."
kubectl apply -f kafka-ui.yaml

# Deploy backend services
echo "🔧 Deploying backend services..."
kubectl apply -f recipe-service.yaml
kubectl apply -f user-service.yaml
kubectl apply -f gateway-service.yaml
kubectl apply -f other-services.yaml

# Deploy frontend
echo "🌐 Deploying frontend..."
kubectl apply -f frontend.yaml

# Wait for all deployments to be ready
echo "⏳ Waiting for all services to be ready..."
kubectl wait --for=condition=Available deployment --all -n cooknect --timeout=300s

# Show service URLs
echo ""
echo "✅ Deployment completed successfully!"
echo ""
echo "📋 Service Information:"
echo "========================"

# Get Minikube IP
MINIKUBE_IP=$(minikube ip)

# Get service ports
GATEWAY_PORT=$(kubectl get service gateway-service -n cooknect -o jsonpath='{.spec.ports[0].nodePort}' 2>/dev/null || echo "8080")
FRONTEND_PORT=$(kubectl get service frontend-service -n cooknect -o jsonpath='{.spec.ports[0].nodePort}' 2>/dev/null || echo "80")
KAFKA_UI_PORT=$(kubectl get service kafka-ui-service -n cooknect -o jsonpath='{.spec.ports[0].nodePort}' 2>/dev/null || echo "8080")

echo "🌐 Frontend: http://$MINIKUBE_IP:$FRONTEND_PORT"
echo "🔧 Gateway API: http://$MINIKUBE_IP:$GATEWAY_PORT"
echo "📡 Kafka UI: http://$MINIKUBE_IP:$KAFKA_UI_PORT"
echo ""

# Show pod status
echo "📊 Pod Status:"
kubectl get pods -n cooknect -o wide

echo ""
echo "🔍 To monitor your deployment:"
echo "   kubectl get pods -n cooknect -w"
echo ""
echo "🏠 To access services using minikube service:"
echo "   minikube service frontend-service -n cooknect"
echo "   minikube service gateway-service -n cooknect"
echo "   minikube service kafka-ui-service -n cooknect"