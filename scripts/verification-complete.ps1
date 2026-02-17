# Script PowerShell - Vérification Complète de Toutes les VMs
# Usage: .\scripts\verification-complete.ps1

$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"
$BACKEND_IP = "13.63.15.86"
$FRONTEND_IP = "13.50.221.51"
$DATABASE_IP = "13.48.83.147"
$MONITORING_IP = "16.170.74.58"
$SSH_USER = "ubuntu"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  VERIFICATION COMPLETE - Toutes les VMs" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

if (-not (Test-Path $SSH_KEY)) {
    Write-Host "❌ Erreur: Clé SSH introuvable: $SSH_KEY" -ForegroundColor Red
    exit 1
}

# ============================================
# 1. VM BACKEND (13.63.15.86)
# ============================================
Write-Host "`n=== VM BACKEND ($BACKEND_IP) ===" -ForegroundColor Blue
Write-Host "========================================`n" -ForegroundColor Blue

$backendCheck = @'
echo '=== Conteneurs Docker ==='
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
echo ''
echo '=== Health Check Backend ==='
curl -s http://localhost:8081/actuator/health || echo 'ERREUR: Backend non accessible'
echo ''
echo '=== Métriques Prometheus ==='
curl -s http://localhost:8081/actuator/prometheus | head -5 || echo 'ERREUR: Métriques non accessibles'
echo ''
echo '=== Logs Backend (dernières 10 lignes) ==='
docker logs hotel-ticket-hub-backend-staging --tail 10 2>&1 || echo 'ERREUR: Logs non accessibles'
'@

ssh -i $SSH_KEY -o StrictHostKeyChecking=no $SSH_USER@$BACKEND_IP $backendCheck

# ============================================
# 2. VM FRONTEND (13.50.221.51)
# ============================================
Write-Host "`n=== VM FRONTEND ($FRONTEND_IP) ===" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green

$frontendCheck = @'
echo '=== Conteneurs Docker ==='
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
echo ''
echo '=== Test Frontend ==='
curl -s -o /dev/null -w 'HTTP Status: %{http_code}\n' http://localhost:80 || echo 'ERREUR: Frontend non accessible'
echo ''
echo '=== Logs Frontend (dernières 10 lignes) ==='
docker logs hotel-ticket-hub-frontend-staging --tail 10 2>&1 || echo 'ERREUR: Logs non accessibles'
'@

ssh -i $SSH_KEY -o StrictHostKeyChecking=no $SSH_USER@$FRONTEND_IP $frontendCheck 2>&1

# ============================================
# 3. VM DATABASE (13.48.83.147)
# ============================================
Write-Host "`n=== VM DATABASE ($DATABASE_IP) ===" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

$databaseCheck = @'
echo '=== Statut PostgreSQL ==='
sudo systemctl status postgresql --no-pager | head -10 || echo 'ERREUR: PostgreSQL non accessible'
echo ''
echo '=== Connexion à la base de données ==='
sudo -u postgres psql -d hotel_ticket_hub -c '\dt' 2>&1 | head -30 || echo 'ERREUR: Connexion à la base impossible'
echo ''
echo '=== Liste des tables ==='
sudo -u postgres psql -d hotel_ticket_hub -c "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;" 2>&1 || echo 'ERREUR: Impossible de lister les tables'
echo ''
echo '=== Nombre de lignes par table ==='
sudo -u postgres psql -d hotel_ticket_hub -c "
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size,
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = schemaname AND table_name = tablename) AS columns
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY tablename;
" 2>&1 || echo 'ERREUR: Impossible de compter les lignes'
'@

ssh -i $SSH_KEY -o StrictHostKeyChecking=no $SSH_USER@$DATABASE_IP $databaseCheck

# ============================================
# 4. VM MONITORING (16.170.74.58)
# ============================================
Write-Host "`n=== VM MONITORING ($MONITORING_IP) ===" -ForegroundColor Red
Write-Host "========================================`n" -ForegroundColor Red

$monitoringCheck = @'
echo '=== Conteneurs Docker ==='
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
echo ''
echo '=== Health Check Grafana ==='
curl -s http://localhost:3000/api/health || echo 'ERREUR: Grafana non accessible'
echo ''
echo '=== Health Check Prometheus ==='
curl -s http://localhost:9090/-/healthy || echo 'ERREUR: Prometheus non accessible'
echo ''
echo '=== Health Check Loki ==='
curl -s http://localhost:3100/ready || echo 'ERREUR: Loki non accessible'
echo ''
echo '=== Labels Loki ==='
curl -s http://localhost:3100/loki/api/v1/labels | head -5 || echo 'ERREUR: API Loki non accessible'
echo ''
echo '=== Health Check Alertmanager ==='
curl -s http://localhost:9093/-/healthy || echo 'ERREUR: Alertmanager non accessible'
echo ''
echo '=== Targets Prometheus ==='
curl -s http://localhost:9090/api/v1/targets | grep -o '"health":"[^"]*"' | head -10 || echo 'ERREUR: API Prometheus non accessible'
echo ''
echo '=== Logs Grafana (dernières 5 lignes) ==='
docker logs grafana --tail 5 2>&1 || echo 'ERREUR: Logs Grafana non accessibles'
echo ''
echo '=== Logs Prometheus (dernières 5 lignes) ==='
docker logs prometheus --tail 5 2>&1 || echo 'ERREUR: Logs Prometheus non accessibles'
echo ''
echo '=== Logs Loki (dernières 5 lignes) ==='
docker logs loki --tail 5 2>&1 || echo 'ERREUR: Logs Loki non accessibles'
'@

ssh -i $SSH_KEY -o StrictHostKeyChecking=no $SSH_USER@$MONITORING_IP $monitoringCheck

# ============================================
# RÉSUMÉ FINAL
# ============================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  RÉSUMÉ DE LA VÉRIFICATION" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "OK: Verification terminee!" -ForegroundColor Green
Write-Host "`nURLs de monitoring:" -ForegroundColor Yellow
Write-Host "   Grafana:    http://$MONITORING_IP:3000 (admin/admin)" -ForegroundColor White
Write-Host "   Prometheus: http://$MONITORING_IP:9090" -ForegroundColor White
Write-Host "   Loki:       http://$MONITORING_IP:3100" -ForegroundColor White
Write-Host "`nURLs Application:" -ForegroundColor Yellow
Write-Host "   Frontend:   http://$FRONTEND_IP" -ForegroundColor White
Write-Host "   Backend:    http://$BACKEND_IP:8081/api" -ForegroundColor White
Write-Host "   Swagger:    http://$BACKEND_IP:8081/swagger-ui.html" -ForegroundColor White
