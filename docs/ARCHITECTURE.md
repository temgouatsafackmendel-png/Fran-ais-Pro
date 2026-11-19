# Documentation Architecturale — FRANÇAIS PRO

## 1. Principes directeurs

- **Clean Architecture & MVVM** : séparation stricte entre l'interface Jetpack Compose, la gestion d'état (`ViewModel` + `StateFlow`), les moteurs métier (`linguistics/`, `learning/`) et la couche données (`data/`, `core/database/`, `core/datastore/`).
- **Injection de dépendances par constructeur** : centralisée dans `AppContainer` (`di/AppContainer.kt`), instancié par `FrancaisProApplication`.
- **Zéro dépendance réseau** : les bibliothèques réseau et cloud sont désactivées dans Gradle et la permission `INTERNET` est volontairement absente de `AndroidManifest.xml`.

## 2. Moteur linguistique hybride (`linguistics/`)

Le moteur `HybridCorrectionEngine` orchestre des sous-modules implémentant l'interface `CorrectionRuleModule`, ce qui permet d'enrichir ou de remplacer chaque brique indépendamment :

1. `FrenchTokenizer` : segmentation en phrases, mots, nombres, élisions (`l'`, `j'`, `qu'`) et ponctuation avec conservation exacte des indices `[start, end]`.
2. `FrenchLexicon` : lexique local indexé sans accents, métadonnées genre/pluriel des noms et distance Damerau-Levenshtein.
3. `FrenchSpellingEngine` : fautes d'accents, cédilles, doubles consonnes et fautes de frappe sans faux positifs sur les noms propres ou acronymes.
4. `FrenchGrammarRules` : élisions, homophones contextuels, accords déterminant/nom/adjectif, accords sujet-verbe, infinitif vs participe passé, accord du participe passé (être, avoir, COD antéposé `que`), subjonctif (`il faut que`) et concordance hypothétique (`si`).
5. `FrenchSyntaxAndStyleEngine` : doublons immédiats, pléonasmes, ponctuation française et conseils stylistiques marqués `isAmbiguous = true` (jamais appliqués silencieusement).
6. `FrenchConjugatorEngine` : conjugaison complète sur 10 temps + table inversée pour reconnaître une forme conjuguée.

## 3. Base de données locale Room (19 entités)

`FrancaisProDatabase` regroupe les 19 entités normalisées avec clés étrangères (`CASCADE`) et index : `UserProfile`, `Lesson`, `LessonProgress`, `Exercise`, `ExerciseAttempt`, `DictionaryEntry`, `Verb`, `Conjugation`, `CorrectionHistory`, `CorrectionError`, `VocabularyWord`, `VocabularyProgress`, `Dictation`, `DictationAttempt`, `DailyGoal`, `Achievement`, `LearningStatistics`, `Favorite`, `AppSettings`.

## 4. Sauvegarde & Restauration

`BackupManager` sérialise la progression en JSON versionné (`schemaVersion = 1`) via Moshi et valide l'intégralité du fichier avant d'exécuter une transaction Room atomique (`withTransaction`), éliminant tout risque de corruption lors d'un import invalide.
