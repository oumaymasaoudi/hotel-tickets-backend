#!/bin/bash

# Script pour corriger le health check sur la VM de staging
# À exécuter sur la VM après avoir mis à jour le code

echo "=========================================="
echo "Correction du health check sur la VM"
echo "=========================================="

cd /opt/hotel-ticket-hub-backend-staging

# Option 1: Redémarrer le conteneur pour prendre en compte la nouvelle configuration
echo "Redémarrage du conteneur..."
docker compose restart backend

echo "Attente de 30 secondes pour le démarrage..."
sleep 30

echo "Vérification du health check..."
curl -s http://localhost:8081/actuator/health | jq . || curl -s http://localhost:8081/actuator/health

echo ""
echo "Si le health check est toujours DOWN, vérifiez les logs:"
echo "docker logs hotel-ticket-hub-backend-staging --tail 50"

