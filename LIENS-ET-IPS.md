# 🔗 Liens et Adresses IP - TicketHotel

**Date:** 8 Février 2026

---

## 📍 Adresses IP des VMs

| VM | IP | Rôle |
|----|----|------|
| **Backend** | 13.63.15.86 | API Spring Boot |
| **Frontend** | 13.50.221.51 | Application React |
| **Database** | 13.48.83.147 | PostgreSQL |
| **Monitoring** | 16.170.74.58 | Grafana, Prometheus, Loki |

---

## 🌐 URLs Application

### Frontend
- **URL:** http://13.50.221.51
- **Port:** 80

### Backend API
- **URL:** http://13.63.15.86:8081/api
- **Swagger:** http://13.63.15.86:8081/swagger-ui.html
- **Health Check:** http://13.63.15.86:8081/actuator/health
- **Prometheus Metrics:** http://13.63.15.86:8081/actuator/prometheus
- **Port:** 8081

### Database
- **IP:** 13.48.83.147
- **Port:** 5432
- **Database:** hotel_ticket_hub

---

## 📊 URLs Monitoring

### Grafana
- **URL:** http://16.170.74.58:3000
- **Port:** 3000
- **Username:** `admin`
- **Password:** `admin`
- **Health Check:** http://16.170.74.58:3000/api/health

### Prometheus
- **URL:** http://16.170.74.58:9090
- **Port:** 9090
- **Health Check:** http://16.170.74.58:9090/-/healthy

### Loki
- **URL:** http://16.170.74.58:3100
- **Port:** 3100
- **Health Check:** http://16.170.74.58:3100/ready
- **API Labels:** http://16.170.74.58:3100/loki/api/v1/labels

### Alertmanager
- **URL:** http://16.170.74.58:9093
- **Port:** 9093
- **Health Check:** http://16.170.74.58:9093/-/healthy

### cAdvisor
- **URL:** http://16.170.74.58:8080
- **Port:** 8080

### Node Exporter
- **Backend:** http://13.63.15.86:9100
- **Monitoring:** http://16.170.74.58:9100
- **Port:** 9100

---

## 📋 Tableau Récapitulatif Complet

| Service | IP | Port | URL Complète |
|---------|----|----|--------------|
| **Frontend** | 13.50.221.51 | 80 | http://13.50.221.51 |
| **Backend API** | 13.63.15.86 | 8081 | http://13.63.15.86:8081/api |
| **Backend Swagger** | 13.63.15.86 | 8081 | http://13.63.15.86:8081/swagger-ui.html |
| **Backend Health** | 13.63.15.86 | 8081 | http://13.63.15.86:8081/actuator/health |
| **Database** | 13.48.83.147 | 5432 | postgresql://13.48.83.147:5432/hotel_ticket_hub |
| **Grafana** | 16.170.74.58 | 3000 | http://16.170.74.58:3000 |
| **Prometheus** | 16.170.74.58 | 9090 | http://16.170.74.58:9090 |
| **Loki** | 16.170.74.58 | 3100 | http://16.170.74.58:3100 |
| **Alertmanager** | 16.170.74.58 | 9093 | http://16.170.74.58:9093 |
| **cAdvisor** | 16.170.74.58 | 8080 | http://16.170.74.58:8080 |
| **Node Exporter (Backend)** | 13.63.15.86 | 9100 | http://13.63.15.86:9100 |
| **Node Exporter (Monitoring)** | 16.170.74.58 | 9100 | http://16.170.74.58:9100 |

---

## 🔑 Identifiants

### Grafana
- **Username:** `admin`
- **Password:** `admin`

### Database PostgreSQL
- **User:** `postgres`
- **Database:** `hotel_ticket_hub`

### SSH
- **User:** `ubuntu`
- **Key:** `~/.ssh/oumayma-key.pem`

---

## 🚀 Commandes SSH Rapides

### Backend VM (13.63.15.86)
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
```

### Frontend VM (13.50.221.51)
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.50.221.51
```

### Database VM (13.48.83.147)
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.48.83.147
```

### Monitoring VM (16.170.74.58)
```bash
ssh -i ~/.ssh/oumayma-key.pem ubuntu@16.170.74.58
```

---

## ✅ Tests Rapides

```bash
# Backend
curl http://13.63.15.86:8081/actuator/health

# Frontend
curl http://13.50.221.51

# Grafana
curl http://16.170.74.58:3000/api/health

# Prometheus
curl http://16.170.74.58:9090/-/healthy

# Loki
curl http://16.170.74.58:3100/ready
```

---

**Dernière mise à jour:** 8 Février 2026
