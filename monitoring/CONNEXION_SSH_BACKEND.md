# 🔐 Connexion SSH à la VM Backend

## 🚨 Problème

La clé SSH `github-actions-key` n'est pas sur votre PC local. Elle est stockée dans GitHub Secrets (`STAGING_SSH_PRIVATE_KEY`).

---

## ✅ Solution 1 : Utiliser le Workflow GitHub Actions (Recommandé)

Le workflow `check-backend-status.yml` se connecte automatiquement à la VM backend.

### Étape 1 : Exécuter le Workflow

1. **GitHub** > votre repo > **Actions**
2. **Sélectionnez** "Check Backend Status"
3. **Cliquez sur** "Run workflow"
4. **Sélectionnez** la branche `develop` ou `main`
5. **Cliquez sur** "Run workflow"

### Étape 2 : Modifier le Workflow pour Exécuter des Commandes Personnalisées

Si vous voulez exécuter vos propres commandes, modifiez temporairement le workflow :

```yaml
# Dans .github/workflows/check-backend-status.yml
# Remplacez la section "Check Backend Status" par :

- name: Execute Custom Commands
  run: |
    ssh -o StrictHostKeyChecking=no -i ~/.ssh/id_rsa ubuntu@13.49.44.219 << 'EOF'
      # Vos commandes ici
      docker logs hotel-ticket-hub-backend --tail=100
      curl -v http://localhost:8081/actuator/prometheus
      # etc.
    EOF
```

---

## ✅ Solution 2 : Récupérer la Clé SSH via Workflow Temporaire

⚠️ **ATTENTION : Sécurité** - Supprimez ce workflow immédiatement après usage !

### Créer un Workflow Temporaire

Créez `.github/workflows/export-ssh-key-temp.yml` :

```yaml
name: Export SSH Key (TEMPORAIRE - À SUPPRIMER)

on:
  workflow_dispatch:

jobs:
  export-key:
    runs-on: ubuntu-latest
    steps:
      - name: Export SSH Key
        run: |
          echo "${{ secrets.STAGING_SSH_PRIVATE_KEY }}" > key.pem
          chmod 600 key.pem
          echo "=== CLÉ SSH (COPIEZ CETTE SORTIE) ==="
          cat key.pem
          echo "=== FIN DE LA CLÉ ==="
```

### Utiliser le Workflow

1. **GitHub** > **Actions** > **Export SSH Key (TEMPORAIRE)** > **Run workflow**
2. **Regardez** les logs de l'étape "Export SSH Key"
3. **Copiez** la clé SSH complète (de `-----BEGIN` à `-----END`)
4. **Sauvegardez** dans un fichier local : `C:\Users\oumay\.ssh\github-actions-key`
5. **Supprimez** le workflow immédiatement après !

### Utiliser la Clé

```powershell
# Définir les permissions (si nécessaire)
icacls C:\Users\oumay\.ssh\github-actions-key /inheritance:r
icacls C:\Users\oumay\.ssh\github-actions-key /grant:r "%USERNAME%:R"

# Se connecter
ssh -i C:\Users\oumay\.ssh\github-actions-key ubuntu@13.49.44.219
```

---

## ✅ Solution 3 : Créer une Nouvelle Clé SSH

Si vous ne pouvez pas récupérer l'ancienne clé, créez-en une nouvelle :

### Étape 1 : Générer une Nouvelle Clé

```powershell
# Générer une nouvelle clé SSH
ssh-keygen -t rsa -b 4096 -f C:\Users\oumay\.ssh\backend-new-key -C "backend-vm-access"

# Afficher la clé publique
Get-Content C:\Users\oumay\.ssh\backend-new-key.pub
```

### Étape 2 : Ajouter la Clé à la VM Backend

**Option A : Via GitHub Actions Workflow**

Créez un workflow temporaire :

```yaml
name: Add SSH Key to Backend

on:
  workflow_dispatch:

jobs:
  add-key:
    runs-on: ubuntu-latest
    steps:
      - name: Setup SSH
        run: |
          mkdir -p ~/.ssh
          echo "${{ secrets.STAGING_SSH_PRIVATE_KEY }}" > ~/.ssh/id_rsa
          chmod 600 ~/.ssh/id_rsa
          ssh-keyscan -H 13.49.44.219 >> ~/.ssh/known_hosts
      
      - name: Add New Public Key
        run: |
          ssh -o StrictHostKeyChecking=no -i ~/.ssh/id_rsa ubuntu@13.49.44.219 << EOF
            echo "VOTRE_CLÉ_PUBLIQUE_ICI" >> ~/.ssh/authorized_keys
            chmod 600 ~/.ssh/authorized_keys
          EOF
        env:
          NEW_PUBLIC_KEY: ${{ secrets.NEW_BACKEND_SSH_PUBLIC_KEY }}
```

**Option B : Via AWS Systems Manager (SSM)**

Si la VM backend a SSM activé :

1. **AWS Console** > **EC2** > **Instances**
2. **Sélectionnez** `backend-staging`
3. **Actions** > **Connect** > **Session Manager**
4. **Exécutez** :
   ```bash
   echo "VOTRE_CLÉ_PUBLIQUE" >> ~/.ssh/authorized_keys
   chmod 600 ~/.ssh/authorized_keys
   ```

---

## ✅ Solution 4 : Utiliser AWS Systems Manager (SSM)

Si SSM est activé sur la VM backend, vous pouvez vous connecter sans clé SSH :

1. **AWS Console** > **EC2** > **Instances**
2. **Sélectionnez** `backend-staging` (13.49.44.219)
3. **Actions** > **Connect** > **Session Manager**
4. **Cliquez sur** "Connect"

Vous serez connecté directement dans un terminal !

---

## 🎯 Commandes Utiles une Fois Connecté

```bash
# Vérifier les conteneurs
docker ps | grep backend

# Logs du backend
docker logs hotel-ticket-hub-backend --tail=100

# Logs avec erreurs
docker logs hotel-ticket-hub-backend 2>&1 | grep -i "error\|exception\|500"

# Tester localement
curl -v http://localhost:8081/actuator/health
curl -v http://localhost:8081/actuator/prometheus

# Redémarrer le backend
cd /opt/backend
docker-compose restart backend

# Vérifier la configuration
cat /opt/backend/docker-compose.yml | grep -A 5 "ports"
```

---

## 📋 Checklist

- [ ] Méthode choisie (Workflow GitHub Actions / SSM / Nouvelle clé)
- [ ] Connexion réussie à la VM backend
- [ ] Logs du backend analysés
- [ ] Erreur HTTP 500 identifiée
- [ ] Solution appliquée

---

## 🚀 Action Immédiate Recommandée

**Option 1 : AWS Systems Manager (le plus simple)**

1. AWS Console > EC2 > Instances > `backend-staging`
2. Actions > Connect > Session Manager
3. Connect

**Option 2 : Workflow GitHub Actions**

1. GitHub > Actions > "Check Backend Status" > Run workflow
2. Regardez les logs pour voir l'état du backend

---

**Quelle méthode préférez-vous utiliser ?**

