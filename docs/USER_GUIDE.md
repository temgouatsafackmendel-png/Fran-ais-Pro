# Guide d'utilisation complet — FRANÇAIS PRO

*Application Android native 100 % hors ligne — Projet réalisé de bout en bout par **Temgoua Mendel**.*

---

## 1. Premiers pas

1. **Onboarding guidé (7 étapes)** : présente l'objectif de l'application, le correcteur, le parcours CECRL et vous permet de choisir votre prénom d'affichage ainsi que votre objectif quotidien (ex. 10, 15 ou 20 minutes/jour).
   - *Astuce* : le bouton **Ignorer** est disponible à chaque étape pour accéder immédiatement à l'écran d'accueil.
2. **Évaluation initiale (optionnelle)** : estime rapidement votre niveau indicatif (**A1 à C2**) pour personnaliser vos recommandations. *(Cette estimation pédagogique interne ne remplace pas une certification officielle.)*
3. **Démo intégrée à l'application** : à tout moment, rendez-vous dans **Profil → Démo & Guide d'utilisation** pour tester le correcteur sur des exemples prêts à l'emploi ou consulter ce manuel hors ligne.

---

## 2. Navigation principale (5 destinations)

La barre de navigation inférieure (ou latérale sur tablette ≥ 600 dp) donne accès aux 5 espaces clés avec des transitions fluides de 200 ms :

| Onglet | Rôle principal |
| :--- | :--- |
| **Accueil** | Score global `/ 100`, niveau CECRL, série active, objectif du jour et raccourcis *Votre entraînement du jour* |
| **Corriger** | Correcteur linguistique hybride hors ligne, surlignage 4 couleurs, fiche explicative et Atelier de rédaction |
| **Apprendre** | Académie CECRL (A1 à C2), Dictionnaire hors ligne, Conjugueur (+ recherche inverse) et Vocabulaire SM-2 |
| **Exercices** | 10 formats d'exercices adaptatifs, Dictées progressives et Expression orale guidée |
| **Profil** | Compétences maîtrisées / à renforcer, Statistiques interactives, Démo & Guide, Historique, Favoris, Paramètres et Sauvegarde JSON |

---

## 3. Utiliser le Correcteur linguistique

