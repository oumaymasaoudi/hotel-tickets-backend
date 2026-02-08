# ⚠️ Solution: Rate Limit Docker Hub

**Problème:** "You have reached your unauthenticated pull rate limit"

**Cause:** Docker Hub limite les téléchargements à 100 par 6 heures pour les utilisateurs non authentifiés.

---

## ✅ Solutions

### Solution 1: Attendre (Recommandé)

**Attendez 1-2 heures** et réessayez:

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml up -d
```

### Solution 2: Se Connecter à Docker Hub

Si vous avez un compte Docker Hub (gratuit):

```bash
ssh ubuntu@16.170.74.58

# Se connecter à Docker Hub
docker login

# Entrer votre username et password

# Puis redémarrer les services
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml up -d
```

**Avantage:** 200 téléchargements par 6 heures au lieu de 100.

### Solution 3: Utiliser les Images Existantes

Si les images sont déjà téléchargées:

```bash
# Voir les images disponibles
docker images

# Démarrer avec les images existantes
docker compose -f docker-compose.monitoring.yml up -d
```

---

## 🔍 Vérifier les Images Disponibles

```bash
docker images | grep -E 'grafana|prometheus|loki|alertmanager'
```

**Si les images existent, vous pouvez démarrer les services même avec le rate limit.**

---

## 📝 Services et Images Requises

| Service | Image | Taille |
|---------|-------|--------|
| Grafana | `grafana/grafana:latest` | ~200MB |
| Prometheus | `prom/prometheus:latest` | ~200MB |
| Loki | `grafana/loki:latest` | ~100MB |
| Alertmanager | `prom/alertmanager:latest` | ~50MB |
| Node Exporter | `prom/node-exporter:latest` | ~20MB |
| cAdvisor | `gcr.io/cadvisor/cadvisor:latest` | ~100MB |

---

## 🎯 Recommandation

**Pour l'instant:**
1. **Attendre 1-2 heures** pour que le rate limit se réinitialise
2. **Ou créer un compte Docker Hub gratuit** et se connecter

**Ensuite:**
```bash
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml up -d
docker compose -f docker-compose.loki.yml up -d
```

---

## ✅ Vérification Après Démarrage

```bash
# Vérifier les services
docker ps

# Tester les ports
curl http://localhost:3000  # Grafana
curl http://localhost:9090  # Prometheus
```

---

**Dernière mise à jour:** 8 Février 2026
