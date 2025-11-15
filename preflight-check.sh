#!/bin/bash

# Pre-flight Check Script
# Run this before attempting deployment

echo "✈️ Pre-flight Checklist for Cooknect Deployment"
echo "==============================================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

CHECKS_PASSED=0
CHECKS_FAILED=0

check_requirement() {
    local name="$1"
    local command="$2"
    local install_hint="$3"
    
    echo -n "🔍 Checking $name... "
    
    if eval "$command" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ OK${NC}"
        ((CHECKS_PASSED++))
    else
        echo -e "${RED}❌ MISSING${NC}"
        echo -e "   ${YELLOW}Install: $install_hint${NC}"
        ((CHECKS_FAILED++))
    fi
}

echo ""
echo "📋 Checking Required Tools"
echo "--------------------------"

check_requirement "Docker" "docker --version" "Download from https://www.docker.com/products/docker-desktop"
check_requirement "Docker Daemon" "docker info" "Start Docker Desktop application"
check_requirement "kubectl" "kubectl version --client" "brew install kubectl"
check_requirement "Minikube" "minikube version" "brew install minikube"

echo ""
echo "💾 Checking System Resources"
echo "----------------------------"

# Check available memory (in GB)
MEMORY_GB=$(echo "$(sysctl -n hw.memsize) / 1024 / 1024 / 1024" | bc)
echo -n "🔍 Available Memory... "
if [ "$MEMORY_GB" -ge 8 ]; then
    echo -e "${GREEN}✅ ${MEMORY_GB}GB (sufficient)${NC}"
    ((CHECKS_PASSED++))
else
    echo -e "${YELLOW}⚠️ ${MEMORY_GB}GB (may be tight)${NC}"
    echo -e "   ${YELLOW}Recommendation: Close other applications${NC}"
fi

# Check available disk space (in GB)
DISK_GB=$(df -BG . | awk 'NR==2 {print $4}' | sed 's/G//')
echo -n "🔍 Available Disk Space... "
if [ "$DISK_GB" -ge 10 ]; then
    echo -e "${GREEN}✅ ${DISK_GB}GB (sufficient)${NC}"
    ((CHECKS_PASSED++))
else
    echo -e "${RED}❌ ${DISK_GB}GB (insufficient)${NC}"
    echo -e "   ${YELLOW}Need at least 10GB free space${NC}"
    ((CHECKS_FAILED++))
fi

# Check CPU cores
CPU_CORES=$(sysctl -n hw.ncpu)
echo -n "🔍 CPU Cores... "
if [ "$CPU_CORES" -ge 2 ]; then
    echo -e "${GREEN}✅ ${CPU_CORES} cores (sufficient)${NC}"
    ((CHECKS_PASSED++))
else
    echo -e "${YELLOW}⚠️ ${CPU_CORES} cores (may be slow)${NC}"
fi

echo ""
echo "📁 Checking Project Files"
echo "-------------------------"

check_requirement "Project directory" "test -d /Users/I528935/Desktop/APIBP-20242YB-Team-07" "Make sure you're in the right directory"
check_requirement "Kubernetes manifests" "test -d /Users/I528935/Desktop/APIBP-20242YB-Team-07/k8s" "k8s directory should exist"
check_requirement "Docker files" "find /Users/I528935/Desktop/APIBP-20242YB-Team-07/backend -name Dockerfile | head -1" "Dockerfiles should exist in backend services"
check_requirement "Deploy script" "test -x /Users/I528935/Desktop/APIBP-20242YB-Team-07/k8s/deploy.sh" "deploy.sh should be executable"

echo ""
echo "🌐 Network Connectivity"
echo "----------------------"

check_requirement "Internet connection" "ping -c 1 google.com" "Check your internet connection"
check_requirement "Docker Hub access" "curl -s https://hub.docker.com" "Check if Docker Hub is accessible"

echo ""
echo "📊 Pre-flight Summary"
echo "====================="
echo -e "✅ Checks Passed: ${GREEN}$CHECKS_PASSED${NC}"
echo -e "❌ Checks Failed: ${RED}$CHECKS_FAILED${NC}"

echo ""
if [ $CHECKS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All pre-flight checks passed!${NC}"
    echo ""
    echo "🚀 You're ready to deploy. Run:"
    echo "   ./test-deployment.sh    # For automated testing"
    echo "   ./demo-build.sh         # For quick demo"
    echo "   ./k8s/deploy.sh         # For full deployment"
else
    echo -e "${RED}❌ Please fix the issues above before proceeding.${NC}"
    echo ""
    echo "🔧 Common fixes:"
    echo "   • Start Docker Desktop"
    echo "   • Install missing tools with brew"
    echo "   • Free up disk space"
    echo "   • Check internet connection"
fi

echo ""
echo "📖 For detailed instructions, see:"
echo "   README-DEPLOYMENT.md"
echo "   TESTING-GUIDE.md"