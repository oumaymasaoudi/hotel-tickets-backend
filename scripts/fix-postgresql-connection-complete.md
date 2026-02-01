# 🔧 Guide complet : Corriger la connexion PostgreSQL

## Problème

```
nc: connect to 13.48.83.147 port 5432 (tcp) failed: Connection refused
```

L'application Spring Boot ne peut pas démarrer car elle ne peut pas se connecter à PostgreSQL.

## Diagnostic

### Étape 1 : Vérifier PostgreSQL sur la VM Database

```bash
ssh ubuntu@13.48.83.147

# Vérifier si PostgreSQL est installé
psql --version

# Vérifier le statut
sudo systemctl status postgresql

# Vérifier que PostgreSQL écoute sur le port 5432
sudo ss -tlnp | grep 5432
# OU
sudo netstat -tlnp | grep 5432
```

### Étape 2 : Vérifier la configuration PostgreSQL

**Si PostgreSQL n'écoute que sur localhost :**

1. **Modifier postgresql.conf :**
   ```bash
   sudo nano /etc/postgresql/*/main/postgresql.conf
   ```
   
   Trouvez la ligne :
   ```conf
   listen_addresses = 'localhost'
   ```
   
   Changez en :
   ```conf
   listen_addresses = '*'
   ```

2. **Modifier pg_hba.conf pour autoriser les connexions :**
   ```bash
   sudo nano /etc/postgresql/*/main/pg_hba.conf
   ```
   
   Ajoutez à la fin :
   ```
   # Allow connections from backend VM
   host    all             all             13.63.15.86/32         md5
   ```

3. **Redémarrer PostgreSQL :**
   ```bash
   sudo systemctl restart postgresql
   ```

### Étape 3 : Vérifier le Security Group AWS

1. **AWS Console** → **EC2** → **Security Groups**
2. **Trouvez le Security Group de la VM Database** (`13.48.83.147`)
3. **Inbound rules** → **Edit inbound rules** → **Add rule**
4. **Configuration :**
   - Type: PostgreSQL
   - Protocol: TCP
   - Port: 5432
   - Source: `13.63.15.86/32` (ou Security Group de la VM Backend)
   - Description: Allow PostgreSQL from backend VM
5. **Save rules**

### Étape 4 : Vérifier que la base de données existe

```bash
ssh ubuntu@13.48.83.147

# Lister les bases de données
sudo -u postgres psql -l

# Si hotel_ticket_hub n'existe pas, la créer
sudo -u postgres psql -c "CREATE DATABASE hotel_ticket_hub;"
```

### Étape 5 : Tester la connexion depuis la VM Backend

```bash
ssh ubuntu@13.63.15.86

# Test de connexion
nc -zv 13.48.83.147 5432

# Si nc n'est pas installé
sudo apt install netcat-openbsd

# Test avec psql (si installé)
psql -h 13.48.83.147 -p 5432 -U postgres -d hotel_ticket_hub
# Mot de passe: admin
```

### Étape 6 : Redémarrer le backend

```bash
ssh ubuntu@13.63.15.86
cd /opt/hotel-ticket-hub-backend-staging

# Redémarrer
docker compose restart backend

# Attendre 30-60 secondes
sleep 30

# Vérifier les logs
docker logs hotel-ticket-hub-backend-staging --tail 50 | grep -iE "started|error|database"
```

**Résultat attendu :**
- Plus d'erreur "Unable to determine Dialect"
- Message "Started Application" dans les logs
- L'endpoint `/actuator/health` répond

## Checklist de vérification

- [ ] PostgreSQL est installé et démarré sur la VM Database
- [ ] PostgreSQL écoute sur le port 5432
- [ ] `listen_addresses = '*'` dans postgresql.conf
- [ ] Règle dans pg_hba.conf pour autoriser 13.63.15.86/32
- [ ] Security Group AWS autorise le port 5432 depuis 13.63.15.86
- [ ] La base de données `hotel_ticket_hub` existe
- [ ] Test de connexion depuis la VM Backend réussit
- [ ] Le backend démarre sans erreur de base de données

## Commandes rapides

### Sur la VM Database
```bash
# Démarrer PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Configurer pour accepter les connexions externes
sudo sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/" /etc/postgresql/*/main/postgresql.conf
echo "host    all             all             13.63.15.86/32         md5" | sudo tee -a /etc/postgresql/*/main/pg_hba.conf
sudo systemctl restart postgresql

# Créer la base de données si nécessaire
sudo -u postgres psql -c "CREATE DATABASE hotel_ticket_hub;" 2>/dev/null || echo "Database already exists"
```

### Sur la VM Backend
```bash
# Tester la connexion
nc -zv 13.48.83.147 5432

# Redémarrer le backend
cd /opt/hotel-ticket-hub-backend-staging
docker compose restart backend
```

## Après correction

Une fois que le backend démarre correctement :
1. ✅ Vérifier que `/actuator/health` répond
2. ✅ Vérifier que `/actuator/prometheus` répond
3. ✅ Prometheus pourra scraper les métriques
4. ✅ Le target `staging-backend` passera à UP dans Prometheus

