# Script simplifié pour vérifier les métriques HTTP
$MONITORING_IP = "16.170.74.58"

Write-Host "Verification des metriques HTTP dans Prometheus..." -ForegroundColor Yellow
Write-Host ""

# 1. Vérifier http_server_requests_seconds_count
Write-Host "1. Verification de http_server_requests_seconds_count..." -ForegroundColor Cyan
try {
    $uri = "http://$MONITORING_IP" + ":9090/api/v1/query?query=http_server_requests_seconds_count{job=`"staging-backend`"}"
    $response = Invoke-RestMethod -Uri $uri -Method Get
    $count = $response.data.result.Count
    Write-Host "   Resultats trouves: $count" -ForegroundColor $(if ($count -gt 0) { "Green" } else { "Red" })
    
    if ($count -gt 0) {
        Write-Host "   Premiers resultats:" -ForegroundColor Cyan
        $response.data.result | Select-Object -First 3 | ForEach-Object {
            Write-Host "     - $($_.metric.status): $($_.value[1])" -ForegroundColor Gray
        }
    }
}
catch {
    Write-Host "   Erreur: $_" -ForegroundColor Red
}
Write-Host ""

# 2. Vérifier http_server_requests_seconds_bucket
Write-Host "2. Verification de http_server_requests_seconds_bucket..." -ForegroundColor Cyan
try {
    $uri = "http://$MONITORING_IP" + ":9090/api/v1/query?query=http_server_requests_seconds_bucket{job=`"staging-backend`"}"
    $response = Invoke-RestMethod -Uri $uri -Method Get
    $count = $response.data.result.Count
    Write-Host "   Resultats trouves: $count" -ForegroundColor $(if ($count -gt 0) { "Green" } else { "Red" })
    
    if ($count -gt 0) {
        Write-Host "   OK Les metriques histogram sont disponibles" -ForegroundColor Green
        Write-Host "   Les panneaux Response Time et Error Rate devraient fonctionner" -ForegroundColor Cyan
    }
    else {
        Write-Host "   Les metriques histogram ne sont pas disponibles" -ForegroundColor Red
        Write-Host "   Cela peut etre du a:" -ForegroundColor Yellow
        Write-Host "     - Pas assez de requetes HTTP generees" -ForegroundColor Gray
        Write-Host "     - Les metriques histogram ne sont pas activees dans Spring Boot" -ForegroundColor Gray
    }
}
catch {
    Write-Host "   Erreur: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "Pour voir les metriques dans Prometheus UI:" -ForegroundColor Yellow
Write-Host "  http://$MONITORING_IP:9090/graph" -ForegroundColor Cyan
Write-Host ""

