# 📖 Guide Simple - Tester Loki dans Grafana

**Date:** 8 Février 2026

---

## 🎯 Objectif

Faire fonctionner Loki dans Grafana pour voir les logs du backend.

---

## ✅ Étape 1: Vérifier que Loki fonctionne

**Commande:**
```bash
ssh ubuntu@16.170.74.58
docker ps | grep loki
```

**Résultat attendu:**
```
loki    Up X minutes (healthy)
```

**Si Loki n'est pas là:**
```bash
cd /opt/monitoring
docker compose -f docker-compose.loki.yml up -d
```

---

## ✅ Étape 2: Tester Loki directement

**Commande:**
```bash
curl http://localhost:3100/ready
```

**Résultat attendu:**
```
ready
```

Si vous voyez "ready", Loki fonctionne ✅

---

## ✅ Étape 3: Tester dans Grafana

### 3.1 Ouvrir Grafana

1. Ouvrir votre navigateur
2. Aller sur: **http://16.170.74.58:3000**
3. Se connecter:
   - **Username:** `admin`
   - **Password:** `admin`

### 3.2 Aller dans les Data Sources

1. Cliquer sur **Connections** (menu de gauche)
2. Cliquer sur **Data sources**
3. Chercher **Loki** dans la liste
4. Cliquer sur **Loki**

### 3.3 Tester la connexion

1. Sur la page de configuration Loki, cliquer sur le bouton **"Test"** (en bas)
2. Attendre le résultat

**Résultat attendu:**
- ✅ **"Data source is working"** = Tout fonctionne !
- ❌ **"Unable to connect"** = Il y a un problème

---

## 🔧 Si ça ne fonctionne pas

### Problème 1: Loki n'est pas démarré

**Solution:**
```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring
docker compose -f docker-compose.loki.yml up -d
```

Attendre 30 secondes, puis retester dans Grafana.

### Problème 2: Grafana ne trouve pas Loki

**Solution:**
```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring

# Redémarrer Grafana
docker compose -f docker-compose.monitoring.yml restart grafana

# Attendre 10 secondes
sleep 10
```

Puis retester dans Grafana.

### Problème 3: L'erreur persiste

**Vérifier les logs:**
```bash
ssh ubuntu@16.170.74.58
docker logs loki --tail 20
docker logs grafana --tail 20 | grep -i loki
```

---

## 📊 Utiliser Loki dans Grafana

### Créer un dashboard de logs

1. Dans Grafana, cliquer sur **Dashboards** (menu de gauche)
2. Cliquer sur **New dashboard**
3. Cliquer sur **Add visualization**
4. Sélectionner **Loki** comme datasource
5. Dans la requête, taper:
   ```
   {container="hotel-ticket-hub-backend-staging"}
   ```
6. Cliquer sur **Run query**

**Vous devriez voir les logs du backend !** ✅

---

## 🎯 Résumé Simple

1. **Loki doit être démarré** → `docker ps | grep loki`
2. **Loki doit répondre** → `curl http://localhost:3100/ready` → "ready"
3. **Dans Grafana** → Connections > Data sources > Loki > Test
4. **Si "Data source is working"** → C'est bon ! ✅

---

## ❓ Questions Fréquentes

**Q: Pourquoi je vois "Unable to connect"?**
R: Loki n'est probablement pas démarré ou Grafana ne peut pas le trouver. Redémarrer les deux services.

**Q: Comment savoir si Loki fonctionne?**
R: Taper `curl http://localhost:3100/ready` sur la VM. Si ça répond "ready", ça fonctionne.

**Q: Je ne vois pas de logs dans Grafana?**
R: Vérifier que Promtail collecte les logs: `docker ps | grep promtail`

---

**Dernière mise à jour:** 8 Février 2026
