#!/bin/bash

# Script pour corriger la configuration Prometheus sur la VM de staging
# À exécuter sur la VM de staging

echo "=========================================="
echo "Correction de la configuration Prometheus"
echo "=========================================="

cd /opt/hotel-ticket-hub-backend-staging || exit 1

# Vérifier que docker-compose.yml existe
if [ ! -f docker-compose.yml ]; then
    echo "ERREUR: docker-compose.yml non trouvé dans $(pwd)"
    exit 1
fi

# Sauvegarder le fichier original
cp docker-compose.yml docker-compose.yml.backup

# Vérifier si la variable existe déjà
if grep -q "MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE" docker-compose.yml; then
    echo "La variable MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE existe déjà"
    # Mettre à jour la valeur
    sed -i 's|MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=.*|MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,prometheus,metrics|' docker-compose.yml
else
    # Ajouter la variable après MANAGEMENT_ENDPOINT_PROMETHEUS_ENABLED
    sed -i '/MANAGEMENT_ENDPOINT_PROMETHEUS_ENABLED=true/a\      # Exposer les endpoints Actuator\n      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,prometheus,metrics' docker-compose.yml
fi

echo ""
echo "Configuration mise à jour. Redémarrage du conteneur..."
docker compose down
docker compose up -d

echo ""
echo "Attente du démarrage (15 secondes)..."
sleep 15

echo ""
echo "Vérification de l'endpoint /actuator..."
curl -s http://localhost:8081/actuator | jq '.' || curl -s http://localhost:8081/actuator

echo ""
echo "Test de l'endpoint /actuator/prometheus..."
curl -s http://localhost:8081/actuator/prometheus | head -20 || echo "Endpoint toujours non disponible"

echo ""
echo "=========================================="
echo "Correction terminée"
echo "=========================================="

