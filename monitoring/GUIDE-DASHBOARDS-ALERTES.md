# Guide : Dashboards Grafana et Alertes Prometheus

## Ce qui a été configuré

### 1. Dashboards Grafana

Deux dashboards ont été créés :

#### A. Backend Spring Boot Dashboard
**Fichier :** `monitoring/grafana/dashboards/backend-spring-boot.json`

**Métriques affichées :**
- Application Status (UP/DOWN)
- HTTP Requests Rate (par statut)
- JVM Heap Memory (Used/Max)
- JVM Memory Usage % (gauge)
- Response Time (95th percentile)
- Error Rate %
- JVM Threads (Live/Peak)
- GC Pause Time
- Database Connections (Active/Max/Idle)

#### B. System Overview Dashboard
**Fichier :** `monitoring/grafana/dashboards/system-overview.json`

**Métriques affichées :**
- CPU Usage (par instance)
- Memory Usage (par instance)
- Disk Space Available (par instance)
- Services Status (backend, frontend, database)
- Memory Usage by Instance

### 2. Alertes Prometheus

**Fichier :** `monitoring/prometheus/rules/alerts.yml`

#### Alertes Système
- **HighCPUUsage** : CPU > 80% pendant 5 minutes
- **HighMemoryUsage** : RAM > 85% pendant 5 minutes
- **DiskSpaceLow** : Disque < 15% pendant 5 minutes

#### Alertes Application
- **BackendServiceDown** : Service backend down pendant 1 minute (CRITICAL)
- **HighErrorRate** : Taux d'erreur HTTP > 5% pendant 5 minutes (WARNING)
- **HighResponseTime** : Temps de réponse 95e percentile > 2s pendant 5 minutes (WARNING)
- **HighJVMMemory** : Mémoire JVM heap > 85% pendant 5 minutes (WARNING)
- **CriticalJVMMemory** : Mémoire JVM heap > 95% pendant 2 minutes (CRITICAL)
- **HighThreadCount** : Nombre de threads > 200 pendant 5 minutes (WARNING)
- **HighGCRate** : Taux de GC > 10 collections/s pendant 5 minutes (WARNING)
- **HighGCDuration** : Durée de GC > 1s/s pendant 5 minutes (WARNING)
- **HighDatabaseConnections** : Pool de connexions DB > 80% pendant 5 minutes (WARNING)

## Déploiement

### 1. Copier les fichiers sur la VM Monitoring

```bash
# Depuis votre machine locale
cd hotel-ticket-hub-backend

# Copier les dashboards
scp -i ~/.ssh/oumayma-key.pem -r monitoring/grafana/dashboards/*.json ubuntu@16.170.74.58:/opt/monitoring/grafana/dashboards/

# Copier les règles d'alerte
scp -i ~/.ssh/oumayma-key.pem monitoring/prometheus/rules/alerts.yml ubuntu@16.170.74.58:/opt/monitoring/prometheus/rules/alerts.yml
```

### 2. Redémarrer les services

```bash
# Sur la VM Monitoring
ssh -i ~/.ssh/oumayma-key.pem ubuntu@16.170.74.58

cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml restart prometheus grafana
```

### 3. Vérifier que les dashboards sont chargés

1. **Ouvrez Grafana** : http://16.170.74.58:3000
2. **Login** : `admin` / `admin`
3. **Dashboards** : Menu → Dashboards
4. **Vérifiez** : Les dashboards "Hotel Ticket Hub - Backend Spring Boot" et "Hotel Ticket Hub - System Overview" devraient apparaître

### 4. Vérifier les alertes

1. **Ouvrez Prometheus** : http://16.170.74.58:9090
2. **Alerts** : Menu → Alerts
3. **Vérifiez** : Les règles d'alerte devraient être listées

## Utilisation

### Dashboards Grafana

#### Accéder aux dashboards
1. Grafana UI : http://16.170.74.58:3000
2. Menu → Dashboards → Browse
3. Sélectionnez le dashboard souhaité

#### Personnaliser les dashboards
- Cliquez sur le bouton **Edit** (icône crayon) en haut à droite
- Modifiez les panneaux, ajoutez des métriques, changez les seuils
- Cliquez sur **Save** pour sauvegarder

#### Créer des alertes depuis Grafana
1. Ouvrez un dashboard
2. Cliquez sur un panneau → **Edit**
3. Onglet **Alert** → **Create Alert**
4. Configurez les conditions et notifications

### Alertes Prometheus

#### Voir les alertes actives
- Prometheus UI → Alerts : http://16.170.74.58:9090/alerts
- Alertmanager UI : http://16.170.74.58:9093

#### Configurer les notifications

**Fichier :** `monitoring/alertmanager/alertmanager.yml`

**Actuellement configuré :**
- Email (nécessite configuration SMTP)
- Webhook (optionnel)

**Pour activer les emails :**
1. Modifiez `alertmanager.yml`
2. Configurez les paramètres SMTP :
   ```yaml
   smtp_smarthost: 'smtp.gmail.com:587'
   smtp_from: 'alertmanager@hotel-ticket-hub.com'
   smtp_auth_username: 'your-email@gmail.com'
   smtp_auth_password: 'your-app-password'
   ```
3. Redémarrez Alertmanager :
   ```bash
   docker restart alertmanager
   ```

## Métriques importantes à surveiller

### Backend Spring Boot
- **JVM Memory Usage** : Doit rester < 85%
- **Error Rate** : Doit rester < 5%
- **Response Time** : 95e percentile < 2s
- **Database Connections** : Pool < 80%
- **GC Pause Time** : < 1s/s

### Système
- **CPU Usage** : < 80%
- **Memory Usage** : < 85%
- **Disk Space** : > 15%

## Troubleshooting

### Les dashboards ne s'affichent pas
1. Vérifiez que les fichiers JSON sont dans `/opt/monitoring/grafana/dashboards/`
2. Vérifiez les permissions : `chmod 644 /opt/monitoring/grafana/dashboards/*.json`
3. Redémarrez Grafana : `docker restart grafana`
4. Vérifiez les logs : `docker logs grafana --tail 50`

### Les alertes ne se déclenchent pas
1. Vérifiez que les règles sont chargées : Prometheus → Status → Rules
2. Vérifiez la syntaxe des requêtes PromQL dans `alerts.yml`
3. Vérifiez que les métriques existent : Prometheus → Graph → Testez les requêtes
4. Redémarrez Prometheus : `docker restart prometheus`

### Les notifications ne fonctionnent pas
1. Vérifiez la configuration SMTP dans `alertmanager.yml`
2. Testez la connexion SMTP
3. Vérifiez les logs : `docker logs alertmanager --tail 50`
4. Vérifiez Alertmanager UI : http://16.170.74.58:9093

## Prochaines étapes

1. **Configurer les notifications** : Email, Slack, Discord, etc.
2. **Créer des dashboards personnalisés** : Métriques business, KPIs spécifiques
3. **Configurer des alertes avancées** : Basées sur des patterns, agrégations complexes
4. **Documenter les runbooks** : Procédures à suivre quand une alerte se déclenche

