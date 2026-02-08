# 🔧 Guide complet : Corriger les problèmes sur les VMs

## Problèmes identifiés

1. **Erreur "No enum constant com.hotel.tickethub.model.enums.SubscriptionPlan.BASIC"**
   - Cause : Des plans avec le nom "BASIC" existent dans la base de données, mais l'enum ne contient que STARTER, PRO, ENTERPRISE
   - Solution : Exécuter le script SQL pour corriger les noms de plans

2. **Erreurs 400/500 sur les APIs**
   - Cause : Problèmes de sérialisation JSON avec les relations JPA
   - Solution : Ajout de @JsonIgnore sur les relations problématiques

3. **Hôtels sans plan**
   - Cause : Certains hôtels n'ont pas de plan assigné
   - Solution : Assigner un plan STARTER par défaut

## 📋 Étapes de correction

### Étape 1 : Corriger la base de données sur la VM Database

**Sur la VM Database (13.48.83.147) :**

```bash
# 1. Se connecter à la VM Database
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.48.83.147

# 2. Exécuter le script de correction
cd /path/to/hotel-ticket-hub-backend
chmod +x scripts/fix-vm-database-issues.sh
./scripts/fix-vm-database-issues.sh
```

**Ou exécuter manuellement les commandes SQL :**

```bash
sudo -u postgres psql -d hotel_ticket_hub <<EOF
-- Corriger les plans BASIC
UPDATE plans SET name = 'STARTER' WHERE name = 'BASIC' OR name::text = 'BASIC';

-- Créer les plans par défaut
INSERT INTO plans (id, name, base_cost, ticket_quota, excess_ticket_cost, max_technicians, sla_hours, created_at)
SELECT gen_random_uuid(), 'STARTER', 49.99, 50, 2.50, 2, 24, NOW()
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE name = 'STARTER');

INSERT INTO plans (id, name, base_cost, ticket_quota, excess_ticket_cost, max_technicians, sla_hours, created_at)
SELECT gen_random_uuid(), 'PRO', 99.99, 150, 2.00, 5, 12, NOW()
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE name = 'PRO');

INSERT INTO plans (id, name, base_cost, ticket_quota, excess_ticket_cost, max_technicians, sla_hours, created_at)
SELECT gen_random_uuid(), 'ENTERPRISE', 199.99, 500, 1.50, 15, 6, NOW()
WHERE NOT EXISTS (SELECT 1 FROM plans WHERE name = 'ENTERPRISE');

-- Assigner STARTER aux hôtels sans plan
UPDATE hotels 
SET plan_id = (SELECT id FROM plans WHERE name = 'STARTER' LIMIT 1)
WHERE plan_id IS NULL;
EOF
```

### Étape 2 : Redéployer le backend avec les corrections

**Sur la VM Backend (13.63.15.86) :**

```bash
# 1. Se connecter à la VM Backend
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86

# 2. Aller dans le répertoire du projet
cd /path/to/hotel-ticket-hub-backend

# 3. Pull les dernières modifications
git pull origin main

# 4. Rebuild et redémarrer le conteneur
docker compose down
docker compose pull
docker compose up -d --build

# 5. Vérifier les logs
docker logs -f hotel-ticket-hub-backend-staging
```

### Étape 3 : Vérifier que tout fonctionne

**Tester les endpoints :**

```bash
# Test 1 : Récupérer les hôtels publics
curl http://13.63.15.86:8081/api/hotels/public

# Test 2 : Tester le login
curl -X POST http://13.63.15.86:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"nour@gmail.com","password":"votre_mot_de_passe"}'

# Test 3 : Vérifier la santé de l'application
curl http://13.63.15.86:8081/actuator/health
```

## 🔍 Vérifications

### Vérifier les plans dans la base de données

```bash
sudo -u postgres psql -d hotel_ticket_hub -c "SELECT id, name, base_cost FROM plans ORDER BY name;"
```

**Résultat attendu :**
```
id | name       | base_cost
---+------------+-----------
   | ENTERPRISE | 199.99
   | PRO        | 99.99
   | STARTER     | 49.99
```

### Vérifier les hôtels et leurs plans

```bash
sudo -u postgres psql -d hotel_ticket_hub -c "SELECT h.name, p.name as plan_name FROM hotels h LEFT JOIN plans p ON h.plan_id = p.id;"
```

**Résultat attendu :** Tous les hôtels doivent avoir un plan (STARTER, PRO, ou ENTERPRISE)

### Vérifier les logs du backend

```bash
docker logs hotel-ticket-hub-backend-staging | grep -i "error\|exception\|basic"
```

**Ne doit pas contenir :**
- "No enum constant ... BASIC"
- "Error converting hotel to DTO"
- "IllegalArgumentException"

## 🚨 Problèmes courants et solutions

### Problème 1 : Le script SQL échoue

**Solution :** Vérifier que PostgreSQL est démarré et accessible

```bash
sudo systemctl status postgresql
sudo -u postgres psql -d hotel_ticket_hub -c "SELECT 1;"
```

### Problème 2 : Le backend ne démarre toujours pas

**Solution :** Vérifier les variables d'environnement

```bash
docker exec hotel-ticket-hub-backend-staging env | grep SPRING_DATASOURCE
```

### Problème 3 : Les APIs retournent toujours des erreurs 500

**Solution :** Vérifier que le DataInitializer s'exécute correctement

```bash
docker logs hotel-ticket-hub-backend-staging | grep -i "DataInitializer\|Initializing default data"
```

## ✅ Checklist de vérification

- [ ] Script SQL exécuté sur la VM Database
- [ ] Plans STARTER, PRO, ENTERPRISE existent dans la base de données
- [ ] Tous les hôtels ont un plan assigné
- [ ] Backend redéployé avec les dernières modifications
- [ ] DataInitializer s'exécute au démarrage
- [ ] `/api/hotels/public` retourne une liste (même vide)
- [ ] `/api/auth/login` fonctionne sans erreur "BASIC"
- [ ] Les logs ne contiennent plus d'erreurs liées aux plans

## 📞 Support

Si les problèmes persistent après avoir suivi ce guide :

1. Vérifier les logs complets : `docker logs hotel-ticket-hub-backend-staging`
2. Vérifier la connexion à la base de données : `docker exec hotel-ticket-hub-backend-staging env | grep DATASOURCE`
3. Vérifier que le DataInitializer s'exécute : `docker logs hotel-ticket-hub-backend-staging | grep DataInitializer`
