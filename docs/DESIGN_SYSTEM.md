# FRANÇAIS PRO — Design System & Guide UX

Ce document formalise le système de design centralisé de **FRANÇAIS PRO** (*« Écrivez mieux. Parlez juste. Progressez chaque jour. »*) en Jetpack Compose et Material Design 3.

---

## 1. Palette Sémantique Centralisée

Toutes les couleurs sont déclarées dans `ui/theme/Color.kt` et consommées via `MaterialTheme.colorScheme` et `LocalSemanticColors.current`. Aucun code hexadécimal n'est dispersé dans les écrans.

| Rôle | Mode Clair | Mode Sombre | Usage |
| :--- | :--- | :--- | :--- |
| Primary Navy | `#101B36` | `#101B36` | Carte principale, avatars d'initiales |
| Primary Blue | `#356AE6` | `#7098FF` | Actions principales, onglet actif, liens |
| Secondary Blue | `#6B8FF0` | `#93B4FF` | Accents secondaires |
| Background | `#F4F6FA` | `#0B1220` | Fond général d'écran |
| Surface | `#FFFFFF` | `#131E31` | Cartes, éditeur, barre de recherche |
| Elevated Surface | `#FFFFFF` | `#1B2940` | BottomSheets, dialogues |
| Text Primary | `#172033` | `#F5F7FC` | Titres et corps de lecture |
| Text Secondary | `#788397` | `#A6B2C5` | Sous-titres et métadonnées |
| Border | `#E6EAF0` | `#24344D` | Contours fins `1 dp` |
| Success | `#16A36A` | `#34D399` | Validations, objectifs atteints |
| Warning | `#F59E0B` | `#FBBF24` | Grammaire, conjugaison, points à renforcer |
| Error | `#DC4545` | `#F87171` | Orthographe, erreurs, confirmations destructives |
| Syntax Violet | `#7C3AED` | `#A78BFA` | Syntaxe et ponctuation (Correcteur) |
| Style Blue | `#356AE6` | `#7098FF` | Améliorations de style (Correcteur) |

---

## 2. Typographie (9 Rôles)

Polices embarquées localement (`res/font/`) : **Plus Jakarta Sans** (interface et lecture), **Fraunces** (accents *Display*), **JetBrains Mono** (compteurs, scores, phonétique).

- `Display` : 32 sp / 38 sp
- `Heading 1` : 26 sp / 32 sp
- `Heading 2` : 21 sp / 27 sp
- `Heading 3` : 17 sp / 23 sp
- `Body Large` : 16 sp / 24 sp
- `Body Medium` : 14 sp / 21 sp
- `Body Small` : 13 sp / 19 sp
- `Caption` : 12 sp / 16 sp
- `Label` : 12 sp semi-gras

---

## 3. Grille d'Espacement & Rayons

- **Grille** : multiples stricts de `4 dp` (`4, 8, 12, 16, 20, 24, 32 dp`).
- **Marges adaptatives** : `16 dp` sur écrans compacts (`< 412 dp`), `20 dp` sur écrans plus larges (`>= 412 dp`), largeur de contenu plafonnée à `720 dp` sur tablettes.
- **Rayons** : `10 dp` (petits composants/badges), `14 dp` (boutons/champs), `18 dp` (cartes), `24 dp` (grandes surfaces et BottomSheets).

---

## 4. Bibliothèque des 19 Composants Réutilisables

1. `PrimaryButton`
2. `SecondaryButton`
3. `AppIconButton`
4. `AppTopBar`
5. `AppBottomNavigation` (+ `AppNavigationRail` sur écrans larges)
6. `AppSearchBar`
7. `ProgressCard`
8. `LevelCard`
9. `LessonCard`
10. `ExerciseCard`
11. `CorrectionCard`
12. `ErrorHighlightText`
13. `StatisticsCard`
14. `EmptyState`
15. `LoadingState`
16. `ErrorState`
17. `SuccessFeedback`
18. `ErrorDetailBottomSheet`
19. `ConfirmationDialog`

---

## 5. Micro-interactions, Accessibilité & Sobriété Hors Ligne

- **Transitions** : `180–240 ms`, automatiquement réduites à `0 ms` lorsque `Settings.Global.ANIMATOR_DURATION_SCALE == 0f` (`LocalReducedMotion`).
- **Accessibilité** : cibles tactiles minimales `48 dp`, descriptions TalkBack sur toutes les icônes interactives, double encodage des erreurs (couleur + soulignement + libellé de catégorie).
- **Autonomie hors ligne** : aucune bannière permanente dans l'interface quotidienne ; indicateur discret `DiscreetOfflineBadge` uniquement dans l'Onboarding et les Paramètres.
