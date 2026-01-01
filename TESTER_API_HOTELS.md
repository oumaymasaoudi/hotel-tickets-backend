# 🧪 Tester l'API des Hôtels

## ✅ Les Hôtels Existent dans la Base de Données

Vous avez 2 hôtels actifs :
1. "Hôtel de Test" - test@hotel.com
2. "lyon" - oumayma@gmail.com

## 🔍 Tester l'API

### Depuis la VM Backend

```powershell
ssh -i github-actions-key ubuntu@13.49.44.219
```

```bash
# Tester l'endpoint public
curl http://localhost:8081/api/hotels/public

# Ou avec formatage JSON
curl http://localhost:8081/api/hotels/public | python3 -m json.tool
```

**Résultat attendu** : Une liste JSON avec les 2 hôtels.

### Depuis votre Machine Locale

```powershell
# Tester depuis votre navigateur ou PowerShell
curl http://13.49.44.219:8081/api/hotels/public
```

---

## ⚠️ Si l'API ne Retourne Rien

### Vérifier les Logs du Backend

```bash
# Sur la VM backend
docker logs hotel-ticket-hub-backend-staging --tail=50 | grep -i hotel
```

### Vérifier CORS

L'endpoint `/api/hotels/public` doit autoriser les requêtes depuis `http://51.21.196.104`.

Vérifiez que `http://51.21.196.104` est dans la liste CORS du backend.

---

## 🔧 Solution : Vérifier le Frontend

Ouvrez la console du navigateur (F12) sur http://51.21.196.104/signup et vérifiez :

1. **Onglet Network** : Cherchez la requête vers `/api/hotels/public`
2. **Vérifiez la réponse** : Est-ce que l'API retourne les hôtels ?
3. **Vérifiez les erreurs** : Y a-t-il des erreurs CORS ou autres ?

---

## ✅ Si l'API Fonctionne mais le Frontend ne les Affiche Pas

Le problème peut venir du frontend. Vérifiez :
- La console du navigateur pour les erreurs
- Le code qui filtre les hôtels (peut-être un filtre par `is_active` côté frontend)

