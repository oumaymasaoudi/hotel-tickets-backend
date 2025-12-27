# 🔐 Configuration des Secrets GitHub

## 📋 Secrets à créer

Allez sur : https://github.com/oumaymasaoudi/hotel-ticket-hub/settings/secrets/actions

Créez ces **3 secrets** :

### 1. STAGING_HOST

- **Name** : `STAGING_HOST`
- **Value** : `13.49.44.219`

### 2. STAGING_USER

- **Name** : `STAGING_USER`
- **Value** : `ubuntu`

### 3. STAGING_SSH_PRIVATE_KEY

- **Name** : `STAGING_SSH_PRIVATE_KEY`
- **Value** : Contenu complet de `github-actions-key` (clé privée)

## 📝 Comment copier la clé privée

Dans PowerShell :

```powershell
cd hotel-ticket-hub-backend
Get-Content github-actions-key -Raw | Set-Clipboard
```

Puis collez dans le champ "Secret" sur GitHub.

**⚠️ Important** : 
- Copiez TOUT le contenu (de `-----BEGIN OPENSSH PRIVATE KEY-----` jusqu'à `-----END OPENSSH PRIVATE KEY-----`)
- Ne partagez JAMAIS cette clé privée publiquement

## ✅ Vérification

Une fois les 3 secrets créés, le workflow `backend-staging.yml` pourra se connecter à votre VM et déployer automatiquement.

