#!/bin/bash
# Script de déploiement du monitoring stack
# À exécuter sur la VM de monitoring

set -euo pipefail
cd /opt/monitoring

# ⭐ CRITICAL: Resize filesystem if EBS volume was expanded
echo "Checking and resizing filesystem if needed..."
df -h / | tail -1

REAL_DEVICE=$(findmnt -n -o SOURCE / | head -1)
if [ -z "$REAL_DEVICE" ] || [ "$REAL_DEVICE" = "/dev/root" ]; then
  REAL_DEVICE=$(lsblk -n -o NAME,MOUNTPOINT | grep -E '\s/$' | awk '{print "/dev/"$1}' | head -1)
fi

if [ -z "$REAL_DEVICE" ] || [ "$REAL_DEVICE" = "/dev/root" ]; then
  for dev in /dev/xvda1 /dev/nvme0n1p1 /dev/sda1; do
    if [ -b "$dev" ]; then
      REAL_DEVICE="$dev"
      break
    fi
  done
fi

if [ -n "$REAL_DEVICE" ] && [ "$REAL_DEVICE" != "/dev/root" ]; then
  echo "Real block device: $REAL_DEVICE"
  if echo "$REAL_DEVICE" | grep -q "nvme"; then
    BLOCK_DEVICE=$(echo "$REAL_DEVICE" | sed 's/p[0-9]*$//')
    PARTITION_NUM=$(echo "$REAL_DEVICE" | grep -o 'p[0-9]*$' | sed 's/p//')
  else
    BLOCK_DEVICE=$(echo "$REAL_DEVICE" | sed 's/[0-9]*$//')
    PARTITION_NUM=$(echo "$REAL_DEVICE" | grep -o '[0-9]*$')
  fi
  [ -z "$PARTITION_NUM" ] && PARTITION_NUM="1"
  
  if command -v growpart &> /dev/null; then
    sudo growpart "$BLOCK_DEVICE" "$PARTITION_NUM" || true
    FSTYPE=$(findmnt -n -o FSTYPE / || echo "")
    if [ "$FSTYPE" = "xfs" ]; then
      sudo xfs_growfs / || true
    elif [ -n "$FSTYPE" ]; then
      sudo resize2fs "$REAL_DEVICE" || true
    else
      sudo xfs_growfs / 2>/dev/null || sudo resize2fs "$REAL_DEVICE" || true
    fi
    df -h / | tail -1
  fi
fi

# Verify config files
if [ ! -f "docker-compose.monitoring.yml" ] || [ ! -f "prometheus/prometheus.yml" ]; then
  echo "ERROR: Required configuration files not found!"
  exit 1
fi

# Disk cleanup
df -h / | tail -1
DISK_USAGE_INITIAL=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')

docker ps --format "{{.Names}}" | grep -v -E "(grafana|prometheus|loki|alertmanager|node-exporter|cadvisor|promtail)" | xargs -r docker stop 2>/dev/null || true
docker ps -a --format "{{.Names}}" | grep -v -E "(grafana|prometheus|loki|alertmanager|node-exporter|cadvisor|promtail)" | xargs -r docker rm 2>/dev/null || true
docker image prune -f || true

if [ "$DISK_USAGE_INITIAL" -gt 90 ]; then
  docker image prune -af || true
  docker builder prune -af || true
  find /var/lib/docker/containers/ -type f -name "*.log" -delete 2>/dev/null || true
  journalctl --vacuum-time=1h 2>/dev/null || true
else
  docker image prune -af --filter "until=24h" || true
  docker builder prune -af --filter "until=24h" || true
  find /var/lib/docker/containers/ -type f -name "*.log" -mtime +1 -delete 2>/dev/null || true
  journalctl --vacuum-time=1d 2>/dev/null || true
fi

docker network ls --format "{{.Name}}" | grep -v -E "(monitoring-network|bridge|host)" | xargs -r docker network rm 2>/dev/null || true

if [ "$DISK_USAGE_INITIAL" -gt 90 ]; then
  docker system prune -af || true
else
  docker system prune -af --filter "until=24h" || true
fi

DISK_USAGE=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')
if [ "$DISK_USAGE" -gt 95 ]; then
  MONITORING_CONTAINERS=$(docker ps --format "{{.Names}}" | grep -E "(grafana|prometheus|loki|alertmanager|node-exporter|cadvisor|promtail)" | tr '\n' ' ' || true)
  if [ -n "$MONITORING_CONTAINERS" ]; then
    docker stop $MONITORING_CONTAINERS 2>/dev/null || true
    sleep 2
    docker image prune -af || true
    docker system prune -af || true
    DISK_USAGE=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')
  fi
  if [ "$DISK_USAGE" -gt 95 ]; then
    echo "❌ ERROR: Disk usage still above 95%"
    exit 1
  fi
fi

DISK_USAGE=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')
if [ "$DISK_USAGE" -gt 95 ]; then
  echo "ERROR: Disk usage too high, cannot pull images"
  exit 1
fi

# Docker Compose
docker compose version || {
  docker-compose version || exit 1
  COMPOSE_CMD="docker-compose"
}
COMPOSE_CMD="${COMPOSE_CMD:-docker compose}"

# Remove old images
MONITORING_IMAGES=$(docker images --format "{{.Repository}}:{{.Tag}}" | grep -E "(grafana|prometheus|alertmanager|node-exporter|cadvisor|promtail)" || true)
[ -n "$MONITORING_IMAGES" ] && echo "$MONITORING_IMAGES" | xargs -r docker rmi -f 2>/dev/null || true
docker image prune -f || true

