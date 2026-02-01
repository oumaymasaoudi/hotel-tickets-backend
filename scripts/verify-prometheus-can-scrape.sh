#!/bin/bash

# Script pour vérifier que Prometheus peut scraper le backend
# À exécuter depuis la VM Monitoring (16.170.74.58)

set -e

BACKEND_IP="13.63.15.86"
BACKEND_PORT="8081"
MONITORING_IP="16.170.74.58"

echo "=========================================="
echo "Vérification que Prometheus peut scraper le backend"
echo "=========================================="
echo ""

# 1. Vérifier que l'endpoint Prometheus est accessible depuis la VM Monitoring
echo "1. Test de l'endpoint Prometheus depuis la VM Monitoring..."
if curl -s --max-time 10 "http://$BACKEND_IP:$BACKEND_PORT/actuator/prometheus" | head -1 | grep -q "# HELP"; then
    echo "   ✓ L'endpoint /actuator/prometheus est accessible"
    echo "   ✓ Les métriques sont disponibles"
else
    echo "   ✗ L'endpoint /actuator/prometheus n'est pas accessible"
    echo "   Vérifiez le Security Group AWS (port 8081)"
    exit 1
fi
echo ""

# 2. Vérifier depuis le conteneur Prometheus
echo "2. Test depuis le conteneur Prometheus..."
if docker ps | grep -q prometheus; then
    PROMETHEUS_CONTAINER=$(docker ps | grep prometheus | awk '{print $1}')
    if docker exec $PROMETHEUS_CONTAINER wget -q -O- --timeout=10 "http://$BACKEND_IP:$BACKEND_PORT/actuator/prometheus" 2>/dev/null | head -1 | grep -q "# HELP"; then
        echo "   ✓ Prometheus peut accéder à l'endpoint"
    else
        echo "   ✗ Prometheus ne peut pas accéder à l'endpoint"
        echo "   Vérifiez la configuration réseau Docker"
    fi
else
    echo "   ⚠ Conteneur Prometheus non trouvé"
fi
echo ""

# 3. Vérifier la configuration Prometheus
echo "3. Vérification de la configuration Prometheus..."
if docker exec prometheus cat /etc/prometheus/prometheus.yml 2>/dev/null | grep -A 5 "staging-backend" | grep -q "$BACKEND_IP:$BACKEND_PORT"; then
    echo "   ✓ La configuration Prometheus est correcte"
    echo "   Target: $BACKEND_IP:$BACKEND_PORT/actuator/prometheus"
else
    echo "   ✗ La configuration Prometheus ne contient pas la bonne target"
    echo "   Vérifiez prometheus.yml"
fi
echo ""

# 4. Vérifier les targets dans Prometheus
echo "4. Vérification des targets Prometheus..."
TARGETS=$(curl -s "http://localhost:9090/api/v1/targets" 2>/dev/null || echo "")
if echo "$TARGETS" | grep -q "staging-backend"; then
    echo "   ✓ La cible 'staging-backend' est configurée"
    
    # Vérifier le statut
    if echo "$TARGETS" | grep -A 10 "staging-backend" | grep -q '"health":"up"'; then
        echo "   ✓ La cible 'staging-backend' est UP"
        echo "   ✓ Prometheus peut scraper les métriques"
    else
        echo "   ⚠ La cible 'staging-backend' est DOWN ou UNKNOWN"
        echo "   Attendez 15-30 secondes et réessayez"
        echo "   Vérifiez: http://$MONITORING_IP:9090/targets"
    fi
else
    echo "   ✗ La cible 'staging-backend' n'est pas trouvée"
fi
echo ""

# 5. Vérifier les métriques collectées
echo "5. Vérification des métriques collectées..."
if curl -s "http://localhost:9090/api/v1/query?query=up{job=\"staging-backend\"}" 2>/dev/null | grep -q '"value"'; then
    echo "   ✓ Des métriques sont collectées pour le backend"
    
    # Afficher quelques métriques
    JVM_METRICS=$(curl -s "http://localhost:9090/api/v1/query?query=jvm_memory_used_bytes{job=\"staging-backend\"}" 2>/dev/null | grep -c "value" || echo "0")
    if [ "$JVM_METRICS" -gt 0 ]; then
        echo "   ✓ Les métriques JVM sont collectées ($JVM_METRICS métriques trouvées)"
    fi
else
    echo "   ⚠ Aucune métrique collectée pour le backend"
    echo "   Attendez 15-30 secondes (intervalle de scraping)"
fi
echo ""

# 6. Résumé
echo "=========================================="
echo "Résumé"
echo "=========================================="
echo ""
echo "Backend:"
echo "  - URL: http://$BACKEND_IP:$BACKEND_PORT"
echo "  - Health: http://$BACKEND_IP:$BACKEND_PORT/actuator/health"
echo "  - Prometheus: http://$BACKEND_IP:$BACKEND_PORT/actuator/prometheus"
echo ""
echo "Prometheus:"
echo "  - UI: http://$MONITORING_IP:9090"
echo "  - Targets: http://$MONITORING_IP:9090/targets"
echo "  - Graph: http://$MONITORING_IP:9090/graph"
echo ""
echo "Pour vérifier manuellement:"
echo "  1. Prometheus Targets: http://$MONITORING_IP:9090/targets"
echo "  2. Cherchez 'staging-backend' - devrait être UP (vert)"
echo "  3. Testez une requête: up{job=\"staging-backend\"}"
echo ""

