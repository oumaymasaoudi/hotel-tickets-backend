# 🐳 Déploiement Docker - Guide Complet

## 📋 Résumé des changements

Le déploiement a été migré de **déploiement direct JAR** vers **déploiement Docker**.

### Fichiers créés

1. **`Dockerfile`** : Image Docker multi-stage pour le backend Spring Boot
2. **`docker-compose.yml`** : Configuration Docker Compose pour le déploiement
3. **`.dockerignore`** : Fichiers à exclure lors de la construction Docker
4. **`SETUP_DOCKER_STAGING.md`** : Guide de configuration Docker sur la VM

### Fichiers modifiés

1. **`.github/workflows/backend-staging.yml`** : 
   - Construction de l'image Docker
   - Push vers GitHub Container Registry (ghcr.io)
   - Déploiement avec docker-compose sur la VM

## 🚀 Architecture du déploiement

```
┌─────────────────┐
│  GitHub Actions │
│                 │
│  1. Build JAR   │
│  2. Build Image │
│  3. Push to GHCR│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  GitHub         │
│  Container      │
│  Registry       │
│  (ghcr.io)      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Staging VM    │
│                 │
│  docker-compose │
│  pull & run     │
└─────────────────┘
```

## ✅ Étapes de configuration

### 1. Configurer Docker sur la VM

Suivez le guide **`SETUP_DOCKER_STAGING.md`** pour :
- Installer Docker et Docker Compose
- Créer le répertoire de déploiement
- Configurer le fichier `.env`

### 2. Créer un Personal Access Token GitHub

1. GitHub → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
2. **Generate new token (classic)**
3. Nom : `Docker Registry Access`
4. Scopes : `read:packages` (et `write:packages` si nécessaire)
5. **Generate token** et copiez-le

### 3. Ajouter le secret GitHub

1. Repository → **Settings** → **Secrets and variables** → **Actions**
2. **New repository secret**
3. Nom : `GHCR_TOKEN`
4. Valeur : collez le token créé à l'étape 2
5. **Add secret**

### 4. Vérifier les secrets existants

Assurez-vous que ces secrets sont configurés :
- ✅ `STAGING_HOST` : IP de la VM (ex: `13.49.44.219`)
- ✅ `STAGING_USER` : Utilisateur SSH (ex: `ubuntu`)
- ✅ `STAGING_SSH_PRIVATE_KEY` : Clé privée SSH
- ✅ `GHCR_TOKEN` : Token GitHub pour accéder au registry (nouveau)

## 🔄 Flux de déploiement automatique

1. **Push vers `develop`** → Déclenche le workflow
2. **Build** : Construction de l'image Docker
3. **Push** : Envoi vers `ghcr.io/OWNER/REPO/backend:develop`
4. **Deploy** : 
   - Connexion SSH à la VM
   - Pull de la nouvelle image
   - Arrêt de l'ancien conteneur
   - Démarrage du nouveau conteneur avec docker-compose

## 📝 Structure sur la VM

```
/opt/hotel-ticket-hub-backend-staging/
├── docker-compose.yml    # Copié automatiquement par le workflow
├── .env                  # Variables d'environnement (à créer manuellement)
└── uploads/              # Volume monté pour les fichiers uploadés
```

## 🧪 Tester le déploiement

### Test manuel (sur la VM)

```bash
# Se connecter à la VM
ssh -i github-actions-key ubuntu@13.49.44.219

# Aller dans le répertoire
cd /opt/hotel-ticket-hub-backend-staging

# Se connecter à GHCR
echo "VOTRE_GHCR_TOKEN" | docker login ghcr.io -u VOTRE_USERNAME --password-stdin

# Pull l'image
docker pull ghcr.io/OWNER/REPO/backend:develop

# Démarrer avec docker-compose
export DOCKER_IMAGE=ghcr.io/OWNER/REPO/backend:develop
docker compose up -d

# Voir les logs
docker compose logs -f
```

### Test automatique

1. Faites un commit et push vers `develop`
2. Allez dans **Actions** sur GitHub
3. Suivez l'exécution du workflow `Backend - Staging Deploy (Docker)`
4. Vérifiez que l'application démarre :

```bash
# Sur la VM
docker ps
curl http://localhost:8081/api/auth/login
```

## 🔧 Commandes utiles

### Sur la VM

```bash
# Voir les conteneurs
docker ps

# Voir les logs
cd /opt/hotel-ticket-hub-backend-staging
docker compose logs -f

# Redémarrer
docker compose restart

# Arrêter
docker compose down

# Voir les images
docker images

# Nettoyer
docker image prune -a
```

### Localement (pour tester le build)

```bash
# Build l'image
docker build -t ticket-hub-backend:local .

# Run l'image
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=staging \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://... \
  ticket-hub-backend:local
```

## ⚠️ Points importants

1. **Fichier `.env`** : Doit être créé manuellement sur la VM (voir `SETUP_DOCKER_STAGING.md`)
2. **Token GHCR** : Le secret `GHCR_TOKEN` doit être configuré dans GitHub
3. **Port** : Le port externe est configuré dans `.env` (`SERVER_PORT=8081`)
4. **Volumes** : Le dossier `uploads/` est monté comme volume pour persister les fichiers
5. **Health check** : L'image Docker inclut un health check automatique

## 🐛 Dépannage

### L'image ne se build pas

```bash
# Vérifier les logs GitHub Actions
# Vérifier que le Dockerfile est correct
docker build -t test .  # Test local
```

### Le conteneur ne démarre pas

```bash
# Voir les logs
docker compose logs

# Vérifier les variables d'environnement
docker compose config
```

### Erreur d'authentification GHCR

- Vérifier que `GHCR_TOKEN` est bien configuré
- Vérifier que le token a les permissions `read:packages`
- Tester la connexion manuelle : `docker login ghcr.io`

### Le port n'est pas accessible

- Vérifier les Security Groups AWS (port 8081)
- Vérifier que le conteneur écoute : `docker ps`
- Tester localement : `curl http://localhost:8081`

## 📚 Ressources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)

