# Script final pour activer les histogrammes
$BACKEND_IP = "13.63.15.86"

Write-Host "Activation des histogrammes - etape finale" -ForegroundColor Yellow
Write-Host ""

# 1. Vérifier que l'application est démarrée
Write-Host "1. Verification du demarrage de l'application..." -ForegroundColor Cyan
$health = curl.exe -s "http://$BACKEND_IP:8081/actuator/health" 2>$null
if ($health -match '"status":"UP"') {
    Write-Host "   OK Application demarree" -ForegroundColor Green
} else {
    Write-Host "   ATTENTION Application peut-etre encore en cours de demarrage" -ForegroundColor Yellow
    Write-Host "   Attente de 30 secondes supplementaires..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
}
Write-Host ""

# 2. Générer des requêtes HTTP
Write-Host "2. Generation de requetes HTTP (50 requetes)..." -ForegroundColor Cyan
$endpoints = @("/actuator/health", "/actuator/info", "/actuator/prometheus")
$successCount = 0

for ($i=1; $i -le 50; $i++) {
    $endpoint = $endpoints[$i % $endpoints.Length]
    try {
        $response = curl.exe -s "http://$BACKEND_IP:8081$endpoint" 2>$null
        if ($response) {
            $successCount++
        }
        if ($i % 10 -eq 0) {
            Write-Host "   $i requetes generees..." -ForegroundColor Gray
        }
    } catch {
        # Ignore errors
    }
    Start-Sleep -Milliseconds 200
}

Write-Host "   OK $successCount requetes generees" -ForegroundColor Green
Write-Host ""

# 3. Attendre que Prometheus scrape
Write-Host "3. Attente du scraping Prometheus (20 secondes)..." -ForegroundColor Cyan
Start-Sleep -Seconds 20
Write-Host ""

# 4. Vérifier les métriques histogram
Write-Host "4. Verification des metriques histogram..." -ForegroundColor Cyan
$MONITORING_IP = "16.170.74.58"
$uri = "http://$MONITORING_IP" + ":9090/api/v1/query?query=http_server_requests_seconds_bucket{job=`"staging-backend`"}"
try {
    $response = Invoke-RestMethod -Uri $uri -Method Get
    $count = $response.data.result.Count
    if ($count -gt 0) {
        Write-Host "   OK Les metriques histogram sont disponibles! ($count resultats)" -ForegroundColor Green
        Write-Host ""
        Write-Host "   Les panneaux Response Time et Error Rate devraient maintenant fonctionner" -ForegroundColor Cyan
    } else {
        Write-Host "   ATTENTION Les metriques histogram ne sont pas encore disponibles" -ForegroundColor Yellow
        Write-Host "   Attendez encore 15-30 secondes et rafraichissez le dashboard Grafana" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ERREUR Impossible de verifier les metriques: $_" -ForegroundColor Red
}
Write-Host ""

# 5. Vérifier dans l'endpoint Prometheus du backend
Write-Host "5. Verification dans l'endpoint Prometheus du backend..." -ForegroundColor Cyan
$prometheusMetrics = curl.exe -s "http://$BACKEND_IP:8081/actuator/prometheus" 2>$null | Select-String "http_server_requests_seconds_bucket" | Select-Object -First 3
if ($prometheusMetrics) {
    Write-Host "   OK Les metriques histogram sont presentes dans l'endpoint" -ForegroundColor Green
} else {
    Write-Host "   ATTENTION Les metriques histogram ne sont pas encore dans l'endpoint" -ForegroundColor Yellow
    Write-Host "   Cela peut prendre quelques secondes apres les requetes HTTP" -ForegroundColor Gray
}
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Prochaines etapes:" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Attendez 15-30 secondes" -ForegroundColor Yellow
Write-Host "2. Rafraichissez le dashboard Grafana" -ForegroundColor Yellow
Write-Host "3. Les panneaux Response Time et Error Rate devraient afficher des donnees" -ForegroundColor Yellow
Write-Host ""
Write-Host "Dashboard Grafana: http://16.170.74.58:3000" -ForegroundColor Cyan
Write-Host ""

