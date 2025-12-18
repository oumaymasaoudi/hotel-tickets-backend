# Tester la Couverture JaCoCo en Local

## 1. Générer le rapport JaCoCo

```powershell
cd hotel-ticket-hub-backend
mvn clean test
```

## 2. Vérifier que le fichier jacoco.xml existe

```powershell
dir target\site\jacoco\jacoco.xml
```

**Résultat attendu** : Le fichier doit exister et avoir une taille > 0.

## 3. Voir le rapport HTML (optionnel)

Ouvrir dans le navigateur :
```
target\site\jacoco\index.html
```

## 4. Tester avec SonarCloud en local (optionnel)

Si vous avez configuré les secrets GitHub, vous pouvez tester l'analyse SonarCloud :

```powershell
mvn clean verify sonar:sonar ^
  -Dsonar.projectKey=oumaymasaoudi_hotel-tickets-backend ^
  -Dsonar.organization=oumaymasaoudi ^
  -Dsonar.host.url=https://sonarcloud.io ^
  -Dsonar.login=VOTRE_TOKEN_SONAR
```

**Note** : Remplacez `VOTRE_TOKEN_SONAR` par votre token SonarCloud.

---

## Résultat attendu

- ✅ `target/site/jacoco/jacoco.xml` existe
- ✅ Le rapport HTML montre la couverture de code
- ✅ SonarCloud pourra lire ce fichier lors du prochain scan CI/CD

