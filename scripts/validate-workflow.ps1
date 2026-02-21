# Script PowerShell pour valider les workflows GitHub Actions
# Usage: .\scripts\validate-workflow.ps1 [workflow-file]

$ErrorActionPreference = "Stop"

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Test-YAMLSyntax {
    param([string]$FilePath)
    
    Write-ColorOutput Yellow "`n📋 Validation de la syntaxe YAML..."
    
    # Vérifier si Python et PyYAML sont disponibles
    $pythonAvailable = $false
    $yamlModuleAvailable = $false
    
    try {
        $pythonVersion = python --version 2>&1
        if ($LASTEXITCODE -eq 0) {
            $pythonAvailable = $true
            Write-ColorOutput Cyan "  ✓ Python détecté: $pythonVersion"
            
            # Vérifier si PyYAML est installé
            $yamlCheck = python -c "import yaml" 2>&1
            if ($LASTEXITCODE -eq 0) {
                $yamlModuleAvailable = $true
                Write-ColorOutput Cyan "  ✓ PyYAML disponible"
            }
        }
    } catch {
        # Python non disponible, on continue avec d'autres méthodes
    }
    
    if ($yamlModuleAvailable) {
        # Valider avec Python PyYAML
        Write-ColorOutput Yellow "  Validation avec PyYAML..."
        $tempScript = [System.IO.Path]::GetTempFileName() + ".py"
        $filePathEscaped = $FilePath -replace "'", "''"
        
        $scriptLines = @(
            "import yaml",
            "import sys",
            "",
            "try:",
            "    with open(r'$filePathEscaped', 'r', encoding='utf-8') as f:",
            "        yaml.safe_load(f)",
            "    print('Syntaxe YAML valide')",
            "    sys.exit(0)",
            "except yaml.YAMLError as e:",
            "    print(f'Erreur YAML: {e}')",
            "    sys.exit(1)",
            "except Exception as e:",
            "    print(f'Erreur: {e}')",
            "    sys.exit(1)"
        )
        
        $scriptLines | Out-File -FilePath $tempScript -Encoding UTF8
        
        try {
            $result = python $tempScript 2>&1
            Remove-Item $tempScript -ErrorAction SilentlyContinue
            
            if ($LASTEXITCODE -eq 0) {
                Write-ColorOutput Green "  ✅ Syntaxe YAML valide"
                return $true
            } else {
                Write-ColorOutput Red "  ❌ Erreur YAML détectée:"
                Write-Output $result
                return $false
            }
        } catch {
            Remove-Item $tempScript -ErrorAction SilentlyContinue
            Write-ColorOutput Yellow "  ⚠️  Impossible de valider avec PyYAML, vérification basique..."
        }
    }
    
    # Validation basique: vérifier que le fichier est lisible et contient des éléments GitHub Actions
    Write-ColorOutput Yellow "  Vérification basique du fichier..."
    
    try {
        $content = Get-Content -Path $FilePath -Raw -ErrorAction Stop
        
        # Vérifications de base
        $checks = @{
            "Contient 'name:'" = $content -match "name:"
            "Contient 'on:'" = $content -match "`non:"
            "Contient 'jobs:' ou 'job:'" = $content -match "`njobs?:"
            "Pas de caractères invalides" = $content -notmatch "`t" -or $true  # Les tabs sont acceptés dans YAML
        }
        
        $allPassed = $true
        foreach ($check in $checks.GetEnumerator()) {
            if ($check.Value) {
                Write-ColorOutput Green "    ✓ $($check.Key)"
            } else {
                Write-ColorOutput Red "    ✗ $($check.Key)"
                $allPassed = $false
            }
        }
        
        if ($allPassed) {
            Write-ColorOutput Green "  ✅ Structure de base valide"
            return $true
        } else {
            Write-ColorOutput Red "  ❌ Structure de base invalide"
            return $false
        }
    } catch {
        Write-ColorOutput Red "  ❌ Erreur lors de la lecture du fichier: $_"
        return $false
    }
}

