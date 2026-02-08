# 📝 Comment Utiliser Grafana Explore avec Loki

**Question:** "J'écris quoi ? C'est vide ici ou comment ?"

---

## ✅ Solution Simple: Utiliser l'Onglet "Code"

### Étape 1: Cliquer sur "Code"

Dans Grafana Explore, **cliquez sur l'onglet "Code"** (à côté de "Builder" en haut).

**Pourquoi ?** Le "Builder" peut être compliqué. L'onglet "Code" est plus simple et direct.

---

## ✅ Étape 2: Taper la Requête

Dans le champ de texte, **tapez simplement:**

```
{}
```

**C'est tout !** Cette requête affiche **tous les logs**.

---

## ✅ Étape 3: Cliquer sur "Run query"

Cliquez sur le bouton **"Run query"** (ou appuyez sur Entrée).

**Résultat:** Vous verrez tous les logs collectés par Promtail !

---

## 📝 Autres Requêtes Utiles

### 1. Voir Tous les Logs
```
{}
```

### 2. Logs du Backend
```
{container="hotel-ticket-hub-backend-staging"}
```

### 3. Erreurs Uniquement
```
{} |= "ERROR"
```

### 4. Erreurs du Backend
```
{container="hotel-ticket-hub-backend-staging"} |= "ERROR"
```

### 5. Logs par Service
```
{service="hotel-ticket-hub-backend-staging"}
```

---

## 🎯 Instructions Visuelles

1. **Ouvrir Grafana:** http://16.170.74.58:3000
2. **Menu de gauche** → **Explore** (icône boussole)
3. **En haut à gauche:** Sélectionner **"Loki"** comme datasource
4. **En haut au centre:** Cliquer sur **"Code"** (pas "Builder")
5. **Dans le champ de texte:** Taper `{}`
6. **Cliquer sur "Run query"** (ou Entrée)

**C'est tout !** ✅

---

## ❌ Si le Builder est Utilisé

Si vous êtes dans le "Builder" et que c'est vide:

1. **Cliquez sur "Code"** (onglet à côté de "Builder")
2. **Tapez:** `{}`
3. **Run query**

**Ne pas utiliser le Builder** - il peut générer des erreurs avec les backticks.

---

## 🔍 Explication des Requêtes

### `{}` - Tous les logs
- Affiche **tous les logs** collectés par Promtail
- Pas de filtre

### `{container="hotel-ticket-hub-backend-staging"}` - Logs du backend
- Affiche **uniquement les logs** du conteneur backend
- Filtre par nom de conteneur

### `{} |= "ERROR"` - Erreurs
- Affiche **tous les logs** qui contiennent le mot "ERROR"
- Filtre par contenu

---

## ✅ Checklist

- [ ] Grafana Explore ouvert
- [ ] Loki sélectionné comme datasource
- [ ] Onglet "Code" sélectionné (pas "Builder")
- [ ] Requête `{}` tapée
- [ ] "Run query" cliqué
- [ ] Logs visibles ✅

---

## 🎯 Résumé

**Étape par étape:**

1. **Explore** → **Loki** → **"Code"**
2. **Taper:** `{}`
3. **Run query**
4. **Voir les logs !** 🚀

**C'est aussi simple que ça !** Pas besoin de remplir le Builder. ✅

---

**Dernière mise à jour:** 8 Février 2026
