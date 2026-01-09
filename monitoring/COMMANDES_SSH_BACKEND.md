# 🔐 Commandes SSH pour la VM Backend

## ✅ Commande de Connexion

```powershell
ssh -i C:\Users\oumay\.ssh\github-actions-key ubuntu@13.49.44.219
```

---

## 🔍 Vérifier si la Clé Existe

```powershell
# Vérifier si le fichier existe
Test-Path C:\Users\oumay\.ssh\github-actions-key

# Lister les clés SSH disponibles
Get-ChildItem C:\Users\oumay\.ssh\* -File | Select-Object Name
```

---

## 📋 Commandes Utiles une Fois Connecté

```bash
# Vérifier les conteneurs Docker
docker ps | grep backend

# Logs du backend (dernières 100 lignes)
docker logs hotel-ticket-hub-backend --tail=100

# Logs avec erreurs (HTTP 500)
docker logs hotel-ticket-hub-backend 2>&1 | grep -i "error\|exception\|500\|actuator\|prometheus"

# Tester localement
curl -v http://localhost:8081/actuator/health
curl -v http://localhost:8081/actuator/prometheus

# Redémarrer le backend
cd /opt/backend
docker-compose restart backend

# Vérifier la configuration
cat /opt/backend/docker-compose.yml | grep -A 5 "ports"
```

---

## 🚨 Si la Clé n'Existe Pas

Si `Test-Path` retourne `False`, la clé n'existe pas. Options :

1. **Récupérer depuis GitHub Secrets** (via workflow temporaire)
2. **Utiliser AWS Systems Manager (SSM)**
3. **Créer une nouvelle clé SSH**

Voir `CONNEXION_SSH_BACKEND.md` pour les détails.

---

## 🎯 Action Immédiate

```powershell
# 1. Vérifier si la clé existe
Test-Path C:\Users\oumay\.ssh\github-actions-key

# 2. Si elle existe, se connecter
ssh -i C:\Users\oumay\.ssh\github-actions-key ubuntu@13.49.44.219

# 3. Une fois connecté, diagnostiquer l'erreur HTTP 500
docker logs hotel-ticket-hub-backend --tail=100 | grep -i "error\|exception\|500"
```

