# ✅ Vérification finale : Prometheus et Supervision

## État actuel

✅ **PostgreSQL** : Configuré et accessible  
✅ **Backend** : Démarré avec succès  
✅ **Endpoint Prometheus** : Accessible depuis la VM Monitoring  
⏳ **Prometheus Scraping** : À vérifier

## Vérification finale

### 1. Vérifier dans Prometheus UI

**Ouvrez dans votre navigateur :**
- **Prometheus Targets** : http://16.170.74.58:9090/targets
- **Cherchez** : `staging-backend`
- **Statut attendu** : **UP** (vert) dans 15-30 secondes

### 2. Tester une requête Prometheus

**Prometheus Graph** : http://16.170.74.58:9090/graph

**Testez ces requêtes :**
```
up{job="staging-backend"}
```
**Résultat attendu :** `up{job="staging-backend"} 1`

```
jvm_memory_used_bytes{job="staging-backend"}
```
**Résultat attendu :** Des valeurs de métriques JVM

### 3. Vérifier depuis la VM Monitoring (optionnel)

```bash
# Sur la VM Monitoring
ssh -i ~/.ssh/oumayma-key.pem ubuntu@16.170.74.58

# Vérifier les targets
curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | select(.labels.job=="staging-backend")'

# Vérifier les métriques collectées
curl -s "http://localhost:9090/api/v1/query?query=up{job=\"staging-backend\"}" | jq '.data.result'
```

## Si le target est toujours UNKNOWN

### Vérifier les logs Prometheus

```bash
# Sur la VM Monitoring
docker logs prometheus --tail 50 | grep -iE "staging-backend|13.63.15.86|error"
```

### Redémarrer Prometheus

```bash
# Sur la VM Monitoring
docker restart prometheus

# Attendre 30 secondes
sleep 30

# Vérifier à nouveau
curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | select(.labels.job=="staging-backend")'
```

## Résumé de la configuration

### Architecture
- **VM Backend** (`13.63.15.86:8081`) : Application Spring Boot
- **VM Database** (`13.48.83.147:5432`) : PostgreSQL
- **VM Monitoring** (`16.170.74.58`) : Prometheus + Grafana

### Security Groups
- ✅ Backend : Port 8081 ouvert depuis Monitoring (`16.170.74.58/32`)
- ✅ Database : Port 5432 ouvert depuis Backend (`13.63.15.86/32`)
- ✅ Monitoring : Ports 9090 (Prometheus) et 3000 (Grafana) ouverts

### Endpoints
- **Backend Health** : http://13.63.15.86:8081/actuator/health
- **Backend Prometheus** : http://13.63.15.86:8081/actuator/prometheus
- **Prometheus UI** : http://16.170.74.58:9090
- **Grafana UI** : http://16.170.74.58:3000

## Prochaines étapes

Une fois que Prometheus scrape correctement :
1. ✅ Configurer des dashboards Grafana
2. ✅ Configurer des alertes Prometheus
3. ✅ Documenter les métriques importantes
4. ✅ Configurer des notifications

