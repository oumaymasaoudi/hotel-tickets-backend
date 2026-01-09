# 🔍 Vérifier le Backend via GitHub Actions

## 🚨 Problème

Le backend n'est pas accessible depuis la VM monitoring. Il faut vérifier :
1. Si le backend est démarré
2. Si le backend écoute sur le port 8081
3. Si la configuration Docker est correcte

**Problème :** Pas d'accès SSH direct à la VM backend (clé manquante).

---

## ✅ Solution 1 : Vérifier via GitHub Actions

### Option A : Créer un Workflow de Vérification

Créez un workflow GitHub Actions qui se connecte à la VM backend et vérifie l'état :

```yaml
# .github/workflows/check-backend.yml
name: Check Backend Status

on:
  workflow_dispatch:  # Déclenchement manuel

jobs:
  check-backend:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      
      - name: Setup SSH
        run: |
          mkdir -p ~/.ssh
          echo "${{ secrets.BACKEND_SSH_KEY }}" > ~/.ssh/id_rsa
          chmod 600 ~/.ssh/id_rsa
          ssh-keyscan -H 13.49.44.219 >> ~/.ssh/known_hosts
      
      - name: Check Backend Status
        run: |
          ssh -i ~/.ssh/id_rsa ubuntu@13.49.44.219 << 'EOF'
            echo "=== Conteneurs Docker ==="
            docker ps | grep backend || echo "Backend non démarré"
            
            echo ""
            echo "=== Port 8081 ==="
            sudo ss -tlnp | grep 8081 || echo "Port 8081 non utilisé"
            
            echo ""
            echo "=== Test local ==="
            curl -s http://localhost:8081/actuator/health || echo "Backend non accessible localement"
            
            echo ""
            echo "=== Logs récents ==="
            docker logs hotel-ticket-hub-backend --tail=20 2>&1 || echo "Pas de logs"
          EOF
```

**Pour l'utiliser :**
1. Allez dans GitHub > Actions
2. Sélectionnez "Check Backend Status"
3. Cliquez sur "Run workflow"

---

## ✅ Solution 2 : Vérifier via l'API Publique

Si le backend expose une API publique (via le frontend), testez :

```powershell
# Depuis votre machine Windows
curl http://13.49.44.219:8081/api/health
# OU
curl http://51.21.196.104/api/health  # Si le frontend fait du proxy
```

---

## ✅ Solution 3 : Vérifier via AWS Systems Manager (SSM)

Si la VM backend a SSM activé :

1. AWS Console > EC2 > Instances
2. Sélectionnez la VM backend (13.49.44.219)
3. Actions > Connect > Session Manager
4. Exécutez :
   ```bash
   docker ps | grep backend
   sudo ss -tlnp | grep 8081
   curl http://localhost:8081/actuator/health
   ```

---

## ✅ Solution 4 : Vérifier via le Frontend

Si le frontend est accessible, vérifiez s'il peut communiquer avec le backend :

1. Ouvrez le frontend : `http://51.21.196.104`
2. Ouvrez la console du navigateur (F12)
3. Regardez les appels API
4. Si les appels échouent, le backend n'est probablement pas démarré

---

## 🎯 Action Immédiate Recommandée

### Option 1 : Utiliser GitHub Actions (si la clé SSH est dans les secrets)

1. Créez le workflow ci-dessus
2. Ajoutez la clé SSH dans GitHub Secrets : `BACKEND_SSH_KEY`
3. Exécutez le workflow

### Option 2 : Vérifier via AWS Console

1. AWS Console > EC2 > Instances
2. Sélectionnez la VM backend
3. Vérifiez l'onglet **Status checks**
4. Si "2/2 checks passed" → La VM est OK
5. Si "1/2 checks passed" → Problème système

### Option 3 : Redémarrer le Backend via GitHub Actions

Si vous avez un workflow de déploiement, relancez-le :

```yaml
# Déclenchez le workflow de déploiement backend
# Il devrait redémarrer le backend automatiquement
```

---

## 📋 Checklist

- [ ] Backend démarré : `docker ps | grep backend`
- [ ] Port 8081 écouté : `sudo ss -tlnp | grep 8081`
- [ ] Backend accessible localement : `curl http://localhost:8081/actuator/health`
- [ ] Security Group : port 8081 ouvert pour `13.62.53.224/32`
- [ ] Backend accessible depuis VM monitoring : `curl http://13.49.44.219:8081/actuator/health`

---

**La solution la plus rapide est de vérifier via GitHub Actions ou AWS Systems Manager !**

