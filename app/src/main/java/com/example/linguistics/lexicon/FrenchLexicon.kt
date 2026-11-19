package com.example.linguistics.lexicon

import com.example.linguistics.model.FrenchNormalizer
import kotlin.math.min

enum class Gender { MASCULINE, FEMININE, BOTH }

data class NounInfo(
    val singular: String,
    val plural: String,
    val gender: Gender,
    val startsWithVowelOrMuteH: Boolean = false
)

/**
 * Lexique local indexé et moteur de proximité orthographique (Damerau-Levenshtein).
 * Contient un vocabulaire courant et académique vérifié, les fautes d'accents/doubles consonnes
 * fréquentes, ainsi que les métadonnées de genre/nombre pour l'analyseur grammatical.
 */
class FrenchLexicon {

    // Fautes d'orthographe fréquentes (accents, doubles consonnes, graphies erronées) -> forme exacte
    val commonMisspellings: Map<String, Pair<String, String>> = mapOf(
        "francais" to ("français" to "La cédille est obligatoire devant « a » pour conserver le son [s]."),
        "francaise" to ("française" to "La cédille est obligatoire devant « a » dans « française »."),
        "francaises" to ("françaises" to "La cédille est obligatoire devant « a »."),
        "etudiant" to ("étudiant" to "Accent aigu manquant sur le « e » initial."),
        "etudiants" to ("étudiants" to "Accent aigu manquant sur le « e » initial."),
        "etudiante" to ("étudiante" to "Accent aigu manquant sur le « e » initial."),
        "etudiantes" to ("étudiantes" to "Accent aigu manquant sur le « e » initial."),
        "ecole" to ("école" to "Accent aigu manquant sur le « e » initial de « école »."),
        "ecoles" to ("écoles" to "Accent aigu manquant sur le « e » initial."),
        "eleve" to ("élève" to "Accents manquants dans « élève » (aigu puis grave)."),
        "eleves" to ("élèves" to "Accents manquants dans « élèves »."),
        "etre" to ("être" to "Accent circonflexe requis sur le « ê » de « être »."),
        "meme" to ("même" to "Accent circonflexe requis sur le « ê » de « même »."),
        "memes" to ("mêmes" to "Accent circonflexe requis sur le « ê » de « mêmes »."),
        "tete" to ("tête" to "Accent circonflexe requis sur le « ê » de « tête »."),
        "fete" to ("fête" to "Accent circonflexe requis sur le « ê » de « fête »."),
        "foret" to ("forêt" to "Accent circonflexe requis sur le « ê » de « forêt »."),
        "garcon" to ("garçon" to "Cédille requise devant « o » pour le son [s]."),
        "lecon" to ("leçon" to "Cédille requise devant « o » dans « leçon »."),
        "lecons" to ("leçons" to "Cédille requise devant « o » dans « leçons »."),
        "apercu" to ("aperçu" to "Cédille requise devant « u » dans « aperçu »."),
        "recu" to ("reçu" to "Cédille requise devant « u » dans « reçu »."),
        "deja" to ("déjà" to "Accent aigu sur le « é » et accent grave sur le « à »."),
        "apres" to ("après" to "Accent grave requis sur le « è » de « après »."),
        "tres" to ("très" to "Accent grave requis sur le « è » de « très »."),
        "pres" to ("près" to "Accent grave requis sur le « è » de « près »."),
        "voila" to ("voilà" to "Accent grave requis sur le « à » final de « voilà »."),
        "bientot" to ("bientôt" to "Accent circonflexe requis sur le « ô » de « bientôt »."),
        "plutot" to ("plutôt" to "Accent circonflexe requis sur le « ô » de « plutôt »."),
        "aussitot" to ("aussitôt" to "Accent circonflexe requis sur le « ô »."),
        "peut-etre" to ("peut-être" to "Accent circonflexe requis dans « peut-être »."),
        "developpement" to ("développement" to "Accent aigu manquant sur le premier « é »."),
        "developper" to ("développer" to "Accent aigu manquant sur le premier « é »."),
        "reussi" to ("réussi" to "Accent aigu requis sur le « é » de « réussi »."),
        "reussir" to ("réussir" to "Accent aigu requis sur le « é » de « réussir »."),
        "reussite" to ("réussite" to "Accent aigu requis sur le « é » de « réussite »."),
        "reponse" to ("réponse" to "Accent aigu requis sur le « é » de « réponse »."),
        "reponses" to ("réponses" to "Accent aigu requis sur le « é » de « réponses »."),
        "probleme" to ("problème" to "Accent grave requis sur le « è » de « problème »."),
        "problemes" to ("problèmes" to "Accent grave requis sur le « è » de « problèmes »."),
        "systeme" to ("système" to "Accent grave requis sur le « è » de « système »."),
        "systemes" to ("systèmes" to "Accent grave requis sur le « è » de « systèmes »."),
        "methode" to ("méthode" to "Accent aigu requis sur le « é » de « méthode »."),
        "strategie" to ("stratégie" to "Accent aigu requis sur le « é » de « stratégie »."),
        "verite" to ("vérité" to "Accents aigus requis dans « vérité »."),
        "qualite" to ("qualité" to "Accent aigu requis sur le « é » final."),
        "securite" to ("sécurité" to "Accents aigus requis dans « sécurité »."),
        "societe" to ("société" to "Accents aigus requis dans « société »."),
        "activite" to ("activité" to "Accent aigu requis sur le « é » final."),
        "facilite" to ("facilité" to "Accent aigu requis sur le « é » final."),
        "realite" to ("réalité" to "Accents aigus requis dans « réalité »."),
        "idee" to ("idée" to "Accent aigu requis sur le premier « é » de « idée »."),
        "idees" to ("idées" to "Accent aigu requis sur le premier « é » de « idées »."),
        "annee" to ("année" to "Accent aigu requis dans « année »."),
        "annees" to ("années" to "Accent aigu requis dans « années »."),
        "journee" to ("journée" to "Accent aigu requis dans « journée »."),
        "soiree" to ("soirée" to "Accent aigu requis dans « soirée »."),
        "ecrire" to ("écrire" to "Accent aigu requis sur le « é » initial."),
        "ecrit" to ("écrit" to "Accent aigu requis sur le « é » initial."),
        "ecoute" to ("écoute" to "Accent aigu requis sur le « é » initial."),
        "ecouter" to ("écouter" to "Accent aigu requis sur le « é » initial."),
        "general" to ("général" to "Accents aigus requis dans « général »."),
        "generale" to ("générale" to "Accents aigus requis dans « générale »."),
        "different" to ("différent" to "Accent aigu requis dans « différent »."),
        "differente" to ("différente" to "Accent aigu requis dans « différente »."),
        "differents" to ("différents" to "Accent aigu requis dans « différents »."),
        "regle" to ("règle" to "Accent grave requis sur le « è » de « règle »."),
        "regles" to ("règles" to "Accent grave requis sur le « è » de « règles »."),
        "preposition" to ("préposition" to "Accent aigu requis dans « préposition »."),
        "accentue" to ("accentué" to "Accent aigu requis sur le « é » final."),
        "language" to ("langage" to "En français, « langage » s'écrit sans « u » (contrairement à l'anglais)."),
        "addresse" to ("adresse" to "En français, « adresse » ne prend qu'un seul « d »."),
        "addresses" to ("adresses" to "En français, « adresses » ne prend qu'un seul « d »."),
        "appeller" to ("appeler" to "L'infinitif « appeler » ne prend qu'un seul « l »."),
        "professionel" to ("professionnel" to "« Professionnel » prend deux « n »."),
        "professionelle" to ("professionnelle" to "« Professionnelle » prend deux « n » et deux « l »."),
        "professionels" to ("professionnels" to "« Professionnels » prend deux « n »."),
        "orthografe" to ("orthographe" to "Le son [f] final s'écrit « ph » dans « orthographe »."),
        "gramaire" to ("grammaire" to "« Grammaire » prend deux « m »."),
        "toujour" to ("toujours" to "L'adverbe « toujours » prend toujours un « s » final."),
        "beaucoups" to ("beaucoup" to "L'adverbe « beaucoup » est invariable et ne prend jamais de « s »."),
        "maintenent" to ("maintenant" to "La terminaison correcte est « -ant »."),
        "acceuil" to ("accueil" to "Après « c », le son [œj] s'écrit « ueil » dans « accueil »."),
        "acceuillir" to ("accueillir" to "Le verbe s'écrit « accueillir » (u avant e)."),
        "nouriture" to ("nourriture" to "« Nourriture » prend deux « r »."),
        "courrir" to ("courir" to "L'infinitif « courir » ne prend qu'un seul « r »."),
        "mourrir" to ("mourir" to "L'infinitif « mourir » ne prend qu'un seul « r »."),
        "malgrès" to ("malgré" to "La préposition « malgré » se termine par « é » sans « s »."),
        "malgre" to ("malgré" to "Accent aigu requis sur le « é » final de « malgré »."),
        "parmis" to ("parmi" to "La préposition « parmi » ne prend jamais de « s » final."),
        "hormis" to ("hormis" to ""), // valid - omitted below if empty
        "horsmis" to ("hormis" to "« Hormis » s'écrit sans « s » au milieu."),
        "biensur" to ("bien sûr" to "« Bien sûr » s'écrit en deux mots avec un accent circonflexe."),
        "biensûr" to ("bien sûr" to "« Bien sûr » s'écrit en deux mots séparés."),
        "biensure" to ("bien sûr" to "« Bien sûr » s'écrit en deux mots avec un accent circonflexe."),
        "daccord" to ("d'accord" to "Apostrophe obligatoire dans « d'accord »."),
        "aujourdhui" to ("aujourd'hui" to "Apostrophe obligatoire dans « aujourd'hui »."),
        "quelquun" to ("quelqu'un" to "Apostrophe obligatoire dans « quelqu'un »."),
        "parceque" to ("parce que" to "« Parce que » s'écrit en deux mots."),
        "entrain" to ("en train" to "Pour une action en cours, écrire « en train » (deux mots)."),
        "bizard" to ("bizarre" to "L'adjectif correct est « bizarre »."),
        "faisez" to ("faites" to "À la 2e personne du pluriel, le verbe « faire » se conjugue « vous faites »."),
        "disez" to ("dites" to "À la 2e personne du pluriel, le verbe « dire » se conjugue « vous dites »."),
        "croivent" to ("croient" to "La 3e personne du pluriel de « croire » est « ils croient »."),
        "voyent" to ("voient" to "La 3e personne du pluriel de « voir » est « ils voient »."),
        "prennentt" to ("prennent" to "Faute de frappe : double consonne finale superflue."),
        "travailller" to ("travailler" to "Faute de frappe : « travailler » ne prend que deux « l »."),
        "exercisse" to ("exercice" to "Le nom « exercice » s'écrit avec un « c » final."),
        "exercisses" to ("exercices" to "Le nom « exercices » s'écrit avec un « c »."),
        "dictee" to ("dictée" to "Accent aigu requis sur le « é » de « dictée »."),
        "dictees" to ("dictées" to "Accent aigu requis sur le « é » de « dictées »."),
        "vocabulairee" to ("vocabulaire" to "Faute de frappe sur la voyelle finale."),
        "conjugaison" to ("conjugaison" to ""),
        "conjugaisonss" to ("conjugaisons" to "Faute de frappe sur le « s » final."),
        "connaissance" to ("connaissance" to ""),
        "connaisance" to ("connaissance" to "« Connaissance » prend deux « s »."),
        "interessant" to ("intéressant" to "Accent aigu requis sur le premier « é »."),
        "interessante" to ("intéressante" to "Accent aigu requis sur le premier « é »."),
        "interressant" to ("intéressant" to "Un seul « r » et accent aigu : « intéressant »."),
        "evenement" to ("événement" to "Accents aigus requis dans « événement »."),
        "evidemment" to ("évidemment" to "Accent aigu requis sur le « é » initial."),
        "absoluement" to ("absolument" to "« Absolument » s'écrit sans « e » avant « -ment »."),
        "vraimment" to ("vraiment" to "« Vraiment » ne prend qu'un seul « m »."),
        "notament" to ("notamment" to "« Notamment » prend deux « m »."),
        "recemment" to ("récemment" to "Accent aigu requis sur le « é » de « récemment »."),
        "independamment" to ("indépendamment" to "Accent aigu requis dans « indépendamment »."),
        "exellent" to ("excellent" to "« Excellent » s'écrit avec « xc »."),
        "excellant" to ("excellent" to "L'adjectif s'écrit « excellent » avec « -ent »."),
        "changementt" to ("changement" to "Faute de frappe finale."),
        "environment" to ("environnement" to "En français, « environnement » prend deux « n »."),
        "gouvernementt" to ("gouvernement" to "Faute de frappe finale."),
        "appartementt" to ("appartement" to "Faute de frappe finale."),
        "apartement" to ("appartement" to "« Appartement » prend deux « p »."),
        "connection" to ("connexion" to "En français, le nom s'écrit « connexion » avec un « x »."),
        "definitif" to ("définitif" to "Accent aigu requis sur le « é » initial."),
        "ameliorer" to ("améliorer" to "Accent aigu requis sur le « é » de « améliorer »."),
        "amelioration" to ("amélioration" to "Accent aigu requis sur le « é » de « amélioration »."),
        "redaction" to ("rédaction" to "Accent aigu requis sur le « é » de « rédaction »."),
        "reviser" to ("réviser" to "Accent aigu requis sur le « é » de « réviser »."),
        "revision" to ("révision" to "Accent aigu requis sur le « é » de « révision »."),
        "recompense" to ("récompense" to "Accent aigu requis sur le « é » de « récompense »."),
        "criteres" to ("critères" to "Accent grave requis sur le « è » de « critères »."),
        "critere" to ("critère" to "Accent grave requis sur le « è » de « critère »."),
        "experience" to ("expérience" to "Accent aigu requis sur le « é » de « expérience »."),
        "experiences" to ("expériences" to "Accent aigu requis sur le « é » de « expériences »."),
        "competence" to ("compétence" to "Accent aigu requis sur le « é » de « compétence »."),
        "competences" to ("compétences" to "Accent aigu requis sur le « é » de « compétences »."),
        "maitrise" to ("maîtrise" to "Graphie traditionnelle recommandée : « maîtrise »."),
        "succes" to ("succès" to "Accent grave requis sur le « è » de « succès »."),
        "progres" to ("progrès" to "Accent grave requis sur le « è » de « progrès »."),
        "acces" to ("accès" to "Accent grave requis sur le « è » de « accès »."),
        "proces" to ("procès" to "Accent grave requis sur le « è » de « procès »."),
        "delai" to ("délai" to "Accent aigu requis sur le « é » de « délai »."),
        "resume" to ("résumé" to "Accents aigus requis dans « résumé »."),
        "numero" to ("numéro" to "Accent aigu requis sur le « é » de « numéro »."),
        "telephone" to ("téléphone" to "Accents aigus requis dans « téléphone »."),
        "francophone" to ("francophone" to "")
    ).filterValues { it.second.isNotEmpty() }

