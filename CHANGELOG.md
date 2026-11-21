# Journal des modifications (Changelog)

Toutes les évolutions notables de **FRANÇAIS PRO** sont documentées dans ce fichier selon le format [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) et la gestion sémantique de version ([SemVer](https://semver.org/lang/fr/)).

## [1.3.0] — Design System Premium complet, Aperçu 30 jours & Ergonomie modernisée

### Ajouté
- **Système de tokens Design System complet** (`Color.kt`, `Type.kt`, `Theme.kt`) :
  - Tokens chromatiques structurés Clair & Sombre (`FrancaisProPalette`, `SemanticColors`) incluant `syntaxViolet`, `styleBlue` et `surfaceSubtle`, sans aucun code hexadécimal dispersé dans les écrans.
  - Échelle typographique complète à 9 rôles + `displaySmall` (*Fraunces*) et styles tabulaires `MonoMetric` / `MonoLarge` (*JetBrains Mono*), complétée par *Plus Jakarta Sans* embarquée localement.
  - Grille stricte 4 dp via `FrancaisProSpacing` et `LocalSpacing`, exposée via `FrancaisProThemeTokens`, avec marges horizontales adaptatives (`16 dp` / `20 dp`), bordures `1 dp` et rayons unifiés (`10 / 14 / 18 / 24 dp` + pilule).
  - Adaptation automatique des icônes système (barre d'état et navigation) au thème clair/sombre et fonds de lancement XML dédiés (`values/themes.xml` & `values-night/themes.xml`).
- **Aperçu des performances sur 30 jours** (`PerformanceOverviewScreen`) accessible directement depuis le tableau de bord (*Vos progrès*), les Statistiques et le Profil : graphique multi-séries interactif (Grammaire, Vocabulaire, Écriture), sélecteur 7 j / 14 j / 30 j et sparklines par domaine.
- **Écoute audio directe dans le Dictionnaire** : bouton de prononciation TTS instantanée sur chaque fiche de résultat sans quitter la liste.
- **Bilan des Exercices enrichi** : indicateur global de précision (`%`) dans l'en-tête et filtre « À revoir » dans le récapitulatif de fin de session.

### Amélioré
- **Composants & micro-interactions** : retour tactile sobre sur `PrimaryButton` / `SecondaryButton` (respectant *Réduire les animations*), cibles tactiles ≥ 48 dp sur les actions de section, dégradé éditorial et badge *Objectif ✓* sur `ProgressCard`, double codage sémantique (pastille + libellé) sur les badges d'analyse du Correcteur.
- **Fiabilité & couverture de tests** : corrections de synchronisation des paramètres et migrations Room, plus nouveaux tests unitaires et UI Robolectric vérifiant l'intégralité des tokens du Design System et les nouvelles interactions.

## [1.2.0] — Parcours progressif CECRL, Missions du jour & Studio d'écriture

### Ajouté
- **24 vrais cours CECRL progressifs (A1 → C2)** : 4 chapitres structurés par niveau avec QCM dédiés, mini-exercices intégrés, frise visuelle verticale et déblocage séquentiel chapitre par chapitre puis niveau par niveau (bonus palier **+100 XP**, seuil de validation quiz **≥ 60 %**).
- **Missions du jour sur l'Accueil** (`DailyQuestsEngine`) : 3 objectifs quotidiens calculés en direct depuis Room (*Valider 1 chapitre*, *Réussir 3 exercices*, *Pratiquer son objectif quotidien*) avec barres de progression et récompenses XP.
- **Studio d'écriture dans le Correcteur** : indicateurs de lisibilité en direct (*indice de clarté*, *nombre de phrases*, *temps de lecture estimé*), jauge circulaire de score `/ 100`, filtres interactifs par catégorie d'erreur (*Orthographe*, *Grammaire*, *Syntaxe*, *Style*) et confirmation visuelle de copie.
- **Mode Défi Express (5 questions) & Combo** dans les Exercices : sprint guidé avec barre de progression de session, compteur de série de bonnes réponses en direct et filtres thématiques avec icônes modernes.
- **Historique Room des 10 dernières recherches du Dictionnaire** (migration non destructive SQLite `v1 → v2`) : persistance locale, déduplication insensible aux accents, sélection en un geste, suppression individuelle et effacement global.
- **Vocabulaire SM-2 enrichi** : jauge de maîtrise globale et boutons de rappel dotés d'icônes modernes.

### Amélioré
- Structuration homogène de toutes les pages et sections (`SectionHeader` avec icône dédiée et sous-titre, cartes `LevelCard` et `LessonCard` avec états *Validé / À suivre / Verrouillé*, page de cours en 7 étapes numérotées).
- Suite de tests étendue à **41+ tests unitaires et UI Robolectric** couvrant l'ensemble des moteurs et écrans.

## [1.1.0] — Mise à jour majeure hors ligne

### Ajouté
- **Mot du jour sur l'Accueil** : carte interactive alimentée par la base locale Room (prononciation IPA, niveau CECRL, définition, aperçu synonyme/antonyme, écoute audio locale TTS et accès direct à la fiche complète).
- **Dictionnaire local Room enrichi (48 entrées A1–C2)** :
  - Recherche SQLite instantanée avec tri par pertinence sur les mots (insensible aux accents), définitions, synonymes et antonymes.
  - Filtres de portée dédiés (*Tout*, *Définitions*, *Synonymes*, *Antonymes*, *Favoris*) et catégories grammaticales issues directement de Room.
  - Suggestions floues hors ligne (*« Vous vouliez peut-être dire… »*) en cas de faute de frappe.
  - Puces interactives de synonymes et d'antonymes dans la fiche de mot (navigation directe vers l'entrée liée dans Room ou recherche instantanée).
  - Ajout et suppression de mots personnels hors ligne persistés dans SQLite.
- **Écran intégré Démo & Guide d'utilisation** (`Profil → Démo & Guide` et raccourci Accueil) : bac à sable en direct relié au moteur linguistique hors ligne + manuel interactif en 6 sections filtrables, complété par `docs/DEMO.md` et `docs/USER_GUIDE.md`.
- **Statistiques visuelles interactives** : graphiques natifs Compose Canvas (barres hebdomadaires avec ligne d'objectif et infobulle au toucher, anneau interactif de répartition des erreurs, courbe d'évolution lissée).
- **Transitions de navigation 200 ms** : animations `AnimatedContent` fluides entre les 5 destinations principales (*Accueil*, *Corriger*, *Apprendre*, *Exercices*, *Profil*) respectant la préférence de réduction de mouvement.
- **Encart Version & Nouveautés dans le Profil** : affichage dynamique de `v1.1.0` et du crédit d'auteur (*Temgoua Mendel*).

### Amélioré
- Suite de tests étendue à **28 tests unitaires et UI Robolectric** (incluant les tests Room en mémoire pour le dictionnaire et les tests UI des nouveaux écrans).

## [1.0.0] — Version initiale complète

### Ajouté
- **Autonomie 100 % hors ligne & Confidentialité** :
  - Suppression totale de la permission `INTERNET` dans `AndroidManifest.xml`.
  - Base locale Room (19 entités SQLite) pré-peuplée en ressources linguistiques françaises, sans compte obligatoire, sans publicité et sans suivi.
  - Export et import complets des données utilisateur au format JSON via Android SAF (`CreateDocument` / `OpenDocument`).
- **Moteur linguistique français embarqué** :
  - Tokenisation, analyse lexicale, morphologie, conjugaison multi-temps + recherche inverse de formes conjuguées.
  - Moteur hybride de correction couvrant orthographe (distance d'édition + lexique), accords grammaticaux, homophones fréquents, conjugaison, ponctuation typographique française et style.
  - Calcul de score de qualité (`/ 100`), comparaison Avant/Après et journalisation des erreurs fréquentes.
- **Parcours pédagogique complet (24 écrans)** :
  - Onboarding interactif en 7 étapes (entièrement ignorable).
  - Tableau de bord quotidien avec niveau CECRL, série quotidienne et raccourcis d'entraînement.
  - Académie CECRL (A1 à C2) avec leçons structurées en 8 sections et mini-exercice intégré.
  - 10 formats d'exercices interactifs avec écran de bilan et révision ciblée.
  - Dictées progressives avec synthèse vocale locale `TextToSpeech` (`fr-FR`) et repli visuel automatique.
  - Dictionnaire hors ligne avec favoris et historique.
  - Vocabulaire avec algorithme de répétition espacée SM-2 et ajout de mots personnels.
  - Atelier de rédaction guidé (10 modèles professionnels et académiques) et module d'expression orale.
  - Statistiques visuelles avec 4 graphiques Canvas alimentés en direct par Room.
- **Design System Premium (Material 3)** :
  - Palette sémantique dédiée Clair (`#F4F6FA`, `#FFFFFF`, `#101B36`, `#356AE6`) et Sombre (`#0B1220`, `#131E31`, `#1B2940`, `#7098FF`).
  - Échelle typographique à 9 rôles, grille 4 dp, rayons standardisés (`10 / 14 / 18 / 24 dp`), icône adaptative personnalisée et 19 composants réutilisables.
  - Surlignage interactif des erreurs 4 couleurs et fiche explicative `ErrorDetailBottomSheet`.
  - Accessibilité : `enableEdgeToEdge()`, cibles tactiles ≥ 48 dp, police ajustable et mode lecture facilitée.
- **Qualité, Documentation & GitHub** :
  - 18 tests unitaires et UI Robolectric validés (`gradle :app:testDebugUnitTest`).
  - Analyse statique Android Lint sans erreur (`gradle :app:lintDebug`).
  - Documentation structurée (`README.md`, `ARCHITECTURE.md`, `DESIGN_SYSTEM.md`, `USER_GUIDE.md`, `RELEASE_GUIDE.md`), aperçus UI dans `docs/screenshots/`, modèles d'issues/PR et workflow GitHub Actions CI + Release APK.
