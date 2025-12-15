#!/usr/bin/env bash
set -euo pipefail

SURICATA_CFG_YAML="k8s/suricata-config.yaml"
SURICATA_DEPLOY_YAML="k8s/suricata.yaml"
NAMESPACE="project-fwk"

IFACE=$(ip route get 8.8.8.8 2>/dev/null | awk '/dev/ { for(i=1;i<=NF;i++) if($i=="dev") print $(i+1) }' | head -n1)
if [ -z "$IFACE" ]; then
  echo "Failed to detect default interface. Try specifying manually."
  exit 1
fi
echo "Detected interface: $IFACE"

IP_REF=$(ip -o -f inet addr show dev "$IFACE" | awk '{print $4}' | head -n1)
if [ -z "$IP_REF" ]; then
  echo "No IPv4 address found on $IFACE. Please ensure interface has IPv4."
  exit 1
fi
echo "Detected IP/prefix: $IP_REF"

NET=$(
    python3 -c 'import ipaddress,sys; print(ipaddress.ip_interface(sys.argv[1]).network)' "$IP_REF"
)
if [ -z "$NET" ]; then
  echo "Failed to compute network from $IP_REF"
  exit 1
fi
echo "Computed network: $NET"

sed -i -E "s|(HOME_NET: ).*|\1\"[${NET}]\"|" "$SURICATA_CFG_YAML"
sed -i -E "s|(interface: ).*|\1\"${IFACE}\"|" "$SURICATA_CFG_YAML"

DOCKER_SURICATA_YAML="docker/suricata/suricata.yaml"
DOCKER_DOCKERFILE="docker/Dockerfile.suricata"

if [ -f "$DOCKER_SURICATA_YAML" ]; then
  
  sed -i -E "s|(^\s*HOME_NET:\s*).*$|\1\"[${NET}]\"|" "$DOCKER_SURICATA_YAML"
  
  sed -i -E "s|(^\s*- interface:\s*).*$|\1${IFACE}|" "$DOCKER_SURICATA_YAML"
  echo "Updated $DOCKER_SURICATA_YAML -> HOME_NET=${NET}, interface=${IFACE}"
fi

if [ -f "$DOCKER_DOCKERFILE" ]; then
  sed -i -E "s|(^ENV\s+NETWORK_INTERFACE=).*$|\1${IFACE}|" "$DOCKER_DOCKERFILE"
  echo "Updated $DOCKER_DOCKERFILE -> NETWORK_INTERFACE=${IFACE}"
fi

sed -i -E "s|(\"?-i\"?,?[[:space:]]+\"?)[^\"[:space:],]+(\"?)|\1${IFACE}\2|g" "$SURICATA_DEPLOY_YAML"

echo "Done. Suricata config updated locally to interface=${IFACE}, HOME_NET=${NET}"
