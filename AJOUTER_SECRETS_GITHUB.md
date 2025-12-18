# Ajouter les Secrets GitHub pour SonarCloud

## Secrets à ajouter

Allez sur votre repo GitHub : `https://github.com/oumaymasaoudi/hotel-tickets-backend`

**Settings** → **Secrets and variables** → **Actions** → **New repository secret**

### Secret 1 : SONAR_PROJECT_KEY

- **Name** : `SONAR_PROJECT_KEY`
- **Secret** : `oumaymasaoudi_hotel-tickets-backend`
- Cliquer sur **"Add secret"**

### Secret 2 : SONAR_ORGANIZATION

- **Name** : `SONAR_ORGANIZATION`
- **Secret** : `oumaymasaoudi`
- Cliquer sur **"Add secret"**

### Secret 3 : SONAR_TOKEN (déjà ajouté ?)

- **Name** : `SONAR_TOKEN`
- **Secret** : `696ce301899fc972f0434c1ba1dad14a696f77a1` (votre token)
- Vérifier s'il existe déjà, sinon l'ajouter

### Secret 4 : SONAR_HOST_URL (déjà ajouté ?)

- **Name** : `SONAR_HOST_URL`
- **Secret** : `https://sonarcloud.io`
- Vérifier s'il existe déjà, sinon l'ajouter

---

## Résultat attendu

Vous devriez avoir **4 secrets** dans la liste :

| Name | Updated |
|------|---------|
| `SONAR_TOKEN` | ... |
| `SONAR_HOST_URL` | ... |
| `SONAR_PROJECT_KEY` | ... |
| `SONAR_ORGANIZATION` | ... |

---

## Après avoir ajouté les secrets

1. Faire un commit et push
2. Le workflow GitHub Actions utilisera ces secrets
3. SonarCloud affichera la couverture de code
4. Le Quality Gate sera calculé

