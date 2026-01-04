# ✅ Actions Finales - IP Backend Fixe Configurée

## 🎉 État Actuel

✅ **Elastic IP associée** : `13.63.15.86` → Instance `backend-staging`  
✅ **Instance type** : `t3.small` (2 Go RAM)  
✅ **IP fixe** : Ne changera plus jamais !

---

## 📋 Actions à Faire MAINTENANT

### 1. 🔑 Mettre à Jour GitHub Secrets (PRIORITAIRE)

1. **GitHub** → Repo `hotel-ticket-hub-backend`
2. **Settings** → **Secrets and variables** → **Actions**
3. Mettez à jour `STAGING_HOST` : `13.63.15.86`
4. **Save**

### 2. 🔄 Mettre à Jour Prometheus sur la VM Monitoring

```powershell
# Se connecter à la VM Monitoring
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224

# Éditer la configuration Prometheus
nano /opt/monitoring/prometheus/prometheus.yml

# Vérifier/Changer la ligne 53 :
# - targets: ['13.63.15.86:8081']

# Sauvegarder (Ctrl+O, Enter, Ctrl+X)

# Redémarrer Prometheus
docker restart prometheus

# Vérifier les logs
docker logs prometheus --tail=20
```

### 3. ✅ Tester que Tout Fonctionne

#### Test 1 : Health Check Backend
```powershell
curl http://13.63.15.86:8081/actuator/health
```
**Résultat attendu** : `{"status":"UP"}`

#### Test 2 : Métriques Prometheus
```powershell
curl http://13.63.15.86:8081/actuator/prometheus | head -20
```
**Résultat attendu** : Des métriques (lignes commençant par `#` et `http_server_requests_seconds_count`, etc.)

#### Test 3 : Connexion SSH
```powershell
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.63.15.86 "echo OK"
```
**Résultat attendu** : `OK`

#### Test 4 : Vérifier dans Prometheus UI
1. Ouvrez : `http://13.62.53.224:9090/targets`
2. Vérifiez que le target `backend` est **UP** (vert)
3. L'IP doit être `13.63.15.86:8081`

#### Test 5 : Vérifier dans Grafana
1. Ouvrez : `http://13.62.53.224:3000`
2. Connectez-vous (admin/admin)
3. **Explore** → Testez : `up{job="backend"}`
4. **Résultat attendu** : `up{job="backend", instance="13.63.15.86:8081"}` = 1

---

## 📝 Résumé des IPs

| VM | IP | Elastic IP | Type |
|---|---|---|---|
| **Backend** | `13.63.15.86` | ✅ `13.63.15.86` | `t3.small` |
| **Ansible/Monitoring** | `13.62.53.224` | ❌ | `t3.micro` |
| **Frontend** | `51.21.196.104` | ❌ | `t3.micro` |
| **Database** | `13.48.83.147` | ✅ `13.48.83.147` | `t3.micro` |

---

## 🎯 Checklist Finale

- [ ] Mettre à jour GitHub Secret `STAGING_HOST` → `13.63.15.86`
- [ ] Mettre à jour Prometheus sur la VM Monitoring
- [ ] Tester Health Check : `curl http://13.63.15.86:8081/actuator/health`
- [ ] Vérifier dans Prometheus UI : Target `backend` = UP
- [ ] Vérifier dans Grafana : Métriques backend visibles

---

## 🚀 Prochaines Étapes (Optionnel)

### Si le Backend n'est pas Démarré

```powershell
# Se connecter à la VM Backend
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.63.15.86

# Vérifier les conteneurs
docker ps

# Si le backend n'est pas là, le démarrer
cd /opt/hotel-ticket-hub-backend-staging
docker compose up -d

# Vérifier les logs
docker compose logs -f backend
```

### Redéployer via GitHub Actions

```powershell
cd hotel-ticket-hub-backend
git add .
git commit -m "fix: update backend IP to Elastic IP 13.63.15.86"
git push origin develop
```

---

## ✅ Une Fois Terminé

Votre infrastructure est maintenant **stable** avec :
- ✅ IP fixe pour le backend (ne changera plus)
- ✅ Instance `t3.small` (plus de problème OOM)
- ✅ Monitoring configuré (Prometheus → Backend)

**Tout devrait fonctionner parfaitement !** 🎉

