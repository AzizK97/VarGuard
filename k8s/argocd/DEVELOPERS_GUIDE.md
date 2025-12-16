# Argo CD - Developer Guide

This guide explains what each developer needs to add to their branch for Argo CD to auto-deploy.

---

## **Quick Summary**

Each developer branch must contain a `k8s/` folder with 3 files:
1. `deployment.yaml` — How your app runs
2. `service.yaml` — How to expose your app
3. `kustomization.yaml` — Applies all the above

---

## **Step-by-Step for Each Developer**

### **Step 1: Create k8s Folder**

```bash
# Switch to your branch
git checkout your-branch-name

# Create k8s folder if it doesn't exist
mkdir -p k8s
```

---

### **Step 2: Create deployment.yaml**

Create file: `k8s/deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
      - name: app
        image: your-docker-image:latest
        ports:
        - containerPort: 8080
```

**⚠️ IMPORTANT:** Replace `your-docker-image:latest` with your actual Docker image!

Examples:
- `clean-version-app:latest`
- `aziztest-app:latest`
- `hedi1-app:latest`
- `test-app:latest`

---

### **Step 3: Create service.yaml**

Create file: `k8s/service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: app-service
spec:
  selector:
    app: myapp
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
  type: ClusterIP
```

---

### **Step 4: Create kustomization.yaml**

Create file: `k8s/kustomization.yaml`

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
- deployment.yaml
- service.yaml
```

---

### **Step 5: Commit and Push**

```bash
# Add the k8s folder
git add k8s/

# Commit
git commit -m "Add k8s manifests for Argo CD deployment"

# Push to your branch
git push origin your-branch-name
```

---

## **Directory Structure**

After adding the files, your branch should look like:

```
your-branch/
├── k8s/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── kustomization.yaml
├── src/
│   └── ... (your code)
├── docker/
│   └── ... (other files)
└── ... (other files)
```

---

## **What Happens Next**

1. ✅ You push your branch with the k8s files
2. ✅ Argo CD detects the change (within 3 minutes)
3. ✅ Argo CD reads your k8s manifests from your branch
4. ✅ Your app is deployed to your namespace automatically

---

## **Verify It Deployed**

Check if your app deployed successfully:

```bash
# List all applications
kubectl get applications -n argocd

# Should show your app with "Synced" status

# Check pods in your namespace
kubectl get pods -n your-namespace

# Examples:
kubectl get pods -n clean-version-ns
kubectl get pods -n aziztest-ns
kubectl get pods -n hedi1-ns
kubectl get pods -n test-ns
```

---

## **Namespace Mapping**

| Branch | Namespace | Command |
|--------|-----------|---------|
| clean-version | clean-version-ns | `kubectl get pods -n clean-version-ns` |
| azizTest | aziztest-ns | `kubectl get pods -n aziztest-ns` |
| hedi1 | hedi1-ns | `kubectl get pods -n hedi1-ns` |
| test | test-ns | `kubectl get pods -n test-ns` |

---

## **Example: For clean-version Branch**

```bash
# 1. Checkout branch
git checkout clean-version

# 2. Create k8s folder
mkdir -p k8s

# 3. Create deployment.yaml
cat > k8s/deployment.yaml << 'EOF'
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
      - name: app
        image: clean-version-app:latest
        ports:
        - containerPort: 8080
EOF

# 4. Create service.yaml
cat > k8s/service.yaml << 'EOF'
apiVersion: v1
kind: Service
metadata:
  name: app-service
spec:
  selector:
    app: myapp
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
  type: ClusterIP
EOF

# 5. Create kustomization.yaml
cat > k8s/kustomization.yaml << 'EOF'
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
- deployment.yaml
- service.yaml
EOF

# 6. Commit and push
git add k8s/
git commit -m "Add k8s manifests for Argo CD"
git push origin clean-version

# 7. Check deployment (after 2-3 minutes)
kubectl get pods -n clean-version-ns
```

---

## **Troubleshooting**

### **Application shows "Unknown" status**
- Check if all 3 k8s files exist in your branch
- Verify YAML syntax is correct (no typos)
- Check Argo CD logs: `kubectl logs -n argocd deployment/argocd-controller-manager`

### **Pods don't appear**
- Check image name in deployment.yaml
- Verify image exists in your Docker registry
- Check pod logs: `kubectl logs -n your-namespace deployment/app-deployment`

### **Service not accessible**
- Verify service.yaml has correct selectors
- Check port mappings match your app
- Use port-forward to test: `kubectl port-forward -n your-namespace svc/app-service 8080:8080`

---

## **Questions?**

Contact the DevOps team or check Argo CD dashboard:
```bash
kubectl -n argocd port-forward svc/argocd-server 8080:443
# Open https://localhost:8080
```

---

## **Key Points**

✅ **Each branch gets its own namespace** (isolated)  
✅ **Push code + k8s files to your branch** (no main branch touch)  
✅ **Argo CD auto-detects and deploys** (every 3 minutes)  
✅ **No manual `kubectl apply` needed** (everything automatic)  
✅ **Update your image name** in deployment.yaml (important!)  

---

**Ready? Add these 3 files to your branch and push!** 🚀
