# Vérifier le contenu du dashboard sur la VM
$MONITORING_IP = "16.170.74.58"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "Verification du dashboard sur la VM..." -ForegroundColor Yellow
Write-Host ""

# Vérifier la ligne avec la requête JVM Memory
$result = ssh -i $SSH_KEY ubuntu@$MONITORING_IP "sudo grep -A 2 'JVM Memory Usage' /opt/monitoring/grafana/dashboards/backend-spring-boot.json | grep 'expr'"

if ($result -match "avg") {
    Write-Host "OK La correction est presente dans le fichier sur la VM" -ForegroundColor Green
    Write-Host "  Requete: $result" -ForegroundColor Gray
}
else {
    Write-Host "ERREUR La correction n'est pas presente" -ForegroundColor Red
    Write-Host ""
    Write-Host "Re-deploiement necessaire..." -ForegroundColor Yellow
    
    # Re-déployer
    scp -i $SSH_KEY monitoring/grafana/dashboards/backend-spring-boot.json ubuntu@${MONITORING_IP}:/tmp/
    ssh -i $SSH_KEY ubuntu@$MONITORING_IP "sudo mv /tmp/backend-spring-boot.json /opt/monitoring/grafana/dashboards/; sudo chown root:root /opt/monitoring/grafana/dashboards/backend-spring-boot.json; sudo chmod 644 /opt/monitoring/grafana/dashboards/backend-spring-boot.json"
    
    Write-Host "OK Dashboard re-deploye" -ForegroundColor Green
    Write-Host ""
    Write-Host "Redemarrage de Grafana..." -ForegroundColor Yellow
    ssh -i $SSH_KEY ubuntu@$MONITORING_IP "cd /opt/monitoring; docker compose -f docker-compose.monitoring.yml restart grafana"
}

Write-Host ""

