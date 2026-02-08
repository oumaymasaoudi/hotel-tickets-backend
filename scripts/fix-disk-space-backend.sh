#!/bin/bash
# Script pour libérer de l'espace disque sur la VM Backend
# Résout le problème "no space left on device"

echo "=========================================="
echo "NETTOYAGE DE L'ESPACE DISQUE - VM BACKEND"
echo "=========================================="
echo ""

# Vérifier l'espace avant
echo "Espace disque AVANT nettoyage:"
df -h / | tail -1
echo ""

# Nettoyer les conteneurs arrêtés
echo "1. Suppression des conteneurs arrêtés..."
docker container prune -f

# Nettoyer les images non utilisées
echo "2. Suppression des images Docker non utilisées..."
docker image prune -af

# Nettoyer les volumes non utilisés
echo "3. Suppression des volumes non utilisés..."
docker volume prune -f

# Nettoyer le build cache
echo "4. Nettoyage du build cache..."
docker builder prune -af

# Nettoyage système complet
echo "5. Nettoyage système Docker complet..."
docker system prune -af --volumes

# Vérifier l'espace après
echo ""
echo "Espace disque APRÈS nettoyage:"
df -h / | tail -1
echo ""

# Vérifier l'espace Docker
echo "Utilisation Docker:"
docker system df
echo ""

echo "✅ Nettoyage terminé!"
