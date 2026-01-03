# 🔧 Accéder à la VM Backend

## 🚨 Problème

Vous êtes actuellement sur la **VM Monitoring** (`13.62.53.224`), mais le backend est sur la **VM Backend** (`13.63.15.86`).

---

## ✅ Solution : Se Connecter à la VM Backend

### Option 1 : Via AWS EC2 Instance Connect (Recommandé)

1. **AWS Console** → **EC2** → **Instances**
2. Sélectionnez l'instance `backend-staging` (IP : `13.63.15.86`)
3. **Connect** → **EC2 Instance Connect**
4. Cliquez sur **Connect**

Vous serez connecté directement à la VM Backend.

### Option 2 : Via SSH depuis la VM Monitoring

Depuis la VM Monitoring où vous êtes actuellement :

```bash
# Se connecter à la VM Backend
ssh ubuntu@13.63.15.86

# Si ça demande une clé, utilisez EC2 Instance Connect à la place
```

---

## 🔍 Vérifier l'État du Backend

Une fois connecté à la VM Backend (`13.63.15.86`), exécutez :

```bash
# Vérifier les conteneurs Docker
docker ps

# Vérifier si le répertoire existe
ls -la /opt/

# Si le répertoire n'existe pas, le créer
sudo mkdir -p /opt/hotel-ticket-hub-backend-staging
sudo chown ubuntu:ubuntu /opt/hotel-ticket-hub-backend-staging

# Vérifier si docker-compose.yml existe
ls -la /opt/hotel-ticket-hub-backend-staging/
```

---

## 🚀 Si le Backend n'est pas Déployé

### Option A : Déployer via GitHub Actions

1. **GitHub** → Repo `hotel-ticket-hub-backend`
2. **Settings** → **Secrets and variables** → **Actions**
3. Vérifiez que `STAGING_HOST` = `13.63.15.86`
4. **Actions** → **CI/CD Pipeline**
5. **Run workflow** → Sélectionnez la branche `develop`

Le workflow va :
- Build l'image Docker
- La pousser sur GHCR
- Copier `docker-compose.yml` sur la VM
- Démarrer le backend

### Option B : Déployer Manuellement

Sur la VM Backend :

```bash
# Créer le répertoire
sudo mkdir -p /opt/hotel-ticket-hub-backend-staging
cd /opt/hotel-ticket-hub-backend-staging

# Créer docker-compose.yml (copier depuis votre repo local)
# OU utiliser scp depuis votre machine Windows

# Démarrer le backend
docker compose up -d

# Vérifier les logs
docker compose logs -f backend
```

---

## 📋 Checklist

- [ ] **Se connecter à la VM Backend** (`13.63.15.86`) via EC2 Instance Connect
- [ ] **Vérifier** : `docker ps` (le backend doit être dans la liste)
- [ ] **Vérifier** : `ls -la /opt/hotel-ticket-hub-backend-staging/`
- [ ] **Si le répertoire n'existe pas** : Le créer ou déployer via GitHub Actions
- [ ] **Si le backend n'est pas démarré** : `docker compose up -d`
- [ ] **Tester** : `curl http://localhost:8081/actuator/health`

---

## 🎯 Action Immédiate

1. **AWS Console** → **EC2** → **Instances**
2. Sélectionnez `backend-staging` (IP : `13.63.15.86`)
3. **Connect** → **EC2 Instance Connect**
4. Exécutez : `docker ps` pour voir si le backend est démarré

---

**Vous êtes actuellement sur la VM Monitoring. Connectez-vous à la VM Backend pour gérer le backend !** 🎯

