# Script pour vérifier et redémarrer le backend
$BACKEND_IP = "13.63.15.86"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "Verification et redemarrage du backend..." -ForegroundColor Yellow
Write-Host ""

# 1. Vérifier le statut du conteneur
Write-Host "1. Statut du conteneur..." -ForegroundColor Cyan
$containerStatus = ssh -i $SSH_KEY ubuntu@$BACKEND_IP "docker ps -a --filter name=hotel-ticket-hub-backend-staging --format '{{.Status}}'"
Write-Host "   Statut: $containerStatus" -ForegroundColor $(if ($containerStatus -match "Up") { "Green" } else { "Red" })
Write-Host ""

# 2. Vérifier les logs récents
Write-Host "2. Dernieres lignes des logs (recherche d'erreurs)..." -ForegroundColor Cyan
ssh -i $SSH_KEY ubuntu@$BACKEND_IP "docker logs hotel-ticket-hub-backend-staging --tail 30 2>&1 | tail -20"
Write-Host ""

# 3. Vérifier la connexion à la base de données
Write-Host "3. Verification de la connexion a la base de donnees..." -ForegroundColor Cyan
$dbCheck = ssh -i $SSH_KEY ubuntu@$BACKEND_IP "nc -zv 13.48.83.147 5432 2>&1"
if ($dbCheck -match "succeeded") {
    Write-Host "   OK Connexion DB reussie" -ForegroundColor Green
} else {
    Write-Host "   ERREUR Connexion DB echouee" -ForegroundColor Red
    Write-Host "   $dbCheck" -ForegroundColor Gray
}
Write-Host ""

# 4. Redémarrer le backend
Write-Host "4. Redemarrage du backend..." -ForegroundColor Cyan
ssh -i $SSH_KEY ubuntu@$BACKEND_IP "cd /opt/hotel-ticket-hub-backend-staging && docker compose restart backend"
Write-Host "   OK Backend redemarre" -ForegroundColor Green
Write-Host ""

# 5. Attendre le démarrage
Write-Host "5. Attente du demarrage (45 secondes)..." -ForegroundColor Cyan
Start-Sleep -Seconds 45
Write-Host ""

# 6. Vérifier que l'application a démarré
Write-Host "6. Verification du demarrage..." -ForegroundColor Cyan
$health = curl.exe -s "http://$BACKEND_IP:8081/actuator/health" 2>$null
if ($health -match '"status":"UP"') {
    Write-Host "   OK Application demarree" -ForegroundColor Green
    Write-Host "   $health" -ForegroundColor Gray
} else {
    Write-Host "   ERREUR Application n'a pas demarre" -ForegroundColor Red
    Write-Host "   Verifiez les logs: docker logs hotel-ticket-hub-backend-staging --tail 50" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "   Logs recents:" -ForegroundColor Yellow
    ssh -i $SSH_KEY ubuntu@$BACKEND_IP "docker logs hotel-ticket-hub-backend-staging --tail 50 2>&1 | grep -iE 'error|exception|failed|started' | tail -10"
}
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Si l'application est UP:" -ForegroundColor Yellow
Write-Host "  - Rafraichissez le dashboard Grafana" -ForegroundColor Cyan
Write-Host "  - L'Application Status devrait passer a UP" -ForegroundColor Cyan
Write-Host ""
Write-Host "Si l'application est toujours DOWN:" -ForegroundColor Yellow
Write-Host "  - Verifiez les logs: docker logs hotel-ticket-hub-backend-staging --tail 100" -ForegroundColor Cyan
Write-Host "  - Verifiez la connexion DB: nc -zv 13.48.83.147 5432" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

