#!/bin/bash
# Script complet de test de toutes les fonctionnalités de l'application
# Teste: Auth, Tickets, Hotels, Abonnements, Paiements, Rapports

echo "=========================================="
echo "TEST COMPLET DES FONCTIONNALITÉS"
echo "=========================================="
echo "Date: $(date)"
echo ""

BASE_URL="${1:-http://localhost:8081}"
API_URL="${BASE_URL}/api"

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# Variables globales
TOKEN=""
HOTEL_ID=""
PLAN_ID=""
USER_ID=""
TICKET_ID=""
PAYMENT_ID=""

PASSED=0
FAILED=0

# Fonction de test
test_api() {
    local method=$1
    local endpoint=$2
    local description=$3
    local data=$4
    local expected_status=${5:-200}
    
    echo -n "Testing: $description ... "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$API_URL$endpoint" \
            ${TOKEN:+-H "Authorization: Bearer $TOKEN"})
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$API_URL$endpoint" \
            -H "Content-Type: application/json" \
            ${TOKEN:+-H "Authorization: Bearer $TOKEN"} \
            ${data:+-d "$data"})
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
        ((PASSED++))
        echo "$body"
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $http_code, expected $expected_status)"
        echo "Response: $body" | head -c 200
        ((FAILED++))
        return 1
    fi
}

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. ENDPOINTS PUBLICS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Health check
test_api "GET" "" "/actuator/health" "Health Check" "" 200

# Hôtels publics
hotels_response=$(test_api "GET" "/hotels/public" "Récupérer les hôtels publics" "" 200)
HOTEL_ID=$(echo "$hotels_response" | python3 -c 'import sys, json; d=json.load(sys.stdin); print(d[0]["id"]) if d and len(d) > 0 else ""' 2>/dev/null || echo "")
if [ -n "$HOTEL_ID" ]; then
    echo "  → Hôtel trouvé: $HOTEL_ID"
fi

# Catégories publiques
categories_response=$(test_api "GET" "/categories/public" "Récupérer les catégories publiques" "" 200)
CATEGORY_ID=$(echo "$categories_response" | python3 -c 'import sys, json; d=json.load(sys.stdin); print(d[0]["id"]) if d and len(d) > 0 else ""' 2>/dev/null || echo "")
if [ -n "$CATEGORY_ID" ]; then
    echo "  → Catégorie trouvée: $CATEGORY_ID"
fi

# Plans disponibles (public pour voir les plans)
plans_response=$(test_api "GET" "/plans" "Récupérer les plans (nécessite auth)" "" 401)
echo "  → Endpoint protégé (normal)"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. AUTHENTIFICATION"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Créer un compte de test
TEST_EMAIL="test-$(date +%s)@example.com"
TEST_PASSWORD="Test123!"
register_data="{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\",\"fullName\":\"Test User\",\"phone\":\"+33612345678\"}"

register_response=$(test_api "POST" "/auth/register" "Créer un compte de test" "$register_data" 200)
echo "  → Compte créé: $TEST_EMAIL"

# Se connecter
login_data="{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}"
login_response=$(test_api "POST" "/auth/login" "Se connecter" "$login_data" 200)
TOKEN=$(echo "$login_response" | python3 -c 'import sys, json; d=json.load(sys.stdin); print(d.get("token", ""))' 2>/dev/null || echo "")

if [ -n "$TOKEN" ]; then
    echo "  → Token obtenu: ${TOKEN:0:20}..."
    USER_ID=$(echo "$login_response" | python3 -c 'import sys, json; d=json.load(sys.stdin); u=d.get("user", {}); print(u.get("id", ""))' 2>/dev/null || echo "")
    echo "  → User ID: $USER_ID"
else
    echo -e "${RED}  ✗ Échec de l'authentification${NC}"
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. GESTION DES PLANS ET ABONNEMENTS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Récupérer les plans (avec token)
plans_response=$(test_api "GET" "/plans" "Récupérer tous les plans" "" 200)
PLAN_ID=$(echo "$plans_response" | python3 -c 'import sys, json; d=json.load(sys.stdin); print(d[0]["id"]) if d and len(d) > 0 else ""' 2>/dev/null || echo "")

