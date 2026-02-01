#!/bin/bash

# Script pour diagnostiquer pourquoi Prometheus ne peut pas scraper le backend
# À exécuter depuis la VM Monitoring

set -e

BACKEND_IP="13.63.15.86"
BACKEND_PORT="8081"
MONITORING_IP="16.170.74.58"

echo "=========================================="
echo "Diagnostic connexion Prometheus -> Backend"
echo "=========================================="
echo ""
echo "VM Monitoring: $MONITORING_IP"
echo "VM Backend: $BACKEND_IP:$BACKEND_PORT"
echo ""

# 1. Vérifier la connectivité réseau
echo "1. Test de connectivité réseau (ping)..."
if ping -c 2 -W 5 $BACKEND_IP > /dev/null 2>&1; then
    echo "   ✓ La VM Backend répond au ping"
else
    echo "   ✗ La VM Backend ne répond pas au ping"
    echo "   (Cela peut être normal si ICMP est bloqué)"
fi
echo ""

# 2. Vérifier que le port 8081 est accessible
echo "2. Test de connexion au port 8081..."
if timeout 5 bash -c "echo > /dev/tcp/$BACKEND_IP/$BACKEND_PORT" 2>/dev/null; then
    echo "   ✓ Le port 8081 est accessible"
else
    echo "   ✗ Le port 8081 n'est pas accessible"
    echo "   Vérifiez le Security Group AWS"
    exit 1
fi
echo ""

# 3. Vérifier l'endpoint Prometheus
echo "3. Test de l'endpoint /actuator/prometheus..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "http://$BACKEND_IP:$BACKEND_PORT/actuator/prometheus" 2>/dev/null || echo "000")

if [ "$HTTP_CODE" = "200" ]; then
    echo "   ✓ L'endpoint /actuator/prometheus répond (HTTP 200)"
    
    # Vérifier le contenu
    METRICS=$(curl -s --max-time 10 "http://$BACKEND_IP:$BACKEND_PORT/actuator/prometheus" 2>/dev/null | head -1)
    if echo "$METRICS" | grep -q "# HELP"; then
        echo "   ✓ Les métriques sont disponibles (format Prometheus)"
    else
        echo "   ⚠ L'endpoint répond mais le format semble incorrect"
    fi
elif [ "$HTTP_CODE" = "000" ]; then
    echo "   ✗ Impossible de se connecter (timeout ou connexion refusée)"
    echo "   Vérifiez:"
    echo "   - Que le backend est en cours d'exécution"
    echo "   - Que le Security Group autorise les connexions depuis $MONITORING_IP"
    exit 1
else
    echo "   ✗ L'endpoint répond avec le code HTTP $HTTP_CODE"
    echo "   Vérifiez les logs du backend"
    exit 1
fi
echo ""

# 4. Vérifier depuis le conteneur Prometheus
echo "4. Test depuis le conteneur Prometheus..."
if docker ps | grep -q prometheus; then
    PROMETHEUS_CONTAINER=$(docker ps | grep prometheus | awk '{print $1}')
    echo "   Conteneur Prometheus trouvé: $PROMETHEUS_CONTAINER"
    
    if docker exec $PROMETHEUS_CONTAINER wget -q -O- --timeout=5 "http://$BACKEND_IP:$BACKEND_PORT/actuator/prometheus" 2>/dev/null | head -1 | grep -q "# HELP"; then
        echo "   ✓ Prometheus peut accéder à l'endpoint"
    else
        echo "   ✗ Prometheus ne peut pas accéder à l'endpoint"
        echo "   Vérifiez la configuration réseau Docker"
    fi
else
    echo "   ⚠ Conteneur Prometheus non trouvé"
fi
echo ""

# 5. Vérifier la configuration Prometheus
echo "5. Vérification de la configuration Prometheus..."
if docker exec prometheus cat /etc/prometheus/prometheus.yml 2>/dev/null | grep -A 5 "staging-backend" | grep -q "$BACKEND_IP:$BACKEND_PORT"; then
    echo "   ✓ La configuration Prometheus est correcte"
    echo "   Target: $BACKEND_IP:$BACKEND_PORT/actuator/prometheus"
else
    echo "   ✗ La configuration Prometheus ne contient pas la bonne target"
    echo "   Vérifiez prometheus.yml"
fi
echo ""

# 6. Vérifier les logs Prometheus
echo "6. Dernières erreurs dans les logs Prometheus..."
docker logs prometheus 2>&1 | grep -iE "error|failed|$BACKEND_IP" | tail -5 || echo "   Aucune erreur récente"
echo ""

# 7. Résumé et recommandations
echo "=========================================="
echo "Résumé"
echo "=========================================="
echo ""
echo "Si tous les tests passent mais que Prometheus montre UNKNOWN:"
echo "1. Attendez 15-30 secondes (intervalle de scraping)"
echo "2. Vérifiez dans Prometheus UI: http://$MONITORING_IP:9090/targets"
echo "3. Cliquez sur 'Show more' pour voir les détails de l'erreur"
echo "4. Vérifiez les logs: docker logs prometheus --tail 50"
echo ""

