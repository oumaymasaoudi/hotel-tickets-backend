# Script pour vérifier si les métriques HTTP sont disponibles dans Prometheus
# À exécuter depuis votre machine locale

$MONITORING_IP = "16.170.74.58"

Write-Host "Vérification des métriques HTTP dans Prometheus..." -ForegroundColor Yellow
Write-Host ""

# 1. Vérifier http_server_requests_seconds_count
Write-Host "1. Vérification de http_server_requests_seconds_count..." -ForegroundColor Cyan
$response = Invoke-RestMethod -Uri "http://$MONITORING_IP:9090/api/v1/query?query=http_server_requests_seconds_count{job=`"staging-backend`"}" -Method Get
$count = $response.data.result.Count
Write-Host "   Résultats trouvés: $count" -ForegroundColor $(if ($count -gt 0) { "Green" } else { "Red" })

if ($count -gt 0) {
    Write-Host "   Premiers résultats:" -ForegroundColor Cyan
    $response.data.result | Select-Object -First 3 | ForEach-Object {
        Write-Host "     - $($_.metric.status): $($_.value[1])" -ForegroundColor Gray
    }
}
Write-Host ""

# 2. Vérifier http_server_requests_seconds_bucket (pour histogram)
Write-Host "2. Vérification de http_server_requests_seconds_bucket..." -ForegroundColor Cyan
$response = Invoke-RestMethod -Uri "http://$MONITORING_IP:9090/api/v1/query?query=http_server_requests_seconds_bucket{job=`"staging-backend`"}" -Method Get
$count = $response.data.result.Count
Write-Host "   Résultats trouvés: $count" -ForegroundColor $(if ($count -gt 0) { "Green" } else { "Red" })

if ($count -gt 0) {
    Write-Host "   ✓ Les métriques histogram sont disponibles" -ForegroundColor Green
    Write-Host "   Les panneaux Response Time et Error Rate devraient fonctionner" -ForegroundColor Cyan
}
else {
    Write-Host "   ✗ Les métriques histogram ne sont pas disponibles" -ForegroundColor Red
    Write-Host "   Cela peut être dû à:" -ForegroundColor Yellow
    Write-Host "     - Pas assez de requêtes HTTP générées" -ForegroundColor Gray
    Write-Host "     - Les métriques histogram ne sont pas activées dans Spring Boot" -ForegroundColor Gray
}
Write-Host ""

# 3. Vérifier les métriques disponibles
Write-Host "3. Toutes les métriques HTTP disponibles:" -ForegroundColor Cyan
$response = Invoke-RestMethod -Uri "http://$MONITORING_IP:9090/api/v1/series?match[]={job=`"staging-backend`"}" -Method Get
$httpMetrics = $response.data | Where-Object { $_.__name__ -like "*http*" } | ForEach-Object { $_.__name__ } | Sort-Object -Unique
Write-Host "   Métriques trouvées:" -ForegroundColor Cyan
$httpMetrics | ForEach-Object {
    Write-Host "     - $_" -ForegroundColor Gray
}
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Pour voir les métriques dans Prometheus UI:" -ForegroundColor Yellow
Write-Host "  http://$MONITORING_IP:9090/graph" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

