#!/bin/bash
# Script de monitoring pour maintenir le backend actif
# À exécuter via cron toutes les 5 minutes: */5 * * * * /opt/hotel-ticket-hub-backend-staging/scripts/monitor-backend.sh

CONTAINER_NAME="hotel-ticket-hub-backend-staging"
COMPOSE_FILE="/opt/hotel-ticket-hub-backend-staging/docker-compose.yml"
LOG_FILE="/opt/hotel-ticket-hub-backend-staging/logs/monitor.log"
HEALTH_CHECK_URL="http://localhost:8081/actuator/health"

# Créer le répertoire de logs si nécessaire
mkdir -p "$(dirname "$LOG_FILE")"

# Fonction de logging
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Vérifier si le container existe et est en cours d'exécution
if ! docker ps --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
    log "WARNING: Container $CONTAINER_NAME is not running"
    
    # Vérifier si le container existe mais est arrêté
    if docker ps -a --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
        log "Container exists but is stopped. Attempting to start..."
        cd "$(dirname "$COMPOSE_FILE")" || exit 1
        docker compose -f "$COMPOSE_FILE" up -d backend
        sleep 10
        
        # Vérifier si le démarrage a réussi
        if docker ps --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
            log "SUCCESS: Container $CONTAINER_NAME started successfully"
        else
            log "ERROR: Failed to start container $CONTAINER_NAME"
            # Envoyer une notification (optionnel)
            # echo "Backend container failed to start" | mail -s "Backend Alert" admin@example.com
        fi
    else
        log "ERROR: Container $CONTAINER_NAME does not exist"
        log "Please check docker-compose.yml and ensure the container is properly configured"
    fi
else
    # Container est en cours d'exécution, vérifier la santé
    if curl -f -s "$HEALTH_CHECK_URL" > /dev/null 2>&1; then
        log "OK: Container $CONTAINER_NAME is running and healthy"
    else
        log "WARNING: Container $CONTAINER_NAME is running but health check failed"
        log "Attempting to restart container..."
        cd "$(dirname "$COMPOSE_FILE")" || exit 1
        docker compose -f "$COMPOSE_FILE" restart backend
        sleep 10
        
        if curl -f -s "$HEALTH_CHECK_URL" > /dev/null 2>&1; then
            log "SUCCESS: Container $CONTAINER_NAME restarted and is now healthy"
        else
            log "ERROR: Container $CONTAINER_NAME restarted but health check still failing"
        fi
    fi
fi

# Nettoyer les logs anciens (garder seulement les 7 derniers jours)
find "$(dirname "$LOG_FILE")" -name "monitor.log" -type f -mtime +7 -delete 2>/dev/null
