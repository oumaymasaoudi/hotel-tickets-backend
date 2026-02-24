# Script PowerShell pour exécuter les corrections sur les VMs
# Usage: .\scripts\execute-vm-fixes.ps1

$ErrorActionPreference = "Stop"

# Configuration
$DB_VM = "13.48.83.147"
$BACKEND_VM = "13.63.15.86"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"
$SSH_USER = "ubuntu"
$PROJECT_DIR = "~/hotel-ticket-hub-backend"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Correction complète des VMs" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Fonction pour exécuter une commande sur une VM
function Execute-OnVM {
    param(
        [string]$VM,
        [string]$Command
    )
    Write-Host "Exécution sur $VM : $Command" -ForegroundColor Yellow
    ssh -i $SSH_KEY -o StrictHostKeyChecking=no "$SSH_USER@$VM" $Command
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Erreur lors de l'exécution sur $VM" -ForegroundColor Red
        exit 1
    }
}

# Étape 1 : Corriger la base de données
Write-Host "📊 Étape 1 : Correction de la base de données sur $DB_VM" -ForegroundColor Green
Write-Host "---------------------------------------------------" -ForegroundColor Gray

Execute-OnVM -VM $DB_VM -Command "cd $PROJECT_DIR; git pull origin main"
Execute-OnVM -VM $DB_VM -Command "cd $PROJECT_DIR; chmod +x scripts/fix-vm-database-issues.sh; ./scripts/fix-vm-database-issues.sh"

Write-Host ""
Write-Host "✅ Base de données corrigée" -ForegroundColor Green
Write-Host ""

# Étape 2 : Redéployer le backend
Write-Host "🚀 Étape 2 : Redéploiement du backend sur $BACKEND_VM" -ForegroundColor Green
Write-Host "---------------------------------------------------" -ForegroundColor Gray

Execute-OnVM -VM $BACKEND_VM -Command "cd $PROJECT_DIR; git pull origin main"
Execute-OnVM -VM $BACKEND_VM -Command "cd $PROJECT_DIR; docker compose down"
Execute-OnVM -VM $BACKEND_VM -Command "cd $PROJECT_DIR; docker compose pull"
Execute-OnVM -VM $BACKEND_VM -Command "cd $PROJECT_DIR; docker compose up -d --build"

Write-Host ""
Write-Host "✅ Backend redéployé" -ForegroundColor Green
Write-Host ""

# Étape 3 : Vérifications
Write-Host "🔍 Étape 3 : Vérifications" -ForegroundColor Green
Write-Host "---------------------------------------------------" -ForegroundColor Gray

Write-Host "Attente de 10 secondes pour le démarrage du backend..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "Vérification de la santé du backend:" -ForegroundColor Cyan
Execute-OnVM -VM $BACKEND_VM -Command "curl -s http://localhost:8081/actuator/health"

Write-Host ""
Write-Host "Vérification des hôtels publics:" -ForegroundColor Cyan
Execute-OnVM -VM $BACKEND_VM -Command "curl -s http://localhost:8081/api/hotels/public | head -c 200"

Write-Host ""
Write-Host "Dernières lignes des logs:" -ForegroundColor Cyan
Execute-OnVM -VM $BACKEND_VM -Command "docker logs hotel-ticket-hub-backend-staging --tail 20"

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "✅ Corrections terminées" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pour voir les logs en temps réel:" -ForegroundColor Yellow
Write-Host "ssh -i $SSH_KEY $SSH_USER@$BACKEND_VM 'docker logs -f hotel-ticket-hub-backend-staging'" -ForegroundColor Gray
