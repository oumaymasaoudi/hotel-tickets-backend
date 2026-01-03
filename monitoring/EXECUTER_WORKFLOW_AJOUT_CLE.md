# 🚀 Exécuter le Workflow pour Ajouter la Clé

## ✅ Vérifier que le Workflow est Disponible

1. **GitHub** > votre repo > **Actions**
2. **Dans la liste des workflows à gauche**, cherchez **"Add Oumayma Key to Backend"**
3. **Cliquez dessus**

---

## ✅ Si le Workflow Apparaît

### Exécuter le Workflow

1. **Cliquez sur** le bouton bleu **"Run workflow"** (en haut à droite)
2. **Sélectionnez** la branche `develop` ou `main`
3. **Dans le champ `public_key`**, collez votre clé publique complète :
   ```
   ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDMEvsx5uQc0EpN5iKdRpdQrRifnkNvFOukMkDWHHSPVsSsf+Lv2SUk9Q7+WKcNoK2gRglOBIT0Kl61GrR73d/HzOWAPZlaWyEYwLcahEkba/0TbEHeskKGF8OODyc1YtNnuNCvCCiyifmDQk13mLW8tIkLhhxQQwOce5QjhxPk+DYRWaNOmEEo4clSF237BjF4hdefn0ZdNgQTK7dK7JeiE8A9lVYh/LAXg8hUYA0xy7ahqOpE9wdo3PsI0bkKOhwoXl9i6ANtjANpJaWSvBzFk6QucKSTTqTxJU0U6k3dBjrbkeOqEbl1JIupKJCRLedX3rVjmCI48JHvS0P/YYix
   ```
4. **Cliquez sur** le bouton vert **"Run workflow"**

### Attendre la Fin

- Le workflow prend **30-60 secondes**
- Vérifiez que l'étape **"Add Oumayma Public Key"** affiche **"✅ Clé ajoutée avec succès"**

---

## ✅ Si le Workflow N'Apparaît PAS

### Option 1 : Attendre quelques minutes

Parfois GitHub met quelques minutes à indexer les nouveaux workflows. Attendez 2-3 minutes et rafraîchissez la page.

### Option 2 : Vérifier le Fichier

Vérifiez que le fichier existe bien :
```powershell
Test-Path hotel-ticket-hub-backend\.github\workflows\add-oumayma-key.yml
```

Si le fichier n'existe pas, créez-le ou vérifiez le chemin.

### Option 3 : Vérifier dans "All workflows"

1. **GitHub** > **Actions**
2. **Cliquez sur** "All workflows" (en haut)
3. **Cherchez** "Add Oumayma Key to Backend" dans la liste

---

## ✅ Après l'Exécution du Workflow

### 1. Attendre 1-2 minutes

Laissez le temps à la VM de mettre à jour les clés autorisées.

### 2. Tester la Connexion

```powershell
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219
```

**Si ça fonctionne :** Vous verrez :
```
Welcome to Ubuntu...
```

### 3. Diagnostiquer l'Erreur HTTP 500

Une fois connecté :

```bash
# Logs avec erreurs
docker logs hotel-ticket-hub-backend --tail=100 | grep -i "error\|exception\|500\|actuator"

# Test Prometheus endpoint
curl -v http://localhost:8081/actuator/prometheus
```

---

## 📋 Checklist

- [ ] Workflow "Add Oumayma Key to Backend" visible dans GitHub Actions
- [ ] Workflow exécuté avec succès
- [ ] Clé publique ajoutée (message "✅ Clé ajoutée avec succès")
- [ ] Connexion SSH testée : `ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219`
- [ ] Logs du backend analysés pour l'erreur HTTP 500

---

**Allez dans GitHub Actions et cherchez "Add Oumayma Key to Backend" dans la liste des workflows !**