    val nouns: Map<String, NounInfo> = listOf(
        NounInfo("étudiant", "étudiants", Gender.MASCULINE, true),
        NounInfo("étudiante", "étudiantes", Gender.FEMININE, true),
        NounInfo("enfant", "enfants", Gender.MASCULINE, true),
        NounInfo("fleur", "fleurs", Gender.FEMININE, false),
        NounInfo("maison", "maisons", Gender.FEMININE, false),
        NounInfo("entreprise", "entreprises", Gender.FEMININE, true),
        NounInfo("cour", "cours", Gender.FEMININE, false),
        NounInfo("école", "écoles", Gender.FEMININE, true),
        NounInfo("livre", "livres", Gender.MASCULINE, false),
        NounInfo("lettre", "lettres", Gender.FEMININE, false),
        NounInfo("phrase", "phrases", Gender.FEMININE, false),
        NounInfo("texte", "textes", Gender.MASCULINE, false),
        NounInfo("mot", "mots", Gender.MASCULINE, false),
        NounInfo("règle", "règles", Gender.FEMININE, false),
        NounInfo("faute", "fautes", Gender.FEMININE, false),
        NounInfo("erreur", "erreurs", Gender.FEMININE, true),
        NounInfo("ami", "amis", Gender.MASCULINE, true),
        NounInfo("amie", "amies", Gender.FEMININE, true),
        NounInfo("homme", "hommes", Gender.MASCULINE, true),
        NounInfo("femme", "femmes", Gender.FEMININE, false),
        NounInfo("histoire", "histoires", Gender.FEMININE, true),
        NounInfo("idée", "idées", Gender.FEMININE, true),
        NounInfo("journée", "journées", Gender.FEMININE, false),
        NounInfo("année", "années", Gender.FEMININE, true),
        NounInfo("jour", "jours", Gender.MASCULINE, false),
        NounInfo("projet", "projets", Gender.MASCULINE, false),
        NounInfo("rapport", "rapports", Gender.MASCULINE, false),
        NounInfo("message", "messages", Gender.MASCULINE, false),
        NounInfo("travail", "travaux", Gender.MASCULINE, false),
        NounInfo("langue", "langues", Gender.FEMININE, false),
        NounInfo("ville", "villes", Gender.FEMININE, false),
        NounInfo("voiture", "voitures", Gender.FEMININE, false),
        NounInfo("table", "tables", Gender.FEMININE, false),
        NounInfo("chaise", "chaises", Gender.FEMININE, false),
        NounInfo("porte", "portes", Gender.FEMININE, false),
        NounInfo("fenêtre", "fenêtres", Gender.FEMININE, false),
        NounInfo("question", "questions", Gender.FEMININE, false),
        NounInfo("réponse", "réponses", Gender.FEMININE, false),
        NounInfo("solution", "solutions", Gender.FEMININE, false),
        NounInfo("chambre", "chambres", Gender.FEMININE, false),
        NounInfo("professeur", "professeurs", Gender.MASCULINE, false),
        NounInfo("élève", "élèves", Gender.BOTH, true),
        NounInfo("ami", "amis", Gender.MASCULINE, true),
        NounInfo("équipe", "équipes", Gender.FEMININE, true),
        NounInfo("client", "clients", Gender.MASCULINE, false),
        NounInfo("cliente", "clientes", Gender.FEMININE, false),
        NounInfo("candidat", "candidats", Gender.MASCULINE, false),
        NounInfo("candidature", "candidatures", Gender.FEMININE, false),
        NounInfo("compétence", "compétences", Gender.FEMININE, false),
        NounInfo("expérience", "expériences", Gender.FEMININE, true),
        NounInfo("leçon", "leçons", Gender.FEMININE, false),
        NounInfo("exercice", "exercices", Gender.MASCULINE, true),
        NounInfo("dictée", "dictées", Gender.FEMININE, false),
        NounInfo("verbe", "verbes", Gender.MASCULINE, false),
        NounInfo("pomme", "pommes", Gender.FEMININE, false),
        NounInfo("chanson", "chansons", Gender.FEMININE, false),
        NounInfo("robe", "robes", Gender.FEMININE, false),
        NounInfo("film", "films", Gender.MASCULINE, false),
        NounInfo("article", "articles", Gender.MASCULINE, true),
        NounInfo("dossier", "dossiers", Gender.MASCULINE, false)
    ).associateBy { it.singular }

