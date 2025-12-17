# ArgoCD Quick Start Guide

## Overview

ArgoCD is a **Continuous Deployment (CD) tool** that automatically deploys applications from your GitHub repository to your Kubernetes cluster. It watches your repo for changes and syncs them to the cluster.

## Current Setup

- **ArgoCD Namespace:** `argocd-fresh`
- **Dashboard URL:** https://localhost:8443
- **Username:** admin
- **Password:** `LPaXDqq7kGRTl4Ds`
- **Connected Repository:** https://github.com/turkimedaziz/Projet_fwk.git

## How to Run

### 1. Start ArgoCD Port-Forward
```bash
kubectl port-forward svc/argocd-server -n argocd-fresh 8443:443 &
```

### 2. Access Dashboard
```
https://localhost:8443
```

### 3. Login
- Username: `admin`
- Password: `LPaXDqq7kGRTl4Ds`

## Creating a Deployment Application

### Step 1: Create Application
1. Click **Applications** → **+ NEW APP**
2. Fill in:
   - **Application Name:** `my-app` (lowercase, hyphens only, no spaces)
   - **Project:** `default`
   - **Repository URL:** `https://github.com/turkimedaziz/Projet_fwk.git`
   - **Path:** `k8s/` (your Kubernetes manifests folder)
   - **Destination Cluster:** `https://kubernetes.default.svc`
   - **Destination Namespace:** `project-fwk` (or your target namespace)

### Step 2: Sync Application
1. Click **SYNC** button
2. Select **SYNCHRONIZE** to deploy to cluster

### Step 3: View Deployment
- **DETAILS tab:** Application health and sync status
- **RESOURCES tab:** Deployed Kubernetes resources
- **LOGS tab:** Deployment logs
- **DIFF tab:** Git vs Cluster differences (only shows if differences exist)

## Key Features

| Feature | What it does |
|---------|-------------|
| **Sync** | Deploy changes from Git to cluster |
| **DIFF** | Compare Git desired state vs current cluster state |
| **Health** | Shows pod/resource health status |
| **Auto-Sync** | Automatically deploy when Git changes (optional) |

## Troubleshooting

### Application won't connect to GitHub
**Error:** "Unable to connect to repository"
- Check DNS: `kubectl get pods -n kube-system | grep coredns`
- If CoreDNS is down, restart: `kubectl rollout restart deployment/coredns -n kube-system`

### Port-Forward fails
```bash
# Kill old process
pkill -f "port-forward"

# Restart
kubectl port-forward svc/argocd-server -n argocd-fresh 8443:443 &
```

### Application name invalid
**Error:** "lowercase RFC 1123 subdomain..."
- Use lowercase names with hyphens: ✅ `my-app-name`
- No spaces: ❌ `my app name`

## Useful Commands

```bash
# List all applications
kubectl get applications -n argocd-fresh

# Get application details
kubectl describe app <app-name> -n argocd-fresh

# View application logs
kubectl logs deployment/argocd-server -n argocd-fresh --tail=50

# Check DNS status
kubectl get pods -n kube-system

# Get admin password
kubectl -n argocd-fresh get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

## File Structure

Your deployment manifests should be in:
```
Projet_fwk/
├── k8s/
│   ├── scan-app.yaml          # Scan application
│   ├── dashboard.yaml         # Dashboard
│   ├── postgres.yaml          # Database
│   ├── redis.yaml             # Cache
│   └── ... (other services)
```

## Auto-Sync Setup (Optional)

To auto-deploy when you push to GitHub:
1. Open Application → **APP DETAILS**
2. Scroll to **SYNC POLICY**
3. Select **AUTOMATIC**
4. Save

Now every GitHub push = automatic cluster update ✨

## What's Next?

1. ✅ ArgoCD is running
2. ✅ GitHub repo connected
3. ⏭️ Create applications for each service
4. ⏭️ Set up auto-sync for CI/CD pipeline
5. ⏭️ Monitor deployments in real-time

---

**Need help?** Check ArgoCD docs: https://argo-cd.readthedocs.io/
