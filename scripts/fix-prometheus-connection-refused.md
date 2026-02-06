# 🔧 Fix: Prometheus "Connection Refused" Error

## Problème

Prometheus ne peut pas scraper le backend : `connection refused` sur `13.63.15.86:8081`

## Causes possibles

1. **Security Group bloque les connexions** depuis la VM Monitoring vers le backend
2. **Backend s'est arrêté** après le déploiement
3. **Port 8081 non accessible** depuis l'extérieur

## Solutions

### 1. Vérifier que le backend est en cours d'exécution

**Sur la VM Backend (`13.63.15.86`):**
```bash
# Se connecter au serveur
ssh -i votre-cle.pem utilisateur@13.63.15.86

# Vérifier que le container tourne
docker ps | grep hotel-ticket-hub-backend-staging

# Si le container n'est pas en cours d'exécution, le démarrer
cd /opt/hotel-ticket-hub-backend-staging
docker compose up -d

# Vérifier les logs
docker compose logs --tail=50 hotel-ticket-hub-backend-staging
```

**Tester l'endpoint depuis la VM Backend:**
```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/prometheus | head -20
```

### 2. Vérifier le Security Group AWS

**Le Security Group du backend doit autoriser :**
- **Type**: Custom TCP
- **Port**: 8081
- **Source**: L'IP de la VM Monitoring (`16.170.74.58/32`) OU le Security Group de la VM Monitoring

**Pour vérifier/modifier :**
1. AWS Console > EC2 > Security Groups
2. Trouvez le Security Group attaché à l'instance backend (`13.63.15.86`)
3. Vérifiez les **Inbound Rules**
4. Ajoutez une règle si nécessaire :
   - Type: Custom TCP
   - Port: 8081
   - Source: `16.170.74.58/32` (IP de la VM Monitoring)

### 3. Tester la connexion depuis la VM Monitoring

**Sur la VM Monitoring (`16.170.74.58`):**
```bash
# Se connecter au serveur
ssh -i votre-cle.pem utilisateur@16.170.74.58

# Tester la connexion au backend
curl -v http://13.63.15.86:8081/actuator/health
curl -v http://13.63.15.86:8081/actuator/prometheus | head -20

# Si ça fonctionne, redémarrer Prometheus
docker restart prometheus
```

### 4. Vérifier dans Prometheus UI

1. **Accédez à** : http://16.170.74.58:9090/targets
2. **Cherchez** : `staging-backend`
3. **Attendez** 15-30 secondes après avoir corrigé le problème
4. **Vérifiez** que le statut passe à **UP** (vert)

## Script de vérification rapide

**Depuis votre machine locale (PowerShell):**
```powershell
# Vérifier que le backend répond
curl http://13.63.15.86:8081/actuator/health

# Vérifier l'endpoint Prometheus
curl http://13.63.15.86:8081/actuator/prometheus | Select-Object -First 10
```

**Si ces commandes fonctionnent**, le problème est probablement le Security Group qui bloque depuis la VM Monitoring.

## Solution automatique (si le backend s'arrête souvent)

Ajoutez `restart: always` dans `docker-compose.yml` :

```yaml
services:
  backend:
    restart: always  # Au lieu de unless-stopped
```

Puis redéployez :
```bash
cd /opt/hotel-ticket-hub-backend-staging
docker compose down
docker compose up -d
```

## Vérification finale

1. ✅ Backend répond sur `http://13.63.15.86:8081/actuator/prometheus`
2. ✅ Security Group autorise les connexions depuis la VM Monitoring
3. ✅ Prometheus peut scraper (vérifier dans http://16.170.74.58:9090/targets)
4. ✅ Métriques apparaissent dans Prometheus Graph
