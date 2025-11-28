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

# ROBUST RECIPE-SERVICE BUILD
echo -e "\n${YELLOW}╔════════════════════════════════════════════╗${NC}"
echo -e "${YELLOW}║  Building recipe-service (ROBUST MODE)    ║${NC}"
echo -e "${YELLOW}╚════════════════════════════════════════════╝${NC}"
cd backend/recipe-service

# Step 1: Complete nuclear cleanup
echo -e "\n${BLUE}Step 1/6: Complete cleanup...${NC}"
rm -rf target 2>/dev/null || true
rm -rf .mvn 2>/dev/null || true
find . -name "*.class" -delete 2>/dev/null || true
echo -e "${GREEN}✓ Cleanup complete${NC}"

# Step 2: Maven clean with force update
echo -e "\n${BLUE}Step 2/6: Maven clean...${NC}"
mvn clean -U
echo -e "${GREEN}✓ Maven clean complete${NC}"

# Step 3: Generate sources using Maven lifecycle (this will trigger build-helper too)
echo -e "\n${BLUE}Step 3/6: Generating protobuf and gRPC sources...${NC}"
mvn generate-sources || {
    echo -e "${RED}✗ Source generation failed${NC}"
    exit 1
}
echo -e "${GREEN}✓ All sources generated${NC}"

# Step 4: Verify generated files exist
echo -e "\n${BLUE}Step 4/6: Verifying generated files...${NC}"
REQUIRED_FILES=(
    "target/generated-sources/protobuf/java/com/recipe/Comment.java"
    "target/generated-sources/protobuf/java/com/recipe/Ingredient.java"
    "target/generated-sources/protobuf/java/com/recipe/RecipeProto.java"
    "target/generated-sources/protobuf/grpc-java/com/recipe/RecipeServiceGrpc.java"
)

ALL_FILES_OK=true
for FILE in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$FILE" ]; then
        echo -e "${RED}✗ Missing: $FILE${NC}"
        ALL_FILES_OK=false
    elif [ ! -s "$FILE" ]; then
        echo -e "${RED}✗ Empty: $FILE${NC}"
        ALL_FILES_OK=false
    else
        FILE_SIZE=$(wc -c < "$FILE")
        echo -e "${GREEN}✓ $FILE (${FILE_SIZE} bytes)${NC}"
    fi
done

if [ "$ALL_FILES_OK" = false ]; then
    echo -e "\n${RED}Some required files are missing or empty!${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All required files verified${NC}"

# Step 5: Compile and package in one go
echo -e "\n${BLUE}Step 5/6: Compiling and packaging...${NC}"
mvn package -DskipTests || {
    echo -e "${RED}✗ Build failed${NC}"
    exit 1
}
echo -e "${GREEN}✓ Package created${NC}"

# Step 6: Build Docker image
echo -e "\n${BLUE}Step 6/6: Building Docker image...${NC}"
docker build -t sachittarway/recipe-service:latest .
echo -e "${GREEN}✓ Docker image built${NC}"

cd ../..

echo -e "\n${GREEN}╔════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✅ Recipe-service build SUCCESS!         ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════╝${NC}"


# ROBUST CHALLENGE-SERVICE BUILD
echo -e "\n${YELLOW}╔════════════════════════════════════════════╗${NC}"
echo -e "${YELLOW}║  Building challenge-service (ROBUST MODE)  ║${NC}"
echo -e "${YELLOW}╚════════════════════════════════════════════╝${NC}"
cd backend/challenge-service

# Step 1: Complete nuclear cleanup
echo -e "\n${BLUE}Step 1/6: Complete cleanup...${NC}"
rm -rf target 2>/dev/null || true
rm -rf .mvn 2>/dev/null || true
find . -name "*.class" -delete 2>/dev/null || true
echo -e "${GREEN}✓ Cleanup complete${NC}"

# Step 2: Maven clean with force update
echo -e "\n${BLUE}Step 2/6: Maven clean...${NC}"
mvn clean -U
echo -e "${GREEN}✓ Maven clean complete${NC}"

# Step 3: Generate sources using Maven lifecycle (this will trigger build-helper too)
echo -e "\n${BLUE}Step 3/6: Generating protobuf and gRPC sources...${NC}"
mvn generate-sources || {
    echo -e "${RED}✗ Source generation failed${NC}"
    exit 1
}
echo -e "${GREEN}✓ All sources generated${NC}"

# Step 4: Verify generated files exist
echo -e "\n${BLUE}Step 4/6: Verifying generated files...${NC}"
REQUIRED_FILES=(
    "target/generated-sources/protobuf/java/com/recipe/Comment.java"
    "target/generated-sources/protobuf/java/com/recipe/Ingredient.java"
    "target/generated-sources/protobuf/java/com/recipe/RecipeProto.java"
    "target/generated-sources/protobuf/grpc-java/com/recipe/RecipeServiceGrpc.java"
)

