# Script pour déboguer la requête JVM Memory Usage
$MONITORING_IP = "16.170.74.58"

Write-Host "Debogage de la requete JVM Memory Usage..." -ForegroundColor Yellow
Write-Host ""

# 1. Vérifier jvm_memory_used_bytes
Write-Host "1. Verification de jvm_memory_used_bytes..." -ForegroundColor Cyan
$uri1 = "http://$MONITORING_IP" + ":9090/api/v1/query?query=jvm_memory_used_bytes{job=`"staging-backend`",area=`"heap`"}"
try {
    $response1 = Invoke-RestMethod -Uri $uri1 -Method Get
    Write-Host "   Resultats: $($response1.data.result.Count)" -ForegroundColor Green
    $response1.data.result | ForEach-Object {
        Write-Host "     - Instance: $($_.metric.instance), Value: $($_.value[1])" -ForegroundColor Gray
    }
} catch {
    Write-Host "   Erreur: $_" -ForegroundColor Red
}
Write-Host ""

# 2. Vérifier jvm_memory_max_bytes
Write-Host "2. Verification de jvm_memory_max_bytes..." -ForegroundColor Cyan
$uri2 = "http://$MONITORING_IP" + ":9090/api/v1/query?query=jvm_memory_max_bytes{job=`"staging-backend`",area=`"heap`"}"
try {
    $response2 = Invoke-RestMethod -Uri $uri2 -Method Get
    Write-Host "   Resultats: $($response2.data.result.Count)" -ForegroundColor Green
    $response2.data.result | ForEach-Object {
        Write-Host "     - Instance: $($_.metric.instance), Value: $($_.value[1])" -ForegroundColor Gray
    }
} catch {
    Write-Host "   Erreur: $_" -ForegroundColor Red
}
Write-Host ""

# 3. Tester la requête complète
Write-Host "3. Test de la requete complete..." -ForegroundColor Cyan
$uri3 = "http://$MONITORING_IP" + ":9090/api/v1/query?query=avg((jvm_memory_used_bytes{job=`"staging-backend`",area=`"heap`"} / jvm_memory_max_bytes{job=`"staging-backend`",area=`"heap`"}) * 100)"
try {
    $response3 = Invoke-RestMethod -Uri $uri3 -Method Get
    Write-Host "   Resultats: $($response3.data.result.Count)" -ForegroundColor Green
    $response3.data.result | ForEach-Object {
        Write-Host "     - Value: $($_.value[1])" -ForegroundColor $(if ([double]$_.value[1] -ge 0 -and [double]$_.value[1] -le 100) { "Green" } else { "Red" })
    }
} catch {
    Write-Host "   Erreur: $_" -ForegroundColor Red
}
Write-Host ""

# 4. Tester une requête alternative (par instance)
Write-Host "4. Test d'une requete alternative (par instance)..." -ForegroundColor Cyan
$uri4 = "http://$MONITORING_IP" + ":9090/api/v1/query?query=(jvm_memory_used_bytes{job=`"staging-backend`",area=`"heap`"} / jvm_memory_max_bytes{job=`"staging-backend`",area=`"heap`"}) * 100"
try {
    $response4 = Invoke-RestMethod -Uri $uri4 -Method Get
    Write-Host "   Resultats: $($response4.data.result.Count)" -ForegroundColor Green
    $response4.data.result | ForEach-Object {
        $value = [double]$_.value[1]
        Write-Host "     - Instance: $($_.metric.instance), Value: $value%" -ForegroundColor $(if ($value -ge 0 -and $value -le 100) { "Green" } else { "Red" })
    }
} catch {
    Write-Host "   Erreur: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "Si les valeurs sont negatives ou enormes, il y a un probleme avec les donnees." -ForegroundColor Yellow
Write-Host "Si les valeurs sont correctes mais avg() ne fonctionne pas, utilisez la requete alternative." -ForegroundColor Yellow
Write-Host ""

