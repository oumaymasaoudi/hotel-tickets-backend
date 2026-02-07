# 📊 Explication des Requêtes Prometheus dans Grafana

## `up{job="staging-backend"} or vector(0)` - Explication détaillée

### 1. La métrique `up`

**Qu'est-ce que `up` ?**

`up` est une métrique **automatique** générée par Prometheus pour chaque target qu'il scrape. Elle indique si Prometheus peut se connecter à la cible.

**Valeurs possibles :**
- `up = 1` → La cible est **accessible** (UP) ✅
- `up = 0` → La cible est **inaccessible** (DOWN) ❌
- `up` n'existe pas → Prometheus n'a jamais réussi à scraper cette cible

**Exemple :**
```
up{job="staging-backend"} = 1  → Backend accessible
up{job="staging-backend"} = 0  → Backend inaccessible (erreur de connexion)
```

### 2. Le filtre `{job="staging-backend"}`

**À quoi ça sert ?**

Le filtre `{job="staging-backend"}` sélectionne uniquement la métrique `up` pour le job spécifique "staging-backend".

**Dans Prometheus, vous pouvez avoir plusieurs jobs :**
- `job="prometheus"` → Prometheus lui-même
- `job="staging-backend"` → Votre backend Spring Boot
- `job="staging-backend-node"` → Node Exporter sur la VM backend
- `job="ansible-controller"` → Node Exporter sur la VM monitoring

**Sans filtre :**
```promql
up  # Retourne up pour TOUS les jobs
```

**Avec filtre :**
```promql
up{job="staging-backend"}  # Retourne up SEULEMENT pour staging-backend
```

### 3. `or vector(0)` - La partie importante

**Pourquoi `or vector(0)` ?**

C'est une **protection contre les erreurs** dans Grafana.

**Sans `or vector(0)` :**
```promql
up{job="staging-backend"}
```

**Problèmes possibles :**
1. Si le job n'existe pas encore → **Erreur "no data"** dans Grafana
2. Si Prometheus n'a jamais scrapé cette cible → **Erreur "no data"**
3. Si la métrique n'est pas disponible → **Panneau vide/erreur**

**Avec `or vector(0)` :**
```promql
up{job="staging-backend"} or vector(0)
```

**Avantages :**
1. Si la métrique existe → Retourne la valeur réelle (0 ou 1)
2. Si la métrique n'existe pas → Retourne `0` (au lieu d'une erreur)
3. Le panneau Grafana affiche toujours quelque chose (même si c'est 0)
4. Pas d'erreur "no data" qui casse le dashboard

### 4. Exemples concrets

#### Exemple 1 : Backend accessible
```promql
up{job="staging-backend"} or vector(0)
→ Résultat: 1
→ Affichage Grafana: "UP" (vert) ✅
```

#### Exemple 2 : Backend inaccessible
```promql
up{job="staging-backend"} or vector(0)
→ Résultat: 0
→ Affichage Grafana: "DOWN" (rouge) ❌
```

#### Exemple 3 : Job n'existe pas encore
```promql
up{job="staging-backend"} or vector(0)
→ Résultat: 0 (grace à vector(0))
→ Affichage Grafana: "DOWN" (rouge) au lieu d'erreur
```

### 5. Autres exemples dans le dashboard

#### JVM Memory
```promql
jvm_memory_used_bytes{job="staging-backend",area="heap"} or vector(0)
```
- Si les métriques JVM existent → Affiche la mémoire utilisée
- Si les métriques n'existent pas → Affiche 0 (au lieu d'erreur)

#### HTTP Requests
```promql
sum(rate(http_server_requests_seconds_count{job="staging-backend"}[5m])) by (status) or vector(0)
```
- Si des requêtes HTTP ont été faites → Affiche le taux de requêtes
- Si aucune requête → Affiche 0 (au lieu d'erreur)

#### Database Connections
```promql
hikaricp_connections_active{job="staging-backend"} or vector(0)
```
- Si HikariCP expose des métriques → Affiche les connexions actives
- Si les métriques n'existent pas → Affiche 0 (au lieu d'erreur)

### 6. Pourquoi c'est important dans notre cas

**Problème initial :**
- Le backend peut ne pas être démarré
- Prometheus peut ne pas avoir encore scrapé
- Certaines métriques peuvent ne pas être disponibles immédiatement

**Sans `or vector(0)` :**
- Les panneaux affichent "No data" ❌
- Le dashboard semble cassé
- Difficile de savoir si c'est un problème ou juste "pas encore de données"

**Avec `or vector(0)` :**
- Les panneaux affichent 0 ou une valeur par défaut ✅
- Le dashboard reste fonctionnel
- On peut voir que les métriques ne sont pas encore disponibles (0 = pas de données)

### 7. Syntaxe Prometheus

**`vector(0)` :**
- Crée un vecteur avec une seule valeur : `0`
- Utilisé comme valeur par défaut

**`or` :**
- Opérateur logique "OU"
- Si la partie gauche existe → retourne la partie gauche
- Si la partie gauche n'existe pas → retourne la partie droite

**Équivalent en pseudo-code :**
```javascript
if (up{job="staging-backend"} exists) {
    return up{job="staging-backend"};
} else {
    return 0;
}
```

### 8. Alternatives

**Autre syntaxe possible :**
```promql
up{job="staging-backend"} or on() vector(0)
```
- `on()` spécifie sur quels labels faire le matching (ici aucun, donc toujours)

**Sans `or vector(0)` (non recommandé) :**
```promql
up{job="staging-backend"}
```
- Fonctionne seulement si la métrique existe
- Peut causer des erreurs dans Grafana

## Résumé

| Syntaxe | Résultat si métrique existe | Résultat si métrique n'existe pas |
|---------|------------------------------|-----------------------------------|
| `up{job="staging-backend"}` | Valeur réelle (0 ou 1) | ❌ Erreur "no data" |
| `up{job="staging-backend"} or vector(0)` | Valeur réelle (0 ou 1) | ✅ 0 (pas d'erreur) |

**Conclusion :** `or vector(0)` rend les dashboards Grafana plus robustes et évite les erreurs "no data" qui peuvent casser l'affichage.