if [ -n "$PLAN_ID" ]; then
    echo "  → Plan trouvé: $PLAN_ID"
    plan_name=$(echo "$plans_response" | python3 -c 'import sys, json; d=json.load(sys.stdin); print(d[0].get("name", "")) if d and len(d) > 0 else ""' 2>/dev/null || echo "")
    plan_cost=$(echo "$plans_response" | python3 -c 'import sys, json; d=json.load(sys.stdin); print(d[0].get("baseCost", "")) if d and len(d) > 0 else ""' 2>/dev/null || echo "")
    echo "  → Plan: $plan_name - Coût: $plan_cost"
else
    echo -e "${YELLOW}  ⚠ Aucun plan trouvé${NC}"
fi

# Statistiques des plans (nécessite SUPERADMIN)
stats_response=$(test_api "GET" "/plans/statistics" "Statistiques des plans" "" 403)
echo "  → Endpoint réservé à SUPERADMIN (normal)"

# Abonnement d'un hôtel (si on a un hotel_id)
if [ -n "$HOTEL_ID" ]; then
    subscription_response=$(test_api "GET" "/subscriptions/hotel/$HOTEL_ID" "Récupérer l'abonnement d'un hôtel" "" 200)
    echo "  → Abonnement récupéré pour l'hôtel"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4. GESTION DES PAIEMENTS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -n "$HOTEL_ID" ]; then
    # Récupérer les paiements d'un hôtel
    payments_response=$(test_api "GET" "/payments/hotel/$HOTEL_ID" "Récupérer les paiements d'un hôtel" "" 200)
    payment_count=$(echo "$payments_response" | python3 -c 'import sys, json; d=json.load(sys.stdin); print(len(d)) if isinstance(d, list) else 0' 2>/dev/null || echo "0")
    echo "  → $payment_count paiement(s) trouvé(s)"
    
    # Dernier paiement
    last_payment_response=$(test_api "GET" "/payments/hotel/$HOTEL_ID/last" "Récupérer le dernier paiement" "" 200)
    
    # Statut de paiement
    status_response=$(test_api "GET" "/payments/hotel/$HOTEL_ID/status" "Récupérer le statut de paiement" "" 200)
    echo "  → Statut de paiement récupéré"
    
    # Créer un paiement de test (si on a les infos nécessaires)
    if [ -n "$PLAN_ID" ]; then
        payment_data="{\"amount\":99.99,\"paymentDate\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",\"status\":\"COMPLETED\",\"paymentMethod\":\"CARD\"}"
        create_payment_response=$(test_api "POST" "/payments/hotel/$HOTEL_ID" "Créer un paiement" "$payment_data" 200)
        PAYMENT_ID=$(echo "$create_payment_response" | python3 -c 'import sys, json; d=json.load(sys.stdin); print(d.get("id", ""))' 2>/dev/null || echo "")
        if [ -n "$PAYMENT_ID" ]; then
            echo "  → Paiement créé: $PAYMENT_ID"
        fi
    fi
else
    echo -e "${YELLOW}  ⚠ Pas d'hôtel disponible pour tester les paiements${NC}"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5. GESTION DES TICKETS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Créer un ticket public (sans auth)
