# Network Security Monitor on K3s - Documentation & Guide

This document explains the migration of the Network Security Monitor project from Docker Compose to **k3s** (a lightweight Kubernetes distribution). It covers the architecture, core concepts, implementation details, and essential commands for managing the system.

## 1. K3s Architecture Explained

### What is K3s?
K3s is a fully compliant Kubernetes distribution designed to be lightweight and easy to install. It replaces the heavy Docker daemon with `containerd` and simplifies the Kubernetes control plane.

### Core Concepts in Our Setup

*   **Cluster**: The entire system. In our case, it's a **single-node cluster** running on your local machine.
*   **Node**: The machine running the cluster. You have one node (your laptop/server) acting as both the "control plane" (manager) and "worker" (running apps).
*   **Pod**: The smallest deployable unit. A Pod runs one or more containers.
    *   *Example*: The `scan-app` pod runs the Java backend container.
*   **Service**: A stable network endpoint to access Pods. Since Pods can die and restart (changing IPs), Services provide a permanent IP/DNS name.
    *   *Example*: The `postgres` Service allows `scan-app` to always reach the database at `postgres:5432`.
*   **Deployment**: Manages Pods. It ensures the desired number of Pods are always running. If a Pod crashes, the Deployment restarts it.
*   **ConfigMap**: Stores configuration data separately from code.
    *   *Example*: `suricata-config` stores `suricata.yaml` and custom rules.

## 2. Implementation Details

We migrated 5 services to Kubernetes manifests located in the `k8s/` directory.

### 🛡️ Suricata (`k8s/suricata.yaml`)
*   **Deployment Strategy**: Uses `hostNetwork: true`.
*   **Why?**: Suricata needs to see *real* network traffic from your physical interface (`wlp45s0`). Standard Pod networking is isolated, so we bypass it to allow packet capture.
*   **Storage**: Uses a Kubernetes `PersistentVolumeClaim` named `suricata-logs` (backed by the cluster's storage class, e.g., `local-path` on k3s) to store Suricata logs. This avoids hard-coded host paths and makes the deployment portable. To use a hostPath instead, edit `k8s/suricata.yaml` and `k8s/scan-app.yaml`.

### 🧠 Scan App (`k8s/scan-app.yaml`)
*   **Role**: The Java Spring Boot backend.
*   **Log Monitoring**: Mounts the same `suricata-logs` directory to read `eve.json` in real-time.
*   **Fixes Applied**:
    *   **CORS**: Configured to allow requests from `localhost:30080`.
    *   **Log Reading**: Modified to read `eve.json` from the beginning on startup to capture past alerts.

### 📊 Dashboard (`k8s/dashboard.yaml`)
*   **Role**: React frontend served by Nginx.
*   **Access**: Exposed via a **NodePort** service on port `30080`. You access it at `http://localhost:30080`.
*   **Nginx Proxy**: Uses a ConfigMap (`dashboard-config.yaml`) to configure Nginx. It serves the frontend AND proxies `/api` requests to the `scan-app` backend, solving mixed content and routing issues.

### 🗄️ Infrastructure
*   **Postgres (`k8s/postgres.yaml`)**: Database for storing alerts. Uses a PersistentVolume to save data even if the pod restarts.
*   **Elasticsearch (`k8s/elasticsearch.yaml`)**: Search engine for advanced querying.

## 3. Essential K3s Commands Cheat Sheet

Run these commands in your terminal to check everything about your cluster.

### 🔍 Check Status
```bash
# List all pods (running applications)
sudo /usr/local/bin/k3s kubectl get pods -A

# List all services (network endpoints)
sudo /usr/local/bin/k3s kubectl get svc

# Watch pods in real-time (Ctrl+C to stop)
sudo /usr/local/bin/k3s kubectl get pods -w
```

### 📜 View Logs
```bash
# View logs for the backend
sudo /usr/local/bin/k3s kubectl logs -l app=scan-app --tail=50 -f

# View logs for Suricata
sudo /usr/local/bin/k3s kubectl logs -l app=suricata --tail=50 -f

# View logs for the Dashboard
sudo /usr/local/bin/k3s kubectl logs -l app=dashboard
```

### 🛠️ Troubleshooting & Management
```bash
# Restart a specific component (e.g., scan-app)
sudo /usr/local/bin/k3s kubectl rollout restart deployment scan-app

# Delete a pod (forces a restart)
sudo /usr/local/bin/k3s kubectl delete pod -l app=scan-app

# Describe a pod (to see why it's crashing or pending)
sudo /usr/local/bin/k3s kubectl describe pod -l app=postgres
```

### 📦 Image Management
Since we are using local images (not pushing to Docker Hub):
```bash
# List images imported into k3s
sudo /usr/local/bin/k3s ctr images ls

# Import a new image after building it
sudo docker save -o app.tar app:latest
sudo /usr/local/bin/k3s ctr images import app.tar
```

## 4. Project Structure
```
Projet_fwk/
├── k8s/                  # Kubernetes Manifests
│   ├── suricata.yaml     # IDS definition
│   ├── scan-app.yaml     # Backend definition
│   ├── dashboard.yaml    # Frontend definition
│   ├── postgres.yaml     # Database definition
│   └── ...
├── scan/                 # Backend Source Code
├── dashboard/            # Frontend Source Code
├── docker/               # Dockerfiles
└── suricata-logs/        # Shared log directory (optional). Manifests now mount the `suricata-logs` PVC; a host directory is not required for k3s deployments.
```
