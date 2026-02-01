# ✅ Correction : Affichage Automatique de Toutes les Métriques

## Problème résolu

Les panneaux **Response Time**, **Error Rate** et **Database Connections** affichaient "No data" dans Grafana, nécessitant de modifier les requêtes manuellement à chaque fois.

## Solutions appliquées

### 1. ✅ Requêtes Prometheus améliorées

Les requêtes ont été corrigées pour afficher **0** au lieu de "No data" quand il n'y a pas de données :

- **Response Time** : `histogram_quantile(...) or vector(0)`
- **Error Rate** : Gestion de la division par zéro avec `or vector(0)` et `or vector(1)`
- **Database Connections** : `hikari_connections_* or vector(0)`

### 2. ✅ Configuration Grafana

Ajout de `nullValueMode: "null as zero"` dans tous les panneaux pour afficher 0 au lieu de "No data".

### 3. ✅ Activation des histogrammes

Les métriques histogram ont été activées dans `docker-compose.yml` :
```yaml
- MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILES_HISTOGRAM_HTTP_SERVER_REQUESTS_ENABLED=true
- MANAGEMENT_METRICS_DISTRIBUTION_SLA_HTTP_SERVER_REQUESTS=10ms,50ms,100ms,200ms,500ms,1s,2s,5s
```

## Déploiement automatique

### Option 1 : Script PowerShell (Recommandé)

```powershell
cd hotel-ticket-hub-backend\scripts
.\fix-dashboard-auto-display.ps1
```

Ce script va :
- ✅ Déployer le dashboard corrigé sur la VM Monitoring
- ✅ Redémarrer Grafana
- ✅ Vérifier et activer les histogrammes dans le backend
- ✅ Redémarrer le backend
- ✅ Générer des requêtes HTTP pour créer des métriques

### Option 2 : Déploiement manuel

#### Étape 1 : Copier le dashboard sur la VM Monitoring

```bash
# Depuis votre machine locale
scp -i ~/.ssh/oumayma-key.pem \
  hotel-ticket-hub-backend/monitoring/grafana/dashboards/backend-spring-boot.json \
  ubuntu@16.170.74.58:/tmp/

# Sur la VM Monitoring
ssh -i ~/.ssh/oumayma-key.pem ubuntu@16.170.74.58
sudo cp /tmp/backend-spring-boot.json /opt/docker/monitoring/grafana/dashboards/
sudo chown root:root /opt/docker/monitoring/grafana/dashboards/backend-spring-boot.json
cd /opt/docker/monitoring && docker compose restart grafana
```

#### Étape 2 : Activer les histogrammes dans le backend

```bash
# Sur la VM Backend
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
cd /opt/hotel-ticket-hub-backend-staging

# Vérifier que docker-compose.yml contient les histogrammes
grep MANAGEMENT_METRICS_DISTRIBUTION docker-compose.yml

# Si absent, copier le fichier corrigé depuis votre machine locale
# Puis redémarrer
docker compose restart backend

# Attendre 30-45 secondes
docker logs hotel-ticket-hub-backend-staging --tail 20 | grep -i "started"
```

#### Étape 3 : Générer des métriques

```powershell
# Depuis votre machine locale
$endpoints = @("/actuator/health", "/actuator/info", "/actuator/prometheus")
for ($i=1; $i -le 30; $i++) {
    $endpoint = $endpoints[$i % $endpoints.Length]
    curl.exe -s "http://13.63.15.86:8081$endpoint" | Out-Null
    Start-Sleep -Milliseconds 200
}
```

## Vérification

1. **Ouvrez Grafana** : http://16.170.74.58:3000
2. **Allez dans le dashboard** : "Hotel Ticket Hub - Backend Spring Boot"
3. **Vérifiez que tous les panneaux affichent des données** :
   - ✅ Application Status : UP
   - ✅ HTTP Requests Rate : Affiche des lignes (même à 0)
   - ✅ JVM Heap Memory : Affiche des valeurs
   - ✅ JVM Memory Usage % : Affiche un pourcentage
   - ✅ **Response Time** : Affiche 0 ou des valeurs (plus de "No data")
   - ✅ **Error Rate** : Affiche 0% ou un pourcentage (plus de "No data")
   - ✅ JVM Threads : Affiche des valeurs
   - ✅ GC Pause Time : Affiche des valeurs
   - ✅ **Database Connections** : Affiche 0 ou des valeurs (plus de "No data")

## Comportement attendu

### Avant la correction
- ❌ "No data" dans Response Time
- ❌ "No data" dans Error Rate
- ❌ "No data" dans Database Connections
- ❌ Nécessité de modifier les requêtes manuellement

### Après la correction
- ✅ **Toutes les métriques s'affichent automatiquement**
- ✅ Affichage de **0** quand il n'y a pas de données (au lieu de "No data")
- ✅ Pas besoin de modifier les requêtes
- ✅ Le dashboard se met à jour automatiquement toutes les 30 secondes

## Notes importantes

1. **Premier affichage** : Après le déploiement, attendez 1-2 minutes pour que Prometheus collecte les données
2. **Refresh automatique** : Le dashboard se rafraîchit automatiquement toutes les 30 secondes
3. **Valeurs à 0** : C'est normal si certaines métriques affichent 0 :
   - Response Time = 0 : Pas de requêtes HTTP récentes
   - Error Rate = 0% : Pas d'erreurs (c'est bon !)
   - Database Connections = 0 : Pas de connexions actives (peut être normal selon l'utilisation)

## Dépannage

### Si "No data" persiste après 2 minutes

1. **Vérifier que le backend est UP dans Prometheus** :
   ```
   http://16.170.74.58:9090/targets
   ```
   Le target `staging-backend` doit être **UP** (vert)

2. **Vérifier les histogrammes** :
   ```bash
   curl http://13.63.15.86:8081/actuator/prometheus | grep http_server_requests_seconds_bucket
   ```
   Vous devriez voir des lignes avec `http_server_requests_seconds_bucket`

3. **Vérifier les métriques HikariCP** :
   ```bash
   curl http://13.63.15.86:8081/actuator/prometheus | grep hikari
   ```
   Vous devriez voir `hikari_connections_active`, `hikari_connections_max`, etc.

4. **Rafraîchir le dashboard** : Appuyez sur **F5** dans Grafana

### Si les histogrammes ne sont pas activés

Vérifiez les variables d'environnement dans le conteneur :
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
docker exec hotel-ticket-hub-backend-staging env | grep MANAGEMENT_METRICS_DISTRIBUTION
```

Vous devriez voir :
```
MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILES_HISTOGRAM_HTTP_SERVER_REQUESTS_ENABLED=true
MANAGEMENT_METRICS_DISTRIBUTION_SLA_HTTP_SERVER_REQUESTS=10ms,50ms,100ms,200ms,500ms,1s,2s,5s
```

Si absent, redéployez `docker-compose.yml` et redémarrez le backend.

## Fichiers modifiés

- ✅ `monitoring/grafana/dashboards/backend-spring-boot.json` - Requêtes corrigées
- ✅ `docker-compose.yml` - Histogrammes activés
- ✅ `scripts/fix-dashboard-auto-display.ps1` - Script de déploiement automatique

## Support

Si le problème persiste après avoir suivi toutes les étapes :
1. Vérifiez les logs Grafana : `docker logs grafana --tail 50`
2. Vérifiez les logs Prometheus : `docker logs prometheus --tail 50`
3. Vérifiez que Prometheus peut scraper le backend : http://16.170.74.58:9090/targets

