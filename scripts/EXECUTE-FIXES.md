# 🚀 Exécution des corrections sur les VMs

## Option 1 : Exécution automatique (recommandé)

### Depuis votre machine locale (Windows avec Git Bash ou WSL)

```bash
# 1. Aller dans le répertoire du projet
cd hotel-ticket-hub-backend

# 2. Rendre le script exécutable (si Git Bash)
chmod +x scripts/execute-vm-fixes.sh

# 3. Exécuter le script
./scripts/execute-vm-fixes.sh
```

### Configuration du script

Le script utilise ces variables par défaut :
- `SSH_KEY=~/.ssh/oumayma-key.pem`
- `PROJECT_DIR=~/hotel-ticket-hub-backend`

Vous pouvez les modifier si nécessaire :

```bash
export SSH_KEY="C:/Users/oumay/.ssh/oumayma-key.pem"
export PROJECT_DIR="/opt/hotel-ticket-hub-backend"
./scripts/execute-vm-fixes.sh
```

## Option 2 : Exécution manuelle étape par étape

### Étape 1 : Corriger la base de données

**Sur la VM Database (13.48.83.147) :**

```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.48.83.147

# Aller dans le projet
cd ~/hotel-ticket-hub-backend
# OU
cd /opt/hotel-ticket-hub-backend

# Récupérer les modifications
git pull origin main

# Exécuter le script de correction
chmod +x scripts/fix-vm-database-issues.sh
./scripts/fix-vm-database-issues.sh
```

### Étape 2 : Redéployer le backend

**Sur la VM Backend (13.63.15.86) :**

```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86

# Aller dans le projet
cd ~/hotel-ticket-hub-backend
# OU
cd /opt/hotel-ticket-hub-backend

# Récupérer les modifications
git pull origin main

# Redémarrer le backend
docker compose down
docker compose pull
docker compose up -d --build

# Vérifier les logs
docker logs -f hotel-ticket-hub-backend-staging
```

### Étape 3 : Vérifications

```bash
# Test 1 : Santé de l'application
curl http://13.63.15.86:8081/actuator/health

# Test 2 : Hôtels publics
curl http://13.63.15.86:8081/api/hotels/public

# Test 3 : Vérifier les logs (pas d'erreurs BASIC)
docker logs hotel-ticket-hub-backend-staging | grep -i "error\|exception" | tail -10
```

## Option 3 : Via PowerShell (Windows)

```powershell
# Se connecter à la VM Database
ssh -i $env:USERPROFILE\.ssh\oumayma-key.pem ubuntu@13.48.83.147

# Dans la session SSH :
cd ~/hotel-ticket-hub-backend
git pull origin main
chmod +x scripts/fix-vm-database-issues.sh
./scripts/fix-vm-database-issues.sh

# Se connecter à la VM Backend
ssh -i $env:USERPROFILE\.ssh\oumayma-key.pem ubuntu@13.63.15.86

# Dans la session SSH :
cd ~/hotel-ticket-hub-backend
git pull origin main
docker compose down
docker compose pull
docker compose up -d --build
docker logs -f hotel-ticket-hub-backend-staging
```

## Vérification finale

Après l'exécution, vérifiez que :

1. ✅ Les plans STARTER, PRO, ENTERPRISE existent dans la base de données
2. ✅ Tous les hôtels ont un plan assigné
3. ✅ Le backend démarre sans erreurs
4. ✅ `/api/hotels/public` retourne une réponse valide
5. ✅ `/api/auth/login` fonctionne sans erreur "BASIC"

## Dépannage

### Le script échoue avec "Permission denied"

```bash
chmod 600 ~/.ssh/oumayma-key.pem
chmod +x scripts/execute-vm-fixes.sh
```

### Le script ne trouve pas le répertoire du projet

Vérifiez le chemin exact sur la VM :

```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86 "ls -la ~/ | grep hotel"
```

Puis modifiez `PROJECT_DIR` dans le script.

### Le backend ne démarre pas

Vérifiez les logs :

```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86 "docker logs hotel-ticket-hub-backend-staging --tail 50"
```
