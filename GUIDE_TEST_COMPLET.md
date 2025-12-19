# Guide Complet - Tests, Pipeline CI/CD et SonarQube

## 📋 Table des Matières

1. [Tests Locaux](#1-tests-locaux)
2. [Couverture de Code](#2-couverture-de-code)
3. [Pipeline CI/CD](#3-pipeline-cicd)
4. [SonarQube](#4-sonarqube)
5. [Vérification Finale](#5-vérification-finale)

---

## 1. Tests Locaux

### Exécuter tous les tests

```powershell
cd hotel-ticket-hub-backend
mvn clean test
```

### Exécuter un test spécifique

```powershell
# Un seul test
mvn test -Dtest=AuthServiceTest

# Une méthode spécifique
mvn test -Dtest=AuthServiceTest#testLogin_Success
```

### Voir les résultats détaillés

```powershell
mvn test -X
```

---

## 2. Couverture de Code

### Générer le rapport JaCoCo

```powershell
mvn clean test jacoco:report
```

### Voir le rapport HTML

Ouvrir dans le navigateur :
```
target/site/jacoco/index.html
```

### Vérifier le seuil de couverture

```powershell
mvn test jacoco:check
```

**Résultat attendu** :
- ✅ Si couverture >= 50% : BUILD SUCCESS
- ❌ Si couverture < 50% : BUILD FAILURE avec détails

### Voir uniquement la couverture (sans exécuter les tests)

```powershell
mvn jacoco:report
```

---

## 3. Pipeline CI/CD

### Vérifier la configuration du workflow

Le fichier est dans :
```
hotel-ticket-hub-backend/.github/workflows/ci.yml
```

### Déclencher le pipeline manuellement

#### Option 1 : Push sur GitHub

```powershell
git add .
git commit -m "test: add unit tests for services and controllers"
git push origin develop
```

#### Option 2 : Via l'interface GitHub

1. Allez sur : `https://github.com/oumaymasaoudi/hotel-tickets-backend/actions`
2. Cliquez sur "Run workflow"
3. Sélectionnez la branche `develop`
4. Cliquez sur "Run workflow"

### Vérifier l'état du pipeline

1. Allez sur : `https://github.com/oumaymasaoudi/hotel-tickets-backend/actions`
2. Cliquez sur le dernier workflow
3. Vérifiez les 5 jobs :
   - ✅ Backend - Lint & Code Quality
   - ✅ Backend - Unit Tests
   - ✅ Backend - Code Coverage
   - ✅ Backend - Build
   - ✅ Backend - SonarQube Analysis

### Télécharger les artefacts

Dans GitHub Actions, cliquez sur chaque job pour télécharger :
- **SpotBugs report** : `spotbugs-report`
- **Checkstyle report** : `checkstyle-report`
- **Test reports** : `test-reports`
- **JaCoCo Report** : `jacoco-report`
- **JAR** : `backend-jar`

---

## 4. SonarQube

### Vérifier les secrets GitHub

Allez sur : `https://github.com/oumaymasaoudi/hotel-tickets-backend/settings/secrets/actions`

Vous devez avoir **4 secrets** :
- ✅ `SONAR_TOKEN`
- ✅ `SONAR_HOST_URL`
- ✅ `SONAR_PROJECT_KEY`
- ✅ `SONAR_ORGANIZATION`

### Voir les résultats sur SonarCloud

1. Allez sur : `https://sonarcloud.io/project/overview?id=oumaymasaoudi_hotel-tickets-backend`
2. Vérifiez :
   - **Quality Gate** : "Computed" (pas "Not computed")
   - **Coverage** : Un pourcentage réel (pas 0.0%)
   - **Bugs** : Nombre de bugs détectés
   - **Vulnerabilities** : Nombre de vulnérabilités
   - **Code Smells** : Nombre de code smells

### Tester SonarQube en local (optionnel)

#### Démarrer SonarQube avec Docker

```powershell
cd hotel-ticket-hub-backend
docker compose -f docker-compose.sonarqube.yml up -d
```

Attendre 2-3 minutes, puis :
- Ouvrir : `http://localhost:9000`
- Login : `admin` / Password : `admin` (changé au premier login)

#### Analyser le code localement

```powershell
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=oumaymasaoudi_hotel-tickets-backend \
  -Dsonar.organization=oumaymasaoudi \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=VOTRE_TOKEN_SONAR_LOCAL
```

**Note** : Pour obtenir le token local :
1. Connectez-vous à `http://localhost:9000`
2. Allez dans : **My Account** → **Security** → **Generate Token**

---

## 5. Vérification Finale

### Checklist Complète

#### ✅ Tests Locaux
- [ ] `mvn clean test` → Tous les tests passent
- [ ] `mvn test jacoco:report` → Rapport généré
- [ ] Ouvrir `target/site/jacoco/index.html` → Couverture visible

#### ✅ Pipeline CI/CD
- [ ] Push sur GitHub → Pipeline déclenché
- [ ] Tous les 5 jobs passent (vert)
- [ ] Artefacts téléchargeables

#### ✅ SonarQube
- [ ] Secrets GitHub configurés (4 secrets)
- [ ] Job SonarQube s'exécute dans le pipeline
- [ ] Résultats visibles sur SonarCloud
- [ ] Quality Gate "Computed"
- [ ] Coverage > 0%

---

## 🚀 Commandes Rapides

### Tout tester en une fois

```powershell
cd hotel-ticket-hub-backend

# 1. Tests + Couverture
mvn clean test jacoco:report

# 2. Vérifier le seuil
mvn jacoco:check

# 3. Build complet
mvn clean package

# 4. Push pour déclencher CI/CD
git add .
git commit -m "test: verify all tests and coverage"
git push origin develop
```

---

## 📊 Résultats Attendus

### Tests Locaux
```
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Couverture
```
Coverage check passed (threshold: 50%)
```

### Pipeline CI/CD
```
✅ Backend - Lint & Code Quality
✅ Backend - Unit Tests  
✅ Backend - Code Coverage
✅ Backend - Build
✅ Backend - SonarQube Analysis
```

### SonarCloud
```
Quality Gate: ✅ Passed
Coverage: XX.X%
Bugs: X
Vulnerabilities: X
Code Smells: X
```

---

## 🆘 En Cas de Problème

### Tests échouent
```powershell
# Voir les détails
mvn test -X

# Voir les rapports
cat target/surefire-reports/*.txt
```

### Pipeline échoue
1. Cliquez sur le job qui échoue dans GitHub Actions
2. Regardez les logs pour voir l'erreur
3. Corrigez l'erreur localement
4. Re-push

### SonarQube ne fonctionne pas
1. Vérifiez les secrets GitHub
2. Vérifiez que `sonar-project.properties` est correct
3. Vérifiez les logs du job SonarQube dans GitHub Actions

---

## 📝 Notes Importantes

- **Tests locaux** : Utilisent H2 (base de données en mémoire)
- **Pipeline CI/CD** : S'exécute automatiquement sur chaque push
- **SonarQube** : Nécessite les secrets GitHub pour fonctionner
- **Couverture** : Minimum 50% requis (configuré dans `pom.xml`)

---

## ✅ Prochaines Étapes

1. ✅ Exécuter `mvn clean test` → Vérifier que tous les tests passent
2. ✅ Exécuter `mvn test jacoco:report` → Vérifier la couverture
3. ✅ Push sur GitHub → Vérifier que le pipeline passe
4. ✅ Vérifier SonarCloud → Voir les résultats de l'analyse

**Tout est prêt ! 🎉**

