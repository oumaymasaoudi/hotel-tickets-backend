# 🔧 Correction : Targets Prometheus en état UNKNOWN

## Problème identifié

D'après la capture d'écran Prometheus, plusieurs targets sont en état **UNKNOWN** :
- ❌ `staging-backend` : http://13.63.15.86:8081/actuator/prometheus
- ❌ `staging-backend-node` : http://13.63.15.86:9100/metrics
- ❌ `cadvisor` : http://cadvisor:8080/metrics
- ❌ `database-vm` : http://13.48.83.147:9100/metrics

Seuls `prometheus` et `staging-frontend` sont **UP**.

## Causes possibles

### 1. Security Group AWS bloque les connexions

La VM Monitoring (`16.170.74.58`) ne peut pas accéder au backend (`13.63.15.86:8081`) car le Security Group bloque les connexions.

**Solution :**
1. AWS Console → EC2 → Security Groups
2. Trouvez le Security Group de la VM Backend (`13.63.15.86`)
3. Ajoutez une règle entrante :
   - Type: Custom TCP
   - Port: 8081
   - Source: IP de la VM Monitoring (`16.170.74.58/32`) ou le Security Group de la VM Monitoring
   - Description: Allow Prometheus scraping from monitoring VM

### 2. Backend non accessible depuis la VM Monitoring

**Vérification depuis la VM Monitoring :**
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@16.170.74.58
curl -v http://13.63.15.86:8081/actuator/prometheus
```

**Si erreur "Connection refused" ou "Timeout" :**
- Vérifiez le Security Group (voir point 1)
- Vérifiez que le backend est en cours d'exécution

### 3. Node Exporter non installé sur la VM Backend

Pour `staging-backend-node` (port 9100), Node Exporter doit être installé sur la VM Backend.

**Vérification :**
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
curl http://localhost:9100/metrics
```

**Si Node Exporter n'est pas installé :**
- Option 1 : Installer Node Exporter sur la VM Backend
- Option 2 : Supprimer cette target de `prometheus.yml` si non nécessaire

## Solutions par target

### Target : `staging-backend` (CRITIQUE)

**Problème :** Prometheus ne peut pas scraper les métriques du backend.

**Solution rapide :**
1. **Ouvrir le port 8081 depuis la VM Monitoring** :
   - AWS Console → EC2 → Security Groups
   - Security Group de la VM Backend
   - Inbound rules → Add rule
   - Type: Custom TCP, Port: 8081
   - Source: Security Group de la VM Monitoring (ou IP `16.170.74.58/32`)

2. **Vérifier depuis la VM Monitoring** :
   ```bash
   ssh ubuntu@16.170.74.58
   curl http://13.63.15.86:8081/actuator/prometheus | head -5
   ```

3. **Redémarrer Prometheus** (si nécessaire) :
   ```bash
   docker restart prometheus
   ```

### Target : `staging-backend-node` (OPTIONNEL)

**Problème :** Node Exporter non accessible sur la VM Backend.

**Solutions :**
- **Option A** : Installer Node Exporter sur la VM Backend
- **Option B** : Supprimer cette target si non nécessaire

**Pour installer Node Exporter :**
```bash
ssh ubuntu@13.63.15.86
# Installer Node Exporter
wget https://github.com/prometheus/node_exporter/releases/download/v1.7.0/node_exporter-1.7.0.linux-amd64.tar.gz
tar xvfz node_exporter-1.7.0.linux-amd64.tar.gz
sudo mv node_exporter-1.7.0.linux-amd64/node_exporter /usr/local/bin/
sudo useradd --no-create-home --shell /bin/false node_exporter
sudo systemctl enable node_exporter
sudo systemctl start node_exporter
```

**Ou utiliser Docker :**
```yaml
# Ajouter dans docker-compose.yml sur la VM Backend
node-exporter:
  image: prom/node-exporter:latest
  container_name: node-exporter-backend
  restart: unless-stopped
  ports:
    - "9100:9100"
  command:
    - '--path.procfs=/host/proc'
    - '--path.sysfs=/host/sys'
    - '--collector.filesystem.mount-points-exclude=^/(sys|proc|dev|host|etc)($$|/)'
  volumes:
    - /proc:/host/proc:ro
    - /sys:/host/sys:ro
    - /:/rootfs:ro
```

### Target : `cadvisor` (OPTIONNEL)

**Problème :** cAdvisor non accessible depuis Prometheus.

**Vérification :**
```bash
ssh ubuntu@16.170.74.58
docker ps | grep cadvisor
curl http://localhost:8080/metrics
```

**Si cAdvisor n'est pas en cours d'exécution :**
- Vérifiez `docker-compose.monitoring.yml`
- Démarrez-le : `docker compose -f docker-compose.monitoring.yml up -d cadvisor`

### Target : `database-vm` (OPTIONNEL)

**Problème :** Node Exporter non accessible sur la VM Database.

**Solution :** Similaire à `staging-backend-node` - installer Node Exporter ou supprimer la target.

## Vérification après correction

1. **Attendre 15-30 secondes** (intervalle de scraping Prometheus)

2. **Vérifier dans Prometheus** :
   - http://16.170.74.58:9090/targets
   - `staging-backend` devrait être **UP** (vert)

3. **Tester une requête** :
   - http://16.170.74.58:9090/graph
   - Requête : `up{job="staging-backend"}`
   - Résultat attendu : `up{job="staging-backend"} 1`

## Configuration Security Group recommandée

### VM Backend (13.63.15.86)

**Règles entrantes nécessaires :**
- SSH (22) : Depuis votre IP ou 0.0.0.0/0
- Backend (8081) : Depuis Security Group de la VM Monitoring
- Node Exporter (9100) : Depuis Security Group de la VM Monitoring (si utilisé)

### VM Monitoring (16.170.74.58)

**Règles entrantes nécessaires :**
- SSH (22) : Depuis votre IP ou 0.0.0.0/0
- Prometheus (9090) : Depuis votre IP ou 0.0.0.0/0
- Grafana (3000) : Depuis votre IP ou 0.0.0.0/0

## Script de vérification rapide

```bash
# Depuis votre machine locale
echo "=== Vérification Backend ==="
curl -s http://13.63.15.86:8081/actuator/prometheus | head -3

echo ""
echo "=== Vérification depuis VM Monitoring ==="
ssh ubuntu@16.170.74.58 "curl -s http://13.63.15.86:8081/actuator/prometheus | head -3"
```

Si la première commande fonctionne mais pas la seconde → **Problème de Security Group**

