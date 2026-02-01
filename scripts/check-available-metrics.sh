#!/bin/bash

# Script pour vérifier les métriques disponibles depuis Prometheus
# À exécuter depuis la VM Monitoring

BACKEND_IP="13.63.15.86"
BACKEND_PORT="8081"
MONITORING_IP="16.170.74.58"

echo "=========================================="
echo "Vérification des métriques disponibles"
echo "=========================================="
echo ""

# 1. Vérifier les métriques HTTP
echo "1. Métriques HTTP disponibles:"
curl -s "http://localhost:9090/api/v1/label/__name__/values" | jq -r '.data[]' | grep -i "http" | head -20
echo ""

# 2. Vérifier les métriques JVM
echo "2. Métriques JVM disponibles:"
curl -s "http://localhost:9090/api/v1/label/__name__/values" | jq -r '.data[]' | grep -i "jvm" | head -20
echo ""

# 3. Vérifier les métriques HikariCP (Database)
echo "3. Métriques Database (HikariCP) disponibles:"
curl -s "http://localhost:9090/api/v1/label/__name__/values" | jq -r '.data[]' | grep -i "hikari\|database\|jdbc" | head -20
echo ""

# 4. Tester des requêtes spécifiques
echo "4. Test de requêtes spécifiques:"
echo ""
echo "   HTTP Requests Count:"
curl -s "http://localhost:9090/api/v1/query?query=http_server_requests_seconds_count{job=\"staging-backend\"}" | jq '.data.result | length'
echo ""

echo "   HTTP Requests Bucket (pour histogram):"
curl -s "http://localhost:9090/api/v1/query?query=http_server_requests_seconds_bucket{job=\"staging-backend\"}" | jq '.data.result | length'
echo ""

echo "   JVM Memory:"
curl -s "http://localhost:9090/api/v1/query?query=jvm_memory_used_bytes{job=\"staging-backend\"}" | jq '.data.result | length'
echo ""

echo "   Hikari Connections:"
curl -s "http://localhost:9090/api/v1/query?query=hikari_connections_active{job=\"staging-backend\"}" | jq '.data.result | length'
echo ""

# 5. Lister toutes les métriques pour staging-backend
echo "5. Toutes les métriques pour staging-backend (premiers 50):"
curl -s "http://localhost:9090/api/v1/series?match[]={job=\"staging-backend\"}" | jq -r '.data[].__name__' | sort | uniq | head -50
echo ""

echo "=========================================="
echo "Pour voir toutes les métriques:"
echo "  Prometheus UI → http://$MONITORING_IP:9090/graph"
echo "  Testez: {job=\"staging-backend\"}"
echo "=========================================="

