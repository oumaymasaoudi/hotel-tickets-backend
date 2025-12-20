# Guide SonarQube - Configuration Complète

## Option 1 : SonarCloud (Recommandé - Gratuit pour les projets open source)

### Étape 1 : Créer un compte SonarCloud

1. Aller sur **https://sonarcloud.io**
2. Cliquer sur **"Log in"** en haut à droite
3. Choisir **"Log in with GitHub"** (recommandé)
4. Autoriser SonarCloud à accéder à votre compte GitHub

### Étape 2 : Créer un projet

1. Une fois connecté, cliquer sur **"+"** en haut à droite
2. Sélectionner **"Analyze new project"**
3. Choisir **"From GitHub"**
4. Sélectionner votre organisation GitHub
5. Choisir le repository **`hotel-tickets-backend`** (ou votre repo backend)
6. Cliquer sur **"Set Up"**

### Étape 3 : Générer un token

1. Aller dans **"My Account"** (icône profil en haut à droite)
2. Cliquer sur **"Security"** dans le menu
3. Dans la section **"Generate Tokens"** :
   - **Token name** : `hotel-tickets-backend` (ou un nom descriptif)
   - Cliquer sur **"Generate"**
4. **IMPORTANT** : Copier le token immédiatement (il ne sera plus visible après)
   - Exemple : `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0`

### Étape 4 : Configurer les secrets GitHub

1. Aller sur votre repository GitHub : **`https://github.com/oumaymasaoudi/hotel-tickets-backend`**
2. Cliquer sur **"Settings"** (en haut du repo)
3. Dans le menu de gauche, cliquer sur **"Secrets and variables"** > **"Actions"**
4. Cliquer sur **"New repository secret"**
5. Ajouter les secrets suivants :

   **Secret 1 : SONAR_TOKEN**
   - **Name** : `SONAR_TOKEN`
   - **Secret** : Coller le token généré à l'étape 3
   - Cliquer sur **"Add secret"**

   **Secret 2 : SONAR_HOST_URL**
   - **Name** : `SONAR_HOST_URL`
   - **Secret** : `https://sonarcloud.io`
   - Cliquer sur **"Add secret"**

### Étape 5 : Récupérer la clé du projet SonarCloud

1. Retourner sur SonarCloud
2. Aller dans votre projet
3. Cliquer sur **"Project Information"** (ou "Project Settings")
4. Copier la **"Project Key"** (exemple : `oumaymasaoudi_hotel-tickets-backend`)
5. Copier aussi l'**"Organization Key"** (exemple : `oumaymasaoudi`)

### Étape 6 : Configurer sonar-project.properties

Le fichier `sonar-project.properties` doit contenir :

```properties
sonar.projectKey=oumaymasaoudi_hotel-tickets-backend
sonar.organization=oumaymasaoudi
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.language=java
sonar.sourceEncoding=UTF-8
```

**Remplacez** `oumaymasaoudi_hotel-tickets-backend` et `oumaymasaoudi` par vos propres clés.

---

## Option 2 : SonarQube Self-Hosted (Sur votre VM)

### Étape 1 : Installer SonarQube avec Docker

```bash
# Démarrer SonarQube avec Docker Compose
docker-compose -f docker-compose.sonarqube.yml up -d

# Attendre que SonarQube soit prêt (environ 1-2 minutes)
# Vérifier les logs
docker-compose -f docker-compose.sonarqube.yml logs -f sonarqube
```

### Étape 2 : Accéder à SonarQube

1. Ouvrir votre navigateur : **http://localhost:9000** (ou l'IP de votre VM)
2. **Première connexion** :
   - Username : `admin`
   - Password : `admin`
3. SonarQube vous demandera de changer le mot de passe
4. Créer un nouveau mot de passe (exemple : `Admin123!`)

### Étape 3 : Créer un projet

1. Cliquer sur **"Create Project"**
2. Choisir **"Manually"**
3. **Project key** : `hotel-tickets-backend`
4. **Display name** : `Hotel Tickets Backend`
5. Cliquer sur **"Set Up"**

### Étape 4 : Générer un token

1. Cliquer sur votre profil (en haut à droite)
2. Aller dans **"My Account"** > **"Security"**
3. Dans **"Generate Tokens"** :
   - **Name** : `github-actions`
   - Cliquer sur **"Generate"**
4. **Copier le token** (exemple : `squ_abc123def456...`)

### Étape 5 : Configurer les secrets GitHub

1. Sur GitHub, aller dans **Settings** > **Secrets and variables** > **Actions**
2. Ajouter les secrets :

   **SONAR_TOKEN** : Le token généré
   **SONAR_HOST_URL** : `http://VOTRE_IP:9000` (exemple : `http://192.168.1.100:9000`)

### Étape 6 : Configurer sonar-project.properties

```properties
sonar.projectKey=hotel-tickets-backend
sonar.host.url=http://VOTRE_IP:9000
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.language=java
sonar.sourceEncoding=UTF-8
```

**⚠️ Important** : Si votre SonarQube est sur une VM privée, GitHub Actions ne pourra pas y accéder. Il faut :
- Soit exposer SonarQube publiquement (avec sécurité)
- Soit utiliser SonarCloud (recommandé)

---

## Tester SonarQube Localement

### Backend

```bash
cd hotel-ticket-hub-backend

# 1. Générer le rapport de couverture
mvn clean test jacoco:report

# 2. Analyser avec SonarQube
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=VOTRE_TOKEN
```

### Frontend

```bash
cd hotel-ticket-hub

# 1. Générer le rapport de couverture
npm test -- --coverage --watchAll=false

# 2. Analyser avec SonarQube (si sonar-scanner est installé)
sonar-scanner \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=VOTRE_TOKEN
```

---

## Vérifier que ça fonctionne

1. **Push sur GitHub** : Faire un commit et push sur `main` ou `develop`
2. **Vérifier GitHub Actions** :
   - Aller dans l'onglet **"Actions"** de votre repo
   - Vérifier que le job **"SonarQube Analysis"** s'exécute
   - Si erreur, vérifier les logs
3. **Vérifier SonarCloud/SonarQube** :
   - Aller sur votre projet SonarCloud/SonarQube
   - Vous devriez voir l'analyse avec :
     - Qualité du code
     - Duplication
     - Bugs détectés
     - Couverture de code

---

## Résolution de problèmes

### Erreur : "Authentication failed"

- Vérifier que le token est correct dans les secrets GitHub
- Vérifier que `SONAR_HOST_URL` est correct

### Erreur : "Project key not found"

- Vérifier que le projet existe dans SonarCloud/SonarQube
- Vérifier que `sonar-project.properties` contient la bonne clé

### Erreur : "Connection refused" (Self-hosted)

- Vérifier que SonarQube est démarré : `docker ps`
- Vérifier que le port 9000 est accessible
- Pour GitHub Actions, SonarQube doit être accessible publiquement

### Le job SonarQube est skipped

- Vérifier que vous avez push sur `main` ou `develop`
- Le job ne s'exécute que sur ces branches (configuré dans le workflow)

---

## Recommandation

Pour un projet académique, **SonarCloud est la meilleure option** :
- ✅ Gratuit
- ✅ Pas besoin de serveur
- ✅ Accessible depuis GitHub Actions
- ✅ Facile à configurer

