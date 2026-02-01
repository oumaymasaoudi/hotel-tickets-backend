#!/bin/bash

# Script pour vérifier la connexion à la base de données PostgreSQL
# À exécuter sur la VM Backend

set -e

DB_HOST="13.48.83.147"
DB_PORT="5432"
DB_NAME="hotel_ticket_hub"
DB_USER="postgres"
DB_PASSWORD="admin"

echo "=========================================="
echo "Vérification de la connexion à la base de données"
echo "=========================================="
echo ""
echo "Configuration:"
echo "  Host: $DB_HOST"
echo "  Port: $DB_PORT"
echo "  Database: $DB_NAME"
echo "  User: $DB_USER"
echo ""

# 1. Vérifier la connectivité réseau
echo "1. Test de connectivité réseau (ping)..."
if ping -c 2 -W 5 $DB_HOST > /dev/null 2>&1; then
    echo "   ✓ La VM Database répond au ping"
else
    echo "   ⚠ La VM Database ne répond pas au ping"
    echo "   (Cela peut être normal si ICMP est bloqué)"
fi
echo ""

# 2. Vérifier que le port 5432 est accessible
echo "2. Test de connexion au port PostgreSQL (5432)..."
if timeout 5 bash -c "echo > /dev/tcp/$DB_HOST/$DB_PORT" 2>/dev/null; then
    echo "   ✓ Le port 5432 est accessible"
else
    echo "   ✗ Le port 5432 n'est pas accessible"
    echo "   Vérifiez le Security Group AWS"
    echo "   Le Security Group de la VM Database doit autoriser le port 5432 depuis la VM Backend"
    exit 1
fi
echo ""

# 3. Vérifier la connexion PostgreSQL avec psql (si disponible)
echo "3. Test de connexion PostgreSQL..."
if command -v psql > /dev/null 2>&1; then
    export PGPASSWORD=$DB_PASSWORD
    if psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT version();" > /dev/null 2>&1; then
        echo "   ✓ Connexion PostgreSQL réussie"
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "SELECT version();" 2>/dev/null | head -3
    else
        echo "   ✗ Connexion PostgreSQL échouée"
        echo "   Vérifiez:"
        echo "   - Que PostgreSQL est en cours d'exécution sur $DB_HOST"
        echo "   - Que les identifiants sont corrects"
        echo "   - Que la base de données '$DB_NAME' existe"
    fi
    unset PGPASSWORD
else
    echo "   ⚠ psql n'est pas installé, test avec telnet/nc..."
    if command -v nc > /dev/null 2>&1; then
        if nc -zv -w 5 $DB_HOST $DB_PORT 2>&1 | grep -q "succeeded"; then
            echo "   ✓ Le port PostgreSQL est ouvert et accessible"
        else
            echo "   ✗ Le port PostgreSQL n'est pas accessible"
        fi
    else
        echo "   ⚠ nc n'est pas disponible, impossible de tester la connexion"
    fi
fi
echo ""

# 4. Vérifier depuis le conteneur Docker
echo "4. Test de connexion depuis le conteneur backend..."
if docker ps | grep -q hotel-ticket-hub-backend-staging; then
    echo "   Test avec telnet depuis le conteneur..."
    if docker exec hotel-ticket-hub-backend-staging sh -c "timeout 5 bash -c 'echo > /dev/tcp/$DB_HOST/$DB_PORT'" 2>/dev/null; then
        echo "   ✓ Le conteneur peut accéder au port PostgreSQL"
    else
        echo "   ✗ Le conteneur ne peut pas accéder au port PostgreSQL"
        echo "   Vérifiez la configuration réseau Docker"
    fi
else
    echo "   ⚠ Conteneur backend non trouvé"
fi
echo ""

# 5. Vérifier les variables d'environnement dans le conteneur
echo "5. Variables d'environnement de la base de données..."
if docker ps | grep -q hotel-ticket-hub-backend-staging; then
    docker exec hotel-ticket-hub-backend-staging env | grep -E "SPRING_DATASOURCE" || echo "   Aucune variable trouvée"
else
    echo "   ⚠ Conteneur backend non trouvé"
fi
echo ""

# 6. Résumé et recommandations
echo "=========================================="
echo "Résumé et recommandations"
echo "=========================================="
echo ""
echo "Si le port 5432 n'est pas accessible:"
echo "1. AWS Console → EC2 → Security Groups"
echo "2. Trouvez le Security Group de la VM Database ($DB_HOST)"
echo "3. Inbound rules → Add rule"
echo "   - Type: PostgreSQL"
echo "   - Port: 5432"
echo "   - Source: Security Group de la VM Backend (ou IP 13.63.15.86/32)"
echo "   - Description: Allow PostgreSQL from backend VM"
echo ""
echo "Si la connexion échoue avec les identifiants:"
echo "1. Vérifiez que PostgreSQL est démarré sur la VM Database"
echo "2. Vérifiez que la base de données '$DB_NAME' existe"
echo "3. Vérifiez les identifiants dans le fichier .env"
echo ""

