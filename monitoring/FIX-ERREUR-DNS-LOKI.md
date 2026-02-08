# 🔧 Résolution: Erreur DNS Loki dans Grafana

**Erreur:** `127.0.0.11:53: server misbehaving` + "No data"

---

## 🔍 Le Problème

Deux problèmes:
1. **Erreur DNS:** Grafana ne peut pas résoudre le nom "loki"
2. **Requête incorrecte:** La requête LogQL est mal formatée

---

## ✅ Solution 1: Corriger la Requête LogQL

### ❌ Requête INCORRECTE:
```
{} |= `{container="hotel-ticket-hub-backend-staging"}`
```

### ✅ Requête CORRECTE:
```
{container="hotel-ticket-hub-backend-staging"}
```

**Dans Grafana:**
1. **Effacer complètement** la requête actuelle
2. Taper **seulement**:
   ```
   {container="hotel-ticket-hub-backend-staging"}
   ```
3. **Supprimer** toutes les autres requêtes (Query 2, Query 3)
4. Cliquer sur **Run query**

---

## ✅ Solution 2: Résoudre le Problème DNS

### Vérifier que Loki est sur le réseau

```bash
ssh ubuntu@16.170.74.58
docker network inspect monitoring-network | grep -E "loki|grafana"
```

**Si Loki n'est pas sur le réseau:**

```bash
cd /opt/monitoring
docker compose -f docker-compose.loki.yml down
docker compose -f docker-compose.loki.yml up -d
```

### Redémarrer Grafana

```bash
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml restart grafana
```

**Attendre 10 secondes**, puis retester dans Grafana.

---

## ✅ Solution 3: Vérifier la Connexion

### Tester depuis Grafana

```bash
ssh ubuntu@16.170.74.58
docker exec grafana curl http://loki:3100/ready
```

**Résultat attendu:** `ready`

**Si ça ne fonctionne pas:**
- Vérifier que Loki et Grafana sont sur le même réseau
- Redémarrer les deux services

---

## 📝 Checklist de Correction

1. **Corriger la requête LogQL:**
   - [ ] Effacer la requête actuelle
   - [ ] Taper: `{container="hotel-ticket-hub-backend-staging"}`
   - [ ] Supprimer les autres requêtes

2. **Changer la visualisation:**
   - [ ] Passer de "Time series" à **"Logs"**

3. **Vérifier la connexion:**
   - [ ] Loki est démarré: `docker ps | grep loki`
   - [ ] Grafana peut se connecter: `docker exec grafana curl http://loki:3100/ready`

4. **Redémarrer si nécessaire:**
   - [ ] Redémarrer Grafana: `docker compose restart grafana`

---

## 🎯 Résumé Simple

1. **Requête:** `{container="hotel-ticket-hub-backend-staging"}` (rien d'autre)
2. **Visualisation:** **Logs** (pas Time series)
3. **Si erreur DNS:** Redémarrer Grafana

---

**Dernière mise à jour:** 8 Février 2026
