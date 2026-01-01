# 🎯 Étapes suivantes - Installation PostgreSQL

## ✅ Étape 1 : Installer PostgreSQL sur la VM data-staging

Connectez-vous à la VM data-staging et installez PostgreSQL :

```powershell
# Se connecter à la VM data-staging
ssh -i github-actions-key ubuntu@13.61.27.43

# Une fois connecté, exécutez :
sudo apt update
sudo apt install -y postgresql postgresql-contrib

# Vérifier que PostgreSQL est démarré
sudo systemctl status postgresql
```

Vous devriez voir : `Active: active (running)`

---

## ✅ Étape 2 : Créer la base de données

Toujours sur la VM data-staging, exécutez :

```bash
# Se connecter à PostgreSQL
sudo -u postgres psql
```

Dans le shell PostgreSQL (`postgres=#`), copiez-collez ces commandes :

```sql
-- Créer la base de données
CREATE DATABASE hotel_ticket_hub;

-- Modifier l'utilisateur postgres avec un mot de passe
ALTER USER postgres WITH PASSWORD 'votre_mot_de_passe_fort_ici';

-- Donner tous les privilèges
GRANT ALL PRIVILEGES ON DATABASE hotel_ticket_hub TO postgres;

-- Se connecter à la base de données
\c hotel_ticket_hub

-- Donner les privilèges sur le schéma public
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO postgres;

-- Vérifier
\l
\q
```

**⚠️ Important** : Notez le mot de passe que vous avez choisi, vous en aurez besoin pour le fichier `.env` !

---

## ✅ Étape 3 : Configurer PostgreSQL pour accepter les connexions distantes

Toujours sur la VM data-staging :

```bash
# Trouver la version de PostgreSQL
ls /etc/postgresql/

# Éditer postgresql.conf (remplacez XX par votre version)
sudo nano /etc/postgresql/*/main/postgresql.conf
```

Cherchez et modifiez :
```
#listen_addresses = 'localhost'
```
En :
```
listen_addresses = '*'
```

Sauvegarder : `Ctrl+O`, `Enter`, `Ctrl+X`

```bash
# Éditer pg_hba.conf
sudo nano /etc/postgresql/*/main/pg_hba.conf
```

Ajoutez à la fin :
```
host    all             all             0.0.0.0/0               md5
```

Sauvegarder : `Ctrl+O`, `Enter`, `Ctrl+X`

```bash
# Redémarrer PostgreSQL
sudo systemctl restart postgresql

# Vérifier qu'il écoute sur toutes les interfaces
sudo netstat -tulnp | grep 5432
```

Vous devriez voir : `0.0.0.0:5432`

---

## ✅ Étape 4 : Configurer les Security Groups AWS

Dans la console AWS EC2 :

1. Allez dans **EC2** → **Security Groups**
2. Trouvez le Security Group de la VM `data-staging` (13.61.27.43)
3. **Edit inbound rules** → **Add rule** :
   - **Type** : `PostgreSQL`
   - **Port** : `5432`
   - **Source** : `13.49.44.219/32` (IP de la VM backend)
   - **Description** : `Allow PostgreSQL from backend VM`
4. **Save rules**

---

## ✅ Étape 5 : Tester la connexion depuis la VM backend

```powershell
# Se connecter à la VM backend
ssh -i github-actions-key ubuntu@13.49.44.219

# Installer le client PostgreSQL
sudo apt update
sudo apt install -y postgresql-client

# Tester la connexion (entrez le mot de passe que vous avez défini)
psql -h 13.61.27.43 -U postgres -d hotel_ticket_hub
```

Si ça fonctionne, vous verrez : `hotel_ticket_hub=>`

Tapez `\q` pour quitter.

---

## ✅ Étape 6 : Créer le fichier .env sur la VM backend

```powershell
# Sur la VM backend
ssh -i github-actions-key ubuntu@13.49.44.219

# Aller dans le répertoire
cd /opt/hotel-ticket-hub-backend-staging

# Créer le fichier .env
nano .env
```

Collez ce contenu (remplacez `votre_mot_de_passe_fort_ici` par le vrai mot de passe) :

```bash
SPRING_PROFILES_ACTIVE=staging
SERVER_PORT=8081
SPRING_DATASOURCE_URL=jdbc:postgresql://13.61.27.43:5432/hotel_ticket_hub
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=votre_mot_de_passe_fort_ici
JWT_SECRET=YourSuperSecretJWTKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=http://51.21.196.104,http://localhost:5173
APP_FRONTEND_URL=http://51.21.196.104
```

Sauvegarder : `Ctrl+O`, `Enter`, `Ctrl+X`

```bash
# Protéger le fichier
chmod 600 .env
```

---

## 🎉 C'est terminé !

Une fois tout configuré, le prochain déploiement Docker créera automatiquement les tables dans la base de données.

Pour tester :
```bash
# Voir les logs du conteneur
docker compose logs -f
```

---

## 📝 Résumé des commandes PowerShell

```powershell
# 1. Se connecter à la VM data-staging
ssh -i github-actions-key ubuntu@13.61.27.43

# 2. Installer PostgreSQL (sur la VM data-staging)
sudo apt update && sudo apt install -y postgresql postgresql-contrib

# 3. Créer la base de données (sur la VM data-staging)
sudo -u postgres psql
# Puis exécuter les commandes SQL du guide

# 4. Configurer PostgreSQL (sur la VM data-staging)
sudo nano /etc/postgresql/*/main/postgresql.conf
sudo nano /etc/postgresql/*/main/pg_hba.conf
sudo systemctl restart postgresql

# 5. Tester depuis la VM backend
ssh -i github-actions-key ubuntu@13.49.44.219
sudo apt install -y postgresql-client
psql -h 13.61.27.43 -U postgres -d hotel_ticket_hub

# 6. Créer le fichier .env (sur la VM backend)
cd /opt/hotel-ticket-hub-backend-staging
nano .env
# Coller le contenu
chmod 600 .env
```

