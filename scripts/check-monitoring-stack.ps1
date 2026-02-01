# Script PowerShell pour vérifier la stack de monitoring
# Usage: .\check-monitoring-stack.ps1 [MONITORING_VM_IP]

param(
    [string]$MonitoringVmIp = "16.170.74.58",
    [string]$BackendIp = "13.63.15.86",
    [string]$BackendPort = "8081"
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Verification Prometheus et Supervision" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "  VM Monitoring: $MonitoringVmIp"
Write-Host "  VM Backend: ${BackendIp}:${BackendPort}"
Write-Host ""

# 1. Verifier Prometheus
Write-Host "1. Verification de Prometheus..." -ForegroundColor Yellow
try {
    $prometheusHealth = Invoke-WebRequest -Uri "http://${MonitoringVmIp}:9090/-/healthy" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
    if ($prometheusHealth.StatusCode -eq 200) {
        Write-Host "   [OK] Prometheus est accessible sur http://${MonitoringVmIp}:9090" -ForegroundColor Green
    }
} catch {
    Write-Host "   [ERREUR] Prometheus n'est pas accessible" -ForegroundColor Red
    Write-Host "   Verifiez que:" -ForegroundColor Yellow
    Write-Host "   - Le conteneur Prometheus est en cours d'execution"
    Write-Host "   - Le port 9090 est ouvert dans le Security Group AWS"
    exit 1
}
Write-Host ""

# 2. Verifier Grafana
Write-Host "2. Verification de Grafana..." -ForegroundColor Yellow
try {
    $grafanaHealth = Invoke-WebRequest -Uri "http://${MonitoringVmIp}:3000/api/health" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
    if ($grafanaHealth.StatusCode -eq 200) {
        Write-Host "   [OK] Grafana est accessible sur http://${MonitoringVmIp}:3000" -ForegroundColor Green
    }
} catch {
    Write-Host "   [ATTENTION] Grafana n'est pas accessible" -ForegroundColor Yellow
    Write-Host "   Verifiez que le port 3000 est ouvert dans le Security Group AWS" -ForegroundColor Yellow
}
Write-Host ""

# 3. Verifier l'endpoint Prometheus du backend
Write-Host "3. Verification de l'endpoint Prometheus du backend..." -ForegroundColor Yellow
try {
    $backendMetrics = Invoke-WebRequest -Uri "http://${BackendIp}:${BackendPort}/actuator/prometheus" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
    if ($backendMetrics.Content -match "# HELP") {
        Write-Host "   [OK] L'endpoint /actuator/prometheus du backend est accessible" -ForegroundColor Green
        Write-Host "   [OK] Les metriques sont disponibles" -ForegroundColor Green
    }
} catch {
    Write-Host "   [ERREUR] L'endpoint /actuator/prometheus du backend n'est pas accessible" -ForegroundColor Red
    Write-Host "   Verifiez que:" -ForegroundColor Yellow
    Write-Host "   - Le backend est en cours d'execution"
    Write-Host "   - L'endpoint Prometheus est active"
    Write-Host "   - Le port $BackendPort est ouvert dans le Security Group AWS"
    exit 1
}
Write-Host ""

# 4. Verifier que Prometheus peut scraper le backend
Write-Host "4. Verification du scraping Prometheus..." -ForegroundColor Yellow
try {
    $targets = Invoke-RestMethod -Uri "http://${MonitoringVmIp}:9090/api/v1/targets" -TimeoutSec 5 -ErrorAction Stop
    $backendTarget = $targets.data.activeTargets | Where-Object { $_.labels.job -eq "staging-backend" }
    
    if ($backendTarget) {
        Write-Host "   [OK] La cible 'staging-backend' est configuree dans Prometheus" -ForegroundColor Green
        
        if ($backendTarget.health -eq "up") {
            Write-Host "   [OK] La cible 'staging-backend' est UP (Prometheus peut scraper)" -ForegroundColor Green
        } else {
            Write-Host "   [ATTENTION] La cible 'staging-backend' est DOWN" -ForegroundColor Yellow
            Write-Host "   Verifiez les logs de Prometheus pour plus de details" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   [ERREUR] La cible 'staging-backend' n'est pas trouvee dans Prometheus" -ForegroundColor Red
        Write-Host "   Verifiez la configuration prometheus.yml" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   [ATTENTION] Impossible de verifier les cibles Prometheus" -ForegroundColor Yellow
    Write-Host "   Erreur: $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

# 5. Verifier les metriques collectees
Write-Host "5. Verification des metriques collectees..." -ForegroundColor Yellow
try {
    $upQuery = Invoke-RestMethod -Uri "http://${MonitoringVmIp}:9090/api/v1/query?query=up{job=`"staging-backend`"}" -TimeoutSec 5 -ErrorAction Stop
    if ($upQuery.data.result.Count -gt 0) {
        Write-Host "   [OK] Des metriques sont collectees pour le backend" -ForegroundColor Green
        
        # Verifier les metriques JVM
        $jvmQuery = Invoke-RestMethod -Uri "http://${MonitoringVmIp}:9090/api/v1/query?query=jvm_memory_used_bytes{job=`"staging-backend`"}" -TimeoutSec 5 -ErrorAction Stop
        if ($jvmQuery.data.result.Count -gt 0) {
            Write-Host "   [OK] Les metriques JVM sont collectees ($($jvmQuery.data.result.Count) metriques trouvees)" -ForegroundColor Green
        } else {
            Write-Host "   [ATTENTION] Aucune metrique JVM trouvee" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   [ATTENTION] Aucune metrique collectee pour le backend" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   [ATTENTION] Impossible de verifier les metriques" -ForegroundColor Yellow
    Write-Host "   Erreur: $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

# Resume
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Resume" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Prometheus: http://${MonitoringVmIp}:9090" -ForegroundColor Cyan
Write-Host "Grafana: http://${MonitoringVmIp}:3000" -ForegroundColor Cyan
Write-Host "Backend Prometheus: http://${BackendIp}:${BackendPort}/actuator/prometheus" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pour verifier manuellement:" -ForegroundColor Yellow
Write-Host "1. Prometheus Targets: http://${MonitoringVmIp}:9090/targets" -ForegroundColor Cyan
Write-Host "2. Prometheus Graph: http://${MonitoringVmIp}:9090/graph" -ForegroundColor Cyan
Write-Host "3. Grafana Dashboards: http://${MonitoringVmIp}:3000" -ForegroundColor Cyan
Write-Host ""

