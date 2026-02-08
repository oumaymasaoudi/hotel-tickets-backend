# 🔍 Explication: URL Loki dans Grafana

**Question:** Pourquoi utiliser `http://loki:3100` et non l'IP de la machine ?

---

## 🎯 Réponse Simple

**OUI, vous devez utiliser `http://loki:3100`** dans la configuration Grafana.

**Pourquoi ?** Parce que Grafana et Loki sont dans le **même réseau Docker**, et Docker résout automatiquement le nom du conteneur (`loki`) en IP.

---

## 📝 Comment ça Fonctionne

### 1. Réseau Docker

Grafana et Loki sont tous les deux sur le réseau `monitoring-network`:

```yaml
# docker-compose.monitoring.yml
grafana:
  networks:
    - monitoring-network

# docker-compose.loki.yml
loki:
  networks:
    - monitoring-network
```

### 2. Résolution DNS Automatique

Docker crée automatiquement un **DNS interne** qui résout:
- `loki` → IP interne du conteneur Loki
- `grafana` → IP interne du conteneur Grafana
- `prometheus` → IP interne du conteneur Prometheus

### 3. Configuration dans Grafana

Dans `grafana/provisioning/datasources/loki.yml`:

```yaml
datasources:
  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100  # ✅ Nom du conteneur, pas l'IP !
    isDefault: false
```

**Pourquoi `http://loki:3100` ?**
- `loki` = nom du conteneur Docker
- `3100` = port interne de Loki
- Docker résout automatiquement `loki` → IP interne

---

## 🔍 Différence: URL Interne vs Externe

### URL Interne (Docker Network)
```
http://loki:3100
```
- ✅ Utilisé par Grafana (même réseau)
- ✅ Résolu automatiquement par Docker DNS
- ✅ Fonctionne entre conteneurs

### URL Externe (Depuis votre Machine)
```
http://16.170.74.58:3100
```
- ✅ Utilisé depuis votre navigateur
- ✅ Accès direct à Loki depuis l'extérieur
- ❌ Ne fonctionne PAS depuis Grafana (résolution DNS différente)

---

## ✅ Configuration Correcte

### Fichier: `grafana/provisioning/datasources/loki.yml`

```yaml
apiVersion: 1

datasources:
  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100  # ✅ CORRECT - Nom du conteneur
    isDefault: false
    jsonData:
      maxLines: 1000
    editable: false
```

**C'est la configuration actuelle et elle est correcte !** ✅

---

## 🔧 Vérification

### Tester depuis Grafana

```bash
ssh ubuntu@16.170.74.58

# Vérifier que Grafana peut résoudre "loki"
docker exec grafana nslookup loki

# Tester la connexion
docker exec grafana curl http://loki:3100/ready
# Résultat: "ready"
```

### Vérifier le Réseau

```bash
# Voir les conteneurs sur le réseau
docker network inspect monitoring-network --format '{{range .Containers}}{{.Name}} - {{.IPv4Address}}{{\"\\n\"}}{{end}}'
```

**Résultat attendu:**
```
loki - 172.18.0.X/16
grafana - 172.18.0.Y/16
```

---

## ❌ Erreurs Communes

### ❌ Utiliser l'IP Externe
```yaml
url: http://16.170.74.58:3100  # ❌ Ne fonctionne pas depuis Grafana
```

### ❌ Utiliser localhost
```yaml
url: http://localhost:3100  # ❌ localhost = Grafana lui-même, pas Loki
```

### ✅ Utiliser le Nom du Conteneur
```yaml
url: http://loki:3100  # ✅ CORRECT
```

---

## 📊 Schéma

```
┌─────────────────────────────────────┐
│  Réseau Docker: monitoring-network  │
│                                       │
│  ┌──────────┐      ┌──────────┐    │
│  │  Grafana │──────▶│   Loki   │    │
│  │          │ DNS  │          │    │
│  │ loki:3100│──────▶│ :3100    │    │
│  └──────────┘      └──────────┘    │
│                                     │
└─────────────────────────────────────┘
         │
         │ Port 3000 (Grafana)
         │ Port 3100 (Loki)
         ▼
   Votre Machine
   http://16.170.74.58:3000 (Grafana)
   http://16.170.74.58:3100 (Loki)
```

---

## ✅ Résumé

1. **URL dans Grafana:** `http://loki:3100` ✅
   - `loki` = nom du conteneur Docker
   - Résolu automatiquement par Docker DNS
   - Fonctionne car Grafana et Loki sont sur le même réseau

2. **URL depuis votre navigateur:** `http://16.170.74.58:3100` ✅
   - IP publique de la VM
   - Accès direct depuis l'extérieur

3. **Configuration actuelle:** ✅ **CORRECTE !**

**Votre configuration est bonne, le problème était juste que Loki n'était pas démarré !** 🎯

---

**Dernière mise à jour:** 8 Février 2026
