# Script pour générer des requêtes HTTP et créer des métriques
# Version corrigée

$BACKEND_URL = "http://13.63.15.86:8081"

Write-Host "Generation de requetes HTTP pour creer des metriques..." -ForegroundColor Yellow
Write-Host ""

$endpoints = @("/actuator/health", "/actuator/info", "/actuator/prometheus")

for ($i=1; $i -le 50; $i++) {
    $endpoint = $endpoints[$i % $endpoints.Length]
    try {
        curl.exe -s "$BACKEND_URL$endpoint" | Out-Null
        if ($i % 10 -eq 0) {
            Write-Host "  $i requetes generees..." -ForegroundColor Gray
        }
    } catch {
        Write-Host "  Erreur sur la requete $i" -ForegroundColor Red
    }
    Start-Sleep -Milliseconds 200
}

Write-Host ""
Write-Host "OK 50 requetes generees" -ForegroundColor Green
Write-Host ""
Write-Host "Attendez 15-30 secondes puis verifiez les metriques:" -ForegroundColor Cyan
Write-Host "  .\scripts\check-http-metrics-simple.ps1" -ForegroundColor Yellow
Write-Host ""

