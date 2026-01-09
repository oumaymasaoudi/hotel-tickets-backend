# 🔧 Configurer Prometheus dans Grafana

## 📋 Étape 1 : Vérifier si Prometheus est déjà configuré

1. Cliquez sur **"View configured data sources"** (carte orange à droite)
2. Vérifiez si **Prometheus** apparaît dans la liste

**Si Prometheus est déjà là :**
- ✅ C'est bon, passez à l'étape 2

**Si Prometheus n'est pas là :**
- Continuez avec l'étape suivante pour l'ajouter

---

## 📋 Étape 2 : Ajouter Prometheus (si nécessaire)

### 2.1 Cliquer sur "Add new connection"

1. Cliquez sur la carte verte **"Add new connection"**
2. OU cliquez sur **"Add new connection"** en haut à droite

### 2.2 Sélectionner Prometheus

1. Dans la liste des sources de données, cherchez **Prometheus**
2. Cliquez sur **Prometheus**

### 2.3 Configurer Prometheus

**Remplissez les champs :**

1. **Name** : `Prometheus` (ou laissez par défaut)

2. **URL** : `http://prometheus:9090`
   - ⚠️ **IMPORTANT** : Utilisez `prometheus` (nom du service Docker) et non `localhost`
   - Si vous êtes sur la VM monitoring, utilisez : `http://prometheus:9090`
   - Si ça ne fonctionne pas, essayez : `http://localhost:9090`

3. **Access** : Laissez **Server (default)**

4. **Scrape interval** : `15s` (optionnel)

5. **Query timeout** : `60s` (optionnel)

6. **HTTP Method** : `POST` (recommandé)

### 2.4 Tester la connexion

1. Cliquez sur **"Save & Test"** en bas
2. Vous devriez voir : ✅ **"Data source is working"**

**Si vous voyez une erreur :**
- Vérifiez que Prometheus est démarré :
  ```powershell
  ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224
  docker ps | grep prometheus
  ```
- Vérifiez l'URL : Essayez `http://localhost:9090` si `http://prometheus:9090` ne fonctionne pas

---

## 📋 Étape 3 : Vérifier que Prometheus collecte les métriques

### 3.1 Vérifier dans Prometheus

1. Ouvrez un nouvel onglet : `http://13.62.53.224:9090`
2. Allez dans **Status** > **Targets**
3. Vérifiez que tous les targets sont **UP** (vert) :
   - ✅ `prometheus` (Prometheus lui-même)
   - ✅ `node-exporter` (Métriques système)
   - ✅ `cadvisor` (Métriques Docker)
   - ✅ `backend` (Métriques backend Spring Boot)

**Si un target est DOWN (rouge) :**
- Cliquez sur le target pour voir l'erreur
- Vérifiez la configuration dans `prometheus.yml`

### 3.2 Tester une query dans Grafana

1. Dans Grafana, allez dans **Explore** (icône boussole dans le menu gauche)
2. Sélectionnez **Prometheus** comme datasource (en haut)
3. Tapez cette query : `up`
4. Cliquez sur **Run query**

**Vous devriez voir :**
- `up{job="prometheus"}` = 1
- `up{job="node-exporter"}` = 1
- `up{job="cadvisor"}` = 1
- `up{job="backend"}` = 1

**Si vous voyez des données :** ✅ Prometheus fonctionne !

---

## 📋 Étape 4 : Importer les Dashboards

Maintenant que Prometheus est configuré, importez les dashboards :

### 4.1 Aller dans Dashboards

1. Cliquez sur **Dashboards** dans le menu gauche
2. Cliquez sur **"+ Import"** (en haut à droite)

### 4.2 Importer Dashboard 1 : Node Exporter Full

1. **Import via grafana.com** : Entrez l'ID `1860`
2. Cliquez sur **Load**
3. Sélectionnez **Prometheus** comme datasource
4. Cliquez sur **Import**

