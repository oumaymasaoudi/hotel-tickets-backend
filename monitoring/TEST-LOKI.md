# 🧪 Guide de Test - Loki

**Date:** 8 Février 2026

---

## ✅ Vérification de Loki

### 1. Vérifier que Loki est démarré

```bash
ssh ubuntu@16.170.74.58
docker ps | grep loki
```

**Résultat attendu:**
```
loki    Up X minutes (healthy)
```

### 2. Tester l'endpoint Loki

```bash
# Health check
curl http://localhost:3100/ready
# Résultat: "ready"

# Métriques
curl http://localhost:3100/metrics | head -10
```

### 3. Vérifier Promtail (collecteur de logs)

```bash
docker ps | grep promtail
docker logs promtail --tail 20
```

---

## 🔍 Test dans Grafana

### 1. Accéder à Grafana

1. Ouvrir http://16.170.74.58:3000
2. Se connecter (admin/admin par défaut)
3. Aller dans **Connections > Data sources**

### 2. Vérifier la configuration Loki

La datasource Loki devrait être configurée avec:
- **Name:** Loki
- **URL:** http://loki:3100
- **Status:** Provisioned (ne peut pas être modifiée via UI)

### 3. Tester la connexion

1. Cliquer sur **Loki** dans la liste des datasources
2. Cliquer sur **Save & test**
3. Vérifier que le message "Data source is working" apparaît

---

## 📊 Créer un Dashboard de Logs

### 1. Créer un nouveau Dashboard

1. Aller dans **Dashboards > New dashboard**
2. Cliquer sur **Add visualization**
3. Sélectionner **Loki** comme datasource

### 2. Requête LogQL de base

**Requête simple:**
```logql
{job="varlogs"}
```

**Requête avec filtre:**
```logql
{job="varlogs"} |= "error"
```

**Requête avec agrégation:**
```logql
sum by (level) (count_over_time({job="varlogs"}[5m]))
```

### 3. Exemple de Requêtes pour le Backend

**Logs du backend Spring Boot:**
```logql
{container="hotel-ticket-hub-backend-staging"}
```

**Erreurs uniquement:**
```logql
{container="hotel-ticket-hub-backend-staging"} |= "ERROR"
```

**Logs par niveau:**
```logql
{container="hotel-ticket-hub-backend-staging"} | json | level="ERROR"
```

**Comptage d'erreurs:**
```logql
sum(count_over_time({container="hotel-ticket-hub-backend-staging"} |= "ERROR" [1m]))
```

---

## 🔧 Configuration Promtail

### Vérifier la configuration Promtail

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring
cat promtail/promtail-config.yml
```

**Configuration attendue:**
- Collecte des logs Docker: `/var/lib/docker/containers`
- Envoi vers Loki: `http://loki:3100/loki/api/v1/push`

### Vérifier que Promtail collecte les logs

```bash
docker logs promtail --tail 50 | grep -i "error\|warn\|info"
```

---

## 📝 Tests Pratiques

### Test 1: Vérifier les logs Docker

```bash
# Générer un log de test
docker logs hotel-ticket-hub-backend-staging --tail 10

# Vérifier dans Loki (via Grafana)
# Requête: {container="hotel-ticket-hub-backend-staging"}
```

### Test 2: Rechercher des erreurs

1. Dans Grafana, créer une requête:
   ```logql
   {container="hotel-ticket-hub-backend-staging"} |= "ERROR"
   ```
2. Vérifier que les erreurs apparaissent

### Test 3: Dashboard de logs en temps réel

1. Créer un nouveau dashboard
2. Ajouter un panel de type **Logs**
3. Requête: `{container="hotel-ticket-hub-backend-staging"}`
4. Configurer l'auto-refresh: **5s** ou **10s**

---

## 🎯 Ce qu'il faut ajouter après

### 1. Dashboards de Logs

Créer des dashboards pour:
- ✅ **Logs du Backend:** Tous les logs Spring Boot
- ✅ **Erreurs:** Filtrage des erreurs uniquement
- ✅ **Logs par niveau:** INFO, WARN, ERROR
- ✅ **Logs par endpoint:** Filtrage par route API

### 2. Alertes sur les Logs

Créer des alertes Grafana pour:
- ✅ **Trop d'erreurs:** Plus de 10 erreurs en 5 minutes
- ✅ **Erreurs critiques:** Patterns spécifiques (ex: "OutOfMemoryError")
- ✅ **Absence de logs:** Pas de logs depuis 5 minutes (service down)

### 3. Intégration avec Prometheus

Lier les logs Loki aux métriques Prometheus:
- ✅ **Corrélation:** Lier les logs aux métriques par timestamp
- ✅ **Traces:** Si vous utilisez des traces (Jaeger, etc.)

### 4. Labels et Filtres

Améliorer la configuration Promtail pour:
- ✅ **Labels personnalisés:** Ajouter des labels (environment, service, etc.)
- ✅ **Filtres:** Exclure certains logs (ex: health checks)

---

## 📊 Exemple de Dashboard Complet

### Panel 1: Logs en temps réel
- **Type:** Logs
- **Requête:** `{container="hotel-ticket-hub-backend-staging"}`
- **Refresh:** 5s

### Panel 2: Comptage d'erreurs
- **Type:** Stat
- **Requête:** `sum(count_over_time({container="hotel-ticket-hub-backend-staging"} |= "ERROR" [5m]))`

### Panel 3: Logs par niveau
- **Type:** Pie chart
- **Requête:** `sum by (level) (count_over_time({container="hotel-ticket-hub-backend-staging"} | json [5m]))`

### Panel 4: Top erreurs
- **Type:** Table
- **Requête:** `topk(10, sum by (message) (count_over_time({container="hotel-ticket-hub-backend-staging"} |= "ERROR" [1h])))`

---

## ✅ Checklist de Test

- [ ] Loki est démarré et healthy
- [ ] Promtail collecte les logs
- [ ] Datasource Loki configurée dans Grafana
- [ ] Test de connexion Loki réussi dans Grafana
- [ ] Requête LogQL de base fonctionne
- [ ] Logs du backend visibles dans Grafana
- [ ] Dashboard de logs créé
- [ ] Alertes configurées (optionnel)

---

**Dernière mise à jour:** 8 Février 2026
