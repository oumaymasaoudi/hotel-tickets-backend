#!/bin/bash
# Script pour tester la connexion SuperAdmin

BACKEND_URL="${BACKEND_URL:-http://13.63.15.86:8081}"

echo "=========================================="
echo "Test de connexion SuperAdmin"
echo "=========================================="
echo ""

echo "1. Test de santé du backend..."
curl -s "$BACKEND_URL/actuator/health" | jq .
echo ""

echo "2. Test de connexion SuperAdmin..."
RESPONSE=$(curl -s -X POST "$BACKEND_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "oumaymasaoudi6@gmail.com",
    "password": "admin123"
  }')

echo "$RESPONSE" | jq . 2>/dev/null || echo "$RESPONSE"
echo ""

if echo "$RESPONSE" | grep -q "token"; then
    echo "✅ Connexion réussie!"
    TOKEN=$(echo "$RESPONSE" | jq -r '.token' 2>/dev/null)
    if [ -n "$TOKEN" ]; then
        echo "Token: ${TOKEN:0:50}..."
        echo ""
        echo "3. Test d'accès aux hôtels (avec token)..."
        curl -s -X GET "$BACKEND_URL/api/hotels" \
          -H "Authorization: Bearer $TOKEN" | jq . | head -20
    fi
else
    echo "❌ Échec de la connexion"
    echo "Vérifiez:"
    echo "  - Que l'utilisateur existe dans la base de données"
    echo "  - Que le mot de passe est correct"
    echo "  - Que le backend est démarré"
fi
