# Script PowerShell pour corriger automatiquement le Security Group AWS
# Usage: .\fix-aws-security-group-auto.ps1

param(
    [string]$VM_IP = "13.63.15.86",
    [string]$Region = "eu-north-1"
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Correction automatique du Security Group AWS" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que AWS CLI est installé et configuré
if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
    Write-Host "[ERREUR] AWS CLI n'est pas installe" -ForegroundColor Red
    Write-Host "Installez-le depuis: https://aws.amazon.com/cli/" -ForegroundColor Yellow
    exit 1
}

Write-Host "[OK] AWS CLI est installe" -ForegroundColor Green

# Vérifier la configuration AWS
Write-Host "Verification de la configuration AWS..." -ForegroundColor Yellow
try {
    $awsIdentity = aws sts get-caller-identity 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERREUR] AWS CLI n'est pas configure ou vous n'etes pas connecte" -ForegroundColor Red
        Write-Host "Configurez AWS CLI avec: aws configure" -ForegroundColor Yellow
        Write-Host "Ou connectez-vous avec: aws sso login" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "[OK] AWS CLI est configure" -ForegroundColor Green
    Write-Host $awsIdentity
} catch {
    Write-Host "[ERREUR] Impossible de verifier la configuration AWS" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Trouver l'instance par IP
Write-Host "Recherche de l'instance avec l'IP $VM_IP..." -ForegroundColor Yellow
try {
    $instanceInfo = aws ec2 describe-instances `
        --filters "Name=ip-address,Values=$VM_IP" `
        --region $Region `
        --query 'Reservations[0].Instances[0].[InstanceId,State.Name,PublicIpAddress,SecurityGroups[0].GroupId]' `
        --output text 2>&1
    
    if ($LASTEXITCODE -ne 0 -or $instanceInfo -match "None") {
        Write-Host "[ERREUR] Instance non trouvee avec l'IP $VM_IP" -ForegroundColor Red
        Write-Host "Verifiez que l'IP est correcte et que l'instance existe dans la region $Region" -ForegroundColor Yellow
        exit 1
    }
    
    $instanceData = $instanceInfo -split "`t"
    $instanceId = $instanceData[0]
    $instanceState = $instanceData[1]
    $instanceIP = $instanceData[2]
    $securityGroupId = $instanceData[3]
    
    Write-Host "[OK] Instance trouvee:" -ForegroundColor Green
    Write-Host "  Instance ID: $instanceId"
    Write-Host "  Etat: $instanceState"
    Write-Host "  IP Publique: $instanceIP"
    Write-Host "  Security Group ID: $securityGroupId"
    
    if ($instanceState -ne "running") {
        Write-Host "[ATTENTION] L'instance n'est pas en etat 'running'" -ForegroundColor Yellow
        Write-Host "Demarrez l'instance avec: aws ec2 start-instances --instance-ids $instanceId --region $Region" -ForegroundColor Cyan
    }
} catch {
    Write-Host "[ERREUR] Impossible de trouver l'instance" -ForegroundColor Red
    Write-Host $_.Exception.Message
    exit 1
}
Write-Host ""

# Vérifier les règles SSH existantes
Write-Host "Verification des regles SSH existantes..." -ForegroundColor Yellow
try {
    $existingRules = aws ec2 describe-security-groups `
        --group-ids $securityGroupId `
        --region $Region `
        --query 'SecurityGroups[0].IpPermissions[?FromPort==`22` && ToPort==`22` && IpProtocol==`tcp`]' `
        --output json 2>&1
    
    $hasSSHRule = $false
    $hasPublicAccess = $false
    
    if ($existingRules -and $existingRules -ne "[]" -and $existingRules -ne "null") {
        $rulesJson = $existingRules | ConvertFrom-Json
        foreach ($rule in $rulesJson) {
            $hasSSHRule = $true
            foreach ($ipRange in $rule.IpRanges) {
                if ($ipRange.CidrIp -eq "0.0.0.0/0") {
                    $hasPublicAccess = $true
                    Write-Host "[OK] Une regle SSH avec acces public (0.0.0.0/0) existe deja" -ForegroundColor Green
                    break
                }
            }
            if ($hasPublicAccess) { break }
        }
    }
    
    if (-not $hasSSHRule -or -not $hasPublicAccess) {
        Write-Host "[INFO] Aucune regle SSH publique trouvee" -ForegroundColor Yellow
        Write-Host "Ajout d'une regle SSH pour autoriser les connexions depuis n'importe ou..." -ForegroundColor Yellow
        
        $addRuleResult = aws ec2 authorize-security-group-ingress `
            --group-id $securityGroupId `
            --protocol tcp `
            --port 22 `
            --cidr 0.0.0.0/0 `
            --region $Region `
            --description "Allow SSH from anywhere (GitHub Actions)" `
            2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[OK] Regle SSH ajoutee avec succes!" -ForegroundColor Green
        } else {
            if ($addRuleResult -match "already exists") {
                Write-Host "[INFO] La regle existe deja (peut-etre avec une description differente)" -ForegroundColor Yellow
            } else {
                Write-Host "[ERREUR] Impossible d'ajouter la regle SSH" -ForegroundColor Red
                Write-Host $addRuleResult
                exit 1
            }
        }
    }
} catch {
    Write-Host "[ERREUR] Impossible de verifier/modifier les regles SSH" -ForegroundColor Red
    Write-Host $_.Exception.Message
    exit 1
}
Write-Host ""

# Test de connexion SSH
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Test de connexion SSH" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Test de la connexion SSH vers $VM_IP..." -ForegroundColor Yellow

$sshKeyPath = "$env:USERPROFILE\.ssh\oumayma-key.pem"
if (-not (Test-Path $sshKeyPath)) {
    Write-Host "[ATTENTION] Cle SSH non trouvee: $sshKeyPath" -ForegroundColor Yellow
    Write-Host "Testez manuellement avec:" -ForegroundColor Cyan
    Write-Host "ssh -i `"$sshKeyPath`" ubuntu@$VM_IP" -ForegroundColor Cyan
} else {
    $sshTest = ssh -i $sshKeyPath -o ConnectTimeout=10 -o StrictHostKeyChecking=no ubuntu@${VM_IP} "echo 'SSH connection successful'" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Connexion SSH reussie!" -ForegroundColor Green
        Write-Host $sshTest
    } else {
        Write-Host "[ERREUR] Connexion SSH echouee" -ForegroundColor Red
        Write-Host $sshTest
        Write-Host ""
        Write-Host "Verifiez:" -ForegroundColor Yellow
        Write-Host "1. Que l'instance est bien demarree"
        Write-Host "2. Que la cle SSH est correcte"
        Write-Host "3. Attendez quelques secondes pour que les changements du Security Group prennent effet"
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Correction terminee" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

