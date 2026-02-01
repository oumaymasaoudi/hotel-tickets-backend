# Script pour générer des requêtes HTTP et créer des métriques
# À exécuter depuis votre machine locale

$BACKEND_URL = "http://13.63.15.86:8081"

Write-Host "Génération de requêtes HTTP pour créer des métriques..." -ForegroundColor Yellow
Write-Host ""

for ($i = 1; $i -le 20; $i++) {
    try {
        # Utiliser Invoke-WebRequest au lieu de curl
        $response = Invoke-WebRequest -Uri "$BACKEND_URL/actuator/health" -UseBasicParsing -TimeoutSec 5
        Write-Host "Requête $i : $($response.StatusCode)" -ForegroundColor Green
    }
    catch {
        Write-Host "Requête $i : Erreur - $($_.Exception.Message)" -ForegroundColor Red
    }
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "✓ 20 requêtes générées" -ForegroundColor Green
Write-Host ""
Write-Host "Attendez 15-30 secondes puis vérifiez les dashboards Grafana" -ForegroundColor Cyan
Write-Host "Les métriques Response Time et Error Rate devraient apparaître" -ForegroundColor Cyan
Write-Host ""

