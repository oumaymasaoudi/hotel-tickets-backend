# 🔍 Diagnostiquer les Métriques Backend dans Grafana

## 📊 Situation Actuelle

✅ **Node Exporter** : Fonctionne (métriques système OK)
❌ **Backend JVM/Spring Boot** : "No data" (métriques application manquantes)

---

## 🔍 Étape 1 : Vérifier que Prometheus peut accéder au Backend

### 1.1 Vérifier dans Prometheus UI

1. Ouvrez un nouvel onglet : `http://13.62.53.224:9090`
2. Allez dans **Status** > **Targets**
3. Vérifiez le statut du target `backend` :
   - ✅ **UP** (vert) = Prometheus peut accéder au backend
   - ❌ **DOWN** (rouge) = Problème de connexion

### 1.2 Si le target est DOWN

**Vérifiez l'erreur :**
- Cliquez sur le target `backend` pour voir l'erreur
- Erreurs courantes :
  - `connection refused` = Le backend n'est pas accessible
  - `timeout` = Le backend ne répond pas
  - `404` = Le chemin `/actuator/prometheus` n'existe pas

---

## 🔧 Étape 2 : Vérifier que le Backend expose les métriques

### 2.1 Tester depuis la VM Monitoring

```powershell
# Se connecter à la VM monitoring
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224

# Tester l'accès au backend
curl http://13.49.44.219:8081/actuator/health

# Tester les métriques Prometheus
curl http://13.49.44.219:8081/actuator/prometheus | head -50
```

**Si ça fonctionne :**
- Vous devriez voir des métriques comme `http_server_requests_seconds_count`, `jvm_memory_used_bytes`, etc.

**Si ça ne fonctionne pas :**
- Le backend n'est pas accessible depuis la VM monitoring
- Vérifiez le Security Group AWS (port 8081 doit être ouvert)

---

## 🔧 Étape 3 : Vérifier la Configuration Prometheus

### 3.1 Vérifier le fichier de configuration

```powershell
# Se connecter à la VM monitoring
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224

# Vérifier la configuration
cat /opt/monitoring/prometheus/prometheus.yml | grep -A 10 "job_name: 'backend'"
```

**La configuration devrait être :**
```yaml
- job_name: 'backend'
  metrics_path: '/actuator/prometheus'
  static_configs:
    - targets: ['13.49.44.219:8081']
```

### 3.2 Si la configuration est incorrecte

**Corriger le fichier :**
```powershell
# Éditer le fichier
nano /opt/monitoring/prometheus/prometheus.yml

# Modifier la section backend pour pointer vers la bonne IP
# Sauvegarder (Ctrl+O, Enter, Ctrl+X)

# Redémarrer Prometheus
docker restart prometheus
```

---

## 🔧 Étape 4 : Vérifier que le Backend est démarré

### 4.1 Vérifier sur la VM Backend

```powershell
# Se connecter à la VM backend
ssh -i C:\Users\oumay\.ssh\github-actions-key ubuntu@13.49.44.219

# Vérifier les conteneurs
docker ps | grep backend

# Vérifier les logs
docker logs hotel-ticket-hub-backend --tail=50

# Tester localement
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/prometheus | head -20
```

**Si le backend n'est pas démarré :**
```powershell
cd /opt/backend
docker-compose up -d
```

---

## 🔧 Étape 5 : Vérifier le Security Group AWS

### 5.1 Le port 8081 doit être ouvert

**Vérifiez dans AWS Console :**
1. EC2 > Security Groups
2. Trouvez le Security Group de la VM backend (13.49.44.219)
3. Vérifiez qu'une règle Inbound autorise le port 8081 depuis :
   - `0.0.0.0/0` (pour les tests)
   - OU l'IP de la VM monitoring (13.62.53.224)

**Si le port n'est pas ouvert :**
- Ajoutez une règle : Custom TCP, Port 8081, Source : 13.62.53.224/32

---

## 🔧 Étape 6 : Vérifier les Métriques dans Explore

### 6.1 Tester dans Grafana Explore

1. Dans Grafana, allez dans **Explore** (icône boussole)
2. Sélectionnez **Prometheus** comme datasource
3. Testez ces queries :

**Query 1 : Vérifier que le backend est up**
```
up{job="backend"}
```
**Résultat attendu :** `up{job="backend", instance="13.49.44.219:8081"}` = 1

**Query 2 : Vérifier les métriques HTTP**
```
http_server_requests_seconds_count
```
**Résultat attendu :** Des données si le backend reçoit des requêtes

**Query 3 : Vérifier les métriques JVM**
```
jvm_memory_used_bytes
```
**Résultat attendu :** Des données sur la mémoire JVM

---

## 🚨 Solutions selon le Problème

### Problème 1 : Target DOWN dans Prometheus

**Cause :** Prometheus ne peut pas accéder au backend

**Solutions :**
1. Vérifier que le backend est démarré
2. Vérifier que le port 8081 est ouvert dans AWS Security Group
3. Vérifier la configuration Prometheus (bonne IP)

### Problème 2 : Backend accessible mais pas de métriques

**Cause :** Actuator n'est pas configuré ou le endpoint n'existe pas

**Solutions :**
1. Vérifier dans `application.properties` :
   ```properties
   management.endpoints.web.exposure.include=health,info,prometheus,metrics
   management.metrics.export.prometheus.enabled=true
   ```

2. Vérifier que les dépendances sont présentes dans `pom.xml` :
   - `spring-boot-starter-actuator`
   - `micrometer-registry-prometheus`

3. Redémarrer le backend

### Problème 3 : Métriques présentes mais dashboards vides

**Cause :** Les queries des dashboards ne correspondent pas aux métriques disponibles

**Solutions :**
1. Vérifier dans Explore que les métriques existent
2. Modifier les queries dans les dashboards pour correspondre aux métriques réelles
3. Vérifier les labels (application, instance, etc.)

---

## ✅ Checklist de Diagnostic

- [ ] Prometheus target `backend` est UP dans Status > Targets
- [ ] Backend accessible depuis VM monitoring : `curl http://13.49.44.219:8081/actuator/prometheus`
- [ ] Backend démarré sur VM backend : `docker ps | grep backend`
- [ ] Port 8081 ouvert dans AWS Security Group
- [ ] Configuration Prometheus correcte (bonne IP)
- [ ] Actuator configuré dans `application.properties`
- [ ] Dépendances présentes dans `pom.xml`
- [ ] Métriques visibles dans Grafana Explore

---

## 🎯 Commandes Rapides de Diagnostic

```powershell
# 1. Vérifier Prometheus targets
# Ouvrir : http://13.62.53.224:9090/targets

# 2. Tester backend depuis monitoring VM
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224
curl http://13.49.44.219:8081/actuator/prometheus | head -20

# 3. Vérifier backend sur VM backend
ssh -i C:\Users\oumay\.ssh\github-actions-key ubuntu@13.49.44.219
docker ps | grep backend
curl http://localhost:8081/actuator/prometheus | head -20

# 4. Vérifier dans Grafana Explore
# Query : up{job="backend"}
# Query : http_server_requests_seconds_count
```

---

**Une fois le problème résolu, les dashboards JVM et Spring Boot devraient afficher des données !**

