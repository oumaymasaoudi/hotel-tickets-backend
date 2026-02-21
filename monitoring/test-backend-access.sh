#!/bin/bash

# Script pour tester l'accès au backend depuis la VM monitoring

echo "🔍 Test d'accès au backend depuis la VM monitoring..."
echo ""

# Test 1: Health check
echo "1️⃣ Test Health Check:"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://13.63.15.86:8081/actuator/health
echo ""

# Test 2: Prometheus metrics
echo "2️⃣ Test Prometheus Metrics (premiers 20 lignes):"
curl -s http://13.63.15.86:8081/actuator/prometheus | head -20
echo ""

# Test 3: Vérifier si Prometheus peut scraper
echo "3️⃣ Vérification dans Prometheus:"
echo "   Allez sur: http://13.62.53.224:9090/targets"
echo "   Le target 'backend' devrait être UP (vert)"
echo ""

