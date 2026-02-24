# 📋 Checklist Release - develop → main

**Date:** 24 Février 2026  
**Statut:** PR #19 existe mais a des conflits à résoudre

---

## ✅ 1. PR develop → main

### Statut actuel
- **PR #19:** Existe mais a des **conflits** à résoudre
- **URL:** https://github.com/oumaymasaoudi/hotel-tickets-backend/pull/19
- **Titre:** "Merge pull request #10 from oumaymasaoudi/main"

### Actions requises
1. **Résoudre les conflits** dans la PR #19
2. **Merger la PR** vers `main`
3. Le pipeline se déclenchera automatiquement

---

## ✅ 2. Vérification du Pipeline

### Conditions pour la Release
Le job `release` s'exécute uniquement si :
- ✅ `github.ref == 'refs/heads/main'`
- ✅ `github.event_name == 'push'`
- ✅ Les jobs précédents ont réussi : `lint`, `test`, `coverage`, `build`
- ✅ `package.json` existe (pour semantic-release)

### Séquence après merge vers `main`
1. **Tests & Quality** (bloquants)
   - ✅ Lint & Code Quality
   - ✅ Unit Tests
   - ✅ Code Coverage (≥ 80% requis)
   - ✅ OWASP Dependency Check
   - ✅ Security Linting (Trivy)
   - ✅ SonarCloud Analysis (Quality Gate doit passer)

2. **Build & Docker**
   - ✅ Build Maven
   - ✅ Docker Build & Push

3. **Release** (automatique)
   - ✅ Semantic-release analyse les commits
   - ✅ Crée la version (ex: `v1.13.8`)
   - ✅ Met à jour `CHANGELOG.md`
   - ✅ Met à jour `pom.xml` avec la version
   - ✅ Crée un tag Git `v1.13.8`
   - ✅ Crée un commit `chore(release): 1.13.8 [skip ci]`

4. **Docker Tag Release**
   - ✅ Tag l'image Docker avec `v1.13.8`
   - ✅ Tag l'image Docker avec `latest`

---

## ✅ 3. Vérification SonarCloud

### URLs SonarCloud
- **Branche develop:** https://sonarcloud.io/project/overview?id=oumaymasaoudi_hotel-tickets-backend&branch=develop
- **Branche main:** https://sonarcloud.io/project/overview?id=oumaymasaoudi_hotel-tickets-backend&branch=main

### Vérifications à faire
- [ ] L'analyse SonarCloud est visible sur `develop`
- [ ] Le Quality Gate est **PASS** sur `develop`
- [ ] La couverture de code est ≥ 80%
- [ ] Aucun bug critique ou blocker

---

## ✅ 4. Tests sur Staging

### URLs Staging
- **Backend API:** http://13.63.15.86:8081/api
- **Swagger:** http://13.63.15.86:8081/swagger-ui.html
- **Health Check:** http://13.63.15.86:8081/actuator/health

### Tests à effectuer

#### 1. Health Check
```bash
curl http://13.63.15.86:8081/actuator/health
```
**Résultat attendu:** `{"status":"UP"}`

#### 2. API Endpoints
```bash
# Test d'authentification
curl -X POST http://13.63.15.86:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'

# Test de récupération des tickets
curl http://13.63.15.86:8081/api/tickets \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 3. Swagger UI
- Ouvrir http://13.63.15.86:8081/swagger-ui.html
- Vérifier que tous les endpoints sont accessibles
- Tester quelques endpoints via l'interface Swagger

#### 4. Logs
```bash
# SSH vers le serveur staging
ssh -i ~/.ssh/oumayma-key.pem ubuntu@13.63.15.86

# Vérifier les logs Docker
docker ps
docker logs <container_id>
```

#### 5. Monitoring
- **Grafana:** http://16.170.74.58:3000 (admin/admin)
- **Prometheus:** http://16.170.74.58:9090
- Vérifier que les métriques sont collectées
- Vérifier que les dashboards affichent les données

---

## ✅ 5. Checklist avant Release

### Avant de merger la PR develop → main

- [ ] **Tests locaux passent**
  ```bash
  mvn clean verify
  ```

- [ ] **SonarCloud Quality Gate = PASS**
  - Vérifier sur https://sonarcloud.io
  - Couverture ≥ 80%
  - Aucun bug critique

- [ ] **Staging fonctionne correctement**
  - Health check OK
  - API endpoints fonctionnels
  - Pas d'erreurs dans les logs

- [ ] **Conflits résolus**
  - PR #19 sans conflits
  - Code review effectuée

- [ ] **Documentation à jour**
  - CHANGELOG.md (sera mis à jour automatiquement)
  - README.md si nécessaire

---

## ✅ 6. Après la Release

### Vérifications post-release

1. **Tag Git créé**
   ```bash
   git fetch --tags
   git tag --list | grep v1.13
   ```

2. **Image Docker taguée**
   - Vérifier sur https://github.com/oumaymasaoudi/hotel-tickets-backend/pkgs/container/backend
   - Tags `v1.13.8` et `latest` doivent exister

3. **CHANGELOG.md mis à jour**
   - Vérifier que la nouvelle version est dans CHANGELOG.md
   - Vérifier que les notes de release sont correctes

4. **pom.xml mis à jour**
   - Vérifier que la version dans pom.xml correspond à la release

5. **SonarCloud sur main**
   - Vérifier que l'analyse est visible sur la branche `main`
   - Quality Gate doit être PASS

---

## 🚨 Problèmes connus

### PR #19 a des conflits
**Solution:** Résoudre les conflits manuellement sur GitHub ou localement :
```bash
git checkout main
git pull origin main
git checkout develop
git merge main
# Résoudre les conflits
git push origin develop
```

---

## 📚 Ressources

- **Pipeline:** `.github/workflows/ci.yml`
- **Semantic Release:** `.releaserc.json`
- **Documentation Git Flow:** `WORKFLOW.md`
- **URLs & IPs:** `LIENS-ET-IPS.md`

---

**Dernière mise à jour:** 24 Février 2026
