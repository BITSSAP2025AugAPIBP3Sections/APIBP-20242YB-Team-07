#!/bin/bash

# Quick Test Script for Cooknect Deployment
# This script runs essential tests to verify the deployment works

set -e

echo "🧪 Cooknect Deployment Test Suite"
echo "================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Test function
run_test() {
    local test_name="$1"
    local test_command="$2"
    
    echo -n "🔍 Testing: $test_name... "
    
    if eval "$test_command" > /tmp/test_output 2>&1; then
        echo -e "${GREEN}✅ PASS${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}"
        echo "   Error details:"
        cat /tmp/test_output | head -3 | sed 's/^/   /'
        ((TESTS_FAILED++))
    fi
}

# 1. Prerequisites Tests
echo "📋 Phase 1: Prerequisites"
echo "------------------------"

run_test "Docker is running" "docker info"
run_test "kubectl is available" "kubectl version --client"
run_test "Minikube is available" "minikube version"

# 2. Minikube Tests
echo ""
echo "⚙️ Phase 2: Minikube"
echo "-------------------"

# Check if Minikube is running, start if not
if ! minikube status > /dev/null 2>&1; then
    echo "🚀 Starting Minikube..."
    minikube start --memory=8192 --cpus=4 --driver=docker
fi

run_test "Minikube is running" "minikube status"
run_test "Kubernetes cluster is accessible" "kubectl cluster-info"

# 3. Build Tests
echo ""
echo "🐳 Phase 3: Container Build"
echo "---------------------------"

run_test "Demo build script exists" "test -f ./demo-build.sh"
run_test "Build recipe service container" "cd backend/recipe-service && docker build -t test-recipe-service:test . > /dev/null"

# 4. Deployment Tests
echo ""
echo "🚀 Phase 4: Kubernetes Deployment"
echo "---------------------------------"

# Check if already deployed
if kubectl get namespace cooknect > /dev/null 2>&1; then
    echo "📦 Namespace 'cooknect' already exists"
else
    echo "📦 Deploying to Kubernetes..."
    cd k8s
    ./deploy.sh > /dev/null 2>&1 &
    DEPLOY_PID=$!
    
    echo "⏳ Waiting for deployment to complete (this may take 5-10 minutes)..."
    wait $DEPLOY_PID
    cd ..
fi

run_test "Namespace exists" "kubectl get namespace cooknect"
run_test "Kafka pod is running" "kubectl get pods -n cooknect | grep kafka | grep Running"
run_test "Recipe service is running" "kubectl get pods -n cooknect | grep recipe-service | grep Running"
run_test "Frontend is running" "kubectl get pods -n cooknect | grep frontend | grep Running"

# 5. Service Tests
echo ""
echo "🔍 Phase 5: Service Connectivity"
echo "--------------------------------"

# Wait a bit for services to be fully ready
echo "⏳ Waiting for services to be ready..."
sleep 30

run_test "Recipe service health check" "kubectl exec -n cooknect deployment/recipe-service -- curl -f -s http://localhost:8080/actuator/health"
run_test "Service discovery works" "kubectl exec -n cooknect deployment/user-service -- curl -f -s http://recipe-service:8080/actuator/health"
run_test "Frontend health check" "kubectl exec -n cooknect deployment/frontend -- wget -q --spider http://localhost/health"

# 6. External Access Tests
echo ""
echo "🌐 Phase 6: External Access"
echo "---------------------------"

MINIKUBE_IP=$(minikube ip 2>/dev/null || echo "unavailable")
run_test "Can get Minikube IP" "test '$MINIKUBE_IP' != 'unavailable'"

# Get service URLs
FRONTEND_URL=$(minikube service frontend-service -n cooknect --url 2>/dev/null || echo "")
GATEWAY_URL=$(minikube service gateway-service -n cooknect --url 2>/dev/null || echo "")

run_test "Frontend service is accessible" "test -n '$FRONTEND_URL'"
run_test "Gateway service is accessible" "test -n '$GATEWAY_URL'"

# 7. Scaling Test
echo ""
echo "📈 Phase 7: Scaling"
echo "------------------"

run_test "Scale recipe service to 3 replicas" "kubectl scale deployment recipe-service --replicas=3 -n cooknect"
sleep 10
run_test "All 3 replicas are running" "test \$(kubectl get deployment recipe-service -n cooknect -o jsonpath='{.status.readyReplicas}') -eq 3"

# Test Summary
echo ""
echo "📊 Test Summary"
echo "==============="
echo -e "✅ Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "❌ Tests Failed: ${RED}$TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All tests passed! Your deployment is working correctly.${NC}"
    echo ""
    echo "🔗 Access your services:"
    if [ -n "$FRONTEND_URL" ]; then
        echo "   Frontend: $FRONTEND_URL"
    fi
    if [ -n "$GATEWAY_URL" ]; then
        echo "   API Gateway: $GATEWAY_URL" 
    fi
    echo ""
    echo "📱 Quick access commands:"
    echo "   minikube service frontend-service -n cooknect"
    echo "   minikube service kafka-ui-service -n cooknect"
    echo ""
    echo "📊 Monitor your deployment:"
    echo "   kubectl get pods -n cooknect -w"
    
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Check the error messages above.${NC}"
    echo ""
    echo "🔧 Troubleshooting tips:"
    echo "   kubectl get pods -n cooknect"
    echo "   kubectl describe pods -n cooknect"
    echo "   kubectl logs -n cooknect deployment/recipe-service"
    
    exit 1
fi