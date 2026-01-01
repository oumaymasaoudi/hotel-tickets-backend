# 🗄️ Configuration de la Base de Données PostgreSQL

## 📋 Vue d'ensemble

Vous avez 3 options pour la base de données :

1. **Option A** : Base de données sur la VM `data-staging` (13.61.27.43) - **Recommandé**
2. **Option B** : Base de données locale sur la même VM backend (13.49.44.219)
3. **Option C** : Base de données externe (RDS AWS, etc.)

---

## 🎯 Option A : Base de données sur VM séparée (Recommandé)

### Étape 1 : Installer PostgreSQL sur la VM data-staging

```bash
# Se connecter à la VM data-staging
ssh -i github-actions-key ubuntu@13.61.27.43

# Mettre à jour le système
sudo apt update

# Installer PostgreSQL
sudo apt install -y postgresql postgresql-contrib

# Vérifier l'installation
sudo systemctl status postgresql
```

### Étape 2 : Créer la base de données et l'utilisateur

```bash
# Se connecter à PostgreSQL en tant que superutilisateur
sudo -u postgres psql

# Dans le shell PostgreSQL, exécutez :
```

```sql
-- Créer la base de données
CREATE DATABASE hotel_ticket_hub;

-- Créer un utilisateur (remplacez 'votre_mot_de_passe' par un mot de passe fort)
CREATE USER postgres WITH PASSWORD 'votre_mot_de_passe';

-- Donner tous les privilèges sur la base de données
GRANT ALL PRIVILEGES ON DATABASE hotel_ticket_hub TO postgres;

-- Pour PostgreSQL 15+, donner aussi les privilèges sur le schéma public
\c hotel_ticket_hub
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO postgres;

-- Quitter
\q
```

### Étape 3 : Configurer PostgreSQL pour accepter les connexions distantes

```bash
# Éditer le fichier de configuration PostgreSQL
sudo nano /etc/postgresql/*/main/postgresql.conf

# Trouver la ligne et modifier :
# listen_addresses = 'localhost'
# En :
listen_addresses = '*'
```

```bash
# Éditer le fichier pg_hba.conf pour autoriser les connexions
sudo nano /etc/postgresql/*/main/pg_hba.conf

# Ajouter à la fin du fichier :
host    all             all             0.0.0.0/0               md5
```

```bash
# Redémarrer PostgreSQL
sudo systemctl restart postgresql

# Vérifier que PostgreSQL écoute sur toutes les interfaces
sudo netstat -tulnp | grep 5432
# Vous devriez voir : 0.0.0.0:5432
```

### Étape 4 : Configurer les Security Groups AWS

Dans la console AWS EC2 :

1. Allez dans **Security Groups**
2. Sélectionnez le Security Group de la VM `data-staging` (13.61.27.43)
3. **Inbound Rules** → **Edit inbound rules** → **Add rule** :
   - **Type** : PostgreSQL
   - **Port** : 5432
   - **Source** : IP de la VM backend (13.49.44.219/32) ou le Security Group de la VM backend
   - **Description** : Allow PostgreSQL from backend VM
4. **Save rules**

### Étape 5 : Tester la connexion depuis la VM backend

```bash
# Depuis la VM backend (13.49.44.219)
ssh -i github-actions-key ubuntu@13.49.44.219

# Installer le client PostgreSQL (si pas déjà installé)
sudo apt update
sudo apt install -y postgresql-client

# Tester la connexion
psql -h 13.61.27.43 -U postgres -d hotel_ticket_hub

# Entrer le mot de passe quand demandé
# Si ça fonctionne, vous verrez : hotel_ticket_hub=>
# Tapez \q pour quitter
```

### Étape 6 : Configurer le fichier .env sur la VM backend

```bash
# Sur la VM backend (13.49.44.219)
cd /opt/hotel-ticket-hub-backend-staging

# Éditer le fichier .env
nano .env
```

Collez ce contenu (adaptez le mot de passe) :

```bash
# Profil Spring Boot
SPRING_PROFILES_ACTIVE=staging

# Port du serveur
SERVER_PORT=8081

# Base de données PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://13.61.27.43:5432/hotel_ticket_hub
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=votre_mot_de_passe_ici

# JWT
JWT_SECRET=YourSuperSecretJWTKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm
JWT_EXPIRATION=86400000

# CORS
CORS_ALLOWED_ORIGINS=http://51.21.196.104,http://localhost:5173

# Frontend URL
APP_FRONTEND_URL=http://51.21.196.104
```

Sauvegarder (`Ctrl+O`, `Enter`, `Ctrl+X`) et protéger le fichier :

```bash
chmod 600 .env
```

---

## 🏠 Option B : Base de données locale sur la VM backend

### Étape 1 : Installer PostgreSQL sur la VM backend

```bash
# Se connecter à la VM backend
ssh -i github-actions-key ubuntu@13.49.44.219

# Installer PostgreSQL
sudo apt update
sudo apt install -y postgresql postgresql-contrib

# Vérifier l'installation
sudo systemctl status postgresql
```

### Étape 2 : Créer la base de données

```bash
# Se connecter à PostgreSQL
sudo -u postgres psql
```

```sql
-- Créer la base de données
CREATE DATABASE hotel_ticket_hub;

-- Créer/modifier l'utilisateur
ALTER USER postgres WITH PASSWORD 'votre_mot_de_passe';

-- Quitter
\q
```

