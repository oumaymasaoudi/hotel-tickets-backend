# 👤 Créer un Compte SuperAdmin dans la Base de Données

## 📋 Informations du SuperAdmin

- **Nom complet** : oumayma chouichi
- **Email** : oumaymasaoudi6@gmail.com
- **Mot de passe** : `admin123` (vous pouvez le changer après)
- **Rôle** : SUPERADMIN

---

## 🚀 Méthode 1 : Via Script SQL (Recommandé)

### Étape 1 : Se connecter à la VM Database

```powershell
ssh -i github-actions-key ubuntu@13.61.27.43
```

### Étape 2 : Se connecter à PostgreSQL

```bash
sudo -u postgres psql
```

### Étape 3 : Se connecter à la base de données

```sql
\c hotel_ticket_hub
```

### Étape 4 : Exécuter le script SQL

Copiez-collez tout le contenu du fichier `create-superadmin.sql` :

```sql
-- 1. Créer un Plan de base
INSERT INTO plans (id, name, base_cost, ticket_quota, excess_ticket_cost, max_technicians, sla_hours, created_at)
VALUES (
    gen_random_uuid(),
    'BASIC',
    50.00,
    100,
    2.00,
    5,
    24,
    NOW()
)
ON CONFLICT DO NOTHING;

-- 2. Créer l'utilisateur SuperAdmin
INSERT INTO profiles (
    id,
    email,
    password,
    full_name,
    phone,
    is_active,
    failed_login_attempts,
    hotel_id,
    created_at,
    updated_at
)
VALUES (
    gen_random_uuid(),
    'oumaymasaoudi6@gmail.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- admin123
    'oumayma chouichi',
    NULL,
    true,
    0,
    NULL,
    NOW(),
    NOW()
)
ON CONFLICT (email) DO NOTHING;

-- 3. Créer le rôle SUPERADMIN
INSERT INTO user_roles (id, user_id, role, hotel_id)
SELECT 
    gen_random_uuid(),
    p.id,
    'SUPERADMIN',
    NULL
FROM profiles p
WHERE p.email = 'oumaymasaoudi6@gmail.com'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = p.id AND ur.role = 'SUPERADMIN'
);

-- 4. Créer un hôtel de test
INSERT INTO hotels (
    id,
    name,
    address,
    email,
    phone,
    plan_id,
    is_active,
    created_at,
    updated_at
)
SELECT 
    gen_random_uuid(),
    'Hôtel de Test',
    '123 Rue de Test, Ville Test',
    'test@hotel.com',
    '+33123456789',
    (SELECT id FROM plans WHERE name = 'BASIC' LIMIT 1),
    true,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM hotels WHERE name = 'Hôtel de Test'
);

-- 5. Vérifier
SELECT 'SuperAdmin créé:' as info, email, full_name FROM profiles WHERE email = 'oumaymasaoudi6@gmail.com';
SELECT 'Rôle créé:' as info, ur.role, p.email FROM user_roles ur JOIN profiles p ON ur.user_id = p.id WHERE p.email = 'oumaymasaoudi6@gmail.com';
SELECT 'Hôtel créé:' as info, name, email FROM hotels WHERE name = 'Hôtel de Test';
```

### Étape 5 : Quitter PostgreSQL

```sql
\q
```

---

## 🚀 Méthode 2 : Via l'API Backend (Alternative)

Si le script SQL ne fonctionne pas, vous pouvez utiliser l'endpoint API :

```bash
# Depuis la VM backend ou votre machine locale
curl -X POST http://13.49.44.219:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "oumaymasaoudi6@gmail.com",
    "password": "admin123",
    "fullName": "oumayma chouichi",
    "role": "SUPERADMIN"
  }'
```

Puis modifier le rôle manuellement en base de données.

---

## ✅ Vérification

### Se connecter avec le SuperAdmin

1. Allez sur : http://51.21.196.104/login
2. Email : `oumaymasaoudi6@gmail.com`
3. Mot de passe : `admin123`
4. Vous devriez être redirigé vers le dashboard SuperAdmin

### Vérifier dans la base de données

```sql
-- Se connecter à PostgreSQL
sudo -u postgres psql
\c hotel_ticket_hub

-- Vérifier l'utilisateur
SELECT email, full_name, is_active FROM profiles WHERE email = 'oumaymasaoudi6@gmail.com';

-- Vérifier le rôle
SELECT ur.role, p.email, p.full_name 
FROM user_roles ur 
JOIN profiles p ON ur.user_id = p.id 
WHERE p.email = 'oumaymasaoudi6@gmail.com';

-- Vérifier l'hôtel
SELECT name, email, is_active FROM hotels;
```

---

## 🔐 Changer le Mot de Passe

Après la première connexion, changez le mot de passe depuis l'interface ou via l'API.

---

## ⚠️ Notes Importantes

1. **Mot de passe par défaut** : `admin123` - **Changez-le immédiatement après la première connexion !**
2. **Hash BCrypt** : Le hash dans le script est pour le mot de passe `admin123`
3. **Hôtel de test** : Un hôtel de test est créé pour permettre la création d'autres comptes admin si nécessaire
4. **Conflits** : Le script utilise `ON CONFLICT DO NOTHING` pour éviter les erreurs si l'utilisateur existe déjà

