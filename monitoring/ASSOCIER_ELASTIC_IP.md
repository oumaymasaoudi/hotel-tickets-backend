# 🔗 Associer l'Elastic IP à l'Instance Backend

## 📋 Situation Actuelle

- **Nouvelle Elastic IP** : `13.63.15.86` (créée)
- **Instance Backend** : IP actuelle `13.51.56.138`
- **Objectif** : Associer l'Elastic IP à l'instance backend

---

## ✅ Étapes pour Associer l'Elastic IP

### 1. Associer l'Elastic IP à l'Instance

1. Dans la page **Elastic IPs**, vous voyez la bannière verte :
   - Cliquez sur **"Associate this Elastic IP address"** (bouton vert)

2. **OU** manuellement :
   - Sélectionnez l'Elastic IP `13.63.15.86`
   - Cliquez sur **Actions** → **Associate Elastic IP address**

3. Dans le formulaire :
   - **Resource type** : Sélectionnez **Instance**
   - **Instance** : Sélectionnez votre instance backend (celle avec l'IP `13.51.56.138`)
   - **Private IP address** : Laissez par défaut (sélectionne automatiquement)
   - Cliquez sur **Associate**

### 2. Vérifier l'Association

1. **EC2** → **Instances**
2. Sélectionnez votre instance backend
3. Vérifiez que l'**IPv4 Public IP** est maintenant `13.63.15.86`

---

## 🔄 Mettre à Jour Toutes les Configurations

Une fois l'Elastic IP associée, mettez à jour :

### 1. GitHub Secrets (PRIORITAIRE)

1. **GitHub** → Repo `hotel-ticket-hub-backend`
2. **Settings** → **Secrets and variables** → **Actions**
3. Mettez à jour `STAGING_HOST` : `13.63.15.86`

### 2. Configuration Prometheus

**Fichier local** (déjà mis à jour) :
- `monitoring/prometheus/prometheus-remote.yml` ✅

**Sur la VM Monitoring** :

```powershell
# Se connecter à la VM Monitoring
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224

# Éditer la configuration
nano /opt/monitoring/prometheus/prometheus-remote.yml

# Changer la ligne :
# - targets: ['13.51.56.138:8081']
# Par :
# - targets: ['13.63.15.86:8081']

# Sauvegarder (Ctrl+O, Enter, Ctrl+X)

# Redémarrer Prometheus
docker restart prometheus
```

### 3. Security Group AWS

**Vérifier la règle Prometheus** :

1. **AWS Console** → **EC2** → **Security Groups**
2. Trouvez le Security Group de la VM Backend
3. **Inbound rules** → Vérifiez la règle pour le port 8081
4. Si elle pointe vers `13.62.53.224/32` (VM Monitoring), c'est bon ✅
5. Si elle pointe vers une IP spécifique de backend, mettez à jour

### 4. Documentation

Mettez à jour les fichiers de documentation qui référencent l'ancienne IP :
- `COMMANDES_PUSH_ET_VERIFICATION.md`
- Tous les fichiers `monitoring/DIAGNOSTIC_*.md`

---

## ✅ Vérification

### Test 1 : Vérifier l'IP

```powershell
# L'IP devrait être 13.63.15.86 maintenant
curl http://13.63.15.86:8081/actuator/health
```

### Test 2 : Vérifier SSH

```powershell
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.63.15.86 "echo OK"
```

### Test 3 : Vérifier dans Prometheus

1. Ouvrez : `http://13.62.53.224:9090/targets`
2. Vérifiez que le target `backend` pointe vers `13.63.15.86:8081`
3. Le target devrait être **UP** (vert)

---

## 🎯 Résumé des Actions

1. ✅ **Associer l'Elastic IP** `13.63.15.86` à l'instance backend
2. ✅ **Mettre à jour** le secret GitHub `STAGING_HOST`
3. ✅ **Mettre à jour** Prometheus sur la VM Monitoring
4. ✅ **Tester** que tout fonctionne

---

## ⚠️ Note Importante

**L'ancienne IP `13.49.44.219` n'est plus disponible** car elle a été libérée quand l'instance a changé de type. Vous ne pouvez pas la réutiliser.

**La nouvelle Elastic IP `13.63.15.86` sera votre IP fixe** pour la VM Backend. Elle ne changera plus, même après redémarrage ou changement de type d'instance.

---

**Une fois l'Elastic IP associée, votre IP sera fixe !** 🎉

