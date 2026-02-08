#!/bin/bash
# Script pour activer tous les services sur toutes les VMs
# Backend, Frontend, Database, Monitoring

echo "=========================================="
echo "ACTIVATION DE TOUS LES SERVICES"
echo "=========================================="
echo "Date: $(date)"
echo ""

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# VMs
BACKEND_VM="13.63.15.86"
FRONTEND_VM="13.50.221.51"
DATABASE_VM="13.48.83.147"
MONITORING_VM="16.170.74.58"
SSH_KEY="~/.ssh/oumayma-key.pem"

PASSED=0
FAILED=0

execute_ssh() {
    local vm=$1
    local command=$2
    local description=$3
    
    echo -n "VM $vm: $description ... "
    
    if ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$vm "$command" 2>/dev/null; then
        echo -e "${GREEN}✓ OK${NC}"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC}"
        ((FAILED++))
        return 1
    fi
}

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. VM BACKEND ($BACKEND_VM)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Backend
execute_ssh $BACKEND_VM "cd ~/hotel-ticket-hub-backend && docker compose up -d --force-recreate" "Démarrer le backend"
execute_ssh $BACKEND_VM "docker ps | grep -q 'hotel-ticket-hub-backend-staging.*Up'" "Vérifier backend running"
execute_ssh $BACKEND_VM "curl -s http://localhost:8081/actuator/health | grep -q '\"status\":\"UP\"'" "Health check backend"

# Node Exporter
execute_ssh $BACKEND_VM "docker ps | grep -q 'node-exporter-backend.*Up'" "Vérifier node-exporter"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. VM FRONTEND ($FRONTEND_VM)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Frontend
execute_ssh $FRONTEND_VM "cd ~/hotel-ticket-hub && docker compose up -d --force-recreate" "Démarrer le frontend"
execute_ssh $FRONTEND_VM "docker ps | grep -q 'frontend.*Up\|hotel-ticket-hub.*Up'" "Vérifier frontend running"
execute_ssh $FRONTEND_VM "curl -s http://localhost:80 | head -1 || curl -s http://localhost:8080 | head -1" "Health check frontend"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. VM DATABASE ($DATABASE_VM)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Database
execute_ssh $DATABASE_VM "sudo systemctl is-active postgresql || docker ps | grep -q postgres" "Vérifier PostgreSQL"
execute_ssh $DATABASE_VM "sudo systemctl status postgresql --no-pager | grep -q 'active (running)' || docker ps | grep -q postgres" "PostgreSQL running"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4. VM MONITORING ($MONITORING_VM)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Monitoring - Prometheus, Grafana, Alertmanager
execute_ssh $MONITORING_VM "cd /opt/monitoring && docker compose -f docker-compose.monitoring.yml up -d --force-recreate" "Démarrer monitoring stack"
execute_ssh $MONITORING_VM "docker ps | grep -q 'prometheus.*Up'" "Vérifier Prometheus"
execute_ssh $MONITORING_VM "docker ps | grep -q 'grafana.*Up'" "Vérifier Grafana"
execute_ssh $MONITORING_VM "docker ps | grep -q 'alertmanager.*Up'" "Vérifier Alertmanager"
execute_ssh $MONITORING_VM "docker ps | grep -q 'node-exporter.*Up'" "Vérifier Node Exporter"
execute_ssh $MONITORING_VM "docker ps | grep -q 'cadvisor.*Up'" "Vérifier cAdvisor"

# Loki
execute_ssh $MONITORING_VM "cd /opt/monitoring && docker compose -f docker-compose.loki.yml up -d" "Démarrer Loki"
execute_ssh $MONITORING_VM "docker ps | grep -q 'loki.*Up'" "Vérifier Loki"
execute_ssh $MONITORING_VM "sleep 5 && curl -s http://localhost:3100/ready" "Loki ready check"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5. VÉRIFICATIONS FINALES"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Health checks
echo -n "Backend Health Check ... "
if ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$BACKEND_VM "curl -s http://localhost:8081/actuator/health | grep -q '\"status\":\"UP\"'" 2>/dev/null; then
    echo -e "${GREEN}✓ OK${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ FAIL${NC}"
    ((FAILED++))
fi

echo -n "Grafana Health Check ... "
if ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$MONITORING_VM "curl -s http://localhost:3000/api/health | grep -q 'database'" 2>/dev/null; then
    echo -e "${GREEN}✓ OK${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ FAIL${NC}"
    ((FAILED++))
fi

echo -n "Prometheus Health Check ... "
if ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$MONITORING_VM "curl -s http://localhost:9090/-/healthy | grep -q 'Healthy'" 2>/dev/null; then
    echo -e "${GREEN}✓ OK${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ FAIL${NC}"
    ((FAILED++))
fi

echo -n "Loki Health Check ... "
if ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$MONITORING_VM "curl -s http://localhost:3100/ready 2>/dev/null | grep -q 'ready'" 2>/dev/null; then
    echo -e "${GREEN}✓ OK${NC}"
    ((PASSED++))
else
    echo -e "${YELLOW}⚠ WARN${NC} (Loki peut prendre quelques secondes)"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "RÉSUMÉ"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "Services activés: ${GREEN}$PASSED${NC}"
echo -e "Échecs: ${RED}$FAILED${NC}"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "STATUT DES SERVICES"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "Backend ($BACKEND_VM):"
ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$BACKEND_VM "docker ps --format '  {{.Names}}: {{.Status}}' | grep -E 'backend|node-exporter'" 2>/dev/null || echo "  Vérification..."

echo ""
echo "Frontend ($FRONTEND_VM):"
ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$FRONTEND_VM "docker ps --format '  {{.Names}}: {{.Status}}' | head -3" 2>/dev/null || echo "  Vérification..."

echo ""
echo "Database ($DATABASE_VM):"
ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$DATABASE_VM "sudo systemctl status postgresql --no-pager | grep 'Active:' || docker ps --format '  {{.Names}}: {{.Status}}' | grep postgres" 2>/dev/null || echo "  Vérification..."

echo ""
echo "Monitoring ($MONITORING_VM):"
ssh -i $SSH_KEY -o StrictHostKeyChecking=no ubuntu@$MONITORING_VM "docker ps --format '  {{.Names}}: {{.Status}}' | grep -E 'grafana|prometheus|loki|alertmanager|node-exporter|cadvisor'" 2>/dev/null || echo "  Vérification..."

echo ""
if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ Tous les services sont activés!${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠ Certains services nécessitent une attention${NC}"
    exit 1
fi
