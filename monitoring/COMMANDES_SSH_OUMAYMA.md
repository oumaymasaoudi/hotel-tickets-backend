# 🔐 Commandes SSH avec Oumayma Key

## ✅ Commandes Modifiées

### Connexion Backend

```powershell
# Ancienne commande (github-actions-key)
# ssh -i C:\Users\oumay\.ssh\github-actions-key ubuntu@13.49.44.219

# Nouvelle commande (oumayma-key.pem)
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219
```

### Connexion Frontend

```powershell
# Si vous voulez aussi utiliser oumayma-key.pem pour le frontend
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@51.21.196.104
```

### Connexion Monitoring

```powershell
# Déjà configuré avec oumayma-key.pem
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224
```

---

## 📋 Commandes Utiles une Fois Connecté au Backend

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

# Voir tous les logs
docker logs hotel-ticket-hub-backend --tail=200
```

---

## 🎯 Étapes pour Activer la Clé

### 1. Récupérer la Clé Publique

```powershell
# Générer la clé publique depuis la clé privée
ssh-keygen -y -f C:\Users\oumay\.ssh\oumayma-key.pem

# OU si le fichier .pub existe déjà
Get-Content C:\Users\oumay\.ssh\oumayma-key.pem.pub
```

**Copiez la clé publique** (elle commence par `ssh-rsa` ou `ssh-ed25519`).

### 2. Ajouter la Clé sur la VM Backend

**Option A : Via GitHub Actions (Recommandé)**

1. **GitHub** > votre repo > **Actions**
2. **Sélectionnez** "Add Oumayma Key to Backend"
3. **Cliquez sur** "Run workflow"
4. **Collez** la clé publique dans le champ `public_key`
5. **Cliquez sur** "Run workflow"

**Option B : Via AWS Systems Manager (SSM)**

1. **AWS Console** > **EC2** > **Instances** > `backend-staging`
2. **Actions** > **Connect** > **Session Manager**
3. **Connect**
4. **Exécutez** :
   ```bash
   echo "VOTRE_CLÉ_PUBLIQUE_ICI" >> ~/.ssh/authorized_keys
   chmod 600 ~/.ssh/authorized_keys
   ```

### 3. Tester la Connexion

```powershell
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219
```

---

## 🚀 Action Immédiate

1. **Récupérer la clé publique :**
   ```powershell
   ssh-keygen -y -f C:\Users\oumay\.ssh\oumayma-key.pem
   ```

2. **Ajouter la clé sur la VM backend** via GitHub Actions workflow "Add Oumayma Key to Backend"

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

