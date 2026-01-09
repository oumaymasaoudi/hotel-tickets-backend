# 📊 Créer les Dashboards Grafana

## 🎯 Objectif

Créer des dashboards pour visualiser les métriques de votre application.

---

## 📋 Étape 1 : Vérifier la Connexion à Prometheus

### 1.1 Vérifier que Prometheus est la Datasource

1. Dans Grafana, allez dans **Connections** (ou **Configuration** > **Data sources**)
2. Vérifiez que **Prometheus** est configuré
3. L'URL devrait être : `http://prometheus:9090`
4. Cliquez sur **Save & Test** pour vérifier la connexion

**Si Prometheus n'est pas configuré :**
- Cliquez sur **Add data source**
- Sélectionnez **Prometheus**
- URL : `http://prometheus:9090`
- Cliquez sur **Save & Test**

---

## 📊 Étape 2 : Créer un Dashboard "Infrastructure"

### 2.1 Créer le Dashboard

1. Cliquez sur **"+ Create dashboard"** (ou **Dashboards** > **New** > **New Dashboard**)
2. Cliquez sur **Add visualization** (ou **Add** > **Visualization**)

### 2.2 Panel 1 : CPU Usage

**Configuration :**
- **Panel title** : `CPU Usage`
- **Query** : 
  ```
  100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
  ```
- **Legend** : `{{instance}}`
- **Unit** : Percent (0-100)
- **Min** : 0
- **Max** : 100

### 2.3 Panel 2 : Memory Usage

**Configuration :**
- **Panel title** : `Memory Usage`
- **Query** : 
  ```
  (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes * 100
  ```
- **Legend** : `Memory Used`
- **Unit** : Percent (0-100)

### 2.4 Panel 3 : Disk Usage

**Configuration :**
- **Panel title** : `Disk Usage`
- **Query** : 
  ```
  (node_filesystem_size_bytes - node_filesystem_avail_bytes) / node_filesystem_size_bytes * 100
  ```
- **Legend** : `{{mountpoint}}`
- **Unit** : Percent (0-100)
- **Filter** : `mountpoint="/"`

### 2.5 Panel 4 : Network Traffic

**Configuration :**
- **Panel title** : `Network Traffic`
- **Query A** (Incoming) :
  ```
  rate(node_network_receive_bytes_total[5m])
  ```
- **Query B** (Outgoing) :
  ```
  rate(node_network_transmit_bytes_total[5m])
  ```
- **Unit** : bytes/sec (B/s)
- **Legend** : `{{instance}} - {{device}}`

### 2.6 Sauvegarder le Dashboard

1. Cliquez sur **Save dashboard** (icône disquette en haut à droite)
2. Nom : `Infrastructure Monitoring`
3. Folder : `General` (ou créez un dossier "Monitoring")
4. Cliquez sur **Save**

---

## 📊 Étape 3 : Créer un Dashboard "Backend Application"

### 3.1 Créer le Dashboard

1. **Dashboards** > **New** > **New Dashboard**
2. Cliquez sur **Add visualization**

### 3.2 Panel 1 : HTTP Request Rate

**Configuration :**
- **Panel title** : `HTTP Request Rate`
- **Query** : 
  ```
  rate(http_server_requests_seconds_count[5m])
  ```
- **Legend** : `{{method}} {{uri}}`
- **Unit** : requests/sec
- **Visualization** : Time series

### 3.3 Panel 2 : HTTP Response Time (P95)

**Configuration :**
- **Panel title** : `HTTP Response Time (P95)`
- **Query** : 
  ```
  histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
  ```
- **Legend** : `P95 Response Time`
- **Unit** : seconds (s)
- **Visualization** : Time series

### 3.4 Panel 3 : HTTP Status Codes

**Configuration :**
- **Panel title** : `HTTP Status Codes`
- **Query** : 
  ```
  sum by (status) (rate(http_server_requests_seconds_count[5m]))
  ```
- **Legend** : `{{status}}`
- **Unit** : requests/sec
- **Visualization** : Pie chart ou Bar chart

### 3.5 Panel 4 : JVM Memory Usage

**Configuration :**
- **Panel title** : `JVM Memory Usage`
- **Query A** (Used) :
  ```
  jvm_memory_used_bytes{area="heap"}
  ```
- **Query B** (Max) :
  ```
  jvm_memory_max_bytes{area="heap"}
  ```
- **Unit** : bytes (B)
- **Visualization** : Time series

### 3.6 Panel 5 : Active Threads

**Configuration :**
- **Panel title** : `Active Threads`
- **Query** : 
  ```
  jvm_threads_live_threads
  ```
- **Legend** : `Active Threads`
- **Unit** : short
- **Visualization** : Stat (ou Time series)

### 3.7 Sauvegarder le Dashboard

1. **Save dashboard**
2. Nom : `Backend Application Metrics`
3. Folder : `General`
4. **Save**

---

## 📊 Étape 4 : Importer des Dashboards Prêts à l'Emploi

### 4.1 Dashboard Node Exporter Full