    val pluralToNoun: Map<String, NounInfo> = nouns.values.associateBy { it.plural }

    val knownValidWords: Set<String> by lazy {
        buildSet {
            // Mots outils, pronoms, prépositions, conjonctions, adverbes, déterminants
            addAll(
                listOf(
                    "le", "la", "les", "un", "une", "des", "du", "de", "au", "aux", "ce", "cet", "cette", "ces",
                    "mon", "ma", "mes", "ton", "ta", "tes", "son", "sa", "ses", "notre", "nos", "votre", "vos", "leur", "leurs",
                    "je", "tu", "il", "elle", "on", "nous", "vous", "ils", "elles", "me", "te", "se", "lui", "eux", "moi", "toi", "soi",
                    "qui", "que", "quoi", "dont", "où", "lequel", "laquelle", "lesquels", "lesquelles",
                    "et", "ou", "mais", "donc", "or", "ni", "car", "si", "comme", "quand", "lorsque", "puisque", "quoique", "bien",
                    "dans", "sur", "sous", "avec", "sans", "pour", "par", "vers", "chez", "entre", "parmi", "malgré", "depuis",
                    "pendant", "avant", "après", "devant", "derrière", "contre", "en", "à", "y", "ne", "pas", "plus", "jamais",
                    "rien", "personne", "toujours", "souvent", "parfois", "déjà", "encore", "très", "trop", "peu", "beaucoup",
                    "assez", "aussi", "ainsi", "alors", "ensuite", "enfin", "cependant", "toutefois", "néanmoins", "pourtant",
                    "tout", "tous", "toute", "toutes", "chaque", "quelque", "quelques", "plusieurs", "aucun", "aucune",
                    "autre", "autres", "même", "mêmes", "tel", "telle", "tels", "telles", "ça", "cela", "ceci", "celui", "celle",
                    "ceux", "celles", "ici", "là", "hier", "demain", "aujourd'hui", "maintenant", "bientôt", "tôt", "tard",
                    "oui", "non", "merci", "bonjour", "bonsoir", "salut", "madame", "monsieur", "cordialement", "bienveillance",
                    "français", "française", "françaises", "francophone", "francophones", "cour", "grande", "grand", "grands", "grandes",
                    "petit", "petite", "petits", "petites", "beau", "bel", "belle", "beaux", "belles", "bon", "bonne", "bons", "bonnes",
                    "nouveau", "nouvel", "nouvelle", "nouveaux", "nouvelles", "vieux", "vieil", "vieille", "jeune", "jeunes",
                    "haut", "haute", "bas", "basse", "long", "longue", "court", "courte", "clair", "claire", "précis", "précise",
                    "juste", "vrai", "vraie", "faux", "fausse", "simple", "complexe", "facile", "difficile", "important", "importante",
                    "utile", "nécessaire", "possible", "impossible", "rapide", "lent", "lente", "heureux", "heureuse", "content", "contente",
                    "intelligent", "intelligente", "sérieux", "sérieuse", "professionnel", "professionnelle", "quotidien", "quotidienne",
                    "orthographe", "grammaire", "conjugaison", "syntaxe", "vocabulaire", "ponctuation", "style", "rédaction",
                    "expression", "orale", "écrite", "compréhension", "académie", "niveau", "progression", "objectif", "score",
                    "succès", "progrès", "maîtrise", "langage", "adresse", "accueil", "nourriture", "stratégie", "méthode", "système",
                    "problème", "vérité", "qualité", "sécurité", "société", "activité", "réalité", "délai", "résumé", "numéro",
                    "téléphone", "événement", "évidemment", "absolument", "vraiment", "notamment", "récemment", "excellent",
                    "environnement", "gouvernement", "appartement", "connexion", "définitif", "améliorer", "amélioration",
                    "réviser", "révision", "récompense", "critère", "critères", "hormis", "sûr", "sûre", "accord", "train",
                    "bizarre", "parce", "peut-être", "est-ce", "c'est", "s'est", "j'ai", "j'avais", "l'école", "d'accord",
                    "beauté", "liberté", "clarté", "rigueur", "nuance", "nuances", "éloquence", "perspicacité", "subtilité",
                    "pragmatique", "exhaustif", "exhaustive", "pertinent", "pertinente", "cohérent", "cohérente", "concis", "concise",
                    "bonjour", "objet", "pièce", "jointe", "salutations", "distinguées", "veuillez", "agréer", "disponible",
                    "entretien", "poste", "mission", "résultat", "résultats", "analyse", "bilan", "recommandation", "recommandations"
                )
            )
            // Noms singuliers et pluriels
            nouns.values.forEach {
                add(it.singular)
                add(it.plural)
            }
            // Formes verbales fréquentes et participes passés accordés
            addAll(
                listOf(
                    "être", "suis", "es", "est", "sommes", "êtes", "sont", "étais", "était", "étions", "étiez", "étaient",
                    "serai", "seras", "sera", "serons", "serez", "seront", "serais", "serait", "serions", "seriez", "seraient",
                    "sois", "soit", "soyons", "soyez", "soient", "été", "étant", "fus", "fut", "fûmes", "fûtes", "furent",
                    "avoir", "ai", "as", "a", "avons", "avez", "ont", "avais", "avait", "avions", "aviez", "avaient",
                    "aurai", "auras", "aura", "aurons", "aurez", "auront", "aurais", "aurait", "aurions", "auriez", "auraient",
                    "aie", "aies", "ait", "ayons", "ayez", "aient", "eu", "eue", "eus", "eues", "ayant", "eut", "eurent",
                    "aller", "vais", "vas", "va", "allons", "allez", "vont", "allais", "allait", "allions", "alliez", "allaient",
                    "irai", "iras", "ira", "irons", "irez", "iront", "irais", "irait", "aille", "ailles", "aillent", "allé", "allée", "allés", "allées",
                    "faire", "fais", "fait", "faisons", "faites", "font", "faisais", "faisait", "ferai", "feras", "fera", "ferais", "ferait",
                    "fasse", "fasses", "fassions", "fassiez", "fassent", "faite", "faits", "faites", "faisant",
                    "venir", "viens", "vient", "venons", "venez", "viennent", "venais", "venait", "viendrai", "viendras", "viendra",
                    "viendrais", "viendrait", "vienne", "viennes", "venions", "veniez", "venu", "venue", "venus", "venues",
                    "savoir", "sais", "sait", "savons", "savez", "savent", "savais", "savait", "saurai", "saurais", "sache", "saches", "su", "sue", "sus",
                    "pouvoir", "peux", "peut", "pouvons", "pouvez", "peuvent", "pouvais", "pouvait", "pourrai", "pourrais", "puisse", "puisses", "pu",
                    "vouloir", "veux", "veut", "voulons", "voulez", "veulent", "voulais", "voulait", "voudrai", "voudrais", "veuille", "veuillez", "voulu",
                    "devoir", "dois", "doit", "devons", "devez", "doivent", "devais", "devait", "devrai", "devrais", "doive", "dû", "due", "dus",
                    "falloir", "faut", "fallait", "faudra", "faudrait", "faille", "fallu",
                    "apprendre", "apprends", "apprend", "apprenons", "apprenez", "apprennent", "apprenais", "apprenait", "apprendrai", "appris", "apprise", "apprises",
                    "prendre", "prends", "prend", "prenons", "prenez", "prennent", "prenais", "prenait", "prendrai", "pris", "prise", "prises",
                    "comprendre", "comprends", "comprend", "comprenons", "comprenez", "comprennent", "compris", "comprise", "comprises",
                    "jouer", "joue", "joues", "jouons", "jouez", "jouent", "jouais", "jouait", "joué", "jouée", "joués", "jouées",
                    "manger", "mange", "manges", "mangeons", "mangez", "mangent", "mangeais", "mangeait", "mangé", "mangée", "mangés", "mangées",
                    "partir", "pars", "part", "partons", "partez", "partent", "partais", "partait", "parti", "partie", "partis", "parties",
                    "travailler", "travaille", "travailles", "travaillons", "travaillez", "travaillent", "travaillais", "travaillé", "travaillée",
                    "acheter", "achète", "achètes", "achetons", "achetez", "achètent", "acheté", "achetée", "achetés", "achetées",
                    "parler", "parle", "parles", "parlons", "parlez", "parlent", "parlé", "parlée", "parlés", "parlées",
                    "écrire", "écris", "écrit", "écrivons", "écrivez", "écrivent", "écrite", "écrits", "écrites",
                    "lire", "lis", "lit", "lisons", "lisez", "lisent", "lu", "lue", "lus", "lues",
                    "dire", "dis", "dit", "disons", "dites", "disent", "dite", "dits",
                    "voir", "vois", "voit", "voyons", "voyez", "voient", "vu", "vue", "vus", "vues",
                    "croire", "crois", "croit", "croyons", "croyez", "croient", "cru", "crue", "crus",
                    "finir", "finis", "finit", "finissons", "finissez", "finissent", "fini", "finie", "finis", "finies",
                    "choisir", "choisis", "choisit", "choisissons", "choisissez", "choisissent", "choisi", "choisie", "choisis", "choisies",
                    "réussir", "réussis", "réussit", "réussissons", "réussissez", "réussissent", "réussi", "réussie", "réussis", "réussies",
                    "arriver", "arrive", "arrives", "arrivons", "arrivez", "arrivent", "arrivé", "arrivée", "arrivés", "arrivées",
                    "aimer", "aime", "aimes", "aimons", "aimez", "aiment", "aimé", "aimée", "aimés", "aimées",
                    "donner", "donne", "donnes", "donnons", "donnez", "donnent", "donné", "donnée", "donnés", "données",
                    "penser", "pense", "penses", "pensons", "pensez", "pensent", "pensé",
                    "trouver", "trouve", "trouves", "trouvons", "trouvez", "trouvent", "trouvé", "trouvée", "trouvés", "trouvées",
                    "demander", "demande", "demandes", "demandons", "demandez", "demandent", "demandé", "demandée",
                    "progresser", "progresse", "progresses", "progressons", "progressez", "progressent", "progressé",
                    "corriger", "corrige", "corriges", "corrigeons", "corrigez", "corrigent", "corrigé", "corrigée", "corrigés", "corrigées",
                    "envoyer", "envoie", "envoies", "envoyons", "envoyez", "envoient", "envoyé", "envoyée", "envoyés", "envoyées",
                    "recevoir", "reçois", "reçoit", "recevons", "recevez", "reçoivent", "reçu", "reçue", "reçus", "reçues",
                    "mettre", "mets", "met", "mettons", "mettez", "mettent", "mis", "mise", "mises",
                    "connaître", "connais", "connaît", "connaissons", "connaissez", "connaissent", "connu", "connue", "connus", "connues",
                    "suivre", "suis", "suit", "suivons", "suivez", "suivent", "suivi", "suivie", "suivis", "suivies",
                    "vivre", "vis", "vit", "vivons", "vivez", "vivent", "vécu", "vécue",
                    "tenir", "tiens", "tient", "tenons", "tenez", "tiennent", "tenu", "tenue", "tenus", "tenues",
                    "ouvrir", "ouvre", "ouvres", "ouvrons", "ouvrez", "ouvrent", "ouvert", "ouverte", "ouverts", "ouvertes",
                    "préparer", "prépare", "prépares", "préparons", "préparez", "préparent", "préparé", "préparée",
                    "habiter", "habite", "habites", "habitons", "habitez", "habitent", "habité",
                    "tromper", "trompe", "trompes", "trompons", "trompez", "trompent", "trompé", "trompée",
                    "ranger", "range", "ranges", "rangeons", "rangez", "rangent", "rangé", "rangée",
                    "courir", "cours", "court", "courons", "courez", "courent", "couru",
                    "appeler", "appelle", "appelles", "appelons", "appelez", "appellent", "appelé", "appelée"
                )
            )
            // Ajouter les formes correctes des misspellings
            commonMisspellings.values.forEach { (correct, _) ->
                if (!correct.contains(" ")) add(correct)
            }
        }
    }

