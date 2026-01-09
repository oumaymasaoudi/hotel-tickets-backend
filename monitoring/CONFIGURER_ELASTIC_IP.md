# 🔧 Configurer une Elastic IP pour la VM Backend

## 🎯 Objectif

Avoir une **IP fixe** pour la VM Backend, même après redémarrage ou changement de type d'instance.

---

## 📋 Étapes

### 1. Allouer une Elastic IP

1. **AWS Console** → **EC2** → **Network & Security** → **Elastic IPs**
2. Cliquez sur **Allocate Elastic IP address**
3. **Network border group** : Sélectionnez la région (ex: `eu-north-1`)
4. **Public IPv4 address pool** : Laissez par défaut (`Amazon's pool of IPv4 addresses`)
5. Cliquez sur **Allocate**
6. **Notez la nouvelle Elastic IP** (ex: `13.49.44.219` ou une nouvelle)

### 2. Associer l'Elastic IP à l'Instance Backend

1. Dans la liste des **Elastic IPs**, sélectionnez celle que vous venez de créer
2. Cliquez sur **Actions** → **Associate Elastic IP address**
3. **Resource type** : Sélectionnez **Instance**
4. **Instance** : Sélectionnez votre instance backend
5. **Private IP address** : Laissez par défaut (sélectionne automatiquement)
6. Cliquez sur **Associate**

### 3. Vérifier l'Association

1. **EC2** → **Instances**
2. Sélectionnez votre instance backend
3. Vérifiez que l'**IPv4 Public IP** correspond à votre Elastic IP

---

## 🔄 Option : Réutiliser l'Ancienne IP (si disponible)

Si l'ancienne IP `13.49.44.219` est encore disponible :

1. **EC2** → **Elastic IPs**
2. Cliquez sur **Allocate Elastic IP address**
3. **Public IPv4 address pool** : Sélectionnez **"Use an IP address from a pool that I own"**
4. Si l'ancienne IP apparaît dans la liste, sélectionnez-la
5. **Allocate**
6. **Associez-la** à votre instance (étape 2 ci-dessus)

⚠️ **Note** : Si l'ancienne IP n'est plus disponible, vous devrez utiliser une nouvelle Elastic IP.

---

## 🔑 Mettre à Jour les Secrets GitHub

Une fois l'Elastic IP associée :

1. **GitHub** → Votre repo `hotel-ticket-hub-backend`
2. **Settings** → **Secrets and variables** → **Actions**
3. Mettez à jour le secret `STAGING_HOST` avec la nouvelle Elastic IP

---

## 📝 Mettre à Jour les Configurations

### 1. Configuration Prometheus

Configuration Prometheus :

```yaml
# Fichier : monitoring/prometheus/prometheus.yml
- job_name: 'backend'
  metrics_path: '/actuator/prometheus'
  static_configs:
    - targets: ['NOUVELLE_IP:8081']  # Remplacez par la nouvelle Elastic IP
```

### 2. Documentation

Mettez à jour tous les fichiers de documentation qui référencent l'ancienne IP :
- `monitoring/DIAGNOSTIC_*.md`
- `monitoring/SOLUTION_*.md`
- `COMMANDES_PUSH_ET_VERIFICATION.md`

---

## ✅ Vérification

### Test 1 : Vérifier l'IP

```powershell
# L'IP devrait être fixe maintenant
curl http://NOUVELLE_IP:8081/actuator/health
```

### Test 2 : Vérifier depuis Prometheus

1. Ouvrez : `http://13.62.53.224:9090/targets`
2. Vérifiez que le target `backend` pointe vers la nouvelle IP
3. Le target devrait être **UP** (vert)

---

## 🎯 Avantages de l'Elastic IP

✅ **IP fixe** : Ne change pas même après redémarrage
✅ **Pas besoin de mettre à jour les configs** : Une fois configurée, c'est permanent
✅ **Gratuit** : Tant que l'instance est running
✅ **Réutilisable** : Peut être réassociée à une autre instance si besoin

---

## ⚠️ Notes Importantes

1. **Coût** : L'Elastic IP est **gratuite** tant que :
   - L'instance est **running**
   - L'Elastic IP est **associée** à une instance running
   - Si l'instance est **stopped** ou l'IP n'est **pas associée**, AWS facture ~$0.005/heure

2. **Limite** : Par défaut, AWS permet **5 Elastic IPs** par région

3. **Désassociation** : Si vous arrêtez l'instance, l'Elastic IP reste associée mais l'instance n'a plus d'IP publique. Au redémarrage, l'Elastic IP sera automatiquement réassociée.

---

## 🚀 Action Immédiate

1. **Allouer une Elastic IP** dans AWS
2. **L'associer** à l'instance backend
3. **Mettre à jour** le secret `STAGING_HOST` dans GitHub
4. **Mettre à jour** la configuration Prometheus si nécessaire
5. **Tester** que tout fonctionne

---

**Une fois l'Elastic IP configurée, votre IP ne changera plus !** 🎉

