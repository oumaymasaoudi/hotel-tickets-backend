#!/bin/bash

# Script pour vérifier PostgreSQL sur la VM Database
# À exécuter sur la VM Database (13.48.83.147)

set -e

echo "=========================================="
echo "Vérification PostgreSQL sur VM Database"
echo "=========================================="
echo ""

# 1. Vérifier si PostgreSQL est installé
echo "1. Vérification de l'installation PostgreSQL..."
if command -v psql > /dev/null 2>&1; then
    PSQL_VERSION=$(psql --version | head -1)
    echo "   ✓ PostgreSQL est installé: $PSQL_VERSION"
else
    echo "   ✗ PostgreSQL n'est pas installé"
    echo "   Installez PostgreSQL avec: sudo apt update && sudo apt install postgresql postgresql-contrib"
    exit 1
fi
echo ""

# 2. Vérifier le statut du service PostgreSQL
echo "2. Statut du service PostgreSQL..."
if systemctl is-active --quiet postgresql || systemctl is-active --quiet postgresql@*; then
    echo "   ✓ PostgreSQL est en cours d'exécution"
    systemctl status postgresql --no-pager -l | head -10
else
    echo "   ✗ PostgreSQL n'est pas en cours d'exécution"
    echo ""
    echo "   Pour démarrer PostgreSQL:"
    echo "   sudo systemctl start postgresql"
    echo "   sudo systemctl enable postgresql"
    exit 1
fi
echo ""

# 3. Vérifier que PostgreSQL écoute sur le port 5432
echo "3. Vérification du port 5432..."
if sudo netstat -tlnp 2>/dev/null | grep -q ":5432" || sudo ss -tlnp 2>/dev/null | grep -q ":5432"; then
    echo "   ✓ PostgreSQL écoute sur le port 5432"
    sudo ss -tlnp 2>/dev/null | grep ":5432" || sudo netstat -tlnp 2>/dev/null | grep ":5432"
else
    echo "   ✗ PostgreSQL n'écoute pas sur le port 5432"
    echo "   Vérifiez la configuration PostgreSQL (postgresql.conf, pg_hba.conf)"
fi
echo ""

# 4. Vérifier la configuration PostgreSQL
echo "4. Configuration PostgreSQL..."
if [ -f /etc/postgresql/*/main/postgresql.conf ]; then
    LISTEN_ADDRESSES=$(sudo grep "^listen_addresses" /etc/postgresql/*/main/postgresql.conf | head -1 || echo "not found")
    echo "   listen_addresses: $LISTEN_ADDRESSES"
    
    if echo "$LISTEN_ADDRESSES" | grep -q "localhost\|127.0.0.1"; then
        echo "   ⚠ PostgreSQL écoute uniquement sur localhost"
        echo "   Pour accepter les connexions externes, modifiez:"
        echo "   sudo nano /etc/postgresql/*/main/postgresql.conf"
        echo "   listen_addresses = '*'"
        echo "   Puis redémarrez: sudo systemctl restart postgresql"
    fi
fi
echo ""

# 5. Vérifier pg_hba.conf (authentification)
echo "5. Configuration d'authentification (pg_hba.conf)..."
if [ -f /etc/postgresql/*/main/pg_hba.conf ]; then
    echo "   Règles d'authentification pour les connexions réseau:"
    sudo grep -E "^host" /etc/postgresql/*/main/pg_hba.conf | head -5 || echo "   Aucune règle 'host' trouvée"
    
    if ! sudo grep -q "^host.*all.*all" /etc/postgresql/*/main/pg_hba.conf; then
        echo "   ⚠ Aucune règle pour accepter les connexions réseau"
        echo "   Ajoutez dans pg_hba.conf:"
        echo "   host    all             all             13.63.15.86/32         md5"
        echo "   Puis redémarrez: sudo systemctl restart postgresql"
    fi
fi
echo ""

# 6. Vérifier que la base de données existe
echo "6. Vérification de la base de données..."
if sudo -u postgres psql -lqt 2>/dev/null | cut -d \| -f 1 | grep -qw hotel_ticket_hub; then
    echo "   ✓ La base de données 'hotel_ticket_hub' existe"
else
    echo "   ✗ La base de données 'hotel_ticket_hub' n'existe pas"
    echo ""
    echo "   Pour créer la base de données:"
    echo "   sudo -u postgres psql -c \"CREATE DATABASE hotel_ticket_hub;\""
fi
echo ""

# 7. Test de connexion local
echo "7. Test de connexion locale..."
if sudo -u postgres psql -d hotel_ticket_hub -c "SELECT version();" > /dev/null 2>&1; then
    echo "   ✓ Connexion locale réussie"
else
    echo "   ✗ Connexion locale échouée"
fi
echo ""

# 8. Résumé
echo "=========================================="
echo "Résumé"
echo "=========================================="
echo ""
echo "Pour accepter les connexions depuis la VM Backend:"
echo "1. Modifier postgresql.conf: listen_addresses = '*'"
echo "2. Modifier pg_hba.conf: ajouter règle pour 13.63.15.86/32"
echo "3. Redémarrer PostgreSQL: sudo systemctl restart postgresql"
echo "4. Vérifier le Security Group AWS (port 5432 ouvert depuis 13.63.15.86)"
echo ""

