# 🔧 Résolution: Prometheus Connection Refused

## Problème

Prometheus ne peut pas se connecter au backend pour scraper les métriques:
```
Error scraping target: Get "http://13.63.15.86:8081/actuator/prometheus": 
dial tcp 13.63.15.86:8081: connect: connection refused
```

## Causes Possibles

1. **Backend non démarré** - Le conteneur backend n'est pas running
2. **Security Group AWS** - Le port 8081 n'est pas accessible depuis la VM Monitoring
3. **Espace disque plein** - Le backend ne peut pas démarrer (no space left on device)
4. **Firewall local** - UFW ou autre firewall bloque le port

## Solutions

### 1. Vérifier que le backend est démarré

```bash
ssh ubuntu@13.63.15.86
docker ps | grep backend
curl http://localhost:8081/actuator/health
```

Si le backend n'est pas démarré:
```bash
cd /opt/hotel-ticket-hub-backend-staging
docker compose up -d --force-recreate
```

### 2. Libérer l'espace disque (si "no space left on device")

```bash
ssh ubuntu@13.63.15.86
cd ~/hotel-ticket-hub-backend
chmod +x scripts/fix-disk-space-backend.sh
./scripts/fix-disk-space-backend.sh
```

Ou manuellement:
```bash
docker system prune -af --volumes
docker builder prune -af
```

### 3. Configurer le Security Group AWS

**Problème:** Le Security Group de la VM Backend bloque les connexions depuis la VM Monitoring.

**Solution:**
1. Aller dans AWS Console > EC2 > Security Groups
2. Trouver le Security Group de la VM Backend (13.63.15.86)
3. Ajouter une règle entrante:
   - **Type:** Custom TCP
   - **Port:** 8081
   - **Source:** `16.170.74.58/32` (IP de la VM Monitoring)
   - **Description:** "Allow Prometheus scraping from Monitoring VM"

### 4. Vérifier le firewall local (si présent)

```bash
ssh ubuntu@13.63.15.86
sudo ufw status
# Si actif, autoriser le port 8081 depuis la VM Monitoring
sudo ufw allow from 16.170.74.58 to any port 8081
```

### 5. Vérifier la configuration Prometheus

Sur la VM Monitoring:
```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring
cat prometheus/prometheus.yml | grep -A 10 staging-backend
```

La configuration doit pointer vers:
```yaml
- job_name: 'staging-backend'
  static_configs:
    - targets: ['13.63.15.86:8081']
```

### 6. Tester la connexion manuellement

Depuis la VM Monitoring:
```bash
ssh ubuntu@16.170.74.58
curl http://13.63.15.86:8081/actuator/prometheus
```

Si ça fonctionne, Prometheus devrait aussi pouvoir se connecter.

## Vérification Finale

1. **Backend running:**
   ```bash
   ssh ubuntu@13.63.15.86
   docker ps | grep backend
   curl http://localhost:8081/actuator/health
   ```

2. **Prometheus peut se connecter:**
   ```bash
   ssh ubuntu@16.170.74.58
   curl http://13.63.15.86:8081/actuator/prometheus | head -5
   ```

3. **Vérifier dans Prometheus UI:**
   - Aller sur http://16.170.74.58:9090
   - Status > Targets
   - Vérifier que `staging-backend` est `UP`

## Script Automatique

```bash
cd ~/hotel-ticket-hub-backend
chmod +x scripts/fix-disk-space-backend.sh
./scripts/fix-disk-space-backend.sh
```

---

**Dernière mise à jour:** 8 Février 2026
