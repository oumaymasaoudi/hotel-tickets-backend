# 🔍 Diagnostic : Out of Memory (OOM) sur la VM Backend

## ❌ Problème

```
Out of memory: Killed process 1588 (java) total-vm:1867400kB
```

Le processus Java de l'application backend a été tué par le système car il n'y avait plus assez de mémoire disponible.

## 🔍 Analyse

### Instance Actuelle
- **Type** : `t3.micro`
- **RAM** : 1 Go (1024 MB)
- **CPU** : 2 vCPU (burst)

### Consommation Mémoire
- **Java Process** : ~1.8 Go de mémoire virtuelle demandée
- **RAM Disponible** : ~900 Mo (après système)
- **Résultat** : Le système tue le processus Java (OOM Killer)

## ✅ Solutions

### Solution 1 : Augmenter la Taille de l'Instance (Recommandé)

**Architecture :**
- **VM Backend** : Uniquement l'application Spring Boot (backend)
- **VM Monitoring/Ansible** : Prometheus, Grafana, Alertmanager, Node Exporter, cAdvisor

**Option A : t3.small (2 Go RAM) - RECOMMANDÉ**
- Coût : ~$15/mois
- RAM : 2 Go
- **Suffisant pour une application Spring Boot seule** (sans monitoring)
- Recommandé pour staging avec cette architecture

**Option B : t3.medium (4 Go RAM)**
- Coût : ~$30/mois
- RAM : 4 Go
- Recommandé pour la production ou si l'application est très gourmande

**Étapes :**

1. **AWS Console** → **EC2** → **Instances**
2. Sélectionnez l'instance backend
3. **Actions** → **Instance State** → **Stop**
4. Attendez que l'instance soit arrêtée
5. **Actions** → **Instance Settings** → **Change Instance Type**
6. Sélectionnez **`t3.small`** (2 Go RAM - suffisant pour backend seul)
7. **Apply**
8. **Actions** → **Instance State** → **Start**

⚠️ **Note** : L'IP publique peut changer si vous n'utilisez pas d'Elastic IP.

### Solution 2 : Optimiser la Configuration Java (Temporaire)

Limiter la mémoire utilisée par Java dans `docker-compose.yml` :

```yaml
services:
  backend:
    image: ghcr.io/votre-org/ticket-hub-backend:latest
    environment:
      - JAVA_OPTS=-Xmx512m -Xms256m  # Limite à 512 Mo max
      - SPRING_PROFILES_ACTIVE=prod
    # ... autres configs
```

**Limites recommandées :**
- **t3.micro (1 Go)** : `-Xmx512m -Xms256m -XX:MaxMetaspaceSize=128m` ⚠️ Risqué
- **t3.small (2 Go)** : `-Xmx1024m -Xms512m -XX:MaxMetaspaceSize=256m` ✅ Recommandé
- **t3.medium (4 Go)** : `-Xmx2048m -Xms1024m -XX:MaxMetaspaceSize=512m` ✅ Production

⚠️ **Attention** : Sur t3.micro, cela peut causer des problèmes de performance. **t3.small est le minimum recommandé** pour une application Spring Boot seule.

### Solution 3 : Utiliser une Instance avec Plus de RAM

**Alternatives :**
- `t3a.small` : 2 Go RAM (AMD, moins cher)
- `t3a.medium` : 4 Go RAM
- `t4g.small` : 2 Go RAM (ARM, moins cher)

## 🧪 Vérification

### Vérifier la Mémoire Disponible

```bash
# Sur la VM
free -h
df -h
```

### Vérifier les Logs OOM

```bash
# Voir les processus tués par OOM
dmesg | grep -i "out of memory"
journalctl -k | grep -i "killed process"
```

### Vérifier la Consommation Java

```bash
# Si l'application tourne
docker stats
# OU
ps aux | grep java
```

## 📋 Checklist de Résolution

- [ ] **Arrêter l'instance** (si changement de type)
- [ ] **Changer le type d'instance** vers `t3.small` ou `t3.medium`
- [ ] **Redémarrer l'instance**
- [ ] **Vérifier l'IP publique** (mettre à jour `STAGING_HOST` si nécessaire)
- [ ] **Tester la connexion SSH**
- [ ] **Redéployer l'application**
- [ ] **Vérifier que l'application démarre sans OOM**

## 🚀 Action Immédiate Recommandée

1. **AWS Console** → **EC2** → **Instances**
2. Trouvez l'instance backend (`13.49.44.219`)
3. **Actions** → **Instance State** → **Stop**
4. Attendez que l'état soit **Stopped**
5. **Actions** → **Instance Settings** → **Change Instance Type**
6. Sélectionnez **t3.small** (2 Go RAM)
7. **Apply**
8. **Actions** → **Instance State** → **Start**
9. Attendez que l'instance soit **Running**
10. Vérifiez la nouvelle **IPv4 Public IP**
11. Mettez à jour le secret `STAGING_HOST` dans GitHub si l'IP a changé
12. Testez la connexion SSH :
    ```powershell
    ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@NOUVELLE_IP "echo OK"
    ```

## 💡 Configuration Docker Compose Optimisée

Pour éviter les problèmes de mémoire, ajoutez des limites dans `docker-compose.yml` :

```yaml
services:
  backend:
    image: ghcr.io/votre-org/ticket-hub-backend:latest
    mem_limit: 1.5g  # Limite Docker à 1.5 Go
    mem_reservation: 512m  # Réservation minimale
    environment:
      - JAVA_OPTS=-Xmx1024m -Xms512m -XX:MaxMetaspaceSize=256m
    deploy:
      resources:
        limits:
          memory: 1.5G
        reservations:
          memory: 512M
```

## ⚠️ Notes Importantes

1. **Elastic IP** : Si vous changez le type d'instance, l'IP peut changer. Utilisez une Elastic IP pour éviter ce problème.

2. **Coût** : 
   - `t3.micro` : ~$7.50/mois
   - `t3.small` : ~$15/mois
   - `t3.medium` : ~$30/mois

3. **Performance** : Une instance plus grande améliore aussi les performances CPU.

---

**Une fois l'instance agrandie, l'application devrait fonctionner sans problème d'OOM !** 🚀

