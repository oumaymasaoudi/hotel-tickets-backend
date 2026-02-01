#!/bin/bash

# Script pour déployer les dashboards avec correction des permissions
# À exécuter depuis votre machine locale

set -e

MONITORING_IP="16.170.74.58"
SSH_KEY="${HOME}/.ssh/oumayma-key.pem"

echo "=========================================="
echo "Déploiement des Dashboards (avec fix permissions)"
echo "=========================================="
echo ""

# 1. Copier les dashboards dans /tmp (accessible)
echo "1. Copie des dashboards dans /tmp..."
scp -i "$SSH_KEY" \
    monitoring/grafana/dashboards/backend-spring-boot.json \
    monitoring/grafana/dashboards/system-overview.json \
    ubuntu@$MONITORING_IP:/tmp/

if [ $? -eq 0 ]; then
    echo "✓ Dashboards copiés dans /tmp"
else
    echo "✗ Erreur lors de la copie"
    exit 1
fi
echo ""

# 2. Déplacer les fichiers avec sudo et corriger les permissions
echo "2. Déplacement des dashboards et correction des permissions..."
ssh -i "$SSH_KEY" ubuntu@$MONITORING_IP << 'EOF'
sudo mv /tmp/backend-spring-boot.json /opt/monitoring/grafana/dashboards/
sudo mv /tmp/system-overview.json /opt/monitoring/grafana/dashboards/
sudo chown root:root /opt/monitoring/grafana/dashboards/*.json
sudo chmod 644 /opt/monitoring/grafana/dashboards/*.json
echo "✓ Dashboards déplacés et permissions corrigées"
EOF

echo ""

# 3. Vérifier que les fichiers sont bien là
echo "3. Vérification des fichiers..."
ssh -i "$SSH_KEY" ubuntu@$MONITORING_IP << 'EOF'
ls -lh /opt/monitoring/grafana/dashboards/*.json
EOF

echo ""

# 4. Redémarrer Grafana
echo "4. Redémarrage de Grafana..."
ssh -i "$SSH_KEY" ubuntu@$MONITORING_IP << 'EOF'
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml restart grafana
echo "✓ Grafana redémarré"
EOF

echo ""

# 5. Attendre le démarrage
echo "5. Attente du démarrage (10 secondes)..."
sleep 10

echo ""
echo "=========================================="
echo "Déploiement terminé !"
echo "=========================================="
echo ""
echo "Vérifiez les dashboards:"
echo "  - Grafana: http://$MONITORING_IP:3000"
echo ""

