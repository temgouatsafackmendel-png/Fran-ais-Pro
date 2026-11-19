package com.example.learning.content

import com.example.core.database.Achievement
import com.example.core.database.DictionaryEntry
import com.example.linguistics.model.FrenchNormalizer

object DictionarySeed {

    private fun entry(
        id: String,
        word: String,
        definition: String,
        pos: String,
        gender: String,
        plural: String,
        pronunciation: String,
        synonyms: List<String>,
        antonyms: List<String>,
        examples: List<String>,
        expressions: List<String>,
        level: String
    ) = DictionaryEntry(
        id = id,
        word = word,
        normalizedWord = FrenchNormalizer.stripAccents(word),
        definition = definition,
        partOfSpeech = pos,
        gender = gender,
        pluralForm = plural,
        pronunciation = pronunciation,
        synonymsPipe = synonyms.joinToString("|"),
        antonymsPipe = antonyms.joinToString("|"),
        examplesPipe = examples.joinToString("|"),
        expressionsPipe = expressions.joinToString("|"),
        level = level
    )

    fun entries(): List<DictionaryEntry> = listOf(
        entry(
            "dic_01", "Accord", "Conformité grammaticale en genre, en nombre ou en personne entre deux mots liés dans la phrase ; entente entre personnes.",
            "Nom", "Masculin", "Accords", "/a.kɔʁ/",
            listOf("harmonie", "entente", "concordance"), listOf("désaccord", "divergence"),
            listOf("L'accord du participe passé obéit à des règles précises.", "Nous sommes parvenus à un accord durable."),
            listOf("D'accord", "D'un commun accord", "En accord avec"), "A2"
        ),
        entry(
            "dic_02", "Apprendre", "Acquérir une connaissance ou un savoir-faire par l'étude et la pratique ; également enseigner ou informer.",
            "Verbe", "Variable", "— (3e groupe)", "/a.pʁɑ̃dʁ/",
            listOf("étudier", "assimiler", "s'instruire"), listOf("oublier", "ignorer"),
            listOf("Les étudiants apprennent le français.", "J'ai appris une excellente nouvelle."),
            listOf("Apprendre par cœur", "Apprendre sur le tas"), "A1"
        ),
        entry(
            "dic_03", "Bienveillance", "Disposition d'esprit inclinant à la compréhension, à l'indulgence et au souci du bien d'autrui.",
            "Nom", "Féminin", "Bienveillances", "/bjɛ̃.vɛ.jɑ̃s/",
            listOf("bonté", "écoute", "empathie"), listOf("hostilité", "malveillance"),
            listOf("Un retour formulé avec bienveillance aide réellement à progresser."),
            listOf("Avec bienveillance", "Regard bienveillant"), "B2"
        ),
        entry(
            "dic_04", "Clarté", "Qualité de ce qui est limpide, intelligible et aisé à comprendre, tant dans le style que dans la lumière.",
            "Nom", "Féminin", "Clartés", "/klaʁ.te/",
            listOf("limpidité", "netteté", "précision"), listOf("obscurité", "confusion", "opacité"),
            listOf("La clarté de son exposé a convaincu toute l'assemblée."),
            listOf("Par souci de clarté", "En toute clarté"), "B1"
        ),
        entry(
            "dic_05", "Concis", "Qui exprime beaucoup d'idées en peu de mots, sans digression superflue.",
            "Adjectif", "Masculin (fém. concise)", "Concis / Concises", "/kɔ̃.si/",
            listOf("bref", "synthétique", "ramassé"), listOf("prolixe", "verbeux", "bavard"),
            listOf("Rédigez un résumé concis avant d'envoyer le dossier."),
            listOf("Style concis", "Réponse concise"), "B2"
        ),
        entry(
            "dic_06", "Conjoncture", "Ensemble des conditions économiques, sociales ou politiques caractérisant une période donnée.",
            "Nom", "Féminin", "Conjonctures", "/kɔ̃.ʒɔ̃k.tyʁ/",
            listOf("contexte", "situation", "climat"), listOf("constance"),
            listOf("La conjoncture favorable encourage les recrutements."),
            listOf("Dans la conjoncture actuelle", "Conjoncture économique"), "C1"
        ),
        entry(
            "dic_07", "Corroborer", "Apporter un nouvel élément qui confirme ou renforce une preuve ou une hypothèse.",
            "Verbe", "Variable", "— (1er groupe)", "/kɔ.ʁɔ.bɔ.ʁe/",
            listOf("confirmer", "étayer", "conforter"), listOf("infirmer", "réfuter", "démentir"),
            listOf("Ces observations corroborent parfaitement les analyses précédentes."),
            listOf("Corroborer un témoignage", "Corroborer une thèse"), "C2"
        ),
        entry(
            "dic_08", "Cour", "Espace découvert entouré de murs ou de bâtiments ; également assemblée judiciaire ou entourage souverain.",
            "Nom", "Féminin", "Cours", "/kuʁ/",
            listOf("préau", "esplanade", "tribunal"), listOf("intérieur"),
            listOf("Les enfants jouent dans la cour.", "La cour d'appel a rendu sa décision."),
            listOf("Cour de récréation", "Faire la cour"), "A1"
        ),
        entry(
            "dic_09", "Éloquence", "Art de bien parler, de convaincre et d'émouvoir par la justesse du discours.",
            "Nom", "Féminin", "Éloquences", "/e.lɔ.kɑ̃s/",
            listOf("aisance oratoire", "rhétorique", "force de persuasion"), listOf("maladresse", "balbutiement"),
            listOf("Son éloquence naturelle captive immédiatement le public."),
            listOf("Avec éloquence", "Silence éloquent"), "B2"
        ),
        entry(
            "dic_10", "Entreprise", "Organisation économique produisant des biens ou des services ; projet ou initiative d'envergure.",
            "Nom", "Féminin", "Entreprises", "/ɑ̃.tʁə.pʁiz/",
            listOf("société", "firme", "projet", "initiative"), listOf("inaction"),
            listOf("Il travaille dans son entreprise depuis cinq ans."),
            listOf("Esprit d'entreprise", "Chef d'entreprise"), "A2"
        ),
        entry(
            "dic_11", "Exhaustif", "Qui épuise un sujet en couvrant tous ses aspects sans omission.",
            "Adjectif", "Masculin (fém. exhaustive)", "Exhaustifs / Exhaustives", "/ɛɡ.zos.tif/",
            listOf("complet", "intégral", "approfondi"), listOf("partiel", "lacunaire", "sommaire"),
            listOf("Ce rapport offre un panorama exhaustif des usages grammaticaux."),
            listOf("Liste non exhaustive", "Étude exhaustive"), "C1"
        ),
        entry(
            "dic_12", "Fleur", "Partie colorée et souvent parfumée d'une plante ; par métaphore, ce qu'il y a de meilleur.",
            "Nom", "Féminin", "Fleurs", "/flœʁ/",
            listOf("corolle", "bouquet", "fleuron"), listOf("épine"),
            listOf("Les fleurs que j'ai achetées décorent la bibliothèque."),
            listOf("À fleur de peau", "Dans la fleur de l'âge", "Faire une fleur"), "A1"
        ),
        entry(
            "dic_13", "Homophone", "Mot qui se prononce de la même façon qu'un autre mot mais s'écrit différemment et a un sens différent.",
            "Nom", "Masculin", "Homophones", "/ɔ.mɔ.fɔn/",
            listOf("homonyme phonétique"), listOf("antonyme"),
            listOf("« Vert », « verre » et « vers » sont des homophones."),
            listOf("Homophone grammatical", "Homophone lexical"), "A2"
        ),
        entry(
            "dic_14", "Maîtrise", "Connaissance approfondie et domination parfaite d'un art, d'une langue ou de soi-même.",
            "Nom", "Féminin", "Maîtrises", "/mɛ.tʁiz/",
            listOf("expertise", "habileté", "contrôle", "virtuosité"), listOf("maladresse", "méconnaissance"),
            listOf("La maîtrise de l'orthographe s'acquiert par la lecture et la pratique."),
            listOf("Maîtrise de soi", "Coup de maître"), "B1"
        ),
        entry(
            "dic_15", "Néanmoins", "Adverbe de liaison marquant une restriction ou une opposition atténuée.",
            "Adverbe", "Invariable", "Invariable", "/ne.ɑ̃.mwɛ̃/",
            listOf("cependant", "toutefois", "pourtant", "malgré tout"), listOf("donc", "par conséquent"),
            listOf("L'exercice était exigeant ; néanmoins, vous l'avez réussi."),
            listOf("Et néanmoins"), "B1"
        ),
        entry(
            "dic_16", "Nuance", "Différence légère et délicate dans le sens d'un mot, d'une idée ou d'une teinte.",
            "Nom", "Féminin", "Nuances", "/ny.ɑ̃s/",
            listOf("subtilité", "finesse", "distinction"), listOf("simplisme", "opposition nette"),
            listOf("La langue française excelle dans l'expression des nuances."),
            listOf("Tout en nuances", "Sans nuance"), "B1"
        ),
        entry(
            "dic_17", "Pallier", "Compenser ou atténuer temporairement un défaut, un manque ou un inconvénient (transitif direct).",
            "Verbe", "Variable", "— (1er groupe)", "/pa.lje/",
            listOf("compenser", "atténuer", "remédier à"), listOf("aggraver", "empirer"),
            listOf("Ce dictionnaire embarqué permet de pallier l'absence de réseau."),
            listOf("Pallier une carence", "Pallier un manque"), "C1"
        ),
        entry(
            "dic_18", "Perspicace", "Doué d'une vive intelligence d'observation, qui comprend vite ce qui est caché ou complexe.",
            "Adjectif", "Épicène (masc. et fém.)", "Perspicaces", "/pɛʁs.pi.kas/",
            listOf("clairvoyant", "lucide", "sagace"), listOf("naïf", "crédule"),
            listOf("Sa remarque perspicace a éclairé toute la discussion."),
            listOf("Esprit perspicace", "Analyse perspicace"), "B2"
        ),
        entry(
            "dic_19", "Pléonasme", "Expression comportant des termes redondants qui répètent inutilement la même idée.",
            "Nom", "Masculin", "Pléonasmes", "/ple.ɔ.nasm/",
            listOf("redondance", "répétition superflue"), listOf("ellipse", "concision"),
            listOf("« Monter en haut » et « au jour d'aujourd'hui » sont des pléonasmes."),
            listOf("Éviter les pléonasmes"), "B2"
        ),
        entry(
            "dic_20", "Pragmatique", "Qui privilégie l'efficacité concrète et l'adaptation aux réalités pratiques.",
            "Adjectif", "Épicène", "Pragmatiques", "/pʁaɡ.ma.tik/",
            listOf("réaliste", "concret", "pratique"), listOf("théorique", "utopique"),
            listOf("Elle a adopté une approche pragmatique pour réviser sa grammaire."),
            listOf("Esprit pragmatique", "Solution pragmatique"), "C1"
        ),
        entry(
            "dic_21", "Progresser", "Avancer graduellement vers un meilleur niveau de connaissance ou de qualité.",
            "Verbe", "Variable", "— (1er groupe)", "/pʁɔ.ɡʁɛ.se/",
            listOf("évoluer", "s'améliorer", "avancer"), listOf("régresser", "stagner"),
            listOf("En pratiquant chaque jour, vous progressez durablement."),
            listOf("Progresser à grands pas"), "A2"
        ),
        entry(
            "dic_22", "Rigueur", "Exactitude scrupuleuse dans le raisonnement, le travail ou l'application des règles.",
            "Nom", "Féminin", "Rigueurs", "/ʁi.ɡœʁ/",
            listOf("précision", "sérieux", "méthode", "exactitude"), listOf("négligence", "laxisme", "à-peu-près"),
            listOf("La rigueur grammaticale renforce la force de vos arguments."),
            listOf("De rigueur", "En toute rigueur"), "B2"
        ),
        entry(
            "dic_23", "Subjonctif", "Mode verbal servant à exprimer un souhait, un doute, un sentiment ou une obligation.",
            "Nom", "Masculin", "Subjonctifs", "/syb.ʒɔ̃k.tif/",
            listOf("mode du virtuel"), listOf("indicatif"),
            listOf("Après « il faut que », on emploie toujours le subjonctif."),
            listOf("Subjonctif présent", "Subjonctif passé"), "B1"
        ),
        entry(
            "dic_24", "Syntaxe", "Ensemble des règles qui gouvernent l'ordre des mots et la construction des phrases.",
            "Nom", "Féminin", "Syntaxes", "/sɛ̃.taks/",
            listOf("construction", "agencement", "structure phrastique"), listOf("incohérence"),
            listOf("Une syntaxe maîtrisée rend un texte fluide et agréable à lire."),
            listOf("Erreur de syntaxe", "Analyse syntaxique"), "B1"
        ),
        entry(
            "dic_25", "Harmonie", "Accord juste et équilibré entre les parties d'un ensemble, des sons ou des idées.",
            "Nom", "Féminin", "Harmonies", "/aʁ.mɔ.ni/",
            listOf("accord", "équilibre", "entente", "cohérence"), listOf("désaccord", "dissonance", "conflit"),
            listOf("L'harmonie des temps verbaux donne de la fluidité au récit."),
            listOf("En harmonie avec", "Vivre en harmonie"), "B1"
        ),
        entry(
            "dic_26", "Désaccord", "Absence d'entente ou divergence d'opinion entre deux personnes ou deux idées.",
            "Nom", "Masculin", "Désaccords", "/de.za.kɔʁ/",
            listOf("divergence", "différend", "conflit"), listOf("accord", "harmonie", "entente"),
            listOf("Ils ont exprimé leur désaccord avec calme et courtoisie."),
            listOf("Être en désaccord", "Point de désaccord"), "A2"
        ),
        entry(
            "dic_27", "Précision", "Caractère de ce qui est exact, rigoureux et nettement délimité.",
            "Nom", "Féminin", "Précisions", "/pʁe.si.zjɔ̃/",
            listOf("exactitude", "rigueur", "clarté", "netteté"), listOf("flou", "imprécision", "à-peu-près"),
            listOf("La précision du vocabulaire évite toute ambiguïté."),
            listOf("Avec précision", "Apporter une précision"), "B1"
        ),
        entry(
            "dic_28", "Limpidité", "Transparence parfaite d'un liquide ou clarté remarquable d'un raisonnement.",
            "Nom", "Féminin", "Limpidités", "/lɛ̃.pi.di.te/",
            listOf("clarté", "transparence", "netteté"), listOf("opacité", "obscurité", "confusion"),
            listOf("La limpidité de ses explications rend la grammaire accessible."),
            listOf("D'une limpidité exemplaire"), "B2"
        ),
        entry(
            "dic_29", "Confirmer", "Rendre plus certain par une preuve nouvelle ou valider une information déjà annoncée.",
            "Verbe", "Variable", "— (1er groupe)", "/kɔ̃.fiʁ.me/",
            listOf("corroborer", "valider", "attester", "certifier"), listOf("infirmer", "démentir", "réfuter"),
            listOf("Les résultats confirment votre progression en orthographe."),
            listOf("Confirmer la règle", "Confirmer un rendez-vous"), "A2"
        ),
        entry(
            "dic_30", "Réfuter", "Démontrer la fausseté d'une affirmation ou d'une thèse par des arguments solides.",
            "Verbe", "Variable", "— (1er groupe)", "/ʁe.fy.te/",
            listOf("contester", "infirmer", "démentir"), listOf("confirmer", "corroborer", "approuver"),
            listOf("L'orateur a su réfuter chaque objection avec méthode."),
            listOf("Réfuter un argument", "Réfuter une accusation"), "C1"
        ),
        entry(
            "dic_31", "Bref", "Qui dure peu de temps ou qui s'exprime en peu de mots.",
            "Adjectif", "Masculin (fém. brève)", "Brefs / Brèves", "/bʁɛf/",
            listOf("concis", "court", "succinct", "rapide"), listOf("long", "verbeux", "prolixe"),
            listOf("Un bref rappel des règles suffit avant l'exercice."),
            listOf("En bref", "Pour faire bref"), "A2"
        ),
        entry(
            "dic_32", "Verbeux", "Qui emploie trop de mots inutiles pour exprimer une idée simple.",
            "Adjectif", "Masculin (fém. verbeuse)", "Verbeux / Verbeuses", "/vɛʁ.bø/",
            listOf("prolixe", "bavard", "redondant"), listOf("concis", "bref", "synthétique"),
            listOf("Évitez les tournures verbeuses dans une lettre professionnelle."),
            listOf("Style verbeux", "Discours verbeux"), "C1"
        ),
        entry(
            "dic_33", "Complet", "Qui renferme toutes ses parties, auquel il ne manque aucun élément.",
            "Adjectif", "Masculin (fém. complète)", "Complets / Complètes", "/kɔ̃.plɛ/",
            listOf("exhaustif", "intégral", "entier", "total"), listOf("partiel", "incomplet", "lacunaire"),
            listOf("Ce guide complet fonctionne entièrement sans connexion."),
            listOf("À titre complet", "Dossier complet"), "A1"
        ),
        entry(
            "dic_34", "Lacunaire", "Qui comporte des manques, des omissions ou des insuffisances notables.",
            "Adjectif", "Épicène", "Lacunaires", "/la.ky.nɛʁ/",
            listOf("incomplet", "partiel", "sommaire"), listOf("complet", "exhaustif", "approfondi"),
            listOf("Une révision lacunaire laisse subsister des hésitations sur les accords."),
            listOf("Documentation lacunaire", "Savoir lacunaire"), "C1"
        ),
        entry(
            "dic_35", "Clairvoyant", "Qui discerne avec justesse et finesse la réalité ou l'évolution d'une situation.",
            "Adjectif", "Masculin (fém. clairvoyante)", "Clairvoyants / Clairvoyantes", "/klɛʁ.vwa.jɑ̃/",
            listOf("perspicace", "lucide", "avisé", "sagace"), listOf("aveugle", "naïf", "imprévoyant"),
            listOf("Son jugement clairvoyant a permis d'anticiper la difficulté."),
            listOf("Esprit clairvoyant"), "B2"
        ),
        entry(
            "dic_36", "Naïf", "Qui fait preuve d'une confiance excessive ou d'une simplicité ingénue.",
            "Adjectif", "Masculin (fém. naïve)", "Naïfs / Naïves", "/na.if/",
            listOf("crédule", "ingénu", "candide"), listOf("perspicace", "clairvoyant", "méfiant"),
            listOf("Il serait naïf de croire qu'on maîtrise une langue sans pratique."),
            listOf("Question naïve", "Regard naïf"), "B1"
        ),
        entry(
            "dic_37", "Réaliste", "Qui tient compte des faits concrets et des limites réelles avant d'agir.",
            "Adjectif", "Épicène", "Réalistes", "/ʁe.a.list/",
            listOf("pragmatique", "concret", "lucide"), listOf("utopique", "irréaliste", "chimérique"),
            listOf("Fixez-vous un objectif quotidien réaliste de quinze minutes."),
            listOf("Approche réaliste", "Objectif réaliste"), "B1"
        ),
        entry(
            "dic_38", "Utopique", "Qui relève d'un idéal séduisant mais difficilement réalisable dans les faits.",
            "Adjectif", "Épicène", "Utopiques", "/y.tɔ.pik/",
            listOf("irréaliste", "chimérique", "idéaliste"), listOf("réaliste", "pragmatique", "concret"),
            listOf("Apprendre mille mots en une soirée paraît utopique."),
            listOf("Projet utopique", "Vision utopique"), "B2"
        ),
        entry(
            "dic_39", "Évoluer", "Se transformer progressivement, souvent vers un état plus élaboré ou plus performant.",
            "Verbe", "Variable", "— (1er groupe)", "/e.vɔ.lɥe/",
            listOf("progresser", "avancer", "se développer"), listOf("stagner", "régresser", "reculer"),
            listOf("Votre niveau évolue au fil des exercices et des dictées."),
            listOf("Faire évoluer", "Évoluer favorablement"), "B1"
        ),
        entry(
            "dic_40", "Stagner", "Rester immobile, sans progression ni amélioration notable.",
            "Verbe", "Variable", "— (1er groupe)", "/staɡ.ne/",
            listOf("piétiner", "végéter", "s'enliser"), listOf("progresser", "évoluer", "avancer"),
            listOf("Les révisions espacées évitent de stagner dans l'acquisition du lexique."),
            listOf("Laisser stagner"), "B2"
        ),
        entry(
            "dic_41", "Subtilité", "Qualité de ce qui est fin, délicat et perceptible grâce à un esprit attentif.",
            "Nom", "Féminin", "Subtilités", "/syb.ti.li.te/",
            listOf("nuance", "finesse", "délicatesse"), listOf("lourdeur", "simplisme", "grossièreté"),
            listOf("Les subtilités de la ponctuation changent parfois tout le sens d'une phrase."),
            listOf("Saisir une subtilité", "Subtilité de langage"), "B2"
        ),
        entry(
            "dic_42", "Négligence", "Manque de soin, d'attention ou d'application dans l'exécution d'une tâche.",
            "Nom", "Féminin", "Négligences", "/ne.ɡli.ʒɑ̃s/",
            listOf("inattention", "laxisme", "étourderie"), listOf("rigueur", "soin", "vigilance", "précision"),
            listOf("Une simple négligence d'accent peut modifier le sens d'un mot."),
            listOf("Par négligence", "Faute de négligence"), "B1"
        ),
        entry(
            "dic_43", "Compenser", "Contrebalancer un manque ou un défaut par un élément équivalent.",
            "Verbe", "Variable", "— (1er groupe)", "/kɔ̃.pɑ̃.se/",
            listOf("pallier", "équilibrer", "atténuer"), listOf("aggraver", "accentuer", "empirer"),
            listOf("Sa régularité compense largement ses débuts hésitants."),
            listOf("Compenser une perte", "Compenser un manque"), "B1"
        ),
        entry(
            "dic_44", "Empirer", "Devenir pire, s'aggraver au fil du temps.",
            "Verbe", "Variable", "— (1er groupe)", "/ɑ̃.pi.ʁe/",
            listOf("s'aggraver", "dégénérer", "se détériorer"), listOf("s'améliorer", "atténuer", "pallier"),
            listOf("Sans relecture attentive, les fautes d'inattention risquent d'empirer."),
            listOf("Aller en empirant"), "B1"
        ),
        entry(
            "dic_45", "Assimiler", "Intégrer pleinement une connaissance au point de pouvoir l'utiliser naturellement.",
            "Verbe", "Variable", "— (1er groupe)", "/a.si.mi.le/",
            listOf("apprendre", "intégrer", "comprendre", "maîtriser"), listOf("oublier", "rejeter", "ignorer"),
            listOf("Les cartes SM-2 aident à assimiler durablement le vocabulaire."),
            listOf("Assimiler une règle", "Assimiler un concept"), "B2"
        ),
        entry(
            "dic_46", "Oublier", "Perdre le souvenir d'une information ou omettre de faire quelque chose.",
            "Verbe", "Variable", "— (1er groupe)", "/u.bli.je/",
            listOf("omettre", "négliger", "effacer"), listOf("apprendre", "retenir", "assimiler", "se souvenir"),
            listOf("N'oubliez pas d'accorder l'adjectif avec le nom qu'il qualifie."),
            listOf("Oublier de faire", "Se faire oublier"), "A1"
        ),
        entry(
            "dic_47", "Empathie", "Capacité de se mettre à la place d'autrui pour comprendre ses émotions et son point de vue.",
            "Nom", "Féminin", "Empathies", "/ɑ̃.pa.ti/",
            listOf("bienveillance", "compréhension", "écoute", "compassion"), listOf("indifférence", "hostilité", "froideur"),
            listOf("L'empathie améliore la qualité des échanges écrits comme oraux."),
            listOf("Faire preuve d'empathie", "Écoute empathique"), "B2"
        ),
        entry(
            "dic_48", "Toutefois", "Adverbe de liaison introduisant une réserve, une nuance ou une opposition modérée.",
            "Adverbe", "Invariable", "Invariable", "/tut.fwa/",
            listOf("néanmoins", "cependant", "pourtant", "malgré tout"), listOf("donc", "ainsi", "par conséquent"),
            listOf("Le texte est clair ; toutefois, deux accords restent à vérifier."),
            listOf("Et toutefois", "Si toutefois"), "B1"
        )
    )

    fun achievements(): List<Achievement> = listOf(
        Achievement("ach_first_correction", "Première plume", "Réaliser votre première analyse dans le correcteur hors ligne.", "edit", false, 0L),
        Achievement("ach_first_lesson", "Premiers pas à l'Académie", "Terminer votre première leçon complète.", "school", false, 0L),
        Achievement("ach_5_exercises", "Esprit rigoureux", "Réussir 5 exercices sans relâche.", "check_circle", false, 0L),
        Achievement("ach_first_dictation", "Maître de la dictée", "Compléter votre première dictée locale.", "mic", false, 0L),
        Achievement("ach_vocab_master", "Lexique vivant", "Réviser au moins 5 mots avec la répétition espacée SM-2.", "book", false, 0L),
        Achievement("ach_streak_3", "Régularité exemplaire", "Maintenir une série active de 3 jours consécutifs.", "local_fire_department", false, 0L)
    )
}
