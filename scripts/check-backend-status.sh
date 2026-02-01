#!/bin/bash

# Script pour vérifier l'état du backend sur la VM Backend
# À exécuter sur la VM Backend (13.63.15.86)

set -e

echo "=========================================="
echo "Vérification de l'état du Backend"
echo "=========================================="
echo ""

# 1. Vérifier que le conteneur est en cours d'exécution
echo "1. État du conteneur Docker..."
if docker ps | grep -q hotel-ticket-hub-backend-staging; then
    echo "   ✓ Le conteneur est en cours d'exécution"
    docker ps | grep hotel-ticket-hub-backend-staging
else
    echo "   ✗ Le conteneur n'est pas en cours d'exécution"
    echo ""
    echo "   Conteneurs arrêtés:"
    docker ps -a | grep hotel-ticket-hub-backend-staging || echo "   Aucun conteneur trouvé"
    echo ""
    echo "   Pour démarrer:"
    echo "   cd /opt/hotel-ticket-hub-backend-staging"
    echo "   docker compose up -d"
    exit 1
fi
echo ""

# 2. Vérifier les ports exposés
echo "2. Ports exposés..."
PORTS=$(docker ps --filter "name=hotel-ticket-hub-backend-staging" --format "{{.Ports}}")
echo "   Ports: $PORTS"
if echo "$PORTS" | grep -q "8081"; then
    echo "   ✓ Le port 8081 est exposé"
else
    echo "   ✗ Le port 8081 n'est pas exposé"
    echo "   Vérifiez docker-compose.yml"
fi
echo ""

# 3. Vérifier l'endpoint localement
echo "3. Test de l'endpoint localement (depuis la VM)..."
if curl -s --max-time 5 http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo "   ✓ L'endpoint /actuator/health répond"
    HEALTH=$(curl -s http://localhost:8081/actuator/health)
    echo "   Réponse: $HEALTH"
else
    echo "   ✗ L'endpoint /actuator/health ne répond pas"
    echo "   Vérifiez les logs du conteneur"
fi
echo ""

# 4. Vérifier l'endpoint Prometheus localement
echo "4. Test de l'endpoint Prometheus localement..."
if curl -s --max-time 5 http://localhost:8081/actuator/prometheus | head -1 | grep -q "# HELP"; then
    echo "   ✓ L'endpoint /actuator/prometheus répond"
    echo "   ✓ Les métriques sont disponibles"
else
    echo "   ✗ L'endpoint /actuator/prometheus ne répond pas ou format incorrect"
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 http://localhost:8081/actuator/prometheus 2>/dev/null || echo "000")
    echo "   Code HTTP: $HTTP_CODE"
fi
echo ""

# 5. Vérifier depuis l'extérieur (IP publique)
echo "5. Test depuis l'IP publique..."
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "unknown")
echo "   IP publique de cette VM: $PUBLIC_IP"

if [ "$PUBLIC_IP" != "unknown" ]; then
    if curl -s --max-time 5 "http://$PUBLIC_IP:8081/actuator/health" > /dev/null 2>&1; then
        echo "   ✓ Accessible depuis l'extérieur via l'IP publique"
    else
        echo "   ✗ Non accessible depuis l'extérieur"
        echo "   Vérifiez le Security Group AWS"
    fi
fi
echo ""

# 6. Vérifier les logs récents
echo "6. Logs récents du conteneur (dernières 10 lignes)..."
docker logs hotel-ticket-hub-backend-staging --tail 10 2>&1 | tail -10
echo ""

# 7. Vérifier les variables d'environnement
echo "7. Variables d'environnement Prometheus..."
docker exec hotel-ticket-hub-backend-staging env | grep -iE "PROMETHEUS|EXPOSURE|MANAGEMENT" || echo "   Aucune variable trouvée"
echo ""

# 8. Résumé
echo "=========================================="
echo "Résumé"
echo "=========================================="
echo ""
echo "Si le conteneur est UP mais non accessible depuis l'extérieur:"
echo "1. Vérifiez le Security Group AWS (port 8081)"
echo "2. Vérifiez que le port est bien mappé dans docker-compose.yml"
echo "3. Vérifiez les logs pour des erreurs de démarrage"
echo ""

