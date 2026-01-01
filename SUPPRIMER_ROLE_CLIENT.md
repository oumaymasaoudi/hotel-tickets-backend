# 🔧 Supprimer le Rôle CLIENT en Double

## ⚠️ Problème Identifié

L'utilisateur a **2 rôles** :
- CLIENT
- SUPERADMIN

C'est pour ça que la requête retourne "2 results were returned".

## ✅ Solution : Supprimer le Rôle CLIENT

### Dans PostgreSQL (toujours connecté)

```sql
-- Supprimer le rôle CLIENT
DELETE FROM user_roles 
WHERE id = '73f88f25-be59-4c53-84ea-e331e425332f';

-- Vérifier qu'il ne reste que SUPERADMIN
SELECT ur.role, p.email, p.full_name 
FROM user_roles ur 
JOIN profiles p ON ur.user_id = p.id 
WHERE p.email = 'oumaymasaoudi6@gmail.com';
```

Vous devriez voir seulement **1 row** avec le rôle `SUPERADMIN`.

### Quitter PostgreSQL

```sql
\q
```

### Redémarrer le Backend

```powershell
ssh -i github-actions-key ubuntu@13.49.44.219
cd /opt/hotel-ticket-hub-backend-staging
docker compose restart
```

### Tester la Connexion

- http://51.21.196.104/login
- Email : `oumaymasaoudi6@gmail.com`
- Mot de passe : `admin123`

---

## ✅ Résultat Attendu

Après suppression du rôle CLIENT, il ne doit rester qu'**un seul rôle** : `SUPERADMIN`.

