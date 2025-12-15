#!/bin/bash
set -e

echo "1. Building Dashboard..."
docker build -t dashboard:latest dashboard/

echo "2. Building Scan App..."
docker build -t scan-app:latest scan/

echo "3. Building Suricata..."
docker build -t suricata:latest -f docker/Dockerfile.suricata docker/

echo "4. Pulling Infrastructure Images..."
docker pull postgres:latest
docker pull docker.elastic.co/elasticsearch/elasticsearch:8.11.0
docker pull redis:latest

echo "5. Saving images to tar files..."
docker save dashboard:latest -o dashboard.tar
docker save scan-app:latest -o scan-app.tar
docker save suricata:latest -o suricata.tar
docker save postgres:latest -o postgres.tar
docker save docker.elastic.co/elasticsearch/elasticsearch:8.11.0 -o elasticsearch.tar
docker save redis:latest -o redis.tar

echo "6. Importing images into K3s..."
sudo /usr/local/bin/k3s ctr images import dashboard.tar
sudo /usr/local/bin/k3s ctr images import scan-app.tar
sudo /usr/local/bin/k3s ctr images import suricata.tar
sudo /usr/local/bin/k3s ctr images import postgres.tar
sudo /usr/local/bin/k3s ctr images import elasticsearch.tar
sudo /usr/local/bin/k3s ctr images import redis.tar
echo "7. Cleaning up tar files..."
rm *.tar

echo "8. Restarting Pods..."
/usr/local/bin/kubectl delete pods -n project-fwk --all

echo "Done! Watch the pods with: kubectl get pods -n project-fwk -w"
