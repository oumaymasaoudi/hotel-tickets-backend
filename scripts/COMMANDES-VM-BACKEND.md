# Commandes à exécuter sur la VM Backend

## 1. Copier le docker-compose.yml corrigé

**Depuis votre machine locale (PowerShell) :**
```powershell
cd hotel-ticket-hub-backend
scp -i $env:USERPROFILE\.ssh\oumayma-key.pem docker-compose.yml ubuntu@13.63.15.86:/tmp/docker-compose.yml
```

## 2. Sur la VM Backend - Remplacer le fichier

**Connectez-vous à la VM :**
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
```

**Puis exécutez ces commandes :**
```bash
cd /opt/hotel-ticket-hub-backend-staging

# Sauvegarder l'ancien fichier (au cas où)
cp docker-compose.yml docker-compose.yml.backup

# Copier le nouveau fichier
cp /tmp/docker-compose.yml docker-compose.yml

# Vérifier que la propriété est bien là
grep "MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILES_HISTOGRAM_HTTP_SERVER_REQUESTS=true" docker-compose.yml

# Si vous voyez la ligne, c'est bon ! Redémarrer le backend
docker compose restart backend

# Attendre 30-45 secondes puis vérifier les logs
sleep 30
docker logs hotel-ticket-hub-backend-staging --tail 20 | grep -i "started"
```

## 3. Vérifier que les buckets HTTP existent

**Sur la VM Backend :**
```bash
curl -s http://localhost:8081/actuator/prometheus | grep http_server_requests_seconds_bucket | head -n 5
```

**Si vous voyez des lignes avec `..._bucket{le="..."}`, c'est bon !**

**Si vous ne voyez rien, générez du trafic :**
```bash
for i in {1..50}; do curl -s http://localhost:8081/actuator/health >/dev/null; sleep 0.2; done

# Puis retestez
curl -s http://localhost:8081/actuator/prometheus | grep http_server_requests_seconds_bucket | head -n 5
```

## Résumé des commandes (tout en une fois)

**Sur la VM Backend :**
```bash
cd /opt/hotel-ticket-hub-backend-staging
cp docker-compose.yml docker-compose.yml.backup
cp /tmp/docker-compose.yml docker-compose.yml
grep "MANAGEMENT_METRICS_DISTRIBUTION_PERCENTILES_HISTOGRAM_HTTP_SERVER_REQUESTS=true" docker-compose.yml
docker compose restart backend
sleep 45
docker logs hotel-ticket-hub-backend-staging --tail 20 | grep -i "started"
curl -s http://localhost:8081/actuator/prometheus | grep http_server_requests_seconds_bucket | head -n 5
```

