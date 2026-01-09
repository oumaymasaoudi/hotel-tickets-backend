# 🔍 Tester la Connexion et Diagnostiquer l'Erreur HTTP 500

## ✅ Étape 1 : Tester la Connexion SSH

```powershell
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219
```

**Si ça fonctionne :** Vous serez connecté à la VM backend.

**Si ça ne fonctionne pas :** 
- Vérifiez que la clé a bien été ajoutée (attendez 1-2 minutes après l'ajout)
- Vérifiez que vous avez copié la clé complète (de `ssh-rsa` jusqu'à la fin)

---

## ✅ Étape 2 : Diagnostiquer l'Erreur HTTP 500

Une fois connecté, exécutez ces commandes :

### 2.1 Vérifier les Conteneurs

```bash
docker ps | grep backend
```

**Résultat attendu :** Le conteneur `hotel-ticket-hub-backend` devrait être listé.

### 2.2 Logs avec Erreurs

```bash
# Logs récents avec filtrage des erreurs
docker logs hotel-ticket-hub-backend --tail=100 | grep -i "error\|exception\|500\|actuator\|prometheus"
```

**Cherchez :**
- `500`
- `Internal Server Error`
- `Exception`
- `actuator`
- `prometheus`

### 2.3 Tous les Logs Récents

```bash
# Voir les 50 dernières lignes de logs
docker logs hotel-ticket-hub-backend --tail=50
```

### 2.4 Tester Localement

```bash
# Test health endpoint
curl -v http://localhost:8081/actuator/health

# Test Prometheus endpoint (celui qui retourne 500)
curl -v http://localhost:8081/actuator/prometheus
```

**Regardez la réponse :** Vous verrez l'erreur exacte retournée par le backend.

---

## ✅ Étape 3 : Solutions selon l'Erreur

### Erreur 1 : "Dependency missing" ou "ClassNotFoundException"

**Cause :** Dépendance Micrometer manquante.

**Solution :**
```bash
# Vérifier le pom.xml (si accessible)
cat /opt/backend/pom.xml | grep -i micrometer
```

Si la dépendance manque, il faut la rajouter dans le code et redéployer.

### Erreur 2 : "Configuration error" ou "Property missing"

**Cause :** Configuration Actuator incorrecte.

**Solution :**
```bash
# Vérifier la configuration
docker exec hotel-ticket-hub-backend cat /app/application.properties | grep -i actuator
```

### Erreur 3 : "OutOfMemoryError" ou "Heap space"

**Cause :** Problème de mémoire JVM.

**Solution :**
```bash
# Vérifier l'utilisation mémoire
docker stats hotel-ticket-hub-backend --no-stream
```

### Erreur 4 : Exception dans le code

**Cause :** Bug dans le code backend.

**Solution :** Regardez les logs pour voir la stack trace complète.

---

## 📋 Checklist de Diagnostic

- [ ] Connexion SSH réussie
- [ ] Conteneur backend démarré
- [ ] Logs analysés (erreurs identifiées)
- [ ] Test local `/actuator/prometheus` exécuté
- [ ] Cause de l'erreur HTTP 500 identifiée
- [ ] Solution appliquée

---

## 🎯 Commandes Rapides (Copier-Coller)

```bash
# Une fois connecté, exécutez ces commandes dans l'ordre :

# 1. Vérifier les conteneurs
docker ps | grep backend

# 2. Logs avec erreurs
docker logs hotel-ticket-hub-backend --tail=100 | grep -i "error\|exception\|500"

# 3. Test Prometheus endpoint
curl -v http://localhost:8081/actuator/prometheus 2>&1 | head -50

# 4. Tous les logs récents
docker logs hotel-ticket-hub-backend --tail=50
```

---

**Commencez par tester la connexion SSH, puis exécutez les commandes de diagnostic !**

