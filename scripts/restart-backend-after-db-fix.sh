#!/bin/bash

# Script pour redémarrer le backend après correction de la connexion DB
# À exécuter sur la VM Backend

set -e

echo "=========================================="
echo "Redémarrage du Backend"
echo "=========================================="
echo ""

# 1. Aller dans le répertoire du backend
cd /opt/hotel-ticket-hub-backend-staging || {
    echo "✗ Répertoire /opt/hotel-ticket-hub-backend-staging non trouvé"
    exit 1
}

# 2. Redémarrer le backend
echo "1. Redémarrage du conteneur backend..."
docker compose restart backend
echo "   ✓ Conteneur redémarré"
echo ""

# 3. Attendre le démarrage
echo "2. Attente du démarrage de l'application (45 secondes)..."
echo "   (Spring Boot peut prendre 30-60 secondes pour démarrer)"
for i in {1..9}; do
    sleep 5
    echo "   ... $(($i * 5)) secondes"
done
echo ""

# 4. Vérifier les logs
echo "3. Vérification des logs..."
if docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -qi "started.*application"; then
    echo "   ✓ L'application Spring Boot a démarré"
    docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -i "started.*application" | tail -1
else
    echo "   ⚠ L'application n'a pas encore démarré"
    echo ""
    echo "   Dernières lignes des logs:"
    docker logs hotel-ticket-hub-backend-staging --tail 20
    echo ""
    echo "   Recherche d'erreurs..."
    docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -iE "error|exception|failed" | tail -10 || echo "   Aucune erreur récente"
fi
echo ""

# 5. Tester l'endpoint health
echo "4. Test de l'endpoint /actuator/health..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 http://localhost:8081/actuator/health 2>/dev/null || echo "000")

if [ "$HTTP_CODE" = "200" ]; then
    echo "   ✓ L'endpoint /actuator/health répond (HTTP 200)"
    curl -s http://localhost:8081/actuator/health
    echo ""
else
    echo "   ⚠ L'endpoint /actuator/health ne répond pas (HTTP $HTTP_CODE)"
    echo "   L'application est peut-être encore en train de démarrer"
    echo "   Attendez encore 30 secondes et réessayez:"
    echo "   curl http://localhost:8081/actuator/health"
fi
echo ""

# 6. Tester l'endpoint Prometheus
echo "5. Test de l'endpoint /actuator/prometheus..."
if curl -s --max-time 5 http://localhost:8081/actuator/prometheus | head -1 | grep -q "# HELP"; then
    echo "   ✓ L'endpoint /actuator/prometheus répond"
    echo "   ✓ Les métriques sont disponibles"
    echo ""
    echo "   Premières métriques:"
    curl -s http://localhost:8081/actuator/prometheus | head -5
else
    echo "   ⚠ L'endpoint /actuator/prometheus ne répond pas encore"
    echo "   Attendez que l'application soit complètement démarrée"
fi
echo ""

# 7. Résumé
echo "=========================================="
echo "Résumé"
echo "=========================================="
echo ""
if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Backend démarré avec succès !"
    echo ""
    echo "Endpoints disponibles:"
    echo "  - Health: http://13.63.15.86:8081/actuator/health"
    echo "  - Prometheus: http://13.63.15.86:8081/actuator/prometheus"
    echo ""
    echo "Prochaines étapes:"
    echo "  1. Vérifier dans Prometheus: http://16.170.74.58:9090/targets"
    echo "  2. Le target 'staging-backend' devrait passer à UP dans 15-30 secondes"
else
    echo "⚠ Le backend n'a pas encore démarré complètement"
    echo ""
    echo "Pour vérifier manuellement:"
    echo "  docker logs hotel-ticket-hub-backend-staging --tail 100"
    echo "  curl http://localhost:8081/actuator/health"
fi
echo ""

