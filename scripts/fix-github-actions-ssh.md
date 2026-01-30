# Guide : Résoudre les problèmes de connexion SSH depuis GitHub Actions

## Problème
```
ssh: connect to host *** port 22: Connection timed out
ERROR: SSH connection failed
```

## Causes possibles

1. **Security Group AWS bloque les connexions depuis GitHub Actions**
   - Les IPs de GitHub Actions sont dynamiques
   - Le Security Group doit autoriser les connexions depuis n'importe quelle IP (0.0.0.0/0) pour le port 22

2. **Secrets GitHub non configurés**
   - `STAGING_HOST` : IP publique de la VM (ex: 13.63.15.86)
   - `STAGING_USER` : Utilisateur SSH (ex: ubuntu)
   - `STAGING_SSH_PRIVATE_KEY` : Clé privée SSH complète

3. **IP de la VM a changé**
   - Vérifier l'IP Elastic de la VM dans AWS

## Solutions

### Solution 1 : Configurer le Security Group AWS (RECOMMANDÉ)

1. **Connectez-vous à AWS Console**
2. **Allez dans EC2 > Security Groups**
3. **Trouvez le Security Group de votre VM de staging**
4. **Ajoutez une règle entrante :**
   - Type: SSH
   - Protocol: TCP
   - Port: 22
   - Source: 0.0.0.0/0 (pour autoriser depuis n'importe où)
   - Description: "Allow SSH from GitHub Actions"

**Note de sécurité :** Pour la production, utilisez les IP ranges de GitHub Actions :
- https://api.github.com/meta (récupérer les IPs de GitHub Actions)

### Solution 2 : Vérifier les secrets GitHub

1. **Allez dans votre repository GitHub**
2. **Settings > Secrets and variables > Actions**
3. **Vérifiez que ces secrets existent :**
   - `STAGING_HOST` : Doit contenir l'IP publique (ex: 13.63.15.86)
   - `STAGING_USER` : Doit contenir "ubuntu"
   - `STAGING_SSH_PRIVATE_KEY` : Doit contenir la clé privée complète (commence par `-----BEGIN OPENSSH PRIVATE KEY-----`)

### Solution 3 : Vérifier l'IP de la VM

```bash
# Depuis votre machine locale
aws ec2 describe-instances --instance-ids <INSTANCE_ID> --query 'Reservations[0].Instances[0].PublicIpAddress'
```

### Solution 4 : Tester la connexion manuellement

```bash
# Depuis votre machine locale
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86 "echo 'Connection test successful'"
```

Si cela fonctionne localement mais pas depuis GitHub Actions, c'est un problème de Security Group.

## Commandes AWS CLI pour configurer le Security Group

```bash
# Récupérer le Security Group ID de votre instance
INSTANCE_ID="i-xxxxx"
SG_ID=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --query 'Reservations[0].Instances[0].SecurityGroups[0].GroupId' --output text)

# Ajouter une règle pour autoriser SSH depuis n'importe où (0.0.0.0/0)
aws ec2 authorize-security-group-ingress \
    --group-id $SG_ID \
    --protocol tcp \
    --port 22 \
    --cidr 0.0.0.0/0 \
    --description "Allow SSH from GitHub Actions"
```

## Vérification

Après avoir configuré le Security Group, le workflow GitHub Actions devrait pouvoir se connecter.

Si le problème persiste, vérifiez les logs GitHub Actions pour plus de détails.

