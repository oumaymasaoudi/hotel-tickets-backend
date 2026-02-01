# Script pour vérifier les noms de métriques corrects
# Vérifie que les buckets HTTP et les métriques HikariCP existent

$BACKEND_IP = "13.63.15.86"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Verification des metriques Prometheus" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Vérifier les buckets HTTP (pour Response Time p95)
Write-Host "1. Verification des buckets HTTP (http_server_requests_seconds_bucket)..." -ForegroundColor Cyan
Write-Host "   Test: curl -s http://$BACKEND_IP:8081/actuator/prometheus | grep http_server_requests_seconds_bucket | head -n 5" -ForegroundColor Gray
Write-Host ""

$buckets = curl.exe -s "http://$BACKEND_IP:8081/actuator/prometheus" 2>&1 | Select-String "http_server_requests_seconds_bucket" | Select-Object -First 5

if ($buckets) {
    Write-Host "   ✓ Buckets HTTP trouves (histogramme active)" -ForegroundColor Green
    Write-Host "   Premiers buckets:" -ForegroundColor Gray
    $buckets | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    Write-Host ""
    Write-Host "   ✓ Le panel 'Response Time (95th percentile)' devrait fonctionner" -ForegroundColor Green
} else {
    Write-Host "   ✗ Aucun bucket HTTP trouve" -ForegroundColor Red
    Write-Host ""
    Write-Host "   SOLUTION:" -ForegroundColor Yellow
    Write-Host "   1. Verifiez que la propriete est activee dans docker-compose.yml:" -ForegroundColor Gray
    Write-Host "      MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILES_HISTOGRAM_HTTP_SERVER_REQUESTS=true" -ForegroundColor Gray
    Write-Host "   2. Redemarrez le backend:" -ForegroundColor Gray
    Write-Host "      ssh -i $SSH_KEY ubuntu@$BACKEND_IP 'cd /opt/hotel-ticket-hub-backend-staging && docker compose restart backend'" -ForegroundColor Gray
    Write-Host "   3. Attendez 30-45 secondes puis generez du trafic:" -ForegroundColor Gray
    Write-Host "      for (`$i=1; `$i -le 50; `$i++) { curl.exe -s http://$BACKEND_IP:8081/actuator/health | Out-Null; Start-Sleep -Milliseconds 200 }" -ForegroundColor Gray
}
Write-Host ""

# 2. Vérifier les métriques HikariCP (pour Database Connections)
Write-Host "2. Verification des metriques HikariCP (hikaricp_connections_*)..." -ForegroundColor Cyan
Write-Host "   Test: curl -s http://$BACKEND_IP:8081/actuator/prometheus | grep hikaricp" -ForegroundColor Gray
Write-Host ""

$hikaricp = curl.exe -s "http://$BACKEND_IP:8081/actuator/prometheus" 2>&1 | Select-String "hikaricp" | Select-Object -First 5

if ($hikaricp) {
    Write-Host "   ✓ Metriques HikariCP trouvees" -ForegroundColor Green
    Write-Host "   Metriques trouvees:" -ForegroundColor Gray
    $hikaricp | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    Write-Host ""
    
    # Vérifier les métriques spécifiques
    $active = curl.exe -s "http://$BACKEND_IP:8081/actuator/prometheus" 2>&1 | Select-String "hikaricp_connections_active"
    $max = curl.exe -s "http://$BACKEND_IP:8081/actuator/prometheus" 2>&1 | Select-String "hikaricp_connections_max"
    $idle = curl.exe -s "http://$BACKEND_IP:8081/actuator/prometheus" 2>&1 | Select-String "hikaricp_connections_idle"
    
    Write-Host "   Verification des metriques specifiques:" -ForegroundColor Cyan
    if ($active) { Write-Host "     ✓ hikaricp_connections_active" -ForegroundColor Green } else { Write-Host "     ✗ hikaricp_connections_active (manquant)" -ForegroundColor Red }
    if ($max) { Write-Host "     ✓ hikaricp_connections_max" -ForegroundColor Green } else { Write-Host "     ✗ hikaricp_connections_max (manquant)" -ForegroundColor Red }
    if ($idle) { Write-Host "     ✓ hikaricp_connections_idle" -ForegroundColor Green } else { Write-Host "     ✗ hikaricp_connections_idle (manquant)" -ForegroundColor Red }
    Write-Host ""
    Write-Host "   ✓ Le panel 'Database Connections' devrait fonctionner" -ForegroundColor Green
} else {
    Write-Host "   ✗ Aucune metrique HikariCP trouvee" -ForegroundColor Red
    Write-Host ""
    Write-Host "   NOTE: Les metriques HikariCP peuvent ne pas etre disponibles si:" -ForegroundColor Yellow
    Write-Host "   - Le pool de connexions n'a pas encore ete utilise" -ForegroundColor Gray
    Write-Host "   - HikariCP n'est pas configure correctement" -ForegroundColor Gray
    Write-Host "   - L'application n'a pas encore fait de requetes DB" -ForegroundColor Gray
}
Write-Host ""

# 3. Vérifier les métriques HTTP count (pour Error Rate)
Write-Host "3. Verification des metriques HTTP count (http_server_requests_seconds_count)..." -ForegroundColor Cyan
$httpCount = curl.exe -s "http://$BACKEND_IP:8081/actuator/prometheus" 2>&1 | Select-String "http_server_requests_seconds_count" | Select-Object -First 3

if ($httpCount) {
    Write-Host "   ✓ Metriques HTTP count trouvees" -ForegroundColor Green
    Write-Host "   Le panel 'Error Rate %' devrait fonctionner" -ForegroundColor Green
} else {
    Write-Host "   ⚠ Aucune metrique HTTP count trouvee (pas de trafic recent)" -ForegroundColor Yellow
    Write-Host "   Generez du trafic pour voir les metriques" -ForegroundColor Gray
}
Write-Host ""

# Résumé
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "RESUME" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

if ($buckets) {
    Write-Host "✓ Response Time (p95): OK - Les buckets sont disponibles" -ForegroundColor Green
} else {
    Write-Host "✗ Response Time (p95): KO - Les buckets ne sont pas disponibles" -ForegroundColor Red
    Write-Host "  → Activez les histogrammes et redemarrez le backend" -ForegroundColor Yellow
}

if ($hikaricp) {
    Write-Host "✓ Database Connections: OK - Les metriques HikariCP sont disponibles" -ForegroundColor Green
} else {
    Write-Host "⚠ Database Connections: Les metriques peuvent ne pas etre disponibles encore" -ForegroundColor Yellow
    Write-Host "  → Normal si pas de requetes DB recentes" -ForegroundColor Gray
}

if ($httpCount) {
    Write-Host "✓ Error Rate: OK - Les metriques HTTP sont disponibles" -ForegroundColor Green
} else {
    Write-Host "⚠ Error Rate: Pas de trafic recent" -ForegroundColor Yellow
    Write-Host "  → Generez du trafic pour voir les metriques" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Pour generer du trafic:" -ForegroundColor Yellow
Write-Host "  for (`$i=1; `$i -le 50; `$i++) { curl.exe -s http://$BACKEND_IP:8081/actuator/health | Out-Null; Start-Sleep -Milliseconds 200 }" -ForegroundColor Cyan
Write-Host ""

