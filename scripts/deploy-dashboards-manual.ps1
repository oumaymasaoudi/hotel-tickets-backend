# Commandes manuelles pour déployer les dashboards
# Copiez-collez ces commandes une par une dans PowerShell

$MONITORING_IP = "16.170.74.58"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

# 1. Copier dans /tmp
Write-Host "1. Copie des dashboards dans /tmp..." -ForegroundColor Yellow
scp -i $SSH_KEY `
    monitoring/grafana/dashboards/backend-spring-boot.json `
    monitoring/grafana/dashboards/system-overview.json `
    ubuntu@${MONITORING_IP}:/tmp/

# 2. Déplacer avec sudo (commande sur une seule ligne)
Write-Host "2. Déplacement des dashboards..." -ForegroundColor Yellow
ssh -i $SSH_KEY ubuntu@$MONITORING_IP "sudo mv /tmp/backend-spring-boot.json /opt/monitoring/grafana/dashboards/ && sudo mv /tmp/system-overview.json /opt/monitoring/grafana/dashboards/ && sudo chown root:root /opt/monitoring/grafana/dashboards/*.json && sudo chmod 644 /opt/monitoring/grafana/dashboards/*.json"

# 3. Vérifier
Write-Host "3. Vérification..." -ForegroundColor Yellow
ssh -i $SSH_KEY ubuntu@$MONITORING_IP "ls -lh /opt/monitoring/grafana/dashboards/*.json"

# 4. Redémarrer Grafana
Write-Host "4. Redémarrage de Grafana..." -ForegroundColor Yellow
ssh -i $SSH_KEY ubuntu@$MONITORING_IP "cd /opt/monitoring && docker compose -f docker-compose.monitoring.yml restart grafana"

Write-Host ""
Write-Host "✓ Terminé ! Vérifiez: http://$MONITORING_IP:3000" -ForegroundColor Green

