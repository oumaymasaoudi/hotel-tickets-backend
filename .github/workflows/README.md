# 🚀 Workflows GitHub Actions - Documentation

## 📋 Architecture

### Séparation des responsabilités

- **VM Backend** (`13.49.44.219`) : Uniquement le code backend (Spring Boot)
  - `docker-compose.yml` : Contient **seulement** le service backend
  - Pas de services de monitoring sur cette VM

- **VM Monitoring/Ansible** (`13.62.53.224`) : Stack de monitoring complète
  - Prometheus, Grafana, Alertmanager
  - Node Exporter, cAdvisor
  - Collecte les métriques depuis la VM Backend (remote scraping)

## 🔐 Secrets GitHub nécessaires

### Pour le déploiement Backend (`ci.yml`)

| Secret | Description | Exemple |
|--------|-------------|---------|
| `STAGING_HOST` | IP de la VM Backend | `13.49.44.219` |
| `STAGING_USER` | Utilisateur SSH | `ubuntu` |
| `STAGING_SSH_PRIVATE_KEY` | Clé privée SSH pour la VM Backend | `-----BEGIN RSA PRIVATE KEY-----...` |
| `GHCR_TOKEN` | Token GitHub Container Registry | `ghp_...` |
| `MONITORING_HOST` | IP de la VM Monitoring/Ansible | `13.62.53.224` |
| `MONITORING_USER` | Utilisateur SSH pour la VM Monitoring | `ubuntu` |
| `MONITORING_SSH_PRIVATE_KEY` | Clé privée SSH pour la VM Monitoring | `-----BEGIN RSA PRIVATE KEY-----...` |

### Pour le déploiement Monitoring (`deploy-monitoring.yml`)

| Secret | Description | Exemple |
|--------|-------------|---------|
| `MONITORING_HOST` | IP de la VM Monitoring/Ansible | `13.62.53.224` |
| `MONITORING_USER` | Utilisateur SSH | `ubuntu` |
| `MONITORING_SSH_PRIVATE_KEY` | Clé privée SSH pour la VM Monitoring | `-----BEGIN RSA PRIVATE KEY-----...` |

## 📝 Workflows disponibles

### 1. `ci.yml` - Pipeline CI/CD Backend

**Déclenchement :**
- Push sur `main` ou `develop`
- Pull Request sur `main` ou `develop`

**Jobs :**
- `lint` : Vérification du code (Checkstyle, SpotBugs)
- `test` : Tests unitaires avec JaCoCo
- `coverage` : Génération du rapport de couverture
- `build` : Build Maven et création du JAR
- `docker-build` : Build et push de l'image Docker (branche `develop`)
- `deploy-staging` : Déploiement sur la VM Backend (branche `develop`)
- `deploy-monitoring` : Déploiement de la stack Monitoring sur la VM Monitoring (branche `develop`, après `deploy-staging`)
- `sonar` : Analyse SonarQube
- `release` : Release automatique (branche `main`)

**Optimisations SSH :**
- ✅ Configuration SSH optimisée avec `~/.ssh/config`
- ✅ Timeout réduit pour SCP (30s au lieu de 60s)
- ✅ Ciphers sécurisés (aes128-ctr, aes192-ctr, aes256-ctr)
- ✅ Gestion propre des clés SSH (cleanup automatique)
- ✅ Mode strict avec `set -euo pipefail`

### 2. `deploy-monitoring.yml` - Déploiement Monitoring

**Déclenchement :**
- Workflow manuel (`workflow_dispatch`)
- Push sur `main` ou `develop` si fichiers `monitoring/**` modifiés

**Actions :**
- Copie uniquement les fichiers du dossier `monitoring/`
- Déploie Prometheus, Grafana, Alertmanager sur la VM Monitoring
- Utilise `prometheus-remote.yml` pour scraper le backend distant

**Optimisations :**
- ✅ Envoie uniquement le dossier `monitoring/` (pas tout le repo)
- ✅ Utilise `strip_components: 1` pour éviter le préfixe
- ✅ Configuration SSH optimisée
- ✅ Vérification des services après déploiement

### 3. `check-backend-status.yml` - Vérification Backend

**Déclenchement :**
- Workflow manuel (`workflow_dispatch`)

**Actions :**
- Vérifie l'état du backend sur la VM
- Teste les endpoints (health, prometheus)
- Affiche les logs récents

## 🔧 Optimisations SCP/SSH

### Pourquoi c'est rapide maintenant ?

1. **Source précise** : On n'envoie que les fichiers nécessaires
   - Backend : `docker-compose.yml` uniquement
   - Monitoring : `monitoring/` uniquement

2. **Pas de fichiers lourds** : 
   - ❌ Pas de `target/` (build Maven)
   - ❌ Pas de `.git/`
   - ❌ Pas de `node_modules/`
   - ❌ Pas de `dist/`

3. **Configuration SSH optimisée** :
   - `ConnectTimeout: 10` (échec rapide si connexion impossible)
   - `ServerAliveInterval: 60` (maintient la connexion)
   - Ciphers sécurisés et performants

4. **Concurrency** :
   - `cancel-in-progress: false` : Les déploiements ne s'annulent pas mutuellement
   - Chaque push attend la fin du déploiement précédent

## 🚨 En cas de problème

### Le déploiement est lent

1. Vérifiez que vous n'envoyez pas trop de fichiers
2. Vérifiez la connexion réseau vers la VM
3. Vérifiez les logs du workflow GitHub Actions

### Le déploiement est annulé

1. Vérifiez la configuration `concurrency` dans le workflow
2. Vérifiez si plusieurs commits ont été poussés rapidement
3. Vérifiez les timeouts (peut-être trop courts)

### Erreur SSH

1. Vérifiez que les secrets sont correctement configurés
2. Vérifiez que la clé SSH est valide
3. Vérifiez que l'utilisateur SSH a les permissions nécessaires

## 📚 Références

- [appleboy/scp-action](https://github.com/appleboy/scp-action)
- [appleboy/ssh-action](https://github.com/appleboy/ssh-action)
- [GitHub Actions - SSH](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idsteps)