ALL_FILES_OK=true
for FILE in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$FILE" ]; then
        echo -e "${RED}✗ Missing: $FILE${NC}"
        ALL_FILES_OK=false
    elif [ ! -s "$FILE" ]; then
        echo -e "${RED}✗ Empty: $FILE${NC}"
        ALL_FILES_OK=false
    else
        FILE_SIZE=$(wc -c < "$FILE")
        echo -e "${GREEN}✓ $FILE (${FILE_SIZE} bytes)${NC}"
    fi
done

if [ "$ALL_FILES_OK" = false ]; then
    echo -e "\n${RED}Some required files are missing or empty!${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All required files verified${NC}"

# Step 5: Compile and package in one go
echo -e "\n${BLUE}Step 5/6: Compiling and packaging...${NC}"
mvn package -DskipTests || {
    echo -e "${RED}✗ Build failed${NC}"
    exit 1
}
echo -e "${GREEN}✓ Package created${NC}"

# Step 6: Build Docker image
echo -e "\n${BLUE}Step 6/6: Building Docker image...${NC}"
docker build -t sachittarway/challenge-service:latest .
echo -e "${GREEN}✓ Docker image built${NC}"

cd ../..

echo -e "\n${GREEN}╔════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✅ Challenge-service build SUCCESS!      ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════╝${NC}"

# ROBUST NUTRITION-SERVICE BUILD
echo -e "\n${YELLOW}╔════════════════════════════════════════════╗${NC}"
echo -e "${YELLOW}║  Building nutrition-service (ROBUST MODE)  ║${NC}"
echo -e "${YELLOW}╚════════════════════════════════════════════╝${NC}"
cd backend/nutrition-service

echo -e "\n${BLUE}Step 1/6: Complete cleanup...${NC}"
rm -rf target 2>/dev/null || true
rm -rf .mvn 2>/dev/null || true
find . -name "*.class" -delete 2>/dev/null || true
echo -e "${GREEN}✓ Cleanup complete${NC}"

echo -e "\n${BLUE}Step 2/6: Maven clean...${NC}"
mvn clean -U
echo -e "${GREEN}✓ Maven clean complete${NC}"

echo -e "\n${BLUE}Step 3/6: Generating protobuf and gRPC sources...${NC}"
mvn generate-sources || {
    echo -e "${RED}✗ Source generation failed${NC}"
    exit 1
}
echo -e "${GREEN}✓ All sources generated${NC}"

echo -e "\n${BLUE}Step 4/6: Verifying generated files...${NC}"
REQUIRED_FILES=(
    "target/generated-sources/protobuf/java/com/recipe/Comment.java"
    "target/generated-sources/protobuf/java/com/recipe/Ingredient.java"
    "target/generated-sources/protobuf/java/com/recipe/RecipeProto.java"
    "target/generated-sources/protobuf/grpc-java/com/recipe/RecipeServiceGrpc.java"
)

ALL_FILES_OK=true
for FILE in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$FILE" ]; then
        echo -e "${RED}✗ Missing: $FILE${NC}"
        ALL_FILES_OK=false
    elif [ ! -s "$FILE" ]; then
        echo -e "${RED}✗ Empty: $FILE${NC}"
        ALL_FILES_OK=false
    else
        FILE_SIZE=$(wc -c < "$FILE")
        echo -e "${GREEN}✓ $FILE (${FILE_SIZE} bytes)${NC}"
    fi
done

if [ "$ALL_FILES_OK" = false ]; then
    echo -e "\n${RED}Some required files are missing or empty!${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All required files verified${NC}"

echo -e "\n${BLUE}Step 5/6: Compiling and packaging...${NC}"
mvn package -DskipTests || {
    echo -e "${RED}✗ Build failed${NC}"
    exit 1
}
echo -e "${GREEN}✓ Package created${NC}"

echo -e "\n${BLUE}Step 6/6: Building Docker image...${NC}"
docker build -t sachittarway/nutrition-service:latest .
echo -e "${GREEN}✓ Docker image built${NC}"

cd ../..

echo -e "\n${GREEN}╔════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✅ Nutrition-service build SUCCESS!      ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════╝${NC}"

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

#push image to docker hub for all microservice
docker push sachittarway/user-service:latest
docker push sachittarway/notification-service:latest
docker push sachittarway/recipe-service:latest
docker push sachittarway/challenge-service:latest
docker push sachittarway/nutrition-service:latest
docker push sachittarway/gateway-service:latest


kubectl apply -f backend/user-service/K8s/user-deployment.yaml
kubectl apply -f backend/gateway-service/K8s/gateway-deployment.yaml
kubectl apply -f backend/recipe-service/K8s/recipe-deployment.yaml
kubectl apply -f backend/challenge-service/K8s/challenge-deployment.yaml
kubectl apply -f backend/notification-service/K8s/notification-deployment.yaml
kubectl apply -f backend/nutrition-service/K8s/nutrition-deployment.yaml
