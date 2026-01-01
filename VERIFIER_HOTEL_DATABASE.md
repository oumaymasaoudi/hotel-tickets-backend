# 🔍 Vérifier l'Hôtel dans la Base de Données

## 📋 Commandes pour Vérifier

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
-- Voir tous les hôtels
SELECT id, name, email, is_active, plan_id, created_at 
FROM hotels;

-- Voir les détails d'un hôtel spécifique
SELECT h.id, h.name, h.email, h.is_active, h.plan_id, p.name as plan_name
FROM hotels h
LEFT JOIN plans p ON h.plan_id = p.id;
```

### 5. Vérifier si l'hôtel est actif

```sql
-- Voir seulement les hôtels actifs
SELECT id, name, email, is_active 
FROM hotels 
WHERE is_active = true;
```

### 6. Si l'hôtel n'est pas actif, l'activer

```sql
-- Activer tous les hôtels
UPDATE hotels SET is_active = true WHERE is_active = false OR is_active IS NULL;

-- Vérifier
SELECT id, name, is_active FROM hotels;
```

### 7. Tester l'API depuis la VM Backend

```powershell
ssh -i github-actions-key ubuntu@13.49.44.219
curl http://localhost:8081/api/hotels/public
```

Vous devriez voir une liste JSON des hôtels.

---

## ⚠️ Problèmes Possibles

1. **L'hôtel n'est pas actif** (`is_active = false`)
   - Solution : `UPDATE hotels SET is_active = true;`

2. **L'hôtel n'a pas de plan** (`plan_id IS NULL`)
   - Solution : Assigner un plan à l'hôtel

3. **L'API ne retourne pas les hôtels actifs**
   - Vérifier que l'endpoint `/api/hotels/public` filtre par `is_active`

---

## ✅ Solution Rapide

Si l'hôtel existe mais n'est pas actif :

```sql
UPDATE hotels SET is_active = true;
```

Puis tester à nouveau le formulaire d'inscription.

