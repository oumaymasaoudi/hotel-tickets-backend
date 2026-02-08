# 📊 Guide d'Utilisation de Loki dans Grafana

**Status:** ✅ Loki est connecté et fonctionnel !

---

## 🎯 Prochaines Étapes

### Option 1: Explorer les Logs (Explore View)

1. **Dans Grafana**, cliquez sur **"Explore view"** (lien bleu dans le message de succès)
   - Ou allez dans le menu de gauche → **Explore** (icône boussole)
2. **Sélectionner Loki** comme datasource (en haut à gauche)
3. **Faire une requête LogQL** simple:
   ```
   {job="varlogs"}
   ```
   - Cela affiche tous les logs du système
4. **Cliquer sur "Run query"** pour voir les logs

### Option 2: Créer un Dashboard

1. **Dans Grafana**, cliquez sur **"building a dashboard"** (lien bleu)
   - Ou allez dans le menu → **Dashboards** → **New Dashboard**
2. **Ajouter un panel** → **Add visualization**
3. **Sélectionner Loki** comme datasource
4. **Créer une requête LogQL** pour visualiser les logs

---

## 📝 Requêtes LogQL Utiles

### Voir tous les logs
```
{job="varlogs"}
```

### Filtrer par conteneur Docker
```
{container_name="hotel-ticket-hub-backend-staging"}
```

### Filtrer par niveau (ERROR, WARN, INFO)
```
{job="varlogs"} |= "ERROR"
```

### Filtrer par application
```
{job="varlogs"} |= "hotel-ticket-hub"
```

### Compter les logs par niveau
```
sum(count_over_time({job="varlogs"}[5m])) by (level)
```

---

## 🔍 Requêtes pour le Backend

### Logs du Backend Spring Boot
```
{container_name="hotel-ticket-hub-backend-staging"}
```

### Erreurs du Backend
```
{container_name="hotel-ticket-hub-backend-staging"} |= "ERROR"
```

### Logs d'authentification
```
{container_name="hotel-ticket-hub-backend-staging"} |= "authentication"
```

### Logs de tickets
```
{container_name="hotel-ticket-hub-backend-staging"} |= "ticket"
```

---

## 📊 Créer un Dashboard de Monitoring

### Panel 1: Nombre de logs par minute
```
sum(count_over_time({job="varlogs"}[1m]))
```

### Panel 2: Erreurs par minute
```
sum(count_over_time({job="varlogs"} |= "ERROR" [1m]))
```

### Panel 3: Logs du backend
```
{container_name="hotel-ticket-hub-backend-staging"}
```

### Panel 4: Top 10 des erreurs
```
topk(10, sum(count_over_time({job="varlogs"} |= "ERROR" [5m])) by (message))
```

---

## ✅ Checklist

- [x] Loki connecté: "Data source successfully connected" ✅
- [ ] Testé Explore view avec une requête simple
- [ ] Créé un dashboard de base
- [ ] Configuré des requêtes pour le backend

---

## 🎯 Résumé

**Loki fonctionne !** Vous pouvez maintenant:
1. **Explorer les logs** via Explore view
2. **Créer des dashboards** pour visualiser les logs
3. **Faire des requêtes LogQL** pour filtrer et analyser

**Commencez par Explore view pour voir les logs en temps réel !** 🚀

---

**Dernière mise à jour:** 8 Février 2026
