# ⚠️ Prévention : Suspension de compte AWS

## Problème rencontré
Le compte AWS a été suspendu à cause d'un problème de paiement, ce qui a causé :
- ❌ Connexion SSH impossible
- ❌ Accès à la console AWS bloqué
- ❌ Déploiements GitHub Actions échoués

## Solutions préventives

### 1. Configuration des alertes de facturation AWS
Configurez des alertes pour être notifié avant la suspension :

1. **AWS Console** → **Billing** (Facturation)
2. **Preferences** (Préférences) → **Receive PDF Invoice By Email** (Recevoir la facture PDF par email)
3. **Billing Preferences** → Activez les notifications par email
4. **Budgets** → Créez un budget avec alertes :
   - Alerte à 50% du budget
   - Alerte à 80% du budget
   - Alerte à 100% du budget

### 2. Configuration de la méthode de paiement
1. **AWS Console** → **Billing** → **Payment methods** (Méthodes de paiement)
2. Ajoutez une carte de crédit valide
3. Configurez un paiement automatique si possible

### 3. Monitoring des coûts
- Vérifiez régulièrement les coûts dans **Cost Explorer**
- Configurez des budgets mensuels avec alertes
- Surveillez les ressources inutilisées (instances arrêtées, volumes non attachés, etc.)

### 4. Documentation des ressources critiques
Gardez une liste des ressources critiques :
- Instance EC2 de staging : `13.63.15.86`
- Security Groups utilisés
- Elastic IPs alloués

## En cas de suspension

### Actions immédiates
1. **Résoudre le problème de paiement** :
   - AWS Console → Billing → Payments
   - Règler le solde impayé
   - Le compte sera réactivé automatiquement

2. **Vérifier l'état des ressources** :
   - Vérifier que les instances EC2 sont toujours en cours d'exécution
   - Vérifier que les IPs publiques n'ont pas changé
   - Vérifier les Security Groups

3. **Tester les connexions** :
   ```powershell
   ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86
   ```

### Après réactivation
1. Vérifier que tous les services fonctionnent
2. Tester les déploiements GitHub Actions
3. Vérifier les endpoints (health, prometheus, etc.)

## Bonnes pratiques

✅ **Activer les alertes de facturation**
✅ **Configurer un budget avec alertes**
✅ **Vérifier régulièrement les coûts**
✅ **Documenter les ressources critiques**
✅ **Avoir une méthode de paiement de secours**

❌ **Ne pas ignorer les emails de facturation AWS**
❌ **Ne pas laisser le compte sans méthode de paiement valide**

