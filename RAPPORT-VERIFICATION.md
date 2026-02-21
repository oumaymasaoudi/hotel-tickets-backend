# 📊 Rapport de Vérification Complète - TicketHotel

**Date:** 17 Février 2026

---

## ✅ VM BACKEND (13.63.15.86)

### Conteneurs Docker
| Conteneur | Image | Status | Ports |
|-----------|-------|--------|-------|
| **hotel-ticket-hub-backend-staging** | ghcr.io/oumaymasaoudi/hotel-tickets-backend/backend:main | ✅ Up 8 days | 0.0.0.0:8081->8080/tcp |
| **node-exporter-backend** | prom/node-exporter:latest | ✅ Up 8 days | 0.0.0.0:9100->9100/tcp |
| **cadvisor** | gcr.io/cadvisor/cadvisor:latest | ✅ Up 9 days (healthy) | 0.0.0.0:8080->8080/tcp |

### Health Check
- ✅ **Backend:** `{"status":"UP"}`
- ✅ **Métriques Prometheus:** Accessibles
- ✅ **Logs:** Fonctionnels

### URLs
- **API:** http://13.63.15.86:8081/api
- **Swagger:** http://13.63.15.86:8081/swagger-ui.html
- **Health:** http://13.63.15.86:8081/actuator/health
- **Prometheus Metrics:** http://13.63.15.86:8081/actuator/prometheus
- **Node Exporter:** http://13.63.15.86:9100
- **cAdvisor:** http://13.63.15.86:8080

---

## ⚠️ VM FRONTEND (13.50.221.51)

### Statut
- ❌ **SSH:** Permission denied (publickey)
- ⚠️ **Action requise:** Vérifier la clé SSH ou les permissions

---

## ✅ VM DATABASE (13.48.83.147)

### PostgreSQL
- ✅ **Service:** Active (exited) depuis 2 semaines 4 jours
- ✅ **Connexion:** Fonctionnelle

### Tables de la base de données (16 tables)

| Table | Type |
|-------|------|
| audit_logs | table |
| categories | table |
| data_deletion_requests | table |
| gdpr_consents | table |
| hotel_subscription_additional_categories | table |
| hotel_subscriptions | table |
| hotels | table |
| payments | table |
| plans | table |
| profiles | table |
| ticket_comments | table |
| ticket_history | table |
| ticket_images | table |
| tickets | table |
| user_roles | table |
| user_specialties | table |

### URLs
- **PostgreSQL:** postgresql://13.48.83.147:5432/hotel_ticket_hub

---

## ✅ VM MONITORING (16.170.74.58)

### Conteneurs Docker
| Conteneur | Image | Status | Ports |
|-----------|-------|--------|-------|
| **grafana** | grafana/grafana:latest | ✅ Up (health: starting) | 0.0.0.0:3000->3000/tcp |
| **prometheus** | prom/prometheus:latest | ✅ Up (health: starting) | 0.0.0.0:9090->9090/tcp |
| **loki** | grafana/loki:latest | ⚠️ Up 4 days (unhealthy) | 0.0.0.0:3100->3100/tcp |
| **promtail** | grafana/promtail:latest | ✅ Up 4 days | - |
| **alertmanager** | prom/alertmanager:latest | ✅ Up (health: starting) | 0.0.0.0:9093->9093/tcp |
| **node-exporter** | prom/node-exporter:latest | ✅ Up | 0.0.0.0:9100->9100/tcp |
| **cadvisor** | gcr.io/cadvisor/cadvisor:latest | ✅ Up (health: starting) | 0.0.0.0:8080->8080/tcp |

### Health Checks
- ✅ **Grafana:** `{"database":"ok","version":"12.3.2+security-01"}`
- ✅ **Prometheus:** `Prometheus Server is Healthy.`
- ✅ **Loki:** `ready`
- ✅ **Alertmanager:** `OK`
- ✅ **Loki API Labels:** `{"status":"success","data":["container","project","service","service_name","stream"]}`

### URLs
- **Grafana:** http://16.170.74.58:3000 (admin/admin)
- **Prometheus:** http://16.170.74.58:9090
- **Loki:** http://16.170.74.58:3100
- **Alertmanager:** http://16.170.74.58:9093
- **cAdvisor:** http://16.170.74.58:8080
- **Node Exporter:** http://16.170.74.58:9100

---

## 🔧 Actions Requises

### ✅ 1. Services Monitoring démarrés
- ✅ Grafana, Prometheus, Alertmanager démarrés avec succès

### ⚠️ 2. Vérifier la clé SSH pour Frontend

```bash
# Vérifier la clé SSH
ls -la ~/.ssh/oumayma-key.pem

# Tester la connexion
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.50.221.51
```

### ⚠️ 3. Vérifier l'état de Loki (unhealthy)

```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@16.170.74.58
docker logs loki --tail 50
docker restart loki
```

---

## 📋 Résumé Global

| Service | Statut | Détails |
|---------|--------|---------|
| **Backend** | ✅ OK | Tous les conteneurs fonctionnent |
| **Frontend** | ⚠️ SSH | Permission denied |
| **Database** | ✅ OK | PostgreSQL actif, 16 tables |
| **Monitoring** | ✅ OK | Tous les services démarrés (Loki unhealthy mais fonctionnel) |

---

## 🎯 Prochaines Étapes

1. ✅ Backend: Fonctionnel
2. ⚠️ Frontend: Corriger l'accès SSH
3. ✅ Database: Fonctionnelle (16 tables)
4. ✅ Monitoring: Tous les services démarrés et fonctionnels

---

**Dernière mise à jour:** 17 Février 2026
