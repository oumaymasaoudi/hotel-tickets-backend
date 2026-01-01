# 🗄️ Créer la base de données PostgreSQL - Commandes correctes

## ❌ Erreur commune

Vous avez essayé d'exécuter les commandes SQL directement dans le shell bash. Il faut d'abord se connecter à PostgreSQL.

## ✅ Méthode correcte

### Étape 1 : Se connecter à PostgreSQL

```bash
# Sur la VM data-staging
sudo -u postgres psql
```

Vous devriez voir :
```
psql (18.x)
Type "help" for help.

postgres=#
```

**Note** : Le prompt `postgres=#` indique que vous êtes dans le shell PostgreSQL.

---

### Étape 2 : Exécuter les commandes SQL

Une fois dans PostgreSQL (`postgres=#`), exécutez **une par une** ou copiez-collez tout le bloc :

```sql
CREATE DATABASE hotel_ticket_hub;
```

Vous devriez voir : `CREATE DATABASE`

```sql
ALTER USER postgres WITH PASSWORD 'votre_mot_de_passe_fort';
```

Vous devriez voir : `ALTER ROLE`

```sql
GRANT ALL PRIVILEGES ON DATABASE hotel_ticket_hub TO postgres;
```

Vous devriez voir : `GRANT`

```sql
\c hotel_ticket_hub
```

Vous devriez voir : `You are now connected to database "hotel_ticket_hub" as user "postgres".`

```sql
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO postgres;
```

Vous devriez voir plusieurs `GRANT` et `ALTER DEFAULT PRIVILEGES`.

```sql
\l
```

Cela liste toutes les bases de données. Vous devriez voir `hotel_ticket_hub` dans la liste.

```sql
\q
```

Cela vous fait quitter PostgreSQL et retourne au shell bash.

---

## 📝 Exemple complet de session

```bash
ubuntu@ip-172-31-32-250:~$ sudo -u postgres psql
psql (18.x)
Type "help" for help.

postgres=# CREATE DATABASE hotel_ticket_hub;
CREATE DATABASE
postgres=# ALTER USER postgres WITH PASSWORD 'MonMotDePasse123!';
ALTER ROLE
postgres=# GRANT ALL PRIVILEGES ON DATABASE hotel_ticket_hub TO postgres;
GRANT
postgres=# \c hotel_ticket_hub
You are now connected to database "hotel_ticket_hub" as user "postgres".
hotel_ticket_hub=# GRANT ALL ON SCHEMA public TO postgres;
GRANT
hotel_ticket_hub=# GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT
hotel_ticket_hub=# GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
GRANT
hotel_ticket_hub=# ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES
hotel_ticket_hub=# ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO postgres;
ALTER DEFAULT PRIVILEGES
hotel_ticket_hub=# \l
                                 List of databases
   Name    |  Owner   | Encoding |   Collate   |    Ctype    | ICU Locale | Locale Provider |   Access privileges   
-----------+----------+----------+-------------+-------------+------------+----------------+-----------------------
 hotel_ticket_hub | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 |            | libc           | 
 postgres  | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 |            | libc           | 
 template0 | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 |            | libc           | =c/postgres          +
           |          |          |             |             |            |                | postgres=CTc/postgres
 template1 | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 |            | libc           | =c/postgres          +
           |          |          |             |             |            |                | postgres=CTc/postgres
(4 rows)

hotel_ticket_hub=# \q
ubuntu@ip-172-31-32-250:~$
```

---

## ✅ Vérification

Après avoir créé la base de données, vous pouvez vérifier :

```bash
# Se reconnecter à PostgreSQL
sudo -u postgres psql

# Lister les bases de données
\l

# Se connecter à votre base
\c hotel_ticket_hub

# Voir les tables (il n'y en aura pas encore, c'est normal)
\dt

# Quitter
\q
```

---

## 🎯 Prochaines étapes

Une fois la base de données créée :

1. **Configurer PostgreSQL pour accepter les connexions distantes** (voir `SETUP_DATABASE_VM_STAGING.md`)
2. **Configurer les Security Groups AWS**
3. **Tester la connexion depuis la VM backend**
4. **Créer le fichier .env sur la VM backend**

---

## 💡 Astuce

Si vous voulez exécuter toutes les commandes en une seule fois, vous pouvez créer un fichier SQL :

```bash
# Créer un fichier avec les commandes
cat > /tmp/create_db.sql << 'EOF'
CREATE DATABASE hotel_ticket_hub;
ALTER USER postgres WITH PASSWORD 'votre_mot_de_passe_fort';
GRANT ALL PRIVILEGES ON DATABASE hotel_ticket_hub TO postgres;
\c hotel_ticket_hub
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO postgres;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO postgres;
EOF

# Exécuter le fichier
sudo -u postgres psql -f /tmp/create_db.sql
```

Mais la méthode interactive (`sudo -u postgres psql`) est plus simple pour commencer.