**Ce dashboard montre :**
- CPU, RAM, Disque, Réseau
- System Load
- File System

### 4.3 Importer Dashboard 2 : Spring Boot 2.1 Statistics

1. **Import via grafana.com** : Entrez l'ID `11378`
2. Cliquez sur **Load**
3. Sélectionnez **Prometheus** comme datasource
4. Cliquez sur **Import**

**Ce dashboard montre :**
- HTTP Metrics (requêtes, temps de réponse)
- JVM Metrics (mémoire, threads, GC)
- Application Metrics

### 4.4 Importer Dashboard 3 : JVM (Micrometer)

1. **Import via grafana.com** : Entrez l'ID `4701`
2. Cliquez sur **Load**
3. Sélectionnez **Prometheus** comme datasource
4. Cliquez sur **Import**

**Ce dashboard montre :**
- JVM Memory (heap, non-heap)
- Garbage Collection
- Threads

---

## 🔍 Vérification Rapide

### Test 1 : Vérifier les métriques système

Dans **Explore** :
- Query : `node_cpu_seconds_total`
- Vous devriez voir des données

### Test 2 : Vérifier les métriques backend

Dans **Explore** :
- Query : `http_server_requests_seconds_count`
- Vous devriez voir des données si le backend est accessible

### Test 3 : Vérifier les métriques Docker

Dans **Explore** :
- Query : `container_cpu_usage_seconds_total`
- Vous devriez voir des données

---

## 🚨 Problèmes Courants

### Problème 1 : "Data source is not working"

**Solution :**
1. Vérifiez que Prometheus est démarré :
   ```powershell
   ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224
   docker ps | grep prometheus
   ```

2. Essayez une autre URL :
   - `http://localhost:9090` au lieu de `http://prometheus:9090`
   - `http://127.0.0.1:9090`

3. Vérifiez les logs de Prometheus :
   ```powershell
   docker logs prometheus --tail=50
   ```

### Problème 2 : Pas de métriques backend

**Solution :**
1. Vérifiez que le backend est accessible depuis la VM monitoring :
   ```powershell
   ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224
   curl http://13.49.44.219:8081/actuator/prometheus | head -20
   ```

2. Vérifiez la configuration Prometheus :
   - Le fichier `prometheus.yml` doit pointer vers `13.63.15.86:8081`

3. Vérifiez dans Prometheus UI :
   - Allez sur `http://13.62.53.224:9090`
   - Status > Targets
   - Vérifiez que `backend` est UP

### Problème 3 : Dashboard vide

**Solution :**
1. Vérifiez que le datasource est bien **Prometheus**
2. Vérifiez la période de temps (en haut à droite) : Sélectionnez "Last 5 minutes"
3. Vérifiez dans Explore que les queries fonctionnent

---

## ✅ Checklist

- [ ] Prometheus datasource ajouté dans Grafana
- [ ] Test "Save & Test" : ✅ Data source is working
- [ ] Explore fonctionne : Les queries retournent des données
- [ ] Dashboard Node Exporter importé (ID: 1860)
- [ ] Dashboard Spring Boot importé (ID: 11378)
- [ ] Dashboard JVM importé (ID: 4701)
- [ ] Tous les dashboards affichent des données

---

## 🎯 Prochaines Étapes

Une fois les dashboards importés :

1. **Organiser les dashboards** :
   - Créez des dossiers : "Infrastructure", "Application"
   - Déplacez les dashboards dans les dossiers

2. **Configurer les alertes** :
   - Allez dans **Alerting** (menu gauche)
   - Créez des alertes basées sur les métriques

3. **Personnaliser les dashboards** :
   - Ajoutez des panels personnalisés
   - Modifiez les queries selon vos besoins

---

**Une fois terminé, vous aurez :**
- ✅ Visualisation en temps réel de CPU, RAM, Disque
- ✅ Métriques HTTP du backend
- ✅ Métriques JVM
- ✅ Métriques Docker containers

