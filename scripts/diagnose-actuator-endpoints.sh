#!/bin/bash

# Script pour diagnostiquer pourquoi les endpoints Actuator ne sont pas disponibles
# À exécuter sur la VM de staging

echo "=========================================="
echo "Diagnostic des endpoints Actuator"
echo "=========================================="

# 1. Vérifier les endpoints disponibles
echo ""
echo "1. Endpoints Actuator disponibles..."
curl -s http://localhost:8081/actuator | jq . || curl -s http://localhost:8081/actuator

# 2. Vérifier les logs de démarrage pour les erreurs Micrometer/Prometheus
echo ""
echo "2. Logs de démarrage (recherche Micrometer/Prometheus)..."
docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -i -E "micrometer|prometheus|actuator|metric" | head -30

# 3. Vérifier la configuration dans le conteneur
echo ""
echo "3. Configuration Actuator dans le conteneur..."
docker exec hotel-ticket-hub-backend-staging sh -c "cat /app/application.properties 2>/dev/null | grep -i 'management\|actuator\|prometheus' || echo 'Fichier non trouvé'"

# 4. Vérifier si la dépendance Micrometer est dans le JAR
echo ""
echo "4. Vérification de la présence de Micrometer dans le JAR..."
docker exec hotel-ticket-hub-backend-staging sh -c "jar -tf /app/app.jar 2>/dev/null | grep -i micrometer | head -10 || echo 'Impossible de vérifier'"

# 5. Vérifier les variables d'environnement
echo ""
echo "5. Variables d'environnement liées à Actuator..."
docker exec hotel-ticket-hub-backend-staging env | grep -i "MANAGEMENT\|ACTUATOR\|PROMETHEUS" || echo "Aucune variable trouvée"

# 6. Vérifier le health check
echo ""
echo "6. Health check..."
curl -s http://localhost:8081/actuator/health | jq . || curl -s http://localhost:8081/actuator/health

