# 🔍 Connexion Backend et Diagnostic HTTP 500

## 🚨 Problème Actuel

**Prometheus erreur :** `server returned HTTP status 500`

Cela signifie que :
- ✅ Prometheus peut accéder au backend (plus de timeout)
- ❌ Le backend retourne une erreur 500 sur `/actuator/prometheus`

**Cause probable :** Problème dans le code backend ou configuration Actuator.

---

## ✅ Solution 1 : Utiliser le Workflow GitHub Actions

J'ai créé un workflow qui vérifie l'état du backend automatiquement.

### Étape 1 : Vérifier que le Secret existe

1. **Allez dans GitHub** > votre repo > **Settings** > **Secrets and variables** > **Actions**
2. **Vérifiez** que `STAGING_SSH_PRIVATE_KEY` existe
3. Si elle n'existe pas, vous devez l'ajouter (voir Solution 2)

### Étape 2 : Exécuter le Workflow

1. **Allez dans GitHub** > **Actions**
2. **Sélectionnez** "Check Backend Status"
3. **Cliquez sur** "Run workflow" (bouton en haut à droite)
4. **Sélectionnez** la branche `develop` ou `main`
5. **Cliquez sur** "Run workflow"

Le workflow va :
- Se connecter à la VM backend
- Vérifier les conteneurs Docker
- Tester l'accès local
- Afficher les logs récents

---

## ✅ Solution 2 : Récupérer la Clé SSH depuis GitHub Secrets

### Option A : Via GitHub CLI (recommandé)

```powershell
# Installer GitHub CLI si pas déjà fait
# winget install GitHub.cli

# Se connecter à GitHub
gh auth login

# Récupérer le secret (nécessite les permissions)
gh secret list
# Note: Les secrets ne peuvent pas être lus directement pour des raisons de sécurité
```

**Limitation :** GitHub ne permet pas de lire les secrets directement pour des raisons de sécurité.

### Option B : Utiliser le Workflow pour Exporter la Clé

Créez un workflow temporaire pour exporter la clé (⚠️ **ATTENTION : Sécurité**):

```yaml
# .github/workflows/export-ssh-key.yml (TEMPORAIRE - À SUPPRIMER APRÈS)
name: Export SSH Key (TEMPORAIRE)

on:
  workflow_dispatch:

jobs:
  export-key:
    runs-on: ubuntu-latest
    steps:
      - name: Export SSH Key
        run: |
          echo "${{ secrets.STAGING_SSH_PRIVATE_KEY }}" > key.pem
          chmod 600 key.pem
          # ⚠️ NE PAS COMMITTER CETTE CLÉ !
          # Copiez-la manuellement depuis les logs du workflow
          cat key.pem
```

**⚠️ IMPORTANT :** Supprimez ce workflow immédiatement après avoir récupéré la clé !

---

## ✅ Solution 3 : Diagnostiquer l'Erreur HTTP 500

### Via le Workflow GitHub Actions

Le workflow `check-backend-status.yml` affichera les logs. Regardez les erreurs dans les logs.

### Via SSH (si vous avez la clé)

```powershell
# Se connecter à la VM backend
ssh -i <votre-clé> ubuntu@13.49.44.219

# Vérifier les logs du backend
docker logs hotel-ticket-hub-backend --tail=100 | grep -i error

# Vérifier les logs Actuator spécifiquement
docker logs hotel-ticket-hub-backend 2>&1 | grep -i "actuator\|prometheus\|500"

# Tester localement
curl -v http://localhost:8081/actuator/health
curl -v http://localhost:8081/actuator/prometheus

# Vérifier la configuration
docker exec hotel-ticket-hub-backend cat /app/application.properties | grep -i actuator
```

---

## ✅ Solution 4 : Causes Courantes d'Erreur 500 sur /actuator/prometheus

### 1. Dépendance Micrometer manquante

**Vérifier dans `pom.xml` :**
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 2. Configuration Actuator incorrecte

**Vérifier dans `application.properties` :**
```properties
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.metrics.export.prometheus.enabled=true
```

### 3. Problème de mémoire JVM

Si le backend manque de mémoire, Actuator peut planter.

**Vérifier :**
```bash
docker stats hotel-ticket-hub-backend
```

### 4. Exception non gérée dans le code

Les logs Docker contiendront l'exception exacte.

---

## 🎯 Action Immédiate Recommandée

### 1. Exécuter le Workflow GitHub Actions

1. **GitHub** > **Actions** > **Check Backend Status** > **Run workflow**
2. **Attendez** la fin de l'exécution
3. **Regardez** les logs pour voir les erreurs

### 2. Si le Workflow ne fonctionne pas

**Vérifiez que le secret existe :**
- GitHub > Settings > Secrets and variables > Actions
- Cherchez `STAGING_SSH_PRIVATE_KEY`

**Si le secret n'existe pas :**
- Vous devez le créer avec la clé SSH privée de la VM backend
- Ou utiliser une autre méthode d'accès (AWS Systems Manager, etc.)

### 3. Analyser les Logs

Une fois les logs récupérés, cherchez :
- `Exception`
- `Error`
- `500`
- `actuator`
- `prometheus`

---

## 📋 Checklist

- [ ] Workflow "Check Backend Status" exécuté
- [ ] Logs du backend analysés
- [ ] Erreur HTTP 500 identifiée
- [ ] Solution appliquée (redémarrage, configuration, etc.)
- [ ] Prometheus target `backend` : **UP** (vert)
- [ ] Dashboards Grafana affichent des données

---

## 🔧 Commandes Rapides

### Depuis la VM Monitoring (pour tester)

```bash
# Se connecter à la VM monitoring
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.62.53.224

# Tester avec plus de détails
curl -v http://13.49.44.219:8081/actuator/prometheus 2>&1 | head -50
```

### Via GitHub Actions Workflow

1. **Actions** > **Check Backend Status** > **Run workflow**
2. Regardez les logs de l'étape "Check Backend Status"

---

**Commencez par exécuter le workflow GitHub Actions pour voir les logs du backend !**

