#!/bin/bash

# Script pour diagnostiquer le problème avec l'endpoint Prometheus sur la VM
# À exécuter sur la VM de staging

echo "=========================================="
echo "Diagnostic complet de l'endpoint Prometheus"
echo "=========================================="

# 1. Vérifier que le conteneur est en cours d'exécution
echo ""
echo "1. État du conteneur..."
docker ps | grep hotel-ticket-hub-backend-staging || echo "ERREUR: Conteneur non trouvé"

# 2. Vérifier les endpoints Actuator disponibles
echo ""
echo "2. Endpoints Actuator disponibles..."
curl -s http://localhost:8081/actuator 2>/dev/null | jq '.' || curl -s http://localhost:8081/actuator 2>/dev/null

# 3. Vérifier la configuration dans le conteneur
echo ""
echo "3. Variables d'environnement liées à Prometheus/Metrics..."
docker exec hotel-ticket-hub-backend-staging env | grep -iE "PROMETHEUS|METRIC|MANAGEMENT" | sort

# 4. Vérifier les logs de démarrage pour voir si Prometheus est initialisé
echo ""
echo "4. Logs de démarrage (recherche Prometheus/Micrometer)..."
docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -iE "prometheus|micrometer|actuator" | tail -30

# 5. Vérifier les dépendances dans le JAR
echo ""
echo "5. Vérification de la présence de Micrometer Prometheus dans le JAR..."
docker exec hotel-ticket-hub-backend-staging sh -c "if command -v jar >/dev/null 2>&1; then jar -tf /app/app.jar 2>/dev/null | grep -i prometheus | head -10; else echo 'jar command not available, trying unzip...'; unzip -l /app/app.jar 2>/dev/null | grep -i prometheus | head -10; fi" || echo "Impossible de vérifier les dépendances"

# 6. Tester l'endpoint metrics (qui devrait fonctionner)
echo ""
echo "6. Test de l'endpoint /actuator/metrics..."
curl -s http://localhost:8081/actuator/metrics 2>/dev/null | head -20 || echo "Endpoint /metrics non accessible"

# 7. Tester l'endpoint prometheus avec détails
echo ""
echo "7. Test détaillé de l'endpoint /actuator/prometheus..."
curl -v http://localhost:8081/actuator/prometheus 2>&1 | head -40

# 8. Vérifier le fichier application.properties dans le conteneur (si accessible)
echo ""
echo "8. Vérification de la configuration application.properties..."
docker exec hotel-ticket-hub-backend-staging sh -c "if [ -f /app/application.properties ]; then cat /app/application.properties | grep -iE 'prometheus|metric|management'; else echo 'Fichier application.properties non trouvé dans /app/'; fi" || echo "Impossible d'accéder à application.properties"

# 9. Vérifier les logs récents pour les erreurs
echo ""
echo "9. Logs récents (recherche d'erreurs)..."
docker logs hotel-ticket-hub-backend-staging 2>&1 | tail -50 | grep -iE "error|exception|failed" | tail -20

echo ""
echo "=========================================="
echo "Diagnostic terminé"
echo "=========================================="

