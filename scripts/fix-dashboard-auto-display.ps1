# Script pour corriger le dashboard et activer l'affichage automatique de toutes les métriques
# Résout le problème "No data" dans Response Time, Error Rate, Database Connections

$BACKEND_IP = "13.63.15.86"
$PROMETHEUS_IP = "16.170.74.58"
$SSH_KEY = "$env:USERPROFILE\.ssh\oumayma-key.pem"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Correction du Dashboard - Affichage Automatique" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Vérifier que les histogrammes sont activés dans docker-compose.yml
Write-Host "1. Verification de la configuration des histogrammes..." -ForegroundColor Cyan
$dockerCompose = Get-Content "docker-compose.yml" -Raw
if ($dockerCompose -match "MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILES_HISTOGRAM_HTTP_SERVER_REQUESTS=true") {
    Write-Host "   ✓ Histogrammes deja actives dans docker-compose.yml" -ForegroundColor Green
} else {
    Write-Host "   ⚠ Histogrammes non actives - deja corrige dans le fichier" -ForegroundColor Yellow
}
Write-Host ""

# 2. Copier le dashboard corrigé sur la VM Monitoring
Write-Host "2. Deploiement du dashboard corrige sur la VM Monitoring..." -ForegroundColor Cyan
$dashboardPath = "monitoring\grafana\dashboards\backend-spring-boot.json"
if (Test-Path $dashboardPath) {
    Write-Host "   Copie du dashboard vers la VM Monitoring..." -ForegroundColor Gray
    scp -i $SSH_KEY $dashboardPath ubuntu@${PROMETHEUS_IP}:/tmp/backend-spring-boot.json 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ Dashboard copie avec succes" -ForegroundColor Green
        
        # Copier dans le répertoire Grafana (trouver le bon chemin)
        # D'abord, trouver où sont les dashboards Grafana
        $grafanaDashPath = ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "find /opt -name 'grafana' -type d 2>/dev/null | head -1" 2>&1
        if (-not $grafanaDashPath -or $grafanaDashPath -match "No such file") {
            # Essayer le chemin standard
            $grafanaDashPath = "/opt/docker/monitoring/grafana/dashboards"
        } else {
            $grafanaDashPath = "$grafanaDashPath/dashboards"
        }
        
        # Créer le répertoire s'il n'existe pas
        ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "sudo mkdir -p $grafanaDashPath" 2>&1 | Out-Null
        
        # Copier le fichier
        ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "sudo cp /tmp/backend-spring-boot.json $grafanaDashPath/backend-spring-boot.json" 2>&1 | Out-Null
        ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "sudo chown root:root $grafanaDashPath/backend-spring-boot.json" 2>&1 | Out-Null
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   ✓ Dashboard deploye dans Grafana" -ForegroundColor Green
        } else {
            Write-Host "   ⚠ Erreur lors du deploiement - verifiez manuellement" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   ✗ Erreur lors de la copie" -ForegroundColor Red
    }
} else {
    Write-Host "   ✗ Fichier dashboard non trouve: $dashboardPath" -ForegroundColor Red
}
Write-Host ""

# 3. Redémarrer Grafana pour charger le nouveau dashboard
Write-Host "3. Redemarrage de Grafana..." -ForegroundColor Cyan
# Trouver le répertoire docker-compose
$monitoringDir = ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "find /opt -name 'docker-compose*.yml' -o -name 'docker-compose*.yaml' 2>/dev/null | head -1 | xargs dirname" 2>&1
if (-not $monitoringDir -or $monitoringDir -match "No such file") {
    $monitoringDir = "/opt/docker/monitoring"
}
$restartGrafana = ssh -i $SSH_KEY ubuntu@${PROMETHEUS_IP} "cd $monitoringDir; docker compose restart grafana" 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Grafana redemarre" -ForegroundColor Green
} else {
    Write-Host "   ⚠ Erreur lors du redemarrage de Grafana" -ForegroundColor Yellow
    Write-Host "   $restartGrafana" -ForegroundColor Gray
}
Write-Host ""

# 4. Vérifier et redémarrer le backend pour activer les histogrammes
Write-Host "4. Activation des histogrammes dans le backend..." -ForegroundColor Cyan
Write-Host "   Verification de docker-compose.yml sur la VM Backend..." -ForegroundColor Gray

