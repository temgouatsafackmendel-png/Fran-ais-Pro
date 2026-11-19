# Captures d'écran et Aperçus UI — FRANÇAIS PRO

Ce dossier regroupe les visuels officiels référencés dans le [`README.md`](../../README.md) du dépôt GitHub.

## Catalogue actuel

| Fichier | Écran | Description |
| :--- | :--- | :--- |
| `01_accueil.jpg` | Tableau de bord (Accueil) | Carte niveau CECRL bleu nuit, score global, raccourcis d'entraînement du jour, résumé de progression et navigation inférieure 5 onglets |
| `02_correcteur.jpg` | Correcteur interactif | Éditeur avec surlignage sémantique 4 couleurs (orthographe, grammaire, conjugaison, style) et `BottomSheet` d'explication de la règle |
| `03_academie.jpg` | Académie CECRL | Accès Dictionnaire / Conjugueur / Vocabulaire et parcours progressif A1 → C2 |

## Convention de nommage recommandée

Pour conserver une galerie homogène dans GitHub et les fiches de distribution :

- Format : `NN_nom_ecran.png` ou `NN_nom_ecran.jpg` (ex. `04_exercices.png`, `05_statistiques.png`, `06_mode_sombre.png`).
- Ratio : portrait `9:16` (ex. `1080 × 1920` ou `1080 × 2400`), sans bordure d'appareil superflue.
- Poids conseillé : `< 600 Ko` par image pour un chargement instantané sur GitHub.

## Capturer directement depuis un appareil ou émulateur Android

Si vous souhaitez remplacer ou compléter ces aperçus par des captures directes d'un appareil connecté via ADB :

```bash
# 1. Installer l'APK compilé
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. Capturer chaque écran affiché
adb exec-out screencap -p > docs/screenshots/01_accueil.png
adb exec-out screencap -p > docs/screenshots/02_correcteur.png
adb exec-out screencap -p > docs/screenshots/03_academie.png
adb exec-out screencap -p > docs/screenshots/04_statistiques.png
adb exec-out screencap -p > docs/screenshots/05_mode_sombre.png
```
