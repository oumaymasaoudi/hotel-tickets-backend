#!/bin/bash

# Script pour vérifier Prometheus et la supervision complète
# Usage: ./verify-prometheus-monitoring.sh [MONITORING_VM_IP] [MONITORING_USER]

set -e

MONITORING_VM_IP=${1:-"16.170.74.58"}  # IP de la VM ansible-controller (monitoring)
MONITORING_USER=${2:-"ubuntu"}
BACKEND_IP="13.63.15.86"
BACKEND_PORT="8081"

echo "=========================================="
echo "Vérification Prometheus et Supervision"
echo "=========================================="
echo ""
echo "Configuration:"
echo "  VM Monitoring: $MONITORING_VM_IP"
echo "  VM Backend: $BACKEND_IP:$BACKEND_PORT"
echo ""

# 1. Vérifier que Prometheus est accessible
echo "1. Vérification de Prometheus..."
if curl -s --max-time 5 "http://$MONITORING_VM_IP:9090/-/healthy" > /dev/null 2>&1; then
    echo "   ✓ Prometheus est accessible sur http://$MONITORING_VM_IP:9090"
else
    echo "   ✗ Prometheus n'est pas accessible sur http://$MONITORING_VM_IP:9090"
    echo "   Vérifiez que:"
    echo "   - Le conteneur Prometheus est en cours d'exécution"
    echo "   - Le port 9090 est ouvert dans le Security Group AWS"
    exit 1
fi
echo ""

# 2. Vérifier que Grafana est accessible
echo "2. Vérification de Grafana..."
if curl -s --max-time 5 "http://$MONITORING_VM_IP:3000/api/health" > /dev/null 2>&1; then
    echo "   ✓ Grafana est accessible sur http://$MONITORING_VM_IP:3000"
else
    echo "   ✗ Grafana n'est pas accessible sur http://$MONITORING_VM_IP:3000"
    echo "   Vérifiez que:"
    echo "   - Le conteneur Grafana est en cours d'exécution"
    echo "   - Le port 3000 est ouvert dans le Security Group AWS"
fi
echo ""

# 3. Vérifier que l'endpoint Prometheus du backend est accessible
echo "3. Vérification de l'endpoint Prometheus du backend..."
if curl -s --max-time 5 "http://$BACKEND_IP:$BACKEND_PORT/actuator/prometheus" | head -1 | grep -q "# HELP"; then
    echo "   ✓ L'endpoint /actuator/prometheus du backend est accessible"
    echo "   ✓ Les métriques sont disponibles"
else
    echo "   ✗ L'endpoint /actuator/prometheus du backend n'est pas accessible"
    echo "   Vérifiez que:"
    echo "   - Le backend est en cours d'exécution"
    echo "   - L'endpoint Prometheus est activé (MANAGEMENT_ENDPOINT_PROMETHEUS_ENABLED=true)"
    echo "   - Le port $BACKEND_PORT est ouvert dans le Security Group AWS"
    exit 1
fi
echo ""

# 4. Vérifier que Prometheus peut scraper le backend
echo "4. Vérification du scraping Prometheus..."
PROMETHEUS_TARGETS=$(curl -s "http://$MONITORING_VM_IP:9090/api/v1/targets" 2>/dev/null || echo "")
if echo "$PROMETHEUS_TARGETS" | grep -q "staging-backend"; then
    echo "   ✓ La cible 'staging-backend' est configurée dans Prometheus"
    
    # Vérifier le statut de la cible
    if echo "$PROMETHEUS_TARGETS" | grep -A 5 "staging-backend" | grep -q '"health":"up"'; then
        echo "   ✓ La cible 'staging-backend' est UP (Prometheus peut scraper)"
    else
        echo "   ⚠ La cible 'staging-backend' est DOWN"
        echo "   Vérifiez les logs de Prometheus pour plus de détails"
    fi
else
    echo "   ✗ La cible 'staging-backend' n'est pas trouvée dans Prometheus"
    echo "   Vérifiez la configuration prometheus.yml"
fi
echo ""

# 5. Vérifier les métriques collectées
echo "5. Vérification des métriques collectées..."
METRICS_COUNT=$(curl -s "http://$MONITORING_VM_IP:9090/api/v1/query?query=up{job=\"staging-backend\"}" 2>/dev/null | grep -o '"value"' | wc -l || echo "0")
if [ "$METRICS_COUNT" -gt 0 ]; then
    echo "   ✓ Des métriques sont collectées pour le backend"
    
    # Afficher quelques métriques JVM
    JVM_METRICS=$(curl -s "http://$MONITORING_VM_IP:9090/api/v1/query?query=jvm_memory_used_bytes{job=\"staging-backend\"}" 2>/dev/null | grep -c "value" || echo "0")
    if [ "$JVM_METRICS" -gt 0 ]; then
        echo "   ✓ Les métriques JVM sont collectées ($JVM_METRICS métriques trouvées)"
    else
        echo "   ⚠ Aucune métrique JVM trouvée"
    fi
else
    echo "   ⚠ Aucune métrique collectée pour le backend"
    echo "   Vérifiez que Prometheus peut accéder au backend"
fi
echo ""

# 6. Résumé
echo "=========================================="
echo "Résumé"
echo "=========================================="
echo ""
echo "Prometheus: http://$MONITORING_VM_IP:9090"
echo "Grafana: http://$MONITORING_VM_IP:3000"
echo "Backend Prometheus: http://$BACKEND_IP:$BACKEND_PORT/actuator/prometheus"
echo ""
echo "Pour vérifier manuellement:"
echo "1. Prometheus Targets: http://$MONITORING_VM_IP:9090/targets"
echo "2. Prometheus Graph: http://$MONITORING_VM_IP:9090/graph"
echo "3. Grafana Dashboards: http://$MONITORING_VM_IP:3000"
echo ""

