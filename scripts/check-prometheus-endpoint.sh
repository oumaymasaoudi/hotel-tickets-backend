#!/bin/bash

# Script pour diagnostiquer le problème avec l'endpoint Prometheus
# À exécuter sur la VM de staging

echo "=========================================="
echo "Diagnostic de l'endpoint Prometheus"
echo "=========================================="

# 1. Vérifier les logs récents pour les erreurs
echo ""
echo "1. Logs récents (recherche d'erreurs Prometheus)..."
docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -i -E "prometheus|metric|error" | tail -20

# 2. Vérifier que l'endpoint est bien exposé
echo ""
echo "2. Vérification des endpoints Actuator disponibles..."
curl -s http://localhost:8081/actuator 2>/dev/null | head -20 || echo "Endpoint /actuator non accessible"

# 3. Tester l'endpoint metrics (qui devrait fonctionner)
echo ""
echo "3. Test de l'endpoint metrics..."
curl -s http://localhost:8081/actuator/metrics 2>/dev/null | head -10 || echo "Endpoint /metrics non accessible"

# 4. Vérifier la configuration dans le conteneur
echo ""
echo "4. Vérification de la configuration Prometheus..."
docker exec hotel-ticket-hub-backend-staging env | grep -i "PROMETHEUS\|METRIC" || echo "Aucune variable Prometheus trouvée"

# 5. Vérifier les dépendances dans le conteneur
echo ""
echo "5. Vérification de la présence de Micrometer Prometheus..."
docker exec hotel-ticket-hub-backend-staging sh -c "ls /app/app.jar && jar -tf /app/app.jar | grep prometheus | head -5" 2>/dev/null || echo "Impossible de vérifier les dépendances"

# 6. Tester avec plus de détails
echo ""
echo "6. Test détaillé de l'endpoint Prometheus..."
curl -v http://localhost:8081/actuator/prometheus 2>&1 | head -30

