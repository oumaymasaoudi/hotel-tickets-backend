# 🔍 Commandes pour Vérifier l'Hôtel

## 📋 Étapes Rapides

### 1. Se connecter à la VM Database

```powershell
ssh -i github-actions-key ubuntu@13.61.27.43
```

### 2. Se connecter à PostgreSQL

```bash
sudo -u postgres psql
```

### 3. Se connecter à la base de données

```sql
\c hotel_ticket_hub
```

### 4. Vérifier les Hôtels

```sql
-- Voir tous les hôtels avec leur statut
SELECT id, name, email, is_active, plan_id, created_at 
FROM hotels;
```

### 5. Si l'hôtel existe mais n'est pas actif

```sql
-- Activer tous les hôtels
UPDATE hotels SET is_active = true WHERE is_active = false OR is_active IS NULL;

-- Vérifier
SELECT id, name, is_active FROM hotels;
```

### 6. Tester l'API

```powershell
# Depuis la VM backend
ssh -i github-actions-key ubuntu@13.49.44.219
curl http://localhost:8081/api/hotels/public
```

Vous devriez voir une liste JSON des hôtels.

### 7. Quitter PostgreSQL

```sql
\q
```

---

## 🔄 Redémarrer le Backend (si nécessaire)

```powershell
ssh -i github-actions-key ubuntu@13.49.44.219
cd /opt/hotel-ticket-hub-backend-staging
docker compose restart
```

---

## ✅ Tester le Formulaire

Après vérification, testez :
- http://51.21.196.104/signup
- Sélectionnez "Admin Hôtel"
- L'hôtel devrait apparaître dans la liste

