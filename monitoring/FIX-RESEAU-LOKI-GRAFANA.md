# 🔧 Fix: Erreur DNS "lookup loki" dans Grafana

**Problème:** `dial tcp: lookup loki on 127.0.0.11:53: server misbehaving`

**Cause:** Grafana et Loki ne sont pas sur le même réseau Docker.

---

## ✅ Solution Rapide

### Étape 1: Connecter Loki au Réseau Monitoring

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring

# Connecter Loki au réseau monitoring-network
docker network connect monitoring-network loki
```

### Étape 2: Redémarrer Grafana

```bash
docker compose -f docker-compose.monitoring.yml restart grafana
```

**Attendre 15 secondes** que Grafana redémarre.

### Étape 3: Vérifier

```bash
# Tester la connexion depuis Grafana
docker exec grafana curl http://loki:3100/ready
# Résultat: "ready"
```

---

## 🔧 Solution Permanente

### Modifier docker-compose.loki.yml

Ajouter le réseau `monitoring-network` à Loki:

```yaml
services:
  loki:
    # ... autres configurations ...
    networks:
      - monitoring-network

networks:
  monitoring-network:
    external: true
```

Puis redémarrer:

```bash
cd /opt/monitoring
docker compose -f docker-compose.loki.yml down
docker compose -f docker-compose.loki.yml up -d
```

---

## 📝 Correction de la Requête LogQL

**Erreur dans la requête:** `{} |= `{job="varlogs"}`

**Correction:** Enlever les backticks et utiliser:

```
{job="varlogs"}
```

**Ou simplement:**

```
{}
```

Pour voir tous les logs.

---

## ✅ Checklist

- [ ] Loki connecté au réseau `monitoring-network`
- [ ] Grafana redémarré
- [ ] Test de connexion: `docker exec grafana curl http://loki:3100/ready` → "ready"
- [ ] Requête LogQL corrigée: `{job="varlogs"}` (sans backticks)

---

## 🎯 Résumé

1. **Connecter Loki au réseau:** `docker network connect monitoring-network loki`
2. **Redémarrer Grafana:** `docker compose -f docker-compose.monitoring.yml restart grafana`
3. **Corriger la requête:** `{job="varlogs"}` (sans backticks)

**C'est tout !** ✅

---

**Dernière mise à jour:** 8 Février 2026
