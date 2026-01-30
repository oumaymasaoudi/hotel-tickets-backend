#!/bin/bash

# Script de test pour vérifier le déploiement sur la VM de staging
# Usage: ./scripts/test-staging-deployment.sh

set -e

STAGING_HOST="${STAGING_HOST:-13.63.15.86}"
STAGING_USER="${STAGING_USER:-ubuntu}"
SSH_KEY="${SSH_KEY:-~/.ssh/oumayma-key.pem}"

echo "=========================================="
echo "Test du déploiement sur VM de staging"
echo "=========================================="
echo "Host: $STAGING_HOST"
echo "User: $STAGING_USER"
echo ""

# Test de connexion SSH
echo "1. Test de connexion SSH..."
if ssh -i "$SSH_KEY" -o ConnectTimeout=10 -o StrictHostKeyChecking=no "$STAGING_USER@$STAGING_HOST" "echo 'Connexion SSH OK'"; then
  echo "✓ Connexion SSH réussie"
else
  echo "✗ Échec de la connexion SSH"
  exit 1
fi

# Vérifier le conteneur Docker
echo ""
echo "2. Vérification du conteneur Docker..."
CONTAINER_STATUS=$(ssh -i "$SSH_KEY" "$STAGING_USER@$STAGING_HOST" \
  "docker ps --filter 'name=hotel-ticket-hub-backend-staging' --format '{{.Status}}' 2>/dev/null || echo 'NOT_RUNNING'")

if [ "$CONTAINER_STATUS" != "NOT_RUNNING" ] && [ -n "$CONTAINER_STATUS" ]; then
  echo "✓ Conteneur en cours d'exécution: $CONTAINER_STATUS"
else
  echo "✗ Conteneur non trouvé ou arrêté"
  echo "Vérification des conteneurs arrêtés..."
  ssh -i "$SSH_KEY" "$STAGING_USER@$STAGING_HOST" \
    "docker ps -a | grep hotel-ticket-hub-backend-staging || echo 'Aucun conteneur trouvé'"
fi

# Vérifier les logs récents
echo ""
echo "3. Logs récents du conteneur (10 dernières lignes)..."
ssh -i "$SSH_KEY" "$STAGING_USER@$STAGING_HOST" \
  "docker logs hotel-ticket-hub-backend-staging --tail 10 2>&1 || echo 'Impossible de récupérer les logs'"

# Test de l'endpoint de santé
echo ""
echo "4. Test de l'endpoint de santé..."
HEALTH_RESPONSE=$(ssh -i "$SSH_KEY" "$STAGING_USER@$STAGING_HOST" \
  "curl -s -o /dev/null -w '%{http_code}' http://localhost:8081/actuator/health 2>/dev/null || echo '000'")

if [ "$HEALTH_RESPONSE" = "200" ]; then
  echo "✓ Endpoint de santé répond (HTTP $HEALTH_RESPONSE)"
  echo "Détails de la santé:"
  ssh -i "$SSH_KEY" "$STAGING_USER@$STAGING_HOST" \
    "curl -s http://localhost:8081/actuator/health | head -20"
else
  echo "✗ Endpoint de santé ne répond pas (HTTP $HEALTH_RESPONSE)"
fi

# Vérifier l'accès depuis l'extérieur
echo ""
echo "5. Test d'accès depuis l'extérieur..."
EXTERNAL_HEALTH=$(curl -s -o /dev/null -w '%{http_code}' --connect-timeout 5 "http://$STAGING_HOST:8081/actuator/health" 2>/dev/null || echo "000")

if [ "$EXTERNAL_HEALTH" = "200" ]; then
  echo "✓ Accessible depuis l'extérieur (HTTP $EXTERNAL_HEALTH)"
else
  echo "⚠ Non accessible depuis l'extérieur (HTTP $EXTERNAL_HEALTH)"
  echo "  Vérifiez les Security Groups AWS pour autoriser le port 8081"
fi

# Vérifier l'espace disque
echo ""
echo "6. Vérification de l'espace disque..."
ssh -i "$SSH_KEY" "$STAGING_USER@$STAGING_HOST" \
  "df -h / | tail -1"

# Vérifier les fichiers déployés
echo ""
echo "7. Vérification des fichiers déployés..."
ssh -i "$SSH_KEY" "$STAGING_USER@$STAGING_HOST" \
  "ls -lh /opt/hotel-ticket-hub-backend-staging/ 2>/dev/null || echo 'Répertoire non trouvé'"

# Vérifier l'image Docker utilisée
echo ""
echo "8. Image Docker utilisée..."
ssh -i "$SSH_KEY" "$STAGING_USER@$STAGING_HOST" \
  "docker inspect hotel-ticket-hub-backend-staging --format='{{.Config.Image}}' 2>/dev/null || echo 'Conteneur non trouvé'"

echo ""
echo "=========================================="
echo "Test terminé"
echo "=========================================="