    private val accentlessIndex: Map<String, List<String>> by lazy {
        knownValidWords.groupBy { FrenchNormalizer.stripAccents(it) }
    }

    fun isKnownWord(wordLower: String): Boolean {
        return wordLower in knownValidWords
    }

    fun findAccentMatch(wordLower: String): String? {
        val stripped = FrenchNormalizer.stripAccents(wordLower)
        val candidates = accentlessIndex[stripped] ?: return null
        if (wordLower in candidates) return null
        return candidates.firstOrNull()
    }

    fun findClosestSuggestions(wordLower: String, maxDistance: Int = 2, limit: Int = 3): List<String> {
        if (wordLower.length < 3) return emptyList()
        val stripped = FrenchNormalizer.stripAccents(wordLower)
        val scored = mutableListOf<Pair<String, Int>>()

        for (candidate in knownValidWords) {
            if (kotlin.math.abs(candidate.length - wordLower.length) > maxDistance) continue
            val d = damerauLevenshtein(stripped, FrenchNormalizer.stripAccents(candidate))
            if (d in 1..maxDistance) {
                scored += candidate to d
            }
        }
        return scored
            .sortedWith(compareBy<Pair<String, Int>> { it.second }.thenBy { it.first.length })
            .map { it.first }
            .distinct()
            .take(limit)
    }

    fun damerauLevenshtein(a: String, b: String): Int {
        val lenA = a.length
        val lenB = b.length
        if (lenA == 0) return lenB
        if (lenB == 0) return lenA

        val dp = Array(lenA + 1) { IntArray(lenB + 1) }
        for (i in 0..lenA) dp[i][0] = i
        for (j in 0..lenB) dp[0][j] = j

        for (i in 1..lenA) {
            for (j in 1..lenB) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                var best = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                    best = min(best, dp[i - 2][j - 2] + 1)
                }
                dp[i][j] = best
            }
        }
        return dp[lenA][lenB]
    }
}
