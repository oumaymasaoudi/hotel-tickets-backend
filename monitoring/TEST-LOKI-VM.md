# 🔍 Tester Loki depuis la VM Monitoring

**Problème:** `curl http://loki:3100` ne fonctionne pas depuis la VM

---

## ✅ Solution

### Depuis la VM (Host)

**Utilisez `localhost` ou `127.0.0.1`, PAS `loki`:**

```bash
# ✅ CORRECT - Depuis la VM
curl http://localhost:3100/ready
# ou
curl http://127.0.0.1:3100/ready
```

**Pourquoi ?** Le nom `loki` n'existe que dans le réseau Docker, pas sur la VM elle-même.

### Depuis un Conteneur Docker

**Utilisez `loki` (nom du conteneur):**

```bash
# ✅ CORRECT - Depuis un conteneur (ex: Grafana)
docker exec grafana curl http://loki:3100/ready
```

**Pourquoi ?** Les conteneurs Docker résolvent automatiquement les noms de conteneurs.

---

## 📝 Commandes Utiles

### Vérifier que Loki est Démarré

```bash
docker ps | grep loki
```

**Résultat attendu:**
```
CONTAINER ID   IMAGE                 ...   PORTS                    NAMES
xxx   grafana/loki:latest   ...   0.0.0.0:3100->3100/tcp   loki
```

### Tester Loki depuis la VM

```bash
# Test 1: Health check
curl http://localhost:3100/ready
# Résultat: "ready"

# Test 2: Métriques
curl http://localhost:3100/metrics | head -20

# Test 3: Labels
curl http://localhost:3100/loki/api/v1/labels
```

### Tester depuis Grafana (Conteneur)

```bash
# Test depuis Grafana
docker exec grafana curl http://loki:3100/ready
# Résultat: "ready"
```

---

## 🔍 Différence: VM vs Conteneur

| Depuis | URL à Utiliser | Pourquoi |
|--------|----------------|----------|
| **VM (Host)** | `http://localhost:3100` | Le port est mappé sur la VM |
| **Conteneur Docker** | `http://loki:3100` | DNS Docker résout le nom |

---

## ✅ Checklist

- [ ] Loki démarré: `docker ps | grep loki`
- [ ] Port 3100 ouvert: `netstat -tulpn | grep 3100`
- [ ] Test depuis VM: `curl http://localhost:3100/ready` → "ready"
- [ ] Test depuis Grafana: `docker exec grafana curl http://loki:3100/ready` → "ready"

---

## 🎯 Résumé

**Depuis la VM:**
```bash
curl http://localhost:3100/ready  # ✅
```

**Depuis un conteneur:**
```bash
docker exec grafana curl http://loki:3100/ready  # ✅
```

**Ne pas utiliser `loki` depuis la VM - ce nom n'existe que dans Docker !** 🎯

---

**Dernière mise à jour:** 8 Février 2026
