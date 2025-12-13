#!/bin/bash
set -e

# echo "1. Building Suricata locally..."
# docker build -t suricata:latest -f docker/Dockerfile.suricata docker/

echo "2. Saving images from Docker..."
docker save dashboard:latest -o dashboard.tar
docker save scan-app:latest -o scan-app.tar
docker save postgres:latest -o postgres.tar
docker save docker.elastic.co/elasticsearch/elasticsearch:8.11.0 -o elasticsearch.tar
# docker save suricata:latest -o suricata.tar

echo "3. Importing images into K3s..."
sudo /usr/local/bin/k3s ctr images import dashboard.tar
sudo /usr/local/bin/k3s ctr images import scan-app.tar
sudo /usr/local/bin/k3s ctr images import postgres.tar
sudo /usr/local/bin/k3s ctr images import elasticsearch.tar
# sudo /usr/local/bin/k3s ctr images import suricata.tar

echo "4. Cleaning up..."
rm dashboard.tar scan-app.tar postgres.tar elasticsearch.tar # suricata.tar

echo "Done! Restarting pods..."
kubectl delete pods -n project-fwk --all
