# 🚨 Action Immédiate : IP Backend Changée

## 📋 Situation

- **Ancienne IP** : `13.49.44.219`
- **Nouvelle IP** : `13.51.56.138` (après changement de type d'instance)

## ✅ Actions à Faire MAINTENANT

### 1. Mettre à Jour GitHub Secrets

1. **GitHub** → Repo `hotel-ticket-hub-backend`
2. **Settings** → **Secrets and variables** → **Actions**
3. Mettez à jour `STAGING_HOST` : `13.51.56.138`

### 2. Mettre à Jour la Configuration Prometheus

Le fichier `prometheus-remote.yml` a déjà été mis à jour avec la nouvelle IP.

**Sur la VM Ansible/Monitoring :**

```powershell
# Se connecter à la VM Monitoring
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224

# Vérifier la configuration
cat /opt/monitoring/prometheus/prometheus-remote.yml | grep "13.51.56.138"

# Si l'IP n'est pas à jour, éditer le fichier
nano /opt/monitoring/prometheus/prometheus-remote.yml
# Changer 13.49.44.219 par 13.51.56.138

# Redémarrer Prometheus
docker restart prometheus
```

### 3. Mettre à Jour le Security Group AWS

**Si nécessaire** (normalement pas besoin si la règle utilise l'IP spécifique) :

1. **AWS Console** → **EC2** → **Security Groups**
2. Trouvez le Security Group de la VM Backend
3. Vérifiez la règle pour le port 8081 (Prometheus)
4. Si elle pointe vers `13.49.44.219/32`, changez-la pour `13.51.56.138/32`

### 4. Tester la Connexion

```powershell
# Test SSH
ssh -i C:\Users\oumay\\.ssh\oumayma-key.pem ubuntu@13.51.56.138 "echo OK"

# Test Health Check
curl http://13.51.56.138:8081/actuator/health

# Test Métriques Prometheus
curl http://13.51.56.138:8081/actuator/prometheus | head -20
```

### 5. Vérifier dans Prometheus

1. Ouvrez : `http://13.62.53.224:9090/targets`
2. Vérifiez que le target `backend` pointe vers `13.51.56.138:8081`
3. Le target devrait être **UP** (vert)

---

## 🔧 Pour Éviter ce Problème à l'Avenir

**Configurez une Elastic IP** (voir `CONFIGURER_ELASTIC_IP.md`) :

1. **AWS Console** → **EC2** → **Elastic IPs**
2. **Allocate Elastic IP address**
3. **Associate** à l'instance backend
4. L'IP ne changera plus jamais !

---

## 📝 Fichiers à Mettre à Jour

- [x] `monitoring/prometheus/prometheus-remote.yml` ✅ (déjà fait)
- [ ] Secret GitHub `STAGING_HOST` ⚠️ (à faire)
- [ ] Configuration Prometheus sur la VM ⚠️ (à faire)
- [ ] Security Group AWS (si nécessaire) ⚠️ (à vérifier)

---

**Action prioritaire : Mettre à jour le secret GitHub `STAGING_HOST` !** 🚨

