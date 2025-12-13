#!/bin/bash
set -e

echo "1. Rebuilding Dashboard..."
docker build -t dashboard:latest dashboard/

echo "2. Saving Dashboard image..."
docker save dashboard:latest -o dashboard.tar

echo "3. Importing Dashboard into K3s..."
sudo k3s ctr images import dashboard.tar

echo "4. Cleaning up..."
rm dashboard.tar

echo "5. Restarting Dashboard Pod..."
kubectl delete pod -n project-fwk -l app=dashboard

echo "Done! Refresh your browser at http://localhost:30081"
