# ✅ Solution Finale Simple - Loki dans Grafana

**Problème:** "Unable to connect with Loki" dans Grafana

---

## 🎯 Le Message "Provisioned data source" est NORMAL !

Ce message signifie que Loki est configuré automatiquement. **C'est une bonne chose !** Vous ne pouvez pas modifier la datasource via l'UI, mais vous pouvez l'utiliser.

---

## ✅ Solution en 3 Étapes

### Étape 1: Nettoyer l'Espace Disque (si nécessaire)

```bash
ssh ubuntu@16.170.74.58
docker system prune -af --volumes
```

**Cela libère de l'espace disque.**

### Étape 2: Démarrer Loki

```bash
cd /opt/monitoring
docker compose -f docker-compose.loki.yml up -d
```

**Attendre 30 secondes** que Loki démarre.

### Étape 3: Tester dans Grafana

1. **Rafraîchir la page** Grafana (F5)
2. Aller dans: **Connections > Data sources > Loki**
3. **Descendre en bas** de la page
4. Cliquer sur le bouton bleu **"Test"**
5. Attendre le résultat

**Résultat attendu:** "Data source is working" ✅

---

## 📝 Notes Importantes

### Le Message "Provisioned data source"

- ✅ **C'est normal** - La datasource est configurée automatiquement
- ✅ **Vous pouvez l'utiliser** - Créer des dashboards, faire des requêtes
- ❌ **Vous ne pouvez pas la modifier** via l'UI (c'est voulu)

### Pour Tester Loki

**Ne pas modifier** la configuration dans l'UI. Juste:
1. Cliquer sur **"Test"** (bouton en bas)
2. Vérifier: "Data source is working"

---

## 🔧 Si l'Erreur Persiste

### Vérifier que Loki est Démarré

```bash
ssh ubuntu@16.170.74.58
docker ps | grep loki
```

**Si Loki n'est pas là: `docker compose -f docker-compose.loki.yml up -d`**

### Vérifier l'Espace Disque

```bash
df -h /
```

**Si plein (100%):** `docker system prune -af --volumes`

---

## ✅ Checklist

- [ ] Espace disque OK: `df -h /` → moins de 90%
- [ ] Loki démarré: `docker ps | grep loki`
- [ ] Loki répond: `curl http://localhost:3100/ready` → "ready"
- [ ] Grafana peut se connecter: `docker exec grafana curl http://loki:3100/ready` → "ready"
- [ ] Test dans Grafana: "Data source is working" ✅

---

## 🎯 Résumé

1. **"Provisioned data source" = NORMAL** ✅
2. **Nettoyer l'espace disque** si nécessaire
3. **Démarrer Loki:** `docker compose -f docker-compose.loki.yml up -d`
4. **Tester dans Grafana:** Bouton "Test" → "Data source is working"

**C'est tout !** ✅

---

**Dernière mise à jour:** 8 Février 2026