DISK_USAGE=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')
if [ "$DISK_USAGE" -gt 90 ]; then
  docker image prune -af || true
  docker volume ls --format "{{.Name}}" | grep -v -E "(grafana|prometheus|loki|alertmanager)" | xargs -r docker volume rm 2>/dev/null || true
  DISK_USAGE=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')
fi

if [ "$DISK_USAGE" -gt 92 ]; then
  echo "❌ ERROR: Disk usage too high to safely pull images"
  exit 1
fi

# Pull images
for IMAGE in "prom/node-exporter:latest" "gcr.io/cadvisor/cadvisor:latest" "prom/alertmanager:latest" "prom/prometheus:latest" "grafana/grafana:latest"; do
  echo "Pulling $IMAGE..."
  docker pull "$IMAGE" || {
    DISK_USAGE=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')
    if [ "$DISK_USAGE" -gt 90 ]; then
      docker image prune -af || true
      docker volume ls --format "{{.Name}}" | grep -v -E "(grafana|prometheus|loki|alertmanager)" | xargs -r docker volume rm 2>/dev/null || true
      DISK_USAGE=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')
      [ "$DISK_USAGE" -gt 92 ] && exit 1
    fi
    docker pull "$IMAGE" || exit 1
  }
  DISK_USAGE=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')
  echo "✓ Pulled $IMAGE (disk: ${DISK_USAGE}%)"
  [ "$DISK_USAGE" -gt 90 ] && docker image prune -f || true
done

# Start services
set +e
$COMPOSE_CMD -f docker-compose.monitoring.yml up -d --force-recreate --remove-orphans --pull never
COMPOSE_EXIT_CODE=$?
set -e

if [ $COMPOSE_EXIT_CODE -ne 0 ]; then
  if docker network ls | grep -q monitoring-network; then
    CONTAINER_IDS=$(docker ps --filter network=monitoring-network -q 2>/dev/null || true)
    [ -n "$CONTAINER_IDS" ] && echo "$CONTAINER_IDS" | xargs docker stop 2>/dev/null || true
    [ -n "$CONTAINER_IDS" ] && echo "$CONTAINER_IDS" | xargs docker rm -f 2>/dev/null || true
    OLD_CONTAINER_IDS=$(docker ps -a --filter network=monitoring-network -q 2>/dev/null || true)
    [ -n "$OLD_CONTAINER_IDS" ] && echo "$OLD_CONTAINER_IDS" | xargs docker rm -f 2>/dev/null || true
    docker network rm monitoring-network 2>/dev/null || docker network prune -f
    sleep 3
    $COMPOSE_CMD -f docker-compose.monitoring.yml up -d --force-recreate --remove-orphans --pull never
  else
    $COMPOSE_CMD -f docker-compose.monitoring.yml logs
    exit 1
  fi
fi

sleep 30
$COMPOSE_CMD -f docker-compose.monitoring.yml ps

# Verify services
# Vérifier Prometheus
if ! docker ps | grep -q prometheus; then
  echo "❌ ERROR: Prometheus container not running"
  $COMPOSE_CMD -f docker-compose.monitoring.yml logs prometheus | tail -30
  exit 1
fi
echo "✅ Prometheus is running"

# Vérifier Grafana avec une logique plus robuste
GRAFANA_CONTAINER=$(docker ps --filter "name=grafana" --format "{{.Names}}" | head -1)
if [ -z "$GRAFANA_CONTAINER" ]; then
  echo "❌ ERROR: Grafana container not found"
  docker ps -a | grep grafana || echo "No grafana container found"
  $COMPOSE_CMD -f docker-compose.monitoring.yml logs grafana | tail -50
  exit 1
fi

echo "✅ Grafana container found: $GRAFANA_CONTAINER"

# Attendre que Grafana soit prêt (health check)
GRAFANA_STATUS=$(docker ps --filter "name=grafana" --format "{{.Status}}")
if echo "$GRAFANA_STATUS" | grep -q "health: starting"; then
  echo "⏳ Waiting for Grafana to be healthy (max 60s)..."
  for i in {1..12}; do
    sleep 5
    GRAFANA_STATUS=$(docker ps --filter "name=grafana" --format "{{.Status}}" 2>/dev/null || echo "")
    if echo "$GRAFANA_STATUS" | grep -q "healthy"; then
      echo "✅ Grafana is healthy"
      break
    fi
    if [ $i -eq 12 ]; then
      echo "⚠️  WARNING: Grafana health check timeout, but container is running"
      docker ps --filter "name=grafana"
    fi
  done
fi

# Vérifier que Grafana est toujours en cours d'exécution
if ! docker ps --filter "name=grafana" --format "{{.Names}}" | grep -q grafana; then
  echo "❌ ERROR: Grafana container stopped"
  $COMPOSE_CMD -f docker-compose.monitoring.yml logs grafana | tail -50
  exit 1
fi

echo "✅ Grafana is running"

# Vérifier les autres services (non-critiques)
for service in alertmanager node-exporter cadvisor; do
  if docker ps | grep -q "$service"; then
    echo "✅ $service is running"
  else
    echo "⚠️  WARNING: $service not running (non-critical)"
  fi
done

echo ""
echo "=========================================="
echo "✅ Monitoring stack deployed successfully!"
echo "=========================================="
echo ""
echo "Services available:"
echo "  - Prometheus: http://16.170.74.58:9090"
echo "  - Grafana: http://16.170.74.58:3000 (admin/admin)"
echo "  - Alertmanager: http://16.170.74.58:9093"
echo "  - cAdvisor: http://16.170.74.58:8080"
echo ""
