# Vérifier que les histogrammes sont activés dans le backend
$BACKEND_IP = "13.63.15.86"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "Verification de la configuration des histogrammes..." -ForegroundColor Yellow
Write-Host ""

# 1. Vérifier les variables d'environnement dans le conteneur
Write-Host "1. Variables d'environnement dans le conteneur:" -ForegroundColor Cyan
ssh -i $SSH_KEY ubuntu@$BACKEND_IP "docker exec hotel-ticket-hub-backend-staging env | grep -i 'MANAGEMENT_METRICS_DISTRIBUTION'"

Write-Host ""

# 2. Vérifier les logs du backend
Write-Host "2. Dernieres lignes des logs (recherche de 'histogram' ou 'distribution'):" -ForegroundColor Cyan
ssh -i $SSH_KEY ubuntu@$BACKEND_IP "docker logs hotel-ticket-hub-backend-staging --tail 50 | grep -iE 'histogram|distribution|metrics' || echo 'Aucune mention trouvee'"

Write-Host ""

# 3. Vérifier l'endpoint Prometheus
Write-Host "3. Verification de l'endpoint Prometheus (recherche de 'http_server_requests_seconds_bucket'):" -ForegroundColor Cyan
$metrics = curl.exe -s "http://$BACKEND_IP:8081/actuator/prometheus" | Select-String "http_server_requests_seconds_bucket" | Select-Object -First 5
if ($metrics) {
    Write-Host "   OK Les metriques histogram sont presentes dans l'endpoint" -ForegroundColor Green
    $metrics | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ERREUR Les metriques histogram ne sont pas presentes" -ForegroundColor Red
    Write-Host "   Les variables d'environnement ne sont peut-etre pas appliquees" -ForegroundColor Yellow
}

Write-Host ""