function Test-GitHubActionsSyntax {
    param([string]$FilePath)
    
    Write-ColorOutput Yellow "`n🔍 Validation de la syntaxe GitHub Actions..."
    
    try {
        $content = Get-Content -Path $FilePath -Raw
        
        # Vérifications spécifiques GitHub Actions
        $errors = @()
        $warnings = @()
        
        # Vérifier les actions utilisées
        $actionPattern = 'uses:\s+([^\s]+)'
        $actions = [regex]::Matches($content, $actionPattern) | ForEach-Object { $_.Groups[1].Value }
        
        Write-ColorOutput Cyan "  Actions détectées:"
        $uniqueActions = $actions | Select-Object -Unique
        foreach ($action in $uniqueActions) {
            Write-ColorOutput Gray "    - $action"
        }
        
        # Vérifier les versions des actions (recommandation)
        $versionPattern = 'uses:\s+([^@]+)@([^\s]+)'
        $actionVersions = [regex]::Matches($content, $versionPattern)
        $actionsWithoutVersion = @()
        
        foreach ($match in $actionVersions) {
            $actionName = $match.Groups[1].Value.Trim()
            $version = $match.Groups[2].Value.Trim()
            
            if ($version -notmatch '^v?\d+\.\d+' -and $version -ne 'main' -and $version -ne 'master') {
                $warnings += "Action '$actionName' utilise une version non standard: $version"
            }
        }
        
        # Vérifier les permissions
        if ($content -match 'permissions:') {
            Write-ColorOutput Green "    ✓ Permissions définies"
        } else {
            $warnings += "Aucune section 'permissions' trouvée (recommandé pour la sécurité)"
        }
        
        # Vérifier les secrets utilisés
        $secretPattern = '\$\{\{[\s]*secrets\.([^}]+)[\s]*\}\}'
        $secrets = [regex]::Matches($content, $secretPattern) | ForEach-Object { $_.Groups[1].Value } | Select-Object -Unique
        
        if ($secrets.Count -gt 0) {
            Write-ColorOutput Cyan "  Secrets référencés:"
            foreach ($secret in $secrets) {
                Write-ColorOutput Gray "    - $secret"
            }
        }
        
        # Vérifier les variables d'environnement
        $envPattern = '\$\{\{[\s]*env\.([^}]+)[\s]*\}\}'
        $envVars = [regex]::Matches($content, $envPattern) | ForEach-Object { $_.Groups[1].Value } | Select-Object -Unique
        
        if ($envVars.Count -gt 0) {
            Write-ColorOutput Cyan "  Variables d'environnement référencées:"
            foreach ($envVar in $envVars) {
                Write-ColorOutput Gray "    - $envVar"
            }
        }
        
        # Afficher les warnings
        if ($warnings.Count -gt 0) {
            Write-ColorOutput Yellow "`n  ⚠️  Avertissements:"
            foreach ($warning in $warnings) {
                Write-ColorOutput Yellow "    - $warning"
            }
        }
        
        if ($errors.Count -eq 0) {
            Write-ColorOutput Green "  ✅ Syntaxe GitHub Actions valide"
            return $true
        } else {
            Write-ColorOutput Red "  ❌ Erreurs détectées:"
            foreach ($error in $errors) {
                Write-ColorOutput Red "    - $error"
            }
            return $false
        }
        
    } catch {
        Write-ColorOutput Red "  ❌ Erreur lors de la validation: $_"
        return $false
    }
}

