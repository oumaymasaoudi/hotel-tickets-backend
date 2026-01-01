# 🚀 Prochaines Étapes - Backend Staging

## ✅ Étape 1 : Vérifier que le backend fonctionne

### 1.1 Vérifier le statut du conteneur

```bash
# Sur la VM backend (13.49.44.219)
ssh -i github-actions-key ubuntu@13.49.44.219
cd /opt/hotel-ticket-hub-backend-staging

# Vérifier que le conteneur tourne
docker compose ps

# Devrait afficher : Up (running)
```

### 1.2 Vérifier les logs

```bash
# Voir les derniers logs
docker compose logs --tail=50

# Vérifier qu'il n'y a pas d'erreurs
docker compose logs | grep -i "error\|exception" | tail -20
```

### 1.3 Tester l'API localement (sur la VM)

```bash
# Tester l'endpoint de login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'

# Devrait retourner une réponse (même si c'est une erreur d'authentification, c'est bon signe)
```

---

## ✅ Étape 2 : Configurer les Security Groups AWS

Pour accéder au backend depuis l'extérieur, vous devez ouvrir le port 8081 dans AWS.

### 2.1 Accéder à AWS Console

1. Allez sur https://console.aws.amazon.com/ec2/
2. Cliquez sur **Security Groups** dans le menu de gauche
3. Trouvez le Security Group associé à la VM backend (13.49.44.219)

### 2.2 Ajouter la règle Inbound

1. Sélectionnez le Security Group
2. Cliquez sur l'onglet **Inbound rules**
3. Cliquez sur **Edit inbound rules**
4. Cliquez sur **Add rule**
5. Configurez :
   - **Type** : `Custom TCP`
   - **Port range** : `8081`
   - **Source** : `0.0.0.0/0` (ou restreindre à votre IP pour plus de sécurité)
   - **Description** : `Allow API access from anywhere`
6. Cliquez sur **Save rules**

### 2.3 Vérifier la règle

Vous devriez voir une règle :
- **Type** : Custom TCP
- **Port** : 8081
- **Source** : 0.0.0.0/0

---

## ✅ Étape 3 : Tester l'API depuis l'extérieur

### 3.1 Depuis PowerShell (votre machine locale)

```powershell
# Tester l'endpoint de login
curl -Method POST -Uri "http://13.49.44.219:8081/api/auth/login" `
  -ContentType "application/json" `
  -Body '{"email":"test@example.com","password":"test123"}'

# OU avec Invoke-WebRequest
Invoke-WebRequest -Uri "http://13.49.44.219:8081/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"test@example.com","password":"test123"}'
```

### 3.2 Depuis un navigateur

Ouvrez dans votre navigateur :
```
http://13.49.44.219:8081/api/auth/login
```

