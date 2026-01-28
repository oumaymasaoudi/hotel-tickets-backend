# Vérification des secrets GitHub pour le déploiement staging

## Secrets requis dans GitHub

Pour que le déploiement fonctionne, vous devez configurer les secrets suivants dans GitHub :

### Secrets pour le déploiement backend staging

1. **STAGING_HOST**
   - Valeur : `13.63.15.86` (IP publique de backend-staging)
   - Description : Adresse IP ou hostname de la VM de staging

2. **STAGING_USER**
   - Valeur : `ubuntu` (utilisateur par défaut sur AWS EC2)
   - Description : Nom d'utilisateur SSH pour se connecter à la VM

3. **STAGING_SSH_PRIVATE_KEY**
   - Valeur : Contenu de la clé SSH privée (`oumayma-key.pem`)
   - Description : Clé SSH privée pour l'authentification
   - ⚠️ Important : Copiez TOUT le contenu du fichier `.pem`, y compris les lignes `-----BEGIN RSA PRIVATE KEY-----` et `-----END RSA PRIVATE KEY-----`

4. **GHCR_TOKEN**
   - Valeur : Token GitHub avec permissions `read:packages` et `write:packages`
   - Description : Token pour accéder au GitHub Container Registry
   - Création : GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic) → Generate new token

### Secrets pour le déploiement monitoring (optionnel)

5. **MONITORING_HOST**
   - Valeur : IP de la VM de monitoring (si différente de staging)
   - Description : Adresse IP de la VM de monitoring

6. **MONITORING_USER**
   - Valeur : `ubuntu`
   - Description : Nom d'utilisateur SSH pour la VM de monitoring

7. **MONITORING_SSH_PRIVATE_KEY**
   - Valeur : Contenu de la clé SSH privée pour la VM de monitoring
   - Description : Clé SSH privée pour l'authentification

8. **SONAR_TOKEN**
   - Valeur : Token SonarCloud
   - Description : Token pour l'analyse de code SonarCloud

## Comment configurer les secrets dans GitHub

1. Allez sur votre repository GitHub
2. Cliquez sur **Settings** → **Secrets and variables** → **Actions**
3. Cliquez sur **New repository secret**
4. Entrez le nom du secret (ex: `STAGING_HOST`)
5. Entrez la valeur du secret
6. Cliquez sur **Add secret**

## Vérification des secrets

Pour vérifier que les secrets sont bien configurés :

1. Allez sur **Actions** dans votre repository
2. Lancez manuellement le workflow ou faites un push sur `develop`
3. Vérifiez les logs du job `deploy-staging`
4. Si vous voyez des erreurs de connexion SSH, vérifiez les secrets `STAGING_HOST`, `STAGING_USER`, `STAGING_SSH_PRIVATE_KEY`

## Test de connexion SSH local

Pour tester la connexion SSH depuis votre machine locale :

```bash
# Télécharger la clé depuis AWS EC2 si nécessaire
# Puis tester la connexion :
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86

# Si ça fonctionne, les secrets GitHub devraient aussi fonctionner
```

## Notes importantes

- ⚠️ La clé SSH doit être au format PEM (débutant par `-----BEGIN RSA PRIVATE KEY-----` ou `-----BEGIN OPENSSH PRIVATE KEY-----`)
- ⚠️ Assurez-vous que le Security Group AWS autorise les connexions SSH depuis GitHub Actions (IPs GitHub)
- ⚠️ Le Security Group doit aussi autoriser le port 8081 pour l'accès à l'API

