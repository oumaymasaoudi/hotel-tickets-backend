# Script PowerShell pour copier la clé SSH sur l'instance EC2 backend
# Usage: .\copy-ssh-key.ps1 -HostIP "13.49.44.219" -AWSKey "votre-cle-aws.pem"

param(
    [Parameter(Mandatory=$true)]
    [string]$HostIP,
    
    [Parameter(Mandatory=$true)]
    [string]$AWSKey,
    
    [Parameter(Mandatory=$false)]
    [string]$User = "ubuntu"
)

# Verifier que la cle publique existe
if (-not (Test-Path "github-actions-key.pub")) {
    Write-Host "Erreur: github-actions-key.pub introuvable!" -ForegroundColor Red
    Write-Host "Generez d'abord la cle avec: ssh-keygen -t rsa -b 4096 -C 'github-actions-backend' -f github-actions-key" -ForegroundColor Yellow
    exit 1
}

# Lire la cle publique
$publicKey = Get-Content github-actions-key.pub -Raw
$publicKey = $publicKey.Trim()

Write-Host "Copie de la cle SSH vers $HostIP..." -ForegroundColor Cyan

# Commande SSH pour ajouter la cle
$sshCommand = "mkdir -p ~/.ssh && echo '$publicKey' >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys && chmod 700 ~/.ssh && echo 'Cle ajoutee avec succes!'"

# Tester d'abord la connexion avec la cle AWS
Write-Host "Test de connexion avec la cle AWS..." -ForegroundColor Yellow
$testConnection = ssh -i $AWSKey "$User@$HostIP" "echo 'OK'" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erreur: Impossible de se connecter avec la cle AWS!" -ForegroundColor Red
    Write-Host "Verifiez que:" -ForegroundColor Yellow
    Write-Host "  - La cle AWS est correcte: $AWSKey" -ForegroundColor Yellow
    Write-Host "  - L'utilisateur est correct: $User (essayez 'ubuntu' ou 'ec2-user')" -ForegroundColor Yellow
    Write-Host "  - L'IP est correcte: $HostIP" -ForegroundColor Yellow
    Write-Host "  - Les Security Groups AWS autorisent SSH (port 22)" -ForegroundColor Yellow
    exit 1
}
Write-Host "Connexion AWS OK!" -ForegroundColor Green
Write-Host ""

# Executer la commande SSH pour copier la cle
Write-Host "Copie de la cle publique sur le serveur..." -ForegroundColor Cyan
$copyResult = ssh -i $AWSKey "$User@$HostIP" $sshCommand 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Erreur lors de la copie de la cle: $copyResult" -ForegroundColor Red
    exit 1
}

Write-Host "Cle SSH copiee avec succes sur $HostIP" -ForegroundColor Green
Write-Host ""
Write-Host "Test de la connexion avec la nouvelle cle..." -ForegroundColor Cyan

# Tester la connexion avec la nouvelle cle
$testNewKey = ssh -i github-actions-key "$User@$HostIP" "echo 'Connexion reussie avec la nouvelle cle!' && hostname" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ATTENTION: La cle a ete copiee mais la connexion echoue!" -ForegroundColor Yellow
    Write-Host "Erreur: $testNewKey" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifiez manuellement:" -ForegroundColor Yellow
    Write-Host "  ssh -i github-actions-key $User@$HostIP" -ForegroundColor Cyan
    exit 1
}

Write-Host $testNewKey
Write-Host ""
Write-Host "Configuration SSH terminee avec succes pour $HostIP!" -ForegroundColor Green

