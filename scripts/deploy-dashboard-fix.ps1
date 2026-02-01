# Script pour déployer la correction du dashboard JVM Memory Usage
# À exécuter depuis votre machine locale

$MONITORING_IP = "16.170.74.58"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "Déploiement de la correction du dashboard..." -ForegroundColor Yellow
Write-Host ""

# 1. Copier le dashboard corrigé
Write-Host "1. Copie du dashboard corrigé..." -ForegroundColor Cyan
scp -i $SSH_KEY `
    monitoring/grafana/dashboards/backend-spring-boot.json `
    ubuntu@${MONITORING_IP}:/tmp/

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Dashboard copié" -ForegroundColor Green
}
else {
    Write-Host "   ✗ Erreur lors de la copie" -ForegroundColor Red
    exit 1
}

# 2. Déplacer avec sudo
Write-Host "2. Déplacement du dashboard..." -ForegroundColor Cyan
$moveCommand = "sudo mv /tmp/backend-spring-boot.json /opt/monitoring/grafana/dashboards/; sudo chown root:root /opt/monitoring/grafana/dashboards/backend-spring-boot.json; sudo chmod 644 /opt/monitoring/grafana/dashboards/backend-spring-boot.json"
ssh -i $SSH_KEY ubuntu@$MONITORING_IP $moveCommand

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Dashboard déplacé" -ForegroundColor Green
}
else {
    Write-Host "   ✗ Erreur lors du déplacement" -ForegroundColor Red
    exit 1
}

# 3. Redémarrer Grafana
Write-Host "3. Redémarrage de Grafana..." -ForegroundColor Cyan
$restartCommand = "cd /opt/monitoring; docker compose -f docker-compose.monitoring.yml restart grafana"
ssh -i $SSH_KEY ubuntu@$MONITORING_IP $restartCommand

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Grafana redémarré" -ForegroundColor Green
}
else {
    Write-Host "   ✗ Erreur lors du redémarrage" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Attente du demarrage (10 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "✓ Correction déployée !" -ForegroundColor Green
Write-Host ""
Write-Host "Rafraîchissez le dashboard Grafana:" -ForegroundColor Cyan
Write-Host "  http://$MONITORING_IP:3000" -ForegroundColor Yellow
Write-Host ""
Write-Host "Le gauge JVM Memory Usage % devrait maintenant afficher une valeur correcte" -ForegroundColor Cyan
Write-Host ""

