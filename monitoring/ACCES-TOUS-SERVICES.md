# 🌐 Accès à Tous les Services de Monitoring

**Problème:** "ERR_CONNECTION_REFUSED" - Tous les sites ne fonctionnent pas

---

## ✅ URLs des Services

### Services Disponibles

| Service | URL | Port | Description |
|---------|-----|------|-------------|
| **Grafana** | http://16.170.74.58:3000 | 3000 | Visualisation des métriques et logs |
| **Prometheus** | http://16.170.74.58:9090 | 9090 | Collecte des métriques |
| **Loki** | http://16.170.74.58:3100 | 3100 | Stockage des logs |
| **Alertmanager** | http://16.170.74.58:9093 | 9093 | Gestion des alertes |

---

## 🔧 Vérifier l'État des Services

### Depuis la VM Monitoring

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring

# Voir l'état de tous les services
docker ps

# Vérifier les ports
netstat -tulpn | grep -E '3000|9090|3100|9093'
```

---

## ✅ Démarrer Tous les Services

### Étape 1: Démarrer le Stack Monitoring

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring

# Démarrer Prometheus, Grafana, Alertmanager, etc.
docker compose -f docker-compose.monitoring.yml up -d
```

### Étape 2: Démarrer Loki

```bash
# Démarrer Loki et Promtail
docker compose -f docker-compose.loki.yml up -d
```

### Étape 3: Vérifier

```bash
# Attendre 30 secondes
sleep 30

# Vérifier que tous les services sont démarrés
docker ps

# Tester les ports
curl http://localhost:3000  # Grafana
curl http://localhost:9090  # Prometheus
curl http://localhost:3100/ready  # Loki
```

---

## 🔍 Diagnostic

### Si un Service ne Répond Pas

#### 1. Vérifier que le Conteneur est Démarré

```bash
docker ps | grep <service-name>
```

**Si pas démarré:**
```bash
docker compose -f docker-compose.monitoring.yml up -d <service-name>
```

#### 2. Vérifier les Logs

```bash
docker logs <service-name> --tail 50
```

#### 3. Vérifier le Port

```bash
netstat -tulpn | grep <port>
```

**Si le port n'est pas ouvert, le service n'écoute pas.**

---

## 🚨 Problèmes Courants

### 1. Service Non Démarré

**Solution:**
```bash
docker compose -f docker-compose.monitoring.yml up -d
docker compose -f docker-compose.loki.yml up -d
```

### 2. Port Déjà Utilisé

**Vérifier:**
```bash
sudo lsof -i :3000  # ou le port concerné
```

**Solution:** Arrêter le processus qui utilise le port.

### 3. Pare-feu

**Vérifier les règles de sécurité AWS:**
- Port 3000 (Grafana) doit être ouvert
- Port 9090 (Prometheus) doit être ouvert
- Port 3100 (Loki) doit être ouvert
- Port 9093 (Alertmanager) doit être ouvert

---

## 📝 Checklist Complète

### Services

- [ ] Grafana démarré: `docker ps | grep grafana`
- [ ] Prometheus démarré: `docker ps | grep prometheus`
- [ ] Loki démarré: `docker ps | grep loki`
- [ ] Alertmanager démarré: `docker ps | grep alertmanager`

### Ports

- [ ] Port 3000 ouvert: `curl http://localhost:3000` → 200
- [ ] Port 9090 ouvert: `curl http://localhost:9090` → 200
- [ ] Port 3100 ouvert: `curl http://localhost:3100/ready` → "ready"
- [ ] Port 9093 ouvert: `curl http://localhost:9093` → 200

### Accès Externe

- [ ] Grafana accessible: http://16.170.74.58:3000
- [ ] Prometheus accessible: http://16.170.74.58:9090
- [ ] Loki accessible: http://16.170.74.58:3100
- [ ] Alertmanager accessible: http://16.170.74.58:9093

---

## 🎯 Script de Démarrage Complet

```bash
#!/bin/bash
cd /opt/monitoring

echo "1. Démarrer le stack monitoring..."
docker compose -f docker-compose.monitoring.yml up -d

echo "2. Démarrer Loki..."
docker compose -f docker-compose.loki.yml up -d

echo "3. Attendre 30 secondes..."
sleep 30

echo "4. Vérifier les services..."
docker ps

echo "5. Tester les ports..."
curl -s -o /dev/null -w "Grafana: %{http_code}\n" http://localhost:3000
curl -s -o /dev/null -w "Prometheus: %{http_code}\n" http://localhost:9090
curl -s -o /dev/null -w "Loki: %{http_code}\n" http://localhost:3100/ready
curl -s -o /dev/null -w "Alertmanager: %{http_code}\n" http://localhost:9093

echo "✅ Terminé!"
```

---

## 🎯 Résumé

1. **Démarrer tous les services:**
   ```bash
   docker compose -f docker-compose.monitoring.yml up -d
   docker compose -f docker-compose.loki.yml up -d
   ```

2. **Attendre 30 secondes** que les services démarrent

3. **Tester les URLs:**
   - Grafana: http://16.170.74.58:3000
   - Prometheus: http://16.170.74.58:9090
   - Loki: http://16.170.74.58:3100
   - Alertmanager: http://16.170.74.58:9093

**Si les services ne répondent pas, vérifier les règles de sécurité AWS (ports ouverts).** 🚀

---

**Dernière mise à jour:** 8 Février 2026
