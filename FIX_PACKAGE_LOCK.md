# 🔧 Fix Package Lock - Instructions

## Problème

Le workflow `release` échoue car `package-lock.json` n'est pas synchronisé avec `package.json`.

## ✅ Solution Recommandée : Générer package-lock.json

### Étape 1 : Générer le package-lock.json

```powershell
cd hotel-ticket-hub-backend
npm install
```

Cela va créer le fichier `package-lock.json`.

### Étape 2 : Commiter le package-lock.json

```powershell
git add package-lock.json
git commit -m "chore: add package-lock.json for semantic-release dependencies"
git push origin main
```

### Étape 3 : Revenir à npm ci dans le workflow

Une fois le `package-lock.json` committé, vous pouvez remettre `npm ci` dans le workflow pour une meilleure reproductibilité.

---

## ⚡ Solution Temporaire (Déjà Appliquée)

J'ai changé `npm ci` en `npm install` dans le workflow. Cela fonctionne mais est moins strict.

Pour revenir à `npm ci` après avoir généré le `package-lock.json` :

1. Générez le `package-lock.json` (voir ci-dessus)
2. Commitez-le
3. Remettez `npm ci` dans le workflow

---

## 🎯 Action Immédiate

Le workflow devrait maintenant fonctionner avec `npm install`. 

Si vous voulez la solution recommandée, exécutez :
```powershell
cd hotel-ticket-hub-backend
npm install
git add package-lock.json
git commit -m "chore: add package-lock.json"
git push origin main
```

