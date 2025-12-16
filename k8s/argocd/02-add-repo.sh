#!/bin/bash
# Add Git Repository to Argo CD (Public Repository - No Token Needed)

REPO_URL="https://github.com/turkimedaziz/Projet_fwk"

echo "=========================================="
echo "Adding Git Repository to Argo CD..."
echo "=========================================="

# Check if argocd CLI is available
echo "[1] Checking if argocd CLI is installed..."
if ! command -v argocd &> /dev/null; then
    echo "argocd CLI not found. Installing..."
    curl -sSL -o /usr/local/bin/argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
    chmod +x /usr/local/bin/argocd
fi

echo "[2] Port-forwarding to Argo CD (background)..."
kubectl -n argocd port-forward svc/argocd-server 8080:443 &
PFPID=$!
sleep 3

echo "[3] Logging in to Argo CD..."
ADMIN_PASS=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)
argocd login localhost:8080 --username admin --password "$ADMIN_PASS" --insecure

echo "[4] Adding Git repository (public - no token needed)..."
argocd repo add "$REPO_URL" \
  --insecure-skip-server-verification

echo ""
echo "=========================================="
echo "Repository added successfully!"
echo "=========================================="

# Clean up port-forward
kill $PFPID 2>/dev/null || true

echo ""
echo "You can now deploy the ApplicationSet!"
