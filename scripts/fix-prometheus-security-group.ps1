# Script PowerShell pour ouvrir le port 8081 dans le Security Group AWS
# Permet à Prometheus de scraper les métriques du backend
# Usage: .\fix-prometheus-security-group.ps1

param(
    [string]$BackendVM_IP = "13.63.15.86",
    [string]$PrometheusVM_IP = "16.170.74.58",
    [string]$Region = "eu-north-1",
    [int]$Port = 8081
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Ouverture du port $Port pour Prometheus" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que AWS CLI est installé
if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
    Write-Host "[ERREUR] AWS CLI n'est pas installe" -ForegroundColor Red
    Write-Host "Installez-le depuis: https://aws.amazon.com/cli/" -ForegroundColor Yellow
    exit 1
}

Write-Host "[OK] AWS CLI est installe" -ForegroundColor Green
Write-Host ""

# Vérifier la configuration AWS
Write-Host "Verification de la configuration AWS..." -ForegroundColor Yellow
try {
    $awsIdentity = aws sts get-caller-identity 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERREUR] AWS CLI n'est pas configure" -ForegroundColor Red
        Write-Host "Configurez AWS CLI avec: aws configure" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "[OK] AWS CLI est configure" -ForegroundColor Green
} catch {
    Write-Host "[ERREUR] Impossible de verifier la configuration AWS" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Trouver l'instance Backend par IP
Write-Host "Recherche de l'instance Backend avec l'IP $BackendVM_IP..." -ForegroundColor Yellow
try {
    $instanceInfo = aws ec2 describe-instances `
        --filters "Name=ip-address,Values=$BackendVM_IP" `
        --region $Region `
        --query 'Reservations[0].Instances[0].[InstanceId,State.Name,PublicIpAddress,SecurityGroups[0].GroupId]' `
        --output text 2>&1
    
    if ($LASTEXITCODE -ne 0 -or $instanceInfo -match "None" -or -not $instanceInfo) {
        Write-Host "[ERREUR] Instance non trouvee avec l'IP $BackendVM_IP" -ForegroundColor Red
        Write-Host "Verifiez que l'IP est correcte et que l'instance existe dans la region $Region" -ForegroundColor Yellow
        exit 1
    }
    
    $instanceData = $instanceInfo -split "`t"
    $instanceId = $instanceData[0]
    $instanceState = $instanceData[1]
    $instanceIP = $instanceData[2]
    $securityGroupId = $instanceData[3]
    
    Write-Host "[OK] Instance Backend trouvee:" -ForegroundColor Green
    Write-Host "  Instance ID: $instanceId"
    Write-Host "  Etat: $instanceState"
    Write-Host "  IP Publique: $instanceIP"
    Write-Host "  Security Group ID: $securityGroupId"
    Write-Host ""
} catch {
    Write-Host "[ERREUR] Impossible de trouver l'instance Backend" -ForegroundColor Red
    Write-Host $_.Exception.Message
    exit 1
}

# Vérifier les règles existantes pour le port 8081
Write-Host "Verification des regles existantes pour le port $Port..." -ForegroundColor Yellow
try {
    $existingRules = aws ec2 describe-security-groups `
        --group-ids $securityGroupId `
        --region $Region `
        --query "SecurityGroups[0].IpPermissions[?FromPort==`$Port && ToPort==`$Port && IpProtocol==`'tcp`']" `
        --output json 2>&1
    
    $hasPrometheusRule = $false
    $hasPrometheusIP = $false
    
    if ($existingRules -and $existingRules -ne "[]" -and $existingRules -ne "null") {
        $rulesJson = $existingRules | ConvertFrom-Json
        foreach ($rule in $rulesJson) {
            $hasPrometheusRule = $true
            foreach ($ipRange in $rule.IpRanges) {
                if ($ipRange.CidrIp -eq "$PrometheusVM_IP/32") {
                    $hasPrometheusIP = $true
                    Write-Host "[OK] Une regle pour le port $Port depuis $PrometheusVM_IP existe deja" -ForegroundColor Green
                    Write-Host "  Description: $($ipRange.Description)" -ForegroundColor Gray
                    break
                }
            }
            if ($hasPrometheusIP) { break }
        }
    }
    
    if (-not $hasPrometheusRule -or -not $hasPrometheusIP) {
        Write-Host "[INFO] Aucune regle trouvee pour le port $Port depuis $PrometheusVM_IP" -ForegroundColor Yellow
        Write-Host "Ajout d'une regle pour autoriser Prometheus..." -ForegroundColor Yellow
        
        $addRuleResult = aws ec2 authorize-security-group-ingress `
            --group-id $securityGroupId `
            --protocol tcp `
            --port $Port `
            --cidr "$PrometheusVM_IP/32" `
            --region $Region `
            --description "Allow Prometheus scraping from monitoring VM ($PrometheusVM_IP)" `
            2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[OK] Regle ajoutee avec succes!" -ForegroundColor Green
            Write-Host "  Port: $Port" -ForegroundColor Gray
            Write-Host "  Source: $PrometheusVM_IP/32" -ForegroundColor Gray
        } else {
            if ($addRuleResult -match "already exists") {
                Write-Host "[INFO] La regle existe deja (peut-etre avec une description differente)" -ForegroundColor Yellow
            } else {
                Write-Host "[ERREUR] Impossible d'ajouter la regle" -ForegroundColor Red
                Write-Host $addRuleResult
                exit 1
            }
        }
    }
} catch {
    Write-Host "[ERREUR] Impossible de verifier/modifier les regles" -ForegroundColor Red
    Write-Host $_.Exception.Message
    exit 1
}
Write-Host ""

# Test de connectivité
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Test de connectivite" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Test de la connexion depuis la VM Prometheus..." -ForegroundColor Yellow
$sshKeyPath = "$env:USERPROFILE\.ssh\oumayma-key.pem"

if (Test-Path $sshKeyPath) {
    Write-Host "Attente de 5 secondes pour que les changements prennent effet..." -ForegroundColor Gray
    Start-Sleep -Seconds 5
    
    $connectivityTest = ssh -i $sshKeyPath -o ConnectTimeout=10 ubuntu@${PrometheusVM_IP} "curl -s -o /dev/null -w '%{http_code}' --connect-timeout 5 http://$BackendVM_IP:$Port/actuator/prometheus 2>&1" 2>$null
    
    if ($connectivityTest -eq "200") {
        Write-Host "[OK] Connexion reussie! Le backend est accessible depuis Prometheus" -ForegroundColor Green
    } else {
        Write-Host "[ATTENTION] Connexion echouee (HTTP $connectivityTest)" -ForegroundColor Yellow
        Write-Host "Verifiez que:" -ForegroundColor Yellow
        Write-Host "  1. Le backend est en cours d'execution" -ForegroundColor Gray
        Write-Host "  2. Attendez quelques secondes de plus pour que les changements prennent effet" -ForegroundColor Gray
        Write-Host "  3. Testez manuellement: curl http://$BackendVM_IP:$Port/actuator/prometheus" -ForegroundColor Gray
    }
} else {
    Write-Host "[ATTENTION] Cle SSH non trouvee: $sshKeyPath" -ForegroundColor Yellow
    Write-Host "Testez manuellement depuis la VM Prometheus:" -ForegroundColor Cyan
    Write-Host "  curl http://$BackendVM_IP:$Port/actuator/prometheus" -ForegroundColor Gray
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Correction terminee" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Prochaines etapes:" -ForegroundColor Yellow
Write-Host "  1. Verifiez dans Prometheus: http://$PrometheusVM_IP:9090/targets" -ForegroundColor Cyan
Write-Host "  2. Le target 'staging-backend' devrait passer a UP dans 15-30 secondes" -ForegroundColor Cyan
Write-Host ""

