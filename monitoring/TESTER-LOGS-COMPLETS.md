# 📊 Guide Complet: Tester et Collecter Tous les Logs

**Objectif:** Collecter et visualiser les logs du backend, frontend et base de données dans Loki.

---

## 🎯 Vue d'Ensemble

```
┌─────────────┐     ┌──────────┐     ┌─────────┐
│  Backend    │────▶│ Promtail │────▶│  Loki   │
│  Frontend   │     │ (Collect)│     │ (Store) │
│  Database   │     └──────────┘     └─────────┘
└─────────────┘                            │
                                            ▼
                                      ┌──────────┐
                                      │ Grafana │
                                      │ (View)  │
                                      └──────────┘
```

---

## ✅ Étape 1: Vérifier que Promtail Collecte les Logs

### Vérifier l'État de Promtail

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring

# Vérifier que Promtail est démarré
docker ps | grep promtail

# Voir les logs de Promtail
docker logs promtail --tail 50
```

**Résultat attendu:** Pas d'erreurs, Promtail collecte les logs.

---

## ✅ Étape 2: Vérifier les Conteneurs à Monitorer

### Backend (VM Backend: 13.63.15.86)

```bash
ssh ubuntu@13.63.15.86

# Voir les conteneurs
docker ps | grep backend

# Voir les logs du backend
docker logs hotel-ticket-hub-backend-staging --tail 10
```

### Frontend (VM Frontend)

```bash
# Voir les conteneurs frontend
docker ps | grep frontend
```

### Base de Données (VM Database: 13.48.83.147)

```bash
ssh ubuntu@13.48.83.147

# Voir les conteneurs PostgreSQL
docker ps | grep postgres
```

---

## ✅ Étape 3: Tester dans Grafana Explore

### 1. Accéder à Grafana Explore

1. **Ouvrir Grafana:** http://16.170.74.58:3000
2. **Menu de gauche** → **Explore** (icône boussole)
3. **Sélectionner Loki** comme datasource (en haut à gauche)

### 2. Requêtes de Test

#### Voir Tous les Logs

```
{}
```

#### Logs du Backend

```
{container_name="hotel-ticket-hub-backend-staging"}
```

#### Logs d'Erreur

```
{} |= "ERROR"
```

#### Logs du Backend avec Erreurs

```
{container_name="hotel-ticket-hub-backend-staging"} |= "ERROR"
```

#### Logs par Niveau (INFO, WARN, ERROR)

```
{container_name="hotel-ticket-hub-backend-staging"} | json | level="ERROR"
```

#### Logs de la Base de Données

```
{container_name=~".*postgres.*"}
```

#### Logs du Frontend

```
{container_name=~".*frontend.*"}
```

### 3. Cliquer sur "Run query"

**Résultat attendu:** Vous voyez les logs en temps réel !

---

## 📝 Requêtes LogQL Avancées

### Compter les Logs par Conteneur

```
sum(count_over_time({}[5m])) by (container_name)
```

### Top 10 des Erreurs

```
topk(10, sum(count_over_time({} |= "ERROR" [5m])) by (message))
```

### Logs par Niveau (Graphique)

```
sum(count_over_time({container_name="hotel-ticket-hub-backend-staging"} | json [1m])) by (level)
```

### Logs d'Authentification

```
{container_name="hotel-ticket-hub-backend-staging"} |= "authentication"
```

### Logs de Tickets

```
{container_name="hotel-ticket-hub-backend-staging"} |= "ticket"
```

---

## 🔧 Configuration Promtail

### Fichier: `promtail/promtail-config.yml`

Promtail collecte automatiquement:
- ✅ **Logs Docker** (`/var/lib/docker/containers`)
- ✅ **Logs Application** (`/var/log/app`)

### Vérifier la Configuration

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring

# Voir la configuration Promtail
cat promtail/promtail-config.yml
```

---

## 📊 Créer un Dashboard de Monitoring

### 1. Créer un Nouveau Dashboard

1. **Grafana** → **Dashboards** → **New Dashboard**
2. **Add visualization**

### 2. Panel 1: Nombre de Logs par Minute

**Requête:**
```
sum(count_over_time({}[1m]))
```

**Visualisation:** Time series

### 3. Panel 2: Erreurs par Minute

**Requête:**
```
sum(count_over_time({} |= "ERROR" [1m]))
```

**Visualisation:** Time series (rouge)

### 4. Panel 3: Logs par Conteneur

**Requête:**
```
sum(count_over_time({}[5m])) by (container_name)
```

**Visualisation:** Bar chart

### 5. Panel 4: Logs du Backend (Table)

**Requête:**
```
{container_name="hotel-ticket-hub-backend-staging"}
```

**Visualisation:** Logs (table)

---

## ✅ Checklist Complète

### Infrastructure

- [ ] Loki démarré: `docker ps | grep loki`
- [ ] Promtail démarré: `docker ps | grep promtail`
- [ ] Grafana démarré: `docker ps | grep grafana`

### Collecte de Logs

- [ ] Promtail collecte les logs Docker
- [ ] Logs du backend visibles dans Loki
- [ ] Logs de la base de données visibles (si configuré)
- [ ] Logs du frontend visibles (si configuré)

### Test dans Grafana

- [ ] Explore fonctionne avec requête `{}`
- [ ] Logs du backend visibles: `{container_name="hotel-ticket-hub-backend-staging"}`
- [ ] Erreurs visibles: `{} |= "ERROR"`
- [ ] Dashboard créé avec visualisations

---

## 🚀 Commandes Rapides

### Voir les Logs en Temps Réel (Terminal)

```bash
# Backend
ssh ubuntu@13.63.15.86 "docker logs -f hotel-ticket-hub-backend-staging"

# Frontend
ssh ubuntu@<FRONTEND_IP> "docker logs -f <frontend-container>"

# Base de données
ssh ubuntu@13.48.83.147 "docker logs -f <postgres-container>"
```

### Voir les Logs dans Loki (Grafana)

1. **Grafana** → **Explore**
2. **Requête:** `{container_name="hotel-ticket-hub-backend-staging"}`
3. **Run query**

---

## 🎯 Résumé

1. **Promtail collecte automatiquement** les logs Docker
2. **Loki stocke** les logs
3. **Grafana visualise** les logs via Explore ou Dashboards

**Commencez par Grafana Explore avec la requête `{}` pour voir tous les logs !** 🚀

---

**Dernière mise à jour:** 8 Février 2026
