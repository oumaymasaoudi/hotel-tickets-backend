# 🚀 GUIDE RAPIDE : Corriger SSH en 3 minutes

## Le problème
```
ssh: connect to host 13.63.15.86 port 22: Connection timed out
```

## Solution : AWS Console (pas besoin d'AWS CLI)

### Étape 1 : Ouvrir AWS Console
👉 **https://console.aws.amazon.com/ec2**

### Étape 2 : Trouver l'instance
1. Dans le menu gauche : **Instances** (Instances en cours d'exécution)
2. Recherchez l'instance avec l'IP **13.63.15.86**
3. **Sélectionnez-la** (cochez la case)

### Étape 3 : Ouvrir le Security Group
1. En bas de la page, onglet **Security** (Sécurité)
2. Vous verrez : **Security groups** → un nom comme `sg-xxxxx`
3. **Cliquez sur le nom du Security Group** (lien bleu)

### Étape 4 : Ajouter la règle SSH
1. Onglet **Inbound rules** (Règles entrantes)
2. Bouton **Edit inbound rules** (Modifier les règles entrantes)
3. Bouton **Add rule** (Ajouter une règle)
4. Remplir :
   ```
   Type: SSH
   Source: Custom → 0.0.0.0/0
   Description: Allow SSH
   ```
5. **Save rules** (Enregistrer les règles)

### Étape 5 : Tester
Attendez 10 secondes, puis :
```powershell
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
```

## ✅ Vérification
Dans **Inbound rules**, vous devez voir :
```
SSH | TCP | 22 | 0.0.0.0/0
```

Si cette règle existe déjà mais que SSH ne fonctionne pas :
- Vérifiez que l'instance est **running** (en cours d'exécution)
- Vérifiez que l'IP est toujours **13.63.15.86**

## 📸 Aide visuelle
Si besoin, voir le guide détaillé : `scripts/FIX-SSH-SECURITY-GROUP.md`

