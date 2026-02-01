# 📊 Guide : Vérification Prometheus et Supervision

## Architecture

- **VM Backend** (`13.63.15.86:8081`) : Application Spring Boot avec endpoint `/actuator/prometheus`
- **VM Monitoring** (`16.170.74.58`) : Stack Prometheus + Grafana qui scrape le backend

## Vérifications rapides

### 1. Vérifier l'endpoint Prometheus du backend

**Depuis votre machine locale :**
```powershell
curl http://13.63.15.86:8081/actuator/prometheus | head -20
```

**Résultat attendu :** Des métriques au format Prometheus (commençant par `# HELP`)

### 2. Vérifier Prometheus

**Depuis votre navigateur :**
- **Prometheus UI** : http://16.170.74.58:9090
- **Targets** : http://16.170.74.58:9090/targets
  - Vérifiez que `staging-backend` est **UP** (vert)
- **Graph** : http://16.170.74.58:9090/graph
  - Testez une requête : `up{job="staging-backend"}`

### 3. Vérifier Grafana

**Depuis votre navigateur :**
- **Grafana UI** : http://16.170.74.58:3000
- **Login par défaut** : `admin` / `admin` (à changer après première connexion)
- **Data Sources** : Vérifiez que Prometheus est configuré comme source de données

## Scripts de vérification automatique

### PowerShell (Windows)
```powershell
cd hotel-ticket-hub-backend
.\scripts\check-monitoring-stack.ps1
```

### Bash (Linux/WSL)
```bash
cd hotel-ticket-hub-backend
./scripts/verify-prometheus-monitoring.sh
```

## Vérifications détaillées

### A. Vérifier que Prometheus scrape le backend

1. **Accédez à** : http://16.170.74.58:9090/targets
2. **Cherchez** : `staging-backend` dans la liste
3. **Vérifiez** :
   - ✅ **State: UP** (vert) → Prometheus peut scraper
   - ❌ **State: DOWN** (rouge) → Problème de connexion

### B. Vérifier les métriques collectées

1. **Accédez à** : http://16.170.74.58:9090/graph
2. **Testez ces requêtes** :
   ```
   up{job="staging-backend"}
   jvm_memory_used_bytes{job="staging-backend"}
   http_server_requests_seconds_count{job="staging-backend"}
   ```

### C. Vérifier Grafana Dashboards

1. **Connectez-vous** : http://16.170.74.58:3000
2. **Allez dans** : Dashboards → Import
3. **Vérifiez** : Que des dashboards sont configurés pour visualiser les métriques

## Problèmes courants et solutions

### Problème 1 : Prometheus ne peut pas scraper le backend

**Symptômes :**
- Target `staging-backend` est DOWN dans Prometheus
- Erreur : `connection refused` ou `timeout`

**Solutions :**
1. Vérifier que le backend est en cours d'exécution :
   ```bash
   ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
   docker ps | grep hotel-ticket-hub-backend-staging
   ```

2. Vérifier que l'endpoint Prometheus est accessible :
   ```bash
   curl http://localhost:8081/actuator/prometheus | head -5
   ```

3. Vérifier le Security Group AWS :
   - Le port 8081 doit être ouvert depuis la VM Monitoring (`16.170.74.58`)
   - Ou autoriser depuis `0.0.0.0/0` pour le staging

### Problème 2 : Aucune métrique JVM collectée

**Symptômes :**
- Target est UP mais aucune métrique JVM visible

**Solutions :**
1. Vérifier que Micrometer Prometheus est dans les dépendances :
   ```bash
   # Sur la VM backend
   docker exec hotel-ticket-hub-backend-staging sh -c "jar -tf /app/app.jar | grep micrometer"
   ```

2. Vérifier les variables d'environnement :
   ```bash
   docker exec hotel-ticket-hub-backend-staging env | grep -iE "PROMETHEUS|EXPOSURE"
   ```
   Doit afficher :
   ```
   MANAGEMENT_ENDPOINT_PROMETHEUS_ENABLED=true
   MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,prometheus,metrics
   ```

### Problème 3 : Prometheus n'est pas accessible

**Symptômes :**
- Impossible d'accéder à http://16.170.74.58:9090

**Solutions :**
1. Vérifier que le conteneur Prometheus est en cours d'exécution :
   ```bash
   ssh -i ~/.ssh/oumayma-key.pem ubuntu@16.170.74.58
   docker ps | grep prometheus
   ```

2. Vérifier les logs :
   ```bash
   docker logs prometheus
   ```

3. Vérifier le Security Group AWS :
   - Le port 9090 doit être ouvert depuis votre IP
   - Ou autoriser depuis `0.0.0.0/0` pour le staging

## Commandes utiles

### Sur la VM Backend
```bash
# Vérifier le conteneur
docker ps | grep backend

# Vérifier l'endpoint Prometheus
curl http://localhost:8081/actuator/prometheus | head -10

# Vérifier les variables d'environnement
docker exec hotel-ticket-hub-backend-staging env | grep -iE "PROMETHEUS|EXPOSURE"
```

### Sur la VM Monitoring
```bash
# Vérifier les conteneurs
docker ps | grep -E "prometheus|grafana"

# Vérifier la configuration Prometheus
docker exec prometheus cat /etc/prometheus/prometheus.yml

# Vérifier les logs Prometheus
docker logs prometheus --tail 50
```

## Métriques importantes à surveiller

1. **JVM Memory** : `jvm_memory_used_bytes`
2. **HTTP Requests** : `http_server_requests_seconds_count`
3. **Database Connections** : `hikari_connections_active`
4. **Application Health** : `health_status`
5. **System Metrics** : `node_memory_MemAvailable_bytes`

## Prochaines étapes

Une fois que tout fonctionne :
1. ✅ Configurer des dashboards Grafana
2. ✅ Configurer des alertes Prometheus
3. ✅ Documenter les métriques importantes
4. ✅ Configurer des notifications (email, Slack, etc.)

