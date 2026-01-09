# 🚀 Pousser le Workflow et Se Connecter avec Oumayma Key

## ✅ Étape 1 : Ajouter et Pousser le Workflow

```powershell
cd hotel-ticket-hub-backend

# Ajouter le workflow
git add .github/workflows/add-oumayma-key.yml

# Commiter
git commit -m "feat: add workflow to add oumayma key to backend"

# Pousser
git push origin develop
```

---

## ✅ Étape 2 : Attendre 1-2 minutes

GitHub met quelques minutes à indexer les nouveaux workflows. Attendez 1-2 minutes.

---

## ✅ Étape 3 : Exécuter le Workflow dans GitHub Actions

1. **GitHub** > votre repo > **Actions**
2. **Cherchez** "Add Oumayma Key to Backend" dans la liste des workflows (à gauche)
3. **Cliquez dessus**
4. **Cliquez sur** "Run workflow" (en haut à droite)
5. **Sélectionnez** la branche `develop`
6. **Dans le champ `public_key`**, collez votre clé publique complète :
   ```
   ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDMEvsx5uQc0EpN5iKdRpdQrRifnkNvFOukMkDWHHSPVsSsf+Lv2SUk9Q7+WKcNoK2gRglOBIT0Kl61GrR73d/HzOWAPZlaWyEYwLcahEkba/0TbEHeskKGF8OODyc1YtNnuNCvCCiyifmDQk13mLW8tIkLhhxQQwOce5QjhxPk+DYRWaNOmEEo4clSF237BjF4hdefn0ZdNgQTK7dK7JeiE8A9lVYh/LAXg8hUYA0xy7ahqOpE9wdo3PsI0bkKOhwoXl9i6ANtjANpJaWSvBzFk6QucKSTTqTxJU0U6k3dBjrbkeOqEbl1JIupKJCRLedX3rVjmCI48JHvS0P/YYix
   ```
7. **Cliquez sur** "Run workflow"

---

## ✅ Étape 4 : Attendre la Fin du Workflow

- Le workflow prend **30-60 secondes**
- Vérifiez que l'étape **"Add Oumayma Public Key"** affiche **"✅ Clé ajoutée avec succès"**

---

## ✅ Étape 5 : Tester la Connexion

Attendez **1-2 minutes** après la fin du workflow, puis :

```powershell
ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219
```

**Si ça fonctionne :** Vous verrez :
```
Welcome to Ubuntu...
```

---

## ✅ Étape 6 : Diagnostiquer l'Erreur HTTP 500

Une fois connecté :

```bash
# Logs avec erreurs
docker logs hotel-ticket-hub-backend --tail=100 | grep -i "error\|exception\|500\|actuator\|prometheus"

# Test Prometheus endpoint
curl -v http://localhost:8081/actuator/prometheus
```

---

## 📋 Checklist

- [ ] Workflow ajouté et poussé : `git add .github/workflows/add-oumayma-key.yml && git commit -m "feat: add workflow" && git push`
- [ ] Attendu 1-2 minutes pour l'indexation GitHub
- [ ] Workflow "Add Oumayma Key to Backend" visible dans GitHub Actions
- [ ] Workflow exécuté avec succès
- [ ] Clé publique ajoutée (message "✅ Clé ajoutée avec succès")
- [ ] Connexion SSH testée : `ssh -i C:\Users\oumay\.ssh\oumayma-key.pem ubuntu@13.49.44.219`
- [ ] Logs du backend analysés pour l'erreur HTTP 500

---

**Commencez par pousser le workflow, puis exécutez-le depuis GitHub Actions !**

