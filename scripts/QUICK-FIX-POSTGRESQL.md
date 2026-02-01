# 🚀 Correction rapide : Connexion PostgreSQL

## Problème
- Backend ne peut pas se connecter à PostgreSQL
- `Connection refused` sur le port 5432
- Application Spring Boot ne démarre pas

## Solution rapide (3 étapes)

### Étape 1 : Se connecter à la VM Database

**Depuis PowerShell (Windows) :**
```powershell
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.48.83.147
```

**Depuis WSL/Linux :**
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.48.83.147
```

### Étape 2 : Vérifier et configurer PostgreSQL

Une fois connecté à la VM Database :

```bash
# 1. Vérifier le statut
sudo systemctl status postgresql

# 2. Si PostgreSQL n'est pas démarré, le démarrer
sudo systemctl start postgresql
sudo systemctl enable postgresql

# 3. Vérifier qu'il écoute sur le port 5432
sudo ss -tlnp | grep 5432

# 4. Si PostgreSQL n'écoute que sur localhost, modifier la configuration
sudo sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/" /etc/postgresql/*/main/postgresql.conf
sudo sed -i "s/listen_addresses = 'localhost'/listen_addresses = '*'/" /etc/postgresql/*/main/postgresql.conf

# 5. Autoriser les connexions depuis la VM Backend
echo "host    all             all             13.63.15.86/32         md5" | sudo tee -a /etc/postgresql/*/main/pg_hba.conf

# 6. Redémarrer PostgreSQL
sudo systemctl restart postgresql

# 7. Vérifier que ça fonctionne
sudo ss -tlnp | grep 5432
```

### Étape 3 : Configurer le Security Group AWS

1. **AWS Console** → **EC2** → **Security Groups**
2. **Trouvez le Security Group de la VM Database** (`13.48.83.147`)
3. **Inbound rules** → **Edit inbound rules** → **Add rule**
4. **Configuration :**
   ```
   Type: PostgreSQL
   Protocol: TCP
   Port: 5432
   Source: 13.63.15.86/32
   Description: Allow PostgreSQL from backend VM
   ```
5. **Save rules**

### Étape 4 : Vérifier la connexion

**Depuis la VM Backend :**
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
nc -zv 13.48.83.147 5432
```

**Résultat attendu :** `Connection to 13.48.83.147 5432 port [tcp/postgresql] succeeded!`

### Étape 5 : Redémarrer le backend

```bash
# Sur la VM Backend
cd /opt/hotel-ticket-hub-backend-staging
docker compose restart backend

# Attendre 30-60 secondes
sleep 30

# Vérifier les logs
docker logs hotel-ticket-hub-backend-staging --tail 50 | grep -iE "started|error"
```

**Résultat attendu :** Message "Started Application" dans les logs

## Commandes complètes (copier-coller)

### Sur la VM Database
```bash
# Se connecter
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.48.83.147

# Démarrer et configurer PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql
sudo sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/" /etc/postgresql/*/main/postgresql.conf 2>/dev/null || sudo sed -i "s/listen_addresses = 'localhost'/listen_addresses = '*'/" /etc/postgresql/*/main/postgresql.conf
echo "host    all             all             13.63.15.86/32         md5" | sudo tee -a /etc/postgresql/*/main/pg_hba.conf
sudo systemctl restart postgresql

# Vérifier
sudo ss -tlnp | grep 5432
```

### Sur la VM Backend
```bash
# Se connecter
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86

# Tester la connexion
nc -zv 13.48.83.147 5432

# Redémarrer le backend
cd /opt/hotel-ticket-hub-backend-staging
docker compose restart backend

# Vérifier les logs (attendre 30 secondes)
sleep 30
docker logs hotel-ticket-hub-backend-staging --tail 50
```

## Vérification finale

Une fois que tout fonctionne :
1. ✅ Backend démarre sans erreur
2. ✅ `/actuator/health` répond
3. ✅ `/actuator/prometheus` répond
4. ✅ Prometheus peut scraper les métriques

