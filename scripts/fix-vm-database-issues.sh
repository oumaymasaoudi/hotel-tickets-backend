#!/bin/bash
# Script pour corriger les problèmes de base de données sur les VMs
# À exécuter sur la VM Database (13.48.83.147)

set -e

DB_HOST="${DB_HOST:-13.48.83.147}"
DB_NAME="${DB_NAME:-hotel_ticket_hub}"
DB_USER="${DB_USER:-postgres}"

echo "=========================================="
echo "Correction des problèmes de base de données"
echo "=========================================="
echo ""

# Vérifier que PostgreSQL est accessible
echo "1. Vérification de la connexion PostgreSQL..."
if ! sudo -u postgres psql -d "$DB_NAME" -c "SELECT 1;" > /dev/null 2>&1; then
    echo "   ✗ Impossible de se connecter à la base de données"
    echo "   Vérifiez que PostgreSQL est démarré: sudo systemctl status postgresql"
    exit 1
fi
echo "   ✓ Connexion PostgreSQL réussie"
echo ""

# Exécuter les corrections SQL
echo "2. Correction des plans avec nom 'BASIC'..."
sudo -u postgres psql -d "$DB_NAME" <<EOF
-- Corriger les plans avec le nom "BASIC" (remplacer par "STARTER")
UPDATE plans 
SET name = 'STARTER' 
WHERE name = 'BASIC' OR name::text = 'BASIC';
EOF
echo "   ✓ Plans 'BASIC' corrigés"
echo ""

echo "3. Création des plans par défaut si inexistants..."
sudo -u postgres psql -d "$DB_NAME" <<EOF
-- Créer STARTER si n'existe pas
INSERT INTO plans (id, name, base_cost, ticket_quota, excess_ticket_cost, max_technicians, sla_hours, created_at)
SELECT 
    gen_random_uuid(),
    'STARTER',
    49.99,
    50,
    2.50,
    2,
    24,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE name = 'STARTER');

-- Créer PRO si n'existe pas
INSERT INTO plans (id, name, base_cost, ticket_quota, excess_ticket_cost, max_technicians, sla_hours, created_at)
SELECT 
    gen_random_uuid(),
    'PRO',
    99.99,
    150,
    2.00,
    5,
    12,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE name = 'PRO');

-- Créer ENTERPRISE si n'existe pas
INSERT INTO plans (id, name, base_cost, ticket_quota, excess_ticket_cost, max_technicians, sla_hours, created_at)
SELECT 
    gen_random_uuid(),
    'ENTERPRISE',
    199.99,
    500,
    1.50,
    15,
    6,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE name = 'ENTERPRISE');
EOF
echo "   ✓ Plans par défaut créés/vérifiés"
echo ""

echo "4. Assignation d'un plan STARTER aux hôtels sans plan..."
sudo -u postgres psql -d "$DB_NAME" <<EOF
UPDATE hotels 
SET plan_id = (SELECT id FROM plans WHERE name = 'STARTER' LIMIT 1)
WHERE plan_id IS NULL;
EOF
echo "   ✓ Hôtels sans plan corrigés"
echo ""

echo "5. Vérification des données..."
echo ""
echo "Plans disponibles:"
sudo -u postgres psql -d "$DB_NAME" -c "SELECT id, name, base_cost, ticket_quota FROM plans ORDER BY name;"
echo ""

echo "Hôtels avec leurs plans:"
sudo -u postgres psql -d "$DB_NAME" -c "SELECT h.id, h.name, p.name as plan_name FROM hotels h LEFT JOIN plans p ON h.plan_id = p.id ORDER BY h.name;"
echo ""

echo "=========================================="
echo "✓ Corrections terminées avec succès"
echo "=========================================="
echo ""
echo "Prochaines étapes:"
echo "1. Redémarrer le backend sur la VM Backend (13.63.15.86)"
echo "2. Vérifier les logs: docker logs hotel-ticket-hub-backend-staging"
echo "3. Tester l'API: curl http://13.63.15.86:8081/api/hotels/public"
