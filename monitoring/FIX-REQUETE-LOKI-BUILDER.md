# 🔧 Fix: Erreur DNS et Requête Incorrecte dans Grafana

**Problèmes:**
1. ❌ Erreur DNS: `lookup loki on 127.0.0.11:53: server misbehaving`
2. ❌ Requête mal formée: `{} |= `{}`` (avec backticks)

---

## ✅ Solution 1: Redémarrer Grafana

### Problème DNS

Grafana ne peut pas résoudre "loki". Redémarrer Grafana pour recharger la résolution DNS:

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring

# Redémarrer Grafana
docker compose -f docker-compose.monitoring.yml restart grafana

# Attendre 20 secondes
sleep 20

# Vérifier la connexion
docker exec grafana curl http://loki:3100/ready
# Résultat: "ready"
```

---

## ✅ Solution 2: Corriger la Requête dans Grafana

### Problème: Backticks dans la Requête

**❌ Requête incorrecte (avec backticks):**
```
{} |= `{}`
```

**✅ Requête correcte (sans backticks):**

#### Option 1: Requête Simple (Recommandé)
```
{}
```

#### Option 2: Avec Filtre
```
{} |= "ERROR"
```

#### Option 3: Logs du Backend
```
{container="hotel-ticket-hub-backend-staging"}
```

---

## 📝 Comment Utiliser le Query Builder

### Méthode 1: Utiliser "Code" au lieu de "Builder"

1. Dans Grafana Explore, **cliquez sur l'onglet "Code"** (à côté de "Builder")
2. **Tapez directement** la requête:
   ```
   {}
   ```
3. **Cliquez sur "Run query"**

**Avantage:** Pas de problème avec les backticks !

### Méthode 2: Utiliser le Builder Correctement

1. **Label filters:**
   - **Select label:** `container`
   - **Select value:** `hotel-ticket-hub-backend-staging`
   
2. **Step 1:** Laisser vide ou ajouter un filtre "Line contains"

3. **Step 2:** Ne PAS ajouter de filtre avec backticks

4. **Cliquer sur "Run query"**

---

## 🎯 Requêtes Recommandées

### 1. Voir Tous les Logs

**Dans l'onglet "Code":**
```
{}
```

### 2. Logs du Backend

**Dans l'onglet "Code":**
```
{container="hotel-ticket-hub-backend-staging"}
```

### 3. Erreurs Uniquement

**Dans l'onglet "Code":**
```
{} |= "ERROR"
```

### 4. Erreurs du Backend

**Dans l'onglet "Code":**
```
{container="hotel-ticket-hub-backend-staging"} |= "ERROR"
```

---

## ✅ Checklist

- [ ] Grafana redémarré: `docker compose -f docker-compose.monitoring.yml restart grafana`
- [ ] Test DNS: `docker exec grafana curl http://loki:3100/ready` → "ready"
- [ ] Utiliser l'onglet "Code" dans Grafana Explore
- [ ] Requête simple: `{}` (sans backticks)
- [ ] Cliquer sur "Run query"

---

## 🎯 Résumé

1. **Redémarrer Grafana** pour résoudre le DNS
2. **Utiliser l'onglet "Code"** au lieu de "Builder"
3. **Requête simple:** `{}` (sans backticks)
4. **Run query** → Voir les logs ! ✅

**Le builder peut générer des requêtes incorrectes. Utilisez "Code" pour plus de contrôle !** 🚀

---

**Dernière mise à jour:** 8 Février 2026
