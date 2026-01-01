# 🗄️ Configuration PostgreSQL sur VM data-staging (Option A)

## 📋 Vue d'ensemble

Base de données PostgreSQL sur la VM `data-staging` (13.61.27.43) accessible depuis la VM `backend-staging` (13.49.44.219).

---

## ✅ Étape 1 : Installer PostgreSQL sur la VM data-staging

```bash
# Se connecter à la VM data-staging
cd hotel-ticket-hub-backend
ssh -i github-actions-key ubuntu@13.61.27.43

# Mettre à jour le système
sudo apt update

# Installer PostgreSQL
sudo apt install -y postgresql postgresql-contrib

# Vérifier que PostgreSQL est démarré
sudo systemctl status postgresql
```

Vous devriez voir : `Active: active (running)`

---

## ✅ Étape 2 : Créer la base de données et l'utilisateur

```bash
# Se connecter à PostgreSQL
sudo -u postgres psql
```

Dans le shell PostgreSQL (`postgres=#`), exécutez ces commandes :

```sql
-- Créer la base de données
CREATE DATABASE hotel_ticket_hub;

-- Créer/modifier l'utilisateur postgres avec un mot de passe
ALTER USER postgres WITH PASSWORD 'votre_mot_de_passe_fort_ici';

-- Donner tous les privilèges sur la base de données
GRANT ALL PRIVILEGES ON DATABASE hotel_ticket_hub TO postgres;

-- Se connecter à la base de données
\c hotel_ticket_hub

-- Donner les privilèges sur le schéma public (PostgreSQL 15+)
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO postgres;

-- Vérifier que tout est OK
\l
\q
```

**⚠️ Important** : Notez le mot de passe que vous avez choisi, vous en aurez besoin pour le fichier `.env`.

---

## ✅ Étape 3 : Configurer PostgreSQL pour accepter les connexions distantes

### 3.1 Modifier postgresql.conf

```bash
# Trouver la version de PostgreSQL installée
ls /etc/postgresql/

# Éditer le fichier de configuration (remplacez XX par votre version, ex: 14, 15, etc.)
sudo nano /etc/postgresql/*/main/postgresql.conf
```

Cherchez la ligne :
```
#listen_addresses = 'localhost'
```

Décommentez et modifiez en :
```
listen_addresses = '*'
```

Sauvegarder : `Ctrl+O`, `Enter`, `Ctrl+X`

### 3.2 Modifier pg_hba.conf

```bash
# Éditer le fichier d'authentification
sudo nano /etc/postgresql/*/main/pg_hba.conf
```

Ajoutez à la fin du fichier :
```
host    all             all             0.0.0.0/0               md5
```

Sauvegarder : `Ctrl+O`, `Enter`, `Ctrl+X`

### 3.3 Redémarrer PostgreSQL

```bash
# Redémarrer le service
sudo systemctl restart postgresql

# Vérifier le statut
sudo systemctl status postgresql

# Vérifier que PostgreSQL écoute sur toutes les interfaces
sudo netstat -tulnp | grep 5432
```

Vous devriez voir quelque chose comme :
```
tcp  0  0  0.0.0.0:5432  0.0.0.0:*  LISTEN  ...
```

---

## ✅ Étape 4 : Configurer les Security Groups AWS

### Dans la console AWS EC2 :

1. Allez dans **EC2** → **Security Groups**
2. Trouvez le Security Group de la VM `data-staging` (13.61.27.43)
3. Cliquez sur **Edit inbound rules**
4. Cliquez sur **Add rule** :
   - **Type** : `PostgreSQL`
   - **Port** : `5432`
   - **Source** : 
     - Option 1 : IP de la VM backend `13.49.44.219/32`
     - Option 2 : Le Security Group de la VM backend (plus sécurisé)
   - **Description** : `Allow PostgreSQL from backend VM`
5. Cliquez sur **Save rules**

---

## ✅ Étape 5 : Tester la connexion depuis la VM backend

```bash
# Se connecter à la VM backend
ssh -i github-actions-key ubuntu@13.49.44.219

# Installer le client PostgreSQL (si pas déjà installé)
sudo apt update
sudo apt install -y postgresql-client

# Tester la connexion
psql -h 13.61.27.43 -U postgres -d hotel_ticket_hub
```

Entrez le mot de passe que vous avez défini à l'étape 2.

Si la connexion réussit, vous verrez :
```
hotel_ticket_hub=>
```

Tapez `\q` pour quitter.

**Si ça ne fonctionne pas**, vérifiez :
- Le mot de passe est correct
- Les Security Groups AWS sont bien configurés
- PostgreSQL écoute bien sur `0.0.0.0:5432` (voir étape 3.3)

---

## ✅ Étape 6 : Créer le fichier .env sur la VM backend

```bash
# Sur la VM backend (13.49.44.219)
cd /opt/hotel-ticket-hub-backend-staging

# Créer le fichier .env
nano .env
```

Collez ce contenu (remplacez `votre_mot_de_passe_fort_ici` par le vrai mot de passe) :

