# Workflow Git Flow - Hotel Ticket Hub Backend

## 📋 Vue d'ensemble

Ce projet utilise un workflow **Git Flow** avec intégration CI/CD automatique.

## 🔄 Étapes du workflow

### 1️⃣ **Développement : Feature Branch → PR vers `develop`**

```bash
# Créer une branche feature
git checkout -b feature/ma-feature

# Faire vos modifications et commits
git add .
git commit -m "feat: Ajouter nouvelle fonctionnalité"

# Pousser la branche
git push -u origin feature/ma-feature
```

**👉 Ouvrir une Pull Request vers `develop`**

**Pipeline déclenché :**
- ✅ Lint & Code Quality
- ✅ Unit Tests
- ✅ Code Coverage
- ✅ OWASP Dependency Check (permissif)
- ✅ Security Linting - Trivy (permissif)
- ✅ Build
- ✅ SonarCloud Analysis (permissif)
- ❌ Docker Build (uniquement sur push, pas sur PR)
- ❌ Deploy (uniquement sur push vers `develop`)

**⚠️ Important :** Le pipeline ne se déclenche que si vous modifiez des fichiers dans :
- `src/**`
- `pom.xml`
- `Dockerfile`
- `docker-compose*.yml`
- `.github/workflows/**`
- `monitoring/**`
- `scripts/**`
- `checkstyle.xml`
- `spotbugs-exclude.xml`
- `owasp-dependency-check-suppressions.xml`
- `infrastructure/**`
- `.env.example`
- `Makefile`

### 2️⃣ **Validation : Merge vers `develop` → Staging**

Une fois la PR approuvée et mergée vers `develop` :

**Pipeline déclenché (push vers `develop`) :**
- ✅ Tous les jobs de l'étape 1 (bloquants)
- ✅ Docker Build & Push
- ✅ **Deploy to Staging** 🚀
- ✅ **Deploy Monitoring Stack** 📊

**Résultat :**
- Application déployée sur l'environnement **staging**
- Monitoring (Prometheus, Grafana) déployé et configuré
- Tests d'intégration possibles sur staging

### 3️⃣ **Release : PR `develop` → `main` → Release**

Quand vous êtes prêt à sortir une version :

```bash
# Créer une PR de develop vers main sur GitHub
# Après merge sur main...
```

**Pipeline déclenché (push vers `main`) :**
- ✅ Tous les jobs de l'étape 1 (**bloquants** sur `main`)
- ✅ Docker Build & Push
- ✅ **Release** (semantic-release) 🏷️
- ✅ **Docker Tag Release Version** (tag `vX.Y.Z` et `latest`)

**Résultat :**
- Version créée automatiquement (semantic-release)
- Tag Git créé (`vX.Y.Z`)
- Image Docker taguée avec la version et `latest`
- Changelog généré automatiquement

## 🎯 Récapitulatif simple

| Étape | Branche | Action | Pipeline | Déploiement |
|-------|---------|--------|----------|-------------|
| **Développement** | `feature/*` | PR → `develop` | Tests uniquement | ❌ |
| **Validation** | `develop` | Push/Merge | Tests + Build + Docker | ✅ Staging + Monitoring |
| **Release** | `main` | Push/Merge | Tests + Build + Release | ✅ Production (via tags) |

## 🔒 Règles de sécurité

### Sur `main` (bloquant) :
- ✅ Tous les tests doivent passer
- ✅ Coverage ≥ 80% (nouveau code)
- ✅ SonarCloud Quality Gate = PASS
- ✅ OWASP Dependency Check (CVSS < 7)
- ✅ Trivy Security Scan (CRITICAL/HIGH bloquent)
- ✅ Docker Build uniquement si Sonar Quality Gate OK

### Sur `develop` (permissif) :
- ⚠️ Tests peuvent échouer (warning seulement)
- ⚠️ Security scans permissifs (warnings)
- ✅ Déploiement staging même si certains checks échouent

### Sur les PR :
- ✅ Tests bloquants (même logique que la branche cible)
- ✅ PR vers `main` = bloquant (comme push sur `main`)
- ✅ PR vers `develop` = permissif (comme push sur `develop`)

## 📝 Exemple concret

### Scénario : Ajouter une nouvelle fonctionnalité

```bash
# 1. Créer la branche feature
git checkout -b feature/nouvelle-fonctionnalite

# 2. Développer et commiter
git add src/main/java/com/hotel/tickethub/service/NouveauService.java
git commit -m "feat: Ajouter NouveauService"

# 3. Pousser et créer PR vers develop
git push -u origin feature/nouvelle-fonctionnalite
# → Aller sur GitHub et créer la PR vers develop

# 4. Après review et merge vers develop
# → Le pipeline déploie automatiquement sur staging

# 5. Tester sur staging, puis créer PR develop → main
# → Après merge sur main, release automatique
```

## ⚠️ Points d'attention

1. **Paths filter** : Modifiez uniquement les fichiers listés dans `paths:` pour déclencher le pipeline
2. **PR vs Push** : Les PR ne déclenchent pas les déploiements, seulement les tests
3. **Branche cible** : Le comportement (bloquant/permissif) dépend de la branche **cible** de la PR, pas de la branche source
4. **Semantic Release** : Nécessite `package.json` pour fonctionner (création automatique de tags)

## 🔗 Liens utiles

- **Pipeline GitHub Actions** : `.github/workflows/ci.yml`
- **SonarCloud** : https://sonarcloud.io/project/overview?id=oumaymasaoudi_hotel-tickets-backend
- **Docker Registry** : `ghcr.io/oumaymasaoudi/hotel-tickets-backend/backend`

## 📚 Références

- [Git Flow Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Semantic Release](https://semantic-release.gitbook.io/)
