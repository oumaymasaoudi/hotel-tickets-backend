# 🚀 Commandes pour Créer le SuperAdmin

## 📋 Étapes Rapides

### 1. Se connecter à la VM Database

```powershell
ssh -i github-actions-key ubuntu@13.61.27.43
```

### 2. Se connecter à PostgreSQL

```bash
sudo -u postgres psql
```

### 3. Se connecter à la base de données

```sql
\c hotel_ticket_hub
```

### 4. Copier-coller le script SQL

Copiez tout le contenu du fichier `create-superadmin.sql` et collez-le dans le terminal PostgreSQL.

**OU** exécutez directement ces commandes :

```sql
-- 1. Créer un Plan de base
INSERT INTO plans (id, name, base_cost, ticket_quota, excess_ticket_cost, max_technicians, sla_hours, created_at)
VALUES (gen_random_uuid(), 'BASIC', 50.00, 100, 2.00, 5, 24, NOW())
ON CONFLICT DO NOTHING;

-- 2. Créer l'utilisateur SuperAdmin (mot de passe: admin123)
INSERT INTO profiles (id, email, password, full_name, phone, is_active, failed_login_attempts, hotel_id, created_at, updated_at)
VALUES (gen_random_uuid(), 'oumaymasaoudi6@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'oumayma chouichi', NULL, true, 0, NULL, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- 3. Créer le rôle SUPERADMIN
INSERT INTO user_roles (id, user_id, role, hotel_id)
SELECT gen_random_uuid(), p.id, 'SUPERADMIN', NULL
FROM profiles p
WHERE p.email = 'oumaymasaoudi6@gmail.com'
AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = p.id AND ur.role = 'SUPERADMIN');

-- 4. Créer un hôtel de test
INSERT INTO hotels (id, name, address, email, phone, plan_id, is_active, created_at, updated_at)
SELECT gen_random_uuid(), 'Hôtel de Test', '123 Rue de Test', 'test@hotel.com', '+33123456789', (SELECT id FROM plans WHERE name = 'BASIC' LIMIT 1), true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM hotels WHERE name = 'Hôtel de Test');

-- 5. Vérifier
SELECT email, full_name FROM profiles WHERE email = 'oumaymasaoudi6@gmail.com';
SELECT ur.role, p.email FROM user_roles ur JOIN profiles p ON ur.user_id = p.id WHERE p.email = 'oumaymasaoudi6@gmail.com';
```

### 5. Quitter PostgreSQL

```sql
\q
```

---

## ✅ Vérification

### Se connecter avec le SuperAdmin

1. Allez sur : **http://51.21.196.104/login**
2. **Email** : `oumaymasaoudi6@gmail.com`
3. **Mot de passe** : `admin123`
4. Vous devriez être redirigé vers le dashboard SuperAdmin

---

## 🔐 Important

**Changez le mot de passe après la première connexion !**

