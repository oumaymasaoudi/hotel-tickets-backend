#!/bin/bash

# Script de scan de sécurité avec OWASP ZAP
# Usage: ./owasp-zap-scan.sh <target_url>

set -e

TARGET_URL=${1:-"http://localhost:8080"}
REPORT_DIR="./security-reports"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "🔒 OWASP ZAP Security Scan"
echo "Target: $TARGET_URL"
echo "Report directory: $REPORT_DIR"
echo ""

# Créer le dossier de rapports
mkdir -p "$REPORT_DIR"

# Vérifier si ZAP est installé
if ! command -v zap-cli &> /dev/null; then
    echo "⚠️  zap-cli n'est pas installé. Installation via Docker..."
    
    # Utiliser Docker si disponible
    if command -v docker &> /dev/null; then
        echo "🐳 Utilisation de OWASP ZAP via Docker..."
        
        # Démarrer ZAP en mode daemon
        docker run -d --name zap -p 8080:8080 -i owasp/zap2docker-stable zap.sh -daemon \
            -host 0.0.0.0 -port 8080 \
            -config api.disablekey=true \
            -config api.addrs.addr.name=.* \
            -config api.addrs.addr.regex=true
        
        sleep 10
        
        # Exécuter le scan baseline
        docker exec zap zap-baseline.py -t "$TARGET_URL" \
            -J "zap-report-$TIMESTAMP.json" \
            -r "zap-report-$TIMESTAMP.html" \
            -I
        
        # Copier les rapports
        docker cp zap:/zap/zap-report-$TIMESTAMP.json "$REPORT_DIR/"
        docker cp zap:/zap/zap-report-$TIMESTAMP.html "$REPORT_DIR/"
        
        # Arrêter et supprimer le conteneur
        docker stop zap
        docker rm zap
        
        echo "✅ Scan terminé. Rapports disponibles dans $REPORT_DIR/"
    else
        echo "❌ Docker n'est pas installé. Veuillez installer Docker ou zap-cli."
        exit 1
    fi
else
    # Utiliser zap-cli directement
    echo "🔍 Exécution du scan avec zap-cli..."
    
    zap-cli start
    sleep 5
    
    zap-cli quick-scan --self-contained "$TARGET_URL"
    zap-cli report -o "$REPORT_DIR/zap-report-$TIMESTAMP.html" -f html
    zap-cli report -o "$REPORT_DIR/zap-report-$TIMESTAMP.json" -f json
    
    zap-cli shutdown
    
    echo "✅ Scan terminé. Rapports disponibles dans $REPORT_DIR/"
fi

echo ""
echo "📊 Résumé :"
echo "  - Rapport HTML : $REPORT_DIR/zap-report-$TIMESTAMP.html"
echo "  - Rapport JSON : $REPORT_DIR/zap-report-$TIMESTAMP.json"

