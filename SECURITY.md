# Sécurité et Confidentialité — FRANÇAIS PRO

## Engagements de confidentialité

**FRANÇAIS PRO** est conçu selon le principe de confidentialité par défaut (*Privacy by Design*) :

- **Aucune connexion réseau** : l'application ne déclare pas la permission `android.permission.INTERNET` et ne peut donc transmettre aucune donnée vers l'extérieur.
- **Stockage strictement local** : vos textes corrigés, votre progression, votre vocabulaire et vos statistiques restent sur votre appareil dans la base SQLite privée de l'application (Room).
- **Sauvegarde sous votre contrôle** : les exports JSON utilisent le sélecteur système Android (Storage Access Framework), sans jamais demander d'accès global au stockage de l'appareil.
- **Aucune clé secrète ni traceur embarqué** : aucun SDK publicitaire, aucun outil d'analytics, aucun identifiant tiers.

## Versions prises en charge

| Version | Support sécurité |
| :---: | :---: |
| `1.0.x` | ✅ Prise en charge active |

## Signaler une vulnérabilité

Si vous identifiez une faille de sécurité (par exemple sur l'import JSON, la validation de fichiers, la persistance locale ou une dépendance vulnérable) :

1. **Ne publiez pas** les détails exploitables dans une issue publique.
2. Utilisez l'onglet **Security → Advisories → Report a vulnerability** du dépôt GitHub (ou contactez le mainteneur directement).
3. Décrivez :
   - la version de l'application et la version d'Android concernées ;
   - les étapes de reproduction ;
   - l'impact potentiel et, si possible, une piste de correctif.

Un accusé de réception et une analyse seront communiqués dans les meilleurs délais, suivis d'un correctif dans une version mineure/corrective dédiée.
