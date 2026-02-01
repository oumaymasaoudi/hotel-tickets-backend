#!/bin/bash

# Script pour tester la connexion DB et redémarrer le backend
# À exécuter sur la VM Backend (13.63.15.86)

set -e

DB_HOST="13.48.83.147"
DB_PORT="5432"

echo "=========================================="
echo "Test de connexion DB et redémarrage Backend"
echo "=========================================="
echo ""

# 1. Vérifier la connexion à la base de données
echo "1. Test de connexion à PostgreSQL..."
if nc -zv -w 5 $DB_HOST $DB_PORT 2>&1 | grep -q "succeeded"; then
    echo "   ✓ Connexion à PostgreSQL réussie"
else
    echo "   ✗ Connexion à PostgreSQL échouée"
    echo "   Vérifiez le Security Group AWS (port 5432)"
    exit 1
fi
echo ""

# 2. Vérifier que le conteneur backend existe
echo "2. Vérification du conteneur backend..."
if docker ps -a | grep -q hotel-ticket-hub-backend-staging; then
    echo "   ✓ Conteneur backend trouvé"
else
    echo "   ✗ Conteneur backend non trouvé"
    echo "   Vérifiez que docker-compose.yml est dans /opt/hotel-ticket-hub-backend-staging"
    exit 1
fi
echo ""

# 3. Redémarrer le backend
echo "3. Redémarrage du backend..."
cd /opt/hotel-ticket-hub-backend-staging || exit 1
docker compose restart backend
echo "   ✓ Backend redémarré"
echo ""

# 4. Attendre le démarrage
echo "4. Attente du démarrage de l'application (30 secondes)..."
sleep 30
echo ""

# 5. Vérifier les logs
echo "5. Vérification des logs..."
if docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -qi "started.*application"; then
    echo "   ✓ L'application Spring Boot a démarré"
    docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -i "started.*application" | tail -1
else
    echo "   ⚠ L'application n'a pas encore démarré ou erreur détectée"
    echo ""
    echo "   Dernières erreurs dans les logs:"
    docker logs hotel-ticket-hub-backend-staging --tail 30 2>&1 | grep -iE "error|exception|failed" | tail -10
fi
echo ""

# 6. Tester l'endpoint health
echo "6. Test de l'endpoint /actuator/health..."
if curl -s --max-time 5 http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo "   ✓ L'endpoint /actuator/health répond"
    curl -s http://localhost:8081/actuator/health
else
    echo "   ✗ L'endpoint /actuator/health ne répond pas"
    echo "   L'application est peut-être encore en train de démarrer"
    echo "   Attendez encore 30 secondes et réessayez"
fi
echo ""

# 7. Tester l'endpoint Prometheus
echo "7. Test de l'endpoint /actuator/prometheus..."
if curl -s --max-time 5 http://localhost:8081/actuator/prometheus | head -1 | grep -q "# HELP"; then
    echo "   ✓ L'endpoint /actuator/prometheus répond"
    echo "   ✓ Les métriques sont disponibles"
else
    echo "   ⚠ L'endpoint /actuator/prometheus ne répond pas encore"
    echo "   Attendez que l'application soit complètement démarrée"
fi
echo ""

# 8. Résumé
echo "=========================================="
echo "Résumé"
echo "=========================================="
echo ""
echo "Si l'application a démarré:"
echo "  - Backend: http://13.63.15.86:8081/actuator/health"
echo "  - Prometheus: http://13.63.15.86:8081/actuator/prometheus"
echo ""
echo "Si l'application n'a pas démarré:"
echo "  1. Vérifiez les logs: docker logs hotel-ticket-hub-backend-staging --tail 100"
echo "  2. Vérifiez le Security Group AWS (port 5432)"
echo "  3. Vérifiez que la base de données existe: sudo -u postgres psql -l"
echo ""

