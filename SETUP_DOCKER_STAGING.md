# 🐳 Configuration Docker pour Staging

## ✅ Étape 1 : Installer Docker sur la VM

Connectez-vous à la VM staging :

```bash
ssh -i github-actions-key ubuntu@13.49.44.219
```

Installez Docker et Docker Compose :

```bash
# Mettre à jour le système
sudo apt update

# Installer les dépendances
sudo apt install -y ca-certificates curl gnupg lsb-release

# Ajouter la clé GPG officielle de Docker
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Ajouter le repository Docker
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Installer Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Ajouter l'utilisateur ubuntu au groupe docker
sudo usermod -aG docker ubuntu

# Vérifier l'installation
docker --version
docker compose version

# Redémarrer la session SSH pour que les changements de groupe prennent effet
exit
```

Reconnectez-vous :

```bash
ssh -i github-actions-key ubuntu@13.49.44.219
```

## ✅ Étape 2 : Créer le répertoire de déploiement

```bash
sudo mkdir -p /opt/hotel-ticket-hub-backend-staging
sudo chown -R ubuntu:ubuntu /opt/hotel-ticket-hub-backend-staging
cd /opt/hotel-ticket-hub-backend-staging
```

## ✅ Étape 3 : Créer le fichier .env

Créez le fichier `.env` avec vos variables d'environnement :

```bash
nano /opt/hotel-ticket-hub-backend-staging/.env
```

Collez le contenu suivant (adaptez selon votre configuration) :

```bash
# Profil Spring Boot
SPRING_PROFILES_ACTIVE=staging

# Port du serveur (port externe:port interne)
SERVER_PORT=8081

# Base de données PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://13.61.27.43:5432/hotel_ticket_hub
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=votre_mot_de_passe_ici

# JWT
JWT_SECRET=YourSuperSecretJWTKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm
JWT_EXPIRATION=86400000

# CORS
CORS_ALLOWED_ORIGINS=http://51.21.196.104,http://localhost:5173

# Frontend URL
APP_FRONTEND_URL=http://51.21.196.104

# Stripe (optionnel)
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLIC_KEY=pk_test_...

# Mail (optionnel)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
```

Sauvegardez avec `Ctrl+O`, puis `Enter`, puis `Ctrl+X`.

Protégez le fichier :

```bash
chmod 600 /opt/hotel-ticket-hub-backend-staging/.env
```

## ✅ Étape 4 : Configurer l'accès à GitHub Container Registry

Pour que la VM puisse pull les images Docker depuis GitHub Container Registry, vous devez créer un **Personal Access Token (PAT)** :

### 4.1 Créer un PAT sur GitHub

1. Allez sur GitHub → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
2. Cliquez sur **Generate new token (classic)**
3. Donnez un nom : `Docker Registry Access`
4. Sélectionnez les scopes :
   - ✅ `read:packages` (pour pull les images)
   - ✅ `write:packages` (optionnel, si vous voulez push depuis la VM)
5. Cliquez sur **Generate token**
6. **Copiez le token** (vous ne pourrez plus le voir après)

### 4.2 Ajouter le token comme secret GitHub

1. Allez dans votre repository → **Settings** → **Secrets and variables** → **Actions**
2. Cliquez sur **New repository secret**
3. Nom : `GHCR_TOKEN`
4. Valeur : collez le token que vous venez de créer
5. Cliquez sur **Add secret**

### 4.3 Se connecter à GHCR sur la VM (optionnel, pour test manuel)

```bash
# Sur la VM
echo "VOTRE_PAT_TOKEN" | docker login ghcr.io -u VOTRE_USERNAME --password-stdin
```

## ✅ Étape 5 : Vérifier la configuration

```bash
# Vérifier que Docker fonctionne
docker ps

# Vérifier que le répertoire existe
ls -la /opt/hotel-ticket-hub-backend-staging/

# Vérifier le fichier .env
cat /opt/hotel-ticket-hub-backend-staging/.env
```

## 🚀 Déploiement automatique

Une fois configuré, chaque push vers `develop` déclenchera automatiquement :

1. **Build** : Construction de l'image Docker
2. **Push** : Envoi de l'image vers GitHub Container Registry
3. **Deploy** : Pull de l'image sur la VM et démarrage avec docker-compose

## 📝 Commandes utiles

```bash
# Voir les logs du conteneur
cd /opt/hotel-ticket-hub-backend-staging
docker-compose logs -f

# Voir le statut
docker-compose ps

# Redémarrer le conteneur
docker-compose restart

# Arrêter le conteneur
docker-compose down

# Voir les images Docker
docker images

# Nettoyer les images inutilisées
docker image prune -a
```

## 🔧 Dépannage

### Le conteneur ne démarre pas

```bash
# Voir les logs d'erreur
docker-compose logs

# Vérifier les variables d'environnement
docker-compose config
```

### Problème de connexion à la base de données

Vérifiez que :
- La VM de base de données est accessible depuis la VM backend
- Les Security Groups AWS autorisent le port 5432
- Les identifiants dans `.env` sont corrects

### Problème d'authentification GHCR

Vérifiez que :
- Le secret `GHCR_TOKEN` est bien configuré dans GitHub
- Le token a les permissions `read:packages`
- Le workflow utilise bien `secrets.GHCR_TOKEN` (et non `secrets.GITHUB_TOKEN`)

