# 📖 Comment Utiliser Loki dans Grafana

**Date:** 8 Février 2026

---

## ✅ C'est Normal !

Le message **"Provisioned data source"** est **normal**. Cela signifie que Loki a été configuré automatiquement via les fichiers de configuration. C'est une bonne chose !

**Vous ne pouvez pas modifier cette datasource via l'interface**, mais vous pouvez l'utiliser pour créer des dashboards.

---

## 🎯 Tester la Connexion

### Option 1: Via le bouton "Test"

1. Sur la page de configuration Loki, **descendre tout en bas**
2. Cliquer sur le bouton bleu **"Test"**
3. Attendre le résultat

**Résultat attendu:**
- ✅ **"Data source is working"** = Tout fonctionne !
- ❌ **"Unable to connect"** = Il y a un problème

### Option 2: Créer un Dashboard

Si le test fonctionne, vous pouvez directement créer un dashboard pour voir les logs.

---

## 📊 Créer un Dashboard de Logs

### Étape 1: Créer un Nouveau Dashboard

1. Cliquer sur **Dashboards** (menu de gauche)
2. Cliquer sur **New dashboard**
3. Cliquer sur **Add visualization**

### Étape 2: Sélectionner Loki

1. Dans la section "Queries", sélectionner **Loki** comme datasource
2. Dans le champ de requête, taper:
   ```
   {container="hotel-ticket-hub-backend-staging"}
   ```

### Étape 3: Changer la Visualisation

1. Dans le panneau de droite, section **Visualization**
2. Changer de **Time series** à **Logs**
3. Cliquer sur **Run query**

**Vous devriez voir les logs du backend !** ✅

---

## 📝 Requêtes LogQL Utiles

### Tous les logs
```
{container="hotel-ticket-hub-backend-staging"}
```

### Seulement les erreurs
```
{container="hotel-ticket-hub-backend-staging"} |= "ERROR"
```

### Logs avec filtre JSON
```
{container="hotel-ticket-hub-backend-staging"} | json | level="ERROR"
```

### Comptage d'erreurs
```
sum(count_over_time({container="hotel-ticket-hub-backend-staging"} |= "ERROR" [5m]))
```

---

## ❓ Questions Fréquentes

**Q: Pourquoi je ne peux pas modifier la datasource?**
R: C'est normal ! Elle est "Provisioned" (configurée automatiquement). C'est une bonne pratique pour éviter les modifications accidentelles.

**Q: Comment savoir si Loki fonctionne?**
R: Cliquer sur le bouton "Test" en bas de la page. Si vous voyez "Data source is working", c'est bon !

**Q: Je ne vois pas de logs?**
R: Vérifier que:
1. Le backend est démarré: `docker ps | grep backend`
2. Promtail collecte les logs: `docker ps | grep promtail`
3. La requête est correcte: `{container="hotel-ticket-hub-backend-staging"}`

---

## ✅ Checklist

- [ ] Loki datasource visible dans Grafana
- [ ] Message "Provisioned data source" (normal)
- [ ] Test de connexion réussi: "Data source is working"
- [ ] Dashboard de logs créé
- [ ] Logs visibles dans le dashboard

---

## 🎯 Résumé

1. **C'est normal** que la datasource soit "Provisioned"
2. **Tester** avec le bouton "Test" en bas
3. **Créer un dashboard** pour voir les logs
4. **Utiliser** la requête: `{container="hotel-ticket-hub-backend-staging"}`

**Tout est prêt ! Vous pouvez maintenant créer vos dashboards de logs.** ✅

---

**Dernière mise à jour:** 8 Février 2026
