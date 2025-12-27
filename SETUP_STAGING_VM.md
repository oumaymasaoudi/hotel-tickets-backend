# 🚀 Configuration de la VM Staging - Backend

## ✅ Étape 1 : Se connecter à la VM

```powershell
cd hotel-ticket-hub-backend
ssh -i github-actions-key ubuntu@13.49.44.219
```

## ✅ Étape 2 : Installer Java 17

Une fois connecté sur la VM, exécutez :

```bash
# Mettre à jour le système
sudo apt update

# Installer Java 17
sudo apt install -y openjdk-17-jdk

# Vérifier l'installation
java -version
```

## ✅ Étape 3 : Créer les répertoires

```bash
# Dossier de déploiement staging
sudo mkdir -p /opt/hotel-ticket-hub-backend-staging
sudo chown -R ubuntu:ubuntu /opt/hotel-ticket-hub-backend-staging

# Dossier logs
sudo mkdir -p /var/log/hotel-ticket-hub-backend-staging
sudo chown -R ubuntu:ubuntu /var/log/hotel-ticket-hub-backend-staging
```

## ✅ Étape 4 : Créer le fichier d'environnement

**📖 Consultez `CONFIGURATION_ENV_STAGING.md` pour comprendre d'où viennent ces valeurs.**

### Exemple minimal (sans base de données pour l'instant)

```bash
sudo tee /opt/hotel-ticket-hub-backend-staging/.env >/dev/null <<'EOF'
SPRING_PROFILES_ACTIVE=staging
SERVER_PORT=8081
EOF
```

### Exemple complet (avec base de données)

Si vous avez une base de données PostgreSQL sur la VM `data-staging` (13.61.27.43) :

```bash
sudo tee /opt/hotel-ticket-hub-backend-staging/.env >/dev/null <<'EOF'
SPRING_PROFILES_ACTIVE=staging
SERVER_PORT=8081
SPRING_DATASOURCE_URL=jdbc:postgresql://13.61.27.43:5432/hotel_ticket_hub
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=votre_mot_de_passe_ici
CORS_ALLOWED_ORIGINS=http://51.21.196.104,http://localhost:5173
APP_FRONTEND_URL=http://51.21.196.104
EOF
```

**⚠️ Important** : 
- Remplacez `votre_mot_de_passe_ici` par le vrai mot de passe de votre base de données
- Adaptez l'URL de la base de données selon votre configuration
- Voir `CONFIGURATION_ENV_STAGING.md` pour plus de détails

```bash
sudo chown ubuntu:ubuntu /opt/hotel-ticket-hub-backend-staging/.env
sudo chmod 600 /opt/hotel-ticket-hub-backend-staging/.env
```

## ✅ Étape 5 : Créer le service systemd

```bash
sudo tee /etc/systemd/system/hotel-ticket-hub-backend-staging.service >/dev/null <<'EOF'
[Unit]
Description=Hotel Ticket Hub Backend (STAGING)
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/opt/hotel-ticket-hub-backend-staging

EnvironmentFile=/opt/hotel-ticket-hub-backend-staging/.env
ExecStart=/usr/bin/java -jar /opt/hotel-ticket-hub-backend-staging/app.jar

SuccessExitStatus=143
Restart=always
RestartSec=5

StandardOutput=append:/var/log/hotel-ticket-hub-backend-staging/app.log
StandardError=append:/var/log/hotel-ticket-hub-backend-staging/app-error.log

[Install]
WantedBy=multi-user.target
EOF

# Recharger systemd et activer le service
sudo systemctl daemon-reload
sudo systemctl enable hotel-ticket-hub-backend-staging
```

## ✅ Étape 6 : Vérifier la configuration

```bash
# Vérifier que le service est bien créé
sudo systemctl status hotel-ticket-hub-backend-staging --no-pager

# Vérifier les permissions
ls -la /opt/hotel-ticket-hub-backend-staging/
ls -la /var/log/hotel-ticket-hub-backend-staging/
```

## ✅ Étape 7 : Configurer les Security Groups AWS

Dans la console AWS EC2 :
- **Port 8081** : Autoriser depuis `0.0.0.0/0` (ou restreindre selon vos besoins)
- **Port 22 (SSH)** : Autoriser depuis votre IP uniquement

## 🎯 Résultat

Une fois tout configuré, le service sera prêt à recevoir les déploiements automatiques via GitHub Actions.

Le JAR déployé sera toujours nommé `app.jar` dans `/opt/hotel-ticket-hub-backend-staging/`.

## 📝 Commandes utiles

```bash
# Vérifier le statut du service
sudo systemctl status hotel-ticket-hub-backend-staging

# Voir les logs
tail -f /var/log/hotel-ticket-hub-backend-staging/app.log

# Voir les erreurs
tail -f /var/log/hotel-ticket-hub-backend-staging/app-error.log

# Redémarrer le service
sudo systemctl restart hotel-ticket-hub-backend-staging

# Tester l'API (si health endpoint existe)
curl -i http://localhost:8081/actuator/health
```

