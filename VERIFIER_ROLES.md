# 🔍 Vérifier les Rôles - Problème "2 results were returned"

## ✅ Il n'y a qu'un seul utilisateur

D'après votre résultat, il n'y a qu'un seul utilisateur. Le problème vient probablement des **rôles**.

## 🔍 Commandes pour Vérifier les Rôles

### Dans PostgreSQL (toujours connecté)

```sql
-- Vérifier les rôles pour cet utilisateur
SELECT ur.id, ur.role, p.email, p.full_name, p.id as user_id
FROM user_roles ur
JOIN profiles p ON ur.user_id = p.id
WHERE p.email = 'oumaymasaoudi6@gmail.com';
```

**Si vous voyez 2 rôles ou plus**, c'est le problème !

## ✅ Solution : Supprimer les Rôles en Double

```sql
-- Voir tous les rôles pour cet utilisateur
SELECT ur.id, ur.role, ur.user_id
FROM user_roles ur
JOIN profiles p ON ur.user_id = p.id
WHERE p.email = 'oumaymasaoudi6@gmail.com';

-- Supprimer tous les rôles sauf un (garder le plus récent ou le SUPERADMIN)
DELETE FROM user_roles 
WHERE id IN (
    SELECT ur.id
    FROM user_roles ur
    JOIN profiles p ON ur.user_id = p.id
    WHERE p.email = 'oumaymasaoudi6@gmail.com'
    AND ur.id NOT IN (
        SELECT ur2.id
        FROM user_roles ur2
        JOIN profiles p2 ON ur2.user_id = p2.id
        WHERE p2.email = 'oumaymasaoudi6@gmail.com'
        AND ur2.role = 'SUPERADMIN'
        LIMIT 1
    )
);

-- Si aucun rôle SUPERADMIN n'existe, en créer un
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

-- Vérifier qu'il n'y a qu'un seul rôle maintenant
SELECT ur.role, p.email, p.full_name 
FROM user_roles ur 
JOIN profiles p ON ur.user_id = p.id 
WHERE p.email = 'oumaymasaoudi6@gmail.com';
```

## 🔄 Redémarrer le Backend

Après avoir nettoyé les rôles :

```powershell
ssh -i github-actions-key ubuntu@13.49.44.219
cd /opt/hotel-ticket-hub-backend-staging
docker compose restart
```

## ✅ Tester

- http://51.21.196.104/login
- Email : `oumaymasaoudi6@gmail.com`
- Mot de passe : `admin123`

