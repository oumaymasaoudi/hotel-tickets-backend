# 🔑 Ajouter la Clé SSH sur la VM Backend

## 🎯 Objectif

Configurer votre clé SSH pour pouvoir vous connecter à la VM Backend (`13.63.15.86`) avec :
```powershell
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.63.15.86
```

---

## ✅ Méthode 1 : Via AWS EC2 Instance Connect (Temporaire)

### Étape 1 : Se Connecter via EC2 Instance Connect

1. **AWS Console** → **EC2** → **Instances**
2. Sélectionnez `backend-staging` (IP : `13.63.15.86`)
3. **Connect** → **EC2 Instance Connect**
4. Cliquez sur **Connect**

### Étape 2 : Obtenir Votre Clé Publique SSH

**Sur votre machine Windows :**

```powershell
# Si vous avez une clé .pub
cat C:\Users\oumay\.ssh\oumayma-key.pem.pub

# OU si vous avez id_rsa.pub
cat C:\Users\oumay\.ssh\id_rsa.pub

# OU générer depuis la clé privée (si c'est une clé OpenSSH)
ssh-keygen -y -f C:\Users\oumay\.ssh\oumayma-key.pem
```

**Copiez la sortie** (commence par `ssh-rsa` ou `ssh-ed25519`)

### Étape 3 : Ajouter la Clé sur la VM Backend

**Dans le terminal EC2 Instance Connect de la VM Backend :**

```bash
# Créer le répertoire .ssh si nécessaire
mkdir -p ~/.ssh
chmod 700 ~/.ssh

# Ajouter votre clé publique
echo "VOTRE_CLE_PUBLIQUE_SSH_ICI" >> ~/.ssh/authorized_keys

# Vérifier les permissions
chmod 600 ~/.ssh/authorized_keys

# Vérifier que la clé a été ajoutée
tail -3 ~/.ssh/authorized_keys
```

**Remplacez `VOTRE_CLE_PUBLIQUE_SSH_ICI`** par la clé que vous avez copiée à l'étape 2.

---

## ✅ Méthode 2 : Via GitHub Actions Workflow

### Étape 1 : Obtenir Votre Clé Publique

```powershell
# Sur Windows
cat C:\Users\oumay\.ssh\oumayma-key.pem.pub
# OU
ssh-keygen -y -f C:\Users\oumay\.ssh\oumayma-key.pem
```

**Copiez la clé publique complète** (une seule ligne)

### Étape 2 : Vérifier les Secrets GitHub

1. **GitHub** → Repo `hotel-ticket-hub-backend`
2. **Settings** → **Secrets and variables** → **Actions**
3. Vérifiez que :
   - `STAGING_HOST` = `13.63.15.86`
   - `STAGING_USER` = `ubuntu`
   - `STAGING_SSH_PRIVATE_KEY` = Votre clé privée SSH (pour que GitHub Actions puisse se connecter)

### Étape 3 : Lancer le Workflow

1. **GitHub** → Repo `hotel-ticket-hub-backend`
2. **Actions** → **Add Oumayma Key to Backend**
3. **Run workflow** → Sélectionnez la branche `develop`
4. Dans le champ **public_key**, collez votre clé publique SSH
5. Cliquez sur **Run workflow**

Le workflow va :
- Se connecter à la VM Backend avec la clé privée GitHub
- Ajouter votre clé publique dans `~/.ssh/authorized_keys`

---

## ✅ Méthode 3 : Via AWS Systems Manager (SSM)

Si SSM est activé sur votre instance :

```bash
# Sur votre machine Windows (avec AWS CLI)
aws ssm start-session --target i-0840a325da5cbd50c --region eu-north-1

# Puis dans la session SSM :
echo "VOTRE_CLE_PUBLIQUE_SSH" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

---

## 🧪 Tester la Connexion SSH

Une fois la clé ajoutée :

```powershell
# Sur votre machine Windows
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.63.15.86 "echo OK"
```

**Résultat attendu** : `OK`

Si ça fonctionne, vous pouvez vous connecter :

```powershell
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.63.15.86
```

---

## 📋 Checklist

- [ ] **Obtenir votre clé publique SSH** (`oumayma-key.pem.pub` ou générer depuis la clé privée)
- [ ] **Se connecter à la VM Backend** via EC2 Instance Connect
- [ ] **Ajouter la clé** dans `~/.ssh/authorized_keys`
- [ ] **Vérifier les permissions** : `chmod 600 ~/.ssh/authorized_keys`
- [ ] **Tester** : `ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.63.15.86`

---

## 🚀 Action Immédiate Recommandée

**Méthode 1 (la plus simple)** :

1. **AWS Console** → **EC2** → **Instances** → `backend-staging`
2. **Connect** → **EC2 Instance Connect**
3. **Sur Windows**, exécutez :
   ```powershell
   ssh-keygen -y -f C:\Users\oumay\.ssh\oumayma-key.pem
   ```
4. **Copiez la clé publique** affichée
5. **Dans EC2 Instance Connect**, exécutez :
   ```bash
   echo "COLLER_LA_CLE_ICI" >> ~/.ssh/authorized_keys
   chmod 600 ~/.ssh/authorized_keys
   ```
6. **Testez** :
   ```powershell
   ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.63.15.86
   ```

---

**Une fois la clé ajoutée, vous pourrez vous connecter avec SSH !** 🔑

