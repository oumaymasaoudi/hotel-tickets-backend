# Guide de Test - Pipeline CI/CD

Ce guide explique comment tester le pipeline de déploiement et la partie release.

## 📋 Table des matières

1. [Test du Pipeline de Déploiement](#test-du-pipeline-de-déploiement)
2. [Test de la Release (Semantic Release)](#test-de-la-release-semantic-release)
3. [Vérification des Secrets](#vérification-des-secrets)
4. [Dépannage](#dépannage)

---

## 🚀 Test du Pipeline de Déploiement

### Prérequis

Avant de tester le déploiement, assurez-vous que les secrets suivants sont configurés dans GitHub :

1. **STAGING_HOST** : L'IP publique ou le domaine de votre serveur AWS
2. **STAGING_USER** : Le nom d'utilisateur SSH (généralement `ubuntu` ou `ec2-user`)
3. **STAGING_SSH_PRIVATE_KEY** : La clé privée SSH complète (avec les lignes `-----BEGIN...` et `-----END...`)

### Configuration des Secrets

1. Allez dans votre dépôt GitHub
2. **Settings** > **Secrets and variables** > **Actions**
3. Cliquez sur **New repository secret** pour chaque secret

### Test du Déploiement

#### Option 1 : Test via Push sur `main` ou `develop`

```bash
# Faire un petit changement
echo "# Test deployment" >> README.md
git add README.md
git commit -m "test: trigger deployment"
git push origin main
```

#### Option 2 : Test via Workflow Dispatch (si configuré)

1. Allez dans **Actions** > **Backend CI/CD Pipeline**
2. Cliquez sur **Run workflow**
3. Sélectionnez la branche (`main` ou `develop`)
4. Cliquez sur **Run workflow**

### Vérification du Déploiement

1. **Vérifier les logs GitHub Actions** :
   - Allez dans **Actions** > Sélectionnez le workflow en cours
   - Vérifiez le job **Backend - Deploy to Staging**
   - Les logs doivent montrer :
     - ✅ Connexion SSH réussie
     - ✅ Docker image pullée
     - ✅ Container démarré
     - ✅ Health check réussi

2. **Vérifier sur le serveur** :
   ```bash
   # Se connecter au serveur
   ssh -i votre-cle.pem utilisateur@votre-ip
   
   # Vérifier que le container tourne
   docker ps | grep hotel-ticket-hub-backend
   
   # Vérifier les logs
   docker logs hotel-ticket-hub-backend-staging
   
   # Tester l'endpoint
   curl http://localhost:8081/actuator/health
   ```

### Test du Monitoring

Le job **Backend - Deploy Monitoring** déploie automatiquement après le déploiement du backend.

**Vérification** :
- Prometheus : `http://votre-ip:9090`
- Grafana : `http://votre-ip:3000` (défaut: admin/admin)
- Node Exporter : `http://votre-ip:9100/metrics`

---

## 🏷️ Test de la Release (Semantic Release)

### Comment ça fonctionne

Semantic Release analyse les commits et crée automatiquement :
- Une nouvelle version (suivant [Semantic Versioning](https://semver.org/))
- Un tag Git
- Une release GitHub
- Une mise à jour du CHANGELOG.md
- Une mise à jour du pom.xml

### Format des commits

Semantic Release utilise les conventions de commit :
- `feat:` → Version mineure (1.0.0 → 1.1.0)
- `fix:` → Version patch (1.0.0 → 1.0.1)
- `BREAKING CHANGE:` ou `feat!:` → Version majeure (1.0.0 → 2.0.0)

### Test de la Release

#### Étape 1 : Créer un commit avec un type valide

```bash
# Exemple : Ajout d'une nouvelle fonctionnalité
git commit -m "feat: add user authentication endpoint"

# Ou une correction de bug
git commit -m "fix: resolve memory leak in ticket service"

# Ou un changement majeur (breaking change)
git commit -m "feat!: refactor API structure

BREAKING CHANGE: API endpoints have been restructured"
```

#### Étape 2 : Pousser sur `main`

```bash
git push origin main
```

#### Étape 3 : Vérifier la Release

1. **Vérifier le workflow** :
   - Allez dans **Actions** > Sélectionnez le workflow
   - Vérifiez que le job **Release** a réussi

2. **Vérifier les changements** :
   - Le fichier `CHANGELOG.md` doit être mis à jour
   - Le fichier `pom.xml` doit avoir la nouvelle version
   - Un nouveau commit `chore(release): X.X.X` doit être créé

3. **Vérifier GitHub** :
   - Allez dans **Releases** dans votre dépôt
   - Une nouvelle release doit être créée avec le tag de version
   - Les notes de release doivent être générées automatiquement

### Test sans créer de release

Pour tester sans créer une vraie release, utilisez le mode `dry-run` :

```bash
# Localement (nécessite Node.js)
npm install
npx semantic-release --dry-run
```

### Exemples de commits pour tester

```bash
# Test version patch (1.0.0 → 1.0.1)
git commit -m "fix: correct typo in error message"
git push origin main

# Test version mineure (1.0.1 → 1.1.0)
git commit -m "feat: add email notification feature"
git push origin main

# Test version majeure (1.1.0 → 2.0.0)
git commit -m "feat!: change authentication method

BREAKING CHANGE: JWT tokens are now required for all API calls"
git push origin main
```

---

## 🔐 Vérification des Secrets

### Secrets pour le Déploiement

| Secret | Description | Exemple |
|-------|-------------|---------|
| `STAGING_HOST` | IP publique ou domaine du serveur | `54.123.45.67` ou `staging.example.com` |
| `STAGING_USER` | Utilisateur SSH | `ubuntu` ou `ec2-user` |
| `STAGING_SSH_PRIVATE_KEY` | Clé privée SSH complète | Contenu du fichier `.pem` |

### Secrets pour le Monitoring

| Secret | Description | Exemple |
|-------|-------------|---------|
| `MONITORING_HOST` | IP publique ou domaine du serveur de monitoring | `54.123.45.68` |
| `MONITORING_USER` | Utilisateur SSH | `ubuntu` |
| `MONITORING_SSH_PRIVATE_KEY` | Clé privée SSH complète | Contenu du fichier `.pem` |

### Vérifier que les secrets sont configurés

1. Allez dans **Settings** > **Secrets and variables** > **Actions**
2. Vérifiez que tous les secrets listés ci-dessus existent
3. Si un secret manque, cliquez sur **New repository secret**

---

## 🔧 Dépannage

### Problème : Déploiement échoue avec erreur SSH

**Solutions** :
1. Vérifiez que les secrets sont bien configurés
2. Vérifiez le Security Group AWS :
   - Port 22 (SSH) doit être ouvert depuis `0.0.0.0/0`
3. Vérifiez que l'instance EC2 est en cours d'exécution
4. Testez la connexion SSH localement :
   ```bash
   ssh -i votre-cle.pem utilisateur@votre-ip
   ```

### Problème : Release ne se déclenche pas

**Solutions** :
1. Vérifiez que vous êtes sur la branche `main`
2. Vérifiez le format du commit (doit commencer par `feat:`, `fix:`, etc.)
3. Vérifiez que le token `GITHUB_TOKEN` a les permissions `contents: write`
4. Vérifiez les logs du job **Release** dans GitHub Actions

### Problème : Semantic Release ne crée pas de version

**Solutions** :
1. Vérifiez que le commit respecte les conventions :
   - `feat:` pour nouvelles fonctionnalités
   - `fix:` pour corrections de bugs
   - `BREAKING CHANGE:` pour changements majeurs
2. Vérifiez que `.releaserc.json` est correctement configuré
3. Vérifiez les logs du job **Release** pour voir les erreurs

### Problème : Docker Build échoue

**Solutions** :
1. Vérifiez que `Dockerfile` existe à la racine du projet
2. Vérifiez que le build fonctionne localement :
   ```bash
   docker build -t test-image .
   ```
3. Vérifiez les logs du job **Backend - Docker Build & Push**

---

## 📊 Monitoring du Pipeline

### Vérifier l'état du pipeline

1. Allez dans **Actions** dans votre dépôt GitHub
2. Sélectionnez le workflow **Backend CI/CD Pipeline**
3. Vérifiez l'état de chaque job :
   - ✅ Vert = Succès
   - ⚠️ Jaune = En cours ou avertissement
   - ❌ Rouge = Échec

### Artifacts générés

Le pipeline génère plusieurs artifacts :
- `backend-jar` : Le JAR compilé
- `jacoco-report` : Rapport de couverture de code
- `test-reports` : Rapports de tests
- `checkstyle-report` : Rapport de style de code

Pour télécharger un artifact :
1. Allez dans **Actions** > Sélectionnez un workflow
2. Faites défiler jusqu'à **Artifacts**
3. Cliquez sur l'artifact à télécharger

---

## ✅ Checklist de Test

### Déploiement
- [ ] Secrets GitHub configurés (STAGING_HOST, STAGING_USER, STAGING_SSH_PRIVATE_KEY)
- [ ] Security Group AWS configuré (port 22 ouvert)
- [ ] Instance EC2 en cours d'exécution
- [ ] Test SSH local réussi
- [ ] Push sur `main` ou `develop` déclenche le déploiement
- [ ] Container Docker démarre correctement
- [ ] Health check réussi (`/actuator/health`)

### Release
- [ ] Commit avec format valide (`feat:`, `fix:`, etc.)
- [ ] Push sur `main` déclenche la release
- [ ] CHANGELOG.md mis à jour
- [ ] pom.xml mis à jour avec la nouvelle version
- [ ] Tag Git créé
- [ ] Release GitHub créée

---

## 📚 Ressources

- [Semantic Release Documentation](https://semantic-release.gitbook.io/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
