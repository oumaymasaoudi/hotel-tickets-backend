# 🔧 Guide : Résoudre le conflit SonarCloud (Analyse Automatique vs CI)

## ❌ Problème

Erreur dans le pipeline :
```
ERROR: You are running CI analysis while Automatic Analysis is enabled. 
Please consider disabling one or the other.
```

## ✅ Solution : Désactiver l'Analyse Automatique dans SonarCloud

L'analyse automatique est activée dans SonarCloud et entre en conflit avec l'analyse CI. Il faut la désactiver dans l'interface SonarCloud.

### Étapes à suivre :

1. **Aller sur SonarCloud**
   - Ouvrir : https://sonarcloud.io
   - Se connecter avec votre compte GitHub

2. **Sélectionner le projet**
   - Cliquer sur l'organisation : `oumaymasaoudi`
   - Ouvrir le projet : `hotel-tickets-backend`

3. **Accéder aux paramètres d'analyse**
   - Cliquer sur **"Administration"** (en haut à droite)
   - Dans le menu de gauche, cliquer sur **"Analysis Method"** (ou "Méthode d'analyse")

4. **Désactiver l'Analyse Automatique**
   - Trouver la section **"Automatic Analysis"**
   - **Désactiver** le toggle "Automatic Analysis"
   - **Activer** le toggle "CI/CD Analysis" (si disponible)

5. **Sauvegarder**
   - Cliquer sur **"Save"** ou **"Enregistrer"**

### Alternative 1 : Via le script PowerShell

Un script PowerShell est disponible pour automatiser la désactivation :

```powershell
# Option 1: Avec le token en paramètre
.\scripts\disable-sonarcloud-auto-analysis.ps1 -SonarToken 'votre-token-sonarcloud'

# Option 2: Avec la variable d'environnement
$env:SONAR_TOKEN = 'votre-token-sonarcloud'
.\scripts\disable-sonarcloud-auto-analysis.ps1
```

**Récupérer le token SonarCloud :**
- GitHub Repository > Settings > Secrets and variables > Actions > `SONAR_TOKEN`
- Ou créer un nouveau token sur https://sonarcloud.io > My Account > Security

### Alternative 2 : Via l'API SonarCloud (curl)

Si vous préférez utiliser curl directement :

```bash
# Récupérer votre token SonarCloud depuis les secrets GitHub
# Puis appeler l'API pour désactiver l'analyse automatique
curl -u YOUR_SONAR_TOKEN: \
  -X POST \
  'https://sonarcloud.io/api/analysis_methods/disable_automatic_analysis?project=oumaymasaoudi_hotel-tickets-backend'
```

## 📋 Vérification

Après avoir désactivé l'analyse automatique :

1. **Relancer le pipeline**
   - Faire un commit/push ou relancer manuellement le workflow

2. **Vérifier les logs**
   - Le job "Backend - SonarCloud Analysis" devrait passer sans erreur
   - Plus d'erreur "Automatic Analysis is enabled"

## 🔍 Pourquoi ce problème ?

SonarCloud propose deux modes d'analyse :
- **Automatic Analysis** : Analyse automatique déclenchée par SonarCloud
- **CI/CD Analysis** : Analyse déclenchée par votre pipeline CI/CD

Les deux modes ne peuvent pas être actifs simultanément. Comme vous utilisez GitHub Actions pour l'analyse CI/CD, il faut désactiver l'analyse automatique.

## 📝 Note

Le paramètre `sonar.ci.skip=false` dans `sonar-project.properties` indique seulement que l'analyse CI ne doit pas être ignorée. Il ne désactive pas l'analyse automatique dans SonarCloud.

---

**Une fois l'analyse automatique désactivée, le pipeline devrait fonctionner correctement !** ✅
