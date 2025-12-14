#!/bin/bash
# Deploy ApplicationSet to Argo CD
# This tells Argo CD to watch your 4 developer branches

echo "=========================================="
echo "Deploying ApplicationSet..."
echo "=========================================="

echo "[1] Checking if applicationset.yaml was updated..."
if grep -q "YOUR_ORG" k8s/argocd/applicationset.yaml; then
    echo "ERROR: Please update k8s/argocd/applicationset.yaml"
    echo "Replace 'https://github.com/YOUR_ORG/Projet_fwk' with your actual GitHub URL"
    exit 1
fi

echo "[2] Applying ApplicationSet..."
kubectl apply -f k8s/argocd/applicationset.yaml -n argocd

echo "[3] Waiting for Applications to be created..."
sleep 5

echo "[4] Listing generated Applications..."
kubectl get applications -n argocd

echo ""
echo "=========================================="
echo "ApplicationSet deployed successfully!"
echo "=========================================="
echo ""
echo "Argo CD will now:"
echo "  - Watch feature/dev1 branch → deploy to dev1-ns"
echo "  - Watch feature/dev2 branch → deploy to dev2-ns"
echo "  - Watch feature/dev3 branch → deploy to dev3-ns"
echo "  - Watch feature/dev4 branch → deploy to dev4-ns"
echo ""
echo "Check status:"
echo "  kubectl get applications -n argocd -w"
echo ""
