# ✅ Solution Définitive - Erreur Loki dans Grafana

**Erreur:** "Unable to connect with Loki. Please check the server logs for more details."

---

## 🔍 Diagnostic

### Vérifications à Faire

```bash
ssh ubuntu@16.170.74.58

# 1. Loki est démarré?
docker ps | grep loki

# 2. Loki répond?
curl http://localhost:3100/ready
# Résultat: "ready"

# 3. Grafana peut se connecter?
docker exec grafana curl http://loki:3100/ready
# Résultat: "ready"

# 4. Loki est sur le réseau?
docker network inspect monitoring-network | grep loki
```

---

## ✅ Solution Complète (Étape par Étape)

### Étape 1: Redémarrer Loki

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring
docker compose -f docker-compose.loki.yml down
docker compose -f docker-compose.loki.yml up -d
```

**Attendre 20 secondes** que Loki démarre complètement.

### Étape 2: Vérifier Loki

```bash
# Vérifier que Loki est démarré
docker ps | grep loki

# Tester Loki
curl http://localhost:3100/ready
# Résultat attendu: "ready"
```

### Étape 3: Redémarrer Grafana

```bash
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml restart grafana
```

**Attendre 15 secondes** que Grafana redémarre.

### Étape 4: Vérifier la Connexion

```bash
# Tester depuis Grafana
docker exec grafana curl http://loki:3100/ready
# Résultat attendu: "ready"
```

### Étape 5: Tester dans Grafana

1. **Rafraîchir la page** Grafana (F5)
2. Aller dans: **Connections > Data sources > Loki**
3. **Descendre en bas** de la page
4. Cliquer sur le bouton bleu **"Test"**
5. Attendre le résultat

**Résultat attendu:** "Data source is working" ✅

---

## 🔧 Si l'Erreur Persiste

### Vérifier les Logs

```bash
# Logs Loki
docker logs loki --tail 50

# Logs Grafana
docker logs grafana --tail 50 | grep -i loki
```

### Vérifier le Réseau

```bash
# Vérifier que Loki et Grafana sont sur le même réseau
docker network inspect monitoring-network --format '{{range .Containers}}{{.Name}} {{end}}'
# Résultat attendu: doit contenir "loki" et "grafana"
```

### Solution Alternative: Utiliser l'IP

Si le DNS ne fonctionne pas, vous pouvez modifier temporairement la configuration:

```bash
cd /opt/monitoring
# Obtenir l'IP de Loki
docker inspect loki | grep IPAddress

# Modifier la configuration Grafana (temporaire)
# Remplacer http://loki:3100 par http://<IP_LOKI>:3100
```

**Mais normalement, le nom "loki" devrait fonctionner.**

---

## 📝 Script Automatique

```bash
#!/bin/bash
# Script pour résoudre le problème Loki

cd /opt/monitoring

echo "1. Arrêt de Loki..."
docker compose -f docker-compose.loki.yml down

echo "2. Démarrage de Loki..."
docker compose -f docker-compose.loki.yml up -d

echo "3. Attente (20s)..."
sleep 20

echo "4. Vérification Loki..."
curl http://localhost:3100/ready && echo " - Loki OK" || echo " - Loki KO"

echo "5. Redémarrage Grafana..."
docker compose -f docker-compose.monitoring.yml restart grafana

echo "6. Attente (15s)..."
sleep 15

echo "7. Test connexion..."
docker exec grafana curl http://loki:3100/ready && echo " - Connexion OK" || echo " - Connexion KO"

echo "✅ Terminé! Testez maintenant dans Grafana."
```

---

## ✅ Checklist Finale

- [ ] Loki est démarré: `docker ps | grep loki`
- [ ] Loki répond: `curl http://localhost:3100/ready` → "ready"
- [ ] Loki est sur le réseau: `docker network inspect monitoring-network | grep loki`
- [ ] Grafana peut se connecter: `docker exec grafana curl http://loki:3100/ready` → "ready"
- [ ] Test dans Grafana: "Data source is working" ✅

---

## 🎯 Résumé

1. **Redémarrer Loki:** `docker compose -f docker-compose.loki.yml up -d`
2. **Attendre 20 secondes**
3. **Redémarrer Grafana:** `docker compose -f docker-compose.monitoring.yml restart grafana`
4. **Attendre 15 secondes**
5. **Rafraîchir Grafana** (F5)
6. **Tester** avec le bouton "Test"

**Si tout est OK, vous devriez voir "Data source is working" !** ✅

---

**Dernière mise à jour:** 8 Février 2026
