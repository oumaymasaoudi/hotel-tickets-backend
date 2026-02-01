# 🔴 Résolution : Backend DOWN dans Prometheus

## Problème

Le backend staging est marqué comme **DOWN** dans Prometheus avec l'erreur :
```
Error scraping target: Get "http://13.63.15.86:8081/actuator/prometheus": 
dial tcp 13.63.15.86:8081: connect: connection refused
```

## Causes possibles

1. **Conteneur Docker arrêté** - Le backend n'est pas en cours d'exécution
2. **Security Group AWS** - Le port 8081 n'est pas accessible depuis la VM Prometheus
3. **Application non démarrée** - L'application Spring Boot n'a pas démarré correctement

## Diagnostic rapide

### Option 1 : Script PowerShell (Recommandé)

```powershell
cd hotel-ticket-hub-backend\scripts
.\diagnose-prometheus-backend-down.ps1
```

Ce script va :
- ✅ Vérifier le statut du conteneur
- ✅ Tester l'endpoint localement
- ✅ Tester la connectivité depuis Prometheus
- ✅ Vérifier les logs d'erreurs
- ✅ Donner des solutions spécifiques

### Option 2 : Diagnostic manuel

#### Étape 1 : Vérifier le conteneur

```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
docker ps -a | grep hotel-ticket-hub-backend-staging
```

**Si le conteneur est arrêté :**
```bash
cd /opt/hotel-ticket-hub-backend-staging
docker compose up -d backend
docker logs hotel-ticket-hub-backend-staging --tail 50
```

#### Étape 2 : Tester l'endpoint localement

```bash
# Sur la VM Backend
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/prometheus
```

**Si ça ne fonctionne pas localement :**
- Vérifier les logs : `docker logs hotel-ticket-hub-backend-staging --tail 100`
- Vérifier la configuration : `docker exec hotel-ticket-hub-backend-staging env | grep MANAGEMENT`

#### Étape 3 : Tester depuis la VM Prometheus

```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@16.170.74.58
curl http://13.63.15.86:8081/actuator/prometheus
```

**Si "Connection refused" ou "Timeout" :**
→ **Problème de Security Group AWS** (voir solution ci-dessous)

## Solutions

### Solution 1 : Redémarrer le backend

Si le conteneur est arrêté :

```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
cd /opt/hotel-ticket-hub-backend-staging
docker compose restart backend
# Attendre 30-45 secondes
docker logs hotel-ticket-hub-backend-staging --tail 50 | grep -i "started"
```

### Solution 2 : Ouvrir le port 8081 dans le Security Group AWS

**C'est probablement la cause principale !**

1. **AWS Console** → **EC2** → **Security Groups**
2. Trouvez le Security Group de la VM Backend (celle avec l'IP `13.63.15.86`)
3. **Inbound rules** → **Edit inbound rules** → **Add rule**
4. Configuration :
   ```
   Type: Custom TCP
   Protocol: TCP
   Port range: 8081
   Source: 16.170.74.58/32 (IP de la VM Prometheus)
   Description: Allow Prometheus scraping from monitoring VM
   ```
5. **Save rules**

**Alternative : Autoriser depuis le Security Group de Prometheus**
- Source : Sélectionnez le Security Group de la VM Prometheus (au lieu de l'IP)

### Solution 3 : Vérifier la configuration Actuator

Si l'endpoint n'est pas accessible localement :

```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
docker exec hotel-ticket-hub-backend-staging env | grep MANAGEMENT
```

Vérifiez que ces variables sont définies :
- `MANAGEMENT_ENDPOINT_PROMETHEUS_ENABLED=true`
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,prometheus,metrics`

Si elles ne sont pas définies, vérifiez le fichier `docker-compose.yml` et redémarrez le conteneur.

## Vérification après correction

1. **Vérifier dans Prometheus** (attendre 15-30 secondes) :
   ```
   http://16.170.74.58:9090/targets
   ```
   Le target `staging-backend` devrait passer à **UP** (vert)

2. **Tester l'endpoint directement** :
   ```bash
   curl http://13.63.15.86:8081/actuator/prometheus | head -20
   ```

3. **Vérifier les métriques dans Grafana** :
   ```
   http://16.170.74.58:3000
   ```

## Scripts utiles

- `diagnose-prometheus-backend-down.ps1` - Diagnostic complet
- `check-and-restart-backend.ps1` - Vérifier et redémarrer le backend
- `diagnose-prometheus-on-vm.sh` - Diagnostic depuis la VM (à exécuter sur la VM)

## Problèmes courants

### Le conteneur redémarre en boucle

```bash
docker logs hotel-ticket-hub-backend-staging --tail 100
```

Causes possibles :
- Erreur de connexion à la base de données
- Problème de mémoire (OOM)
- Erreur de configuration

### L'endpoint répond mais Prometheus ne peut pas scraper

→ Vérifier le Security Group (Solution 2)

### L'application démarre mais l'endpoint /actuator/prometheus retourne 404

→ Vérifier la configuration Actuator (Solution 3)

## Support

Si le problème persiste après avoir essayé toutes les solutions :
1. Vérifier les logs complets : `docker logs hotel-ticket-hub-backend-staging`
2. Vérifier la connectivité réseau : `nc -zv 13.63.15.86 8081` (depuis la VM Prometheus)
3. Vérifier les Security Groups AWS pour les deux VMs

