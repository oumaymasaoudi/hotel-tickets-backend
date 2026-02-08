# 🔧 Résolution: Erreur de Connexion Loki dans Grafana

**Erreur:** "Unable to connect with Loki. Please check the server logs for more details."

---

## 🔍 Diagnostic

### Causes Possibles

1. **Loki n'est pas démarré**
2. **Réseau Docker non partagé** - Grafana et Loki ne sont pas sur le même réseau
3. **URL incorrecte** - L'URL dans la configuration Grafana est incorrecte
4. **Loki pas accessible** - Problème de connectivité réseau

---

## ✅ Solutions

### Solution 1: Vérifier que Loki est démarré

```bash
ssh ubuntu@16.170.74.58
docker ps | grep loki
```

**Si Loki n'est pas démarré:**
```bash
cd /opt/monitoring
docker compose -f docker-compose.loki.yml up -d
```

### Solution 2: Vérifier le réseau Docker

Grafana et Loki doivent être sur le même réseau Docker (`monitoring-network`).

**Vérifier:**
```bash
docker network inspect monitoring-network
```

**Créer le réseau si nécessaire:**
```bash
docker network create monitoring-network
```

**Redémarrer les services:**
```bash
cd /opt/monitoring
docker compose -f docker-compose.loki.yml down
docker compose -f docker-compose.monitoring.yml down
docker compose -f docker-compose.loki.yml up -d
docker compose -f docker-compose.monitoring.yml up -d
```

### Solution 3: Vérifier l'URL dans Grafana

L'URL doit être `http://loki:3100` (nom du service Docker, pas localhost).

**Vérifier la configuration:**
```bash
cd /opt/monitoring
cat grafana/provisioning/datasources/loki.yml
```

**Configuration correcte:**
```yaml
apiVersion: 1

datasources:
  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100  # ← Nom du service Docker
    isDefault: false
    jsonData:
      maxLines: 1000
    editable: false
```

### Solution 4: Tester la connectivité

**Depuis le conteneur Grafana:**
```bash
docker exec grafana curl http://loki:3100/ready
# Résultat attendu: "ready"
```

**Si ça ne fonctionne pas:**
- Vérifier que Loki et Grafana sont sur le même réseau
- Vérifier que le service s'appelle bien "loki"

---

## 🔧 Solution Complète

### Étape 1: Vérifier et créer le réseau

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring

# Créer le réseau si nécessaire
docker network create monitoring-network 2>/dev/null || echo "Network exists"

# Vérifier que les services utilisent ce réseau
docker network inspect monitoring-network | grep -E "loki|grafana"
```

### Étape 2: Redémarrer les services dans le bon ordre

```bash
# Arrêter tout
docker compose -f docker-compose.loki.yml down
docker compose -f docker-compose.monitoring.yml down

# Démarrer Loki d'abord
docker compose -f docker-compose.loki.yml up -d

# Attendre que Loki soit prêt
sleep 10
curl http://localhost:3100/ready

# Démarrer Grafana
docker compose -f docker-compose.monitoring.yml up -d

# Attendre que Grafana soit prêt
sleep 10
```

### Étape 3: Vérifier la configuration Grafana

```bash
# Vérifier que le fichier de provisioning existe
cat grafana/provisioning/datasources/loki.yml

# Si le fichier n'existe pas, le créer
mkdir -p grafana/provisioning/datasources
cat > grafana/provisioning/datasources/loki.yml << 'EOF'
apiVersion: 1

datasources:
  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    isDefault: false
    jsonData:
      maxLines: 1000
    editable: false
EOF

# Redémarrer Grafana pour charger la config
docker compose -f docker-compose.monitoring.yml restart grafana
```

### Étape 4: Tester la connexion

```bash
# Tester depuis Grafana
docker exec grafana curl http://loki:3100/ready

# Vérifier les logs Grafana
docker logs grafana --tail 30 | grep -i loki
```

---

## ✅ Vérification Finale

1. **Loki est démarré:**
   ```bash
   docker ps | grep loki
   curl http://localhost:3100/ready
   ```

2. **Grafana peut se connecter à Loki:**
   ```bash
   docker exec grafana curl http://loki:3100/ready
   ```

3. **Configuration dans Grafana:**
   - Aller sur http://16.170.74.58:3000
   - Connections > Data sources > Loki
   - Cliquer sur "Test"
   - Vérifier: "Data source is working" ✅

---

## 🐛 Dépannage Avancé

### Si l'erreur persiste

**Vérifier les logs:**
```bash
docker logs loki --tail 50
docker logs grafana --tail 50 | grep -i loki
```

**Vérifier le réseau:**
```bash
docker network inspect monitoring-network
# Vérifier que loki et grafana sont listés
```

**Tester avec l'IP du conteneur:**
```bash
# Obtenir l'IP de Loki
docker inspect loki | grep IPAddress

# Tester depuis Grafana avec l'IP
docker exec grafana curl http://<IP_LOKI>:3100/ready
```

---

**Dernière mise à jour:** 8 Février 2026
