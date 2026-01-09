# ❓ Pourquoi on ne peut pas utiliser l'ancienne IP ?

## 🚫 Problème

L'ancienne IP `13.49.44.219` **n'est plus disponible** car :
- Elle a été **libérée automatiquement** quand l'instance a changé de type (t3.micro → t3.small)
- AWS ne garde pas les IPs publiques après un changement d'instance
- Une fois libérée, l'IP retourne dans le pool AWS et n'est plus accessible

## ✅ Solution : Utiliser la Nouvelle Elastic IP

Vous avez créé une **nouvelle Elastic IP** : `13.63.15.86`

Cette IP sera **votre IP fixe** pour la VM Backend. Elle ne changera plus jamais, même après :
- Redémarrage de l'instance
- Changement de type d'instance
- Arrêt/démarrage

## 🔗 Comment Associer l'Elastic IP

### Dans le Menu Contextuel (ce que vous voyez)

1. **Cliquez sur "Associate Elastic IP address"** (pas "Release" !)
2. Dans le formulaire :
   - **Resource type** : `Instance`
   - **Instance** : Sélectionnez votre instance backend
   - **Private IP address** : Laissez par défaut
   - Cliquez sur **Associate**

### Vérification

Après association :
- L'instance backend aura l'IP `13.63.15.86` (au lieu de `13.51.56.138`)
- Cette IP sera **fixe** et ne changera plus

## 📝 Ce qu'il faut faire ensuite

1. ✅ **Associer** l'Elastic IP `13.63.15.86` à l'instance
2. ✅ **Mettre à jour** le secret GitHub `STAGING_HOST` → `13.63.15.86`
3. ✅ **Mettre à jour** Prometheus sur la VM Monitoring
4. ✅ **Tester** que tout fonctionne

## 💡 Alternative (si vous voulez vraiment l'ancienne IP)

**Option 1 : Attendre** (pas recommandé)
- L'ancienne IP pourrait être réallouée par AWS à quelqu'un d'autre
- Pas de garantie de la récupérer

**Option 2 : Utiliser la nouvelle Elastic IP** (recommandé)
- IP fixe garantie
- Ne changera jamais
- Fonctionne exactement comme l'ancienne

## ⚠️ Important

**Ne cliquez PAS sur "Release Elastic IP addresses"** dans le menu !
Cela supprimerait l'Elastic IP que vous venez de créer.

---

**Conclusion : Utilisez la nouvelle Elastic IP `13.63.15.86` comme IP fixe. Elle fonctionnera exactement comme l'ancienne !** ✅

