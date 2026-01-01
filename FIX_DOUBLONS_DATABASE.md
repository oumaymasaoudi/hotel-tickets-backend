# 🔧 Corriger les Doublons dans la Base de Données

## ⚠️ Problème

L'erreur "Query did not return a unique result: 2 results were returned" signifie qu'il y a des **doublons** dans la base de données :
- Soit plusieurs utilisateurs avec le même email
- Soit plusieurs rôles pour le même utilisateur

## ✅ Solution : Nettoyer les Doublons

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

### Étape 4 : Vérifier les Doublons

```sql
-- Vérifier les utilisateurs avec le même email
SELECT email, COUNT(*) as count 
FROM profiles 
WHERE email = 'oumaymasaoudi6@gmail.com'
GROUP BY email 
HAVING COUNT(*) > 1;

-- Vérifier tous les utilisateurs
SELECT email, full_name, created_at 
FROM profiles 
WHERE email = 'oumaymasaoudi6@gmail.com';

-- Vérifier les rôles pour cet utilisateur
SELECT ur.id, ur.role, p.email, p.full_name
FROM user_roles ur
JOIN profiles p ON ur.user_id = p.id
WHERE p.email = 'oumaymasaoudi6@gmail.com';
```

### Étape 5 : Supprimer les Doublons

**Option A : Supprimer les utilisateurs en double (garder le plus récent)**

```sql
-- Voir les IDs des utilisateurs en double
SELECT id, email, full_name, created_at 
FROM profiles 
WHERE email = 'oumaymasaoudi6@gmail.com'
ORDER BY created_at DESC;

-- Supprimer les rôles des anciens utilisateurs (garder seulement le plus récent)
DELETE FROM user_roles 
WHERE user_id IN (
    SELECT id FROM profiles 
    WHERE email = 'oumaymasaoudi6@gmail.com' 
    AND id NOT IN (
        SELECT id FROM profiles 
        WHERE email = 'oumaymasaoudi6@gmail.com' 
        ORDER BY created_at DESC 
        LIMIT 1
    )
);

-- Supprimer les anciens utilisateurs (garder seulement le plus récent)
DELETE FROM profiles 
WHERE email = 'oumaymasaoudi6@gmail.com' 
AND id NOT IN (
    SELECT id FROM profiles 
    WHERE email = 'oumaymasaoudi6@gmail.com' 
    ORDER BY created_at DESC 
    LIMIT 1
);
```

**Option B : Supprimer TOUT et recréer proprement**

```sql
-- Supprimer tous les rôles pour cet email
DELETE FROM user_roles 
WHERE user_id IN (
    SELECT id FROM profiles WHERE email = 'oumaymasaoudi6@gmail.com'
);

-- Supprimer tous les utilisateurs avec cet email
DELETE FROM profiles WHERE email = 'oumaymasaoudi6@gmail.com';
```

Puis recréer avec le script `create-superadmin.sql`.

### Étape 6 : Recréer le SuperAdmin (si Option B)

```sql
-- 1. Créer l'utilisateur SuperAdmin
INSERT INTO profiles (id, email, password, full_name, phone, is_active, failed_login_attempts, hotel_id, created_at, updated_at)
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
);

-- 2. Créer le rôle SUPERADMIN
INSERT INTO user_roles (id, user_id, role, hotel_id)
SELECT 
    gen_random_uuid(),
    p.id,
    'SUPERADMIN',
    NULL
FROM profiles p
WHERE p.email = 'oumaymasaoudi6@gmail.com';
```

### Étape 7 : Vérifier

```sql
-- Vérifier qu'il n'y a qu'un seul utilisateur
SELECT email, full_name, created_at FROM profiles WHERE email = 'oumaymasaoudi6@gmail.com';

-- Vérifier qu'il n'y a qu'un seul rôle
SELECT ur.role, p.email FROM user_roles ur JOIN profiles p ON ur.user_id = p.id WHERE p.email = 'oumaymasaoudi6@gmail.com';
```

### Étape 8 : Quitter PostgreSQL

```sql
\q
```

---

## ✅ Après le Nettoyage

1. Redémarrer le backend (pour vider le cache) :
```bash
# Sur la VM backend
ssh -i github-actions-key ubuntu@13.49.44.219
cd /opt/hotel-ticket-hub-backend-staging
docker compose restart
```

2. Tester la connexion :
- http://51.21.196.104/login
- Email : `oumaymasaoudi6@gmail.com`
- Mot de passe : `admin123`

---

## 🔍 Prévention

Pour éviter les doublons à l'avenir :
- La contrainte `UNIQUE` sur `email` dans la table `profiles` devrait empêcher les doublons
- Si des doublons existent, c'est qu'ils ont été créés avant l'application de la contrainte

