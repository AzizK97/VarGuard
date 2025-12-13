# Projet_fwk
Network Security Monitoring project (semester project)

This repository contains a multi-component network-security monitoring stack:

- `suricata` — IDS producing `eve.json` events
- `scan-app` — Java Spring backend (ingests Suricata logs, exposes REST/SSE, runs Nmap scans)
- `dashboard` — React + Vite frontend showing alerts and statistics
- `postgres` — relational database for persistence
- `elasticsearch` — search/index store for alerts

The project supports both Docker Compose and k3s manifests under the `k8s/` folder. For local developer convenience the repo provides automation scripts to build images and import them into a local k3s cluster.

## What changed in this branch
- HostPath → PVC: The k8s manifests for Suricata and the backend were updated to mount a `PersistentVolumeClaim` named `suricata-logs` instead of using an absolute `hostPath` pointing into a developer's home directory. This makes deployments portable across machines and avoids creating repo-specific absolute paths on developer nodes.
- Documentation: `k8s/README_K3S.md` was updated to describe the PVC behavior.

> If you need the old `hostPath` behavior for quick local testing, you can revert the volumes in `k8s/suricata.yaml` and `k8s/scan-app.yaml` to the original `hostPath` snippet (see "Revert to hostPath" below).

## Quickstart (recommended)
Prerequisites

- k3s installed and running (single-node k3s is fine)
- Docker (or Podman) installed locally to build images
- `kubectl` configured to talk to your k3s cluster (or use `sudo k3s kubectl`)
- Sudo access to import images into k3s containerd (scripts use `k3s ctr images import`)
- Enough free disk space for image tarballs (check with `df -h`)

Steps

1. Clone the branch and change to repo root:

```bash
git checkout <branch>
cd Projet_fwk
```

2. Build and import images into k3s (recommended, automated):

```bash
chmod +x rebuild_and_setup.sh
./rebuild_and_setup.sh
# Or if images are already built: ./finish_setup.sh
```

What the script does:

- Builds `dashboard`, `scan-app`, and `suricata` images
- Pulls infra images (`postgres`, `elasticsearch`)
- Saves all images to temporary tar files
- Imports tar files into k3s containerd so Deployments with `imagePullPolicy: Never` can run
- Deletes the tar files and restarts pods in the `project-fwk` namespace

3. Apply k8s manifests (if not already applied or after edits):

```bash
kubectl apply -f k8s/01-storage.yaml
kubectl apply -f k8s/suricata.yaml
kubectl apply -f k8s/scan-app.yaml
kubectl apply -f k8s/elasticsearch.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/dashboard.yaml
```

4. Watch pods and verify health:

```bash
kubectl get pods,svc,pvc -n project-fwk -w
kubectl describe pvc suricata-logs -n project-fwk
```

5. Access the dashboard:

```
http://localhost:30080
```

## Troubleshooting (common issues)

- ErrImageNeverPull / missing images
	- Cause: local images (`dashboard:latest`, `scan-app:latest`) are referenced but not present in k3s containerd and manifests use `imagePullPolicy: Never`.
	- Fix: run `./rebuild_and_setup.sh` to build and import images, or build+import manually.

- ImagePullBackOff for infra images
	- Cause: node cannot pull from remote registry (DNS/network) or image name is incorrect.
	- Fix: ensure network/DNS working, or preload images locally (script handles this).

- NodeHasDiskPressure (pods Pending)
	- Cause: node ran low on disk and kubelet prevented scheduling.
	- Fix: delete unused images or prune, or temporarily remove the taint:

```bash
kubectl taint nodes <NODE_NAME> node.kubernetes.io/disk-pressure:NoSchedule-
```

- PVC Pending
	- Cause: no dynamic provisioner or storage class mismatch.
	- Fix: ensure `local-path` provisioner is installed (k3s usually has it) or adjust `storageClassName` on PVC/PV.

- Suricata permissions and networking
	- Suricata runs with `hostNetwork: true` and requires capabilities (`NET_ADMIN`, `NET_RAW`). On some environments these are restricted. If Suricata fails to start, check pod events and logs. For restricted clusters you can run Suricata outside k3s or remove privileged requirements for non-capture testing.

## Revert to hostPath (if desired)
To revert the PVC mount back to hostPath, edit `k8s/suricata.yaml` and `k8s/scan-app.yaml` volumes and replace the `persistentVolumeClaim` block with:

```yaml
volumes:
- name: logs
	hostPath:
		path: /home/<username>/Projet_fwk/suricata-logs
		type: DirectoryOrCreate
```

Make sure the directory exists on the node (or kubelet will create it) and has correct permissions:

```bash
sudo mkdir -p /home/<username>/Projet_fwk/suricata-logs
sudo chown 1000:1000 /home/<username>/Projet_fwk/suricata-logs
```

## Recommendations & next improvements
- Keep `suricata-logs/` in `.gitignore` to avoid committing large logs into Git history.
- Prefer pushing images to a registry and using `imagePullPolicy: IfNotPresent` for easier CI/cluster portability.
- Add readiness/liveness probes to the `scan-app` and `elasticsearch` deployments to improve startup resilience.
- Consider adding a `DEVELOPMENT.md` that includes this guide and a portable `scripts/load-images-to-k3s.sh` helper.

## Where to look for more details
- Kubernetes manifests: `k8s/`
- Dashboard code: `dashboard/`
- Backend code: `scan/`
- Suricata config/rules: `docker/suricata/`
