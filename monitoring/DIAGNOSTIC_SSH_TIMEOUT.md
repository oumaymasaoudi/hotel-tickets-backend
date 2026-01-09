# 🔍 Diagnostic : SSH Timeout vers la VM Backend

## ❌ Problème

```
ssh: connect to host 13.49.44.219 port 22: Connection timed out
```

La connexion SSH vers la VM Backend (`13.49.44.219`) échoue avec un timeout.

## 🔍 Causes Possibles

### 1. Security Group AWS - Port 22 Fermé

Le Security Group de la VM Backend ne permet pas les connexions SSH depuis votre IP ou depuis GitHub Actions.

**Solution :**

1. **AWS Console** → **EC2** → **Instances**
2. Sélectionnez l'instance `backend-staging` (ou similaire)
3. **Security** → **Security groups** → Cliquez sur le Security Group
4. **Inbound rules** → **Edit inbound rules**
5. Vérifiez qu'il y a une règle pour le port **22 (SSH)** :
   - **Type** : SSH
   - **Protocol** : TCP
   - **Port** : 22
   - **Source** : 
     - `0.0.0.0/0` (pour tester depuis n'importe où)
     - OU votre IP publique
     - OU les IPs de GitHub Actions (voir ci-dessous)

### 2. VM Arrêtée ou Non Démarrée

La VM Backend peut être arrêtée.

**Solution :**

1. **AWS Console** → **EC2** → **Instances**
2. Vérifiez l'état de l'instance :
   - ✅ **Running** = OK
   - ❌ **Stopped** = Démarrer l'instance
   - ❌ **Terminated** = Instance supprimée, créer une nouvelle

### 3. IP Publique Changée

L'IP publique de la VM peut avoir changé si l'instance a été arrêtée/redémarrée (sauf si vous utilisez une Elastic IP).

**Solution :**

1. **AWS Console** → **EC2** → **Instances**
2. Vérifiez l'**IPv4 Public IP** de l'instance
3. Si elle a changé, mettez à jour le secret `STAGING_HOST` dans GitHub

### 4. Firewall Local ou Réseau

Votre firewall local ou réseau peut bloquer les connexions SSH.

**Solution :**

```powershell
# Tester depuis un autre réseau (ex: hotspot mobile)
# OU désactiver temporairement le firewall Windows
```

## ✅ Solutions par Ordre de Priorité

### Solution 1 : Vérifier le Security Group (Le Plus Probable)

```powershell
# 1. Allez sur AWS Console
# 2. EC2 → Instances → backend-staging
# 3. Security → Security groups → Edit inbound rules
# 4. Ajoutez/modifiez la règle SSH :
#    - Type: SSH
#    - Port: 22
#    - Source: 0.0.0.0/0 (temporairement pour tester)
```

### Solution 2 : Vérifier l'État de la VM

```powershell
# Via AWS Console
# EC2 → Instances → Vérifier l'état
# Si "Stopped" → Actions → Instance State → Start
```

### Solution 3 : Vérifier l'IP Publique

```powershell
# Via AWS Console
# EC2 → Instances → backend-staging
# Vérifier "IPv4 Public IP"
# Si différent de 13.49.44.219, mettre à jour le secret STAGING_HOST
```

### Solution 4 : Tester avec une Elastic IP

Si l'IP change souvent, utilisez une Elastic IP :

1. **AWS Console** → **EC2** → **Elastic IPs**
2. **Allocate Elastic IP address**
3. **Actions** → **Associate Elastic IP address**
4. Sélectionnez l'instance backend
5. Mettez à jour le secret `STAGING_HOST` avec la nouvelle IP

## 🧪 Tests de Diagnostic

### Test 1 : Ping

```powershell
# Tester si la VM répond au ping
ping 13.49.44.219
```

Si le ping échoue, la VM est probablement arrêtée ou le Security Group bloque ICMP.

### Test 2 : Port 22 Ouvert

```powershell
# Tester si le port 22 est ouvert (nécessite Test-NetConnection)
Test-NetConnection -ComputerName 13.49.44.219 -Port 22
```

Si `TcpTestSucceeded: False`, le port 22 est fermé dans le Security Group.

### Test 3 : Connexion SSH avec Timeout Court

```powershell
# Test avec timeout de 5 secondes
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem -o ConnectTimeout=5 ubuntu@13.49.44.219 "echo OK"
```

## 📋 Checklist de Diagnostic

- [ ] Security Group : Port 22 ouvert pour `0.0.0.0/0` ou votre IP
- [ ] VM Backend : État = **Running**
- [ ] IP Publique : Vérifier que c'est bien `13.49.44.219`
- [ ] Elastic IP : Si configurée, vérifier qu'elle est associée
- [ ] Firewall Local : Vérifier qu'il ne bloque pas SSH
- [ ] Clé SSH : Vérifier que `oumayma-key.pem` est la bonne clé

## 🚨 Action Immédiate

1. **Allez sur AWS Console** → **EC2** → **Instances**
2. **Trouvez l'instance backend** (cherchez par nom ou IP)
3. **Vérifiez l'état** :
   - Si **Stopped** → **Start** l'instance
   - Si **Running** → Passez à l'étape suivante
4. **Vérifiez le Security Group** :
   - Cliquez sur le Security Group
   - **Inbound rules** → Vérifiez qu'il y a SSH (port 22)
   - Si absent, **Edit inbound rules** → **Add rule** :
     - Type: SSH
     - Source: `0.0.0.0/0` (temporairement)
5. **Testez à nouveau** :
   ```powershell
   ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219 "echo OK"
   ```

## 💡 Note Importante

Si vous utilisez une **Elastic IP**, l'IP ne changera pas même si l'instance est arrêtée/redémarrée. C'est recommandé pour les environnements de staging/production.

---

**Une fois le problème résolu, le pipeline GitHub Actions devrait fonctionner !** 🚀