### Étape 3 : Configurer le fichier .env

```bash
cd /opt/hotel-ticket-hub-backend-staging
nano .env
```

```bash
SPRING_PROFILES_ACTIVE=staging
SERVER_PORT=8081
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/hotel_ticket_hub
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=votre_mot_de_passe
JWT_SECRET=YourSuperSecretJWTKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=http://51.21.196.104,http://localhost:5173
APP_FRONTEND_URL=http://51.21.196.104
```

```bash
chmod 600 .env
```

---

## ☁️ Option C : Base de données externe (RDS AWS)

Si vous utilisez Amazon RDS :

1. Créez une instance RDS PostgreSQL dans AWS
2. Notez l'**endpoint** (ex: `mydb.xxxxx.us-east-1.rds.amazonaws.com`)
3. Configurez le Security Group pour autoriser la connexion depuis la VM backend
4. Configurez le fichier `.env` avec l'endpoint RDS :

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://mydb.xxxxx.us-east-1.rds.amazonaws.com:5432/hotel_ticket_hub
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=votre_mot_de_passe_rds
```

---

## ✅ Vérifier la configuration

### Test 1 : Vérifier que PostgreSQL est accessible

```bash
# Depuis la VM backend
psql -h 13.61.27.43 -U postgres -d hotel_ticket_hub
# Ou si local :
psql -U postgres -d hotel_ticket_hub
```

### Test 2 : Vérifier le fichier .env

```bash
cd /opt/hotel-ticket-hub-backend-staging
cat .env
# Vérifiez que toutes les variables sont présentes
```

### Test 3 : Tester avec Docker Compose

```bash
cd /opt/hotel-ticket-hub-backend-staging

# Vérifier la configuration
docker compose config

# Démarrer le conteneur
export DOCKER_IMAGE=ghcr.io/oumaymasaoudi/hotel-tickets-backend/backend:develop
docker compose --env-file .env up -d

# Voir les logs
docker compose logs -f

# Si vous voyez des erreurs de connexion à la base de données, vérifiez :
# - Le mot de passe dans .env
# - L'URL de la base de données
# - Les Security Groups AWS
# - Que PostgreSQL écoute bien sur le bon port
```

---

## 🔧 Dépannage

### Erreur : "Connection refused"

```bash
# Vérifier que PostgreSQL est démarré
sudo systemctl status postgresql

# Vérifier qu'il écoute sur le bon port
sudo netstat -tulnp | grep 5432

# Vérifier les Security Groups AWS
```

### Erreur : "Authentication failed"

```bash
# Vérifier le mot de passe dans .env
cat /opt/hotel-ticket-hub-backend-staging/.env | grep PASSWORD

# Réinitialiser le mot de passe PostgreSQL
sudo -u postgres psql
ALTER USER postgres WITH PASSWORD 'nouveau_mot_de_passe';
\q
```

### Erreur : "Database does not exist"

```bash
# Créer la base de données
sudo -u postgres psql
CREATE DATABASE hotel_ticket_hub;
\q
```

### Erreur : "Permission denied"

```bash
# Donner les privilèges
sudo -u postgres psql -d hotel_ticket_hub
GRANT ALL PRIVILEGES ON DATABASE hotel_ticket_hub TO postgres;
\c hotel_ticket_hub
GRANT ALL ON SCHEMA public TO postgres;
\q
```

---

## 📝 Commandes utiles PostgreSQL

```bash
# Se connecter à PostgreSQL
sudo -u postgres psql

# Lister les bases de données
\l

# Se connecter à une base de données
\c hotel_ticket_hub

# Lister les tables
\dt

# Voir la structure d'une table
\d nom_table

# Quitter
\q

# Sauvegarder la base de données
sudo -u postgres pg_dump hotel_ticket_hub > backup.sql

# Restaurer la base de données
sudo -u postgres psql hotel_ticket_hub < backup.sql
```

---

## 🔒 Sécurité

### Bonnes pratiques

1. **Mot de passe fort** : Utilisez un mot de passe complexe pour PostgreSQL
2. **Security Groups** : Limitez l'accès au port 5432 uniquement depuis la VM backend
3. **Fichier .env** : Protégez-le avec `chmod 600`
4. **Ne jamais commiter** : Le fichier `.env` ne doit JAMAIS être dans Git

### Changer le mot de passe

```bash
# Sur la VM PostgreSQL
sudo -u postgres psql
ALTER USER postgres WITH PASSWORD 'nouveau_mot_de_passe_fort';
\q

# Mettre à jour le fichier .env sur la VM backend
nano /opt/hotel-ticket-hub-backend-staging/.env
# Modifier SPRING_DATASOURCE_PASSWORD
```

---

## 🎯 Résumé des étapes

1. ✅ Installer PostgreSQL sur la VM de base de données
2. ✅ Créer la base de données `hotel_ticket_hub`
3. ✅ Créer l'utilisateur et donner les privilèges
4. ✅ Configurer PostgreSQL pour accepter les connexions distantes
5. ✅ Configurer les Security Groups AWS
6. ✅ Tester la connexion depuis la VM backend
7. ✅ Créer le fichier `.env` sur la VM backend avec les bonnes valeurs
8. ✅ Tester le déploiement Docker

Une fois tout configuré, Spring Boot créera automatiquement les tables au premier démarrage grâce à `spring.jpa.hibernate.ddl-auto=update` dans `application.properties`.