```bash
# Profil Spring Boot
SPRING_PROFILES_ACTIVE=staging

# Port du serveur
SERVER_PORT=8081

# Base de données PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://13.61.27.43:5432/hotel_ticket_hub
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=votre_mot_de_passe_fort_ici

# JWT
JWT_SECRET=YourSuperSecretJWTKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm
JWT_EXPIRATION=86400000

# CORS
CORS_ALLOWED_ORIGINS=http://51.21.196.104,http://localhost:5173

# Frontend URL
APP_FRONTEND_URL=http://51.21.196.104
```

Sauvegarder : `Ctrl+O`, `Enter`, `Ctrl+X`

### Protéger le fichier .env

```bash
# Donner les bonnes permissions
chmod 600 .env

# Vérifier les permissions
ls -la .env
# Devrait afficher : -rw------- (seul le propriétaire peut lire/écrire)
```

---

## ✅ Étape 7 : Vérifier la configuration complète

```bash
# Sur la VM backend
cd /opt/hotel-ticket-hub-backend-staging

# Vérifier que le fichier .env existe et contient les bonnes valeurs
cat .env

# Vérifier la configuration Docker Compose
docker compose config

# Si vous voyez des erreurs, vérifiez que toutes les variables sont définies
```

---

## 🧪 Test final : Déployer et vérifier

Une fois tout configuré, le prochain déploiement Docker créera automatiquement les tables dans la base de données.

Pour tester manuellement :

```bash
# Sur la VM backend
cd /opt/hotel-ticket-hub-backend-staging

# Se connecter à GHCR (si pas déjà fait)
echo "VOTRE_GHCR_TOKEN" | docker login ghcr.io -u VOTRE_USERNAME --password-stdin

# Pull l'image
export DOCKER_IMAGE=ghcr.io/oumaymasaoudi/hotel-tickets-backend/backend:develop
docker pull $DOCKER_IMAGE

# Démarrer le conteneur
docker compose --env-file .env up -d

# Voir les logs
docker compose logs -f
```

Si tout fonctionne, vous devriez voir dans les logs :
- Connexion à la base de données réussie
- Tables créées automatiquement par Hibernate
- Application démarrée sur le port 8081

---

## 🔧 Dépannage

### Erreur : "Connection refused"

```bash
# Sur la VM data-staging, vérifier que PostgreSQL écoute bien
sudo netstat -tulnp | grep 5432
# Doit afficher : 0.0.0.0:5432

# Vérifier les Security Groups AWS
# Vérifier que le port 5432 est ouvert depuis la VM backend
```

### Erreur : "Authentication failed"

```bash
# Vérifier le mot de passe dans .env
cat /opt/hotel-ticket-hub-backend-staging/.env | grep PASSWORD

# Réinitialiser le mot de passe sur la VM data-staging
sudo -u postgres psql
ALTER USER postgres WITH PASSWORD 'nouveau_mot_de_passe';
\q

# Mettre à jour le fichier .env sur la VM backend
```

### Erreur : "Database does not exist"

```bash
# Sur la VM data-staging
sudo -u postgres psql
CREATE DATABASE hotel_ticket_hub;
\q
```

### Erreur : "Permission denied"

```bash
# Sur la VM data-staging
sudo -u postgres psql -d hotel_ticket_hub
GRANT ALL PRIVILEGES ON DATABASE hotel_ticket_hub TO postgres;
\c hotel_ticket_hub
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
\q
```

---

## 📝 Commandes utiles

### Sur la VM data-staging

```bash
# Voir le statut de PostgreSQL
sudo systemctl status postgresql

# Redémarrer PostgreSQL
sudo systemctl restart postgresql

# Voir les logs PostgreSQL
sudo tail -f /var/log/postgresql/postgresql-*-main.log

# Se connecter à PostgreSQL
sudo -u postgres psql

# Lister les bases de données
\l

# Se connecter à une base
\c hotel_ticket_hub

# Lister les tables
\dt
```

### Sur la VM backend

```bash
# Tester la connexion
psql -h 13.61.27.43 -U postgres -d hotel_ticket_hub

# Voir les logs du conteneur
docker compose logs -f

# Redémarrer le conteneur
docker compose restart
```

---

## ✅ Checklist finale

- [ ] PostgreSQL installé sur la VM data-staging
- [ ] Base de données `hotel_ticket_hub` créée
- [ ] Utilisateur `postgres` avec mot de passe configuré
- [ ] Privilèges accordés sur la base de données
- [ ] PostgreSQL configuré pour écouter sur toutes les interfaces (`listen_addresses = '*'`)
- [ ] `pg_hba.conf` configuré pour accepter les connexions distantes
- [ ] Security Groups AWS configurés (port 5432 ouvert depuis la VM backend)
- [ ] Connexion testée depuis la VM backend : `psql -h 13.61.27.43 -U postgres -d hotel_ticket_hub`
- [ ] Fichier `.env` créé sur la VM backend avec les bonnes valeurs
- [ ] Fichier `.env` protégé avec `chmod 600`

Une fois tout configuré, le prochain déploiement Docker créera automatiquement les tables dans la base de données ! 🎉

