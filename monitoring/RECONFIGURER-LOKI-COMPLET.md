# 🔧 Reconfiguration Complète de Loki dans Grafana

**Problème:** "Unable to connect with Loki" malgré la configuration

---

## ✅ Solution Complète

### Étape 1: Vérifier l'État

```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring

# Vérifier que Loki est démarré
docker ps | grep loki

# Tester Loki
curl http://localhost:3100/ready
# Résultat: "ready"
```

### Étape 2: Recréer la Configuration

```bash
cd /opt/monitoring

# Créer le dossier si nécessaire
mkdir -p grafana/provisioning/datasources

# Créer le fichier de configuration
cat > grafana/provisioning/datasources/loki.yml << 'EOF'
apiVersion: 1

datasources:
  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    isDefault: false
    jsonData:
      maxLines: 1000
    editable: false
EOF
```

### Étape 3: Redémarrer Complètement

```bash
cd /opt/monitoring

# Arrêter Grafana
docker compose -f docker-compose.monitoring.yml down

# Redémarrer Grafana
docker compose -f docker-compose.monitoring.yml up -d

# Attendre 20 secondes
sleep 20
```

### Étape 4: Vérifier

```bash
# Vérifier que Grafana a chargé la config
docker exec grafana cat /etc/grafana/provisioning/datasources/loki.yml

# Tester la connexion
docker exec grafana curl http://loki:3100/ready
# Résultat: "ready"
```

---

## 🔍 Vérifications Avancées

### Vérifier que le Volume est Monté

```bash
docker inspect grafana | grep -A 10 'Mounts' | grep provisioning
```

**Résultat attendu:** Doit montrer le montage du volume.

### Vérifier les Logs Grafana

```bash
docker logs grafana --tail 50 | grep -i 'loki\|datasource\|provisioning'
```

### Vérifier le Réseau

```bash
docker network inspect monitoring-network | grep -E 'loki|grafana'
```

**Les deux doivent être sur le même réseau.**

---

## 🚨 Si Rien ne Fonctionne

### Solution Alternative: Configuration Manuelle

Si la configuration automatique ne fonctionne pas, vous pouvez créer la datasource manuellement dans Grafana:

1. **Aller dans Grafana:** http://16.170.74.58:3000
2. **Connections > Data sources > Add new data source**
3. **Sélectionner:** Loki
4. **URL:** `http://loki:3100`
5. **Cliquer sur:** "Save & test"

**Note:** Cette configuration sera perdue si Grafana redémarre. La configuration automatique (provisioning) est préférable.

---

## ✅ Checklist Complète

- [ ] Loki est démarré: `docker ps | grep loki`
- [ ] Loki répond: `curl http://localhost:3100/ready` → "ready"
- [ ] Fichier de configuration existe: `cat grafana/provisioning/datasources/loki.yml`
- [ ] Grafana peut se connecter: `docker exec grafana curl http://loki:3100/ready` → "ready"
- [ ] Grafana a chargé la config: `docker exec grafana cat /etc/grafana/provisioning/datasources/loki.yml`
- [ ] Test dans Grafana: "Data source is working" ✅

---

## 🎯 Résumé

1. **Recréer la configuration:** Fichier `loki.yml` dans `grafana/provisioning/datasources/`
2. **Redémarrer Grafana complètement:** `docker compose down && docker compose up -d`
3. **Vérifier:** Grafana peut se connecter à Loki
4. **Tester dans Grafana:** Bouton "Test" → "Data source is working"

---

**Dernière mise à jour:** 8 Février 2026
