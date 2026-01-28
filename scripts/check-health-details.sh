#!/bin/bash

# Script pour vérifier les détails du health check
# À exécuter sur la VM de staging

echo "=========================================="
echo "Vérification détaillée du health check"
echo "=========================================="

# Vérifier le health check avec show-details
echo "1. Health check détaillé..."
curl -s http://localhost:8081/actuator/health | jq . || curl -s http://localhost:8081/actuator/health

echo ""
echo "2. Vérification des health checks individuels..."
echo "Database:"
curl -s http://localhost:8081/actuator/health/db 2>/dev/null || echo "Endpoint /db non disponible"

echo ""
echo "Disk Space:"
curl -s http://localhost:8081/actuator/health/diskSpace 2>/dev/null || echo "Endpoint /diskSpace non disponible"

echo ""
echo "3. Logs récents (recherche d'erreurs mail)..."
docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -i "mail" | tail -10

echo ""
echo "4. Vérification de la configuration Actuator..."
docker exec hotel-ticket-hub-backend-staging cat /app/application.properties 2>/dev/null | grep -i "management.health.mail" || echo "Fichier non trouvé dans le conteneur"

echo ""
echo "5. Vérification de la version de l'image..."
docker inspect hotel-ticket-hub-backend-staging --format='{{.Config.Image}}'

