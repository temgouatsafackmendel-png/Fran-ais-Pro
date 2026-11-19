package com.example.linguistics.conjugation

import com.example.linguistics.model.FrenchNormalizer

data class ConjugationEntryData(
    val pronoun: String,
    val form: String
)

data class VerbConjugationTable(
    val infinitive: String,
    val groupNumber: Int,
    val auxiliary: String,
    val presentParticiple: String,
    val pastParticiple: String,
    val isIrregular: Boolean,
    val modelInfo: String,
    val tenses: Map<String, List<ConjugationEntryData>>
)

data class IdentifiedVerbForm(
    val infinitive: String,
    val tense: String,
    val pronoun: String,
    val matchedForm: String
)

/**
 * Conjugueur local complet du français :
 * - Prend en charge les 3 groupes (réguliers -er, -ir, et grands verbes irréguliers)
 * - Génère tous les temps exigés : Présent, Imparfait, Passé simple, Futur simple,
 *   Passé composé, Plus-que-parfait, Futur antérieur, Conditionnel présent,
 *   Subjonctif présent et Impératif présent.
 * - Permet l'identification inverse d'une forme conjuguée.
 */
class FrenchConjugatorEngine {

    private data class IrregularSpec(
        val group: Int,
        val auxiliary: String,
        val presentParticiple: String,
        val pastParticiple: String,
        val present: List<String>,
        val imperfectStem: String,
        val passeSimple: List<String>,
        val futureStem: String,
        val subjunctive: List<String>,
        val imperative: List<String>,
        val modelInfo: String
    )

    private val pronouns = listOf("je", "tu", "il/elle", "nous", "vous", "ils/elles")
    private val imperativePronouns = listOf("(tu)", "(nous)", "(vous)")

    private val auxAvoirPresent = listOf("ai", "as", "a", "avons", "avez", "ont")
    private val auxAvoirImperfect = listOf("avais", "avais", "avait", "avions", "aviez", "avaient")
    private val auxAvoirFuture = listOf("aurai", "auras", "aura", "aurons", "aurez", "auront")

    private val auxEtrePresent = listOf("suis", "es", "est", "sommes", "êtes", "sont")
    private val auxEtreImperfect = listOf("étais", "étais", "était", "étions", "étiez", "étaient")
    private val auxEtreFuture = listOf("serai", "seras", "sera", "serons", "serez", "seront")

    private val etreVerbs = setOf(
        "aller", "venir", "arriver", "partir", "entrer", "sortir", "monter", "descendre",
        "naître", "mourir", "rester", "tomber", "devenir", "revenir", "passer", "retourner"
    )

