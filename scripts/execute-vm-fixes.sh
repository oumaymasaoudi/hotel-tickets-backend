#!/bin/bash
# Script complet pour exécuter toutes les corrections sur les VMs
# Ce script peut être exécuté depuis votre machine locale ou via SSH

set -e

# Configuration
DB_VM="13.48.83.147"
BACKEND_VM="13.63.15.86"
SSH_KEY="${SSH_KEY:-~/.ssh/oumayma-key.pem}"
SSH_USER="ubuntu"
PROJECT_DIR="${PROJECT_DIR:-~/hotel-ticket-hub-backend}"

echo "=========================================="
echo "Correction complète des VMs"
echo "=========================================="
echo ""

# Fonction pour exécuter une commande sur une VM
execute_on_vm() {
    local vm=$1
    local command=$2
    echo "Exécution sur $vm: $command"
    ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no "$SSH_USER@$vm" "$command"
}

# Étape 1 : Corriger la base de données
echo "📊 Étape 1 : Correction de la base de données sur $DB_VM"
echo "---------------------------------------------------"

execute_on_vm "$DB_VM" "cd $PROJECT_DIR && git pull origin main || echo 'Git pull failed, continuing...'"

execute_on_vm "$DB_VM" "cd $PROJECT_DIR && chmod +x scripts/fix-vm-database-issues.sh && ./scripts/fix-vm-database-issues.sh"

echo ""
echo "✅ Base de données corrigée"
echo ""

# Étape 2 : Redéployer le backend
echo "🚀 Étape 2 : Redéploiement du backend sur $BACKEND_VM"
echo "---------------------------------------------------"

execute_on_vm "$BACKEND_VM" "cd $PROJECT_DIR && git pull origin main"

execute_on_vm "$BACKEND_VM" "cd $PROJECT_DIR && docker compose down"

execute_on_vm "$BACKEND_VM" "cd $PROJECT_DIR && docker compose pull"

execute_on_vm "$BACKEND_VM" "cd $PROJECT_DIR && docker compose up -d --build"

echo ""
echo "✅ Backend redéployé"
echo ""

# Étape 3 : Vérifications
echo "🔍 Étape 3 : Vérifications"
echo "---------------------------------------------------"

echo "Attente de 10 secondes pour le démarrage du backend..."
sleep 10

echo ""
echo "Vérification de la santé du backend:"
execute_on_vm "$BACKEND_VM" "curl -s http://localhost:8081/actuator/health || echo 'Backend pas encore prêt'"

echo ""
echo "Vérification des hôtels publics:"
execute_on_vm "$BACKEND_VM" "curl -s http://localhost:8081/api/hotels/public | head -c 200"

echo ""
echo "Dernières lignes des logs:"
execute_on_vm "$BACKEND_VM" "docker logs hotel-ticket-hub-backend-staging --tail 20"

echo ""
echo "=========================================="
echo "✅ Corrections terminées"
echo "=========================================="
echo ""
echo "Pour voir les logs en temps réel:"
echo "ssh -i $SSH_KEY $SSH_USER@$BACKEND_VM 'docker logs -f hotel-ticket-hub-backend-staging'"
