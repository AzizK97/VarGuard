# Argo CD Setup Guide

## Overview
This guide installs Argo CD to automatically deploy your 4 developer branches to separate Kubernetes namespaces.

**Architecture:**
- Argo CD (single instance, managed from `main` branch)
- ApplicationSet (in `k8s/argocd/applicationset.yaml`)
- 4 Applications (auto-generated, one per branch):
  - `dev1-app` → watches `feature/dev1`
  - `dev2-app` → watches `feature/dev2`
  - `dev3-app` → watches `feature/dev3`
  - `dev4-app` → watches `feature/dev4`

---

## Prerequisites

```bash
# You need:
# - kubectl configured to access your k3s cluster
# - A GitHub repo URL (https or SSH)
# - A GitHub Personal Access Token (with 'repo' scope) if using HTTPS
```

---

## Step-by-Step Installation

### **1. Install Argo CD**

```bash
cd /path/to/Projet_fwk

chmod +x k8s/argocd/01-install.sh
./k8s/argocd/01-install.sh
```

This will:
- Create `argocd` namespace
- Deploy Argo CD server, controller, and API

**Output:** You'll see instructions for accessing Argo CD UI.

---

### **2. Configure GitHub Access**

#### Option A: Using HTTPS + GitHub Token (Recommended)

1. **Create a Personal Access Token** on GitHub:
   - Go to GitHub → Settings → Developer settings → Personal access tokens
   - Click "Generate new token (classic)"
   - Scopes needed: `repo` (full control)
   - Copy the token (you'll use it once)

2. **Edit the repo add script:**

```bash
# Edit k8s/argocd/02-add-repo.sh
# Replace these lines with your values:
REPO_URL="https://github.com/YOUR_ORG/Projet_fwk"    # e.g., https://github.com/myorg/Projet_fwk
GITHUB_TOKEN="ghp_xxxxxxxxxxxxxxxxxxxxxx"            # e.g., ghp_abc123def456...
```

3. **Run the script:**

```bash
chmod +x k8s/argocd/02-add-repo.sh
./k8s/argocd/02-add-repo.sh
```

#### Option B: Using SSH (Advanced)

If you prefer SSH:

```bash
# Manually add repo via argocd CLI
kubectl -n argocd port-forward svc/argocd-server 8080:443 &
ADMIN_PASS=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)
argocd login localhost:8080 --username admin --password "$ADMIN_PASS" --insecure

# Add SSH repo
argocd repo add git@github.com:YOUR_ORG/Projet_fwk.git \
  --ssh-private-key-path ~/.ssh/id_rsa
```

---

### **3. Update ApplicationSet with Your Repo URL**

Edit `k8s/argocd/applicationset.yaml`:

```bash
nano k8s/argocd/applicationset.yaml
```

Find this line and replace it with your actual repo URL:

```yaml
repoURL: 'https://github.com/YOUR_ORG/Projet_fwk'  # <- CHANGE THIS
```

Example:
```yaml
repoURL: 'https://github.com/mycompany/Projet_fwk'
```

---

### **4. Deploy ApplicationSet**

```bash
chmod +x k8s/argocd/03-deploy-appset.sh
./k8s/argocd/03-deploy-appset.sh
```

This will:
- Apply `applicationset.yaml` to the cluster
- Argo CD automatically creates 4 Applications
- Each Application watches one developer branch

---

### **5. Verify Everything Works**

```bash
# Check if Applications were created
kubectl get applications -n argocd

# You should see 4 apps:
# NAME              SYNC STATUS
# dev1-app          Syncing / Synced
# dev2-app          Syncing / Synced
# dev3-app          Syncing / Synced
# dev4-app          Syncing / Synced

# Watch sync progress
kubectl get applications -n argocd -w

# Check created namespaces
kubectl get namespaces | grep dev

# You should see: dev1-ns, dev2-ns, dev3-ns, dev4-ns
```

---

### **6. Access Argo CD Dashboard**

```bash
# Port-forward to the UI
kubectl -n argocd port-forward svc/argocd-server 8080:443

# In another terminal, get the password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

Then open in browser: **https://localhost:8080**
- Username: `admin`
- Password: (from command above)

You'll see all 4 applications and their sync status!

---

## Developer Workflow

### **For Each Developer (e.g., Dev1):**

```bash
# 1. Work on your branch
git checkout feature/dev1
# ... make code changes ...
git push origin feature/dev1

# 2. Wait for Argo CD to detect changes
# Argo CD polls Git every 3 minutes by default

# 3. Check deployment status
kubectl get application dev1-app -n argocd -o yaml | grep -A5 status
# OR use the dashboard

# 4. Verify pods are running in your namespace
kubectl get pods -n dev1-ns
```

### **Changing Branch Names**

If your actual branch names are different (e.g., `dev1-branch` instead of `feature/dev1`):

1. Edit `k8s/argocd/applicationset.yaml`
2. Update the `branch` field under `generators.list.elements`
3. Commit to `main` and push
4. Argo CD will automatically update

---

## Troubleshooting

### **Applications stay in "Syncing" state**

```bash
# Check application details
kubectl describe application dev1-app -n argocd

# Check logs
kubectl logs -n argocd deployment/argocd-controller-manager | tail -50
```

### **"Repository not accessible" error**

- Verify GitHub token is correct and not expired
- Make sure repo URL is exactly correct
- Check firewall/network access to GitHub

```bash
# Test connectivity
kubectl exec -it deployment/argocd-server -n argocd -- bash
# Inside: ping github.com
```

### **Namespaces not created**

- Ensure `syncOptions: [CreateNamespace=true]` is in applicationset.yaml
- Manually create if needed:

```bash
kubectl create namespace dev1-ns
kubectl create namespace dev2-ns
kubectl create namespace dev3-ns
kubectl create namespace dev4-ns
```

---

## Next Steps

1. **Protect main branch** (GitHub settings):
   - Require PR reviews before merge
   - Require status checks (CI) to pass

2. **Train team** on the new workflow

3. **Monitor** deployments in Argo CD dashboard

4. **Optional:** Configure notifications (Slack, email) when deployments complete

---

## Cleanup

If you need to remove Argo CD:

```bash
chmod +x k8s/argocd/99-uninstall.sh
./k8s/argocd/99-uninstall.sh
```

---

## Files Reference

| File | Purpose |
|------|---------|
| `01-install.sh` | Install Argo CD to cluster |
| `02-add-repo.sh` | Add GitHub repo to Argo CD |
| `applicationset.yaml` | Define which branches to deploy |
| `03-deploy-appset.sh` | Deploy ApplicationSet |
| `99-uninstall.sh` | Remove Argo CD (cleanup) |

