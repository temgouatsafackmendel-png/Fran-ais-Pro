# Journal des modifications (Changelog)

Toutes les évolutions notables de **FRANÇAIS PRO** sont documentées dans ce fichier selon le format [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) et la gestion sémantique de version ([SemVer](https://semver.org/lang/fr/)).

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
