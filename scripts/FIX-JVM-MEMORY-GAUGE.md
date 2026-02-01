# Correction du Gauge JVM Memory Usage %

## Problème

Le gauge affiche plusieurs valeurs, dont certaines sont négatives énormes (-3255443200%, -257793600%) et une est correcte (10.1%).

## Cause

La requête retourne plusieurs séries (une par instance/label), et certaines ont des données invalides (max_bytes = 0 ou négatif).

## Solution : Requête corrigée

Dans Grafana, remplacez la requête par :

```
sum(jvm_memory_used_bytes{job="staging-backend",area="heap"}) / sum(jvm_memory_max_bytes{job="staging-backend",area="heap"}) * 100
```

Cette requête :
- Utilise `sum()` pour agréger toutes les séries
- Évite les problèmes de division par zéro
- Retourne une seule valeur

## Alternative : Filtrer les valeurs invalides

Si vous voulez garder la requête par instance, utilisez :

```
(jvm_memory_used_bytes{job="staging-backend",area="heap"} / jvm_memory_max_bytes{job="staging-backend",area="heap"}) * 100
```

Puis dans Grafana :
1. Onglet "Transform" (en bas)
2. Ajoutez une transformation "Filter data by values"
3. Filtrez pour garder seulement les valeurs entre 0 et 100

## Solution recommandée (la plus simple)

**Utilisez la requête avec `sum()`** - c'est la plus simple et la plus fiable.

