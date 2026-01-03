# 🚀 Guide d'Action - Prochaines Étapes

## ✅ Étape 1 : Ajouter les Secrets GitHub Manquants

### Dans GitHub → Settings → Secrets and variables → Actions

#### Pour le Monitoring (nouveau workflow)

1. Allez sur votre repo GitHub : `hotel-ticket-hub-backend`
2. **Settings** → **Secrets and variables** → **Actions**
3. Cliquez sur **"New repository secret"** et ajoutez :

| Nom du Secret | Valeur | Comment obtenir |
|---------------|--------|-----------------|
| `MONITORING_HOST` | `13.62.53.224` | IP de votre VM Ansible/Monitoring |
| `MONITORING_USER` | `ubuntu` | Utilisateur SSH (généralement `ubuntu`) |
| `MONITORING_SSH_PRIVATE_KEY` | Votre clé privée SSH | Contenu de votre fichier `.pem` ou `id_rsa` pour la VM Monitoring |

**Comment obtenir la clé privée :**
```powershell
# Si vous avez le fichier .pem
cat C:\Users\oumay\.ssh\oumayma-key.pem

# Copiez TOUT le contenu (de -----BEGIN jusqu'à -----END)
```

#### Vérifier les secrets existants pour le Backend

Vérifiez que ces secrets existent déjà :
- ✅ `STAGING_HOST` (doit être `13.49.44.219`)
- ✅ `STAGING_USER` (doit être `ubuntu`)
- ✅ `STAGING_SSH_PRIVATE_KEY` (clé privée pour la VM Backend)
- ✅ `GHCR_TOKEN` (token GitHub Container Registry)

---

## ✅ Étape 2 : Tester les Workflows Améliorés

### Test 1 : Vérifier le Backend (Workflow existant amélioré)

1. Allez sur GitHub → **Actions**
2. Sélectionnez **"Check Backend Status"**
3. Cliquez sur **"Run workflow"** → **"Run workflow"**
4. ✅ Vérifiez que ça fonctionne avec la nouvelle configuration SSH

### Test 2 : Déployer le Monitoring (Nouveau workflow)

1. Allez sur GitHub → **Actions**
2. Sélectionnez **"Deploy Monitoring Stack"**
3. Cliquez sur **"Run workflow"** → **"Run workflow"**
4. ✅ Vérifiez que le monitoring se déploie sur la VM Ansible

### Test 3 : Déployer le Backend (Workflow amélioré)

1. Faites un petit changement dans le code backend
2. Committez et pushez sur la branche `develop` :
   ```powershell
   git add .
   git commit -m "test: vérification du déploiement amélioré"
   git push origin develop
   ```
3. Allez sur GitHub → **Actions**
4. ✅ Vérifiez que le workflow `ci.yml` fonctionne et que le déploiement est plus rapide

---

## ✅ Étape 3 : Vérifier l'Architecture

### Sur la VM Backend (`13.49.44.219`)

Connectez-vous et vérifiez :
```bash
ssh -i ~/.ssh/your-key.pem ubuntu@13.49.44.219

# Vérifier qu'il n'y a QUE le backend
cd /opt/hotel-ticket-hub-backend-staging
ls -la
# ✅ Doit contenir : docker-compose.yml, .env, uploads/
# ❌ Ne doit PAS contenir : prometheus/, grafana/, monitoring/

# Vérifier les conteneurs
docker ps
# ✅ Doit voir : hotel-ticket-hub-backend-staging
# ❌ Ne doit PAS voir : prometheus, grafana, alertmanager
```

### Sur la VM Monitoring (`13.62.53.224`)

Connectez-vous et vérifiez :
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.62.53.224

# Vérifier que le monitoring est déployé
cd /opt/monitoring
ls -la
# ✅ Doit contenir : docker-compose.monitoring.yml, prometheus/, grafana/, etc.

# Vérifier les conteneurs
docker ps
# ✅ Doit voir : prometheus, grafana, alertmanager, node-exporter, cadvisor
# ❌ Ne doit PAS voir : hotel-ticket-hub-backend-staging
```

---

## ✅ Étape 4 : Vérifier que Prometheus Scrape le Backend

### Sur la VM Monitoring

```bash
# Vérifier la configuration Prometheus
cat /opt/monitoring/prometheus/prometheus.yml | grep -A 5 "job_name: 'backend'"

# Doit afficher :
# - targets: ['13.49.44.219:8081']  # IP de la VM Backend
```

### Dans Prometheus UI

1. Ouvrez : `http://13.62.53.224:9090`
2. Allez dans **Status** → **Targets**
3. ✅ Vérifiez que le target `backend` est **UP** (vert)
4. ✅ Vérifiez que les métriques sont collectées

---

## 🚨 En Cas de Problème

### Le workflow échoue avec "Secret not found"

→ Vérifiez que vous avez bien ajouté tous les secrets dans GitHub Settings

### Le déploiement est toujours lent

→ Vérifiez les logs du workflow pour voir quelle étape prend du temps
→ Vérifiez que vous n'envoyez pas trop de fichiers (on doit envoyer seulement `docker-compose.yml`)

### Erreur SSH "Permission denied"

→ Vérifiez que la clé privée est correcte (copiez TOUT le contenu, y compris les lignes `-----BEGIN` et `-----END`)
→ Vérifiez que l'utilisateur SSH est correct (`ubuntu` généralement)

### Le monitoring ne scrape pas le backend

→ Vérifiez que le port 8081 est ouvert dans le Security Group AWS pour la VM Monitoring
→ Vérifiez que Prometheus utilise `prometheus-remote.yml` (pour VM séparée)

---

## 📝 Checklist Finale

- [ ] Secrets GitHub ajoutés (`MONITORING_HOST`, `MONITORING_USER`, `MONITORING_SSH_PRIVATE_KEY`)
- [ ] Workflow "Check Backend Status" testé et fonctionnel
- [ ] Workflow "Deploy Monitoring Stack" testé et fonctionnel
- [ ] Workflow "Backend CI/CD Pipeline" testé avec un push sur `develop`
- [ ] VM Backend contient uniquement le backend (pas de monitoring)
- [ ] VM Monitoring contient uniquement le monitoring (pas de backend)
- [ ] Prometheus scrape correctement le backend (target UP dans Prometheus UI)

---

## 🎉 Une Fois Tout Vérifié

Vous pouvez maintenant :
- ✅ Pousser du code sur `develop` → déploiement automatique sur la VM Backend
- ✅ Déployer le monitoring manuellement via le workflow "Deploy Monitoring Stack"
- ✅ Vérifier l'état du backend via le workflow "Check Backend Status"

Les déploiements devraient être **plus rapides** et **plus fiables** ! 🚀

