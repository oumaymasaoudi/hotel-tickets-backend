# 🔧 Correction : Problème de connexion à la base de données

## Problème identifié

L'application Spring Boot ne peut pas démarrer car elle ne peut pas se connecter à PostgreSQL :

```
Unable to determine Dialect without JDBC metadata
(please set 'javax.persistence.jdbc.url', 'hibernate.connection.url', or 'hibernate.dialect')
```

**Configuration actuelle :**
- Database Host: `13.48.83.147`
- Database Port: `5432`
- Database Name: `hotel_ticket_hub`
- Database User: `postgres`

## Causes possibles

### 1. Security Group bloque les connexions (PROBABLE)

Le Security Group de la VM Database (`13.48.83.147`) ne permet probablement pas les connexions depuis la VM Backend (`13.63.15.86`).

**Solution :**
1. AWS Console → EC2 → Security Groups
2. Trouvez le Security Group de la VM Database (`13.48.83.147`)
3. Inbound rules → Edit inbound rules → Add rule
4. Configuration :
   - Type: PostgreSQL
   - Port: 5432
   - Source: Security Group de la VM Backend OU IP `13.63.15.86/32`
   - Description: Allow PostgreSQL from backend VM

### 2. PostgreSQL n'est pas démarré

**Vérification sur la VM Database :**
```bash
ssh ubuntu@13.48.83.147
sudo systemctl status postgresql
# OU
docker ps | grep postgres
```

**Si PostgreSQL n'est pas démarré :**
```bash
# Si installé via systemd
sudo systemctl start postgresql

# Si dans Docker
docker start <postgres-container>
```

### 3. Base de données n'existe pas

**Vérification :**
```bash
ssh ubuntu@13.48.83.147
sudo -u postgres psql -c "\l" | grep hotel_ticket_hub
```

**Si la base n'existe pas :**
```bash
sudo -u postgres psql -c "CREATE DATABASE hotel_ticket_hub;"
```

### 4. Identifiants incorrects

**Vérification :**
```bash
ssh ubuntu@13.48.83.147
sudo -u postgres psql -c "\du" | grep postgres
```

**Si les identifiants sont incorrects :**
- Modifiez le fichier `.env` sur la VM Backend
- Ou modifiez le mot de passe PostgreSQL

## Diagnostic étape par étape

### Étape 1 : Vérifier depuis la VM Backend

```bash
ssh ubuntu@13.63.15.86

# Test de connectivité
telnet 13.48.83.147 5432
# OU
nc -zv 13.48.83.147 5432
```

**Si "Connection refused" ou "Connection timed out" :**
→ Problème de Security Group (voir solution 1)

### Étape 2 : Vérifier sur la VM Database

```bash
ssh ubuntu@13.48.83.147

# Vérifier que PostgreSQL écoute
sudo netstat -tlnp | grep 5432
# OU
sudo ss -tlnp | grep 5432

# Vérifier le statut
sudo systemctl status postgresql
```

### Étape 3 : Tester la connexion

```bash
# Depuis la VM Backend
psql -h 13.48.83.147 -p 5432 -U postgres -d hotel_ticket_hub
# Mot de passe: admin
```

## Solution rapide (Security Group)

**Si c'est un problème de Security Group :**

1. **AWS Console** → **EC2** → **Security Groups**
2. **Trouvez le Security Group de la VM Database** (celle avec l'IP `13.48.83.147`)
3. **Inbound rules** → **Edit inbound rules** → **Add rule**
4. **Configuration :**
   ```
   Type: PostgreSQL
   Protocol: TCP
   Port: 5432
   Source: 13.63.15.86/32 (ou Security Group de la VM Backend)
   Description: Allow PostgreSQL from backend VM
   ```
5. **Save rules**

## Vérification après correction

```bash
# Depuis la VM Backend
ssh ubuntu@13.63.15.86

# Test de connexion
nc -zv 13.48.83.147 5432

# Redémarrer le conteneur backend
cd /opt/hotel-ticket-hub-backend-staging
docker compose restart backend

# Vérifier les logs
docker logs hotel-ticket-hub-backend-staging --tail 50 | grep -iE "started|error|database"
```

**Résultat attendu :**
- Plus d'erreur "Unable to determine Dialect"
- Message "Started Application" dans les logs
- L'endpoint `/actuator/health` répond

## Script de diagnostic

Utilisez le script `check-database-connection.sh` pour diagnostiquer automatiquement :

```bash
# Sur la VM Backend
chmod +x check-database-connection.sh
./check-database-connection.sh
```