1. **Dashboards** > **Import**
2. **Import via grafana.com** : Entrez l'ID `1860`
3. Cliquez sur **Load**
4. Sélectionnez **Prometheus** comme datasource
5. Cliquez sur **Import**

**Ce dashboard inclut :**
- CPU, Memory, Disk, Network
- System Load
- File System
- Network Interfaces

### 4.2 Dashboard Spring Boot 2.1 Statistics

1. **Dashboards** > **Import**
2. ID : `11378`
3. Cliquez sur **Load**
4. Sélectionnez **Prometheus** comme datasource
5. Cliquez sur **Import**

**Ce dashboard inclut :**
- HTTP Metrics
- JVM Metrics
- Application Metrics

### 4.3 Dashboard JVM (Micrometer)

1. **Dashboards** > **Import**
2. ID : `4701`
3. Cliquez sur **Load**
4. Sélectionnez **Prometheus** comme datasource
5. Cliquez sur **Import**

---

## 📊 Étape 5 : Créer un Dashboard "Docker Containers"

### 5.1 Panel 1 : Container CPU Usage

**Configuration :**
- **Panel title** : `Container CPU Usage`
- **Query** : 
  ```
  rate(container_cpu_usage_seconds_total[5m]) * 100
  ```
- **Legend** : `{{name}}`
- **Unit** : Percent (0-100)
- **Filter** : `name=~".*backend.*|.*frontend.*"`

### 5.2 Panel 2 : Container Memory Usage

**Configuration :**
- **Panel title** : `Container Memory Usage`
- **Query** : 
  ```
  container_memory_usage_bytes
  ```
- **Legend** : `{{name}}`
- **Unit** : bytes (B)
- **Filter** : `name=~".*backend.*|.*frontend.*"`

### 5.3 Panel 3 : Container Network I/O

**Configuration :**
- **Panel title** : `Container Network I/O`
- **Query A** (RX) :
  ```
  rate(container_network_receive_bytes_total[5m])
  ```
- **Query B** (TX) :
  ```
  rate(container_network_transmit_bytes_total[5m])
  ```
- **Legend** : `{{name}} - {{interface}}`
- **Unit** : bytes/sec (B/s)

---

## 🎨 Étape 6 : Personnaliser les Dashboards

### 6.1 Ajouter des Alertes Visuelles

Pour chaque panel :
1. Cliquez sur le panel
2. Allez dans **Alert** (ou **Overrides**)
3. Ajoutez des **Thresholds** :
   - **Warning** : 70% (jaune)
   - **Critical** : 90% (rouge)

### 6.2 Organiser les Panels

1. **Drag & Drop** pour réorganiser
2. **Resize** en cliquant sur les coins
3. **Edit** en cliquant sur le titre du panel

### 6.3 Ajouter des Variables (Templates)

1. Cliquez sur **Dashboard settings** (icône engrenage)
2. Allez dans **Variables**
3. Cliquez sur **Add variable**
4. **Name** : `instance`
5. **Type** : Query
6. **Query** : `label_values(node_cpu_seconds_total, instance)`
7. **Save**

Ensuite, utilisez `$instance` dans vos queries.

---

## 📋 Checklist Rapide

- [ ] Prometheus datasource configuré et testé
- [ ] Dashboard "Infrastructure" créé (CPU, RAM, Disque, Réseau)
- [ ] Dashboard "Backend Application" créé (HTTP, JVM)
- [ ] Dashboard Node Exporter importé (ID: 1860)
- [ ] Dashboard Spring Boot importé (ID: 11378)
- [ ] Dashboard JVM importé (ID: 4701)
- [ ] Dashboard "Docker Containers" créé
- [ ] Alertes visuelles configurées (thresholds)
- [ ] Dashboards sauvegardés et organisés

---

## 🚀 Commandes Utiles

### Vérifier les Métriques Disponibles

```powershell
# Se connecter à la VM monitoring
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224

# Vérifier Prometheus
curl http://localhost:9090/api/v1/label/__name__/values | jq

# Vérifier les métriques backend
curl http://13.49.44.219:8081/actuator/prometheus | head -50
```

### Tester une Query Prometheus

1. Dans Grafana, allez dans **Explore** (icône boussole)
2. Sélectionnez **Prometheus** comme datasource
3. Tapez votre query : `node_cpu_seconds_total`
4. Cliquez sur **Run query**
5. Vérifiez les résultats

---

## 💡 Astuces

1. **Utilisez les dashboards pré-configurés** : Plus rapide que de tout créer
2. **Dupliquez les dashboards** : Pour créer des variantes
3. **Organisez en dossiers** : Infrastructure, Application, Database
4. **Ajoutez des annotations** : Pour marquer les déploiements
5. **Partagez les dashboards** : Via des liens ou exports JSON

---

**Une fois les dashboards créés, vous pourrez visualiser en temps réel :**
- ✅ CPU, RAM, Disque de vos VMs
- ✅ Métriques HTTP du backend
- ✅ Métriques JVM
- ✅ Métriques Docker containers
- ✅ Alertes visuelles

