# 🧹 Commandes pour Nettoyer les Doublons

## 🚀 Étapes Rapides

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

### 4. Exécuter le script de nettoyage

Copiez-collez tout le contenu du fichier `nettoyer-doublons.sql` :

```sql
-- Supprimer les rôles en double
DELETE FROM user_roles 
WHERE user_id IN (
    SELECT id FROM profiles WHERE email = 'oumaymasaoudi6@gmail.com'
);

-- Supprimer les utilisateurs en double
DELETE FROM profiles WHERE email = 'oumaymasaoudi6@gmail.com';

-- Recréer proprement
INSERT INTO profiles (id, email, password, full_name, phone, is_active, failed_login_attempts, hotel_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'oumaymasaoudi6@gmail.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'oumayma chouichi',
    NULL,
    true,
    0,
    NULL,
    NOW(),
    NOW()
);

INSERT INTO user_roles (id, user_id, role, hotel_id)
SELECT gen_random_uuid(), p.id, 'SUPERADMIN', NULL
FROM profiles p
WHERE p.email = 'oumaymasaoudi6@gmail.com';

-- Vérifier
SELECT email, full_name FROM profiles WHERE email = 'oumaymasaoudi6@gmail.com';
SELECT ur.role, p.email FROM user_roles ur JOIN profiles p ON ur.user_id = p.id WHERE p.email = 'oumaymasaoudi6@gmail.com';
```

### 5. Quitter PostgreSQL

```sql
\q
```

### 6. Redémarrer le Backend

```powershell
ssh -i github-actions-key ubuntu@13.49.44.219
cd /opt/hotel-ticket-hub-backend-staging
docker compose restart
```

### 7. Tester la Connexion

- http://51.21.196.104/login
- Email : `oumaymasaoudi6@gmail.com`
- Mot de passe : `admin123`

---

## ✅ Résultat Attendu

Après le nettoyage, il ne doit y avoir qu'**un seul** utilisateur et **un seul** rôle SUPERADMIN pour cet email.