1. Ouvrez l'onglet **Corriger** et saisissez ou collez votre texte (ou sélectionnez un texte d'exemple).
2. Touchez **Analyser** (ou activez l'analyse automatique dans **Paramètres**).
3. Repérez les anomalies grâce au **code couleur sémantique** :
   - 🔴 **Rouge** : Orthographe (fautes d'usage, accents, lexique)
   - 🟠 **Orange** : Grammaire & Conjugaison (accords nom/verbe, participe passé, auxiliaires)
   - 🟣 **Violet** : Syntaxe & Ponctuation (tournures incorrectes, espaces typographiques françaises)
   - 🔵 **Bleu** : Style (répétitions, améliorations de formulation)
4. **Touchez un fragment surligné** pour ouvrir la fiche pédagogique (`BottomSheet`) :
   - Consultez la règle et un exemple correct.
   - Touchez **Accepter** pour appliquer la correction ou **Ignorer** pour conserver votre tournure.
5. Utilisez **Tout corriger** pour appliquer en un geste les corrections sûres, **Annuler / Rétablir** pour revenir en arrière, et **Avant / Après** pour comparer le texte initial et le texte corrigé.
6. Besoin de rédiger un texte structuré ? Touchez **Atelier de rédaction** pour choisir parmi **10 modèles guidés** (e-mail professionnel, lettre de motivation, résumé, dissertation, rapport, etc.).

---

## 4. Académie CECRL, Dictionnaire, Conjugueur & Vocabulaire

### Académie CECRL (A1 → C2)
- Choisissez votre niveau dans l'onglet **Apprendre** (*A1 Découverte* jusqu'à *C2 Maîtrise*).
- Chaque leçon est organisée en **8 sections** : objectif, explication claire, règle à retenir, exemples commentés, pièges fréquents, mini-exercice intégré, récapitulatif et quiz de validation.
- Ajoutez vos leçons importantes en **Favoris** (icône cœur) pour les relire rapidement.

### Dictionnaire hors ligne
- Recherchez un mot instantanément sans Internet : nature grammaticale, définition, exemples, synonymes, antonymes et prononciation vocale locale.

### Conjugueur & Recherche inverse
- Consultez les tableaux de conjugaison (indicatif, subjonctif, conditionnel, impératif, participe).
- Saisissez une forme conjuguée (ex. *« firent »*, *« irais »*) dans la barre de **recherche inverse** pour retrouver son infinitif, son mode, son temps et sa personne.

### Vocabulaire intelligent (SM-2)
- Révisez vos cartes mémoire chaque jour et notez votre facilité de rappel (*À revoir*, *Difficile*, *Bien*, *Facile*) : l'algorithme **SM-2** calcule automatiquement la date idéale de votre prochaine révision.

---

## 5. Exercices, Dictées & Expression orale

- **10 formats d'exercices** : QCM, texte à trous, correction de phrase, accords, conjugaison, homophones, ponctuation, ordre des mots, transformation de phrase et vocabulaire. Un écran de bilan récapitule votre score et les notions à retravailler.
- **Dictées progressives** :
  - Si une voix française hors ligne est disponible sur votre appareil, la dictée est lue automatiquement via `TextToSpeech` (vitesse réglable dans **Paramètres**).
  - Si aucune voix française n'est installée, le **mode lecture visuelle rythmée** s'active automatiquement pour vous permettre de réaliser la dictée sans blocage.
  - À la validation, une comparaison mot à mot détaille vos erreurs et calcule votre note sur 20.
- **Expression orale** :
  - Entraînez votre prononciation et votre diction avec les phrases modèles (virelangues, liaisons, articulation).
  - Pour la reconnaissance vocale en mode avion, téléchargez le pack *« Français (France) »* dans les paramètres vocaux hors ligne d'Android ; à défaut, utilisez le mode d'auto-transcription guidée intégré.

---

## 6. Statistiques interactives, Paramètres & Sauvegarde JSON

### Statistiques détaillées (**Profil → Statistiques détaillées**)
- **Progression hebdomadaire (BarChart)** : touchez n'importe quel jour pour afficher l'infobulle détaillée (minutes effectuées et état d'objectif quotidien).
- **Évolution des scores (AreaChart)** : inspectez chaque session d'exercice ou de dictée et suivez votre moyenne.
- **Répartition des erreurs (Donut Chart)** : touchez une catégorie dans la légende pour mettre son arc en évidence et afficher son pourcentage exact au centre.
- **Compétences maîtrisées & Réalisations** : consultez vos domaines forts et vos badges débloqués.

### Paramètres & Accessibilité (**Profil → Paramètres**)
- **Apparence** : thème **Clair**, **Sombre** ou **Système**.
- **Objectif quotidien** : personnalisez votre durée cible quotidienne.
- **Synthèse vocale** : ajustez la vitesse de lecture des dictées et exemples.
- **Rappel quotidien local** : planifié en local via WorkManager (aucun serveur distant).
- **Réduire les animations** : respecté automatiquement si activé dans les paramètres d'accessibilité d'Android.

### Sauvegarder et transférer vos progrès (**Profil → Sauvegarde & restauration**)
1. Touchez **Exporter en JSON** pour enregistrer un fichier `.json` dans le dossier de votre choix via le sélecteur système Android (SAF — aucune permission stockage requise).
2. Conservez ce fichier en sécurité (PC, clé USB, stockage local).
3. Après une réinstallation ou sur un nouveau téléphone, ouvrez **Importer une sauvegarde JSON** et sélectionnez votre fichier pour restaurer immédiatement votre profil, votre progression, vos favoris et votre historique.

---

## 7. Dépannage rapide (FAQ)

- **La dictée ne lit pas le texte à voix haute ?** Vérifiez dans *Paramètres Android → Système → Langues → Synthèse vocale* qu'une voix « Français (France) » hors ligne est installée, ou utilisez le **mode lecture visuelle rythmée** intégré.
- **Mes données sont-elles envoyées sur un serveur ?** Non. L'application ne possède pas la permission `INTERNET` : tout reste exclusivement sur votre appareil.
- **Comment repartir à zéro ou changer de niveau ?** Utilisez **Profil → Refaire l'évaluation de niveau** pour recalibrer vos recommandations.
