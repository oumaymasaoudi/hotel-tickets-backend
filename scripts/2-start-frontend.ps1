# Script PowerShell - Démarrage Frontend VM (13.50.221.51)
# Usage: .\scripts\2-start-frontend.ps1

$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"
$FRONTEND_IP = "13.50.221.51"
$FRONTEND_USER = "ubuntu"

Write-Host "🌐 Démarrage Frontend VM ($FRONTEND_IP)..." -ForegroundColor Cyan

# Vérifier que la clé SSH existe
if (-not (Test-Path $SSH_KEY)) {
    Write-Host "❌ Erreur: Clé SSH introuvable: $SSH_KEY" -ForegroundColor Red
    Write-Host "   Veuillez créer la clé SSH ou mettre à jour le chemin." -ForegroundColor Yellow
    exit 1
}

Write-Host "Connexion SSH à $FRONTEND_USER@$FRONTEND_IP..." -ForegroundColor Yellow

$bashCmd = 'bash -c "cd ~/hotel-ticket-hub || cd /opt/hotel-ticket-hub; echo INFO: Demarrage du conteneur Frontend...; docker compose up -d --force-recreate --remove-orphans; echo; echo INFO: Attente du demarrage 10 secondes...; sleep 10; echo; echo INFO: Statut des conteneurs:; docker ps --filter name=hotel-ticket-hub-frontend; echo; echo INFO: Test de connexion:; curl -s -o /dev/null -w HTTP_Status:_%{http_code} http://localhost:80 || echo WARN: Frontend non accessible encore; echo; echo OK: Frontend demarre!"'

ssh -i $SSH_KEY -o StrictHostKeyChecking=no $FRONTEND_USER@$FRONTEND_IP $bashCmd

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Frontend démarré avec succès!" -ForegroundColor Green
    Write-Host "   URL: http://$FRONTEND_IP" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "❌ Erreur lors du démarrage du Frontend" -ForegroundColor Red
    exit 1
}
