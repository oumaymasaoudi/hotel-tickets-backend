# 🧪 Tester Avant de Lancer le Pipeline

## ✅ Checklist Avant de Pousser

### 1. Tests Locaux Backend

```powershell
cd hotel-ticket-hub-backend

# Tests unitaires
mvn test

# Lint (Checkstyle)
mvn checkstyle:check

# Build
mvn clean package -DskipTests

# Vérifier que le JAR est créé
ls target/*.jar
```

### 2. Tests Locaux Frontend

```powershell
cd hotel-ticket-hub

# Lint
npm run lint

# Tests
npm run test

# Build
npm run build

# Vérifier que le build est créé
ls dist/
```

### 3. Test de Connexion SSH (Backend)

```powershell
# Tester la connexion SSH vers la VM Backend
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219 "echo 'Connexion OK'"

# OU avec la clé configurée dans GitHub Secrets
# (vous devez d'abord créer un fichier temporaire avec la clé)
```

### 4. Test de Connexion SSH (Monitoring)

```powershell
# Tester la connexion SSH vers la VM Monitoring
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224 "echo 'Connexion OK'"
```

### 5. Vérifier les Secrets GitHub

Vérifiez que tous les secrets sont configurés dans GitHub :

**Backend (`hotel-ticket-hub-backend`) :**
- ✅ `STAGING_HOST`
- ✅ `STAGING_USER`
- ✅ `STAGING_SSH_PRIVATE_KEY`
- ✅ `GHCR_TOKEN`
- ✅ `MONITORING_HOST`
- ✅ `MONITORING_USER`
- ✅ `MONITORING_SSH_PRIVATE_KEY`

**Frontend (`hotel-ticket-hub`) :**
- ✅ `FRONTEND_STAGING_HOST`
- ✅ `FRONTEND_STAGING_USER`
- ✅ `FRONTEND_STAGING_SSH_PRIVATE_KEY`
- ✅ `GHCR_TOKEN`
- ✅ `VITE_API_BASE_URL`

### 6. Vérifier les Fichiers de Configuration

```powershell
# Backend
cd hotel-ticket-hub-backend
# Vérifier que docker-compose.yml existe
Test-Path docker-compose.yml

# Frontend
cd hotel-ticket-hub
# Vérifier que docker-compose.yml existe
Test-Path docker-compose.yml
```

---

## 🚀 Script de Test Complet

Créez un fichier `test-before-push.ps1` :

```powershell
# test-before-push.ps1
Write-Host "🧪 Tests avant push..." -ForegroundColor Cyan

# Backend
Write-Host "`n📦 Backend Tests..." -ForegroundColor Yellow
cd hotel-ticket-hub-backend

Write-Host "  - Tests unitaires..."
mvn test -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ Tests échoués!" -ForegroundColor Red
    exit 1
}

Write-Host "  - Lint..."
mvn checkstyle:check -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ⚠️  Warnings de lint (non bloquant)" -ForegroundColor Yellow
}

Write-Host "  - Build..."
mvn clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ Build échoué!" -ForegroundColor Red
    exit 1
}

# Frontend
Write-Host "`n📦 Frontend Tests..." -ForegroundColor Yellow
cd ..\hotel-ticket-hub

Write-Host "  - Lint..."
npm run lint
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ⚠️  Warnings de lint (non bloquant)" -ForegroundColor Yellow
}

Write-Host "  - Tests..."
npm run test -- --watchAll=false
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ Tests échoués!" -ForegroundColor Red
    exit 1
}

Write-Host "  - Build..."
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ Build échoué!" -ForegroundColor Red
    exit 1
}

Write-Host "`n✅ Tous les tests sont passés!" -ForegroundColor Green
Write-Host "Vous pouvez maintenant pousser sur GitHub." -ForegroundColor Green
```

**Utilisation :**
```powershell
.\test-before-push.ps1
```

---

## 🔍 Vérifications Rapides

### Vérifier les changements Git

```powershell
# Voir les fichiers modifiés
git status

# Voir les différences
git diff

# Vérifier qu'on est sur la bonne branche
git branch
```

### Vérifier les Workflows

```powershell
# Vérifier la syntaxe YAML des workflows
# (nécessite yamllint ou un éditeur avec validation YAML)

# Backend
cd hotel-ticket-hub-backend\.github\workflows
# Ouvrir ci.yml dans VS Code pour voir les erreurs

# Frontend
cd hotel-ticket-hub\.github\workflows
# Ouvrir frontend-ci.yml dans VS Code pour voir les erreurs
```

---

## ⚠️ Erreurs Communes à Vérifier

### 1. Secrets manquants
- Vérifiez que tous les secrets sont configurés dans GitHub Settings

### 2. Syntaxe YAML incorrecte
- Utilisez un validateur YAML ou VS Code avec extension YAML

### 3. Fichiers manquants
- `docker-compose.yml` doit exister à la racine
- `package-lock.json` doit être à jour (frontend)

### 4. Connexion SSH
- Testez manuellement la connexion SSH avant de pousser
- Vérifiez que le Security Group AWS autorise les connexions

---

## 🎯 Workflow Recommandé

1. **Faire les changements**
2. **Tester localement** :
   ```powershell
   .\test-before-push.ps1
   ```
3. **Vérifier Git** :
   ```powershell
   git status
   git diff
   ```
4. **Commiter** :
   ```powershell
   git add .
   git commit -m "feat: description"
   ```
5. **Pousser** :
   ```powershell
   git push origin develop
   ```
6. **Vérifier GitHub Actions** :
   - Allez sur GitHub → Actions
   - Vérifiez que le workflow démarre
   - Surveillez les logs en cas d'erreur

---

## 📝 Commandes Rapides

```powershell
# Test complet rapide
cd hotel-ticket-hub-backend && mvn test && cd ..\hotel-ticket-hub && npm run test

# Vérifier les secrets (manuellement dans GitHub)
# GitHub → Settings → Secrets and variables → Actions

# Test SSH Backend
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219 "echo OK"

# Test SSH Monitoring
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224 "echo OK"
```

---

**💡 Astuce :** Créez un alias PowerShell pour les commandes fréquentes :

```powershell
# Ajouter dans votre profil PowerShell ($PROFILE)
function Test-Backend {
    cd hotel-ticket-hub-backend
    mvn test
}

function Test-Frontend {
    cd hotel-ticket-hub
    npm run test
}

function Test-All {
    Test-Backend
    Test-Frontend
}
```

Ensuite utilisez simplement :
```powershell
Test-All
```

