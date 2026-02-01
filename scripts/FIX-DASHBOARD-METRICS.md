# Correction des métriques du Dashboard

## Problèmes identifiés

1. **"No data" pour Response Time, Error Rate, Database Connections**
   - Ces métriques peuvent ne pas être disponibles si :
     - Pas assez de requêtes HTTP ont été faites
     - Les métriques n'existent pas encore dans Prometheus
     - Les noms de métriques sont incorrects

2. **Valeurs erronées dans JVM Memory Usage %**
   - Les gauges montrent des valeurs négatives énormes
   - Probablement dû à plusieurs instances ou à un calcul incorrect

## Solutions

### 1. Vérifier les métriques disponibles

**Sur la VM Monitoring :**
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@16.170.74.58

# Vérifier les métriques HTTP
curl -s "http://localhost:9090/api/v1/label/__name__/values" | jq -r '.data[]' | grep -i "http"

# Vérifier les métriques HikariCP
curl -s "http://localhost:9090/api/v1/label/__name__/values" | jq -r '.data[]' | grep -i "hikari"
```

### 2. Tester les requêtes dans Prometheus UI

1. Ouvrez : http://16.170.74.58:9090/graph
2. Testez ces requêtes :
   ```
   http_server_requests_seconds_count{job="staging-backend"}
   http_server_requests_seconds_bucket{job="staging-backend"}
   hikari_connections_active{job="staging-backend"}
   ```

### 3. Corriger le dashboard JVM Memory Usage %

Le problème vient probablement du fait que la requête retourne plusieurs séries. Il faut utiliser `sum()` ou `avg()` :

**Requête actuelle (problématique) :**
```
(jvm_memory_used_bytes{job="staging-backend",area="heap"} / jvm_memory_max_bytes{job="staging-backend",area="heap"}) * 100
```

**Requête corrigée :**
```
avg((jvm_memory_used_bytes{job="staging-backend",area="heap"} / jvm_memory_max_bytes{job="staging-backend",area="heap"}) * 100)
```

### 4. Si les métriques n'existent pas

**Pour Response Time et Error Rate :**
- Ces métriques nécessitent des requêtes HTTP
- Faites quelques requêtes vers l'API pour générer des métriques :
  ```bash
  curl http://13.63.15.86:8081/actuator/health
  curl http://13.63.15.86:8081/api/tickets
  ```

**Pour Database Connections :**
- Vérifiez que HikariCP expose ses métriques
- Dans `application.properties`, assurez-vous que :
  ```properties
  management.metrics.export.prometheus.enabled=true
  ```

### 5. Alternative : Utiliser des métriques disponibles

Si certaines métriques n'existent pas, vous pouvez :
1. **Modifier le dashboard** dans Grafana (Edit → Modifier la requête)
2. **Utiliser des métriques alternatives** disponibles
3. **Attendre** que plus de données soient collectées

## Commandes rapides

```bash
# Vérifier toutes les métriques disponibles
curl -s "http://localhost:9090/api/v1/series?match[]={job=\"staging-backend\"}" | jq -r '.data[].__name__' | sort | uniq

# Générer des requêtes HTTP pour créer des métriques
for i in {1..10}; do curl -s http://13.63.15.86:8081/actuator/health > /dev/null; done

# Vérifier les métriques après
curl -s "http://localhost:9090/api/v1/query?query=http_server_requests_seconds_count{job=\"staging-backend\"}" | jq '.data.result | length'
```

