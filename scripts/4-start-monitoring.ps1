# Script PowerShell - Démarrage Monitoring VM (16.170.74.58)
# Usage: .\scripts\4-start-monitoring.ps1

$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"
$MONITORING_IP = "16.170.74.58"
$MONITORING_USER = "ubuntu"

Write-Host "📊 Démarrage Monitoring VM ($MONITORING_IP)..." -ForegroundColor Cyan

# Vérifier que la clé SSH existe
if (-not (Test-Path $SSH_KEY)) {
    Write-Host "❌ Erreur: Clé SSH introuvable: $SSH_KEY" -ForegroundColor Red
    Write-Host "   Veuillez créer la clé SSH ou mettre à jour le chemin." -ForegroundColor Yellow
    exit 1
}

Write-Host "Connexion SSH à $MONITORING_USER@$MONITORING_IP..." -ForegroundColor Yellow

$bashCmd = 'bash -c "cd /opt/monitoring; echo INFO: Verification des fichiers de configuration...; if [ ! -f docker-compose.monitoring.yml ]; then echo ERROR: docker-compose.monitoring.yml non trouve; exit 1; fi; if [ ! -f docker-compose.loki.yml ]; then echo ERROR: docker-compose.loki.yml non trouve; exit 1; fi; echo OK: Fichiers de configuration trouves; echo; echo INFO: Demarrage Prometheus Grafana Alertmanager Node Exporter cAdvisor...; docker compose -f docker-compose.monitoring.yml up -d --force-recreate --remove-orphans; echo; echo INFO: Demarrage Loki et Promtail...; docker compose -f docker-compose.loki.yml up -d --force-recreate --remove-orphans; echo; echo INFO: Attente du demarrage 20 secondes...; sleep 20; echo; echo INFO: Statut de tous les conteneurs:; docker ps --format table | grep -E grafana|prometheus|loki|alertmanager|node-exporter|cadvisor; echo; echo INFO: Health Checks:; echo Grafana:; curl -s http://localhost:3000/api/health && echo OK || echo FAIL; echo Prometheus:; curl -s http://localhost:9090/-/healthy && echo OK || echo FAIL; echo Loki:; curl -s http://localhost:3100/ready && echo OK || echo FAIL; echo Alertmanager:; curl -s http://localhost:9093/-/healthy && echo OK || echo FAIL; echo; echo OK: Monitoring demarre!"'

ssh -i $SSH_KEY -o StrictHostKeyChecking=no $MONITORING_USER@$MONITORING_IP $bashCmd

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Monitoring démarré avec succès!" -ForegroundColor Green
    Write-Host "   Grafana: http://$MONITORING_IP:3000 (admin/admin)" -ForegroundColor Cyan
    Write-Host "   Prometheus: http://$MONITORING_IP:9090" -ForegroundColor Cyan
    Write-Host "   Loki: http://$MONITORING_IP:3100" -ForegroundColor Cyan
    Write-Host "   Alertmanager: http://$MONITORING_IP:9093" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "❌ Erreur lors du démarrage du Monitoring" -ForegroundColor Red
    exit 1
}
