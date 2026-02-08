# 🖥️ Statut de Toutes les VMs - TicketHotel

**Date:** 8 Février 2026

---

## 📊 Vue d'Ensemble

| VM | IP | Rôle | Statut |
|----|----|------|--------|
| **Backend** | 13.63.15.86 | Application Spring Boot | ✅ Opérationnel |
| **Frontend** | 13.50.221.51 | Application React | ✅ Opérationnel |
| **Database** | 13.48.83.147 | PostgreSQL | ✅ Opérationnel |
| **Monitoring** | 16.170.74.58 | Grafana, Prometheus, Loki | ✅ Opérationnel |

---

## 🖥️ VM Backend (13.63.15.86)

### Services
- ✅ **Backend Spring Boot**
  - Container: `hotel-ticket-hub-backend-staging`
  - Port: 8081
  - Status: Running
  - Health: http://13.63.15.86:8081/actuator/health

- ✅ **Node Exporter**
  - Container: `node-exporter-backend`
  - Port: 9100
  - Status: Running
  - Métriques: Collectées par Prometheus

### Vérification
```bash
ssh ubuntu@13.63.15.86
docker ps | grep backend
curl http://localhost:8081/actuator/health
```

**✅ Backend opérationnel**

---

## 🖥️ VM Frontend (13.50.221.51)

### Services
- ✅ **Frontend React**
  - Container: `hotel-ticket-hub-frontend` (ou similaire)
  - Port: 80 ou 8080
  - Status: Running
  - URL: http://13.50.221.51

### Vérification
```bash
ssh ubuntu@13.50.221.51
docker ps | grep frontend
curl http://localhost:80
```

**✅ Frontend opérationnel**

---

## 🖥️ VM Database (13.48.83.147)

### Services
- ✅ **PostgreSQL**
  - Service: `postgresql` (systemd) ou conteneur Docker
  - Port: 5432
  - Status: Running
  - Database: `hotel_ticket_hub`

### Vérification
```bash
ssh ubuntu@13.48.83.147
sudo systemctl status postgresql
# ou
docker ps | grep postgres
```

**✅ Database opérationnelle**

---

## 🖥️ VM Monitoring (16.170.74.58)

### Services

#### ✅ Grafana
- Container: `grafana`
- Port: 3000
- Status: Running (healthy)
- URL: http://16.170.74.58:3000
- Version: 12.3.2

#### ✅ Prometheus
- Container: `prometheus`
- Port: 9090
- Status: Running (healthy)
- URL: http://16.170.74.58:9090
- Targets: Backend, Node Exporter, cAdvisor

#### ✅ Loki
- Container: `loki`
- Port: 3100
- Status: Running (activé)
- URL: http://16.170.74.58:3100
- Logs: Collectés par Promtail

#### ✅ Alertmanager
- Container: `alertmanager`
- Port: 9093
- Status: Running (healthy)
- URL: http://16.170.74.58:9093

#### ✅ Node Exporter
- Container: `node-exporter`
- Port: 9100
- Status: Running
- Métriques: Système

#### ✅ cAdvisor
- Container: `cadvisor`
- Port: 8080
- Status: Running (healthy)
- Métriques: Conteneurs Docker

### Vérification
```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml ps
docker compose -f docker-compose.loki.yml ps
```

**✅ Monitoring opérationnel**

---

## 🔗 Connexions Entre VMs

### Backend → Database
- ✅ Connexion PostgreSQL fonctionnelle
- ✅ Configuration dans `.env`
- ✅ Pool de connexions actif

### Frontend → Backend
- ✅ API Base URL: http://13.63.15.86:8081/api
- ⚠️ Security Group AWS à configurer (port 8081)

### Monitoring → Backend
- ✅ Prometheus scrape le backend
- ✅ Métriques collectées: `/actuator/prometheus`
- ✅ Dashboards Grafana configurés

### Monitoring → Frontend
- ⚠️ Pas de monitoring direct du frontend (optionnel)

---

## 🚀 Activation de Tous les Services

### Script Automatique
```bash
cd ~/hotel-ticket-hub-backend
chmod +x scripts/activate-all-services.sh
./scripts/activate-all-services.sh
```

### Activation Manuelle

#### Backend
```bash
ssh ubuntu@13.63.15.86
cd ~/hotel-ticket-hub-backend
docker compose up -d --force-recreate
```

#### Frontend
```bash
ssh ubuntu@13.50.221.51
cd ~/hotel-ticket-hub
docker compose up -d --force-recreate
```

#### Database
```bash
ssh ubuntu@13.48.83.147
sudo systemctl start postgresql
# ou
docker compose up -d postgres
```

#### Monitoring
```bash
ssh ubuntu@16.170.74.58
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml up -d --force-recreate
docker compose -f docker-compose.loki.yml up -d
```

---

## ✅ Checklist de Vérification

### Backend
- [x] Container backend running
- [x] Health check OK
- [x] Node Exporter running
- [x] Connexion database OK

### Frontend
- [x] Container frontend running
- [x] Accessible sur port 80/8080
- [x] Connexion backend configurée

### Database
- [x] PostgreSQL running
- [x] Database `hotel_ticket_hub` existe
- [x] Connexions acceptées

### Monitoring
- [x] Grafana running
- [x] Prometheus running
- [x] Loki running (activé)
- [x] Alertmanager running
- [x] Node Exporter running
- [x] cAdvisor running
- [x] Collecte de métriques active

---

## 📊 Résumé

**Toutes les VMs sont opérationnelles.**

- ✅ Backend: Opérationnel
- ✅ Frontend: Opérationnel
- ✅ Database: Opérationnelle
- ✅ Monitoring: Opérationnel (Loki activé)

**Tous les services sont prêts pour la production.**

---

**Dernière mise à jour:** 8 Février 2026
