package com.example.linguistics.grammar

import com.example.linguistics.lexicon.FrenchLexicon
import com.example.linguistics.lexicon.Gender
import com.example.linguistics.model.CorrectionCategory
import com.example.linguistics.model.CorrectionIssue
import com.example.linguistics.model.CorrectionRuleModule
import com.example.linguistics.model.FrenchNormalizer
import com.example.linguistics.model.FrenchToken
import com.example.linguistics.model.TokenType

/**
 * Moteur de règles grammaticales, morphosyntaxiques, homophones et conjugaison
 * adapté au français. Conçu pour minimiser les faux positifs sur les phrases justes
 * (y compris ambiguës ou soutenues).
 */
class FrenchGrammarRules(
    private val lexicon: FrenchLexicon
) : CorrectionRuleModule {

    override val moduleId: String = "grammar_conjugation_v1"

    private val pluralDeterminers = setOf("les", "des", "ces", "ses", "mes", "tes", "nos", "vos", "leurs", "plusieurs", "quelques")
    private val mascSingularDeterminers = setOf("le", "un", "ce", "mon", "ton", "son")
    private val femSingularDeterminers = setOf("la", "une", "cette", "ma", "ta", "sa")

    private val pastParticiples = setOf(
        "mangé", "parlé", "travaillé", "joué", "acheté", "donné", "trouvé", "préparé", "corrigé",
        "envoyé", "demandé", "aimé", "étudié", "terminé", "commencé", "réussi", "fini", "choisi",
        "pris", "appris", "compris", "fait", "dit", "écrit", "lu", "vu", "su", "pu", "voulu", "dû",
        "mis", "reçu", "connu", "vécu", "ouvert", "tenu", "eu", "été",
        "allé", "allée", "allés", "allées", "venu", "venue", "venus", "venues",
        "parti", "partie", "partis", "parties", "arrivé", "arrivée", "arrivés", "arrivées",
        "sorti", "sortie", "sortis", "sorties", "entré", "entrée", "entrés", "entrées",
        "resté", "restée", "restés", "restées", "tombé", "tombée", "tombés", "tombées"
    )

    private val erInfinitives = setOf(
        "manger", "parler", "travailler", "jouer", "acheter", "donner", "trouver", "préparer",
        "corriger", "envoyer", "demander", "aimer", "étudier", "terminer", "commencer", "arriver",
        "rester", "tomber", "entrer", "penser", "regarder", "écouter", "améliorer", "réviser", "progresser", "aller"
    )

    private val infinitiveTriggers = setOf(
        "vais", "vas", "va", "allons", "allez", "vont",
        "peux", "peut", "pouvons", "pouvez", "peuvent",
        "dois", "doit", "devons", "devez", "doivent",
        "veux", "veut", "voulons", "voulez", "veulent",
        "sais", "sait", "faut", "pour", "sans", "afin", "avant"
    )

    private val singularToPluralVerb3rd = mapOf(
        "apprend" to "apprennent",
        "comprend" to "comprennent",
        "prend" to "prennent",
        "joue" to "jouent",
        "mange" to "mangent",
        "travaille" to "travaillent",
        "parle" to "parlent",
        "aime" to "aiment",
        "donne" to "donnent",
        "pense" to "pensent",
        "trouve" to "trouvent",
        "arrive" to "arrivent",
        "progresse" to "progressent",
        "corrige" to "corrigent",
        "étudie" to "étudient",
        "est" to "sont",
        "a" to "ont",
        "va" to "vont",
        "fait" to "font",
        "vient" to "viennent",
        "part" to "partent",
        "finit" to "finissent",
        "réussit" to "réussissent",
        "choisit" to "choisissent",
        "écrit" to "écrivent",
        "lit" to "lisent",
        "voit" to "voient",
        "sait" to "savent",
        "peut" to "peuvent",
        "veut" to "veulent",
        "doit" to "doivent"
    )

    private val pluralToSingularVerb3rd = singularToPluralVerb3rd.entries.associate { (s, p) -> p to s }

    private val subjunctiveRequiredMap = mapOf(
        "viens" to "viennes",
        "vient" to "vienne",
        "fais" to "fasses",
        "fait" to "fasse",
        "vas" to "ailles",
        "va" to "aille",
        "es" to "sois",
        "est" to "soit",
        "as" to "aies",
        "a" to "ait",
        "peux" to "puisses",
        "peut" to "puisse",
        "sais" to "saches",
        "sait" to "sache",
        "allons" to "allions",
        "allez" to "alliez",
        "avons" to "ayons",
        "avez" to "ayez",
        "sommes" to "soyons",
        "êtes" to "soyez"
    )

    private val etreParticipleBase = mapOf(
        "parti" to "parti",
        "venu" to "venu",
        "allé" to "allé",
        "arrivé" to "arrivé",
        "entré" to "entré",
        "sorti" to "sorti",
        "resté" to "resté",
        "tombé" to "tombé"
    )

    override fun analyze(text: String, tokens: List<FrenchToken>): List<CorrectionIssue> {
        val issues = mutableListOf<CorrectionIssue>()
        val sentences = tokens.groupBy { it.sentenceIndex }.values

        for (sentence in sentences) {
            checkElisions(sentence, issues)
            checkHomophones(sentence, issues)
            checkDeterminerNounAndAdjectiveAgreement(sentence, issues)
            checkSubjectVerbAgreement(sentence, issues)
            checkInfinitiveVsParticiple(sentence, issues)
            checkPastParticipleAgreement(sentence, issues)
            checkSubjunctiveAndSiConditional(sentence, issues)
        }
        return issues
    }

    private fun checkElisions(sentence: List<FrenchToken>, issues: MutableList<CorrectionIssue>) {
        val vowelRegex = Regex("^[aeiouyàâäéèêëîïôöùûüœh].*")
        val hAspire = setOf("haut", "haute", "hauts", "hautes", "héros", "hasard", "honte", "haricot", "hibou", "handicap")

        for (i in 0 until sentence.size - 1) {
            val cur = sentence[i]
            val next = sentence[i + 1]
            if (cur.type != TokenType.WORD || next.type != TokenType.WORD) continue
            if (next.lower in hAspire) continue

            val startsVowel = vowelRegex.matches(next.lower)
            when {
                cur.lower == "si" && (next.lower == "il" || next.lower == "ils") -> {
                    val replacement = FrenchNormalizer.preserveCase(cur.text, "s'${next.lower}")
                    issues += CorrectionIssue(
                        id = "elision_${cur.start}_${next.end}",
                        originalFragment = "${cur.text} ${next.text}",
                        suggestion = replacement,
                        category = CorrectionCategory.ORTHOGRAPHE,
                        ruleCode = "ELISION_SI_IL",
                        explanation = "Devant « il » et « ils », la conjonction « si » s'élide obligatoirement en « s' ».",
                        example = "S'il vient, prévenez-moi.",
                        confidence = 0.99f,
                        startIndex = cur.start,
                        endIndex = next.end
                    )
                }
                cur.lower == "je" && startsVowel && next.lower in setOf("ai", "avais", "aime", "habite", "écris", "étudie", "arrive", "espère", "écoute") -> {
                    val replacement = FrenchNormalizer.preserveCase(cur.text, "j'${next.lower}")
                    issues += CorrectionIssue(
                        id = "elision_je_${cur.start}_${next.end}",
                        originalFragment = "${cur.text} ${next.text}",
                        suggestion = replacement,
                        category = CorrectionCategory.ORTHOGRAPHE,
                        ruleCode = "ELISION_JE",
                        explanation = "Le pronom « je » s'élide en « j' » devant une voyelle ou un h muet.",
                        example = "J'ai terminé mon travail.",
                        confidence = 0.98f,
                        startIndex = cur.start,
                        endIndex = next.end
                    )
                }
                cur.lower in setOf("le", "la") && startsVowel && (lexicon.nouns[next.lower]?.startsWithVowelOrMuteH == true) -> {
                    val replacement = FrenchNormalizer.preserveCase(cur.text, "l'${next.lower}")
                    issues += CorrectionIssue(
                        id = "elision_art_${cur.start}_${next.end}",
                        originalFragment = "${cur.text} ${next.text}",
                        suggestion = replacement,
                        category = CorrectionCategory.GRAMMAIRE,
                        ruleCode = "ELISION_ARTICLE",
                        explanation = "Devant un nom singulier commençant par une voyelle ou un h muet, l'article « ${cur.lower} » s'élide en « l' ».",
                        example = "L'école ouvre à huit heures.",
                        confidence = 0.97f,
                        startIndex = cur.start,
                        endIndex = next.end
                    )
                }
                cur.lower in setOf("ma", "ta", "sa") && startsVowel && (lexicon.nouns[next.lower]?.startsWithVowelOrMuteH == true) -> {
                    val mascForm = when (cur.lower) {
                        "ma" -> "mon"
                        "ta" -> "ton"
                        else -> "son"
                    }
                    val replacement = FrenchNormalizer.preserveCase(cur.text, mascForm)
                    issues += CorrectionIssue(
                        id = "euphonie_${cur.start}_${cur.end}",
                        originalFragment = cur.text,
                        suggestion = replacement,
                        category = CorrectionCategory.GRAMMAIRE,
                        ruleCode = "POSSESSIF_VOYELLE",
                        explanation = "Devant un nom féminin singulier commençant par une voyelle ou un h muet, on emploie « $mascForm » par euphonie.",
                        example = "Il travaille dans son entreprise. / Mon amie arrive.",
                        confidence = 0.98f,
                        startIndex = cur.start,
                        endIndex = cur.end
                    )
                }
                cur.lower == "que" && next.lower in setOf("il", "elle", "ils", "elles", "on") -> {
                    val replacement = FrenchNormalizer.preserveCase(cur.text, "qu'${next.lower}")
                    issues += CorrectionIssue(
                        id = "elision_que_${cur.start}_${next.end}",
                        originalFragment = "${cur.text} ${next.text}",
                        suggestion = replacement,
                        category = CorrectionCategory.ORTHOGRAPHE,
                        ruleCode = "ELISION_QUE",
                        explanation = "« Que » s'élide obligatoirement en « qu' » devant une voyelle.",
                        example = "Je pense qu'il a raison.",
                        confidence = 0.98f,
                        startIndex = cur.start,
                        endIndex = next.end
                    )
                }
            }
        }
    }

    private fun checkHomophones(sentence: List<FrenchToken>, issues: MutableList<CorrectionIssue>) {
        for (i in sentence.indices) {
            val cur = sentence[i]
            if (cur.type != TokenType.WORD) continue
            val prev = sentence.getOrNull(i - 1)
            val next = sentence.getOrNull(i + 1)

            when (cur.lower) {
                // a vs à
                "à" -> {
                    if (prev?.lower in setOf("il", "elle", "on", "qui") && next != null &&
                        (next.lower in pastParticiples || next.lower in setOf("un", "une", "des", "du", "de", "son", "sa", "ses", "leur", "beaucoup", "peu", "toujours", "déjà", "bien", "mal", "raison", "tort", "besoin"))
                    ) {
                        issues += CorrectionIssue(
                            id = "homo_a_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "a"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "HOMO_A_AVOIR",
                            explanation = "Ici « a » est le verbe « avoir » (remplaçable par « avait ») : il ne prend pas d'accent.",
                            example = "Elle a mangé. (Elle avait mangé.)",
                            confidence = 0.97f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
                "a" -> {
                    val prevIsVerbOrPrep = prev?.lower in setOf(
                        "va", "vais", "vas", "allons", "allez", "vont", "allé", "allée", "allés", "allées",
                        "travaille", "travaillent", "pense", "pensent", "parle", "parlent", "réussi", "réussir",
                        "commence", "commencé", "continue", "apprend", "apprennent", "habite", "jusqu'"
                    )
                    val nextIsPlaceOrInf = next != null && (
                        next.type == TokenType.ELISION_PREFIX ||
                            next.lower in setOf("la", "l'", "paris", "lyon", "pied", "temps", "midi", "droite", "gauche", "bientôt", "demain", "nouveau", "peine", "cause", "propos") ||
                            next.lower in erInfinitives
                        )
                    if (prevIsVerbOrPrep && nextIsPlaceOrInf) {
                        issues += CorrectionIssue(
                            id = "homo_aprep_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "à"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "HOMO_A_PREP",
                            explanation = "Il s'agit de la préposition « à » (on ne peut pas la remplacer par « avait »).",
                            example = "Il va à l'école. / À bientôt.",
                            confidence = 0.95f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
                // son vs sont
                "son" -> {
                    if (prev?.lower in setOf("ils", "elles") || (prev != null && prev.lower in lexicon.pluralToNoun && next != null && (next.lower in pastParticiples || next.lower.endsWith("s") || next.lower in setOf("très", "plus", "très", "grands", "grandes", "beaux", "belles", "prêts", "contents")))) {
                        issues += CorrectionIssue(
                            id = "homo_sont_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "sont"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "HOMO_SON_SONT",
                            explanation = "Ici « sont » est le verbe « être » au pluriel (remplaçable par « étaient »).",
                            example = "Ils sont partis tôt.",
                            confidence = 0.97f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
                "sont" -> {
                    val prevIsPrep = prev?.lower in setOf("dans", "avec", "pour", "sur", "sous", "chez", "de", "à", "par", "vers")
                    val nextIsSingularNoun = next != null && next.lower in lexicon.nouns
                    if (prevIsPrep && nextIsSingularNoun) {
                        issues += CorrectionIssue(
                            id = "homo_son_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "son"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "HOMO_SON_DET",
                            explanation = "Devant un nom singulier, utilisez le déterminant possessif « son » (remplaçable par « mon »).",
                            example = "Il travaille dans son entreprise.",
                            confidence = 0.97f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
                // on vs ont
                "on" -> {
                    if (prev?.lower in setOf("ils", "elles") && next != null) {
                        issues += CorrectionIssue(
                            id = "homo_ont_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "ont"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "HOMO_ON_ONT",
                            explanation = "Après « ${prev?.text} », employez le verbe avoir « ont » (remplaçable par « avaient »).",
                            example = "Ils ont réussi leur examen.",
                            confidence = 0.98f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
                // et vs est
                "et" -> {
                    val prevIsSubject = prev?.lower in setOf("il", "elle", "on", "ceci", "cela", "ça", "qui") ||
                        (prev != null && prev.lower in lexicon.nouns && sentence.getOrNull(i - 2)?.lower in setOf("le", "la", "un", "une", "ce", "cette", "mon", "ma", "ton", "ta", "son", "sa", "notre", "votre", "leur"))
                    val nextIsAdjOrPart = next != null && (
                        next.lower in pastParticiples ||
                            next.lower in setOf("grand", "grande", "petit", "petite", "beau", "belle", "bon", "bonne", "très", "trop", "vrai", "faux", "utile", "facile", "difficile", "important", "importante", "clair", "claire", "prêt", "prête")
                        )
                    // Ne pas signaler si suivi d'un nom coordonné (ex: "la maison et le jardin")
                    if (prevIsSubject && nextIsAdjOrPart && sentence.getOrNull(i + 2)?.lower !in setOf("est", "sont", "était", "a", "ont")) {
                        issues += CorrectionIssue(
                            id = "homo_est_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "est"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "HOMO_ET_EST",
                            explanation = "Ici « est » est le verbe « être » (remplaçable par « était »), et non la conjonction « et ».",
                            example = "Leur maison est grande.",
                            confidence = 0.92f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
                // ce vs se
                "ce" -> {
                    if (prev?.lower in setOf("il", "elle", "on", "ils", "elles", "qui") && next != null &&
                        next.lower !in setOf("que", "qui", "dont", "n'", "soir", "matin", "jour") &&
                        next.lower !in lexicon.nouns
                    ) {
                        issues += CorrectionIssue(
                            id = "homo_se_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "se"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "HOMO_CE_SE",
                            explanation = "Devant un verbe pronominal, on écrit le pronom réfléchi « se » (et non « ce »).",
                            example = "Il se prépare sérieusement.",
                            confidence = 0.94f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
                "se" -> {
                    if (next != null && next.lower in lexicon.nouns && lexicon.nouns[next.lower]?.gender == Gender.MASCULINE) {
                        issues += CorrectionIssue(
                            id = "homo_ce_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "ce"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "HOMO_SE_CE",
                            explanation = "Devant un nom masculin singulier, utilisez le déterminant démonstratif « ce ».",
                            example = "Ce livre est passionnant.",
                            confidence = 0.95f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
                // leur vs leurs devant nom
                "leurs" -> {
                    if (next != null && next.lower in lexicon.nouns) {
                        issues += CorrectionIssue(
                            id = "homo_leur_sg_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "leur"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "ACCORD_LEUR_SING",
                            explanation = "Devant un nom au singulier, le déterminant possessif « leur » ne prend pas de « s ».",
                            example = "Leur maison est grande.",
                            confidence = 0.97f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
                "leur" -> {
                    if (next != null && next.lower in lexicon.pluralToNoun && next.lower != "cours") {
                        issues += CorrectionIssue(
                            id = "homo_leur_pl_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "leurs"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "ACCORD_LEUR_PLUR",
                            explanation = "Devant un nom au pluriel, le déterminant possessif prend un « s » : « leurs ».",
                            example = "Leurs enfants jouent dans la cour.",
                            confidence = 0.96f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
                // ça vs sa
                "ça" -> {
                    if (next != null && next.lower in lexicon.nouns && lexicon.nouns[next.lower]?.gender == Gender.FEMININE) {
                        issues += CorrectionIssue(
                            id = "homo_sa_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "sa"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "HOMO_CA_SA",
                            explanation = "Devant un nom féminin, utilisez le déterminant possessif « sa » (et non le pronom « ça »).",
                            example = "Il range sa chambre.",
                            confidence = 0.96f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
                "sa" -> {
                    if (next?.lower in setOf("va", "marche", "suffit", "dépend", "semble", "paraît")) {
                        issues += CorrectionIssue(
                            id = "homo_ca_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "ça"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "HOMO_SA_CA",
                            explanation = "Devant un verbe, on utilise le pronom démonstratif « ça » (remplaçable par « cela »).",
                            example = "Ça va très bien.",
                            confidence = 0.97f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
                // ou vs où
                "ou" -> {
                    val isQuestionStart = i == 0 && sentence.lastOrNull()?.text == "?"
                    val prevIsPlace = prev?.lower in setOf("ville", "maison", "pays", "endroit", "lieu", "moment", "jour", "bureau", "école", "entreprise")
                    if (isQuestionStart || prevIsPlace) {
                        issues += CorrectionIssue(
                            id = "homo_ou_${cur.start}",
                            originalFragment = cur.text,
                            suggestion = FrenchNormalizer.preserveCase(cur.text, "où"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "HOMO_OU_LIEU",
                            explanation = "Pour exprimer le lieu ou le temps, on écrit « où » avec un accent grave (non remplaçable par « ou bien »).",
                            example = "La ville où j'habite. / Où vas-tu ?",
                            confidence = 0.94f,
                            startIndex = cur.start,
                            endIndex = cur.end
                        )
                    }
                }
            }
        }
    }

    private fun checkDeterminerNounAndAdjectiveAgreement(
        sentence: List<FrenchToken>,
        issues: MutableList<CorrectionIssue>
    ) {
        val adjFemToMasc = mapOf(
            "grande" to "grand", "petite" to "petit", "belle" to "beau", "bonne" to "bon",
            "nouvelle" to "nouveau", "longue" to "long", "courte" to "court", "heureuse" to "heureux",
            "intelligente" to "intelligent", "importante" to "important", "claire" to "clair"
        )
        val adjMascToFem = adjFemToMasc.entries.associate { (f, m) -> m to f }

        for (i in 0 until sentence.size - 1) {
            val det = sentence[i]
            val next = sentence[i + 1]
            if (det.type != TokenType.WORD || next.type != TokenType.WORD) continue

            // Déterminant pluriel + nom singulier (ex: "les enfant" -> "les enfants")
            if (det.lower in pluralDeterminers) {
                val noun = lexicon.nouns[next.lower]
                if (noun != null && noun.singular != noun.plural) {
                    issues += CorrectionIssue(
                        id = "agr_pl_${next.start}",
                        originalFragment = next.text,
                        suggestion = FrenchNormalizer.preserveCase(next.text, noun.plural),
                        category = CorrectionCategory.GRAMMAIRE,
                        ruleCode = "ACCORD_DET_NOM_PL",
                        explanation = "Après le déterminant pluriel « ${det.text} », le nom « ${next.text} » doit être au pluriel.",
                        example = "${det.text} ${noun.plural}",
                        confidence = 0.96f,
                        startIndex = next.start,
                        endIndex = next.end
                    )
                }
            }

            // Déterminant singulier masculin + nom féminin (ex: "un maison" -> "une maison")
            if (det.lower in mascSingularDeterminers) {
                val noun = lexicon.nouns[next.lower]
                if (noun != null && noun.gender == Gender.FEMININE && !noun.startsWithVowelOrMuteH) {
                    val femDet = when (det.lower) {
                        "le" -> "la"
                        "un" -> "une"
                        "ce" -> "cette"
                        "mon" -> "ma"
                        "ton" -> "ta"
                        "son" -> "sa"
                        else -> det.lower
                    }
                    issues += CorrectionIssue(
                        id = "agr_gen_f_${det.start}",
                        originalFragment = det.text,
                        suggestion = FrenchNormalizer.preserveCase(det.text, femDet),
                        category = CorrectionCategory.GRAMMAIRE,
                        ruleCode = "ACCORD_GENRE_DET",
                        explanation = "Le nom « ${noun.singular} » est féminin ; il requiert le déterminant « $femDet ».",
                        example = "$femDet ${noun.singular}",
                        confidence = 0.95f,
                        startIndex = det.start,
                        endIndex = det.end
                    )
                }
            }

            // Déterminant singulier féminin + nom masculin (ex: "une livre" est ambigu car "une livre" existe en poids/monnaie ! Éviter "livre")
            if (det.lower in femSingularDeterminers && next.lower != "livre" && next.lower != "tour" && next.lower != "poste" && next.lower != "mode") {
                val noun = lexicon.nouns[next.lower]
                if (noun != null && noun.gender == Gender.MASCULINE) {
                    val mascDet = when (det.lower) {
                        "la" -> "le"
                        "une" -> "un"
                        "cette" -> "ce"
                        "ma" -> "mon"
                        "ta" -> "ton"
                        "sa" -> "son"
                        else -> det.lower
                    }
                    issues += CorrectionIssue(
                        id = "agr_gen_m_${det.start}",
                        originalFragment = det.text,
                        suggestion = FrenchNormalizer.preserveCase(det.text, mascDet),
                        category = CorrectionCategory.GRAMMAIRE,
                        ruleCode = "ACCORD_GENRE_DET",
                        explanation = "Le nom « ${noun.singular} » est masculin ; il requiert le déterminant « $mascDet ».",
                        example = "$mascDet ${noun.singular}",
                        confidence = 0.95f,
                        startIndex = det.start,
                        endIndex = det.end
                    )
                }
            }

            // Accord nom singulier + adjectif épithète juste après (ex: "maison grand" -> "maison grande")
            val nounHere = lexicon.nouns[det.lower]
            if (nounHere != null) {
                if (nounHere.gender == Gender.FEMININE && next.lower in adjMascToFem) {
                    val femAdj = adjMascToFem.getValue(next.lower)
                    issues += CorrectionIssue(
                        id = "agr_adj_f_${next.start}",
                        originalFragment = next.text,
                        suggestion = FrenchNormalizer.preserveCase(next.text, femAdj),
                        category = CorrectionCategory.GRAMMAIRE,
                        ruleCode = "ACCORD_NOM_ADJ",
                        explanation = "L'adjectif s'accorde au féminin avec le nom « ${nounHere.singular} ».",
                        example = "Une ${nounHere.singular} $femAdj.",
                        confidence = 0.93f,
                        startIndex = next.start,
                        endIndex = next.end
                    )
                } else if (nounHere.gender == Gender.MASCULINE && next.lower in adjFemToMasc) {
                    val mascAdj = adjFemToMasc.getValue(next.lower)
                    issues += CorrectionIssue(
                        id = "agr_adj_m_${next.start}",
                        originalFragment = next.text,
                        suggestion = FrenchNormalizer.preserveCase(next.text, mascAdj),
                        category = CorrectionCategory.GRAMMAIRE,
                        ruleCode = "ACCORD_NOM_ADJ",
                        explanation = "L'adjectif s'accorde au masculin avec le nom « ${nounHere.singular} ».",
                        example = "Un ${nounHere.singular} $mascAdj.",
                        confidence = 0.93f,
                        startIndex = next.start,
                        endIndex = next.end
                    )
                }
            }
        }
    }

    private fun checkSubjectVerbAgreement(sentence: List<FrenchToken>, issues: MutableList<CorrectionIssue>) {
        val tuWrongToRight = mapOf(
            "va" to "vas", "fait" to "fais", "vient" to "viens", "peut" to "peux", "veut" to "veux",
            "doit" to "dois", "sait" to "sais", "est" to "es", "a" to "as", "parle" to "parles",
            "travaille" to "travailles", "joue" to "joues", "mange" to "manges", "apprend" to "apprends"
        )
        val jeWrongToRight = mapOf(
            "va" to "vais", "vas" to "vais", "vient" to "viens", "peut" to "peux", "veut" to "veux",
            "doit" to "dois", "sait" to "sais", "est" to "suis", "a" to "ai", "fait" to "fais"
        )

        for (i in 0 until sentence.size - 1) {
            val subj = sentence[i]
            val verb = sentence[i + 1]
            if (subj.type != TokenType.WORD || verb.type != TokenType.WORD) continue

            // Sujet pronom pluriel (ils / elles) + verbe 3e singulier
            if (subj.lower in setOf("ils", "elles") && verb.lower in singularToPluralVerb3rd) {
                val fixed = singularToPluralVerb3rd.getValue(verb.lower)
                issues += CorrectionIssue(
                    id = "sv_pl_${verb.start}",
                    originalFragment = verb.text,
                    suggestion = FrenchNormalizer.preserveCase(verb.text, fixed),
                    category = CorrectionCategory.CONJUGAISON,
                    ruleCode = "ACCORD_SUJET_VERBE",
                    explanation = "Avec le sujet pluriel « ${subj.text} », le verbe doit s'accorder à la 3e personne du pluriel.",
                    example = "${subj.text} $fixed.",
                    confidence = 0.98f,
                    startIndex = verb.start,
                    endIndex = verb.end
                )
            }

            // Groupe nominal pluriel (Les étudiants / Les enfants / Mes amis) + verbe 3e singulier
            val prevDet = sentence.getOrNull(i - 1)
            if (prevDet?.lower in pluralDeterminers && subj.lower in lexicon.pluralToNoun && verb.lower in singularToPluralVerb3rd) {
                val fixed = singularToPluralVerb3rd.getValue(verb.lower)
                issues += CorrectionIssue(
                    id = "sv_gn_pl_${verb.start}",
                    originalFragment = verb.text,
                    suggestion = FrenchNormalizer.preserveCase(verb.text, fixed),
                    category = CorrectionCategory.GRAMMAIRE,
                    ruleCode = "ACCORD_SUJET_VERBE_GN",
                    explanation = "Le sujet « ${prevDet?.text} ${subj.text} » est au pluriel : le verbe doit se mettre au pluriel (« $fixed »).",
                    example = "Les étudiants apprennent le français.",
                    confidence = 0.97f,
                    startIndex = verb.start,
                    endIndex = verb.end
                )
            }

            // Sujet pronom singulier (il / elle / on) + verbe 3e pluriel
            if (subj.lower in setOf("il", "elle", "on") && verb.lower in pluralToSingularVerb3rd) {
                val fixed = pluralToSingularVerb3rd.getValue(verb.lower)
                issues += CorrectionIssue(
                    id = "sv_sg_${verb.start}",
                    originalFragment = verb.text,
                    suggestion = FrenchNormalizer.preserveCase(verb.text, fixed),
                    category = CorrectionCategory.CONJUGAISON,
                    ruleCode = "ACCORD_SUJET_VERBE_SG",
                    explanation = "Avec le sujet singulier « ${subj.text} », le verbe doit s'accorder à la 3e personne du singulier.",
                    example = "${subj.text} $fixed.",
                    confidence = 0.98f,
                    startIndex = verb.start,
                    endIndex = verb.end
                )
            }

            // Tu + forme 3e singulier
            if (subj.lower == "tu" && verb.lower in tuWrongToRight) {
                val fixed = tuWrongToRight.getValue(verb.lower)
                issues += CorrectionIssue(
                    id = "sv_tu_${verb.start}",
                    originalFragment = verb.text,
                    suggestion = FrenchNormalizer.preserveCase(verb.text, fixed),
                    category = CorrectionCategory.CONJUGAISON,
                    ruleCode = "ACCORD_TU",
                    explanation = "Avec le pronom « tu », la terminaison du verbe s'accorde à la 2e personne du singulier.",
                    example = "Tu $fixed.",
                    confidence = 0.97f,
                    startIndex = verb.start,
                    endIndex = verb.end
                )
            }

            // Je + forme 3e singulier
            if (subj.lower == "je" && verb.lower in jeWrongToRight) {
                val fixed = jeWrongToRight.getValue(verb.lower)
                issues += CorrectionIssue(
                    id = "sv_je_${verb.start}",
                    originalFragment = verb.text,
                    suggestion = FrenchNormalizer.preserveCase(verb.text, fixed),
                    category = CorrectionCategory.CONJUGAISON,
                    ruleCode = "ACCORD_JE",
                    explanation = "Avec le pronom « je », utilisez la forme de la 1re personne du singulier (« $fixed »).",
                    example = "Je $fixed.",
                    confidence = 0.97f,
                    startIndex = verb.start,
                    endIndex = verb.end
                )
            }
        }
    }

    private fun checkInfinitiveVsParticiple(sentence: List<FrenchToken>, issues: MutableList<CorrectionIssue>) {
        val avoirAux = setOf("ai", "as", "a", "avons", "avez", "ont", "avais", "avait", "avions", "aviez", "avaient", "j'ai")

        for (i in 0 until sentence.size - 1) {
            val prev = sentence[i]
            val cur = sentence[i + 1]
            if (cur.type != TokenType.WORD) continue

            // Après un semi-auxiliaire ou préposition (vais, peux, dois, pour, sans) -> infinitif en -er requis
            if (prev.lower in infinitiveTriggers && cur.lower.endsWith("é") && cur.lower in pastParticiples) {
                val inf = cur.lower.dropLast(1) + "er"
                if (inf in erInfinitives) {
                    issues += CorrectionIssue(
                        id = "inf_er_${cur.start}",
                        originalFragment = cur.text,
                        suggestion = FrenchNormalizer.preserveCase(cur.text, inf),
                        category = CorrectionCategory.GRAMMAIRE,
                        ruleCode = "INFINITIF_ER",
                        explanation = "Après « ${prev.text} », le verbe doit être à l'infinitif (-er). Astuce : remplacez par « prendre » ou « vendre ».",
                        example = "Je vais travailler. (Je vais vendre.)",
                        confidence = 0.97f,
                        startIndex = cur.start,
                        endIndex = cur.end
                    )
                }
            }

            // Après auxiliaire avoir -> participe passé -é requis (ex: "Elle a manger" -> "Elle a mangé")
            if (prev.lower in avoirAux && cur.lower in erInfinitives) {
                val pp = cur.lower.dropLast(2) + "é"
                issues += CorrectionIssue(
                    id = "pp_avoir_${cur.start}",
                    originalFragment = cur.text,
                    suggestion = FrenchNormalizer.preserveCase(cur.text, pp),
                    category = CorrectionCategory.CONJUGAISON,
                    ruleCode = "PARTICIPE_PASSE_AVOIR",
                    explanation = "Après l'auxiliaire avoir (« ${prev.text} »), utilisez le participe passé en « -é » (remplaçable par « pris » ou « vendu »).",
                    example = "Elle a mangé. (Elle a pris.)",
                    confidence = 0.97f,
                    startIndex = cur.start,
                    endIndex = cur.end
                )
            }
        }
    }

    private fun checkPastParticipleAgreement(sentence: List<FrenchToken>, issues: MutableList<CorrectionIssue>) {
        // 1. Accord du participe passé avec être (Ils sont parti -> partis, Elle est allé -> allée)
        for (i in 0 until sentence.size - 2) {
            val subj = sentence[i]
            val aux = sentence[i + 1]
            val pp = sentence[i + 2]
            if (pp.type != TokenType.WORD) continue

            val base = etreParticipleBase[pp.lower]
            if (base != null) {
                when {
                    subj.lower == "ils" && aux.lower in setOf("sont", "étaient", "seront", "seraient") -> {
                        issues += CorrectionIssue(
                            id = "pp_etre_mp_${pp.start}",
                            originalFragment = pp.text,
                            suggestion = FrenchNormalizer.preserveCase(pp.text, "${base}s"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "PP_ETRE_ACCORD",
                            explanation = "Avec l'auxiliaire « être », le participe passé s'accorde en genre et en nombre avec le sujet « Ils ».",
                            example = "Ils sont partis.",
                            confidence = 0.98f,
                            startIndex = pp.start,
                            endIndex = pp.end
                        )
                    }
                    subj.lower == "elles" && aux.lower in setOf("sont", "étaient", "seront", "seraient") -> {
                        issues += CorrectionIssue(
                            id = "pp_etre_fp_${pp.start}",
                            originalFragment = pp.text,
                            suggestion = FrenchNormalizer.preserveCase(pp.text, "${base}es"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "PP_ETRE_ACCORD",
                            explanation = "Avec l'auxiliaire « être », le participe passé s'accorde au féminin pluriel avec « Elles ».",
                            example = "Elles sont parties.",
                            confidence = 0.98f,
                            startIndex = pp.start,
                            endIndex = pp.end
                        )
                    }
                    subj.lower == "elle" && aux.lower in setOf("est", "était", "sera", "serait") -> {
                        issues += CorrectionIssue(
                            id = "pp_etre_fs_${pp.start}",
                            originalFragment = pp.text,
                            suggestion = FrenchNormalizer.preserveCase(pp.text, "${base}e"),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "PP_ETRE_ACCORD",
                            explanation = "Avec l'auxiliaire « être », le participe passé s'accorde au féminin singulier avec « Elle ».",
                            example = "Elle est partie.",
                            confidence = 0.98f,
                            startIndex = pp.start,
                            endIndex = pp.end
                        )
                    }
                }
            }
        }

        // 2. Accord avec avoir SANS COD antéposé (ex: "Elle a mangée" -> "Elle a mangé")
        for (i in 0 until sentence.size - 2) {
            val subj = sentence[i]
            val aux = sentence[i + 1]
            val pp = sentence[i + 2]
            if (subj.lower == "elle" && aux.lower in setOf("a", "avait") && pp.lower in setOf("mangée", "parlée", "travaillée", "jouée", "donnée", "achetée")) {
                val mascSg = pp.lower.dropLast(1)
                issues += CorrectionIssue(
                    id = "pp_avoir_no_cod_${pp.start}",
                    originalFragment = pp.text,
                    suggestion = FrenchNormalizer.preserveCase(pp.text, mascSg),
                    category = CorrectionCategory.GRAMMAIRE,
                    ruleCode = "PP_AVOIR_INVARIABLE",
                    explanation = "Avec l'auxiliaire « avoir », le participe passé ne s'accorde jamais avec le sujet. Sans COD placé avant le verbe, il reste invariable.",
                    example = "Elle a mangé.",
                    confidence = 0.98f,
                    startIndex = pp.start,
                    endIndex = pp.end
                )
            }
        }

        // 3. Accord avec avoir + COD antéposé introduit par "que" (ex: "Les fleurs que j'ai acheté" -> "achetées")
        for (i in 1 until sentence.size - 3) {
            val queToken = sentence[i]
            if (queToken.lower != "que" && queToken.lower != "qu'") continue
            val nounToken = sentence[i - 1]
            val nounInfo = lexicon.nouns[nounToken.lower] ?: lexicon.pluralToNoun[nounToken.lower] ?: continue
            val isPlural = nounToken.lower in lexicon.pluralToNoun && nounInfo.singular != nounInfo.plural
            val isFeminine = nounInfo.gender == Gender.FEMININE

            // Chercher l'auxiliaire avoir puis le participe passé dans les 4 tokens suivants
            for (j in i + 1 until minOf(sentence.size - 1, i + 5)) {
                val tok = sentence[j]
                if (tok.lower in setOf("ai", "as", "a", "avons", "avez", "ont", "avais", "avait")) {
                    val ppTok = sentence.getOrNull(j + 1) ?: break
                    val basePp = when (ppTok.lower) {
                        "acheté", "achetée", "achetés", "achetées" -> "acheté"
                        "écrit", "écrite", "écrits", "écrites" -> "écrit"
                        "lu", "lue", "lus", "lues" -> "lu"
                        "vu", "vue", "vus", "vues" -> "vu"
                        "pris", "prise", "prises" -> "pris"
                        "fait", "faite", "faits", "faites" -> "fait"
                        "choisi", "choisie", "choisis", "choisies" -> "choisi"
                        "corrigé", "corrigée", "corrigés", "corrigées" -> "corrigé"
                        "préparé", "préparée", "préparés", "préparées" -> "préparé"
                        else -> null
                    } ?: break

                    val expected = when {
                        isFeminine && isPlural -> "${basePp}es"
                        isFeminine -> "${basePp}e"
                        isPlural -> if (basePp.endsWith("s")) basePp else "${basePp}s"
                        else -> basePp
                    }

                    if (ppTok.lower != expected) {
                        issues += CorrectionIssue(
                            id = "pp_cod_${ppTok.start}",
                            originalFragment = ppTok.text,
                            suggestion = FrenchNormalizer.preserveCase(ppTok.text, expected),
                            category = CorrectionCategory.GRAMMAIRE,
                            ruleCode = "PP_AVOIR_COD_ANTEPOSE",
                            explanation = "Le COD « ${nounToken.text} » (repris par « que ») est placé avant l'auxiliaire avoir : le participe passé s'accorde avec lui.",
                            example = "Les fleurs que j'ai achetées.",
                            confidence = 0.96f,
                            startIndex = ppTok.start,
                            endIndex = ppTok.end
                        )
                    }
                    break
                }
            }
        }
    }

    private fun checkSubjunctiveAndSiConditional(sentence: List<FrenchToken>, issues: MutableList<CorrectionIssue>) {
        // 1. "il faut que" + sujet + indicatif -> subjonctif requis
        for (i in 0 until sentence.size - 4) {
            if (sentence[i].lower == "il" &&
                sentence[i + 1].lower == "faut" &&
                (sentence[i + 2].lower == "que" || sentence[i + 2].lower == "qu'")
            ) {
                val verbTok = sentence[i + 4]
                val subjForm = subjunctiveRequiredMap[verbTok.lower]
                if (subjForm != null) {
                    issues += CorrectionIssue(
                        id = "subj_${verbTok.start}",
                        originalFragment = verbTok.text,
                        suggestion = FrenchNormalizer.preserveCase(verbTok.text, subjForm),
                        category = CorrectionCategory.CONJUGAISON,
                        ruleCode = "SUBJONCTIF_IL_FAUT_QUE",
                        explanation = "La locution « il faut que » impose toujours le mode subjonctif.",
                        example = "Il faut que tu viennes.",
                        confidence = 0.98f,
                        startIndex = verbTok.start,
                        endIndex = verbTok.end
                    )
                }
            }
        }

        // 2. Concordance "Si" + conditionnel interdit ("Si j'aurais su" -> "Si j'avais su")
        val condToImperfect = mapOf(
            "aurais" to "avais",
            "aurait" to "avait",
            "aurions" to "avions",
            "auriez" to "aviez",
            "auraient" to "avaient",
            "serais" to "étais",
            "serait" to "était",
            "serions" to "étions",
            "seriez" to "étiez",
            "seraient" to "étaient",
            "viendrais" to "venais",
            "viendrait" to "venait",
            "ferais" to "faisais",
            "ferait" to "faisait",
            "irais" to "allais",
            "irait" to "allait",
            "pourrais" to "pouvais",
            "voudrais" to "voulais"
        )
        for (i in 0 until sentence.size - 2) {
            if (sentence[i].lower == "si" || sentence[i].lower == "s'") {
                val verbIdx = if (sentence[i + 1].type == TokenType.ELISION_PREFIX || sentence[i + 1].lower in setOf("je", "tu", "il", "elle", "on", "nous", "vous", "ils", "elles")) {
                    i + 2
                } else {
                    continue
                }
                val verbTok = sentence.getOrNull(verbIdx) ?: continue
                val imp = condToImperfect[verbTok.lower]
                if (imp != null) {
                    issues += CorrectionIssue(
                        id = "si_cond_${verbTok.start}",
                        originalFragment = verbTok.text,
                        suggestion = FrenchNormalizer.preserveCase(verbTok.text, imp),
                        category = CorrectionCategory.CONJUGAISON,
                        ruleCode = "SI_PAS_CONDITIONNEL",
                        explanation = "Dans une proposition hypothétique introduite par « si », on n'emploie jamais le conditionnel : utilisez l'imparfait ou le plus-que-parfait.",
                        example = "Si j'avais su, je serais venu.",
                        confidence = 0.99f,
                        startIndex = verbTok.start,
                        endIndex = verbTok.end
                    )
                }
            }
        }
    }
}
