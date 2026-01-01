# 🚀 Optimiser les Déploiements - Quand Rebuild ?

## 📋 Types de Changements

### ✅ Changements qui nécessitent un REBUILD (Pipeline)

Ces changements modifient le code compilé et nécessitent un rebuild de l'image Docker :

1. **Modifications du code Java** (comme la correction CORS)
   - ✅ Solution : Commit → Push → Pipeline rebuild automatiquement
   - ⏱️ Temps : 5-10 minutes

2. **Modifications des dépendances** (`pom.xml`)
   - ✅ Solution : Commit → Push → Pipeline
   - ⏱️ Temps : 5-10 minutes

3. **Modifications de la configuration Spring** (fichiers `.properties` dans le code)
   - ✅ Solution : Commit → Push → Pipeline
   - ⏱️ Temps : 5-10 minutes

### ⚡ Changements qui ne nécessitent PAS de rebuild

Ces changements utilisent des variables d'environnement et nécessitent seulement un redémarrage :

1. **Variables d'environnement** (`.env` sur la VM)
   - ✅ Solution : Modifier `.env` → `docker compose restart`
   - ⏱️ Temps : 30 secondes

2. **Configuration Docker Compose**
   - ✅ Solution : Modifier `docker-compose.yml` → `docker compose up -d`
   - ⏱️ Temps : 1 minute

---

## 🎯 Pour la Correction CORS Actuelle

### Option 1 : Pipeline (Recommandé pour production)

```powershell
# Commit et push
cd C:\Users\oumay\projet\hotel-ticket-hub-backend
git add .
git commit -m "fix: add frontend staging URL to CORS allowed origins"
git push origin develop
```

Le pipeline va :
1. Build l'image avec le nouveau code
2. Push vers GHCR
3. Déployer automatiquement (si configuré)

⏱️ **Temps** : 5-10 minutes

### Option 2 : Build Local et Push Manuel (Plus rapide pour test)

```powershell
# Build localement
cd C:\Users\oumay\projet\hotel-ticket-hub-backend
docker build -t ghcr.io/oumaymasaoudi/hotel-tickets-backend/backend:develop .

# Login à GHCR
echo "<GHCR_TOKEN>" | docker login ghcr.io -u oumaymasaoudi --password-stdin

# Push
docker push ghcr.io/oumaymasaoudi/hotel-tickets-backend/backend:develop

# Sur la VM backend, pull et restart
ssh ubuntu@13.49.44.219
cd /opt/hotel-ticket-hub-backend-staging
docker compose pull
docker compose up -d
```

⏱️ **Temps** : 3-5 minutes (si build local rapide)

---

## 💡 Optimisation Future : Utiliser des Variables d'Environnement

Pour éviter les rebuilds fréquents, vous pouvez rendre CORS configurable via variable d'environnement :

### Modifier `SecurityConfig.java` pour lire depuis l'environnement :

```java
@Value("${cors.allowed.origins:http://localhost:5173}")
private String corsAllowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Lire depuis variable d'environnement
    List<String> origins = Arrays.asList(corsAllowedOrigins.split(","));
    configuration.setAllowedOrigins(origins);
    
    // ... reste du code
}
```

Ainsi, vous pourrez modifier CORS via `.env` sans rebuild :
```bash
# Sur la VM, modifier .env
CORS_ALLOWED_ORIGINS=http://51.21.196.104,http://localhost:5173

# Redémarrer
docker compose restart
```

---

## 📊 Comparaison

| Méthode | Temps | Quand l'utiliser |
|---------|-------|------------------|
| **Pipeline GitHub Actions** | 5-10 min | ✅ Production, changements de code |
| **Build local + Push** | 3-5 min | ⚡ Tests rapides |
| **Variables d'env** | 30 sec | 🚀 Configurations fréquentes |

---

## ✅ Recommandation

Pour **maintenant** (correction CORS) : Utilisez le **Pipeline** (Option 1) car c'est du code Java qui doit être recompilé.

Pour **plus tard** : Modifiez le code pour utiliser des variables d'environnement pour CORS, ainsi vous pourrez modifier sans rebuild.

