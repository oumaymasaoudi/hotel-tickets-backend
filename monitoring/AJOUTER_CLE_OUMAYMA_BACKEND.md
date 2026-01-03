# 🔐 Ajouter la Clé Oumayma sur la VM Backend

## 🎯 Objectif

Utiliser `oumayma-key.pem` (qui existe déjà) pour se connecter à la VM backend au lieu de `github-actions-key`.

---

## ✅ Étape 1 : Récupérer la Clé Publique

```powershell
# Afficher la clé publique
Get-Content C:\Users\oumay\.ssh\oumayma-key.pem.pub

# Si le fichier .pub n'existe pas, le générer depuis la clé privée
ssh-keygen -y -f C:\Users\oumay\.ssh\oumayma-key.pem > C:\Users\oumay\.ssh\oumayma-key.pem.pub

# Afficher la clé publique
Get-Content C:\Users\oumay\.ssh\oumayma-key.pem.pub
```

**Copiez la clé publique** (elle commence par `ssh-rsa` ou `ssh-ed25519`).

---

## ✅ Étape 2 : Ajouter la Clé sur la VM Backend

### Option A : Via GitHub Actions Workflow (Recommandé)

Créez un workflow temporaire `.github/workflows/add-oumayma-key.yml` :

```yaml
name: Add Oumayma Key to Backend

on:
  workflow_dispatch:
    inputs:
      public_key:
        description: 'Clé publique SSH (oumayma-key.pem.pub)'
        required: true

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
      
      - name: Add Oumayma Public Key
        run: |
          ssh -o StrictHostKeyChecking=no -i ~/.ssh/id_rsa ubuntu@13.49.44.219 << EOF
            echo "${{ github.event.inputs.public_key }}" >> ~/.ssh/authorized_keys
            chmod 600 ~/.ssh/authorized_keys
            echo "✅ Clé ajoutée avec succès"
          EOF
```

**Utilisation :**
1. GitHub > Actions > "Add Oumayma Key to Backend" > Run workflow
2. Collez la clé publique dans le champ `public_key`
3. Run workflow

### Option B : Via AWS Systems Manager (SSM)

Si SSM est activé :

1. **AWS Console** > **EC2** > **Instances** > `backend-staging`
2. **Actions** > **Connect** > **Session Manager**
3. **Connect**
4. **Exécutez** :
   ```bash
   echo "VOTRE_CLÉ_PUBLIQUE_ICI" >> ~/.ssh/authorized_keys
   chmod 600 ~/.ssh/authorized_keys
   ```

---

## ✅ Étape 3 : Tester la Connexion

Une fois la clé ajoutée, testez :

```powershell
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219
```

---

## 📋 Commandes Modifiées

### Connexion Backend

```powershell
# Ancienne commande (github-actions-key)
# ssh -i C:\Users\oumay\.ssh\github-actions-key ubuntu@13.49.44.219

# Nouvelle commande (oumayma-key.pem)
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219
```

### Commandes Utiles une Fois Connecté

```bash
# Vérifier les conteneurs
docker ps | grep backend

# Logs avec erreurs HTTP 500
docker logs hotel-ticket-hub-backend --tail=100 | grep -i "error\|exception\|500\|actuator\|prometheus"

# Tester localement
curl -v http://localhost:8081/actuator/health
curl -v http://localhost:8081/actuator/prometheus

# Redémarrer le backend
cd /opt/backend
docker-compose restart backend
```

---

## 🎯 Action Immédiate

1. **Récupérer la clé publique :**
   ```powershell
   ssh-keygen -y -f C:\Users\oumay\.ssh\oumayma-key.pem
   ```

2. **Ajouter la clé sur la VM backend** (via GitHub Actions ou SSM)

3. **Tester la connexion :**
   ```powershell
   ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219
   ```

4. **Diagnostiquer l'erreur HTTP 500 :**
   ```bash
   docker logs hotel-ticket-hub-backend --tail=100 | grep -i "error\|exception\|500"
   ```

---

**Commencez par récupérer la clé publique, puis ajoutez-la sur la VM backend !**

