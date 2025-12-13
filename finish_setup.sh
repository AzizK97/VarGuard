#!/bin/bash
set -e

echo "1. Pulling Elasticsearch (Retrying)..."
docker pull docker.elastic.co/elasticsearch/elasticsearch:8.11.0

echo "2. Saving images to tar files..."
# We save all of them to be sure K3s gets the latest built versions
docker save dashboard:latest -o dashboard.tar
docker save scan-app:latest -o scan-app.tar
docker save suricata:latest -o suricata.tar
docker save postgres:latest -o postgres.tar
docker save docker.elastic.co/elasticsearch/elasticsearch:8.11.0 -o elasticsearch.tar

echo "3. Importing images into K3s..."
sudo k3s ctr images import dashboard.tar
sudo k3s ctr images import scan-app.tar
sudo k3s ctr images import suricata.tar
sudo k3s ctr images import postgres.tar
sudo k3s ctr images import elasticsearch.tar

echo "4. Cleaning up tar files..."
rm *.tar

echo "5. Restarting Pods..."
kubectl delete pods -n project-fwk --all

echo "Done! Watch the pods with: kubectl get pods -n project-fwk -w"
