#!/bin/bash
# Script complet pour configurer la VM et créer le SuperAdmin
# À exécuter sur la VM Database (13.48.83.147)

set -e

DB_HOST="${DB_HOST:-localhost}"
DB_NAME="${DB_NAME:-hotel_ticket_hub}"
DB_USER="${DB_USER:-postgres}"

echo "=========================================="
echo "Configuration complète de la VM"
echo "=========================================="
echo ""

# Étape 1 : Corriger la base de données
echo "📊 Étape 1 : Correction de la base de données"
echo "---------------------------------------------------"

sudo -u postgres psql -d "$DB_NAME" <<EOF
-- Corriger les plans BASIC
UPDATE plans SET name = 'STARTER' WHERE name = 'BASIC' OR name::text = 'BASIC';

-- Créer les plans par défaut
INSERT INTO plans (id, name, base_cost, ticket_quota, excess_ticket_cost, max_technicians, sla_hours, created_at)
SELECT gen_random_uuid(), 'STARTER', 49.99, 50, 2.50, 2, 24, NOW()
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE name = 'STARTER');

INSERT INTO plans (id, name, base_cost, ticket_quota, excess_ticket_cost, max_technicians, sla_hours, created_at)
SELECT gen_random_uuid(), 'PRO', 99.99, 150, 2.00, 5, 12, NOW()
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE name = 'PRO');

INSERT INTO plans (id, name, base_cost, ticket_quota, excess_ticket_cost, max_technicians, sla_hours, created_at)
SELECT gen_random_uuid(), 'ENTERPRISE', 199.99, 500, 1.50, 15, 6, NOW()
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE name = 'ENTERPRISE');

-- Assigner STARTER aux hôtels sans plan
UPDATE hotels SET plan_id = (SELECT id FROM plans WHERE name = 'STARTER' LIMIT 1) WHERE plan_id IS NULL;
EOF

echo "✅ Base de données corrigée"
echo ""

# Étape 2 : Créer le SuperAdmin
echo "👤 Étape 2 : Création du SuperAdmin"
echo "---------------------------------------------------"

# Générer le hash BCrypt pour "admin123" via l'API ou utiliser un hash connu
# Hash BCrypt pour "admin123": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

sudo -u postgres psql -d "$DB_NAME" <<'EOF'
DO $$
DECLARE
    user_id UUID;
    role_id UUID;
    bcrypt_hash TEXT := '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
BEGIN
    -- Vérifier si l'utilisateur existe
    SELECT id INTO user_id FROM profiles WHERE email = 'oumaymasaoudi6@gmail.com';
    
    IF user_id IS NULL THEN
        -- Créer l'utilisateur
        INSERT INTO profiles (id, email, password, full_name, is_active, failed_login_attempts, created_at, updated_at)
        VALUES (
            gen_random_uuid(),
            'oumaymasaoudi6@gmail.com',
            bcrypt_hash,
            'Super Admin',
            true,
            0,
            NOW(),
            NOW()
        )
        RETURNING id INTO user_id;
        
        RAISE NOTICE 'Utilisateur créé avec ID: %', user_id;
    ELSE
        -- Mettre à jour le mot de passe
        UPDATE profiles 
        SET password = bcrypt_hash,
            is_active = true,
            failed_login_attempts = 0,
            updated_at = NOW()
        WHERE id = user_id;
        
        RAISE NOTICE 'Mot de passe mis à jour pour l''utilisateur existant: %', user_id;
    END IF;
    
    -- Supprimer les anciens rôles pour cet utilisateur
    DELETE FROM user_roles WHERE user_id = user_id;
    
    -- Créer le rôle SUPERADMIN
    INSERT INTO user_roles (id, user_id, role, created_at, updated_at)
    VALUES (
        gen_random_uuid(),
        user_id,
        'SUPERADMIN',
        NOW(),
        NOW()
    );
    
    RAISE NOTICE 'Rôle SUPERADMIN créé pour l''utilisateur: %', user_id;
    RAISE NOTICE 'SuperAdmin créé/mis à jour avec succès!';
END $$;

-- Vérification
SELECT 
    p.id,
    p.email,
    p.full_name,
    p.is_active,
    ur.role
FROM profiles p
LEFT JOIN user_roles ur ON p.id = ur.user_id
WHERE p.email = 'oumaymasaoudi6@gmail.com';
EOF

echo "✅ SuperAdmin créé"
echo ""

# Étape 3 : Vérifier les données
echo "🔍 Étape 3 : Vérification des données"
echo "---------------------------------------------------"

sudo -u postgres psql -d "$DB_NAME" <<EOF
-- Vérifier les plans
SELECT 'Plans disponibles:' as info;
SELECT id, name, base_cost FROM plans ORDER BY name;

-- Vérifier les hôtels
SELECT 'Hôtels disponibles:' as info;
SELECT h.id, h.name, p.name as plan_name FROM hotels h LEFT JOIN plans p ON h.plan_id = p.id ORDER BY h.name;

-- Vérifier les catégories
SELECT 'Catégories disponibles:' as info;
SELECT id, name, icon, color FROM categories ORDER BY name;

-- Vérifier le SuperAdmin
SELECT 'SuperAdmin:' as info;
SELECT p.email, p.full_name, ur.role FROM profiles p 
LEFT JOIN user_roles ur ON p.id = ur.user_id 
WHERE p.email = 'oumaymasaoudi6@gmail.com';
EOF

echo ""
echo "=========================================="
echo "✅ Configuration terminée"
echo "=========================================="
echo ""
echo "SuperAdmin créé:"
echo "  Email: oumaymasaoudi6@gmail.com"
echo "  Password: admin123"
echo ""
echo "Prochaines étapes:"
echo "1. Redémarrer le backend sur la VM Backend (13.63.15.86)"
echo "2. Tester la connexion: curl http://13.63.15.86:8081/api/auth/login -X POST -H 'Content-Type: application/json' -d '{\"email\":\"oumaymasaoudi6@gmail.com\",\"password\":\"admin123\"}'"
