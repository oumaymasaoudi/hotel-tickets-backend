# 🔧 Correction : Target Prometheus en état UNKNOWN

## Configuration Security Group ✅

D'après vos captures d'écran, les Security Groups sont **correctement configurés** :

### VM Backend/Frontend/Data
- ✅ Port 8081 : Source `16.170.74.58/32` (Prometheus monitoring)
- ✅ Port 9100 : Source `0.0.0.0/0` (monitor)
- ✅ SSH (22) : Source `0.0.0.0/0`

### VM Ansible (Monitoring)
- ✅ Prometheus (9090) : Source `0.0.0.0/0`
- ✅ Grafana (3000) : Source `0.0.0.0/0`
- ✅ Node Exporter (9100) : Source `0.0.0.0/0`
- ✅ cAdvisor (8080) : Source `0.0.0.0/0`
- ✅ Alertmanager (9093) : Source `0.0.0.0/0`
- ✅ Loki (3100) : Source `0.0.0.0/0`

## Diagnostic

Si les Security Groups sont corrects mais que `staging-backend` est toujours UNKNOWN, le problème peut venir de :

### 1. Backend non démarré ou endpoint non accessible

**Vérification depuis la VM Monitoring :**
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@16.170.74.58
./scripts/diagnose-prometheus-connection.sh
```

**Ou test manuel :**
```bash
curl -v http://13.63.15.86:8081/actuator/prometheus
```

### 2. Configuration Prometheus incorrecte

**Vérifier la configuration :**
```bash
ssh ubuntu@16.170.74.58
docker exec prometheus cat /etc/prometheus/prometheus.yml | grep -A 5 "staging-backend"
```

**Doit afficher :**
```yaml
- job_name: 'staging-backend'
  metrics_path: '/actuator/prometheus'
  static_configs:
    - targets: ['13.63.15.86:8081']
```

### 3. Problème réseau Docker

Si Prometheus est dans un conteneur Docker, il peut ne pas pouvoir accéder à l'IP externe.

**Solution :** Utiliser `host.docker.internal` ou le mode réseau `host` :

```yaml
# Dans docker-compose.monitoring.yml
prometheus:
  network_mode: "host"  # Permet d'accéder aux IPs externes
  # OU
  extra_hosts:
    - "host.docker.internal:host-gateway"
```

### 4. Redémarrer Prometheus

Parfois, Prometheus a besoin d'être redémarré après un changement :

```bash
ssh ubuntu@16.170.74.58
docker restart prometheus
```

Attendez 30 secondes, puis vérifiez : http://16.170.74.58:9090/targets

## Solutions par ordre de priorité

### Solution 1 : Vérifier depuis la VM Monitoring (RECOMMANDÉ)

```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@16.170.74.58

# Test de connexion
curl -v http://13.63.15.86:8081/actuator/prometheus | head -10

# Si ça fonctionne, redémarrer Prometheus
docker restart prometheus
```

### Solution 2 : Vérifier les logs Prometheus

```bash
ssh ubuntu@16.170.74.58
docker logs prometheus --tail 50 | grep -iE "error|staging-backend|13.63.15.86"
```

### Solution 3 : Vérifier la configuration réseau Docker

Si Prometheus est dans un conteneur et ne peut pas accéder aux IPs externes :

```bash
# Vérifier le réseau
docker network ls
docker network inspect <network-name>

# Tester depuis le conteneur
docker exec prometheus wget -q -O- http://13.63.15.86:8081/actuator/prometheus | head -5
```

### Solution 4 : Utiliser le mode réseau host

Modifier `docker-compose.monitoring.yml` :

```yaml
prometheus:
  image: prom/prometheus:latest
  network_mode: "host"  # Ajouter cette ligne
  # ... reste de la configuration
```

**Attention :** Cela expose Prometheus directement sur l'interface réseau de l'hôte.

## Vérification finale

1. **Attendez 15-30 secondes** après redémarrage
2. **Vérifiez dans Prometheus** : http://16.170.74.58:9090/targets
3. **Cliquez sur "Show more"** pour voir les détails de l'erreur
4. **Testez une requête** : http://16.170.74.58:9090/graph
   - Requête : `up{job="staging-backend"}`
   - Résultat attendu : `up{job="staging-backend"} 1`

## Commandes de diagnostic complètes

```bash
# Depuis la VM Monitoring
ssh ubuntu@16.170.74.58

# 1. Vérifier la connectivité
curl -v http://13.63.15.86:8081/actuator/prometheus | head -10

# 2. Vérifier depuis le conteneur Prometheus
docker exec prometheus wget -q -O- http://13.63.15.86:8081/actuator/prometheus | head -10

# 3. Vérifier la configuration
docker exec prometheus cat /etc/prometheus/prometheus.yml | grep -A 5 "staging-backend"

# 4. Vérifier les logs
docker logs prometheus --tail 50

# 5. Redémarrer Prometheus
docker restart prometheus

# 6. Attendre et vérifier
sleep 30
curl http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | select(.labels.job=="staging-backend")'
```

