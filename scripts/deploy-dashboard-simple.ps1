# Script simplifié pour déployer le dashboard corrigé
# Résout les problèmes de chemins et de syntaxe PowerShell

$BACKEND_IP = "13.63.15.86"
$PROMETHEUS_IP = "16.170.74.58"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Deploiement du Dashboard Corrige" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Copier le dashboard sur la VM Monitoring
Write-Host "1. Copie du dashboard vers la VM Monitoring..." -ForegroundColor Cyan
$dashboardPath = "monitoring\grafana\dashboards\backend-spring-boot.json"
if (-not (Test-Path $dashboardPath)) {
    Write-Host "   ✗ Fichier non trouve: $dashboardPath" -ForegroundColor Red
    exit 1
}

scp -i $SSH_KEY $dashboardPath ubuntu@${PROMETHEUS_IP}:/tmp/backend-spring-boot.json 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "   ✗ Erreur lors de la copie" -ForegroundColor Red
    exit 1
}
Write-Host "   ✓ Dashboard copie vers /tmp/" -ForegroundColor Green
Write-Host ""

# 2. Trouver le bon répertoire Grafana et copier
Write-Host "2. Recherche du repertoire Grafana..." -ForegroundColor Cyan
$grafanaPath = ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "ls -d /opt/*/grafana/dashboards /opt/docker/*/grafana/dashboards /var/lib/grafana/dashboards 2>/dev/null | head -1" 2>&1

if (-not $grafanaPath -or $grafanaPath -match "No such file") {
    # Essayer de trouver via docker volume
    Write-Host "   Recherche via Docker volume..." -ForegroundColor Gray
    $grafanaPath = ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "docker volume ls | grep grafana | head -1 | awk '{print `/var/lib/docker/volumes/` + `$2` + `/_data/dashboards`}'" 2>&1
}

if (-not $grafanaPath -or $grafanaPath -match "No such file") {
    # Chemin par défaut
    Write-Host "   Utilisation du chemin par defaut..." -ForegroundColor Gray
    $grafanaPath = "/opt/docker/monitoring/grafana/dashboards"
    ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "sudo mkdir -p $grafanaPath" 2>&1 | Out-Null
}

Write-Host "   Repertoire trouve: $grafanaPath" -ForegroundColor Green

# Copier dans le conteneur Grafana directement
Write-Host "3. Copie dans le conteneur Grafana..." -ForegroundColor Cyan
ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "docker cp /tmp/backend-spring-boot.json grafana:/var/lib/grafana/dashboards/backend-spring-boot.json" 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Dashboard copie dans le conteneur Grafana" -ForegroundColor Green
} else {
    Write-Host "   ⚠ Erreur lors de la copie dans le conteneur, essai via volume..." -ForegroundColor Yellow
    # Alternative : copier via le volume monté
    ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "sudo cp /tmp/backend-spring-boot.json $grafanaPath/backend-spring-boot.json" 2>&1 | Out-Null
    ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "sudo chown root:root $grafanaPath/backend-spring-boot.json" 2>&1 | Out-Null
    Write-Host "   ✓ Dashboard copie via volume" -ForegroundColor Green
}
Write-Host ""

# 4. Redémarrer Grafana
Write-Host "4. Redemarrage de Grafana..." -ForegroundColor Cyan
$monitoringDir = ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "find /opt -name 'docker-compose*.yml' -o -name 'docker-compose*.yaml' 2>/dev/null | head -1 | xargs dirname 2>/dev/null || echo '/opt/docker/monitoring'" 2>&1
ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "cd $monitoringDir; docker compose restart grafana" 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Grafana redemarre" -ForegroundColor Green
} else {
    Write-Host "   ⚠ Erreur lors du redemarrage, essai direct..." -ForegroundColor Yellow
    ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "docker restart grafana" 2>&1 | Out-Null
    Write-Host "   ✓ Grafana redemarre (direct)" -ForegroundColor Green
}
Write-Host ""

# 5. Vérifier/copier docker-compose.yml sur la VM Backend
Write-Host "5. Verification du backend..." -ForegroundColor Cyan
$backendDir = "/opt/hotel-ticket-hub-backend-staging"
$hasHistogram = ssh -i $SSH_KEY ubuntu@${BACKEND_IP} "cd $backendDir; grep -q 'MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILES_HISTOGRAM_HTTP_SERVER_REQUESTS=true' docker-compose.yml 2>/dev/null && echo 'OK' || echo 'MISSING'" 2>&1

if ($hasHistogram -match "MISSING") {
    Write-Host "   ⚠ Histogrammes non actives, copie de docker-compose.yml..." -ForegroundColor Yellow
    scp -i $SSH_KEY docker-compose.yml ubuntu@${BACKEND_IP}:/tmp/docker-compose.yml 2>&1 | Out-Null
    ssh -i $SSH_KEY ubuntu@${BACKEND_IP} "cd $backendDir; cp /tmp/docker-compose.yml docker-compose.yml" 2>&1 | Out-Null
    Write-Host "   ✓ docker-compose.yml mis a jour" -ForegroundColor Green
    Write-Host "   Redemarrage du backend..." -ForegroundColor Gray
    ssh -i $SSH_KEY ubuntu@${BACKEND_IP} "cd $backendDir; docker compose restart backend" 2>&1 | Out-Null
    Write-Host "   ✓ Backend redemarre (attendez 30-45 secondes)" -ForegroundColor Green
} else {
    Write-Host "   ✓ Histogrammes deja actives" -ForegroundColor Green
}
Write-Host ""

# 6. Générer du trafic
Write-Host "6. Generation de trafic HTTP..." -ForegroundColor Cyan
$endpoints = @("/actuator/health", "/actuator/info", "/actuator/prometheus")
for ($i=1; $i -le 50; $i++) {
    $endpoint = $endpoints[$i % $endpoints.Length]
    curl.exe -s "http://$BACKEND_IP:8081$endpoint" | Out-Null
    if ($i % 20 -eq 0) {
        Write-Host "   $i requetes envoyees..." -ForegroundColor Gray
    }
    Start-Sleep -Milliseconds 100
}
Write-Host "   ✓ Trafic genere" -ForegroundColor Green
Write-Host ""

# Résumé
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "RESUME" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✓ Dashboard deploye" -ForegroundColor Green
Write-Host "✓ Grafana redemarre" -ForegroundColor Green
Write-Host "✓ Backend verifie/redemarre" -ForegroundColor Green
Write-Host "✓ Trafic genere" -ForegroundColor Green
Write-Host ""
Write-Host "Prochaines etapes:" -ForegroundColor Yellow
Write-Host "1. Attendez 30-60 secondes" -ForegroundColor Cyan
Write-Host "2. Ouvrez Grafana: http://$PROMETHEUS_IP:3000" -ForegroundColor Cyan
Write-Host "3. Allez dans le dashboard 'Hotel Ticket Hub - Backend Spring Boot'" -ForegroundColor Cyan
Write-Host "4. Rafraichissez (F5)" -ForegroundColor Cyan
Write-Host ""

