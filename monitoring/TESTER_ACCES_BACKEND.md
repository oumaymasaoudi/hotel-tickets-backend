# 🔍 Tester l'Accès au Backend après Ouverture du Port 8081

## ✅ Étape 1 : Vérifier les Clés SSH Disponibles

```powershell
# Lister les clés SSH disponibles
ls C:\Users\oumay\.ssh\
```

**Clés possibles :**
- `oumayma-key.pem` (pour la VM monitoring)
- `github-actions-key` (peut-être avec une extension différente)
- Autre clé pour la VM backend

---

## ✅ Étape 2 : Tester l'Accès au Backend depuis la VM Monitoring

Puisque vous avez déjà accès à la VM monitoring, testons depuis là :

```powershell
# Se connecter à la VM monitoring
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224

# Tester l'accès au backend
curl -v http://13.49.44.219:8081/actuator/health

# Tester les métriques Prometheus
curl http://13.49.44.219:8081/actuator/prometheus | head -30
```

**Si vous voyez des métriques :**
- ✅ Le port 8081 est bien ouvert
- ✅ Le backend est accessible
- Prometheus devrait pouvoir scraper

**Si vous voyez "Connection refused" ou "Connection timed out" :**
- Le port 8081 n'est peut-être pas encore propagé (attendez 1-2 minutes)
- Vérifiez que la règle est bien sauvegardée dans AWS

---

## ✅ Étape 3 : Vérifier dans Prometheus

1. Allez sur : `http://13.62.53.224:9090/targets`
2. Attendez 30-60 secondes (Prometheus scrape toutes les 15s)
3. Le target `backend` devrait passer à **UP** (vert)

**Si c'est toujours DOWN :**
- Redémarrez Prometheus :
  ```powershell
  ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224
  docker restart prometheus
  ```

---

## ✅ Étape 4 : Trouver la Clé SSH pour la VM Backend

### Option 1 : Utiliser la même clé que la VM monitoring

```powershell
# Essayer avec oumayma-key.pem
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219
```

### Option 2 : Vérifier dans GitHub Actions

La clé `github-actions-key` est peut-être stockée dans les secrets GitHub. Pour la VM backend, vous pouvez :
- Utiliser la clé depuis GitHub Actions (si elle est configurée)
- Ou utiliser une autre clé si vous en avez une

### Option 3 : Vérifier les clés disponibles

```powershell
# Lister toutes les clés
Get-ChildItem C:\Users\oumay\.ssh\*.pem
Get-ChildItem C:\Users\oumay\.ssh\*.key
Get-ChildItem C:\Users\oumay\.ssh\* -File | Select-Object Name
```

---

## 🎯 Action Immédiate

**Testez depuis la VM monitoring (vous avez déjà la clé) :**

```powershell
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224
curl http://13.49.44.219:8081/actuator/prometheus | head -30
```

**Si ça fonctionne :**
1. Allez sur Prometheus : `http://13.62.53.224:9090/targets`
2. Le target `backend` devrait être UP
3. Les dashboards Grafana devraient afficher des données

---

## 📋 Checklist

- [ ] Port 8081 ouvert dans AWS Security Group (✅ FAIT d'après l'image)
- [ ] Backend accessible depuis VM monitoring : `curl http://13.49.44.219:8081/actuator/prometheus`
- [ ] Target `backend` UP dans Prometheus (`/targets`)
- [ ] Dashboards Grafana affichent des données

---

**Commencez par tester depuis la VM monitoring, c'est le plus rapide !**

