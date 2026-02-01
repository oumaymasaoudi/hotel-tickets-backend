# Script pour diagnostiquer pourquoi le backend est DOWN dans Prometheus
# Problème: "connection refused" sur http://13.63.15.86:8081/actuator/prometheus

$BACKEND_IP = "13.63.15.86"
$PROMETHEUS_IP = "16.170.74.58"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Diagnostic: Backend DOWN dans Prometheus" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Vérifier le statut du conteneur backend
Write-Host "1. Verification du conteneur backend..." -ForegroundColor Cyan
$containerStatus = ssh -i $SSH_KEY ubuntu@$BACKEND_IP "docker ps -a --filter name=hotel-ticket-hub-backend-staging --format '{{.Status}}'" 2>$null
if ($containerStatus -match "Up") {
    Write-Host "   ✓ Conteneur en cours d'execution" -ForegroundColor Green
    Write-Host "   Statut: $containerStatus" -ForegroundColor Gray
} else {
    Write-Host "   ✗ Conteneur ARRETE ou NON TROUVE" -ForegroundColor Red
    Write-Host "   Statut: $containerStatus" -ForegroundColor Gray
    Write-Host ""
    Write-Host "   SOLUTION: Redemarrer le backend" -ForegroundColor Yellow
    Write-Host "   ssh -i $SSH_KEY ubuntu@$BACKEND_IP 'cd /opt/hotel-ticket-hub-backend-staging && docker compose up -d backend'" -ForegroundColor Gray
}
Write-Host ""

# 2. Vérifier que l'endpoint est accessible localement sur la VM Backend
Write-Host "2. Test de l'endpoint local (sur la VM Backend)..." -ForegroundColor Cyan
$localTest = ssh -i $SSH_KEY ubuntu@$BACKEND_IP "curl -s -o /dev/null -w '%{http_code}' http://localhost:8081/actuator/health 2>&1" 2>$null
if ($localTest -eq "200") {
    Write-Host "   ✓ Endpoint accessible localement (HTTP $localTest)" -ForegroundColor Green
} else {
    Write-Host "   ✗ Endpoint NON accessible localement (HTTP $localTest)" -ForegroundColor Red
    Write-Host ""
    Write-Host "   SOLUTION: Verifier les logs du backend" -ForegroundColor Yellow
    Write-Host "   ssh -i $SSH_KEY ubuntu@$BACKEND_IP 'docker logs hotel-ticket-hub-backend-staging --tail 50'" -ForegroundColor Gray
}
Write-Host ""

# 3. Vérifier que l'endpoint Prometheus est accessible localement
Write-Host "3. Test de l'endpoint Prometheus local..." -ForegroundColor Cyan
$prometheusLocal = ssh -i $SSH_KEY ubuntu@$BACKEND_IP "curl -s -o /dev/null -w '%{http_code}' http://localhost:8081/actuator/prometheus 2>&1" 2>$null
if ($prometheusLocal -eq "200") {
    Write-Host "   ✓ Endpoint /actuator/prometheus accessible (HTTP $prometheusLocal)" -ForegroundColor Green
} else {
    Write-Host "   ✗ Endpoint /actuator/prometheus NON accessible (HTTP $prometheusLocal)" -ForegroundColor Red
    Write-Host ""
    Write-Host "   SOLUTION: Verifier la configuration Actuator" -ForegroundColor Yellow
    Write-Host "   ssh -i $SSH_KEY ubuntu@$BACKEND_IP 'docker exec hotel-ticket-hub-backend-staging env | grep MANAGEMENT'" -ForegroundColor Gray
}
Write-Host ""

