# ✅ Résolution Finale - Erreur Loki dans Grafana

**Erreur:** "Unable to connect with Loki. Please check the server logs for more details."

---

## 🔍 Diagnostic Complet

### Vérifications à faire

1. **Loki est démarré:**
   ```bash
   ssh ubuntu@16.170.74.58
   docker ps | grep loki
   ```

2. **Loki répond:**
   ```bash
   curl http://localhost:3100/ready
   # Résultat: "ready"
   ```

3. **Grafana peut se connecter:**
   ```bash
   docker exec grafana curl http://loki:3100/ready
   # Résultat: "ready"
   ```

4. **Loki est sur le bon réseau:**
   ```bash
   docker network inspect monitoring-network | grep loki
   ```

---

## ✅ Solution Complète

### Étape 1: Redémarrer Loki

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring
docker compose -f docker-compose.loki.yml down
docker compose -f docker-compose.loki.yml up -d
```

**Attendre 20 secondes** que Loki démarre complètement.

### Étape 2: Redémarrer Grafana

```bash
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml restart grafana
```

**Attendre 15 secondes** que Grafana redémarre.

### Étape 3: Vérifier la Connexion

```bash
# Tester depuis Grafana
docker exec grafana curl http://loki:3100/ready
# Résultat attendu: "ready"
```

### Étape 4: Tester dans Grafana

1. **Ouvrir Grafana:** http://16.170.74.58:3000
2. **Aller dans:** Connections > Data sources > Loki
3. **Cliquer sur:** "Test" (bouton bleu en bas)
4. **Résultat attendu:** "Data source is working" ✅

---

## 🔧 Si l'erreur persiste

### Vérifier les logs Grafana

```bash
docker logs grafana --tail 50 | grep -i loki
```

### Vérifier les logs Loki

```bash
docker logs loki --tail 50
```

### Vérifier la configuration

```bash
cd /opt/monitoring
cat grafana/provisioning/datasources/loki.yml
```

**Configuration attendue:**
```yaml
apiVersion: 1

datasources:
  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    isDefault: false
    jsonData:
      maxLines: 1000
    editable: false
```

---

## 📝 Note sur le Champ "Query"

Le champ "Query" dans la configuration de la datasource n'est **PAS** pour les requêtes LogQL. C'est pour les derived fields.

**Pour tester Loki:**
- Cliquer sur le bouton **"Test"** en bas de la page
- Ne pas modifier le champ "Query" dans les derived fields

---

## ✅ Checklist

- [ ] Loki est démarré: `docker ps | grep loki`
- [ ] Loki répond: `curl http://localhost:3100/ready` → "ready"
- [ ] Loki est sur le réseau: `docker network inspect monitoring-network | grep loki`
- [ ] Grafana peut se connecter: `docker exec grafana curl http://loki:3100/ready` → "ready"
- [ ] Test dans Grafana: "Data source is working" ✅

---

## 🎯 Résumé

1. **Redémarrer Loki:** `docker compose -f docker-compose.loki.yml up -d`
2. **Redémarrer Grafana:** `docker compose -f docker-compose.monitoring.yml restart grafana`
3. **Tester:** Dans Grafana, cliquer sur "Test"
4. **Résultat:** "Data source is working" ✅

---

**Dernière mise à jour:** 8 Février 2026
