# 🔧 Solution : Backend Target DOWN dans Prometheus

## 🚨 Problème

**Erreur :** `Error scraping target: Get "http://13.49.44.219:8081/actuator/prometheus": context deadline exceeded`

**Cause :** Prometheus (sur la VM monitoring 13.62.53.224) ne peut pas accéder au backend (sur la VM 13.49.44.219:8081).

---

## ✅ Solution 1 : Ouvrir le Port 8081 dans AWS Security Group

### 1.1 Identifier le Security Group

1. Allez dans **AWS Console** > **EC2** > **Instances**
2. Trouvez l'instance avec l'IP `13.49.44.219` (VM Backend)
3. Cliquez sur l'instance
4. Dans l'onglet **Security**, notez le **Security Group** (ex: `sg-xxxxx`)

### 1.2 Ajouter la Règle Inbound

1. Allez dans **EC2** > **Security Groups**
2. Cliquez sur le Security Group de la VM backend
3. Cliquez sur l'onglet **Inbound rules**
4. Cliquez sur **Edit inbound rules**
5. Cliquez sur **Add rule**
6. Configurez :
   - **Type** : Custom TCP
   - **Port range** : 8081
   - **Source** : 
     - **Option 1** (Recommandé) : `13.62.53.224/32` (IP de la VM monitoring uniquement)
     - **Option 2** (Pour les tests) : `0.0.0.0/0` (toutes les IPs)
   - **Description** : "Prometheus monitoring access"
7. Cliquez sur **Save rules**

### 1.3 Vérifier

Attendez 1-2 minutes, puis :
1. Retournez sur Prometheus : `http://13.62.53.224:9090/targets`
2. Le target `backend` devrait passer à **UP** (vert)

---

## ✅ Solution 2 : Vérifier que le Backend est Démarré

### 2.1 Se connecter à la VM Backend

```powershell
ssh -i C:\Users\oumay\.ssh\github-actions-key ubuntu@13.49.44.219
```

### 2.2 Vérifier les conteneurs

```bash
docker ps | grep backend
```

**Si le backend n'est pas là :**
```bash
cd /opt/backend
docker-compose up -d
```

### 2.3 Vérifier les logs

```bash
docker logs hotel-ticket-hub-backend --tail=50
```

### 2.4 Tester localement

```bash
# Health check
curl http://localhost:8081/actuator/health

# Métriques Prometheus
curl http://localhost:8081/actuator/prometheus | head -20
```

**Si ça fonctionne localement mais pas depuis la VM monitoring :**
- C'est un problème de Security Group (Solution 1)

---

## ✅ Solution 3 : Vérifier depuis la VM Monitoring

### 3.1 Tester l'accès depuis la VM Monitoring

```powershell
# Se connecter à la VM monitoring
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224

# Tester l'accès au backend
curl -v http://13.49.44.219:8081/actuator/prometheus
```

**Si vous voyez :**
- ✅ Des métriques → Le problème est résolu
- ❌ `Connection refused` → Le port 8081 n'est pas ouvert (Solution 1)
- ❌ `Connection timed out` → Le port 8081 n'est pas ouvert (Solution 1)
- ❌ `404 Not Found` → Le backend n'expose pas `/actuator/prometheus` (Solution 4)

---

## ✅ Solution 4 : Vérifier la Configuration Actuator

### 4.1 Vérifier application.properties

Le backend doit avoir ces configurations :

```properties
# Actuator endpoints
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

### 4.2 Vérifier pom.xml

Les dépendances doivent être présentes :

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 4.3 Redémarrer le Backend

```powershell
ssh -i C:\Users\oumay\.ssh\github-actions-key ubuntu@13.49.44.219

cd /opt/backend
docker-compose restart backend

# Attendre 30 secondes
sleep 30

# Vérifier
curl http://localhost:8081/actuator/prometheus | head -20
```

---

## 🔍 Diagnostic Complet

### Test 1 : Backend accessible depuis votre machine Windows ?

```powershell
# Depuis PowerShell
curl http://13.49.44.219:8081/actuator/health
```

**Si ça ne fonctionne pas :**
- Le port 8081 n'est pas ouvert dans AWS Security Group
- Ouvrez-le pour `0.0.0.0/0` (temporairement pour les tests)

### Test 2 : Backend accessible depuis la VM Monitoring ?

```powershell
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224
curl http://13.49.44.219:8081/actuator/prometheus
```

**Si ça ne fonctionne pas :**
- Le port 8081 n'est pas ouvert pour la VM monitoring
- Ouvrez-le spécifiquement pour `13.62.53.224/32`

### Test 3 : Backend démarré ?

```powershell
ssh -i C:\Users\oumay\.ssh\github-actions-key ubuntu@13.49.44.219
docker ps | grep backend
curl http://localhost:8081/actuator/health
```

---

## 🎯 Solution Recommandée (Ordre de Priorité)

### 1. **Ouvrir le Port 8081 dans AWS** (🔴 CRITIQUE)

C'est probablement la cause principale. Le port 8081 doit être ouvert pour :
- La VM monitoring (13.62.53.224) pour que Prometheus puisse scraper
- Votre machine Windows (optionnel, pour les tests)

**Action :**
1. AWS Console > EC2 > Security Groups
2. Trouvez le Security Group de la VM backend (13.49.44.219)
3. Ajoutez une règle Inbound : TCP 8081, Source : `13.62.53.224/32`

### 2. **Vérifier que le Backend est Démarré**

```powershell
ssh -i C:\Users\oumay\.ssh\github-actions-key ubuntu@13.49.44.219
docker ps | grep backend
```

### 3. **Redémarrer Prometheus** (après avoir ouvert le port)

```powershell
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224
docker restart prometheus
```

### 4. **Vérifier dans Prometheus**

Attendez 1-2 minutes, puis :
- Allez sur : `http://13.62.53.224:9090/targets`
- Le target `backend` devrait être **UP** (vert)

---

## ✅ Après Correction

Une fois le target `backend` UP :

1. **Dans Grafana :**
   - Les dashboards "JVM (Micrometer)" et "Spring Boot 2.1" devraient afficher des données
   - Allez dans **Explore** et testez : `http_server_requests_seconds_count`

2. **Vérifier les métriques :**
   - Dans Grafana Explore, testez :
     - `up{job="backend"}` → Devrait retourner 1
     - `http_server_requests_seconds_count` → Devrait retourner des données
     - `jvm_memory_used_bytes` → Devrait retourner des données

---

## 📋 Checklist

- [ ] Port 8081 ouvert dans AWS Security Group pour la VM monitoring
- [ ] Backend démarré sur la VM backend
- [ ] Backend accessible depuis la VM monitoring : `curl http://13.49.44.219:8081/actuator/prometheus`
- [ ] Prometheus redémarré après ouverture du port
- [ ] Target `backend` UP dans Prometheus (`/targets`)
- [ ] Dashboards Grafana affichent des données

---

**Commencez par ouvrir le port 8081 dans AWS Security Group, c'est probablement la cause principale !**

