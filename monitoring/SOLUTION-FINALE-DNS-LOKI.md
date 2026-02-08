# ✅ Solution Finale: Erreur DNS Loki dans Grafana

**Problème:** `lookup loki on 127.0.0.11:53: server misbehaving`

**Cause:** Loki n'est pas connecté au réseau `monitoring-network` où se trouve Grafana.

---

## ✅ Solution Définitive

### Étape 1: Connecter Loki au Réseau

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring

# Connecter Loki au réseau monitoring-network
docker network connect monitoring-network loki
```

### Étape 2: Vérifier

```bash
# Vérifier que Loki est sur le réseau
docker network inspect monitoring-network --format '{{range .Containers}}{{.Name}} {{end}}'
# Résultat doit inclure: loki grafana prometheus ...

# Tester la connexion depuis Grafana
docker exec grafana curl http://loki:3100/ready
# Résultat: "ready"
```

### Étape 3: Redémarrer Grafana

```bash
docker compose -f docker-compose.monitoring.yml restart grafana
sleep 20
```

---

## 🔧 Solution Permanente

### Modifier docker-compose.loki.yml

Le fichier est déjà correct, mais si Loki n'est pas démarré avec le bon réseau:

```bash
cd /opt/monitoring

# Arrêter Loki
docker compose -f docker-compose.loki.yml down

# Redémarrer Loki (il se connectera automatiquement au réseau)
docker compose -f docker-compose.loki.yml up -d
```

**Le fichier `docker-compose.loki.yml` contient déjà:**
```yaml
networks:
  monitoring-network:
    external: true
    name: monitoring-network
```

---

## 📝 Utiliser Grafana Explore

### 1. Accéder à Explore

1. **Grafana** → **Explore** (icône boussole)
2. **Sélectionner Loki** (en haut à gauche)

### 2. Utiliser l'Onglet "Code" (Recommandé)

**Cliquez sur "Code"** (à côté de "Builder") et tapez:

```
{}
```

**Puis cliquez sur "Run query"**

### 3. Requêtes Utiles

#### Tous les logs
```
{}
```

#### Logs du backend
```
{container="hotel-ticket-hub-backend-staging"}
```

#### Erreurs uniquement
```
{} |= "ERROR"
```

#### Erreurs du backend
```
{container="hotel-ticket-hub-backend-staging"} |= "ERROR"
```

---

## ✅ Checklist

- [ ] Loki connecté au réseau: `docker network connect monitoring-network loki`
- [ ] Loki visible sur le réseau: `docker network inspect monitoring-network` → voir "loki"
- [ ] Test DNS: `docker exec grafana curl http://loki:3100/ready` → "ready"
- [ ] Grafana redémarré
- [ ] Test dans Grafana Explore avec requête `{}` → Voir les logs ✅

---

## 🎯 Résumé

1. **Connecter Loki au réseau:** `docker network connect monitoring-network loki`
2. **Redémarrer Grafana:** `docker compose -f docker-compose.monitoring.yml restart grafana`
3. **Utiliser "Code" dans Explore:** Requête `{}`
4. **Run query** → Voir les logs ! ✅

**C'est tout !** 🚀

---

**Dernière mise à jour:** 8 Février 2026
