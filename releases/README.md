# Releases & APK prêt à télécharger — FRANÇAIS PRO

*Projet réalisé de bout en bout par **Temgoua Mendel**.*

## Téléchargement direct

| Fichier | Description |
| :--- | :--- |
| [`FrancaisPro-v1.0.0.apk`](FrancaisPro-v1.0.0.apk) | APK Android prêt à installer (Android 7.0+ / API 24–36, 100 % hors ligne) |
| [`SHA256SUMS.txt`](SHA256SUMS.txt) | Empreinte SHA-256 pour vérifier l'intégrité du fichier APK |

## Vérification de l'empreinte SHA-256

```bash
cd releases
sha256sum -c SHA256SUMS.txt
```

## Recompiler l'APK depuis les sources

```bash
gradle :app:assembleDebug
cp app/build/outputs/apk/debug/app-debug.apk releases/FrancaisPro-v1.0.0.apk
(cd releases && sha256sum FrancaisPro-v1.0.0.apk > SHA256SUMS.txt)
```

Pour générer un APK/AAB signé de production ou publier via GitHub Releases, consultez [`docs/RELEASE_GUIDE.md`](../docs/RELEASE_GUIDE.md).
