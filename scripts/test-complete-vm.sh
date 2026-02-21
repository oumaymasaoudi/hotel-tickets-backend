#!/bin/bash
# Script complet de test de l'application sur la VM
# Teste backend, monitoring, et endpoints critiques

echo "=========================================="
echo "TEST COMPLET DE L'APPLICATION TICKETHOTEL"
echo "=========================================="
echo "Date: $(date)"
echo ""

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

BACKEND_URL="http://localhost:8081"
API_URL="${BACKEND_URL}/api"

# Compteurs
PASSED=0
FAILED=0
WARNINGS=0

test_check() {
    local name=$1
    local command=$2
    local expected=$3
    
    echo -n "Testing: $name ... "
    result=$(eval "$command" 2>/dev/null)
    
    if [ $? -eq 0 ] && [ -n "$result" ]; then
        if [ -n "$expected" ]; then
            if echo "$result" | grep -q "$expected"; then
                echo -e "${GREEN}✓ PASS${NC}"
                ((PASSED++))
                return 0
            else
                echo -e "${YELLOW}⚠ WARN${NC} (unexpected result)"
                ((WARNINGS++))
                return 1
            fi
        else
            echo -e "${GREEN}✓ PASS${NC}"
            ((PASSED++))
            return 0
        fi
    else
        echo -e "${RED}✗ FAIL${NC}"
        ((FAILED++))
        return 1
    fi
}

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. BACKEND - Health & Status"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Health Check
test_check "Health Check" "curl -s ${BACKEND_URL}/actuator/health | grep -q '\"status\":\"UP\"'" "UP"

# Container status
test_check "Backend Container" "docker ps | grep -q 'hotel-ticket-hub-backend-staging.*Up'" "Up"

# Node Exporter
test_check "Node Exporter" "docker ps | grep -q 'node-exporter-backend.*Up'" "Up"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. API ENDPOINTS PUBLICS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Hôtels publics
hotels_count=$(curl -s "${API_URL}/hotels/public" | python3 -c 'import sys, json; print(len(json.load(sys.stdin)))' 2>/dev/null || echo "0")
if [ "$hotels_count" -gt 0 ]; then
    echo -e "${GREEN}✓ Hôtels publics: $hotels_count hôtels trouvés${NC}"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠ Hôtels publics: Aucun hôtel trouvé${NC}"
    ((WARNINGS++))
fi

# Catégories publiques
categories_count=$(curl -s "${API_URL}/categories/public" | python3 -c 'import sys, json; print(len(json.load(sys.stdin)))' 2>/dev/null || echo "0")
if [ "$categories_count" -gt 0 ]; then
    echo -e "${GREEN}✓ Catégories publiques: $categories_count catégories trouvées${NC}"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠ Catégories publiques: Aucune catégorie trouvée${NC}"
    ((WARNINGS++))
fi

# Swagger UI
swagger_status=$(curl -s -o /dev/null -w "%{http_code}" "${BACKEND_URL}/swagger-ui.html")
if [ "$swagger_status" -eq 200 ] || [ "$swagger_status" -eq 302 ]; then
    echo -e "${GREEN}✓ Swagger UI accessible (HTTP $swagger_status)${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ Swagger UI inaccessible (HTTP $swagger_status)${NC}"
    ((FAILED++))
fi

# OpenAPI JSON
openapi_status=$(curl -s -o /dev/null -w "%{http_code}" "${BACKEND_URL}/v3/api-docs")
if [ "$openapi_status" -eq 200 ]; then
    echo -e "${GREEN}✓ OpenAPI JSON accessible (HTTP $openapi_status)${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ OpenAPI JSON inaccessible (HTTP $openapi_status)${NC}"
    ((FAILED++))
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. MÉTRIQUES PROMETHEUS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Prometheus endpoint
prometheus_metrics=$(curl -s "${BACKEND_URL}/actuator/prometheus" | head -1)
if echo "$prometheus_metrics" | grep -q "# HELP"; then
    echo -e "${GREEN}✓ Endpoint Prometheus accessible${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ Endpoint Prometheus inaccessible${NC}"
    ((FAILED++))
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4. LOGS & ERREURS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Vérifier les erreurs récentes
error_count=$(docker logs --tail 100 hotel-ticket-hub-backend-staging 2>&1 | grep -i "error\|exception\|failed" | wc -l)
if [ "$error_count" -eq 0 ]; then
    echo -e "${GREEN}✓ Aucune erreur récente dans les logs${NC}"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠ $error_count erreurs récentes dans les logs${NC}"
    ((WARNINGS++))
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "RÉSUMÉ"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "Tests réussis: ${GREEN}$PASSED${NC}"
echo -e "Avertissements: ${YELLOW}$WARNINGS${NC}"
echo -e "Tests échoués: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ Tous les tests critiques sont passés!${NC}"
    exit 0
else
    echo -e "${RED}❌ Certains tests critiques ont échoué${NC}"
    exit 1
fi
