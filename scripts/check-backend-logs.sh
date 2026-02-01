#!/bin/bash

# Script pour vérifier les logs du backend et diagnostiquer les problèmes de démarrage
# À exécuter sur la VM Backend

set -e

echo "=========================================="
echo "Diagnostic des logs du Backend"
echo "=========================================="
echo ""

# 1. Vérifier l'état du conteneur
echo "1. État du conteneur..."
CONTAINER_STATUS=$(docker ps --filter "name=hotel-ticket-hub-backend-staging" --format "{{.Status}}" 2>/dev/null || echo "not running")
echo "   Status: $CONTAINER_STATUS"

if [ "$CONTAINER_STATUS" = "not running" ]; then
    echo "   ✗ Le conteneur n'est pas en cours d'exécution"
    echo ""
    echo "   Conteneurs arrêtés:"
    docker ps -a | grep hotel-ticket-hub-backend-staging
    echo ""
    echo "   Logs du dernier démarrage:"
    docker logs hotel-ticket-hub-backend-staging --tail 50 2>&1
    exit 1
fi
echo ""

# 2. Vérifier les logs récents (dernières 50 lignes)
echo "2. Logs récents (dernières 50 lignes)..."
docker logs hotel-ticket-hub-backend-staging --tail 50 2>&1
echo ""

# 3. Vérifier les erreurs dans les logs
echo "3. Recherche d'erreurs dans les logs..."
ERRORS=$(docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -iE "error|exception|failed|fatal" | tail -20)
if [ -n "$ERRORS" ]; then
    echo "   ⚠ Erreurs trouvées:"
    echo "$ERRORS"
else
    echo "   ✓ Aucune erreur récente trouvée"
fi
echo ""

# 4. Vérifier si l'application Spring Boot a démarré
echo "4. Vérification du démarrage Spring Boot..."
if docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -q "Started.*Application"; then
    echo "   ✓ L'application Spring Boot a démarré"
    STARTUP_TIME=$(docker logs hotel-ticket-hub-backend-staging 2>&1 | grep "Started.*Application" | tail -1)
    echo "   $STARTUP_TIME"
else
    echo "   ✗ L'application Spring Boot n'a pas démarré"
    echo "   Vérifiez les logs ci-dessus pour les erreurs"
fi
echo ""

# 5. Vérifier les logs de démarrage (recherche de problèmes courants)
echo "5. Problèmes courants détectés..."
if docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -qi "database"; then
    echo "   ⚠ Problèmes potentiels avec la base de données"
    docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -i "database" | tail -5
fi

if docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -qi "port.*already.*in.*use"; then
    echo "   ⚠ Le port est déjà utilisé"
fi

if docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -qi "out of memory"; then
    echo "   ⚠ Problème de mémoire"
fi
echo ""

# 6. Vérifier les ressources système
echo "6. Utilisation des ressources..."
docker stats hotel-ticket-hub-backend-staging --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"
echo ""

# 7. Attendre et réessayer la connexion
echo "7. Test de connexion après attente..."
echo "   Attente de 10 secondes pour laisser l'application démarrer..."
sleep 10

if curl -s --max-time 5 http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo "   ✓ L'endpoint /actuator/health répond maintenant"
    curl -s http://localhost:8081/actuator/health
else
    echo "   ✗ L'endpoint ne répond toujours pas"
    echo "   L'application est probablement en train de démarrer ou a crashé"
    echo "   Vérifiez les logs ci-dessus"
fi
echo ""

# 8. Résumé et recommandations
echo "=========================================="
echo "Résumé et recommandations"
echo "=========================================="
echo ""
echo "Si l'application ne démarre pas:"
echo "1. Vérifiez les variables d'environnement (.env)"
echo "2. Vérifiez la connexion à la base de données"
echo "3. Vérifiez les logs complets: docker logs hotel-ticket-hub-backend-staging"
echo "4. Redémarrez le conteneur: docker restart hotel-ticket-hub-backend-staging"
echo ""

