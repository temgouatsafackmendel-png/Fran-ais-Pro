# Contribuer à FRANÇAIS PRO

Merci de votre intérêt pour **FRANÇAIS PRO** ! Ce guide précise les principes fondamentaux, les standards techniques et le déroulement d'une contribution propre.

## 1. Règles non négociables du projet

Avant toute proposition de fonctionnalité ou Pull Request, vérifiez qu'elle respecte strictement ces engagements :

1. **100 % hors ligne** : aucune fonctionnalité ne doit dépendre d'un serveur, d'une API cloud ou de la permission `INTERNET`.
2. **Zéro collecte / Zéro compte** : aucune télémétrie, aucun SDK d'analyse, aucune publicité et aucune inscription obligatoire.
3. **Qualité linguistique française** : toutes les chaînes visibles, règles grammaticales, leçons et messages d'interface doivent être rédigés dans un français irréprochable.

## 2. Standards techniques

- **Architecture** : Kotlin + Jetpack Compose + Material 3 + MVVM + Clean Architecture. Ne placez pas de logique métier lourde dans les composables.
- **Design System** : utilisez exclusivement les tokens centralisés dans `core/design/` (`FrancaisProTheme`, couleurs sémantiques, grille 4 dp, rayons `10 / 14 / 18 / 24 dp`). Ne codez jamais de couleurs hexadécimales en dur dans les écrans.
- **Accessibilité** : toute action interactive doit offrir une zone tactile minimale de `48.dp`, un `contentDescription` pertinent et un `Modifier.testTag(...)` en `snake_case`.
- **Chaînes de caractères** : placez les libellés réutilisables dans `app/src/main/res/values/strings.xml`.
- **Ressources & Taille d'APK** : évitez d'ajouter des dépendances ou des images lourdes inutilisées dans `app/src/main/res/`.

## 3. Flux de contribution

1. **Forkez** le dépôt et créez une branche descriptive depuis `main` :
   ```bash
   git checkout -b feat/nouvelle-regle-accord
   ```
   *(Préfixes recommandés : `feat/`, `fix/`, `linguistics/`, `docs/`, `test/`, `refactor/`.)*
2. **Développez** votre modification en ajoutant des tests correspondants (ex. dans `LinguisticsEngineTest.kt` ou `FrancaisProUiRobolectricTest.kt`).
3. **Vérifiez localement** que la suite complète passe :
   ```bash
   gradle :app:testDebugUnitTest
   gradle :app:lintDebug
   gradle :app:assembleDebug
   ```
4. **Ouvrez une Pull Request** en remplissant le modèle fourni et en expliquant le cas d'usage ou la règle linguistique concernée.

## 4. Idées de contributions particulièrement utiles

- Enrichissement du lexique hors ligne, des homophones et des règles de syntaxe/style.
- Nouvelles leçons CECRL (A1 à C2), dictées et séries d'exercices thématiques.
- Tests unitaires couvrant des cas limites de conjugaison ou d'accords du participe passé.
- Améliorations d'accessibilité (TalkBack, contraste, grands écrans et tablettes).
