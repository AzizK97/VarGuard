# QUICK START - Argo CD Setup (3 Commands)

## Assumption
- You have a GitHub Personal Access Token (from https://github.com/settings/tokens)
- Your repo is: `https://github.com/YOUR_ORG/Projet_fwk`

## 3-Command Installation

### **Command 1: Install Argo CD**
```bash
./k8s/argocd/01-install.sh
```

### **Command 2: Add Your GitHub Repo**
Edit `k8s/argocd/02-add-repo.sh` and set:
```bash
REPO_URL="https://github.com/YOUR_ORG/Projet_fwk"
GITHUB_TOKEN="ghp_your_token_here"
```

Then run:
```bash
./k8s/argocd/02-add-repo.sh
```

### **Command 3: Deploy the ApplicationSet**
Edit `k8s/argocd/applicationset.yaml` and set:
```yaml
repoURL: 'https://github.com/YOUR_ORG/Projet_fwk'  # <- YOUR URL
```

Then run:
```bash
./k8s/argocd/03-deploy-appset.sh
```

---

## Verify It Works

```bash
# Check Applications created
kubectl get applications -n argocd

# Watch sync progress
kubectl get applications -n argocd -w

# Check namespaces created
kubectl get namespaces | grep dev
```

---

## Access Dashboard

```bash
# Terminal 1: Port-forward
kubectl -n argocd port-forward svc/argocd-server 8080:443

# Terminal 2: Get password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo

# Browser: https://localhost:8080
# Username: admin
# Password: (from above)
```

---

Done! Your Argo CD is now watching all 4 developer branches.
