#!/bin/bash

# Cleanup script for Cooknect application
set -e

echo "🧹 Cleaning up Cooknect application from Minikube..."

# Delete all resources in the cooknect namespace
kubectl delete namespace cooknect --ignore-not-found=true

echo "✅ Cleanup completed successfully!"
echo "   All resources in the 'cooknect' namespace have been removed."