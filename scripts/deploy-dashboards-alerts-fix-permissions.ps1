# Script PowerShell pour déployer les dashboards avec correction des permissions
# À exécuter depuis votre machine locale Windows

$MONITORING_IP = "16.170.74.58"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Déploiement des Dashboards (avec fix permissions)" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Copier les dashboards dans /tmp (accessible)
Write-Host "1. Copie des dashboards dans /tmp..." -ForegroundColor Yellow
scp -i $SSH_KEY `
    monitoring/grafana/dashboards/backend-spring-boot.json `
    monitoring/grafana/dashboards/system-overview.json `
    ubuntu@${MONITORING_IP}:/tmp/

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Dashboards copiés dans /tmp" -ForegroundColor Green
} else {
    Write-Host "✗ Erreur lors de la copie" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 2. Déplacer les fichiers avec sudo et corriger les permissions
Write-Host "2. Déplacement des dashboards et correction des permissions..." -ForegroundColor Yellow
$moveCommand = @"
sudo mv /tmp/backend-spring-boot.json /opt/monitoring/grafana/dashboards/
sudo mv /tmp/system-overview.json /opt/monitoring/grafana/dashboards/
sudo chown root:root /opt/monitoring/grafana/dashboards/*.json
sudo chmod 644 /opt/monitoring/grafana/dashboards/*.json
echo "✓ Dashboards déplacés et permissions corrigées"
"@

ssh -i $SSH_KEY ubuntu@$MONITORING_IP $moveCommand

Write-Host ""

# 3. Vérifier que les fichiers sont bien là
Write-Host "3. Vérification des fichiers..." -ForegroundColor Yellow
ssh -i $SSH_KEY ubuntu@$MONITORING_IP "ls -lh /opt/monitoring/grafana/dashboards/*.json"

Write-Host ""

# 4. Redémarrer Grafana
Write-Host "4. Redémarrage de Grafana..." -ForegroundColor Yellow
$restartCommand = @"
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml restart grafana
echo "✓ Grafana redémarré"
"@

ssh -i $SSH_KEY ubuntu@$MONITORING_IP $restartCommand

Write-Host ""

# 5. Attendre le démarrage
Write-Host "5. Attente du démarrage (10 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Déploiement terminé !" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Vérifiez les dashboards:" -ForegroundColor Yellow
Write-Host "  - Grafana: http://$MONITORING_IP:3000" -ForegroundColor Cyan
Write-Host ""

