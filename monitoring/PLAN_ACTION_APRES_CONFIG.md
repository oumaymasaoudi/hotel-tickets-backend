# ✅ Plan d'Action Après Configuration

## 📋 Checklist

### 1. ✅ Security Groups Configurés
- [x] **VM Ansible/Monitoring** : Ports ouverts (Prometheus, Grafana, etc.)
- [x] **VM Backend** : Port 8081 ouvert pour Prometheus depuis VM Ansible

### 2. 🔧 Résoudre le Problème OOM sur la VM Backend

**Action : Augmenter la taille de l'instance**

1. **AWS Console** → **EC2** → **Instances**
2. Trouvez l'instance backend (`13.49.44.219`)
3. **Actions** → **Instance State** → **Stop**
4. Attendez que l'état soit **Stopped**
5. **Actions** → **Instance Settings** → **Change Instance Type**
6. Sélectionnez **`t3.small`** (2 Go RAM)
7. **Apply**
8. **Actions** → **Instance State** → **Start**
9. Attendez que l'instance soit **Running**
10. **Vérifiez la nouvelle IP publique** (si elle a changé)

### 3. 🔑 Mettre à Jour les Secrets GitHub (si IP a changé)

Si l'IP publique a changé :

1. **GitHub** → Votre repo `hotel-ticket-hub-backend`
2. **Settings** → **Secrets and variables** → **Actions**
3. Mettez à jour `STAGING_HOST` avec la nouvelle IP

### 4. 🧪 Tester la Connexion SSH

```powershell
# Tester la connexion SSH vers la VM Backend
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219 "echo OK"
```

**Si ça fonctionne :** ✅ Passez à l'étape suivante
**Si ça ne fonctionne pas :** Vérifiez la nouvelle IP et réessayez

### 5. 🚀 Redéployer le Backend

**Option A : Via GitHub Actions (Recommandé)**

1. Faites un commit et push sur la branche `develop` :
   ```powershell
   cd hotel-ticket-hub-backend
   git add .
   git commit -m "fix: update docker-compose memory limits for t3.small"
   git push origin develop
   ```

2. Le pipeline GitHub Actions va :
   - Build l'image Docker
   - La pousser sur GHCR
   - Déployer sur la VM Backend

**Option B : Déploiement Manuel**

```powershell
# Se connecter à la VM Backend
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219

# Sur la VM Backend
cd /opt/hotel-ticket-hub-backend-staging
docker compose pull
docker compose down
docker compose up -d

# Vérifier les logs
docker compose logs -f backend
```

### 6. ✅ Vérifier que le Backend Fonctionne

```powershell
# Test depuis votre machine
curl http://13.49.44.219:8081/actuator/health

# Test des métriques Prometheus
curl http://13.49.44.219:8081/actuator/prometheus | head -20
```

**Résultat attendu :**
- `{"status":"UP"}` pour le health check
- Des métriques Prometheus (lignes commençant par `#` et `http_server_requests_seconds_count`, etc.)

### 7. 🔍 Vérifier dans Prometheus

1. Ouvrez : `http://13.62.53.224:9090`
2. Allez dans **Status** → **Targets**
3. Vérifiez que le target `backend` est **UP** (vert)

**Si DOWN :**
- Vérifiez que le backend est démarré
- Vérifiez que le port 8081 est accessible depuis la VM Ansible
- Vérifiez les logs Prometheus

### 8. 📊 Vérifier dans Grafana

1. Ouvrez : `http://13.62.53.224:3000`
2. Connectez-vous (admin/admin)
3. Allez dans **Explore**
4. Testez la query : `up{job="backend"}`

**Résultat attendu :** `up{job="backend", instance="13.49.44.219:8081"}` = 1

---

## 🎯 Résumé des Actions Immédiates

1. **Augmenter la VM Backend à t3.small** (résout le problème OOM)
2. **Tester la connexion SSH**
3. **Redéployer le backend** (via GitHub Actions ou manuellement)
4. **Vérifier que tout fonctionne** (health check, Prometheus, Grafana)

---

## ⚠️ Notes Importantes

- **Elastic IP** : Si vous changez le type d'instance, l'IP peut changer. Utilisez une Elastic IP pour éviter ce problème à l'avenir.
- **Coût** : t3.small coûte ~$15/mois (au lieu de ~$7.50/mois pour t3.micro)
- **Performance** : t3.small devrait être suffisant pour le backend seul (monitoring sur VM séparée)

---

**Une fois ces étapes terminées, votre infrastructure devrait être opérationnelle !** 🚀

