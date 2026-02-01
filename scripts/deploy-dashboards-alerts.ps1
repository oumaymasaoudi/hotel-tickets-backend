# Script PowerShell pour déployer les dashboards Grafana et les alertes Prometheus
# À exécuter depuis votre machine locale Windows

$MONITORING_IP = "16.170.74.58"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"
$MONITORING_DIR = "/opt/monitoring"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Déploiement des Dashboards et Alertes" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que les fichiers existent
if (-not (Test-Path "monitoring\grafana\dashboards\backend-spring-boot.json")) {
    Write-Host "✗ Fichier backend-spring-boot.json non trouvé" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path "monitoring\grafana\dashboards\system-overview.json")) {
    Write-Host "✗ Fichier system-overview.json non trouvé" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path "monitoring\prometheus\rules\alerts.yml")) {
    Write-Host "✗ Fichier alerts.yml non trouvé" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Fichiers trouvés" -ForegroundColor Green
Write-Host ""

# 1. Copier les dashboards
Write-Host "1. Copie des dashboards Grafana..." -ForegroundColor Yellow
scp -i $SSH_KEY `
    monitoring/grafana/dashboards/backend-spring-boot.json `
    monitoring/grafana/dashboards/system-overview.json `
    ubuntu@${MONITORING_IP}:${MONITORING_DIR}/grafana/dashboards/

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Dashboards copiés" -ForegroundColor Green
}
else {
    Write-Host "✗ Erreur lors de la copie des dashboards" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 2. Copier les règles d'alerte
Write-Host "2. Copie des règles d'alerte Prometheus..." -ForegroundColor Yellow
scp -i $SSH_KEY `
    monitoring/prometheus/rules/alerts.yml `
    ubuntu@${MONITORING_IP}:${MONITORING_DIR}/prometheus/rules/alerts.yml

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Règles d'alerte copiées" -ForegroundColor Green
}
else {
    Write-Host "✗ Erreur lors de la copie des règles d'alerte" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 3. Redémarrer les services
Write-Host "3. Redémarrage des services..." -ForegroundColor Yellow
$restartCommand = @"
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml restart prometheus grafana
echo "Services redémarrés"
"@

ssh -i $SSH_KEY ubuntu@$MONITORING_IP $restartCommand

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Services redémarrés" -ForegroundColor Green
}
else {
    Write-Host "✗ Erreur lors du redémarrage" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 4. Attendre que les services démarrent
Write-Host "4. Attente du démarrage des services (15 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15
Write-Host ""

# 5. Vérifier que les services sont démarrés
Write-Host "5. Vérification des services..." -ForegroundColor Yellow
$statusCommand = @"
echo "Statut des conteneurs:"
docker ps --filter "name=prometheus" --filter "name=grafana" --format "table {{.Names}}\t{{.Status}}"
"@

ssh -i $SSH_KEY ubuntu@$MONITORING_IP $statusCommand

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Déploiement terminé !" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Vérifiez les dashboards:" -ForegroundColor Yellow
Write-Host "  - Grafana: http://$MONITORING_IP:3000" -ForegroundColor Cyan
Write-Host "  - Prometheus Alerts: http://$MONITORING_IP:9090/alerts" -ForegroundColor Cyan
Write-Host ""
Write-Host "Dashboards disponibles:" -ForegroundColor Yellow
Write-Host "  - Hotel Ticket Hub - Backend Spring Boot" -ForegroundColor Green
Write-Host "  - Hotel Ticket Hub - System Overview" -ForegroundColor Green
Write-Host ""

