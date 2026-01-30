#!/bin/bash

# Script pour configurer le Security Group AWS pour autoriser SSH depuis GitHub Actions
# Usage: ./configure-aws-security-group.sh <INSTANCE_ID> <REGION>

set -e

if [ $# -lt 1 ]; then
    echo "Usage: $0 <INSTANCE_ID> [REGION]"
    echo "Example: $0 i-0123456789abcdef0 us-east-1"
    exit 1
fi

INSTANCE_ID=$1
REGION=${2:-us-east-1}

echo "=========================================="
echo "Configuration du Security Group AWS"
echo "=========================================="
echo "Instance ID: $INSTANCE_ID"
echo "Region: $REGION"
echo ""

# Vérifier que AWS CLI est installé
if ! command -v aws &> /dev/null; then
    echo "ERROR: AWS CLI n'est pas installé"
    echo "Installez-le avec: https://aws.amazon.com/cli/"
    exit 1
fi

# Récupérer le Security Group ID
echo "Récupération du Security Group ID..."
SG_ID=$(aws ec2 describe-instances \
    --instance-ids "$INSTANCE_ID" \
    --region "$REGION" \
    --query 'Reservations[0].Instances[0].SecurityGroups[0].GroupId' \
    --output text 2>/dev/null || echo "")

if [ -z "$SG_ID" ]; then
    echo "ERROR: Impossible de récupérer le Security Group ID"
    echo "Vérifiez que:"
    echo "  1. L'instance ID est correct: $INSTANCE_ID"
    echo "  2. La région est correcte: $REGION"
    echo "  3. Vous avez les permissions AWS nécessaires"
    exit 1
fi

echo "Security Group ID: $SG_ID"
echo ""

# Vérifier si une règle SSH existe déjà
echo "Vérification des règles SSH existantes..."
EXISTING_RULE=$(aws ec2 describe-security-groups \
    --group-ids "$SG_ID" \
    --region "$REGION" \
    --query 'SecurityGroups[0].IpPermissions[?FromPort==`22` && ToPort==`22` && IpProtocol==`tcp`]' \
    --output json 2>/dev/null || echo "[]")

if echo "$EXISTING_RULE" | grep -q "0.0.0.0/0"; then
    echo "INFO: Une règle SSH autorisant 0.0.0.0/0 existe déjà"
    echo "Le Security Group devrait permettre les connexions depuis GitHub Actions"
else
    echo "Aucune règle SSH pour 0.0.0.0/0 trouvée"
    echo ""
    read -p "Voulez-vous ajouter une règle SSH pour autoriser les connexions depuis n'importe où (0.0.0.0/0)? (y/n) " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Ajout de la règle SSH..."
        aws ec2 authorize-security-group-ingress \
            --group-id "$SG_ID" \
            --protocol tcp \
            --port 22 \
            --cidr 0.0.0.0/0 \
            --region "$REGION" \
            --description "Allow SSH from GitHub Actions" \
            && echo "Règle SSH ajoutée avec succès!" || {
                echo "ERROR: Impossible d'ajouter la règle"
                echo "Vérifiez vos permissions AWS"
                exit 1
            }
    else
        echo "Opération annulée"
        exit 0
    fi
fi

echo ""
echo "=========================================="
echo "Configuration terminée"
echo "=========================================="
echo ""
echo "Prochaines étapes:"
echo "1. Vérifiez que les secrets GitHub sont configurés:"
echo "   - STAGING_HOST: $(aws ec2 describe-instances --instance-ids "$INSTANCE_ID" --region "$REGION" --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)"
echo "   - STAGING_USER: ubuntu"
echo "   - STAGING_SSH_PRIVATE_KEY: votre clé privée SSH"
echo ""
echo "2. Testez la connexion depuis GitHub Actions"

