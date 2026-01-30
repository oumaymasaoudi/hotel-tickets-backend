# Guide : Corriger le Security Group via AWS Console

## Étapes pour autoriser SSH depuis n'importe où

### 1. Connectez-vous à AWS Console
- Allez sur https://console.aws.amazon.com
- Connectez-vous avec vos identifiants

### 2. Trouvez votre instance EC2
- Allez dans **EC2** > **Instances**
- Recherchez l'instance avec l'IP `13.63.15.86`
- Notez le **Security Group** associé (ex: `sg-xxxxx`)

### 3. Modifiez le Security Group
- Cliquez sur le nom du **Security Group** dans la liste
- Ou allez dans **EC2** > **Security Groups** et trouvez le groupe

### 4. Ajoutez une règle SSH
- Cliquez sur l'onglet **Inbound rules** (Règles entrantes)
- Cliquez sur **Edit inbound rules** (Modifier les règles entrantes)
- Cliquez sur **Add rule** (Ajouter une règle)
- Configurez :
  - **Type**: SSH
  - **Protocol**: TCP
  - **Port range**: 22
  - **Source**: Custom → `0.0.0.0/0`
  - **Description**: Allow SSH from anywhere
- Cliquez sur **Save rules** (Enregistrer les règles)

### 5. Testez la connexion SSH
```powershell
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
```

## Vérification rapide

Si vous voyez dans les règles entrantes :
- ✅ Une règle SSH (port 22) avec source `0.0.0.0/0` → **OK, SSH devrait fonctionner**
- ❌ Pas de règle SSH ou source limitée → **C'est le problème !**

## Note de sécurité

Autoriser `0.0.0.0/0` permet les connexions depuis n'importe où. Pour plus de sécurité :
- Utilisez votre IP publique actuelle
- Ou utilisez les IP ranges de GitHub Actions : https://api.github.com/meta