# 4. Vérifier la connectivité depuis la VM Prometheus
Write-Host "4. Test de connectivite depuis la VM Prometheus..." -ForegroundColor Cyan
$prometheusTest = ssh -i $SSH_KEY ubuntu@$PROMETHEUS_IP "curl -s -o /dev/null -w '%{http_code}' --connect-timeout 5 http://$BACKEND_IP:8081/actuator/prometheus 2>&1" 2>$null
if ($prometheusTest -eq "200") {
    Write-Host "   ✓ Connexion depuis Prometheus reussie (HTTP $prometheusTest)" -ForegroundColor Green
} else {
    Write-Host "   ✗ Connexion depuis Prometheus ECHOUE (HTTP $prometheusTest)" -ForegroundColor Red
    Write-Host ""
    Write-Host "   CAUSE PROBABLE: Security Group AWS bloque le port 8081" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "   SOLUTION: Ouvrir le port 8081 dans le Security Group" -ForegroundColor Yellow
    Write-Host "   1. AWS Console -> EC2 -> Security Groups" -ForegroundColor Gray
    Write-Host "   2. Trouvez le Security Group de la VM Backend ($BACKEND_IP)" -ForegroundColor Gray
    Write-Host "   3. Inbound Rules -> Edit inbound rules -> Add rule" -ForegroundColor Gray
    Write-Host "      Type: Custom TCP" -ForegroundColor Gray
    Write-Host "      Port: 8081" -ForegroundColor Gray
    Write-Host "      Source: $PROMETHEUS_IP/32 (ou Security Group de la VM Prometheus)" -ForegroundColor Gray
    Write-Host "      Description: Allow Prometheus scraping from monitoring VM" -ForegroundColor Gray
    Write-Host "   4. Save rules" -ForegroundColor Gray
}
Write-Host ""

# 5. Vérifier les logs récents pour les erreurs
Write-Host "5. Verification des logs recents (erreurs)..." -ForegroundColor Cyan
$recentErrors = ssh -i $SSH_KEY ubuntu@$BACKEND_IP "docker logs hotel-ticket-hub-backend-staging --tail 50 2>&1 | grep -iE 'error|exception|failed|refused' | tail -5" 2>$null
if ($recentErrors) {
    Write-Host "   ⚠ Erreurs detectees dans les logs:" -ForegroundColor Yellow
    Write-Host "   $recentErrors" -ForegroundColor Gray
} else {
    Write-Host "   ✓ Aucune erreur recente detectee" -ForegroundColor Green
}
Write-Host ""

# 6. Vérifier que le port 8081 est bien exposé
Write-Host "6. Verification du port 8081..." -ForegroundColor Cyan
$portCheck = ssh -i $SSH_KEY ubuntu@$BACKEND_IP "sudo netstat -tlnp 2>/dev/null | grep 8081 || sudo ss -tlnp 2>/dev/null | grep 8081" 2>$null
if ($portCheck) {
    Write-Host "   ✓ Port 8081 en ecoute" -ForegroundColor Green
    Write-Host "   $portCheck" -ForegroundColor Gray
} else {
    Write-Host "   ✗ Port 8081 NON en ecoute" -ForegroundColor Red
    Write-Host ""
    Write-Host "   SOLUTION: Verifier le mapping de port dans docker-compose.yml" -ForegroundColor Yellow
}
Write-Host ""

# Résumé et actions recommandées
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "RESUME ET ACTIONS RECOMMANDEES" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

if ($containerStatus -notmatch "Up") {
    Write-Host "1. [CRITIQUE] Redemarrer le conteneur backend" -ForegroundColor Red
    Write-Host "   ssh -i $SSH_KEY ubuntu@$BACKEND_IP 'cd /opt/hotel-ticket-hub-backend-staging && docker compose up -d backend'" -ForegroundColor Gray
    Write-Host ""
}

if ($prometheusTest -ne "200") {
    Write-Host "2. [CRITIQUE] Ouvrir le port 8081 dans le Security Group AWS" -ForegroundColor Red
    Write-Host "   Voir les instructions ci-dessus (section 4)" -ForegroundColor Gray
    Write-Host ""
}

if ($localTest -eq "200" -and $prometheusTest -eq "200") {
    Write-Host "✓ Tout semble OK. Le backend devrait etre UP dans Prometheus dans quelques secondes." -ForegroundColor Green
    Write-Host "  Rafraichissez la page Prometheus: http://$PROMETHEUS_IP:9090/targets" -ForegroundColor Cyan
} else {
    Write-Host "3. Apres correction, verifier dans Prometheus:" -ForegroundColor Yellow
    Write-Host "   http://$PROMETHEUS_IP:9090/targets" -ForegroundColor Gray
    Write-Host ""
    Write-Host "4. Pour redemarrer le backend:" -ForegroundColor Yellow
    Write-Host "   ssh -i $SSH_KEY ubuntu@$BACKEND_IP 'cd /opt/hotel-ticket-hub-backend-staging && docker compose restart backend'" -ForegroundColor Gray
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan

