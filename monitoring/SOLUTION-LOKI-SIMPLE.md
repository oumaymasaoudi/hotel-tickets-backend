# ✅ Solution Simple - Erreur Loki dans Grafana

**Problème:** "Unable to connect with Loki" dans Grafana

---

## 🔍 Le Problème

Loki n'est pas sur le même réseau Docker que Grafana. Ils ne peuvent pas se parler.

---

## ✅ La Solution (3 étapes)

### Étape 1: Se connecter à la VM

```bash
ssh ubuntu@16.170.74.58
```

### Étape 2: Redémarrer Loki

```bash
cd /opt/monitoring
docker compose -f docker-compose.loki.yml down
docker compose -f docker-compose.loki.yml up -d
```

**Attendre 15 secondes** que Loki démarre.

### Étape 3: Vérifier

```bash
# Vérifier que Loki est démarré
docker ps | grep loki

# Tester Loki
curl http://localhost:3100/ready
# Résultat: "ready" ✅
```

---

## 🎯 Tester dans Grafana

1. **Ouvrir Grafana:** http://16.170.74.58:3000
2. **Aller dans:** Connections > Data sources > Loki
3. **Cliquer sur:** "Test" (bouton en bas)
4. **Résultat attendu:** "Data source is working" ✅

---

## ✅ C'est tout !

Si vous voyez "Data source is working", c'est bon ! Vous pouvez maintenant créer des dashboards de logs.

---

**Dernière mise à jour:** 8 Février 2026
