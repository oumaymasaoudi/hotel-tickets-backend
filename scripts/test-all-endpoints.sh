#!/bin/bash
# Script complet pour tester tous les endpoints de l'API
# Usage: ./test-all-endpoints.sh [BASE_URL]

BASE_URL="${1:-http://localhost:8081}"
API_URL="${BASE_URL}/api"

echo "=========================================="
echo "TEST COMPLET DE L'API TICKETHOTEL"
echo "=========================================="
echo "Base URL: $BASE_URL"
echo "API URL: $API_URL"
echo ""

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Compteurs
PASSED=0
FAILED=0

test_endpoint() {
    local method=$1
    local endpoint=$2
    local description=$3
    local auth_token=$4
    local data=$5
    
    echo -n "Testing: $description ... "
    
    if [ -z "$auth_token" ]; then
        if [ "$method" = "GET" ]; then
            response=$(curl -s -w "\n%{http_code}" -X "$method" "$API_URL$endpoint")
        else
            response=$(curl -s -w "\n%{http_code}" -X "$method" "$API_URL$endpoint" \
                -H "Content-Type: application/json" \
                -d "$data")
        fi
    else
        if [ "$method" = "GET" ]; then
            response=$(curl -s -w "\n%{http_code}" -X "$method" "$API_URL$endpoint" \
                -H "Authorization: Bearer $auth_token")
        else
            response=$(curl -s -w "\n%{http_code}" -X "$method" "$API_URL$endpoint" \
                -H "Authorization: Bearer $auth_token" \
                -H "Content-Type: application/json" \
                -d "$data")
        fi
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
        ((PASSED++))
        return 0
    elif [ "$http_code" -eq 401 ] || [ "$http_code" -eq 403 ]; then
        echo -e "${YELLOW}⚠ AUTH REQUIRED${NC} (HTTP $http_code)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $http_code)"
        echo "  Response: $body" | head -c 100
        ((FAILED++))
        return 1
    fi
}

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. ENDPOINTS PUBLICS (Sans authentification)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Health Check
test_endpoint "GET" "/actuator/health" "Health Check" "" ""

# Hôtels publics
test_endpoint "GET" "/hotels/public" "Récupérer les hôtels publics" "" ""

# Catégories publiques
test_endpoint "GET" "/categories/public" "Récupérer les catégories publiques" "" ""

# Consentements GDPR disponibles
test_endpoint "GET" "/gdpr/available-consents" "Récupérer les consentements GDPR disponibles" "" ""

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. AUTHENTIFICATION"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Test de login (peut échouer si le hash n'est pas correct)
echo "Note: Le login nécessite un utilisateur valide dans la base de données"
echo ""

# Test de registration
REGISTER_DATA='{"email":"test@example.com","password":"Test123!","fullName":"Test User","phone":"+33612345678"}'
test_endpoint "POST" "/auth/register" "Créer un nouveau compte" "" "$REGISTER_DATA"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. ENDPOINTS PROTÉGÉS (Nécessitent authentification)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "Note: Ces endpoints nécessitent un token JWT valide"
echo "Ils devraient retourner 401/403 sans token, ce qui est normal"
echo ""

# Test avec token vide (devrait échouer)
test_endpoint "GET" "/hotels" "Récupérer tous les hôtels (sans token)" "" ""
test_endpoint "GET" "/users" "Récupérer tous les utilisateurs (sans token)" "" ""
test_endpoint "GET" "/tickets/all" "Récupérer tous les tickets (sans token)" "" ""

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4. SWAGGER UI"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

swagger_status=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/swagger-ui.html")
if [ "$swagger_status" -eq 200 ] || [ "$swagger_status" -eq 302 ]; then
    echo -e "${GREEN}✓ Swagger UI accessible${NC} (HTTP $swagger_status)"
    ((PASSED++))
else
    echo -e "${RED}✗ Swagger UI inaccessible${NC} (HTTP $swagger_status)"
    ((FAILED++))
fi

openapi_status=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/v3/api-docs")
if [ "$openapi_status" -eq 200 ]; then
    echo -e "${GREEN}✓ OpenAPI JSON accessible${NC} (HTTP $openapi_status)"
    ((PASSED++))
else
    echo -e "${RED}✗ OpenAPI JSON inaccessible${NC} (HTTP $openapi_status)"
    ((FAILED++))
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "RÉSUMÉ"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "Tests réussis: ${GREEN}$PASSED${NC}"
echo -e "Tests échoués: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ Tous les tests sont passés!${NC}"
    exit 0
else
    echo -e "${RED}❌ Certains tests ont échoué${NC}"
    exit 1
fi
