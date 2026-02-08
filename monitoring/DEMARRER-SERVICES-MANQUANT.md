# 🚀 Démarrer les Services de Monitoring

**Problème:** Les services ne sont pas démarrés - "ERR_CONNECTION_REFUSED"

---

## ✅ Solution Rapide

### Démarrer Tous les Services

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring

# Démarrer le stack monitoring
docker compose -f docker-compose.monitoring.yml up -d

# Démarrer Loki
docker compose -f docker-compose.loki.yml up -d

# Attendre 30 secondes
sleep 30

# Vérifier
docker ps
```

---

## 🔍 Si Erreur "Rate Limit" Docker Hub

### Solution 1: Attendre et Réessayer

Docker Hub limite les téléchargements. Attendez 1-2 heures et réessayez.

### Solution 2: Utiliser les Images Existantes

```bash
# Voir les images disponibles
docker images

# Démarrer avec les images existantes
docker compose -f docker-compose.monitoring.yml up -d
```

### Solution 3: Se Connecter à Docker Hub

```bash
# Se connecter à Docker Hub (si vous avez un compte)
docker login

# Puis redémarrer
docker compose -f docker-compose.monitoring.yml up -d
```

---

## 📝 URLs des Services

Une fois démarrés, accédez à:

- **Grafana:** http://16.170.74.58:3000
- **Prometheus:** http://16.170.74.58:9090
- **Loki:** http://16.170.74.58:3100
- **Alertmanager:** http://16.170.74.58:9093

---

## ✅ Vérification

```bash
# Vérifier que les services sont démarrés
docker ps

# Tester les ports
curl http://localhost:3000  # Grafana
curl http://localhost:9090  # Prometheus
curl http://localhost:3100/ready  # Loki
```

---

## 🎯 Résumé

1. **Démarrer les services:** `docker compose -f docker-compose.monitoring.yml up -d`
2. **Attendre 30 secondes**
3. **Tester les URLs** dans votre navigateur

**Si erreur rate limit, attendre 1-2 heures ou se connecter à Docker Hub.** 🚀

---

**Dernière mise à jour:** 8 Février 2026
