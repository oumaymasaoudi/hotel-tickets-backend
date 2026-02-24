# Guide : Augmenter la taille du volume EBS pour le monitoring

## 🚨 Problème actuel

Le volume EBS de la VM de monitoring est trop petit (6.8GB), ce qui cause :
- ❌ Échec des déploiements (disque plein à 100%)
- ❌ Impossible de pull de nouvelles images Docker
- ⚠️ Risque de corruption de données

## ✅ Solution : Augmenter le volume EBS à 20GB minimum

### Étape 1 : Identifier le volume EBS

1. Connectez-vous à la [Console AWS](https://console.aws.amazon.com/ec2/)
2. Allez dans **EC2 > Volumes**
3. Trouvez le volume attaché à votre instance de monitoring
   - Filtrez par **Instance ID** ou **Tag** (ex: `Name=monitoring-vm`)
   - Notez le **Volume ID** (ex: `vol-0123456789abcdef0`)

### Étape 2 : Modifier la taille du volume

1. Sélectionnez le volume
2. Clic droit > **Modify Volume** (ou Actions > Modify Volume)
3. Changez la taille de **6.8 GB** à **20 GB** (minimum recommandé)
4. Cliquez sur **Modify**
5. Confirmez la modification

**⏱️ Temps d'attente :** Quelques secondes à quelques minutes

### Étape 3 : Étendre le système de fichiers (sur la VM)

**⚠️ IMPORTANT :** Après avoir modifié le volume, vous devez étendre le système de fichiers sur la VM.

1. **SSH vers la VM de monitoring :**
   ```bash
   ssh -i ~/.ssh/your-key.pem ubuntu@MONITORING_HOST_IP
   ```

2. **Vérifier le nom du device :**
   ```bash
   lsblk
   ```
   Vous devriez voir quelque chose comme :
   ```
   NAME    MAJ:MIN RM SIZE RO TYPE MOUNTPOINT
   xvda    202:0    0  20G  0 disk
   └─xvda1 202:1    0  6.8G  0 part /
   ```

3. **Étendre la partition :**
   ```bash
   sudo growpart /dev/xvda 1
   ```
   Ou si c'est `/dev/nvme0n1` :
   ```bash
   sudo growpart /dev/nvme0n1 1
   ```

4. **Étendre le système de fichiers :**
   ```bash
   sudo resize2fs /dev/xvda1
   ```
   Ou :
   ```bash
   sudo resize2fs /dev/nvme0n1p1
   ```

5. **Vérifier l'espace disponible :**
   ```bash
   df -h /
   ```
   Vous devriez voir :
   ```
   Filesystem      Size  Used Avail Use% Mounted on
   /dev/xvda1       20G  6.7G   13G  35% /
   ```

### Étape 4 : Vérifier que tout fonctionne

```bash
# Vérifier l'espace disque
df -h /

# Vérifier l'utilisation Docker
docker system df

# Tester un pull d'image
docker pull hello-world
docker rmi hello-world
```

## 🔧 Alternative : Nettoyage manuel (temporaire)

Si vous ne pouvez pas augmenter le volume immédiatement :

```bash
# SSH vers la VM
ssh -i ~/.ssh/your-key.pem ubuntu@MONITORING_HOST_IP

# Arrêter les services monitoring temporairement
cd /opt/monitoring
docker compose -f docker-compose.monitoring.yml down

# Nettoyer agressivement (⚠️ supprime les images non utilisées)
docker system prune -af

# Redémarrer les services
docker compose -f docker-compose.monitoring.yml up -d
```

**⚠️ ATTENTION :** Cette méthode supprime les images Docker non utilisées. Les services seront recréés au prochain déploiement.

## 📊 Taille recommandée par environnement

| Environnement | Taille recommandée | Raison |
|---------------|-------------------|--------|
| **Monitoring** | 20 GB minimum | Images Docker + volumes Prometheus/Grafana |
| **Staging** | 15 GB minimum | Images Docker + logs |
| **Production** | 30 GB minimum | Images Docker + logs + backups |

## 🔗 Références

- [AWS Documentation: Modify EBS Volume](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ebs-modify-volume.html)
- [AWS Documentation: Extend Linux File System](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/recognize-expanded-volume-linux.html)
