#!/bin/bash
# Argo CD Installation Script
# Run this ONCE to install Argo CD in your cluster

set -e

echo "=========================================="
echo "Installing Argo CD..."
echo "=========================================="

# Step 1: Create namespace
echo "[1] Creating argocd namespace..."
kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -

# Step 2: Install Argo CD
echo "[2] Installing Argo CD components..."
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Wait for Argo CD to be ready
echo "[3] Waiting for Argo CD server to be ready (this may take 1-2 minutes)..."
kubectl rollout status deployment/argocd-server -n argocd --timeout=5m

echo ""
echo "=========================================="
echo "Argo CD installed successfully!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Port-forward to access UI:"
echo "   kubectl -n argocd port-forward svc/argocd-server 8080:443"
echo ""
echo "2. In another terminal, get admin password:"
echo "   kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath=\"{.data.password}\" | base64 -d"
echo ""
echo "3. Access Argo CD at: https://localhost:8080"
echo "   Username: admin"
echo "   Password: (from step 2)"
echo ""
