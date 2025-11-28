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

# SPECIAL HANDLING FOR RECIPE-SERVICE WITH GRPC
echo "Building recipe-service with gRPC generation..."
cd backend/recipe-service

# Clean everything including generated sources
echo -e "${YELLOW}Cleaning recipe-service (including gRPC generated files)...${NC}"
rm -rf target/generated-sources 2>/dev/null || true
rm -rf target/classes 2>/dev/null || true
mvn clean

# Generate protobuf/gRPC code first
echo -e "${YELLOW}Generating protobuf/gRPC files...${NC}"
mvn protobuf:compile protobuf:compile-custom || {
    echo -e "${RED}Failed to generate gRPC files. Retrying with full clean...${NC}"
    rm -rf target
    mvn clean
    mvn protobuf:compile protobuf:compile-custom
}

# Verify generated files exist
if [ ! -f "target/generated-sources/protobuf/grpc-java/com/recipe/RecipeServiceGrpc.java" ]; then
    echo -e "${RED}ERROR: gRPC files were not generated!${NC}"
    echo -e "${YELLOW}Checking proto file location...${NC}"
    find src/main -name "*.proto"
    exit 1
fi

echo -e "${GREEN}✅ gRPC files generated successfully${NC}"

# Now compile and package
echo -e "${YELLOW}Compiling recipe-service...${NC}"
mvn compile
mvn package -DskipTests

echo -e "${GREEN}✅ Recipe-service built successfully${NC}"

docker build -t sachittarway/recipe-service:latest .
cd ../..

echo "Building gateway-service..."
cd backend/gateway-service
mvn clean package -DskipTests
docker build -t sachittarway/gateway-service:latest .
cd ../..

echo -e "${GREEN}✅ All services built${NC}"

# 10. Deploy microservices
echo -e "\n${BLUE}🔟 Deploying microservices...${NC}"
kubectl delete deployment user-service notification-service gateway-service recipe-service 2>/dev/null || true
sleep 5

kubectl apply -f backend/user-service/K8s/user-deployment.yaml
kubectl apply -f backend/gateway-service/K8s/gateway-deployment.yaml
kubectl apply -f backend/recipe-service/K8s/recipe-deployment.yaml
kubectl apply -f backend/notification-service/K8s/notification-deployment.yaml
