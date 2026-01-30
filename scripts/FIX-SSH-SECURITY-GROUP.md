# 🔧 CORRECTION URGENTE : Problème SSH - Security Group AWS

## Problème
```
ssh: connect to host 13.63.15.86 port 22: Connection timed out
```

**Avant ça fonctionnait, maintenant non** → Le Security Group AWS bloque les connexions SSH.

## Solution rapide via AWS Console (5 minutes)

### Étape 1 : Connectez-vous à AWS Console
1. Allez sur **https://console.aws.amazon.com**
2. Connectez-vous avec vos identifiants AWS
3. Sélectionnez la région **eu-north-1** (Stockholm)

### Étape 2 : Trouvez votre instance EC2
1. Dans le menu de gauche, cliquez sur **EC2**
2. Cliquez sur **Instances** (Instances en cours d'exécution)
3. Recherchez l'instance avec l'IP publique **13.63.15.86**
   - Si vous ne la voyez pas, vérifiez que vous êtes dans la bonne région
   - Vérifiez aussi que l'instance est en état **"running"** (en cours d'exécution)

### Étape 3 : Ouvrez le Security Group
1. Cliquez sur l'instance pour la sélectionner
2. En bas de la page, cliquez sur l'onglet **Security** (Sécurité)
3. Vous verrez le **Security Group** (ex: `sg-xxxxx`)
4. **Cliquez sur le nom du Security Group** (lien bleu)

### Étape 4 : Ajoutez la règle SSH
1. Dans la page du Security Group, cliquez sur l'onglet **Inbound rules** (Règles entrantes)
2. Cliquez sur **Edit inbound rules** (Modifier les règles entrantes)
3. Cliquez sur **Add rule** (Ajouter une règle)
4. Remplissez le formulaire :
   - **Type** : Sélectionnez **SSH**
   - **Protocol** : TCP (automatique)
   - **Port range** : 22 (automatique)
   - **Source** : Sélectionnez **Custom** puis tapez `0.0.0.0/0`
   - **Description** : `Allow SSH from anywhere (GitHub Actions)`
5. Cliquez sur **Save rules** (Enregistrer les règles)

### Étape 5 : Vérifiez
Après avoir sauvegardé, vous devriez voir une règle comme :
```
Type: SSH | Protocol: TCP | Port: 22 | Source: 0.0.0.0/0
```

### Étape 6 : Testez la connexion SSH
Attendez 10-30 secondes pour que les changements prennent effet, puis testez :

**Depuis PowerShell (Windows) :**
```powershell
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
```

**Depuis WSL/Linux :**
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
```

## Vérification rapide

Si vous voyez dans les règles entrantes :
- ✅ **Une règle SSH (port 22) avec source `0.0.0.0/0`** → **OK, SSH devrait fonctionner**
- ❌ **Pas de règle SSH** → **C'est le problème ! Ajoutez-la**
- ❌ **Règle SSH mais source limitée** (ex: `172.31.0.0/16`) → **Modifiez la source en `0.0.0.0/0`**

## Alternative : Script PowerShell automatique

Si AWS CLI est configuré sur votre machine Windows :

```powershell
cd hotel-ticket-hub-backend
.\scripts\fix-aws-security-group-auto.ps1
```

Ce script va automatiquement :
1. Trouver l'instance par IP
2. Vérifier le Security Group
3. Ajouter la règle SSH si nécessaire
4. Tester la connexion

## Pourquoi ça a changé ?

Plusieurs raisons possibles :
1. **Security Group modifié manuellement** (par vous ou quelqu'un d'autre)
2. **Règle SSH supprimée** par erreur
3. **Source de la règle SSH modifiée** (limitée à une IP spécifique)
4. **Nouveau Security Group assigné** à l'instance

## Note de sécurité

Autoriser `0.0.0.0/0` permet les connexions SSH depuis n'importe où. C'est acceptable pour le staging, mais pour la production, vous devriez :
- Limiter à votre IP publique
- Ou utiliser les IP ranges de GitHub Actions : https://api.github.com/meta

## Besoin d'aide ?

Si après avoir ajouté la règle SSH, la connexion ne fonctionne toujours pas :
1. Vérifiez que l'instance est en état **"running"**
2. Vérifiez que l'IP publique est toujours **13.63.15.86**
3. Vérifiez que la clé SSH `~/.ssh/oumayma-key.pem` existe et a les bonnes permissions (600)

