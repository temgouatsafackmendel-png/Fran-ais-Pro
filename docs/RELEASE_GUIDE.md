# Guide de publication GitHub, Captures & APK / AAB

Ce guide explique comment envoyer **FRANÇAIS PRO** sur GitHub proprement, automatiser les builds via GitHub Actions et distribuer des fichiers **APK / AAB** signés et vérifiables.

---

## 1. Préparer et envoyer le projet sur GitHub

Depuis la racine du projet :

```bash
# Initialiser le dépôt Git (si ce n'est pas déjà fait)
git init
git branch -M main

# Vérifier qu'aucun fichier sensible (.env, *.keystore, *.jks, build/) n'est suivi
git status

# Premier commit propre
git add .
git commit -m "feat: version initiale complète de FRANÇAIS PRO v1.0.0"

# Associer le dépôt distant GitHub puis pousser
git remote add origin https://github.com/<VOTRE_UTILISATEUR>/francais-pro.git
git push -u origin main
```

---

## 2. Publier une Release GitHub automatiquement (avec APK joint)

Le modèle de workflow prêt à l'emploi se trouve dans [`docs/ci/android-ci-release.yml`](ci/android-ci-release.yml) (placé dans `docs/ci/` afin que le premier push fonctionne même sans la permission GitHub `workflows`).

Pour activer GitHub Actions après votre premier push :
- Sur GitHub : cliquez sur **Add file → Create new file**, nommez-le `.github/workflows/android-ci-release.yml` et collez le contenu de [`docs/ci/android-ci-release.yml`](ci/android-ci-release.yml).

Une fois activé, le workflow exécute automatiquement :
1. Les tests unitaires et Robolectric (`gradle :app:testDebugUnitTest`).
2. L'analyse statique Android Lint (`gradle :app:lintDebug`).
3. La génération de l'APK de test (`FrancaisPro-<tag>-debug.apk`).
4. Si les secrets de signature sont configurés dans GitHub, la génération de l'APK signé (`FrancaisPro-<tag>-release.apk`) et du bundle (`FrancaisPro-<tag>.aab`).
5. Le calcul du fichier d'empreintes `SHA256SUMS.txt` et la création automatique de la **GitHub Release**.

### Déclencher la publication `v1.0.0`

```bash
git tag -a v1.0.0 -m "FRANÇAIS PRO v1.0.0 — Première version stable hors ligne"
git push origin v1.0.0
```

---

## 3. Configurer la signature Release (APK & AAB signés)

### A. Créer une clé de signature locale (une seule fois)

> ⚠️ Ne committez **jamais** votre fichier `.jks` ni vos mots de passe dans Git (ils sont déjà exclus par `.gitignore`). Conservez une copie sécurisée hors ligne.

```bash
keytool -genkeypair -v \
  -keystore my-upload-key.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### B. Compiler localement l'APK et l'AAB de production

```bash
export KEYSTORE_PATH="$(pwd)/my-upload-key.jks"
export STORE_PASSWORD="<VOTRE_MOT_DE_PASSE_KEYSTORE>"
export KEY_PASSWORD="<VOTRE_MOT_DE_PASSE_CLE>"

# APK prêt à installer
gradle :app:assembleRelease

# Android App Bundle (.aab) pour Google Play
gradle :app:bundleRelease
```

Fichiers produits :
- **APK Debug** : `app/build/outputs/apk/debug/app-debug.apk`
- **APK Release** : `app/build/outputs/apk/release/app-release.apk`
- **AAB Release** : `app/build/outputs/bundle/release/app-release.aab`

### C. Activer la signature automatique dans GitHub Actions

Dans votre dépôt GitHub → **Settings → Secrets and variables → Actions → New repository secret**, ajoutez :

| Secret GitHub | Valeur |
| :--- | :--- |
| `ANDROID_KEYSTORE_BASE64` | Sortie de `base64 -w 0 my-upload-key.jks` (Linux) ou `base64 -i my-upload-key.jks \| tr -d '\n'` (macOS) |
| `STORE_PASSWORD` | Mot de passe du keystore |
| `KEY_PASSWORD` | Mot de passe de la clé `upload` |

---

## 4. Vérification SHA-256 avant publication manuelle

Si vous joignez manuellement un APK à une Release GitHub, générez toujours son empreinte de contrôle :

```bash
cp app/build/outputs/apk/debug/app-debug.apk releases/FrancaisPro-v1.0.0-debug.apk
cd releases
sha256sum FrancaisPro-v1.0.0-debug.apk > SHA256SUMS.txt
```

Téléversez ensuite les fichiers `.apk` et `SHA256SUMS.txt` dans l'interface **GitHub → Releases → Draft a new release** (sans committer les binaires `.apk` dans Git).

---

## 5. Checklist avant chaque Release

- [ ] `versionCode` et `versionName` mis à jour dans `app/build.gradle.kts`
- [ ] `CHANGELOG.md` mis à jour avec la nouvelle version
- [ ] `gradle :app:testDebugUnitTest` → 100 % réussi
- [ ] `gradle :app:lintDebug` → 0 erreur
- [ ] `gradle :app:assembleDebug` (et `assembleRelease`) → succès
- [ ] Test d'installation sur appareil physique ou émulateur en **mode avion**
- [ ] Empreinte `SHA256SUMS.txt` jointe à la Release GitHub
