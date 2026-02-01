# 🚀 Guide de Déploiement - Dashboard Corrigé

## Étapes à suivre

### 1️⃣ Vérifier que le backend a les histogrammes activés

**Sur la VM Backend :**
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
cd /opt/hotel-ticket-hub-backend-staging

# Vérifier que docker-compose.yml contient la bonne propriété
grep "MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILES_HISTOGRAM_HTTP_SERVER_REQUESTS=true" docker-compose.yml
```

**Si la propriété n'est pas là :**
```bash
# Copier le docker-compose.yml corrigé depuis votre machine locale
# (vous devrez le faire manuellement ou utiliser le script)
```

**Redémarrer le backend :**
```bash
docker compose restart backend
# Attendre 30-45 secondes
docker logs hotel-ticket-hub-backend-staging --tail 20 | grep -i "started"
```

### 2️⃣ Vérifier que les buckets HTTP existent

**Depuis votre machine locale (PowerShell) :**
```powershell
# Tester les buckets
curl.exe -s "http://13.63.15.86:8081/actuator/prometheus" | Select-String "http_server_requests_seconds_bucket" | Select-Object -First 5
```

**Si vous ne voyez rien, générez du trafic :**
```powershell
for ($i=1; $i -le 50; $i++) {
    curl.exe -s "http://13.63.15.86:8081/actuator/health" | Out-Null
    Start-Sleep -Milliseconds 200
}
```

**Puis retestez :**
```powershell
curl.exe -s "http://13.63.15.86:8081/actuator/prometheus" | Select-String "http_server_requests_seconds_bucket" | Select-Object -First 5
```

**Vous devriez voir des lignes comme :**
```
http_server_requests_seconds_bucket{exception="None",instance="backend-vm",job="staging-backend",le="0.005",method="GET",outcome="SUCCESS",status="200",uri="/actuator/health"} 50.0
```

### 3️⃣ Déployer le dashboard corrigé sur la VM Monitoring

**Option A : Script automatique (Recommandé)**
```powershell
cd hotel-ticket-hub-backend\scripts
.\fix-dashboard-auto-display.ps1
```

**Option B : Manuellement**
```bash
# Depuis votre machine locale
scp -i ~/.ssh/oumayma-key.pem \
  hotel-ticket-hub-backend/monitoring/grafana/dashboards/backend-spring-boot.json \
  ubuntu@16.170.74.58:/tmp/

# Sur la VM Monitoring
ssh -i ~/.ssh/oumayma-key.pem ubuntu@16.170.74.58
sudo cp /tmp/backend-spring-boot.json /opt/docker/monitoring/grafana/dashboards/
sudo chown root:root /opt/docker/monitoring/grafana/dashboards/backend-spring-boot.json
cd /opt/docker/monitoring
docker compose restart grafana
```

### 4️⃣ Vérifier dans Grafana

1. **Ouvrez Grafana** : http://16.170.74.58:3000
2. **Allez dans le dashboard** : "Hotel Ticket Hub - Backend Spring Boot"
3. **Rafraîchissez** : Appuyez sur **F5** ou cliquez sur le bouton refresh
4. **Vérifiez les panneaux** :
   - ✅ Application Status : UP
   - ✅ HTTP Requests Rate : Affiche des lignes
   - ✅ JVM Memory : Affiche des valeurs
   - ✅ **Response Time (95th percentile)** : Affiche 0 ou des valeurs (plus de "No data")
   - ✅ **Error Rate %** : Affiche 0% ou un pourcentage (plus de "No data")
   - ✅ **Database Connections** : Affiche 0 ou des valeurs (plus de "No data")

### 5️⃣ Si "No data" persiste

**Vérifier les métriques disponibles :**
```powershell
.\scripts\verify-metrics-names.ps1
```

**Vérifier que Prometheus peut scraper le backend :**
```
http://16.170.74.58:9090/targets
```
Le target `staging-backend` doit être **UP** (vert)

**Générer plus de trafic :**
```powershell
# Générer du trafic sur différents endpoints
$endpoints = @("/actuator/health", "/actuator/info", "/actuator/prometheus")
for ($i=1; $i -le 100; $i++) {
    $endpoint = $endpoints[$i % $endpoints.Length]
    curl.exe -s "http://13.63.15.86:8081$endpoint" | Out-Null
    if ($i % 20 -eq 0) { Write-Host "$i requetes envoyees..." }
    Start-Sleep -Milliseconds 100
}
```

## Checklist rapide

- [ ] Backend redémarré avec la propriété histogram activée
- [ ] Buckets HTTP vérifiés (avec `curl` ou le script)
- [ ] Dashboard déployé sur la VM Monitoring
- [ ] Grafana redémarré
- [ ] Trafic HTTP généré (au moins 30-50 requêtes)
- [ ] Dashboard rafraîchi dans Grafana
- [ ] Tous les panneaux affichent des données (ou 0, mais pas "No data")

## Commandes rapides (PowerShell)

```powershell
# 1. Vérifier les métriques
.\scripts\verify-metrics-names.ps1

# 2. Déployer le dashboard
.\scripts\fix-dashboard-auto-display.ps1

# 3. Générer du trafic
for ($i=1; $i -le 50; $i++) {
    curl.exe -s "http://13.63.15.86:8081/actuator/health" | Out-Null
    Start-Sleep -Milliseconds 200
}
```

## Support

Si ça ne fonctionne toujours pas après ces étapes, envoyez-moi :
1. La sortie de : `curl -s http://13.63.15.86:8081/actuator/prometheus | grep http_server_requests_seconds_bucket | head -n 20`
2. La sortie de : `curl -s http://13.63.15.86:8081/actuator/prometheus | grep hikaricp | head -n 10`
3. Un screenshot du dashboard Grafana

