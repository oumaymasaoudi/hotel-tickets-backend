#!/bin/bash

# Script pour corriger le docker-compose.yml sur la VM
# À exécuter sur la VM de staging

cd /opt/hotel-ticket-hub-backend-staging

echo "Nettoyage du docker-compose.yml..."

# Supprimer les lignes mal ajoutées à la fin (lignes 10-12)
# Garder seulement les lignes jusqu'à "driver: bridge"
sed -i '/^# Désactiver le health check mail$/,$d' docker-compose.yml

# Vérifier que la variable est bien dans la section environment du service backend
if ! grep -q "MANAGEMENT_HEALTH_MAIL_ENABLED" docker-compose.yml; then
  echo "Ajout de la variable MANAGEMENT_HEALTH_MAIL_ENABLED..."
  sed -i '/SPRING_MAIL_PASSWORD=/a\      - MANAGEMENT_HEALTH_MAIL_ENABLED=false' docker-compose.yml
fi

# Vérifier la syntaxe
echo "Vérification de la syntaxe..."
docker compose config > /dev/null 2>&1
if [ $? -eq 0 ]; then
  echo "✓ Syntaxe correcte"
else
  echo "✗ Erreur de syntaxe, affichage du fichier:"
  cat docker-compose.yml
  exit 1
fi

echo "Redémarrage des conteneurs..."
docker compose down
docker compose up -d

echo "Attente de 30 secondes..."
sleep 30

echo "Vérification du health check..."
curl -s http://localhost:8081/actuator/health | jq . || curl -s http://localhost:8081/actuator/health