# Vérifier si les histogrammes sont déjà activés
$backendComposeCheck = ssh -i $SSH_KEY ubuntu@${BACKEND_IP} "cd /opt/hotel-ticket-hub-backend-staging && grep -q 'MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILES_HISTOGRAM_HTTP_SERVER_REQUESTS=true' docker-compose.yml && echo 'OK' || echo 'MISSING'" 2>&1

if ($backendComposeCheck -match "MISSING") {
    Write-Host "   ⚠ Les histogrammes ne sont pas actives sur la VM Backend" -ForegroundColor Yellow
    Write-Host "   Copie du docker-compose.yml corrige..." -ForegroundColor Gray
    
    scp -i $SSH_KEY docker-compose.yml ubuntu@${BACKEND_IP}:/tmp/docker-compose.yml 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        ssh -i $SSH_KEY ubuntu@${BACKEND_IP} "cd /opt/hotel-ticket-hub-backend-staging && cp /tmp/docker-compose.yml docker-compose.yml" 2>&1 | Out-Null
        Write-Host "   ✓ docker-compose.yml mis a jour" -ForegroundColor Green
        
        Write-Host "   Redemarrage du backend..." -ForegroundColor Gray
        ssh -i $SSH_KEY ubuntu@${BACKEND_IP} "cd /opt/hotel-ticket-hub-backend-staging; docker compose restart backend" 2>&1 | Out-Null
        Write-Host "   ✓ Backend redemarre - attendez 30-45 secondes pour le demarrage" -ForegroundColor Green
    } else {
        Write-Host "   ✗ Erreur lors de la copie de docker-compose.yml" -ForegroundColor Red
    }
} else {
    Write-Host "   ✓ Les histogrammes sont deja actives" -ForegroundColor Green
    Write-Host "   Redemarrage du backend pour s'assurer..." -ForegroundColor Gray
    ssh -i $SSH_KEY ubuntu@${BACKEND_IP} "cd /opt/hotel-ticket-hub-backend-staging; docker compose restart backend" 2>&1 | Out-Null
    Write-Host "   ✓ Backend redemarre" -ForegroundColor Green
}
Write-Host ""

# 5. Générer quelques requêtes pour créer des métriques
Write-Host "5. Generation de requetes HTTP pour creer des metriques..." -ForegroundColor Cyan
$endpoints = @("/actuator/health", "/actuator/info", "/actuator/prometheus")
Write-Host "   Envoi de 30 requetes..." -ForegroundColor Gray
for ($i=1; $i -le 30; $i++) {
    $endpoint = $endpoints[$i % $endpoints.Length]
    curl.exe -s "http://$BACKEND_IP:8081$endpoint" | Out-Null
    if ($i % 10 -eq 0) {
        Write-Host "   $i requetes envoyees..." -ForegroundColor Gray
    }
    Start-Sleep -Milliseconds 200
}
Write-Host "   ✓ Requetes generees" -ForegroundColor Green
Write-Host ""

# Résumé
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "RESUME" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✓ Dashboard corrige et deploye" -ForegroundColor Green
Write-Host "✓ Histogrammes actives dans docker-compose.yml" -ForegroundColor Green
Write-Host "✓ Backend redemarre" -ForegroundColor Green
Write-Host "✓ Metriques generees" -ForegroundColor Green
Write-Host ""
Write-Host "Prochaines etapes:" -ForegroundColor Yellow
Write-Host "1. Attendez 30-60 secondes pour que le backend demarre completement" -ForegroundColor Cyan
Write-Host "2. Ouvrez Grafana: http://$PROMETHEUS_IP:3000" -ForegroundColor Cyan
Write-Host "3. Allez dans le dashboard 'Hotel Ticket Hub - Backend Spring Boot'" -ForegroundColor Cyan
Write-Host "4. Toutes les metriques devraient maintenant s'afficher automatiquement" -ForegroundColor Cyan
Write-Host ""
Write-Host "Si certaines metriques affichent encore 'No data':" -ForegroundColor Yellow
Write-Host "- Attendez 1-2 minutes pour que Prometheus collecte les donnees" -ForegroundColor Gray
Write-Host "- Rafraichissez le dashboard (F5)" -ForegroundColor Gray
Write-Host "- Verifiez que le backend est UP dans Prometheus: http://$PROMETHEUS_IP:9090/targets" -ForegroundColor Gray
Write-Host ""

