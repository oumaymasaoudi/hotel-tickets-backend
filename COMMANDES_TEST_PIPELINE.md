# 🧪 Commandes pour Tester les Pipelines et les Tests

## 📋 Table des matières
1. [Tests locaux](#tests-locaux)
2. [Build Docker local](#build-docker-local)
3. [Commandes Git pour déclencher le pipeline](#commandes-git)
4. [Vérifier les résultats](#vérifier-les-résultats)

---

## 🧪 Tests locaux

### 1. Tests unitaires

```bash
cd hotel-ticket-hub-backend

# Lancer tous les tests
mvn clean test

# Lancer les tests avec affichage détaillé
mvn clean test -X

# Lancer les tests d'une classe spécifique
mvn test -Dtest=AuthServiceTest

# Lancer les tests sans compilation
mvn surefire:test
```

### 2. Lint & Code Quality

```bash
# Checkstyle (vérification du style de code)
mvn checkstyle:check

# SpotBugs (détection de bugs)
mvn spotbugs:spotbugs -Duser.language=en -Duser.country=US

# Voir le rapport SpotBugs
# Ouvrir: target/spotbugsXml.html dans un navigateur
```

### 3. Coverage (Couverture de code)

```bash
# Générer le rapport de couverture
mvn clean test jacoco:report

# Voir le rapport
# Ouvrir: target/site/jacoco/index.html dans un navigateur

# Vérifier le seuil de couverture
mvn jacoco:check
```

### 4. Build complet

```bash
# Build sans tests (pour tester rapidement)
mvn clean package -DskipTests

# Build avec tests
mvn clean package

# Build avec tous les rapports
mvn clean verify
```

### 5. SonarQube local (optionnel)

```bash
# Analyser avec SonarQube (nécessite un serveur SonarQube local)
mvn sonar:sonar \
  -Dsonar.projectKey=hotel-ticket-hub-backend \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your-token
```

---

## 🐳 Build Docker local

### 1. Build l'image Docker

```bash
cd hotel-ticket-hub-backend

# Build l'image
docker build -t ticket-hub-backend:local .

# Build avec tag spécifique
docker build -t ticket-hub-backend:test -t ticket-hub-backend:latest .
```

### 2. Tester l'image Docker

```bash
# Run l'image en local (sans docker-compose)
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=staging \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/hotel_ticket_hub \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  --name backend-test \
  ticket-hub-backend:local

# Voir les logs
docker logs -f backend-test

# Arrêter le conteneur
docker stop backend-test
docker rm backend-test
```

### 3. Tester avec docker-compose

```bash
cd hotel-ticket-hub-backend

# Créer un fichier .env.local pour les tests
cat > .env.local << EOF
SPRING_PROFILES_ACTIVE=staging
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/hotel_ticket_hub
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
JWT_SECRET=YourSuperSecretJWTKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm
JWT_EXPIRATION=86400000
EOF

# Démarrer avec docker-compose
export DOCKER_IMAGE=ticket-hub-backend:local
docker compose --env-file .env.local up -d

# Voir les logs
docker compose logs -f

# Arrêter
docker compose down
```

---

## 📤 Commandes Git

### 1. Vérifier l'état

```bash
cd hotel-ticket-hub-backend

# Voir les fichiers modifiés
git status

# Voir les différences
git diff

# Voir les fichiers modifiés (résumé)
git status -s
```

### 2. Préparer le commit

```bash
# Ajouter tous les fichiers modifiés
git add .

# Ou ajouter des fichiers spécifiques
git add .github/workflows/ci.yml
git add Dockerfile
git add docker-compose.yml
git add .dockerignore
```

### 3. Commit

```bash
# Commit avec message descriptif
git commit -m "feat: Migration vers déploiement Docker

- Ajout Dockerfile pour build de l'image
- Ajout docker-compose.yml pour déploiement
- Fusion des workflows CI/CD en un seul fichier
- Amélioration du script de déploiement staging"

# Ou message court
git commit -m "feat: déploiement Docker pour staging"
```

### 4. Push vers develop (déclenche le pipeline)

```bash
# Vérifier la branche actuelle
git branch

# Si vous n'êtes pas sur develop
git checkout develop

# Push vers develop
git push origin develop

# Ou si c'est la première fois
git push -u origin develop
```

### 5. Créer une branche pour tester (recommandé)

```bash
# Créer une nouvelle branche
git checkout -b test/docker-deployment

# Faire vos modifications et commits
git add .
git commit -m "test: déploiement Docker"

# Push la branche
git push -u origin test/docker-deployment

# Créer une Pull Request vers develop sur GitHub
# Une fois validée, merge vers develop déclenchera le pipeline
```

---

## ✅ Vérifier les résultats

### 1. Sur GitHub

```bash
# Après le push, allez sur GitHub :
# https://github.com/VOTRE_USERNAME/hotel-tickets-backend/actions

# Ou via la ligne de commande (si vous avez GitHub CLI)
gh run list
gh run watch
```

### 2. Vérifier les jobs du pipeline

Le pipeline `Backend CI/CD Pipeline` devrait exécuter dans l'ordre :

1. ✅ **Backend - Lint & Code Quality**
2. ✅ **Backend - Unit Tests**
3. ✅ **Backend - Code Coverage**
4. ✅ **Backend - Build**
5. ✅ **Backend - SonarQube Analysis**
6. ✅ **Backend - Docker Build & Push** (uniquement sur `develop`)
7. ✅ **Backend - Deploy to Staging** (uniquement sur `develop`)

### 3. Vérifier le déploiement sur la VM

```bash
# Se connecter à la VM
ssh -i github-actions-key ubuntu@13.49.44.219

# Vérifier que le conteneur tourne
docker ps

# Voir les logs
cd /opt/hotel-ticket-hub-backend-staging
docker compose logs -f

# Tester l'API
curl http://localhost:8081/api/auth/login
```

---

## 🚀 Workflow complet de test

### Option 1 : Test rapide (sans push)

```bash
cd hotel-ticket-hub-backend

# 1. Tests locaux
mvn clean test

# 2. Build Docker
docker build -t ticket-hub-backend:test .

# 3. Vérifier que tout compile
mvn clean package -DskipTests
```

### Option 2 : Test complet avec push

```bash
cd hotel-ticket-hub-backend

# 1. Vérifier l'état
git status

# 2. Ajouter les fichiers
git add .

# 3. Commit
git commit -m "test: déploiement Docker"

# 4. Push vers develop
git push origin develop

# 5. Suivre le pipeline sur GitHub Actions
# https://github.com/VOTRE_USERNAME/hotel-tickets-backend/actions
```

---

## 🐛 Dépannage

### Les tests échouent localement

```bash
# Nettoyer et relancer
mvn clean test

# Voir les détails d'erreur
mvn test -X

# Vérifier les rapports
cat target/surefire-reports/*.txt
```

### Le build Docker échoue

```bash
# Voir les logs détaillés
docker build -t ticket-hub-backend:test . --progress=plain --no-cache

# Vérifier les fichiers nécessaires
ls -la Dockerfile
ls -la pom.xml
ls -la checkstyle.xml
```

### Le pipeline GitHub Actions échoue

1. Allez sur **Actions** → Cliquez sur le workflow qui a échoué
2. Cliquez sur le job qui a échoué
3. Cliquez sur l'étape qui a échoué
4. Lisez les logs d'erreur

### Le déploiement échoue

```bash
# Se connecter à la VM
ssh -i github-actions-key ubuntu@13.49.44.219

# Vérifier les logs Docker
cd /opt/hotel-ticket-hub-backend-staging
docker compose logs

# Vérifier que le fichier .env existe
cat .env

# Vérifier les conteneurs
docker ps -a
```

---

## 📝 Commandes rapides (résumé)

```bash
# Tests
mvn clean test

# Build
mvn clean package

# Docker
docker build -t ticket-hub-backend:local .

# Git
git add . && git commit -m "test" && git push origin develop
```

---

## 🎯 Checklist avant de push

- [ ] Tests locaux passent : `mvn clean test`
- [ ] Build local fonctionne : `mvn clean package`
- [ ] Docker build fonctionne : `docker build -t test .`
- [ ] Fichier `.env` créé sur la VM (pour le déploiement)
- [ ] Secret `GHCR_TOKEN` configuré sur GitHub
- [ ] Tous les fichiers sont commités
- [ ] Message de commit descriptif

---

**💡 Astuce** : Testez toujours localement avant de push pour éviter les erreurs dans le pipeline !

