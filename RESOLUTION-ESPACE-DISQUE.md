# ✅ Résolution: Problème d'Espace Disque - Backend

**Date:** 8 Février 2026

---

## 🔴 Problème

Le déploiement du backend a échoué avec l'erreur:
```
failed to copy: failed to send write: write /var/lib/containerd/io.containerd.content.v1.content/ingest/.../data: 
no space left on device
```

**Cause:** Le disque de la VM Backend était plein (100% utilisé, 38MB disponibles sur 6.8GB).

---

## ✅ Solution Appliquée

### 1. Nettoyage Docker

```bash
ssh ubuntu@13.63.15.86
docker system prune -af --volumes
docker builder prune -af
```

**Résultat:**
- ✅ **870.2MB libérés**
- ✅ Espace disque: **73% utilisé** (1.9GB disponibles)

### 2. Redémarrage du Backend

```bash
cd /opt/hotel-ticket-hub-backend-staging
docker compose pull
docker compose up -d --force-recreate
```

**Résultat:**
- ✅ Backend démarré avec succès
- ✅ Health check: `{"status":"UP"}`
- ✅ Port 8081 accessible

---

## 📊 Statut Final

### VM Backend (13.63.15.86)

**Espace Disque:**
- Avant: 100% utilisé (38MB disponibles)
- Après: 73% utilisé (1.9GB disponibles)

**Services:**
- ✅ Backend Spring Boot: Running
- ✅ Node Exporter: Running
- ✅ Health Check: OK

**Connexion Prometheus:**
- ✅ Backend accessible depuis VM Monitoring
- ✅ Endpoint `/actuator/prometheus` fonctionnel

---

## 🔧 Script Automatique

Un script a été créé pour automatiser le nettoyage:

```bash
cd ~/hotel-ticket-hub-backend
chmod +x scripts/fix-disk-space-backend.sh
./scripts/fix-disk-space-backend.sh
```

**Ce que fait le script:**
1. Supprime les conteneurs arrêtés
2. Supprime les images Docker non utilisées
3. Supprime les volumes non utilisés
4. Nettoie le build cache
5. Affiche l'espace libéré

---

## 📝 Prévention

### Surveillance de l'Espace Disque

Ajouter un monitoring pour alerter quand l'espace disque dépasse 80%:

```bash
# Vérifier l'espace disque
df -h /

# Vérifier l'utilisation Docker
docker system df
```

### Nettoyage Automatique

Ajouter un cron job pour nettoyer automatiquement:

```bash
# Éditer crontab
crontab -e

# Ajouter (nettoyage hebdomadaire le dimanche à 2h du matin)
0 2 * * 0 docker system prune -af --volumes > /var/log/docker-cleanup.log 2>&1
```

---

## ✅ Conclusion

**Problème résolu:**
- ✅ Espace disque libéré (870MB)
- ✅ Backend redémarré avec succès
- ✅ Prometheus peut maintenant scraper les métriques

**L'infrastructure est opérationnelle.**

---

**Dernière mise à jour:** 8 Février 2026
