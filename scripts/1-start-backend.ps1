# Script PowerShell - Démarrage Backend VM (13.63.15.86)
# Usage: .\scripts\1-start-backend.ps1

$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"
$BACKEND_IP = "13.63.15.86"
$BACKEND_USER = "ubuntu"

Write-Host "🚀 Démarrage Backend VM ($BACKEND_IP)..." -ForegroundColor Cyan

# Vérifier que la clé SSH existe
if (-not (Test-Path $SSH_KEY)) {
    Write-Host "❌ Erreur: Clé SSH introuvable: $SSH_KEY" -ForegroundColor Red
    Write-Host "   Veuillez créer la clé SSH ou mettre à jour le chemin." -ForegroundColor Yellow
    exit 1
}

Write-Host "Connexion SSH à $BACKEND_USER@$BACKEND_IP..." -ForegroundColor Yellow

# Utiliser ssh avec une commande bash simple
$bashCmd = 'bash -c "cd ~/hotel-ticket-hub-backend || cd /opt/hotel-ticket-hub-backend; echo INFO: Verification du fichier .env...; if [ ! -f .env ]; then echo ERROR: Fichier .env non trouve; exit 1; fi; echo OK: Fichier .env trouve; echo; echo INFO: Demarrage des conteneurs Docker...; docker compose up -d --force-recreate --remove-orphans; echo; echo INFO: Attente du demarrage 10 secondes...; sleep 10; echo; echo INFO: Statut des conteneurs:; docker ps --filter name=hotel-ticket-hub-backend; echo; echo INFO: Health Check:; curl -s http://localhost:8081/actuator/health || echo WARN: Health check non disponible encore; echo; echo OK: Backend demarre!"'

ssh -i $SSH_KEY -o StrictHostKeyChecking=no $BACKEND_USER@$BACKEND_IP $bashCmd

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Backend démarré avec succès!" -ForegroundColor Green
    Write-Host "   URL: http://13.63.15.86:8081/api" -ForegroundColor Cyan
    Write-Host "   Health: http://13.63.15.86:8081/actuator/health" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "❌ Erreur lors du démarrage du Backend" -ForegroundColor Red
    exit 1
}