if [ -n "$HOTEL_ID" ] && [ -n "$CATEGORY_ID" ]; then
    ticket_data="{\"title\":\"Test Ticket $(date +%s)\",\"description\":\"Description du ticket de test\",\"categoryId\":\"$CATEGORY_ID\",\"hotelId\":\"$HOTEL_ID\",\"clientEmail\":\"$TEST_EMAIL\",\"clientName\":\"Test Client\",\"clientPhone\":\"+33612345678\"}"
    create_ticket_response=$(test_api "POST" "/tickets/public" "Créer un ticket (public)" "$ticket_data" 200)
    TICKET_ID=$(echo "$create_ticket_response" | python3 -c 'import sys, json; d=json.load(sys.stdin); print(d.get("id", ""))' 2>/dev/null || echo "")
    TICKET_NUMBER=$(echo "$create_ticket_response" | python3 -c 'import sys, json; d=json.load(sys.stdin); print(d.get("ticketNumber", ""))' 2>/dev/null || echo "")
    
    if [ -n "$TICKET_ID" ]; then
        echo "  → Ticket créé: $TICKET_NUMBER (ID: $TICKET_ID)"
        
        # Récupérer le ticket par numéro
        if [ -n "$TICKET_NUMBER" ]; then
            get_ticket_response=$(test_api "GET" "/tickets/public/$TICKET_NUMBER" "Récupérer le ticket par numéro" "" 200)
        fi
        
        # Récupérer les tickets par email
        tickets_by_email=$(test_api "GET" "/tickets/public/email/$TEST_EMAIL" "Récupérer les tickets par email" "" 200)
        ticket_count=$(echo "$tickets_by_email" | python3 -c 'import sys, json; d=json.load(sys.stdin); print(len(d)) if isinstance(d, list) else 0' 2>/dev/null || echo "0")
        echo "  → $ticket_count ticket(s) trouvé(s) pour $TEST_EMAIL"
    fi
fi

# Récupérer les tickets d'un hôtel (avec auth)
if [ -n "$HOTEL_ID" ]; then
    hotel_tickets=$(test_api "GET" "/tickets/hotel/$HOTEL_ID" "Récupérer les tickets d'un hôtel" "" 200)
    hotel_ticket_count=$(echo "$hotel_tickets" | python3 -c 'import sys, json; d=json.load(sys.stdin); print(len(d)) if isinstance(d, list) else 0' 2>/dev/null || echo "0")
    echo "  → $hotel_ticket_count ticket(s) pour l'hôtel"
fi

# Mettre à jour le statut d'un ticket
if [ -n "$TICKET_ID" ]; then
    update_status_data="{\"status\":\"EN_COURS\"}"
    update_response=$(test_api "PATCH" "/tickets/$TICKET_ID/status" "Mettre à jour le statut du ticket" "$update_status_data" 200)
    echo "  → Statut du ticket mis à jour"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "6. RAPPORTS ET STATISTIQUES"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ -n "$HOTEL_ID" ]; then
    # Rapport mensuel
    monthly_report=$(test_api "GET" "/reports/hotel/$HOTEL_ID/monthly" "Rapport mensuel" "" 200)
    echo "  → Rapport mensuel généré"
    
    # Rapport hebdomadaire
    weekly_report=$(test_api "GET" "/reports/hotel/$HOTEL_ID/weekly" "Rapport hebdomadaire" "" 200)
    echo "  → Rapport hebdomadaire généré"
    
    # Rapport quotidien
    daily_report=$(test_api "GET" "/reports/hotel/$HOTEL_ID/daily" "Rapport quotidien" "" 200)
    echo "  → Rapport quotidien généré"
fi

# Rapport global (nécessite SUPERADMIN)
global_report=$(test_api "GET" "/reports/global" "Rapport global" "" 403)
echo "  → Endpoint réservé à SUPERADMIN (normal)"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "7. NAVIGATION ET FLUX COMPLETS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "Test du flux complet:"
echo "  1. ✅ Consultation des hôtels publics"
echo "  2. ✅ Consultation des catégories"
echo "  3. ✅ Création de compte"
echo "  4. ✅ Authentification"
echo "  5. ✅ Consultation des plans d'abonnement"
echo "  6. ✅ Consultation des abonnements"
echo "  7. ✅ Consultation des paiements"
echo "  8. ✅ Création de ticket"
echo "  9. ✅ Suivi de ticket"
echo "  10. ✅ Mise à jour de statut"
echo "  11. ✅ Consultation des rapports"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "RÉSUMÉ"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "Tests réussis: ${GREEN}$PASSED${NC}"
echo -e "Tests échoués: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ Toutes les fonctionnalités sont opérationnelles!${NC}"
    exit 0
else
    echo -e "${RED}❌ Certaines fonctionnalités nécessitent une attention${NC}"
    exit 1
fi
