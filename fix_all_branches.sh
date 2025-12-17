#!/bin/bash

# Fix all branches for auto-deployment
# Adds missing kustomization.yaml and necessary files to each branch

set -e

REPO_DIR="/home/abir/Downloads/Projet_fwk-main"
cd "$REPO_DIR"

echo "=========================================="
echo "Fixing All Branches for Auto-Deployment"
echo "=========================================="
echo ""

# Get current branch to restore later
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Function to fix a branch
fix_branch() {
    local branch=$1
    local namespace=$2
    
    echo "[*] Fixing branch: $branch → namespace: $namespace"
    
    git fetch origin "$branch"
    git checkout "$branch"
    
    # Ensure k8s directory exists
    mkdir -p k8s
    
    # Copy necessary files from main-with-argocd if missing
    git show main-with-argocd:k8s/kustomization.yaml > k8s/kustomization.yaml.tmp 2>/dev/null || true
    
    if [ -f k8s/kustomization.yaml.tmp ]; then
        # Update namespace in kustomization
        sed "s/namespace: project-fwk/namespace: ${namespace}/" \
            k8s/kustomization.yaml.tmp > k8s/kustomization.yaml
        rm -f k8s/kustomization.yaml.tmp
        
        # Update branch label
        sed -i "s/branch: main-with-argocd/branch: ${branch}/" k8s/kustomization.yaml
        
        # Ensure namespace and storage files exist
        if ! git ls-files | grep -q "k8s/00-namespace.yaml"; then
            git show main-with-argocd:k8s/00-namespace.yaml > k8s/00-namespace.yaml
        fi
        
        if ! git ls-files | grep -q "k8s/01-storage.yaml"; then
            git show main-with-argocd:k8s/01-storage.yaml > k8s/01-storage.yaml
        fi
        
        # Commit if there are changes
        if git diff --quiet; then
            echo "   ✓ Already up to date"
        else
            git add k8s/
            git commit -m "Add ArgoCD auto-deployment files (kustomization.yaml)"
            git push origin "$branch"
            echo "   ✓ Fixed and pushed"
        fi
    else
        echo "   ⚠ Could not get kustomization from main-with-argocd"
    fi
    
    echo ""
}

# Fix each branch
fix_branch "hedil" "hedil-ns"
fix_branch "aziz" "aziz-ns"
fix_branch "turki-redis" "turki-redis-ns"

# Restore original branch
git checkout "$CURRENT_BRANCH"

echo "=========================================="
echo "✓ All branches fixed!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Each branch now has kustomization.yaml"
echo "2. Deploy manually: kubectl apply -k k8s/ -n <namespace>"
echo "3. Or wait for ArgoCD to auto-deploy"
echo ""
