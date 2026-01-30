# Script PowerShell pour diagnostiquer les problèmes de connexion SSH
# Usage: .\diagnose-ssh-connection.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Diagnostic de la connexion SSH" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$VM_IP = "13.63.15.86"
$VM_USER = "ubuntu"
$SSH_KEY_PATH = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "  VM IP: $VM_IP"
Write-Host "  VM User: $VM_USER"
Write-Host "  SSH Key: $SSH_KEY_PATH"
Write-Host ""

# 1. Vérifier que la clé SSH existe
Write-Host "1. Vérification de la clé SSH..." -ForegroundColor Yellow
if (Test-Path $SSH_KEY_PATH) {
    Write-Host "   [OK] Clé SSH trouvée" -ForegroundColor Green
    $keyInfo = Get-Item $SSH_KEY_PATH
    Write-Host "   Taille: $($keyInfo.Length) bytes"
    Write-Host "   Dernière modification: $($keyInfo.LastWriteTime)"
} else {
    Write-Host "   [ERREUR] Clé SSH non trouvée: $SSH_KEY_PATH" -ForegroundColor Red
    Write-Host "   Vérifiez le chemin de la clé SSH"
    exit 1
}
Write-Host ""

# 2. Vérifier la connectivité réseau (ping)
Write-Host "2. Test de connectivité réseau (ping)..." -ForegroundColor Yellow
$pingResult = Test-Connection -ComputerName $VM_IP -Count 2 -Quiet
if ($pingResult) {
    Write-Host "   [OK] La VM répond au ping" -ForegroundColor Green
} else {
    Write-Host "   [ERREUR] La VM ne répond pas au ping" -ForegroundColor Red
    Write-Host "   Cela peut indiquer:"
    Write-Host "   - La VM est arrêtée"
    Write-Host "   - Le Security Group bloque le ping (ICMP)"
    Write-Host "   - Problème réseau"
}
Write-Host ""

# 3. Vérifier si le port SSH (22) est ouvert
Write-Host "3. Test du port SSH (22)..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $connect = $tcpClient.BeginConnect($VM_IP, 22, $null, $null)
    $wait = $connect.AsyncWaitHandle.WaitOne(5000, $false)
    if ($wait) {
        $tcpClient.EndConnect($connect)
        Write-Host "   [OK] Le port 22 est ouvert et accessible" -ForegroundColor Green
        $tcpClient.Close()
    } else {
        Write-Host "   [ERREUR] Timeout lors de la connexion au port 22" -ForegroundColor Red
        Write-Host "   Le Security Group bloque probablement les connexions SSH"
    }
} catch {
    Write-Host "   [ERREUR] Impossible de se connecter au port 22" -ForegroundColor Red
    Write-Host "   Erreur: $($_.Exception.Message)"
    Write-Host "   Le Security Group bloque probablement les connexions SSH"
}
Write-Host ""

# 4. Tester la connexion SSH
Write-Host "4. Test de la connexion SSH..." -ForegroundColor Yellow
$sshCommand = "ssh -i `"$SSH_KEY_PATH`" -o ConnectTimeout=10 -o StrictHostKeyChecking=no ${VM_USER}@${VM_IP} `"echo 'SSH connection successful'`" 2>&1
try {
    $sshResult = Invoke-Expression $sshCommand
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   [OK] Connexion SSH reussie!" -ForegroundColor Green
        Write-Host "   Réponse: $sshResult"
    } else {
        Write-Host "   [ERREUR] Connexion SSH échouée" -ForegroundColor Red
        Write-Host "   Code de sortie: $LASTEXITCODE"
        Write-Host "   Sortie: $sshResult"
    }
} catch {
    Write-Host "   [ERREUR] Exception lors de la connexion SSH" -ForegroundColor Red
    Write-Host "   Erreur: $($_.Exception.Message)"
}
Write-Host ""

# 5. Vérifier l'IP avec AWS CLI (si disponible)
Write-Host "5. Vérification de l'IP via AWS CLI..." -ForegroundColor Yellow
if (Get-Command aws -ErrorAction SilentlyContinue) {
    Write-Host "   AWS CLI est installé"
    Write-Host "   Pour vérifier l'IP de votre instance, exécutez:"
    Write-Host "   aws ec2 describe-instances --filters `"Name=ip-address,Values=$VM_IP`" --query 'Reservations[0].Instances[0].[InstanceId,PublicIpAddress,State.Name]' --output table" -ForegroundColor Cyan
} else {
    Write-Host "   AWS CLI n'est pas installé"
    Write-Host "   Installez-le pour vérifier l'état de l'instance: https://aws.amazon.com/cli/" -ForegroundColor Yellow
}
Write-Host ""

# Résumé et recommandations
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Résumé et recommandations" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Si la connexion SSH échoue, vérifiez:" -ForegroundColor Yellow
Write-Host "1. AWS Console - EC2 - Security Groups"
Write-Host "   - Trouvez le Security Group de votre instance"
Write-Host "   - Vérifiez qu'une règle SSH (port 22) existe"
Write-Host "   - Source doit être 0.0.0.0/0 (ou votre IP publique)"
Write-Host ""
Write-Host "2. AWS Console - EC2 - Instances"
Write-Host "   - Vérifiez que l'instance est en état 'running'"
Write-Host "   - Vérifiez que l'IP publique est toujours $VM_IP"
Write-Host ""
Write-Host "3. Pour ajouter une règle SSH via AWS CLI:" -ForegroundColor Cyan
Write-Host "   # Récupérer le Security Group ID"
Write-Host "   `$SG_ID = aws ec2 describe-instances --filters `"Name=ip-address,Values=$VM_IP`" --query 'Reservations[0].Instances[0].SecurityGroups[0].GroupId' --output text"
Write-Host ""
Write-Host "   # Ajouter la règle SSH"
Write-Host "   aws ec2 authorize-security-group-ingress --group-id `$SG_ID --protocol tcp --port 22 --cidr 0.0.0.0/0 --description 'Allow SSH'"
Write-Host ""

