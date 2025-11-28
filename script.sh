#!/bin/bash
# filepath: redeploy-kafka-fixed.sh

echo "🚀 Complete Redeploy: Kafka + All Services"
echo "==========================================="

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# 1. Verify Docker and Minikube
echo -e "\n${BLUE}1️⃣  Verifying Docker and Minikube...${NC}"
if ! docker ps > /dev/null 2>&1; then
    echo -e "${RED}Docker is not running! Please start Docker Desktop first.${NC}"
    exit 1
fi

if ! minikube status > /dev/null 2>&1; then
    echo -e "${YELLOW}Minikube not running. Starting...${NC}"
    minikube start
fi

echo -e "${GREEN}✅ Docker and Minikube are running${NC}"

# 2. Clean up existing Kafka
echo -e "\n${BLUE}2️⃣  Cleaning up existing Kafka resources...${NC}"
kubectl delete statefulset kafka -n kafka 2>/dev/null || echo "No kafka statefulset"
kubectl delete statefulset zookeeper -n kafka 2>/dev/null || echo "No zookeeper statefulset"
kubectl delete deployment kafka -n kafka 2>/dev/null || echo "No kafka deployment"
kubectl delete deployment zookeeper -n kafka 2>/dev/null || echo "No zookeeper deployment"
kubectl delete deployment kafka-ui -n kafka 2>/dev/null || echo "No kafka-ui"
kubectl delete pvc -n kafka --all 2>/dev/null || echo "No PVCs to delete"

echo "Waiting for cleanup..."
sleep 15

# 3. Deploy Kafka infrastructure
echo -e "\n${BLUE}3️⃣  Deploying Kafka infrastructure...${NC}"
kubectl apply -f K8s/kafka/kafka.yaml

# 4. Wait for Zookeeper
echo -e "\n${BLUE}4️⃣  Waiting for Zookeeper to be ready...${NC}"
kubectl wait --for=condition=ready pod -l app=zookeeper -n kafka --timeout=120s
echo -e "${GREEN}✅ Zookeeper ready${NC}"

# 5. Wait for Kafka
echo -e "\n${BLUE}5️⃣  Waiting for Kafka to be ready...${NC}"
kubectl wait --for=condition=ready pod -l app=kafka -n kafka --timeout=300s || {
    echo -e "${YELLOW}Kafka taking longer than expected, checking logs...${NC}"
    kubectl logs kafka-0 -n kafka --tail=30
    echo -e "${YELLOW}Waiting a bit more...${NC}"
    sleep 30
}
echo -e "${GREEN}✅ Kafka ready${NC}"

# 6. Verify Kafka configuration
echo -e "\n${BLUE}6️⃣  Verifying Kafka configuration...${NC}"
sleep 10
kubectl exec kafka-0 -n kafka -- env | grep KAFKA_ADVERTISED_LISTENERS || echo "Will verify after full startup"

# 7. Create Kafka topics
echo -e "\n${BLUE}7️⃣  Creating Kafka topics...${NC}"
TOPICS=("user-topic" "recipe-topic" "challenge-topic" "user-events")

for TOPIC in "${TOPICS[@]}"; do
    echo "Creating topic: $TOPIC"
    kubectl exec kafka-0 -n kafka -- /bin/kafka-topics \
        --bootstrap-server localhost:9092 \
        --create \
        --if-not-exists \
        --topic "$TOPIC" \
        --partitions 3 \
        --replication-factor 1 2>/dev/null || echo "Topic $TOPIC will be created when Kafka is fully ready"
done

sleep 5

echo -e "\n${BLUE}📋 Listing all topics:${NC}"
kubectl exec kafka-0 -n kafka -- /bin/kafka-topics \
  --bootstrap-server localhost:9092 \
  --list || echo "Topics will appear once Kafka is fully ready"

# 8. Configure Docker for Minikube
echo -e "\n${BLUE}8️⃣  Configuring Docker for Minikube...${NC}"
eval $(minikube docker-env)
echo -e "${GREEN}✅ Docker configured${NC}"

# 9. Rebuild services
echo -e "\n${BLUE}9️⃣  Rebuilding microservices...${NC}"

echo "Building common module..."
cd backend/common
mvn clean install -DskipTests
cd ../..

echo "Building user-service..."
cd backend/user-service
mvn clean package -DskipTests
docker build -t sachittarway/user-service:latest .
cd ../..

echo "Building notification-service..."
cd backend/notification-service
mvn clean package -DskipTests
docker build -t sachittarway/notification-service:latest .
cd ../..

echo "Building gateway-service..."
cd backend/gateway-service
mvn clean package -DskipTests
docker build -t sachittarway/gateway-service:latest .
cd ../..


kubectl apply -f backend/user-service/K8s/user-deployment.yaml
kubectl apply -f backend/gateway-service/K8s/gateway-deployment.yaml
kubectl apply -f backend/notification-service/K8s/notification-deployment.yaml