# Script PowerShell - Vérification/Démarrage Database VM (13.48.83.147)
# Usage: .\scripts\3-start-database.ps1

$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"
$DATABASE_IP = "13.48.83.147"
$DATABASE_USER = "ubuntu"

Write-Host "🗄️  Vérification Database VM ($DATABASE_IP)..." -ForegroundColor Cyan

# Vérifier que la clé SSH existe
if (-not (Test-Path $SSH_KEY)) {
    Write-Host "❌ Erreur: Clé SSH introuvable: $SSH_KEY" -ForegroundColor Red
    Write-Host "   Veuillez créer la clé SSH ou mettre à jour le chemin." -ForegroundColor Yellow
    exit 1
}

Write-Host "Connexion SSH à $DATABASE_USER@$DATABASE_IP..." -ForegroundColor Yellow

$bashCmd = @'
bash -c 'echo INFO: Verification du statut PostgreSQL...; sudo systemctl status postgresql --no-pager 2>&1 || echo WARN: PostgreSQL non demarre; echo; echo INFO: Demarrage de PostgreSQL...; sudo systemctl start postgresql; sudo systemctl enable postgresql; echo; echo INFO: Verification du statut apres demarrage:; sudo systemctl status postgresql --no-pager | head -10; echo; echo INFO: Test de connexion a la base de donnees:; sudo -u postgres psql -d hotel_ticket_hub -c "SELECT version();" 2>&1 || echo WARN: Impossible de se connecter a la base; echo; echo INFO: Liste des bases de donnees:; sudo -u postgres psql -l 2>&1 | grep hotel_ticket_hub || echo WARN: Base hotel_ticket_hub non trouvee; echo; echo OK: Database verifiee!'
'@

ssh -i $SSH_KEY -o StrictHostKeyChecking=no $DATABASE_USER@$DATABASE_IP $bashCmd

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Database vérifiée avec succès!" -ForegroundColor Green
    Write-Host "   Host: $DATABASE_IP:5432" -ForegroundColor Cyan
    Write-Host "   Database: hotel_ticket_hub" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "❌ Erreur lors de la vérification de la Database" -ForegroundColor Red
    exit 1
}