Vous devriez voir une réponse (probablement une erreur 405 Method Not Allowed, ce qui est normal car GET n'est pas autorisé).

### 3.3 Tester avec un outil (Postman, Insomnia, etc.)

- **URL** : `http://13.49.44.219:8081/api/auth/login`
- **Method** : `POST`
- **Headers** : `Content-Type: application/json`
- **Body** :
```json
{
  "email": "test@example.com",
  "password": "test123"
}
```

---

## ✅ Étape 4 : Vérifier la configuration CORS

Le backend doit autoriser les requêtes depuis le frontend. Vérifiez que `CORS_ALLOWED_ORIGINS` contient l'URL du frontend.

### 4.1 Vérifier le fichier .env

```bash
# Sur la VM backend (13.49.44.219)
cd /opt/hotel-ticket-hub-backend-staging
cat .env | grep CORS

# Devrait contenir :
# CORS_ALLOWED_ORIGINS=http://51.21.196.104,http://localhost:5173
```

### 4.2 Si nécessaire, modifier le .env

```bash
nano .env

# Vérifier/modifier :
CORS_ALLOWED_ORIGINS=http://51.21.196.104,http://localhost:5173

# Sauvegarder : Ctrl+O, Enter, Ctrl+X

# Redémarrer le conteneur
docker compose restart
```

---

## ✅ Étape 5 : Configurer le frontend pour communiquer avec le backend

### 5.1 Vérifier la configuration du frontend

Le frontend doit être configuré pour utiliser l'URL du backend staging.

**Fichier à vérifier** : `hotel-ticket-hub/Dockerfile`

```dockerfile
# Devrait contenir :
ARG VITE_API_BASE_URL=http://13.49.44.219:8081/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
```

### 5.2 Vérifier le workflow GitHub Actions

**Fichier** : `hotel-ticket-hub/.github/workflows/frontend-ci.yml`

Vérifiez que le build utilise la bonne URL :

```yaml
- name: Build Docker image
  run: |
    docker build \
      --build-arg VITE_API_BASE_URL=http://13.49.44.219:8081/api \
      -t ghcr.io/oumaymasaoudi/hotel-tickets-frontend/frontend:develop .
```

---

## ✅ Étape 6 : Déployer le frontend (si pas déjà fait)

### 6.1 Vérifier que le frontend est déployé

```bash
# Depuis votre machine locale
curl http://51.21.196.104

# Devrait retourner du HTML (la page du frontend)
```

### 6.2 Si le frontend n'est pas déployé

Suivez le guide : `hotel-ticket-hub/ETAPES_SUIVANTES_FRONTEND.md`

---

## ✅ Étape 7 : Tester l'application complète

### 7.1 Ouvrir le frontend dans le navigateur

```
http://51.21.196.104
```

### 7.2 Tester la connexion

1. Ouvrez la console du navigateur (F12)
2. Allez sur l'onglet **Network**
3. Essayez de vous connecter ou d'utiliser l'application
4. Vérifiez que les requêtes vers `http://13.49.44.219:8081/api/*` fonctionnent

### 7.3 Vérifier les erreurs CORS

Si vous voyez des erreurs CORS dans la console :
- Vérifiez que `CORS_ALLOWED_ORIGINS` contient `http://51.21.196.104`
- Redémarrez le backend après modification

---

## ✅ Étape 8 : Créer un utilisateur de test (optionnel)

Si vous avez besoin de créer un utilisateur pour tester :

### 8.1 Via l'API (si l'endpoint existe)

```bash
# Depuis la VM backend ou votre machine locale
curl -X POST http://13.49.44.219:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123",
    "name": "Admin User"
  }'
```

### 8.2 Via la base de données (si nécessaire)

```bash
# Sur la VM database (13.61.27.43)
sudo -u postgres psql -d hotel_ticket_hub

# Insérer un utilisateur (adaptez selon votre schéma)
INSERT INTO profiles (id, email, password, name, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'admin@example.com',
  '$2a$10$...', -- Hash bcrypt du mot de passe
  'Admin User',
  NOW(),
  NOW()
);

\q
```

---

## 📋 Checklist finale

- [ ] Backend conteneur tourne (`docker compose ps`)
- [ ] Backend logs sans erreurs critiques
- [ ] Tables créées dans la base de données (14 tables)
- [ ] Security Group AWS configuré (port 8081 ouvert)
- [ ] API accessible depuis l'extérieur (`curl http://13.49.44.219:8081/api/auth/login`)
- [ ] CORS configuré correctement (`CORS_ALLOWED_ORIGINS`)
- [ ] Frontend configuré avec la bonne URL backend
- [ ] Frontend déployé et accessible (`http://51.21.196.104`)
- [ ] Application complète fonctionnelle (frontend + backend)

---

## 🐛 Dépannage

### Le backend ne répond pas depuis l'extérieur

1. Vérifiez les Security Groups AWS (port 8081)
2. Vérifiez que le conteneur tourne : `docker compose ps`
3. Testez depuis la VM : `curl http://localhost:8081/api`

### Erreurs CORS

1. Vérifiez `CORS_ALLOWED_ORIGINS` dans le `.env`
2. Redémarrez le backend : `docker compose restart`
3. Vérifiez les logs : `docker compose logs | grep -i cors`

### Le frontend ne se connecte pas au backend

1. Vérifiez que `VITE_API_BASE_URL` est correcte dans le build
2. Vérifiez que le backend est accessible depuis la VM frontend
3. Vérifiez les logs du frontend : `docker compose logs -f`

---

## 📚 Documentation

- **Backend** : `hotel-ticket-hub-backend/SETUP_DOCKER_STAGING.md`
- **Frontend** : `hotel-ticket-hub/ETAPES_SUIVANTES_FRONTEND.md`
- **Database** : `hotel-ticket-hub-backend/SETUP_DATABASE_VM_STAGING.md`

---

## 🎯 URLs importantes

- **Backend API** : http://13.49.44.219:8081/api
- **Frontend** : http://51.21.196.104
- **Database** : 13.61.27.43:5432

