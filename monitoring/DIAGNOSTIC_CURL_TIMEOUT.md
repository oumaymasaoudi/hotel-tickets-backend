# 🔍 Diagnostic : Curl Timeout vers le Backend

## 🚨 Problème

Les commandes `curl` vers `http://13.49.44.219:8081` restent bloquées (timeout).

**Causes possibles :**
1. Le backend n'est pas démarré
2. Le backend n'écoute pas sur le port 8081
3. Le Security Group AWS n'est pas encore propagé (attendre 1-2 minutes)
4. Le backend écoute seulement sur localhost (127.0.0.1) et pas sur 0.0.0.0

---

## ✅ Solution 1 : Vérifier que le Backend est Démarré

### Depuis la VM Backend (si vous avez accès)

```bash
# Vérifier les conteneurs Docker
docker ps | grep backend

# Vérifier les logs
docker logs hotel-ticket-hub-backend --tail=50

# Vérifier que le backend écoute sur le port 8081
sudo netstat -tlnp | grep 8081
# OU
sudo ss -tlnp | grep 8081
```

**Si le backend n'est pas démarré :**
```bash
cd /opt/backend
docker-compose up -d
```

---

## ✅ Solution 2 : Vérifier la Configuration Docker

Le backend doit écouter sur `0.0.0.0:8081` et pas seulement sur `127.0.0.1:8081`.

### Vérifier docker-compose.yml

```bash
# Sur la VM backend
cat /opt/backend/docker-compose.yml | grep -A 5 "ports"
```

**Doit contenir :**
```yaml
ports:
  - "8081:8080"  # ou "0.0.0.0:8081:8080"
```

**Si c'est `127.0.0.1:8081:8080`, changez pour :**
```yaml
ports:
  - "8081:8080"  # Écoute sur toutes les interfaces
```

Puis redémarrez :
```bash
cd /opt/backend
docker-compose down
docker-compose up -d
```

---

## ✅ Solution 3 : Vérifier le Security Group AWS

### Vérifier que la règle est bien sauvegardée

1. AWS Console > EC2 > Security Groups
2. Trouvez le Security Group de la VM backend (13.49.44.219)
3. Vérifiez l'onglet **Inbound rules**
4. Doit avoir une règle :
   - **Type** : Custom TCP
   - **Port** : 8081
   - **Source** : `13.62.53.224/32` (ou `0.0.0.0/0` pour les tests)

### Attendre la propagation

Les changements de Security Group peuvent prendre 1-2 minutes pour se propager.

---

## ✅ Solution 4 : Test avec Timeout

Depuis la VM monitoring, testez avec un timeout explicite :

```bash
# Test avec timeout de 5 secondes
timeout 5 curl -v http://13.49.44.219:8081/actuator/health

# Si ça timeout, le backend n'est pas accessible
```

---

## ✅ Solution 5 : Vérifier depuis votre Machine Windows

Testez depuis votre machine Windows pour voir si c'est un problème réseau :

```powershell
# Test depuis Windows
curl http://13.49.44.219:8081/actuator/health

# Si ça fonctionne depuis Windows mais pas depuis la VM monitoring :
# → Problème de Security Group (autoriser 13.62.53.224/32)
```

---

## 🎯 Actions Immédiates

### 1. Vérifier le Backend (si vous avez accès SSH)

```bash
# Se connecter à la VM backend (si vous avez la clé)
# Sinon, utilisez GitHub Actions ou une autre méthode

# Vérifier les conteneurs
docker ps

# Vérifier les logs
docker logs hotel-ticket-hub-backend --tail=50

# Tester localement
curl http://localhost:8081/actuator/health
```

### 2. Vérifier depuis la VM Monitoring

```bash
# Vous êtes déjà connecté à la VM monitoring
# Testez avec timeout
timeout 10 curl -v http://13.49.44.219:8081/actuator/health 2>&1 | head -20
```

### 3. Vérifier dans Prometheus

Même si curl timeout, Prometheus peut parfois réussir. Vérifiez :
- `http://13.62.53.224:9090/targets`
- Le target `backend` peut être UP même si curl timeout (Prometheus a des timeouts différents)

---

## 📋 Checklist

- [ ] Backend démarré sur la VM backend : `docker ps | grep backend`
- [ ] Backend écoute sur `0.0.0.0:8081` (pas seulement `127.0.0.1`)
- [ ] Security Group AWS : port 8081 ouvert pour `13.62.53.224/32`
- [ ] Attendu 1-2 minutes après modification du Security Group
- [ ] Test avec timeout : `timeout 10 curl http://13.49.44.219:8081/actuator/health`
- [ ] Vérifier dans Prometheus : `/targets` (peut être UP même si curl timeout)

---

**Commencez par vérifier si le backend est démarré et écoute sur le bon port !**

