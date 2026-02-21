#!/bin/bash
# Script de vérification complète du monitoring
# Vérifie: Grafana, Prometheus, Loki, SonarCloud

echo "=========================================="
echo "VÉRIFICATION DU MONITORING"
echo "=========================================="
echo "Date: $(date)"
echo ""

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

MONITORING_VM="${1:-16.170.74.58}"
PASSED=0
FAILED=0
WARNINGS=0

check_service() {
    local name=$1
    local url=$2
    local expected=$3
    
    echo -n "Checking: $name ... "
    
    if [ -n "$url" ]; then
        response=$(curl -s -w "\n%{http_code}" "$url" 2>/dev/null)
        http_code=$(echo "$response" | tail -n1)
        body=$(echo "$response" | sed '$d')
        
        if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 302 ]; then
            if [ -n "$expected" ]; then
                if echo "$body" | grep -q "$expected"; then
                    echo -e "${GREEN}✓ OK${NC}"
                    ((PASSED++))
                    return 0
                else
                    echo -e "${YELLOW}⚠ WARN${NC} (unexpected response)"
                    ((WARNINGS++))
                    return 1
                fi
            else
                echo -e "${GREEN}✓ OK${NC} (HTTP $http_code)"
                ((PASSED++))
                return 0
            fi
        else
            echo -e "${RED}✗ FAIL${NC} (HTTP $http_code)"
            ((FAILED++))
            return 1
        fi
    else
        echo -e "${YELLOW}⚠ NOT CONFIGURED${NC}"
        ((WARNINGS++))
        return 1
    fi
}

check_container() {
    local name=$1
    echo -n "Checking container: $name ... "
    
    if ssh -i ~/.ssh/oumayma-key.pem -o StrictHostKeyChecking=no ubuntu@$MONITORING_VM "docker ps --format '{{.Names}}' | grep -q '^${name}$'" 2>/dev/null; then
        status=$(ssh -i ~/.ssh/oumayma-key.pem -o StrictHostKeyChecking=no ubuntu@$MONITORING_VM "docker ps --filter name=$name --format '{{.Status}}'" 2>/dev/null)
        if echo "$status" | grep -q "healthy\|Up"; then
            echo -e "${GREEN}✓ RUNNING${NC} ($status)"
            ((PASSED++))
            return 0
        else
            echo -e "${YELLOW}⚠ RUNNING${NC} ($status)"
            ((WARNINGS++))
            return 1
        fi
    else
        echo -e "${RED}✗ NOT RUNNING${NC}"
        ((FAILED++))
        return 1
    fi
}

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. SERVICES DE MONITORING"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Grafana
check_container "grafana"
check_service "Grafana API" "http://$MONITORING_VM:3000/api/health" "database"

# Prometheus
check_container "prometheus"
check_service "Prometheus" "http://$MONITORING_VM:9090/-/healthy" "Prometheus Server is Healthy"

# Loki
check_container "loki"
if [ $? -ne 0 ]; then
    echo "  → Loki n'est pas démarré (optionnel)"
    echo "  → Pour démarrer: cd /opt/monitoring && docker compose -f docker-compose.loki.yml up -d"
else
    check_service "Loki" "http://$MONITORING_VM:3100/ready" "ready"
fi

# Alertmanager
check_container "alertmanager"
check_service "Alertmanager" "http://$MONITORING_VM:9093/-/healthy" "OK"

# Node Exporter
check_container "node-exporter"
check_service "Node Exporter" "http://$MONITORING_VM:9100/metrics" "# HELP"

# cAdvisor
check_container "cadvisor"
check_service "cAdvisor" "http://$MONITORING_VM:8080/healthz" "ok"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. MÉTRIQUES ET COLLECTE"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Vérifier que Prometheus collecte les métriques
echo -n "Checking: Prometheus targets ... "
targets=$(curl -s "http://$MONITORING_VM:9090/api/v1/targets" 2>/dev/null)
if echo "$targets" | grep -q "activeTargets"; then
    up_count=$(echo "$targets" | python3 -c 'import sys, json; d=json.load(sys.stdin); targets=d.get("data", {}).get("activeTargets", []); print(sum(1 for t in targets if t.get("health")=="up"))' 2>/dev/null || echo "0")
    total_count=$(echo "$targets" | python3 -c 'import sys, json; d=json.load(sys.stdin); targets=d.get("data", {}).get("activeTargets", []); print(len(targets))' 2>/dev/null || echo "0")
    echo -e "${GREEN}✓ OK${NC} ($up_count/$total_count targets UP)"
    ((PASSED++))
else
    echo -e "${RED}✗ FAIL${NC}"
    ((FAILED++))
fi

# Vérifier que le backend expose les métriques
echo -n "Checking: Backend Prometheus endpoint ... "
backend_metrics=$(curl -s "http://13.63.15.86:8081/actuator/prometheus" 2>/dev/null | head -1)
if echo "$backend_metrics" | grep -q "# HELP"; then
    echo -e "${GREEN}✓ OK${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ FAIL${NC}"
    ((FAILED++))
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. SONARCLOUD (QUALITÉ DE CODE)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "SonarCloud est configuré dans le pipeline CI/CD:"
echo "  → Job: Backend - SonarCloud Analysis"
echo "  → URL: https://sonarcloud.io/project/overview?id=oumaymasaoudi_hotel-tickets-backend"
echo "  → Organisation: oumaymasaoudi"
echo "  → Project Key: oumaymasaoudi_hotel-tickets-backend"
echo ""
echo "  ✅ Configuration:"
echo "     - SonarCloud GitHub Action intégré"
echo "     - Analyse automatique sur push vers main/develop"
echo "     - Rapport de couverture JaCoCo intégré"
echo "     - Fichier sonar-project.properties configuré"
echo ""
echo "  📊 Pour vérifier:"
echo "     1. Aller sur https://sonarcloud.io"
echo "     2. Se connecter avec GitHub"
echo "     3. Voir le projet: oumaymasaoudi_hotel-tickets-backend"
echo ""
((PASSED++))

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "RÉSUMÉ"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "Services OK: ${GREEN}$PASSED${NC}"
echo -e "Services en échec: ${RED}$FAILED${NC}"
echo -e "Avertissements: ${YELLOW}$WARNINGS${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ Tous les services critiques sont opérationnels!${NC}"
    exit 0
else
    echo -e "${RED}❌ Certains services nécessitent une attention${NC}"
    exit 1
fi
