# 🔐 Comment Obtenir la Clé Privée SSH pour GitHub Secrets

## 📍 Localisation de la Clé

La clé privée pour la VM Monitoring (`13.62.53.224`) se trouve dans :
```
C:\Users\oumay\.ssh\oumayma-key.pem
```

---

## ✅ Méthode 1 : Via PowerShell (Recommandé)

### Étape 1 : Ouvrir PowerShell

Ouvrez PowerShell dans votre machine Windows.

### Étape 2 : Afficher le Contenu de la Clé

```powershell
# Afficher le contenu complet de la clé privée
Get-Content C:\Users\oumay\.ssh\oumayma-key.pem
```

**Important :** Copiez **TOUT** le contenu, y compris :
- La ligne `-----BEGIN RSA PRIVATE KEY-----` (ou `-----BEGIN OPENSSH PRIVATE KEY-----`)
- Toutes les lignes au milieu
- La ligne `-----END RSA PRIVATE KEY-----` (ou `-----END OPENSSH PRIVATE KEY-----`)

### Étape 3 : Copier dans GitHub Secrets

1. Allez sur GitHub → votre repo `hotel-ticket-hub-backend`
2. **Settings** → **Secrets and variables** → **Actions**
3. Cliquez sur **"New repository secret"**
4. Nom : `MONITORING_SSH_PRIVATE_KEY`
5. Valeur : Collez **TOUT** le contenu que vous venez de copier
6. Cliquez sur **"Add secret"**

---

## ✅ Méthode 2 : Via l'Explorateur de Fichiers

### Étape 1 : Ouvrir le Fichier

1. Ouvrez l'Explorateur de Fichiers Windows
2. Allez dans : `C:\Users\oumay\.ssh\`
3. Double-cliquez sur `oumayma-key.pem`

### Étape 2 : Choisir un Éditeur

Si Windows demande avec quel programme ouvrir :
- Choisissez **Notepad** (Bloc-notes)
- OU **VS Code** si installé
- OU **Notepad++** si installé

### Étape 3 : Copier le Contenu

1. **Sélectionnez tout** : `Ctrl + A`
2. **Copiez** : `Ctrl + C`
3. **Collez dans GitHub Secrets** (voir Méthode 1, Étape 3)

---

## ✅ Méthode 3 : Via VS Code (Si Installé)

### Étape 1 : Ouvrir dans VS Code

```powershell
# Ouvrir le fichier dans VS Code
code C:\Users\oumay\.ssh\oumayma-key.pem
```

### Étape 2 : Copier le Contenu

1. **Sélectionnez tout** : `Ctrl + A`
2. **Copiez** : `Ctrl + C`
3. **Collez dans GitHub Secrets**

---

## ⚠️ Important : Format de la Clé

La clé privée doit ressembler à ceci :

```
-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEA...
(plusieurs lignes de caractères)
...
-----END RSA PRIVATE KEY-----
```

OU

```
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAA...
(plusieurs lignes de caractères)
...
-----END OPENSSH PRIVATE KEY-----
```

**⚠️ Ne copiez PAS seulement une partie ! Copiez TOUT le contenu du fichier.**

---

## ✅ Vérification

### Vérifier que le Fichier Existe

```powershell
# Vérifier que le fichier existe
Test-Path C:\Users\oumay\.ssh\oumayma-key.pem

# Si ça retourne "True", le fichier existe ✅
# Si ça retourne "False", le fichier n'existe pas ❌
```

### Vérifier la Taille du Fichier

```powershell
# Voir la taille du fichier
(Get-Item C:\Users\oumay\.ssh\oumayma-key.pem).Length

# Une clé privée RSA fait généralement entre 1500 et 2000 octets
# Si c'est beaucoup plus petit, il y a un problème
```

---

## 🚨 Si le Fichier n'Existe Pas

### Option 1 : Vérifier d'Autres Emplacements

```powershell
# Chercher le fichier dans tout le système
Get-ChildItem -Path C:\Users\oumay -Filter "*.pem" -Recurse -ErrorAction SilentlyContinue

# OU chercher "oumayma" dans le nom
Get-ChildItem -Path C:\Users\oumay -Filter "*oumayma*" -Recurse -ErrorAction SilentlyContinue
```

### Option 2 : Télécharger depuis AWS

Si la clé a été créée via AWS EC2 :

1. **AWS Console** → **EC2** → **Key Pairs**
2. Trouvez la clé `oumayma-key` (ou nom similaire)
3. **Téléchargez** la clé privée (si disponible)

**⚠️ Note :** AWS ne stocke que la clé publique, pas la clé privée. Si vous avez perdu la clé privée, vous devrez en créer une nouvelle.

### Option 3 : Utiliser une Autre Clé

Si vous avez une autre clé qui fonctionne pour vous connecter à la VM Monitoring :

```powershell
# Tester la connexion avec une autre clé
ssh -i C:\Users\oumay\.ssh\autre-cle.pem ubuntu@13.62.53.224
```

Si ça fonctionne, utilisez cette clé pour `MONITORING_SSH_PRIVATE_KEY`.

---

## 📋 Checklist

- [ ] Fichier `oumayma-key.pem` trouvé dans `C:\Users\oumay\.ssh\`
- [ ] Contenu complet de la clé copié (de `-----BEGIN` jusqu'à `-----END`)
- [ ] Secret `MONITORING_SSH_PRIVATE_KEY` ajouté dans GitHub
- [ ] Secret `MONITORING_HOST` ajouté (valeur : `13.62.53.224`)
- [ ] Secret `MONITORING_USER` ajouté (valeur : `ubuntu`)

---

## 🎯 Action Immédiate

1. **Ouvrez PowerShell**
2. **Exécutez** :
   ```powershell
   Get-Content C:\Users\oumay\.ssh\oumayma-key.pem
   ```
3. **Copiez TOUT le contenu affiché**
4. **Allez sur GitHub** → votre repo → **Settings** → **Secrets** → **Actions**
5. **Ajoutez le secret** `MONITORING_SSH_PRIVATE_KEY` avec le contenu copié

**C'est tout !** 🚀

