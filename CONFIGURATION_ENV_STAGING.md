# 🔧 Configuration des Variables d'Environnement - Staging

## 📋 D'où viennent ces valeurs ?

Ces valeurs viennent de votre fichier de configuration Spring Boot : `src/main/resources/application.properties`

## 🔍 Valeurs à adapter selon votre configuration

### 1. **SPRING_PROFILES_ACTIVE=staging**

Cette valeur active le profil Spring Boot "staging". 
- Si vous avez un fichier `application-staging.properties`, il sera utilisé
- Sinon, les valeurs par défaut de `application.properties` seront utilisées

**Action** : Laissez `staging` tel quel.

---

### 2. **SERVER_PORT=8081**

Dans votre `application.properties`, le port est `8080`. Pour staging, on utilise `8081` pour éviter les conflits.

**Action** : 
- Si vous voulez garder le port 8080, changez en `SERVER_PORT=8080`
- Sinon, laissez `8081` (recommandé pour staging)

---

### 3. **Configuration Base de Données**

Dans votre `application.properties`, vous avez :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hotel_ticket_hub
spring.datasource.username=postgres
spring.datasource.password=postgres
```

**Pour staging, vous devez adapter selon votre VM de base de données :**

#### Option A : Base de données sur la VM `data-staging` (13.61.27.43)

```bash
# Dans le fichier .env sur la VM
DB_URL=jdbc:postgresql://13.61.27.43:5432/hotel_ticket_hub
DB_USERNAME=postgres
DB_PASSWORD=votre_mot_de_passe
```

#### Option B : Base de données locale sur la même VM backend

```bash
DB_URL=jdbc:postgresql://localhost:5432/hotel_ticket_hub
DB_USERNAME=postgres
DB_PASSWORD=votre_mot_de_passe
```

#### Option C : Base de données externe (RDS, etc.)

```bash
DB_URL=jdbc:postgresql://votre-host:5432/hotel_ticket_hub
DB_USERNAME=votre_username
DB_PASSWORD=votre_password
```

**⚠️ Important** : Remplacez `votre_mot_de_passe` par le vrai mot de passe de votre base de données.

---

## 📝 Fichier .env complet pour staging

Voici un exemple complet à adapter :

```bash
sudo tee /opt/hotel-ticket-hub-backend-staging/.env >/dev/null <<'EOF'
# Profil Spring Boot
SPRING_PROFILES_ACTIVE=staging

# Port du serveur
SERVER_PORT=8081

# Base de données PostgreSQL
# Adaptez selon votre configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://13.61.27.43:5432/hotel_ticket_hub
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=votre_mot_de_passe_ici

# JWT (optionnel, peut rester par défaut)
JWT_SECRET=YourSuperSecretJWTKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm
JWT_EXPIRATION=86400000

# CORS (adaptez selon votre frontend staging)
CORS_ALLOWED_ORIGINS=http://51.21.196.104,http://localhost:5173

# Frontend URL
APP_FRONTEND_URL=http://51.21.196.104
EOF
```

---

## 🔄 Comment Spring Boot utilise ces variables

Spring Boot convertit automatiquement les variables d'environnement :

- `SPRING_DATASOURCE_URL` → `spring.datasource.url`
- `SPRING_DATASOURCE_USERNAME` → `spring.datasource.username`
- `SPRING_DATASOURCE_PASSWORD` → `spring.datasource.password`
- `SERVER_PORT` → `server.port`
- `SPRING_PROFILES_ACTIVE` → `spring.profiles.active`

**Format** : `SPRING_DATASOURCE_URL` (avec underscores et majuscules) devient `spring.datasource.url` (avec points et minuscules).

---

## ✅ Étapes pour configurer

### 1. Vérifier votre configuration de base de données

```bash
# Sur la VM data-staging (13.61.27.43)
ssh -i github-actions-key ubuntu@13.61.27.43

# Vérifier si PostgreSQL est installé et accessible
sudo systemctl status postgresql
```

### 2. Créer le fichier .env avec les bonnes valeurs

```bash
# Sur la VM backend-staging (13.49.44.219)
sudo nano /opt/hotel-ticket-hub-backend-staging/.env
```

Collez le contenu adapté selon votre configuration.

### 3. Vérifier les permissions

```bash
sudo chown ubuntu:ubuntu /opt/hotel-ticket-hub-backend-staging/.env
sudo chmod 600 /opt/hotel-ticket-hub-backend-staging/.env
```

---

## 🚨 Sécurité

**⚠️ Ne commitez JAMAIS le fichier `.env` dans Git !**

Le fichier `.env` contient des mots de passe et secrets. Il doit rester uniquement sur le serveur.

---

## 📚 Références

- Votre configuration actuelle : `src/main/resources/application.properties`
- Documentation Spring Boot : https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config

