# Script PowerShell - Désactiver l'analyse automatique SonarCloud
# Usage: .\scripts\disable-sonarcloud-auto-analysis.ps1

param(
    [Parameter(Mandatory=$false)]
    [string]$SonarToken = $env:SONAR_TOKEN
)

$PROJECT_KEY = "oumaymasaoudi_hotel-tickets-backend"
$SONARCLOUD_API = "https://sonarcloud.io/api"

Write-Host "🔧 Désactivation de l'analyse automatique SonarCloud" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan

if (-not $SonarToken) {
    Write-Host "❌ Erreur: Token SonarCloud non fourni" -ForegroundColor Red
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  1. Passer le token en paramètre:" -ForegroundColor White
    Write-Host "     .\scripts\disable-sonarcloud-auto-analysis.ps1 -SonarToken 'votre-token'" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  2. Définir la variable d'environnement SONAR_TOKEN:" -ForegroundColor White
    Write-Host "     `$env:SONAR_TOKEN = 'votre-token'" -ForegroundColor Gray
    Write-Host "     .\scripts\disable-sonarcloud-auto-analysis.ps1" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  3. Récupérer le token depuis GitHub Secrets:" -ForegroundColor White
    Write-Host "     Repository > Settings > Secrets and variables > Actions > SONAR_TOKEN" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  4. Ou désactiver manuellement dans l'interface SonarCloud:" -ForegroundColor White
    Write-Host "     https://sonarcloud.io > Projet > Administration > Analysis Method" -ForegroundColor Gray
    Write-Host "     Voir SONARCLOUD-FIX.md pour plus de détails" -ForegroundColor Gray
    exit 1
}

Write-Host "Projet: $PROJECT_KEY" -ForegroundColor White
Write-Host "API: $SONARCLOUD_API" -ForegroundColor White
Write-Host ""

# Encoder le token pour l'authentification Basic
$bytes = [System.Text.Encoding]::ASCII.GetBytes("${SonarToken}:")
$base64 = [System.Convert]::ToBase64String($bytes)
$headers = @{
    "Authorization" = "Basic $base64"
    "Content-Type" = "application/json"
}

Write-Host "Vérification de l'état actuel de l'analyse..." -ForegroundColor Yellow

try {
    # Vérifier l'état actuel (si l'API le permet)
    Write-Host "Désactivation de l'analyse automatique..." -ForegroundColor Yellow
    
    $url = "$SONARCLOUD_API/analysis_methods/disable_automatic_analysis?project=$PROJECT_KEY"
    
    $response = Invoke-RestMethod -Uri $url -Method POST -Headers $headers -ErrorAction Stop
    
    Write-Host ""
    Write-Host "✅ Analyse automatique désactivée avec succès!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Prochaines étapes:" -ForegroundColor Yellow
    Write-Host "  1. Relancer le pipeline GitHub Actions" -ForegroundColor White
    Write-Host "  2. Vérifier que le job SonarCloud passe sans erreur" -ForegroundColor White
    Write-Host ""
    
} catch {
    Write-Host ""
    Write-Host "❌ Erreur lors de la désactivation:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "⚠️  Erreur d'authentification (401)" -ForegroundColor Yellow
        Write-Host "   Vérifiez que le token SonarCloud est correct" -ForegroundColor White
    } elseif ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "⚠️  Projet non trouvé (404)" -ForegroundColor Yellow
        Write-Host "   Vérifiez que le projet existe: $PROJECT_KEY" -ForegroundColor White
    } else {
        Write-Host "⚠️  Erreur HTTP: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "Alternative: Désactiver manuellement dans l'interface SonarCloud" -ForegroundColor Yellow
    Write-Host "  https://sonarcloud.io/project/overview?id=$PROJECT_KEY" -ForegroundColor Cyan
    Write-Host "  Administration > Analysis Method > Désactiver 'Automatic Analysis'" -ForegroundColor White
    Write-Host ""
    Write-Host "Voir SONARCLOUD-FIX.md pour plus de détails" -ForegroundColor Gray
    
    exit 1
}
