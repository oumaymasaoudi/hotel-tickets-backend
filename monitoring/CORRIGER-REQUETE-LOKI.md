# 🔧 Corriger la Requête Loki dans Grafana

**Problèmes:**
1. ❌ Erreur DNS: `lookup loki on 127.0.0.11:53: server misbehaving`
2. ❌ Requête incorrecte: `{} |= '{}'` (avec guillemets)

---

## ✅ Solution 1: Redémarrer Grafana

L'erreur DNS peut être résolue en redémarrant Grafana:

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring

# Redémarrer Grafana
docker compose -f docker-compose.monitoring.yml restart grafana

# Attendre 25 secondes
sleep 25

# Vérifier
docker exec grafana curl http://loki:3100/ready
# Résultat: "ready"
```

**Puis rafraîchir la page Grafana (F5).**

---

## ✅ Solution 2: Corriger la Requête

### ❌ Requête Incorrecte (avec guillemets)
```
{} |= '{}'
```

### ✅ Requête Correcte (sans guillemets)

#### Option 1: Requête Simple (Recommandé)
```
{}
```

#### Option 2: Avec Filtre d'Erreur
```
{} |= "ERROR"
```

#### Option 3: Logs du Backend
```
{container="hotel-ticket-hub-backend-staging"}
```

---

## 📝 Instructions Pas à Pas

### 1. Utiliser l'Onglet "Code"

1. Dans Grafana Explore, **cliquez sur "Code"** (pas "Builder")
2. **Effacez** la requête actuelle `{} |= '{}'`
3. **Tapez** simplement: `{}`
4. **Cliquez sur "Run query"**

### 2. Si l'Onglet "Code" n'est pas Visible

1. **Cliquez sur "Builder"** pour le fermer
2. **Cliquez sur "Code"** pour l'ouvrir
3. **Tapez:** `{}`
4. **Run query**

---

## 🎯 Requêtes Correctes

### Voir Tous les Logs
```
{}
```

### Logs du Backend
```
{container="hotel-ticket-hub-backend-staging"}
```

### Erreurs Uniquement
```
{} |= "ERROR"
```

### Erreurs du Backend
```
{container="hotel-ticket-hub-backend-staging"} |= "ERROR"
```

**Important:** Pas de guillemets simples autour de `{}` !

---

## ✅ Checklist

- [ ] Grafana redémarré
- [ ] Page Grafana rafraîchie (F5)
- [ ] Onglet "Code" sélectionné
- [ ] Requête `{}` tapée (sans guillemets)
- [ ] "Run query" cliqué
- [ ] Logs visibles ✅

---

## 🎯 Résumé

1. **Redémarrer Grafana** pour résoudre le DNS
2. **Rafraîchir la page** (F5)
3. **Onglet "Code"** → Taper `{}` (sans guillemets)
4. **Run query** → Voir les logs ! ✅

**Ne pas utiliser le Builder - il génère des requêtes incorrectes avec des guillemets !** 🚀

---

**Dernière mise à jour:** 8 Février 2026
