# 🔐 Copier la clé SSH sur la VM data-staging

## ❌ Problème

Erreur : `Permission denied (publickey)` lors de la connexion à `13.61.27.43`

## ✅ Solution : Copier la clé publique avec votre clé AWS

### Étape 1 : Trouver votre clé AWS (.pem)

Votre clé AWS se trouve probablement dans :
- `C:\Users\oumay\Downloads\*.pem`
- Ou un autre emplacement que vous connaissez

**Chercher la clé :**
```powershell
# Chercher dans Downloads
Get-ChildItem $HOME\Downloads\*.pem

# Ou chercher partout
Get-ChildItem C:\ -Recurse -Filter "*.pem" -ErrorAction SilentlyContinue | Select-Object FullName -First 5
```

**Notez le chemin complet** de votre clé AWS (ex: `C:\Users\oumay\Downloads\my-key.pem`)

---

### Étape 2 : Vérifier que la clé GitHub Actions existe

```powershell
cd C:\Users\oumay\projet\hotel-ticket-hub-backend

# Vérifier que les clés existent
ls github-actions-key*
```

Vous devriez voir :
- `github-actions-key` (clé privée)
- `github-actions-key.pub` (clé publique)

**Si elles n'existent pas**, générez-les :
```powershell
ssh-keygen -t rsa -b 4096 -C "github-actions-backend" -f github-actions-key -N '""'
```

---

### Étape 3 : Copier la clé publique sur la VM data-staging

#### Option A : Utiliser le script PowerShell (Recommandé)

```powershell
# Utiliser le script existant
.\copy-ssh-key.ps1 -HostIP "13.61.27.43" -AWSKey "CHEMIN_VERS_VOTRE_CLE_AWS.pem" -User "ubuntu"
```

**Exemple si votre clé est dans Downloads :**
```powershell
.\copy-ssh-key.ps1 -HostIP "13.61.27.43" -AWSKey "$HOME\Downloads\my-key.pem" -User "ubuntu"
```

#### Option B : Commande manuelle

```powershell
# Lire la clé publique
$publicKey = Get-Content github-actions-key.pub -Raw
$publicKey = $publicKey.Trim()

# Copier sur la VM data-staging (remplacez le chemin de votre clé AWS)
ssh -i "C:\Users\oumay\Downloads\votre-cle-aws.pem" ubuntu@13.61.27.43 "mkdir -p ~/.ssh && echo '$publicKey' >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys && chmod 700 ~/.ssh && echo 'Cle ajoutee avec succes!'"
```

**⚠️ Important** : 
- Remplacez `C:\Users\oumay\Downloads\votre-cle-aws.pem` par le vrai chemin de votre clé AWS
- Si l'utilisateur n'est pas `ubuntu`, essayez `ec2-user`

---

### Étape 4 : Tester la connexion

```powershell
# Tester avec la nouvelle clé
ssh -i github-actions-key ubuntu@13.61.27.43 "echo 'Connexion OK' && hostname"
```

**Résultat attendu :**
```
Connexion OK
ip-xxx-xxx-xxx-xxx
```

Si ça fonctionne **sans demander de mot de passe**, c'est bon ! ✅

---

## 🔧 Dépannage

### Erreur : "Permission denied" avec la clé AWS

**Vérifiez :**
1. Le chemin de la clé AWS est correct
2. L'utilisateur est correct (`ubuntu` ou `ec2-user`)
3. Les Security Groups AWS autorisent SSH (port 22) depuis votre IP

**Tester d'abord avec la clé AWS :**
```powershell
ssh -i "C:\Users\oumay\Downloads\votre-cle-aws.pem" ubuntu@13.61.27.43 "echo 'Test connexion AWS'"
```

Si ça ne fonctionne pas, vérifiez :
- L'IP est correcte : `13.61.27.43`
- L'utilisateur est correct : `ubuntu` ou `ec2-user`
- Les Security Groups AWS

### Erreur : "Connection refused" ou "Connection timed out"

**Vérifiez :**
1. L'instance EC2 est démarrée
2. Les Security Groups AWS autorisent SSH (port 22)
3. L'IP est correcte : `13.61.27.43`

### Erreur : "Host key verification failed"

```powershell
# Supprimer l'ancienne entrée
ssh-keygen -R 13.61.27.43

# Réessayer la connexion
ssh -i github-actions-key ubuntu@13.61.27.43
```

---

## ✅ Une fois la connexion fonctionnelle

Vous pouvez maintenant suivre le guide `SETUP_DATABASE_VM_STAGING.md` pour installer PostgreSQL.

**Test rapide :**
```powershell
ssh -i github-actions-key ubuntu@13.61.27.43 "sudo apt update"
```

Si ça fonctionne, vous pouvez continuer avec l'installation de PostgreSQL ! 🎉

