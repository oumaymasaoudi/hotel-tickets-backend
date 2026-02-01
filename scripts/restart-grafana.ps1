# Redémarrer Grafana pour charger les nouveaux dashboards

$MONITORING_IP = "16.170.74.58"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "Redémarrage de Grafana..." -ForegroundColor Yellow
ssh -i $SSH_KEY ubuntu@$MONITORING_IP "cd /opt/monitoring && docker compose -f docker-compose.monitoring.yml restart grafana"

Write-Host ""
Write-Host "Attente du démarrage (10 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "✓ Grafana redémarré !" -ForegroundColor Green
Write-Host ""
Write-Host "Vérifiez les dashboards:" -ForegroundColor Cyan
Write-Host "  - Grafana: http://$MONITORING_IP:3000" -ForegroundColor Yellow
Write-Host "  - Login: admin / admin" -ForegroundColor Yellow
Write-Host "  - Menu → Dashboards → Browse" -ForegroundColor Yellow
Write-Host ""

