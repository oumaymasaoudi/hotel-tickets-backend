#!/bin/bash

# Script pour déployer les dashboards Grafana et les alertes Prometheus
# À exécuter depuis votre machine locale (pas sur la VM)

set -e

MONITORING_IP="16.170.74.58"
SSH_KEY="${HOME}/.ssh/oumayma-key.pem"
MONITORING_DIR="/opt/monitoring"

# Couleurs pour les messages
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "Déploiement des Dashboards et Alertes"
echo "=========================================="
echo ""

# Vérifier que les fichiers existent
if [ ! -f "monitoring/grafana/dashboards/backend-spring-boot.json" ]; then
    echo -e "${RED}✗ Fichier backend-spring-boot.json non trouvé${NC}"
    exit 1
fi

if [ ! -f "monitoring/grafana/dashboards/system-overview.json" ]; then
    echo -e "${RED}✗ Fichier system-overview.json non trouvé${NC}"
    exit 1
fi

if [ ! -f "monitoring/prometheus/rules/alerts.yml" ]; then
    echo -e "${RED}✗ Fichier alerts.yml non trouvé${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Fichiers trouvés${NC}"
echo ""

# 1. Copier les dashboards
echo "1. Copie des dashboards Grafana..."
scp -i "$SSH_KEY" \
    monitoring/grafana/dashboards/backend-spring-boot.json \
    monitoring/grafana/dashboards/system-overview.json \
    ubuntu@$MONITORING_IP:$MONITORING_DIR/grafana/dashboards/

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Dashboards copiés${NC}"
else
    echo -e "${RED}✗ Erreur lors de la copie des dashboards${NC}"
    exit 1
fi
echo ""

# 2. Copier les règles d'alerte
echo "2. Copie des règles d'alerte Prometheus..."
scp -i "$SSH_KEY" \
    monitoring/prometheus/rules/alerts.yml \
    ubuntu@$MONITORING_IP:$MONITORING_DIR/prometheus/rules/alerts.yml

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Règles d'alerte copiées${NC}"
else
    echo -e "${RED}✗ Erreur lors de la copie des règles d'alerte${NC}"
    exit 1
fi
echo ""

# 3. Redémarrer les services
echo "3. Redémarrage des services..."
ssh -i "$SSH_KEY" ubuntu@$MONITORING_IP << 'EOF'
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml restart prometheus grafana
echo "Services redémarrés"
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Services redémarrés${NC}"
else
    echo -e "${RED}✗ Erreur lors du redémarrage${NC}"
    exit 1
fi
echo ""

# 4. Attendre que les services démarrent
echo "4. Attente du démarrage des services (15 secondes)..."
sleep 15
echo ""

# 5. Vérifier que les services sont démarrés
echo "5. Vérification des services..."
ssh -i "$SSH_KEY" ubuntu@$MONITORING_IP << 'EOF'
echo "Statut des conteneurs:"
docker ps --filter "name=prometheus" --filter "name=grafana" --format "table {{.Names}}\t{{.Status}}"
EOF

echo ""
echo "=========================================="
echo "Déploiement terminé !"
echo "=========================================="
echo ""
echo "Vérifiez les dashboards:"
echo "  - Grafana: http://$MONITORING_IP:3000"
echo "  - Prometheus Alerts: http://$MONITORING_IP:9090/alerts"
echo ""
echo "Dashboards disponibles:"
echo "  - Hotel Ticket Hub - Backend Spring Boot"
echo "  - Hotel Ticket Hub - System Overview"
echo ""

