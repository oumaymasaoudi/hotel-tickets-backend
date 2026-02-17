# Script PowerShell - Démarrage de TOUTES les VMs
# Usage: .\scripts\start-all-vms.ps1

$ErrorActionPreference = "Continue"

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🚀 Démarrage de TOUTES les VMs - TicketHotel" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

# Scripts à exécuter dans l'ordre
$scripts = @(
    @{ Name = "1-start-backend.ps1"; Description = "Backend VM (13.63.15.86)" },
    @{ Name = "2-start-frontend.ps1"; Description = "Frontend VM (13.50.221.51)" },
    @{ Name = "3-start-database.ps1"; Description = "Database VM (13.48.83.147)" },
    @{ Name = "4-start-monitoring.ps1"; Description = "Monitoring VM (16.170.74.58)" }
)

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$successCount = 0
$failCount = 0

foreach ($script in $scripts) {
    $scriptFile = Join-Path $scriptPath $script.Name
    
    Write-Host ""
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
    Write-Host "📦 Exécution: $($script.Description)" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
    
    if (Test-Path $scriptFile) {
        try {
            & $scriptFile
            if ($LASTEXITCODE -eq 0) {
                $successCount++
                Write-Host "✅ $($script.Description) - Succès" -ForegroundColor Green
            } else {
                $failCount++
                Write-Host "❌ $($script.Description) - Échec" -ForegroundColor Red
            }
        } catch {
            $failCount++
            Write-Host "❌ Erreur lors de l'exécution de $($script.Name): $_" -ForegroundColor Red
        }
    } else {
        $failCount++
        Write-Host "❌ Script introuvable: $scriptFile" -ForegroundColor Red
    }
    
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "📊 Résumé" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "✅ Succès: $successCount" -ForegroundColor Green
Write-Host "❌ Échecs: $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })
Write-Host ""

if ($failCount -eq 0) {
    Write-Host "[SUCCESS] Toutes les VMs sont demarrees avec succes!" -ForegroundColor Green
    Write-Host ""
    Write-Host "URLs d'acces:" -ForegroundColor Cyan
    Write-Host "   Frontend: http://13.50.221.51" -ForegroundColor White
    Write-Host "   Backend: http://13.63.15.86:8081/api" -ForegroundColor White
    Write-Host "   Grafana: http://16.170.74.58:3000 (admin/admin)" -ForegroundColor White
    Write-Host "   Prometheus: http://16.170.74.58:9090" -ForegroundColor White
} else {
    Write-Host "[WARNING] Certaines VMs n'ont pas demarre correctement." -ForegroundColor Yellow
    Write-Host "   Verifiez les logs ci-dessus pour plus de details." -ForegroundColor Yellow
}

Write-Host ""
