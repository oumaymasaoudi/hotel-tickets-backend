# 🔧 Résoudre les Problèmes Backend

## 🚨 Problèmes Identifiés

1. **Backend non accessible** : `curl http://13.63.15.86:8081/actuator/health` → Failed to connect
2. **SSH Permission denied** : La clé SSH n'est pas configurée sur la nouvelle instance

---

## ✅ Solution 1 : Configurer la Clé SSH sur la VM Backend

### Option A : Via AWS Systems Manager (SSM) - Si Activé

1. **AWS Console** → **EC2** → **Instances**
2. Sélectionnez l'instance `backend-staging`
3. **Connect** → **Session Manager** (si disponible)
4. Si SSM n'est pas activé, passez à l'Option B

### Option B : Via AWS EC2 Instance Connect

1. **AWS Console** → **EC2** → **Instances**
2. Sélectionnez l'instance `backend-staging`
3. **Connect** → **EC2 Instance Connect**
4. Cliquez sur **Connect**

### Option C : Ajouter la Clé SSH via GitHub Actions

Utilisez le workflow `add-oumayma-key.yml` :

1. **GitHub** → Repo `hotel-ticket-hub-backend`
2. **Actions** → **Add Oumayma SSH Key**
3. **Run workflow** → Sélectionnez la branche `develop`
4. Le workflow va ajouter votre clé publique SSH à la VM

**Note** : Assurez-vous que le secret `STAGING_HOST` est mis à jour avec `13.63.15.86`

---

## ✅ Solution 2 : Vérifier que le Backend est Démarré

### Via AWS Systems Manager (SSM)

```bash
# Si SSM est activé, connectez-vous via Session Manager
# Puis exécutez :

# Vérifier les conteneurs
docker ps

# Si le backend n'est pas là
cd /opt/hotel-ticket-hub-backend-staging
docker compose ps
docker compose up -d

# Vérifier les logs
docker compose logs -f backend
```

### Via EC2 Instance Connect

1. **AWS Console** → **EC2** → **Instances**
2. Sélectionnez `backend-staging`
3. **Connect** → **EC2 Instance Connect**
4. Exécutez les mêmes commandes que ci-dessus

---

## ✅ Solution 3 : Vérifier le Security Group

### Vérifier que le Port 8081 est Ouvert

1. **AWS Console** → **EC2** → **Instances**
2. Sélectionnez `backend-staging`
3. **Security** → Cliquez sur le **Security Group**
4. **Inbound rules** → Vérifiez qu'il y a une règle :
   - **Type** : Custom TCP
   - **Port** : 8081
   - **Source** : `13.62.53.224/32` (VM Monitoring) OU `0.0.0.0/0` (pour les tests)

**Si la règle n'existe pas :**
1. **Edit inbound rules**
2. **Add rule**
3. **Type** : Custom TCP
4. **Port range** : 8081
5. **Source** : `13.62.53.224/32` (pour Prometheus)
6. **Description** : "Prometheus monitoring"
7. **Save rules**

---

## ✅ Solution 4 : Redéployer le Backend

### Via GitHub Actions (Recommandé)

1. **GitHub** → Repo `hotel-ticket-hub-backend`
2. **Actions** → **CI/CD Pipeline**
3. **Run workflow** → Sélectionnez la branche `develop`
4. Le workflow va :
   - Build l'image Docker
   - La pousser sur GHCR
   - Déployer sur la VM Backend

**Prérequis** :
- Le secret `STAGING_HOST` doit être `13.63.15.86`
- Le secret `STAGING_SSH_PRIVATE_KEY` doit être configuré

### Via AWS Systems Manager (SSM)

```bash
# Se connecter via Session Manager
# Puis :

cd /opt/hotel-ticket-hub-backend-staging

# Pull la dernière image
docker compose pull

# Redémarrer les services
docker compose down
docker compose up -d

# Vérifier les logs
docker compose logs -f backend
```

---

## 🧪 Tests de Vérification

### Test 1 : Vérifier que le Backend est Démarré

```bash
# Depuis la VM Monitoring (13.62.53.224)
curl http://13.63.15.86:8081/actuator/health
```

**Résultat attendu** : `{"status":"UP"}`

### Test 2 : Vérifier les Métriques Prometheus

```bash
# Depuis la VM Monitoring
curl http://13.63.15.86:8081/actuator/prometheus | head -20
```

**Résultat attendu** : Des métriques (lignes commençant par `#`)

### Test 3 : Vérifier dans Prometheus UI

1. Ouvrez : `http://13.62.53.224:9090/targets`
2. Le target `backend` doit être **UP** (vert)
3. L'IP doit être `13.63.15.86:8081`

---

## 📋 Checklist de Résolution

- [ ] **Configurer la clé SSH** sur la VM Backend (via SSM ou GitHub Actions)
- [ ] **Vérifier le Security Group** : Port 8081 ouvert depuis VM Monitoring
- [ ] **Vérifier que le backend est démarré** : `docker ps` sur la VM Backend
- [ ] **Redéployer le backend** si nécessaire (via GitHub Actions ou manuellement)
- [ ] **Tester** : `curl http://13.63.15.86:8081/actuator/health`
- [ ] **Vérifier dans Prometheus** : Target backend = UP

---

## 🚀 Action Immédiate Recommandée

1. **Configurer la clé SSH** via GitHub Actions workflow `add-oumayma-key.yml`
2. **Vérifier le Security Group** : Port 8081 ouvert
3. **Se connecter à la VM Backend** via SSM ou EC2 Instance Connect
4. **Vérifier/Démarrer le backend** : `docker compose ps` et `docker compose up -d`
5. **Tester** : `curl http://13.63.15.86:8081/actuator/health`

---

**Une fois ces étapes terminées, le backend devrait être accessible !** 🎉

