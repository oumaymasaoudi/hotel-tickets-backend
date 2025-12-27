# ⚠️ Important : Emplacement du Workflow

## 📍 Structure du Projet

Votre projet est un **monorepo** avec :
- `hotel-ticket-hub/` = Frontend
- `hotel-ticket-hub-backend/` = Backend

## 🔧 GitHub Actions et les Workflows

**GitHub Actions ne détecte automatiquement que les workflows dans `.github/workflows/` à la racine du repository.**

### Option 1 : Copier le workflow à la racine (Recommandé)

Si votre repo GitHub est un monorepo, copiez le workflow à la racine :

```bash
# Depuis la racine du projet
cp hotel-ticket-hub-backend/.github/workflows/backend-staging.yml .github/workflows/backend-staging.yml
```

### Option 2 : Le workflow est déjà à la racine

Si vous avez déjà un dossier `.github/workflows/` à la racine, le workflow y est peut-être déjà.

### Option 3 : Repo séparé pour le backend

Si `hotel-ticket-hub-backend` est un **repo GitHub séparé**, alors le workflow dans `hotel-ticket-hub-backend/.github/workflows/` fonctionnera correctement.

## ✅ Vérification

Pour vérifier où GitHub Actions cherche les workflows :

1. Allez sur : https://github.com/oumaymasaoudi/hotel-ticket-hub/actions
2. Si vous voyez le workflow "Backend - Staging Deploy", c'est bon ✅
3. Sinon, copiez-le à la racine comme indiqué ci-dessus

## 📝 Workflow Corrigé

Le workflow a été corrigé pour fonctionner dans un monorepo :
- `cache-dependency-path: hotel-ticket-hub-backend/pom.xml`
- `working-directory: ./hotel-ticket-hub-backend`
- `path: hotel-ticket-hub-backend/target/*.jar`

