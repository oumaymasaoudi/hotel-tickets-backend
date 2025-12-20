# Diagnostic : Pourquoi SonarCloud ne se met pas à jour ?

## ✅ Vérifications à faire

### 1. Sur quelle branche êtes-vous ?

Le job Sonar ne s'exécute QUE sur `main` ou `develop`.

```powershell
cd hotel-ticket-hub-backend
git branch
```

**Si vous êtes sur une autre branche** → Le job Sonar est **ignoré** (skipped).

**Solution** : Poussez sur `develop` ou `main` :
```powershell
git checkout develop
git push origin develop
```

---

### 2. Les secrets GitHub sont-ils configurés ?

Allez sur GitHub :
```
https://github.com/oumaymasaoudi/hotel-tickets-backend/settings/secrets/actions
```

Vous devez avoir **4 secrets** :

| Secret | Valeur attendue |
|--------|----------------|
| `SONAR_TOKEN` | `696ce301899fc972f0434c1ba1dad14a696f77a1` |
| `SONAR_HOST_URL` | `https://sonarcloud.io` |
| `SONAR_PROJECT_KEY` | `oumaymasaoudi_hotel-tickets-backend` |
| `SONAR_ORGANIZATION` | `oumaymasaoudi` |

**Si un secret manque** → Le job Sonar échoue silencieusement (car `continue-on-error: true`).

---

### 3. Le job Sonar s'exécute-t-il vraiment ?

Sur GitHub Actions :
```
https://github.com/oumaymasaoudi/hotel-tickets-backend/actions
```

Cliquez sur le dernier workflow → Regardez le job **"Backend - SonarQube Analysis"** :

- ✅ **Vert** = Job exécuté avec succès
- ⚠️ **Jaune/Gris** = Job ignoré (skipped) → Vérifiez la branche
- ❌ **Rouge** = Job échoué → Cliquez pour voir les logs

**Cliquez sur le job Sonar** → Regardez les logs pour voir l'erreur.

---

### 4. Erreurs courantes dans les logs

#### Erreur : "Could not resolve placeholder 'SONAR_PROJECT_KEY'"
→ **Solution** : Ajoutez le secret `SONAR_PROJECT_KEY` sur GitHub

#### Erreur : "Could not resolve placeholder 'SONAR_ORGANIZATION'"
→ **Solution** : Ajoutez le secret `SONAR_ORGANIZATION` sur GitHub

#### Erreur : "Unauthorized" ou "Invalid token"
→ **Solution** : Vérifiez que `SONAR_TOKEN` est correct

#### Erreur : "Project not found"
→ **Solution** : Vérifiez que `SONAR_PROJECT_KEY` correspond au projet SonarCloud

---

## 🔧 Solution rapide

### Étape 1 : Vérifier la branche
```powershell
cd hotel-ticket-hub-backend
git branch
# Si vous n'êtes pas sur develop ou main :
git checkout develop
```

### Étape 2 : Ajouter les secrets manquants sur GitHub
Allez sur : `https://github.com/oumaymasaoudi/hotel-tickets-backend/settings/secrets/actions`

Ajoutez si manquant :
- `SONAR_PROJECT_KEY` = `oumaymasaoudi_hotel-tickets-backend`
- `SONAR_ORGANIZATION` = `oumaymasaoudi`

### Étape 3 : Faire un nouveau push
```powershell
git add .
git commit -m "chore: trigger sonar analysis"
git push origin develop
```

### Étape 4 : Vérifier sur GitHub Actions
Attendez 5-10 minutes, puis :
1. Allez sur `https://github.com/oumaymasaoudi/hotel-tickets-backend/actions`
2. Cliquez sur le dernier workflow
3. Cliquez sur le job **"Backend - SonarQube Analysis"**
4. Vérifiez les logs pour voir si ça fonctionne

### Étape 5 : Vérifier sur SonarCloud
Après 5-10 minutes :
```
https://sonarcloud.io/project/overview?id=oumaymasaoudi_hotel-tickets-backend
```

Vous devriez voir :
- ✅ Quality Gate : "Computed"
- ✅ Coverage : un pourcentage (pas 0.0%)
- ✅ Dernière analyse : il y a quelques minutes

---

## 📞 Si ça ne marche toujours pas

Envoyez-moi :
1. La branche actuelle (`git branch`)
2. Une capture d'écran du job Sonar sur GitHub Actions (avec les logs)
3. La liste des secrets GitHub (masquez les valeurs)

