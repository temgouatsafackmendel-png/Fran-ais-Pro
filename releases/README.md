# Dossier des Releases APK / AAB — FRANÇAIS PRO

## Politique de distribution GitHub

Afin de maintenir un dépôt Git rapide, léger et professionnel, **les fichiers binaires `.apk` et `.aab` ne sont pas versionnés dans l'historique Git** (ils sont ignorés par `.gitignore`).

Les paquets installables officiels sont publiés dans l'onglet **GitHub Releases**, accompagnés de leur fichier d'empreintes `SHA256SUMS.txt`.

## Nomenclature officielle des livrables

| Fichier | Usage |
| :--- | :--- |
| `FrancaisPro-vX.Y.Z-release.apk` | Installation directe recommandée sur Android (signé avec clé de production) |
| `FrancaisPro-vX.Y.Z-debug.apk` | Build de démonstration et de test QA (généré automatiquement par GitHub Actions) |
| `FrancaisPro-vX.Y.Z.aab` | Android App Bundle pour publication store |
| `SHA256SUMS.txt` | Empreintes SHA-256 pour vérifier l'intégrité des téléchargements |

## Générer un APK localement

```bash
# APK de test (sortie : app/build/outputs/apk/debug/app-debug.apk)
gradle :app:assembleDebug

# Copier et calculer l'empreinte avant téléversement sur GitHub Releases
cp app/build/outputs/apk/debug/app-debug.apk releases/FrancaisPro-v1.0.0-debug.apk
(cd releases && sha256sum FrancaisPro-v1.0.0-debug.apk > SHA256SUMS.txt)
```

Pour la publication automatisée par tag Git (`v1.0.0`) et la signature Release, consultez [`docs/RELEASE_GUIDE.md`](../docs/RELEASE_GUIDE.md).
