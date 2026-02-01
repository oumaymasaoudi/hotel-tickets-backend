# Activer les métriques histogram pour Response Time et Error Rate

## Problème

Les panneaux "Response Time (95th percentile)" et "Error Rate %" affichent "No data" car les métriques histogram ne sont pas activées dans Spring Boot.

## Solution

### Option 1 : Via docker-compose.yml (Recommandé)

Ajoutez ces variables d'environnement dans `docker-compose.yml` :

```yaml
services:
  backend:
    environment:
      # ... autres variables ...
      - MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILES_HISTOGRAM_HTTP_SERVER_REQUESTS_ENABLED=true
      - MANAGEMENT_METRICS_DISTRIBUTION_SLA_HTTP_SERVER_REQUESTS=10ms,50ms,100ms,200ms,500ms,1s,2s,5s
```

### Option 2 : Via application.properties

Ajoutez ces lignes dans `src/main/resources/application.properties` :

```properties
# Activer les métriques de distribution (histogram) pour les requêtes HTTP
management.metrics.distribution.percentiles-histogram.http.server.requests.enabled=true
management.metrics.distribution.sla.http.server.requests=10ms,50ms,100ms,200ms,500ms,1s,2s,5s
```

## Déploiement

1. **Modifier docker-compose.yml** (Option 1 recommandée)
2. **Redémarrer le backend** :
   ```bash
   # Sur la VM Backend
   ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
   cd /opt/hotel-ticket-hub-backend-staging
   docker compose restart backend
   ```

3. **Attendre 30-60 secondes** pour que l'application redémarre

4. **Générer des requêtes HTTP** pour créer des métriques :
   ```powershell
   # Depuis votre machine locale
   $endpoints = @("/actuator/health", "/actuator/info", "/actuator/prometheus")
   for ($i=1; $i -le 50; $i++) {
       $endpoint = $endpoints[$i % $endpoints.Length]
       curl.exe -s "http://13.63.15.86:8081$endpoint" | Out-Null
       Start-Sleep -Milliseconds 200
   }
   ```

5. **Vérifier les métriques** :
   ```powershell
   .\scripts\check-http-metrics-simple.ps1
   ```

   Vous devriez maintenant voir des résultats pour `http_server_requests_seconds_bucket`

6. **Vérifier dans Grafana** :
   - Attendez 15-30 secondes (temps de scraping Prometheus)
   - Rafraîchissez le dashboard
   - Les panneaux "Response Time" et "Error Rate" devraient maintenant afficher des données

## Vérification

Dans Prometheus UI (http://16.170.74.58:9090/graph), testez :
```
http_server_requests_seconds_bucket{job="staging-backend"}
```

Si vous voyez des résultats, les histogrammes sont activés !

