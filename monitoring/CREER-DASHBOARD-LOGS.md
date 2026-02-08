# 📊 Créer un Dashboard de Logs dans Grafana

**Date:** 8 Février 2026

---

## 🎯 Objectif

Créer un dashboard pour voir les logs du backend dans Grafana.

---

## ✅ Étape par Étape

### 1. Ouvrir Grafana

1. Aller sur **http://16.170.74.58:3000**
2. Se connecter (admin/admin)

### 2. Créer un Nouveau Dashboard

1. Cliquer sur **Dashboards** (menu de gauche)
2. Cliquer sur **New dashboard**
3. Cliquer sur **Add visualization**

### 3. Configurer la Requête LogQL

**IMPORTANT:** La requête doit être correcte !

#### ❌ Requête INCORRECTE (erreur):
```
{container="hotel-ticket-hub-backend-staging"} |= `{container="hotel-ticket-hub-backend-staging"}`
```

#### ✅ Requête CORRECTE (simple):
```
{container="hotel-ticket-hub-backend-staging"}
```

**Comment faire:**
1. Dans la section "Queries", sélectionner **Loki** comme datasource
2. Dans le champ de requête, taper **exactement**:
   ```
   {container="hotel-ticket-hub-backend-staging"}
   ```
3. **Ne pas ajouter** `|=` ou d'autres opérateurs si vous voulez juste voir tous les logs

### 4. Changer le Type de Visualisation

**Pour voir les logs:**
1. Dans le panneau de droite, section **Visualization**
2. Changer de **Time series** à **Logs**
3. Les logs devraient apparaître !

### 5. Sauvegarder

1. Cliquer sur **Save dashboard** (en haut à droite)
2. Donner un nom au dashboard (ex: "Backend Logs")
3. Cliquer sur **Save**

---

## 📝 Exemples de Requêtes LogQL

### Tous les logs du backend
```
{container="hotel-ticket-hub-backend-staging"}
```

### Seulement les erreurs
```
{container="hotel-ticket-hub-backend-staging"} |= "ERROR"
```

### Erreurs avec filtre
```
{container="hotel-ticket-hub-backend-staging"} |= "ERROR" | json
```

### Logs par niveau
```
{container="hotel-ticket-hub-backend-staging"} | json | level="ERROR"
```

---

## 🔧 Résoudre l'Erreur "parse error"

### Erreur:
```
parse error: queries require at least one regexp or equality matcher...
```

### Solution:
1. **Effacer complètement** la requête
2. Taper **seulement**:
   ```
   {container="hotel-ticket-hub-backend-staging"}
   ```
3. **Ne pas ajouter** `|=` ou autres opérateurs
4. Cliquer sur **Run query**

---

## ✅ Checklist

- [ ] Loki datasource configurée et testée
- [ ] Nouveau dashboard créé
- [ ] Requête LogQL correcte: `{container="hotel-ticket-hub-backend-staging"}`
- [ ] Type de visualisation: **Logs** (pas Time series)
- [ ] Logs visibles dans le dashboard
- [ ] Dashboard sauvegardé

---

## 🎯 Résumé Simple

1. **Nouveau dashboard** → Add visualization
2. **Requête:** `{container="hotel-ticket-hub-backend-staging"}`
3. **Visualisation:** Changer en **Logs**
4. **Sauvegarder**

**C'est tout !** ✅

---

**Dernière mise à jour:** 8 Février 2026
