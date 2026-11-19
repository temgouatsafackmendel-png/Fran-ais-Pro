## Résumé des modifications
Décrivez succinctement l'objectif et le périmètre de cette Pull Request.

## Type de changement
- [ ] Correction de bug (`fix`)
- [ ] Nouvelle fonctionnalité (`feat`)
- [ ] Amélioration du moteur linguistique / contenu pédagogique (`linguistics`)
- [ ] Documentation / Captures (`docs`)
- [ ] Refactoring / Tests (`refactor` / `test`)

## Checklist avant revue
- [ ] Aucune dépendance réseau ni permission `INTERNET` ajoutée (100 % hors ligne préservé).
- [ ] Design System respecté (aucune couleur hexadécimale codée en dur dans les écrans Compose).
- [ ] `gradle :app:testDebugUnitTest` passe entièrement.
- [ ] `gradle :app:lintDebug` passe sans erreur.
- [ ] `gradle :app:assembleDebug` compile sans erreur.
- [ ] Documentation / `CHANGELOG.md` mis à jour si nécessaire.
