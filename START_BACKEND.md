# Guide Rapide - Démarrer le Backend

## Problème : ERR_CONNECTION_REFUSED

Si vous voyez `ERR_CONNECTION_REFUSED`, le backend n'est pas démarré.

## Solution Rapide

### Option 1 : Avec Maven (Recommandé)

```bash
cd hotel-ticket-hub-backend
mvn spring-boot:run
```

### Option 2 : Avec Java directement

```bash
cd hotel-ticket-hub-backend
mvn clean package
java -jar target/hotel-ticket-hub-backend-*.jar
```

### Option 3 : Avec IntelliJ IDEA / Eclipse

1. Ouvrez le projet dans votre IDE
2. Trouvez la classe `Application.java` ou `HotelTicketHubApplication.java`
3. Clic droit → Run

## Vérifier que le backend fonctionne

Une fois démarré, vous devriez voir :
- `Started HotelTicketHubApplication in X.XXX seconds`
- Le serveur écoute sur `http://localhost:8080`

Testez avec :
```bash
curl http://localhost:8080/api/auth/health
```

## Prérequis

1. **Java 17+** installé
   ```bash
   java -version
   ```

2. **Maven** installé
   ```bash
   mvn -version
   ```

3. **PostgreSQL** démarré et base de données créée
   - Base : `hotel_ticket_hub`
   - User : `postgres`
   - Password : `postgres`

## Configuration

Le fichier `application.properties` doit contenir :
```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/hotel_ticket_hub
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## Dépannage

### Port 8080 déjà utilisé
Changez le port dans `application.properties` :
```properties
server.port=8081
```

### Erreur de connexion à la base de données
Vérifiez que PostgreSQL est démarré :
```bash
# Windows
net start postgresql-x64-XX

# Linux/Mac
sudo systemctl start postgresql
```

### Erreurs de compilation
```bash
mvn clean install
```

