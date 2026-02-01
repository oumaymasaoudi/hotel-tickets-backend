# Script pour vérifier et corriger le dashboard
# À exécuter depuis votre machine locale

$MONITORING_IP = "16.170.74.58"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "Verification et correction du dashboard..." -ForegroundColor Yellow
Write-Host ""

# 1. Vérifier le contenu du dashboard sur la VM
Write-Host "1. Verification du dashboard sur la VM..." -ForegroundColor Cyan
$dashboardContent = ssh -i $SSH_KEY ubuntu@$MONITORING_IP "sudo cat /opt/monitoring/grafana/dashboards/backend-spring-boot.json | grep -A 2 'JVM Memory Usage' | grep -A 1 'expr'"

if ($dashboardContent -match "avg\(") {
    Write-Host "   ✓ La correction est presente dans le fichier" -ForegroundColor Green
} else {
    Write-Host "   ✗ La correction n'est pas presente" -ForegroundColor Red
    Write-Host "   Re-deploiement necessaire..." -ForegroundColor Yellow
    
    # Re-déployer
    scp -i $SSH_KEY monitoring/grafana/dashboards/backend-spring-boot.json ubuntu@${MONITORING_IP}:/tmp/
    ssh -i $SSH_KEY ubuntu@$MONITORING_IP "sudo mv /tmp/backend-spring-boot.json /opt/monitoring/grafana/dashboards/; sudo chown root:root /opt/monitoring/grafana/dashboards/backend-spring-boot.json; sudo chmod 644 /opt/monitoring/grafana/dashboards/backend-spring-boot.json"
}

Write-Host ""

# 2. Redémarrer Grafana pour forcer le rechargement
Write-Host "2. Redemarrage de Grafana..." -ForegroundColor Cyan
ssh -i $SSH_KEY ubuntu@$MONITORING_IP "cd /opt/monitoring; docker compose -f docker-compose.monitoring.yml restart grafana"

Write-Host "   ✓ Grafana redemarre" -ForegroundColor Green
Write-Host ""

# 3. Attendre
Write-Host "3. Attente du demarrage (15 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Actions a effectuer dans Grafana:" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Ouvrez: http://$MONITORING_IP:3000" -ForegroundColor Cyan
Write-Host "2. Allez dans le dashboard 'Hotel Ticket Hub - Backend Spring Boot'" -ForegroundColor Cyan
Write-Host "3. Cliquez sur le menu (3 points) en haut a droite" -ForegroundColor Cyan
Write-Host "4. Selectionnez 'Settings'" -ForegroundColor Cyan
Write-Host "5. Cliquez sur 'Save dashboard' (meme sans modification)" -ForegroundColor Cyan
Write-Host "6. Ou supprimez et re-importez le dashboard" -ForegroundColor Cyan
Write-Host ""
Write-Host "Cela forcera Grafana a recharger le dashboard depuis le fichier" -ForegroundColor Yellow
Write-Host ""