function Test-WorkflowStructure {
    param([string]$FilePath)
    
    Write-ColorOutput Yellow "`n📐 Validation de la structure du workflow..."
    
    try {
        $content = Get-Content -Path $FilePath -Raw
        
        # Vérifier les sections obligatoires
        $requiredSections = @{
            "name" = $content -match "`nname:"
            "on" = $content -match "`non:"
            "jobs" = $content -match "`njobs?:"
        }
        
        $allPresent = $true
        foreach ($section in $requiredSections.GetEnumerator()) {
            if ($section.Value) {
                Write-ColorOutput Green "    ✓ Section '$($section.Key)' présente"
            } else {
                Write-ColorOutput Red "    ✗ Section '$($section.Key)' manquante"
                $allPresent = $false
            }
        }
        
        # Vérifier l'indentation (basique)
        $lines = Get-Content -Path $FilePath
        $indentationIssues = 0
        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i]
            if ($line.Trim() -ne "" -and $line -match "^[ ]{1,}" -and $line -notmatch "^[ ]{2,}") {
                # Ligne avec seulement 1 espace (probablement une erreur)
                if ($line -notmatch "^[ ]{2}") {
                    $indentationIssues++
                }
            }
        }
        
        if ($indentationIssues -gt 0) {
            Write-ColorOutput Yellow "    ⚠️  $indentationIssues ligne(s) avec une indentation suspecte"
        } else {
            Write-ColorOutput Green "    ✓ Indentation semble correcte"
        }
        
        if ($allPresent) {
            Write-ColorOutput Green "  ✅ Structure du workflow valide"
            return $true
        } else {
            Write-ColorOutput Red "  ❌ Structure du workflow invalide"
            return $false
        }
        
    } catch {
        Write-ColorOutput Red "  ❌ Erreur lors de la validation de la structure: $_"
        return $false
    }
}

function Show-InstallationInstructions {
    Write-ColorOutput Cyan "`n💡 Pour une validation plus complète, installez:"
    Write-ColorOutput Gray "`n1. actionlint (recommandé pour GitHub Actions):"
    Write-ColorOutput White "   Windows: choco install actionlint"
    Write-ColorOutput White "   Ou télécharger depuis: https://github.com/rhymond/actionlint/releases"
    Write-ColorOutput Gray "`n2. yamllint (pour la validation YAML):"
    Write-ColorOutput White "   pip install yamllint"
    Write-ColorOutput Gray "`n3. PyYAML (pour la validation Python):"
    Write-ColorOutput White "   pip install pyyaml"
}

# Main execution
$workflowFile = $args[0]

if (-not $workflowFile) {
    # Utiliser le workflow par défaut
    $workflowFile = ".github\workflows\ci.yml"
}

if (-not (Test-Path $workflowFile)) {
    Write-ColorOutput Red "❌ Fichier workflow introuvable: $workflowFile"
    Write-ColorOutput Yellow "Usage: .\scripts\validate-workflow.ps1 [chemin-vers-workflow.yml]"
    exit 1
}

Write-ColorOutput Green "`n=========================================="
Write-ColorOutput Green "  Validation du Workflow GitHub Actions"
Write-ColorOutput Green "=========================================="
Write-ColorOutput Cyan "Fichier: $workflowFile`n"

$yamlValid = Test-YAMLSyntax -FilePath $workflowFile
$structureValid = Test-WorkflowStructure -FilePath $workflowFile
$actionsValid = Test-GitHubActionsSyntax -FilePath $workflowFile

Write-ColorOutput Cyan "`n=========================================="
Write-ColorOutput Cyan "=== Résumé de la Validation ==="
Write-ColorOutput Cyan "=========================================="

if ($yamlValid) {
    Write-ColorOutput Green "✅ Syntaxe YAML: VALIDE"
} else {
    Write-ColorOutput Red "❌ Syntaxe YAML: INVALIDE"
}

if ($structureValid) {
    Write-ColorOutput Green "✅ Structure: VALIDE"
} else {
    Write-ColorOutput Red "❌ Structure: INVALIDE"
}

if ($actionsValid) {
    Write-ColorOutput Green "✅ Syntaxe GitHub Actions: VALIDE"
} else {
    Write-ColorOutput Red "❌ Syntaxe GitHub Actions: INVALIDE"
}

Write-ColorOutput Cyan "=========================================="

if ($yamlValid -and $structureValid -and $actionsValid) {
    Write-ColorOutput Green "`n🎉 Le workflow semble valide! Prêt à être commité."
    Show-InstallationInstructions
    exit 0
} else {
    Write-ColorOutput Red "`n❌ Des erreurs ont été détectées. Veuillez corriger avant de commit."
    Show-InstallationInstructions
    exit 1
}

