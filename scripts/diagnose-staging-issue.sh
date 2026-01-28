#!/bin/bash

# Script de diagnostic pour l'application staging DOWN
# À exécuter sur la VM de staging

echo "=========================================="
echo "Diagnostic de l'application DOWN"
echo "=========================================="

# 1. Vérifier les logs complets (pas seulement les 50 dernières lignes)
echo ""
echo "1. Logs complets de l'application (recherche d'erreurs)..."
docker logs hotel-ticket-hub-backend-staging 2>&1 | grep -i -E "error|exception|failed|down" | tail -20

# 2. Vérifier les logs de démarrage
echo ""
echo "2. Logs de démarrage (50 premières lignes)..."
docker logs hotel-ticket-hub-backend-staging 2>&1 | head -50

# 3. Vérifier la connexion à la base de données
echo ""
echo "3. Vérification de la connexion à la base de données..."
docker exec hotel-ticket-hub-backend-staging env | grep -E "SPRING_DATASOURCE|DATABASE"

# 4. Vérifier les variables d'environnement critiques
echo ""
echo "4. Variables d'environnement critiques..."
docker exec hotel-ticket-hub-backend-staging env | grep -E "SPRING_PROFILES_ACTIVE|SERVER_PORT|JWT_SECRET" | head -10

# 5. Vérifier si l'application écoute sur le port
echo ""
echo "5. Vérification des ports ouverts dans le conteneur..."
docker exec hotel-ticket-hub-backend-staging netstat -tlnp 2>/dev/null || docker exec hotel-ticket-hub-backend-staging ss -tlnp 2>/dev/null || echo "netstat/ss non disponible"

# 6. Tester la connexion interne au conteneur
echo ""
echo "6. Test de connexion HTTP interne..."
docker exec hotel-ticket-hub-backend-staging curl -s http://localhost:8080/actuator/health || echo "curl non disponible ou endpoint inaccessible"

# 7. Vérifier les health checks détaillés
echo ""
echo "7. Health check détaillé..."
curl -s http://localhost:8081/actuator/health | jq . || curl -s http://localhost:8081/actuator/health

# 8. Vérifier les métriques de l'application
echo ""
echo "8. Métriques de l'application..."
curl -s http://localhost:8081/actuator/metrics 2>/dev/null | head -20 || echo "Endpoint metrics non disponible"

# 9. Vérifier l'utilisation mémoire
echo ""
echo "9. Utilisation mémoire du conteneur..."
docker stats hotel-ticket-hub-backend-staging --no-stream

# 10. Vérifier le fichier .env
echo ""
echo "10. Vérification du fichier .env..."
cd /opt/hotel-ticket-hub-backend-staging
if [ -f .env ]; then
  echo "Fichier .env trouvé"
  echo "Variables présentes (sans valeurs sensibles):"
  grep -E "^[A-Z_]+=" .env | cut -d'=' -f1 | sort
else
  echo "ERREUR: Fichier .env non trouvé!"
fi

echo ""
echo "=========================================="
echo "Diagnostic terminé"
echo "=========================================="