    private val irregulars: Map<String, IrregularSpec> = mapOf(
        "être" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "étant", pastParticiple = "été",
            present = listOf("suis", "es", "est", "sommes", "êtes", "sont"),
            imperfectStem = "ét",
            passeSimple = listOf("fus", "fus", "fut", "fûmes", "fûtes", "furent"),
            futureStem = "ser",
            subjunctive = listOf("sois", "sois", "soit", "soyons", "soyez", "soient"),
            imperative = listOf("sois", "soyons", "soyez"),
            modelInfo = "Auxiliaire fondamental (3e groupe irrégulier)"
        ),
        "avoir" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "ayant", pastParticiple = "eu",
            present = listOf("ai", "as", "a", "avons", "avez", "ont"),
            imperfectStem = "av",
            passeSimple = listOf("eus", "eus", "eut", "eûmes", "eûtes", "eurent"),
            futureStem = "aur",
            subjunctive = listOf("aie", "aies", "ait", "ayons", "ayez", "aient"),
            imperative = listOf("aie", "ayons", "ayez"),
            modelInfo = "Auxiliaire fondamental (3e groupe irrégulier)"
        ),
        "aller" to IrregularSpec(
            group = 3, auxiliary = "être", presentParticiple = "allant", pastParticiple = "allé",
            present = listOf("vais", "vas", "va", "allons", "allez", "vont"),
            imperfectStem = "all",
            passeSimple = listOf("allai", "allas", "alla", "allâmes", "allâtes", "allèrent"),
            futureStem = "ir",
            subjunctive = listOf("aille", "ailles", "aille", "allions", "alliez", "aillent"),
            imperative = listOf("va", "allons", "allez"),
            modelInfo = "Seul verbe en -er du 3e groupe (auxiliaire être)"
        ),
        "faire" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "faisant", pastParticiple = "fait",
            present = listOf("fais", "fais", "fait", "faisons", "faites", "font"),
            imperfectStem = "fais",
            passeSimple = listOf("fis", "fis", "fit", "fîmes", "fîtes", "firent"),
            futureStem = "fer",
            subjunctive = listOf("fasse", "fasses", "fasse", "fassions", "fassiez", "fassent"),
            imperative = listOf("fais", "faisons", "faites"),
            modelInfo = "Verbe irrégulier du 3e groupe (vous faites)"
        ),
        "venir" to IrregularSpec(
            group = 3, auxiliary = "être", presentParticiple = "venant", pastParticiple = "venu",
            present = listOf("viens", "viens", "vient", "venons", "venez", "viennent"),
            imperfectStem = "ven",
            passeSimple = listOf("vins", "vins", "vint", "vînmes", "vîntes", "vinrent"),
            futureStem = "viendr",
            subjunctive = listOf("vienne", "viennes", "vienne", "venions", "veniez", "viennent"),
            imperative = listOf("viens", "venons", "venez"),
            modelInfo = "Modèle des verbes en -enir (auxiliaire être)"
        ),
        "prendre" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "prenant", pastParticiple = "pris",
            present = listOf("prends", "prends", "prend", "prenons", "prenez", "prennent"),
            imperfectStem = "pren",
            passeSimple = listOf("pris", "pris", "prit", "prîmes", "prîtes", "prirent"),
            futureStem = "prendr",
            subjunctive = listOf("prenne", "prennes", "prenne", "prenions", "preniez", "prennent"),
            imperative = listOf("prends", "prenons", "prenez"),
            modelInfo = "Modèle prendre / apprendre / comprendre"
        ),
        "apprendre" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "apprenant", pastParticiple = "appris",
            present = listOf("apprends", "apprends", "apprend", "apprenons", "apprenez", "apprennent"),
            imperfectStem = "appren",
            passeSimple = listOf("appris", "appris", "apprit", "apprîmes", "apprîtes", "apprirent"),
            futureStem = "apprendr",
            subjunctive = listOf("apprenne", "apprennes", "apprenne", "apprenions", "appreniez", "apprennent"),
            imperative = listOf("apprends", "apprenons", "apprenez"),
            modelInfo = "Se conjugue sur le modèle de prendre"
        ),
        "comprendre" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "comprenant", pastParticiple = "compris",
            present = listOf("comprends", "comprends", "comprend", "comprenons", "comprenez", "comprennent"),
            imperfectStem = "compren",
            passeSimple = listOf("compris", "compris", "comprit", "comprîmes", "comprîtes", "comprirent"),
            futureStem = "comprendr",
            subjunctive = listOf("comprenne", "comprennes", "comprenne", "comprenions", "compreniez", "comprennent"),
            imperative = listOf("comprends", "comprenons", "comprenez"),
            modelInfo = "Se conjugue sur le modèle de prendre"
        ),
        "pouvoir" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "pouvant", pastParticiple = "pu",
            present = listOf("peux", "peux", "peut", "pouvons", "pouvez", "peuvent"),
            imperfectStem = "pouv",
            passeSimple = listOf("pus", "pus", "put", "pûmes", "pûtes", "purent"),
            futureStem = "pourr",
            subjunctive = listOf("puisse", "puisses", "puisse", "puissions", "puissiez", "puissent"),
            imperative = listOf("—", "—", "—"),
            modelInfo = "Verbe modal irrégulier du 3e groupe (sans impératif)"
        ),
        "savoir" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "sachant", pastParticiple = "su",
            present = listOf("sais", "sais", "sait", "savons", "savez", "savent"),
            imperfectStem = "sav",
            passeSimple = listOf("sus", "sus", "sut", "sûmes", "sûtes", "surent"),
            futureStem = "saur",
            subjunctive = listOf("sache", "saches", "sache", "sachions", "sachiez", "sachent"),
            imperative = listOf("sache", "sachons", "sachez"),
            modelInfo = "Verbe irrégulier du 3e groupe"
        ),
        "vouloir" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "voulant", pastParticiple = "voulu",
            present = listOf("veux", "veux", "veut", "voulons", "voulez", "veulent"),
            imperfectStem = "voul",
            passeSimple = listOf("voulus", "voulus", "voulut", "voulûmes", "voulûtes", "voulurent"),
            futureStem = "voudr",
            subjunctive = listOf("veuille", "veuilles", "veuille", "voulions", "vouliez", "veuillent"),
            imperative = listOf("veuille", "veuillons", "veuillez"),
            modelInfo = "Verbe modal du 3e groupe"
        ),
        "devoir" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "devant", pastParticiple = "dû",
            present = listOf("dois", "dois", "doit", "devons", "devez", "doivent"),
            imperfectStem = "dev",
            passeSimple = listOf("dus", "dus", "dut", "dûmes", "dûtes", "durent"),
            futureStem = "devr",
            subjunctive = listOf("doive", "doives", "doive", "devions", "deviez", "doivent"),
            imperative = listOf("dois", "devons", "devez"),
            modelInfo = "Participe passé masculin singulier avec accent circonflexe : dû"
        ),
        "partir" to IrregularSpec(
            group = 3, auxiliary = "être", presentParticiple = "partant", pastParticiple = "parti",
            present = listOf("pars", "pars", "part", "partons", "partez", "partent"),
            imperfectStem = "part",
            passeSimple = listOf("partis", "partis", "partit", "partîmes", "partîtes", "partirent"),
            futureStem = "partir",
            subjunctive = listOf("parte", "partes", "parte", "partions", "partiez", "partent"),
            imperative = listOf("pars", "partons", "partez"),
            modelInfo = "Verbe en -ir du 3e groupe (auxiliaire être)"
        ),
        "dire" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "disant", pastParticiple = "dit",
            present = listOf("dis", "dis", "dit", "disons", "dites", "disent"),
            imperfectStem = "dis",
            passeSimple = listOf("dis", "dis", "dit", "dîmes", "dîtes", "dirent"),
            futureStem = "dir",
            subjunctive = listOf("dise", "dises", "dise", "disions", "disiez", "disent"),
            imperative = listOf("dis", "disons", "dites"),
            modelInfo = "Verbe irrégulier du 3e groupe (vous dites)"
        ),
        "écrire" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "écrivant", pastParticiple = "écrit",
            present = listOf("écris", "écris", "écrit", "écrivons", "écrivez", "écrivent"),
            imperfectStem = "écriv",
            passeSimple = listOf("écrivis", "écrivis", "écrivit", "écrivîmes", "écrivîtes", "écrivirent"),
            futureStem = "écrir",
            subjunctive = listOf("écrive", "écrives", "écrive", "écrivions", "écriviez", "écrivent"),
            imperative = listOf("écris", "écrivons", "écrivez"),
            modelInfo = "Modèle écrire / décrire / inscrire"
        ),
        "lire" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "lisant", pastParticiple = "lu",
            present = listOf("lis", "lis", "lit", "lisons", "lisez", "lisent"),
            imperfectStem = "lis",
            passeSimple = listOf("lus", "lus", "lut", "lûmes", "lûtes", "lurent"),
            futureStem = "lir",
            subjunctive = listOf("lise", "lises", "lise", "lisions", "lisiez", "lisent"),
            imperative = listOf("lis", "lisons", "lisez"),
            modelInfo = "Modèle lire / relire / élire"
        ),
        "voir" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "voyant", pastParticiple = "vu",
            present = listOf("vois", "vois", "voit", "voyons", "voyez", "voient"),
            imperfectStem = "voy",
            passeSimple = listOf("vis", "vis", "vit", "vîmes", "vîtes", "virent"),
            futureStem = "verr",
            subjunctive = listOf("voie", "voies", "voie", "voyions", "voyiez", "voient"),
            imperative = listOf("vois", "voyons", "voyez"),
            modelInfo = "Verbe irrégulier du 3e groupe (futur en verr-)"
        ),
        "mettre" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "mettant", pastParticiple = "mis",
            present = listOf("mets", "mets", "met", "mettons", "mettez", "mettent"),
            imperfectStem = "mett",
            passeSimple = listOf("mis", "mis", "mit", "mîmes", "mîtes", "mirent"),
            futureStem = "mettr",
            subjunctive = listOf("mette", "mettes", "mette", "mettions", "mettiez", "mettent"),
            imperative = listOf("mets", "mettons", "mettez"),
            modelInfo = "Modèle mettre / permettre / promettre"
        ),
        "recevoir" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "recevant", pastParticiple = "reçu",
            present = listOf("reçois", "reçois", "reçoit", "recevons", "recevez", "reçoivent"),
            imperfectStem = "recev",
            passeSimple = listOf("reçus", "reçus", "reçut", "reçûmes", "reçûtes", "reçurent"),
            futureStem = "recevr",
            subjunctive = listOf("reçoive", "reçoives", "reçoive", "recevions", "receviez", "reçoivent"),
            imperative = listOf("reçois", "recevons", "recevez"),
            modelInfo = "Modèle des verbes en -cevoir (cédille devant o/u)"
        ),
        "connaître" to IrregularSpec(
            group = 3, auxiliary = "avoir", presentParticiple = "connaissant", pastParticiple = "connu",
            present = listOf("connais", "connais", "connaît", "connaissons", "connaissez", "connaissent"),
            imperfectStem = "connaiss",
            passeSimple = listOf("connus", "connus", "connut", "connûmes", "connûtes", "connurent"),
            futureStem = "connaîtr",
            subjunctive = listOf("connaisse", "connaisses", "connaisse", "connaissions", "connaissiez", "connaissent"),
            imperative = listOf("connais", "connaissons", "connaissez"),
            modelInfo = "Modèle des verbes en -aître (accent circonflexe devant t)"
        )
    )

    val referenceVerbs: List<String> = listOf(
        "être", "avoir", "aller", "faire", "venir", "prendre", "apprendre", "comprendre",
        "pouvoir", "savoir", "vouloir", "devoir", "partir", "dire", "écrire", "lire",
        "voir", "mettre", "recevoir", "connaître",
        "parler", "aimer", "travailler", "jouer", "manger", "corriger", "commencer",
        "acheter", "appeler", "arriver", "progresser", "étudier", "donner", "penser",
        "finir", "choisir", "réussir", "grandir", "réfléchir", "agir"
    )

    private val secondGroupVerbs = setOf(
        "finir", "choisir", "réussir", "grandir", "réfléchir", "agir", "bâtir", "remplir",
        "obéir", "rougir", "applaudir", "définir", "fournir", "garantir", "investir", "nourrir"
    )

    private val reverseLookupCache: Map<String, List<IdentifiedVerbForm>> by lazy {
        val map = mutableMapOf<String, MutableList<IdentifiedVerbForm>>()
        for (inf in referenceVerbs) {
            val table = conjugate(inf) ?: continue
            table.tenses.forEach { (tense, rows) ->
                rows.forEach { row ->
                    if (row.form != "—") {
                        val key = FrenchNormalizer.stripAccents(row.form.lowercase())
                        map.getOrPut(key) { mutableListOf() }.add(
                            IdentifiedVerbForm(
                                infinitive = table.infinitive,
                                tense = tense,
                                pronoun = row.pronoun,
                                matchedForm = row.form
                            )
                        )
                    }
                }
            }
        }
        map
    }

    fun conjugate(rawInfinitive: String): VerbConjugationTable? {
        val inf = rawInfinitive.trim().lowercase()
        if (inf.length < 3) return null

        irregulars[inf]?.let { spec ->
            return buildFromSpec(inf, spec)
        }

        if (inf.endsWith("er") && inf != "aller") {
            return conjugateFirstGroup(inf)
        }

        if (inf.endsWith("ir") && (inf in secondGroupVerbs || inf.length >= 4)) {
            return conjugateSecondGroup(inf)
        }

        return null
    }

    fun identifyForm(query: String): List<IdentifiedVerbForm> {
        val cleaned = query.trim().lowercase()
            .removePrefix("je ").removePrefix("j'")
            .removePrefix("tu ").removePrefix("il ").removePrefix("elle ")
            .removePrefix("nous ").removePrefix("vous ").removePrefix("ils ").removePrefix("elles ")
            .trim()
        val key = FrenchNormalizer.stripAccents(cleaned)
        return reverseLookupCache[key].orEmpty().distinctBy { "${it.infinitive}_${it.tense}_${it.pronoun}" }
    }

    private fun buildFromSpec(inf: String, spec: IrregularSpec): VerbConjugationTable {
        val impEndings = listOf("ais", "ais", "ait", "ions", "iez", "aient")
        val futEndings = listOf("ai", "as", "a", "ons", "ez", "ont")
        val condEndings = listOf("ais", "ais", "ait", "ions", "iez", "aient")

        val imperfect = impEndings.map { spec.imperfectStem + it }
        val future = futEndings.map { spec.futureStem + it }
        val conditional = condEndings.map { spec.futureStem + it }

        val auxPres = if (spec.auxiliary == "être") auxEtrePresent else auxAvoirPresent
        val auxImp = if (spec.auxiliary == "être") auxEtreImperfect else auxAvoirImperfect
        val auxFut = if (spec.auxiliary == "être") auxEtreFuture else auxAvoirFuture

        val passeCompose = auxPres.mapIndexed { idx, aux -> "$aux ${agreeParticiple(spec.pastParticiple, spec.auxiliary, idx)}" }
        val plusQueParfait = auxImp.mapIndexed { idx, aux -> "$aux ${agreeParticiple(spec.pastParticiple, spec.auxiliary, idx)}" }
        val futurAnterieur = auxFut.mapIndexed { idx, aux -> "$aux ${agreeParticiple(spec.pastParticiple, spec.auxiliary, idx)}" }

        val tenses = linkedMapOf(
            "Présent" to pairWithPronouns(spec.present),
            "Imparfait" to pairWithPronouns(imperfect),
            "Passé simple" to pairWithPronouns(spec.passeSimple),
            "Futur simple" to pairWithPronouns(future),
            "Passé composé" to pairWithPronouns(passeCompose),
            "Plus-que-parfait" to pairWithPronouns(plusQueParfait),
            "Futur antérieur" to pairWithPronouns(futurAnterieur),
            "Conditionnel présent" to pairWithPronouns(conditional),
            "Subjonctif présent" to pairWithPronouns(spec.subjunctive, subjunctive = true),
            "Impératif présent" to imperativePronouns.zip(spec.imperative).map { (p, f) -> ConjugationEntryData(p, f) }
        )

        return VerbConjugationTable(
            infinitive = inf,
            groupNumber = spec.group,
            auxiliary = spec.auxiliary,
            presentParticiple = spec.presentParticiple,
            pastParticiple = spec.pastParticiple,
            isIrregular = true,
            modelInfo = spec.modelInfo,
            tenses = tenses
        )
    }

    private fun conjugateFirstGroup(inf: String): VerbConjugationTable {
        val stem = inf.dropLast(2)
        val isGer = inf.endsWith("ger")
        val isCer = inf.endsWith("cer")
        val isAcheter = inf == "acheter"
        val isAppeler = inf == "appeler"
        val aux = if (inf in etreVerbs) "être" else "avoir"

        fun strongStem(): String = when {
            isAcheter -> "achèt"
            isAppeler -> "appell"
            else -> stem
        }

        val softBeforeAO = when {
            isGer -> "${stem}e"
            isCer -> stem.dropLast(1) + "ç"
            else -> stem
        }

        val present = listOf(
            "${strongStem()}e",
            "${strongStem()}es",
            "${strongStem()}e",
            "${softBeforeAO}ons",
            "${stem}ez",
            "${strongStem()}ent"
        )
        val imperfect = listOf(
            "${softBeforeAO}ais",
            "${softBeforeAO}ais",
            "${softBeforeAO}ait",
            "${stem}ions",
            "${stem}iez",
            "${softBeforeAO}aient"
        )
        val passeSimple = listOf(
            "${softBeforeAO}ai",
            "${softBeforeAO}as",
            "${softBeforeAO}a",
            "${softBeforeAO}âmes",
            "${softBeforeAO}âtes",
            "${stem}èrent"
        )
        val futBase = when {
            isAcheter -> "achèter"
            isAppeler -> "appeller"
            else -> inf
        }
        val future = listOf("ai", "as", "a", "ons", "ez", "ont").map { futBase + it }
        val conditional = listOf("ais", "ais", "ait", "ions", "iez", "aient").map { futBase + it }
        val subjunctive = listOf(
            "${strongStem()}e",
            "${strongStem()}es",
            "${strongStem()}e",
            "${stem}ions",
            "${stem}iez",
            "${strongStem()}ent"
        )
        val imperative = listOf("${strongStem()}e", "${softBeforeAO}ons", "${stem}ez")
        val pastPart = "${stem}é"
        val presPart = "${softBeforeAO}ant"

        val auxPres = if (aux == "être") auxEtrePresent else auxAvoirPresent
        val auxImp = if (aux == "être") auxEtreImperfect else auxAvoirImperfect
        val auxFut = if (aux == "être") auxEtreFuture else auxAvoirFuture

        val passeCompose = auxPres.mapIndexed { idx, a -> "$a ${agreeParticiple(pastPart, aux, idx)}" }
        val plusQueParfait = auxImp.mapIndexed { idx, a -> "$a ${agreeParticiple(pastPart, aux, idx)}" }
        val futurAnterieur = auxFut.mapIndexed { idx, a -> "$a ${agreeParticiple(pastPart, aux, idx)}" }

        val tenses = linkedMapOf(
            "Présent" to pairWithPronouns(present),
            "Imparfait" to pairWithPronouns(imperfect),
            "Passé simple" to pairWithPronouns(passeSimple),
            "Futur simple" to pairWithPronouns(future),
            "Passé composé" to pairWithPronouns(passeCompose),
            "Plus-que-parfait" to pairWithPronouns(plusQueParfait),
            "Futur antérieur" to pairWithPronouns(futurAnterieur),
            "Conditionnel présent" to pairWithPronouns(conditional),
            "Subjonctif présent" to pairWithPronouns(subjunctive, subjunctive = true),
            "Impératif présent" to imperativePronouns.zip(imperative).map { (p, f) -> ConjugationEntryData(p, f) }
        )

        val info = when {
            isGer -> "1er groupe en -ger (e euphonique devant a et o)"
            isCer -> "1er groupe en -cer (cédille ç devant a et o)"
            isAcheter -> "1er groupe avec accent grave devant e muet"
            isAppeler -> "1er groupe avec doublement du l devant e muet"
            else -> "1er groupe régulier en -er"
        }

        return VerbConjugationTable(
            infinitive = inf,
            groupNumber = 1,
            auxiliary = aux,
            presentParticiple = presPart,
            pastParticiple = pastPart,
            isIrregular = isAcheter || isAppeler,
            modelInfo = info,
            tenses = tenses
        )
    }

    private fun conjugateSecondGroup(inf: String): VerbConjugationTable {
        val stem = inf.dropLast(2)
        val present = listOf("${stem}is", "${stem}is", "${stem}it", "${stem}issons", "${stem}issez", "${stem}issent")
        val imperfect = listOf("issais", "issais", "issait", "issions", "issiez", "issaient").map { stem + it }
        val passeSimple = listOf("is", "is", "it", "îmes", "îtes", "irent").map { stem + it }
        val future = listOf("ai", "as", "a", "ons", "ez", "ont").map { inf + it }
        val conditional = listOf("ais", "ais", "ait", "ions", "iez", "aient").map { inf + it }
        val subjunctive = listOf("isse", "isses", "isse", "issions", "issiez", "issent").map { stem + it }
        val imperative = listOf("${stem}is", "${stem}issons", "${stem}issez")
        val pastPart = "${stem}i"
        val presPart = "${stem}issant"

        val passeCompose = auxAvoirPresent.map { "$it $pastPart" }
        val plusQueParfait = auxAvoirImperfect.map { "$it $pastPart" }
        val futurAnterieur = auxAvoirFuture.map { "$it $pastPart" }

        val tenses = linkedMapOf(
            "Présent" to pairWithPronouns(present),
            "Imparfait" to pairWithPronouns(imperfect),
            "Passé simple" to pairWithPronouns(passeSimple),
            "Futur simple" to pairWithPronouns(future),
            "Passé composé" to pairWithPronouns(passeCompose),
            "Plus-que-parfait" to pairWithPronouns(plusQueParfait),
            "Futur antérieur" to pairWithPronouns(futurAnterieur),
            "Conditionnel présent" to pairWithPronouns(conditional),
            "Subjonctif présent" to pairWithPronouns(subjunctive, subjunctive = true),
            "Impératif présent" to imperativePronouns.zip(imperative).map { (p, f) -> ConjugationEntryData(p, f) }
        )

        return VerbConjugationTable(
            infinitive = inf,
            groupNumber = 2,
            auxiliary = "avoir",
            presentParticiple = presPart,
            pastParticiple = pastPart,
            isIrregular = false,
            modelInfo = "2e groupe régulier en -ir (participe présent en -issant)",
            tenses = tenses
        )
    }

    private fun agreeParticiple(participle: String, auxiliary: String, personIdx: Int): String {
        if (auxiliary != "être") return participle
        return when (personIdx) {
            3, 4, 5 -> if (participle.endsWith("s")) participle else "${participle}s"
            else -> participle
        }
    }

    private fun pairWithPronouns(forms: List<String>, subjunctive: Boolean = false): List<ConjugationEntryData> {
        return pronouns.zip(forms).mapIndexed { idx, (pron, form) ->
            val displayPron = if (idx == 0 && startsWithVowelOrH(form)) "j'" else pron
            val prefix = if (subjunctive) {
                if (displayPron.startsWith("i") || displayPron.startsWith("e")) "qu'$displayPron" else "que $displayPron"
            } else {
                displayPron
            }
            ConjugationEntryData(prefix, form)
        }
    }

    private fun startsWithVowelOrH(word: String): Boolean {
        if (word.isEmpty()) return false
        return word.first().lowercaseChar() in setOf('a', 'e', 'i', 'o', 'u', 'y', 'h', 'é', 'è', 'ê', 'â', 'î', 'ô', 'û')
    }
}
