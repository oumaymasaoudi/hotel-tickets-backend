# 🔧 Guide : Résoudre ERR_CONNECTION_REFUSED

## Problème
Le frontend (13.50.221.51) ne peut pas se connecter au backend (13.63.15.86:8081).
Erreur : `ERR_CONNECTION_REFUSED`

## Causes possibles

### 1. Security Group AWS bloque les connexions

Le Security Group de la VM Backend (13.63.15.86) doit autoriser les connexions entrantes sur le port 8081 depuis :
- Le frontend VM (13.50.221.51)
- Ou depuis n'importe où (0.0.0.0/0) pour le staging

### 2. Backend non démarré ou crashé

Vérifier que le backend est en cours d'exécution.

## Solutions

### Solution 1 : Configurer le Security Group AWS (RECOMMANDÉ)

1. **Connectez-vous à AWS Console**
2. **Allez dans EC2 > Security Groups**
3. **Trouvez le Security Group de la VM Backend (13.63.15.86)**
4. **Ajoutez une règle entrante :**
   - Type: Custom TCP
   - Port: 8081
   - Source: 
     - Option A (Staging) : `0.0.0.0/0` (autoriser depuis n'importe où)
     - Option B (Sécurisé) : `13.50.221.51/32` (uniquement depuis le frontend)
   - Description: "Allow backend API from frontend"

### Solution 2 : Vérifier que le backend est démarré

```bash
# Se connecter à la VM Backend
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86

# Vérifier les conteneurs
docker ps | grep backend

# Si le backend n'est pas démarré
cd ~/hotel-ticket-hub-backend
docker compose up -d backend

# Vérifier les logs
docker logs -f hotel-ticket-hub-backend-staging
```

### Solution 3 : Vérifier la connectivité réseau

```bash
# Depuis la VM Frontend (si accessible)
curl -v http://13.63.15.86:8081/actuator/health

# Depuis votre machine locale
curl -v http://13.63.15.86:8081/actuator/health
```

### Solution 4 : Vérifier le firewall Ubuntu

```bash
# Sur la VM Backend
sudo ufw status
sudo ufw allow 8081/tcp
```

## Vérification rapide

```bash
# Test depuis la VM Backend elle-même
curl http://localhost:8081/actuator/health

# Test depuis l'extérieur (doit fonctionner si Security Group est correct)
curl http://13.63.15.86:8081/actuator/health
```

## Commandes utiles

```bash
# Redémarrer le backend
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86 "cd ~/hotel-ticket-hub-backend && docker compose restart backend"

# Vérifier les logs
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86 "docker logs --tail 50 hotel-ticket-hub-backend-staging"

# Vérifier le port
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86 "netstat -tlnp | grep 8081"
```
