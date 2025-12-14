#!/bin/bash
# Uninstall Argo CD (cleanup)
# Use this to remove Argo CD if needed

echo "=========================================="
echo "WARNING: Removing Argo CD..."
echo "=========================================="

read -p "Are you sure? This will delete all Argo CD resources. (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "Cancelled."
    exit 0
fi

echo "[1] Deleting Argo CD namespace..."
kubectl delete namespace argocd

echo "[2] Done."
echo "All Argo CD resources have been removed."
