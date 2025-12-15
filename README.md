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

2. Suricata network configuration

Run this script to configure Suricata to your machine's network interface

```bash
chmod +x update_suricata_network.sh
./update_suricata_network.sh
```

### update_suricata_network.sh — local-first configuration helper

This repository includes `update_suricata_network.sh`, a small safe helper that detects your host's primary network interface and network CIDR and then renders a Suricata configuration locally. It follows a "local-first" workflow: by default it only writes files under `tmp/` and updates a few local templates (backups are created). It will only change the running k3s cluster when you explicitly pass `--apply`.

What it does (high level)

- Detects the outbound interface (e.g. `eth0`, `wlp4s0`) and the IP/prefix assigned to that interface.
- Computes the network CIDR to use for Suricata's `HOME_NET` (uses Python's `ipaddress` module for correctness).
- Renders `tmp/suricata.rendered.yaml` from the canonical template at `docker/suricata/suricata.yaml`.
- Updates these local files in-place (but creates backups with a `.bak` suffix):
	- `docker/suricata/suricata.yaml` (template)
	- `docker/Dockerfile.suricata` (sets `ENV NETWORK_INTERFACE` if present)
	- `k8s/suricata.yaml` (deployment arg `-i` for the container command)

CLI / examples

```bash
# local-only (default): renders tmp/suricata.rendered.yaml and updates local templates (backups created)
./update_suricata_network.sh

# apply to k3s cluster (create/update ConfigMap(s) and restart the suricata deployment)
./update_suricata_network.sh --apply

# override detected interface (useful if auto-detection picks a VPN or wrong device)
./update_suricata_network.sh --iface eth0

# show help
./update_suricata_network.sh --help
```

Cluster apply behavior

When run with `--apply` the script will generate a `ConfigMap` YAML from `tmp/suricata.rendered.yaml` and apply it to the `project-fwk` namespace using a safe `kubectl create configmap --from-file=... --dry-run=client -o yaml | kubectl apply -f -` pipeline. It will also update the `suricata-rules` ConfigMap from `docker/suricata/custom.rules` (if present) and then restart the Suricata deployment with `kubectl rollout restart`.

Safety notes and backups

- The script will not (by default) modify the repository `k8s/suricata-config.yaml` file which in some branches may embed the full `suricata.yaml` as a literal block. This avoids accidental YAML corruption. Instead it writes a rendered copy to `tmp/suricata.rendered.yaml` and (optionally) creates cluster ConfigMaps from that file.
- Files modified in-place are backed up with the same path plus `.bak` (for example `docker/suricata/suricata.yaml.bak`). To revert local edits you can restore those backups or use `git checkout -- <file>`.

Verification commands

After rendering or applying, verify the changes with these commands:

```bash
# Inspect the rendered file
less tmp/suricata.rendered.yaml

# Check local backups exist
ls -l docker/suricata/suricata.yaml.bak docker/Dockerfile.suricata.bak k8s/suricata.yaml.bak

# If applied to cluster: check pods and logs
kubectl get pods -n project-fwk
kubectl logs -n project-fwk -l app=suricata --tail=200

# Confirm Suricata reports the network/interface in its startup logs
kubectl logs -n project-fwk -l app=suricata | grep -i "HOME_NET\|creating .* threads\|Engine started"
```

Typical use cases

- Developer onboarding: run once on a developer machine to set the correct capture interface and HOME_NET before building images and starting the stack.
- Network changes: re-run after switching networks (e.g. laptop moves from office to home or to a VPN) and then re-apply with `--apply` if you want the cluster to pick up the new config immediately.
- CI / automation: call the script in a CI job to produce a deterministic `suricata.yaml` file and then `kubectl apply` that rendered file in a controlled CD pipeline (prefer to commit the rendered file or create a reproducible pipeline step rather than mutating local templates on CI agents).

Edge cases & troubleshooting

- Multiple active interfaces / VPNs: auto-detection picks the default route. Use `--iface` to override if the detected interface is undesirable.
- Missing tools: the script requires `ip`, `awk`, and `python3` (standard on most Linux systems). If `python3` is not available the CIDR computation may fail — install the `python3` package or compute the network manually and pass `--iface`.
- Repository manifest hygiene: if `k8s/suricata-config.yaml` is corrupted (YAML parse errors when running `kubectl apply -f k8s/suricata-config.yaml`), prefer the create-from-file approach described above instead of editing that embedded literal. Consider replacing the embedded ConfigMap in the repo with a small manifest that documents the create-from-file workflow.

If you'd like, I can also add a short `scripts/README_UPDATE_SURICATA.md` that contains the same content but focused only on this helper and the recommended safe workflow.


3. Build and import images into k3s (recommended, automated):

```bash
chmod +x rebuild_and_setup.sh
./rebuild_and_setup.sh
```

What the script does:

- Builds `dashboard`, `scan-app`, and `suricata` images
- Pulls infra images (`postgres`, `elasticsearch`)
- Saves all images to temporary tar files
- Imports tar files into k3s containerd so Deployments with `imagePullPolicy: Never` can run
- Deletes the tar files and restarts pods in the `project-fwk` namespace

4. Apply k8s manifests (if not already applied or after edits):

```bash
kubectl apply -f k8s/01-storage.yaml
kubectl apply -f k8s/suricata.yaml
kubectl apply -f k8s/scan-app.yaml
kubectl apply -f k8s/elasticsearch.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/dashboard.yaml
```

5. Watch pods and verify health:

```bash
kubectl get pods,svc,pvc -n project-fwk -w
kubectl describe pvc suricata-logs -n project-fwk
```

6. Access the dashboard:

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
