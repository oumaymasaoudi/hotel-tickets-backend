# 📋 Ce qu'il faut ajouter après la configuration Loki

**Date:** 8 Février 2026

---

## ✅ Étape 1: Vérifier que Loki fonctionne

### Tests de base

```bash
# 1. Vérifier que Loki est démarré
ssh ubuntu@16.170.74.58
docker ps | grep loki

# 2. Tester l'endpoint
curl http://localhost:3100/ready
# Résultat: "ready"

# 3. Vérifier Promtail
docker ps | grep promtail
docker logs promtail --tail 20
```

### Test dans Grafana

1. Aller sur http://16.170.74.58:3000
2. **Connections > Data sources > Loki**
3. Cliquer sur **Save & test**
4. Vérifier: "Data source is working" ✅

---

## 📊 Étape 2: Créer des Dashboards de Logs

### Dashboard 1: Logs Backend en temps réel

**Création:**
1. **Dashboards > New dashboard**
2. **Add visualization > Logs**
3. **Datasource:** Loki
4. **Requête:** `{container="hotel-ticket-hub-backend-staging"}`
5. **Refresh:** 5s

**Panels à ajouter:**
- ✅ Panel Logs: Tous les logs
- ✅ Panel Stat: Comptage d'erreurs
- ✅ Panel Pie Chart: Logs par niveau
- ✅ Panel Logs: Erreurs uniquement

### Dashboard 2: Analyse des erreurs

**Requêtes utiles:**
```logql
# Toutes les erreurs
{container="hotel-ticket-hub-backend-staging"} |= "ERROR"

# Erreurs avec stack trace
{container="hotel-ticket-hub-backend-staging"} |= "ERROR" | json

# Comptage d'erreurs par minute
sum(count_over_time({container="hotel-ticket-hub-backend-staging"} |= "ERROR" [1m]))
```

---

## 🚨 Étape 3: Configurer des Alertes

### Alerte 1: Trop d'erreurs

**Configuration:**
1. **Alerting > Alert rules > New alert rule**
2. **Query:**
   ```logql
   sum(count_over_time({container="hotel-ticket-hub-backend-staging"} |= "ERROR" [5m]))
   ```
3. **Condition:** `WHEN last() OF A IS ABOVE 10`
4. **Evaluation:** Every 1m, For 2m
5. **Notifications:** Email/Slack

### Alerte 2: Service down (pas de logs)

**Configuration:**
1. **Query:**
   ```logql
   sum(count_over_time({container="hotel-ticket-hub-backend-staging"}[5m]))
   ```
2. **Condition:** `WHEN last() OF A IS BELOW 1`
3. **Message:** "Backend ne génère plus de logs - service peut être down"

### Alerte 3: Erreurs critiques

**Configuration:**
1. **Query:**
   ```logql
   {container="hotel-ticket-hub-backend-staging"} |= "OutOfMemoryError" or {container="hotel-ticket-hub-backend-staging"} |= "NullPointerException"
   ```
2. **Condition:** `WHEN count() OF A IS ABOVE 0`
3. **Message:** "Erreur critique détectée dans les logs"

---

## 🔧 Étape 4: Améliorer la configuration Promtail

### Ajouter des labels personnalisés

Éditer `promtail/promtail-config.yml`:

```yaml
scrape_configs:
  - job_name: docker
    docker_sd_configs:
      - host: unix:///var/run/docker.sock
        refresh_interval: 5s
    relabel_configs:
      # Ajouter des labels
      - source_labels: [__meta_docker_container_name]
        regex: '/(.*)'
        target_label: container
      - source_labels: [__meta_docker_container_label_com_docker_compose_service]
        target_label: service
      - target_label: environment
        replacement: staging
```

### Filtrer les logs inutiles

```yaml
pipeline_stages:
  - drop:
      expression: '.*health.*'  # Ignorer les health checks
```

---

## 📈 Étape 5: Corrélation Logs-Métriques

### Lier les logs aux métriques Prometheus

Dans Grafana, créer des **derived fields**:

1. **Data sources > Loki > Settings**
2. **Derived fields:**
   - **Name:** TraceID
   - **Regex:** `traceID=(\w+)`
   - **Datasource:** Prometheus
   - **URL:** `$${__value.raw}`

Cela permet de cliquer sur un log et voir les métriques associées.

---

## 🎯 Étape 6: Dashboards avancés

### Dashboard: Vue d'ensemble complète

**Panels:**
1. **Métriques Prometheus:** CPU, Memory, Requests
2. **Logs Loki:** Logs en temps réel
3. **Corrélation:** Erreurs vs métriques
4. **Top erreurs:** Table des erreurs les plus fréquentes

### Dashboard: Performance et logs

**Requêtes combinées:**
- Métriques: `http_server_requests_seconds_count{application="hotel-ticket-hub-backend"}`
- Logs: `{container="hotel-ticket-hub-backend-staging"} |= "slow"`
- Corrélation: Lier les requêtes lentes aux logs

---

## 📝 Checklist Complète

### Configuration de base
- [x] Loki démarré et healthy
- [x] Promtail collecte les logs
- [x] Datasource Loki configurée dans Grafana
- [x] Test de connexion réussi

### Dashboards
- [ ] Dashboard logs backend en temps réel
- [ ] Dashboard analyse des erreurs
- [ ] Dashboard logs par niveau
- [ ] Dashboard corrélation logs-métriques

### Alertes
- [ ] Alerte: Trop d'erreurs
- [ ] Alerte: Service down
- [ ] Alerte: Erreurs critiques
- [ ] Notifications configurées (Email/Slack)

### Améliorations
- [ ] Labels personnalisés dans Promtail
- [ ] Filtres pour logs inutiles
- [ ] Derived fields pour corrélation
- [ ] Dashboards avancés

---

## 🚀 Prochaines étapes recommandées

1. **Créer le dashboard de logs de base** (30 min)
2. **Configurer 2-3 alertes essentielles** (20 min)
3. **Tester avec des logs réels** (10 min)
4. **Améliorer la configuration Promtail** (30 min)
5. **Créer des dashboards avancés** (1-2h)

---

**Dernière mise à jour:** 8 Février 2026
