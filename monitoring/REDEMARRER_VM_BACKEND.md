# 🔄 Redémarrer la VM Backend (Instance Down)

## 🚨 Problème Identifié

**AWS Status Check :** `Instance reachability check failed`

La VM backend (`backend-staging`, IP: `13.49.44.219`) est **DOWN** depuis environ 12 heures. Le système d'exploitation ne répond pas.

**Conséquences :**
- ❌ Backend non accessible
- ❌ Prometheus ne peut pas scraper les métriques
- ❌ Dashboards Grafana vides
- ❌ Frontend ne peut pas communiquer avec le backend

---

## ✅ Solution : Redémarrer la VM Backend

### Étape 1 : Redémarrer depuis AWS Console

1. **Allez dans AWS Console** > **EC2** > **Instances**
2. **Sélectionnez** l'instance `backend-staging` (IP: `13.49.44.219`)
3. **Cliquez sur** le bouton **"Actions"** (en haut à droite)
4. **Sélectionnez** **"Instance State"** > **"Reboot"** (ou **"Start"** si elle est arrêtée)
5. **Confirmez** le redémarrage

### Étape 2 : Attendre le Redémarrage

- **Temps estimé :** 2-5 minutes
- **Vérifiez** l'onglet **"Status checks"** :
  - ✅ System reachability check passed
  - ✅ Instance reachability check passed (devrait passer à vert)

### Étape 3 : Vérifier que le Backend est Démarré

Une fois la VM redémarrée, vérifiez que le backend Docker est démarré :

**Option A : Via GitHub Actions (si vous avez la clé SSH)**

1. Allez dans GitHub > Actions
2. Sélectionnez "Check Backend Status"
3. Cliquez sur "Run workflow"

**Option B : Via SSH (si vous avez la clé)**

```powershell
# Se connecter à la VM backend
ssh -i <votre-clé> ubuntu@13.49.44.219

# Vérifier les conteneurs
docker ps | grep backend

# Si le backend n'est pas démarré, le démarrer
cd /opt/backend
docker-compose up -d

# Vérifier les logs
docker logs hotel-ticket-hub-backend --tail=50
```

**Option C : Vérifier depuis la VM Monitoring**

```powershell
# Se connecter à la VM monitoring
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224

# Tester l'accès au backend (après redémarrage)
timeout 10 curl -v http://13.49.44.219:8081/actuator/health
```

---

## ✅ Étape 4 : Vérifier dans Prometheus

1. **Attendez 2-3 minutes** après le redémarrage
2. **Allez sur** : `http://13.62.53.224:9090/targets`
3. **Vérifiez** le target `backend` :
   - Devrait passer à **UP** (vert) après quelques minutes

---

## ✅ Étape 5 : Vérifier les Dashboards Grafana

1. **Allez sur** : `http://13.62.53.224:3000`
2. **Ouvrez** les dashboards :
   - "JVM (Micrometer)"
   - "Spring Boot 2.1 System Monitor"
3. **Les métriques** devraient maintenant s'afficher

---

## 🔍 Si le Backend ne Démarre pas Automatiquement

Si après le redémarrage de la VM, le backend Docker n'est pas démarré :

### Solution 1 : Démarrer manuellement (via SSH)

```bash
cd /opt/backend
docker-compose up -d
docker ps | grep backend
```

### Solution 2 : Configurer le Démarrage Automatique

Assurez-vous que Docker Compose démarre automatiquement au boot :

```bash
# Créer un service systemd
sudo nano /etc/systemd/system/backend.service
```

Contenu :
```ini
[Unit]
Description=Hotel Ticket Hub Backend
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/backend
ExecStart=/usr/bin/docker compose up -d
ExecStop=/usr/bin/docker compose down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
```

Puis :
```bash
sudo systemctl daemon-reload
sudo systemctl enable backend.service
sudo systemctl start backend.service
```

---

## 📋 Checklist

- [ ] VM backend redémarrée depuis AWS Console
- [ ] Status checks : "2/2 checks passed" (vert)
- [ ] Backend Docker démarré : `docker ps | grep backend`
- [ ] Backend accessible : `curl http://13.49.44.219:8081/actuator/health`
- [ ] Prometheus target `backend` : **UP** (vert)
- [ ] Dashboards Grafana affichent des données

---

## 🎯 Action Immédiate

**1. Redémarrez la VM backend depuis AWS Console :**
   - EC2 > Instances > `backend-staging` > Actions > Instance State > Reboot

**2. Attendez 2-5 minutes**

**3. Vérifiez dans Prometheus :**
   - `http://13.62.53.224:9090/targets`
   - Le target `backend` devrait être **UP**

**4. Vérifiez les dashboards Grafana :**
   - Les métriques devraient s'afficher

---

**C'est la cause racine du problème ! Une fois la VM redémarrée, tout devrait fonctionner.**

