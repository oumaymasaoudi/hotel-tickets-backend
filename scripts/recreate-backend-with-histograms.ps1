# Script pour recréer le backend avec les histogrammes activés
$BACKEND_IP = "13.63.15.86"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "Recreation du backend avec les histogrammes..." -ForegroundColor Yellow
Write-Host ""

# 1. Vérifier que docker-compose.yml est à jour
Write-Host "1. Verification du docker-compose.yml..." -ForegroundColor Cyan
$hasHistogram = ssh -i $SSH_KEY ubuntu@$BACKEND_IP "grep -q 'MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILES_HISTOGRAM' /opt/hotel-ticket-hub-backend-staging/docker-compose.yml && echo 'OK' || echo 'MANQUANT'"
if ($hasHistogram -match "MANQUANT") {
    Write-Host "   ERREUR docker-compose.yml n'a pas les variables d'histogramme" -ForegroundColor Red
    Write-Host "   Copie du fichier..." -ForegroundColor Yellow
    scp -i $SSH_KEY docker-compose.yml ubuntu@${BACKEND_IP}:/opt/hotel-ticket-hub-backend-staging/
    Write-Host "   OK Fichier copie" -ForegroundColor Green
} else {
    Write-Host "   OK docker-compose.yml contient les variables" -ForegroundColor Green
}
Write-Host ""

# 2. Recréer le conteneur (pas juste restart)
Write-Host "2. Recreation du conteneur backend..." -ForegroundColor Cyan
Write-Host "   (Cela va arreter et recreer le conteneur pour charger les nouvelles variables)" -ForegroundColor Yellow

$recreateCommand = @"
cd /opt/hotel-ticket-hub-backend-staging
docker compose stop backend
docker compose rm -f backend
docker compose up -d backend
echo "OK Conteneur recree"
"@

ssh -i $SSH_KEY ubuntu@$BACKEND_IP $recreateCommand

Write-Host ""

# 3. Attendre le démarrage
Write-Host "3. Attente du demarrage de l'application (60 secondes)..." -ForegroundColor Cyan
Write-Host "   (Spring Boot peut prendre 30-60 secondes pour demarrer)" -ForegroundColor Yellow
Start-Sleep -Seconds 60

Write-Host ""

# 4. Vérifier que l'application a démarré
Write-Host "4. Verification du demarrage..." -ForegroundColor Cyan
$healthCheck = curl.exe -s "http://$BACKEND_IP:8081/actuator/health" 2>$null
if ($healthCheck -match '"status":"UP"') {
    Write-Host "   OK Application demarree" -ForegroundColor Green
} else {
    Write-Host "   ATTENTION Application peut-etre encore en cours de demarrage" -ForegroundColor Yellow
    Write-Host "   Verifiez les logs: docker logs hotel-ticket-hub-backend-staging --tail 50" -ForegroundColor Gray
}
Write-Host ""

# 5. Vérifier les variables d'environnement
Write-Host "5. Verification des variables d'environnement..." -ForegroundColor Cyan
$envCheck = ssh -i $SSH_KEY ubuntu@$BACKEND_IP "docker exec hotel-ticket-hub-backend-staging env | grep -i 'MANAGEMENT_METRICS_DISTRIBUTION'"
if ($envCheck) {
    Write-Host "   OK Variables presentes:" -ForegroundColor Green
    $envCheck | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ERREUR Variables non presentes" -ForegroundColor Red
}
Write-Host ""

# 6. Vérifier les métriques histogram
Write-Host "6. Verification des metriques histogram..." -ForegroundColor Cyan
Start-Sleep -Seconds 10
$metrics = curl.exe -s "http://$BACKEND_IP:8081/actuator/prometheus" 2>$null | Select-String "http_server_requests_seconds_bucket" | Select-Object -First 3
if ($metrics) {
    Write-Host "   OK Les metriques histogram sont presentes!" -ForegroundColor Green
    Write-Host "   (Elles seront disponibles apres quelques requetes HTTP)" -ForegroundColor Cyan
} else {
    Write-Host "   ATTENTION Les metriques histogram ne sont pas encore presentes" -ForegroundColor Yellow
    Write-Host "   Cela est normal si aucune requete HTTP n'a ete faite depuis le redemarrage" -ForegroundColor Gray
}
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Prochaines etapes:" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Generer des requetes HTTP:" -ForegroundColor Yellow
Write-Host "   .\scripts\generate-http-metrics-fixed.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Verifier les metriques:" -ForegroundColor Yellow
Write-Host "   .\scripts\check-http-metrics-simple.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "3. Attendre 15-30 secondes puis rafraichir le dashboard Grafana" -ForegroundColor Yellow
Write-Host ""

