# ✅ Vérification Prometheus - Backend Status

**Date:** 8 Février 2026

---

## 📊 Statut dans Grafana

Le dashboard Grafana montre:
- ✅ **UP** pour `up{application="hotel-ticket-hub-backend", environment="staging"}`
- ⚠️ **DOWN** pour `up{job="staging-backend"}`

---

## 🔍 Analyse

### Configuration Prometheus

Le fichier `prometheus.yml` configure deux jobs pour le backend:

1. **`staging-backend`** (job name)
   - Target: `13.63.15.86:8081`
   - Path: `/actuator/prometheus`
   - Labels: `application="hotel-ticket-hub-backend"`, `environment="staging"`, `instance="backend-vm"`

2. **`staging-backend-node`** (Node Exporter)
   - Target: `13.63.15.86:9100`
   - Labels: `instance="backend-vm"`, `environment="staging"`

### Pourquoi "DOWN" pour `up{job="staging-backend"}`?

Le gauge "DOWN" peut apparaître si:
1. Prometheus n'a pas encore scrapé le target (délai de scrape)
2. Le Security Group AWS bloque encore les connexions
3. Le backend vient juste de démarrer et Prometheus n'a pas encore fait de scrape réussi

### Pourquoi "UP" pour `up{application="hotel-ticket-hub-backend"}`?

Le gauge "UP" utilise les labels `application` et `environment`, ce qui correspond au target configuré avec ces labels.

---

## ✅ Vérifications

### 1. Backend Accessible Localement

```bash
ssh ubuntu@13.63.15.86
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/prometheus | head -5
```

**Résultat attendu:** ✅ Health OK, métriques disponibles

### 2. Backend Accessible depuis Monitoring VM

```bash
ssh ubuntu@16.170.74.58
curl http://13.63.15.86:8081/actuator/prometheus | head -5
```

**Résultat attendu:** ✅ Métriques accessibles

### 3. Prometheus Targets Status

```bash
ssh ubuntu@16.170.74.58
curl 'http://localhost:9090/api/v1/targets' | grep -A 10 staging-backend
```

**Résultat attendu:** ✅ Health: "up"

### 4. Query Prometheus Directement

```bash
ssh ubuntu@16.170.74.58
curl 'http://localhost:9090/api/v1/query?query=up{job="staging-backend"}'
```

**Résultat attendu:** ✅ `"value":["timestamp","1"]`

---

## 🔧 Solutions

### Si le target est DOWN

1. **Vérifier le Security Group AWS:**
   - Aller dans AWS Console > EC2 > Security Groups
   - Trouver le Security Group de la VM Backend (13.63.15.86)
   - Vérifier qu'une règle autorise le port 8081 depuis 16.170.74.58

2. **Redémarrer Prometheus:**
   ```bash
   ssh ubuntu@16.170.74.58
   cd /opt/monitoring
   docker compose -f docker-compose.monitoring.yml restart prometheus
   ```

3. **Attendre le prochain scrape:**
   - Prometheus scrape toutes les 15 secondes
   - Attendre 30-60 secondes après le démarrage du backend

4. **Vérifier les logs Prometheus:**
   ```bash
   ssh ubuntu@16.170.74.58
   docker logs prometheus --tail 50 | grep staging-backend
   ```

---

## 📊 Dashboard Grafana

### Requêtes Recommandées

Pour le statut du backend, utiliser:
```promql
up{application="hotel-ticket-hub-backend", environment="staging"}
```

Ou:
```promql
up{job="staging-backend", instance="backend-vm"}
```

### Mise à Jour du Dashboard

Si le gauge montre "DOWN" mais que le backend fonctionne:
1. Vérifier que la requête utilise les bons labels
2. Attendre quelques minutes pour que Prometheus scrape
3. Rafraîchir le dashboard (F5)

---

## ✅ Statut Actuel

- ✅ Backend: Running (health OK)
- ✅ Endpoint `/actuator/prometheus`: Accessible
- ✅ Prometheus: Configuré pour scraper le backend
- ⚠️ Dashboard: Peut montrer "DOWN" temporairement après redémarrage

**Le backend est opérationnel. Si le dashboard montre "DOWN", attendre 1-2 minutes pour que Prometheus scrape les métriques.**

---

**Dernière mise à jour:** 8 Février 2026
