package com.example.linguistics.syntax

import com.example.linguistics.model.CorrectionCategory
import com.example.linguistics.model.CorrectionIssue
import com.example.linguistics.model.CorrectionRuleModule
import com.example.linguistics.model.FrenchNormalizer
import com.example.linguistics.model.FrenchToken
import com.example.linguistics.model.TokenType

/**
 * Moteur de syntaxe, ponctuation française et améliorations stylistiques non intrusives.
 */
class FrenchSyntaxAndStyleEngine : CorrectionRuleModule {

    override val moduleId: String = "syntax_style_v1"

    private data class PhraseRule(
        val regex: Regex,
        val replacement: String,
        val category: CorrectionCategory,
        val ruleCode: String,
        val explanation: String,
        val example: String,
        val confidence: Float,
        val isAmbiguous: Boolean = false
    )

    private val phraseRules = listOf(
        PhraseRule(
            regex = Regex("""\bmalgré\s+que\s+(il|elle|ils|elles|on)\b""", RegexOption.IGNORE_CASE),
            replacement = "bien qu'$1",
            category = CorrectionCategory.SYNTAXE,
            ruleCode = "SYN_MALGRE_QUE",
            explanation = "La locution « malgré que » est déconseillée en français soigné ; préférez « bien que » avec élision devant voyelle (« bien qu' »).",
            example = "Bien qu'il pleuve, nous sortons.",
            confidence = 0.99f
        ),
        PhraseRule(
            regex = Regex("""\bmalgré\s+qu['’]""", RegexOption.IGNORE_CASE),
            replacement = "bien qu'",
            category = CorrectionCategory.SYNTAXE,
            ruleCode = "SYN_MALGRE_QUE",
            explanation = "La locution « malgré que » est déconseillée en français soigné ; préférez « bien que » ou « quoique ».",
            example = "Bien qu'il pleuve, nous sortons.",
            confidence = 0.98f
        ),
        PhraseRule(
            regex = Regex("""\bmalgré\s+que\b""", RegexOption.IGNORE_CASE),
            replacement = "bien que",
            category = CorrectionCategory.SYNTAXE,
            ruleCode = "SYN_MALGRE_QUE",
            explanation = "La locution « malgré que » est déconseillée en français soigné ; préférez « bien que » ou « quoique ».",
            example = "Bien que tu sois occupé, prends une pause.",
            confidence = 0.96f
        ),
        PhraseRule(
            regex = Regex("""\bau\s+jour\s+d['’]aujourd['’]hui\b""", RegexOption.IGNORE_CASE),
            replacement = "aujourd'hui",
            category = CorrectionCategory.STYLE,
            ruleCode = "PLEONASME_AUJOURDHUI",
            explanation = "Pléonasme : « aujourd'hui » contient déjà le sens de « ce jour ». Préférez « aujourd'hui » ou « actuellement ».",
            example = "Aujourd'hui, cette méthode est reconnue.",
            confidence = 0.92f
        ),
        PhraseRule(
            regex = Regex("""\bvoire\s+même\b""", RegexOption.IGNORE_CASE),
            replacement = "voire",
            category = CorrectionCategory.STYLE,
            ruleCode = "PLEONASME_VOIRE_MEME",
            explanation = "Pléonasme : « voire » signifie déjà « et même ».",
            example = "C'est difficile, voire impossible.",
            confidence = 0.93f
        ),
        PhraseRule(
            regex = Regex("""\bmonter\s+en\s+haut\b""", RegexOption.IGNORE_CASE),
            replacement = "monter",
            category = CorrectionCategory.STYLE,
            ruleCode = "PLEONASME_MONTER",
            explanation = "Pléonasme : le verbe « monter » implique déjà un mouvement vers le haut.",
            example = "Veuillez monter au premier étage.",
            confidence = 0.91f
        ),
        PhraseRule(
            regex = Regex("""\bdescendre\s+en\s+bas\b""", RegexOption.IGNORE_CASE),
            replacement = "descendre",
            category = CorrectionCategory.STYLE,
            ruleCode = "PLEONASME_DESCENDRE",
            explanation = "Pléonasme : le verbe « descendre » indique déjà la direction vers le bas.",
            example = "Il descend au rez-de-chaussée.",
            confidence = 0.91f
        ),
        PhraseRule(
            regex = Regex("""\bprévoir\s+à\s+l['’]avance\b""", RegexOption.IGNORE_CASE),
            replacement = "prévoir",
            category = CorrectionCategory.STYLE,
            ruleCode = "PLEONASME_PREVOIR",
            explanation = "Pléonasme : « prévoir » signifie déjà voir à l'avance.",
            example = "Il est prudent de prévoir un plan de secours.",
            confidence = 0.90f
        ),
        PhraseRule(
            regex = Regex("""\bcar\s+en\s+effet\b""", RegexOption.IGNORE_CASE),
            replacement = "car",
            category = CorrectionCategory.STYLE,
            ruleCode = "PLEONASME_CAR_EN_EFFET",
            explanation = "Redondance : « car » et « en effet » expriment tous deux la cause.",
            example = "Il réussit, car il travaille régulièrement.",
            confidence = 0.89f
        ),
        PhraseRule(
            regex = Regex("""\bmais\s+cependant\b""", RegexOption.IGNORE_CASE),
            replacement = "cependant",
            category = CorrectionCategory.STYLE,
            ruleCode = "PLEONASME_MAIS_CEPENDANT",
            explanation = "Redondance d'opposition : choisissez soit « mais », soit « cependant ».",
            example = "Le défi est grand ; cependant, l'équipe reste confiante.",
            confidence = 0.89f
        ),
        PhraseRule(
            regex = Regex("""\bdu\s+coup\b""", RegexOption.IGNORE_CASE),
            replacement = "par conséquent",
            category = CorrectionCategory.STYLE,
            ruleCode = "STYLE_DU_COUP",
            explanation = "Expression orale familière : à l'écrit, préférez « par conséquent », « ainsi » ou « dès lors ».",
            example = "Par conséquent, nous avons adapté le calendrier.",
            confidence = 0.78f,
            isAmbiguous = true
        ),
        PhraseRule(
            regex = Regex("""\bau\s+niveau\s+de\b""", RegexOption.IGNORE_CASE),
            replacement = "concernant",
            category = CorrectionCategory.STYLE,
            ruleCode = "STYLE_AU_NIVEAU_DE",
            explanation = "Formulation passe-partout : précisez votre propos avec « concernant », « quant à » ou « sur le plan de ».",
            example = "Concernant le budget, les prévisions sont stables.",
            confidence = 0.74f,
            isAmbiguous = true
        ),
        PhraseRule(
            regex = Regex("""\bfaire\s+une\s+erreur\b""", RegexOption.IGNORE_CASE),
            replacement = "commettre une erreur",
            category = CorrectionCategory.STYLE,
            ruleCode = "STYLE_VERBE_PRECIS",
            explanation = "Pour un vocabulaire plus précis, le verbe « commettre » est plus soutenu que « faire » devant « erreur ».",
            example = "Il arrive à chacun de commettre une erreur.",
            confidence = 0.72f,
            isAmbiguous = true
        )
    )

    private val allowedConsecutiveDuplicates = setOf("nous", "vous", "très", "plus", "bien")
    private val lexicalStopWords = setOf(
        "dans", "pour", "avec", "sans", "mais", "donc", "alors", "cette", "cet", "elles", "nous", "vous",
        "être", "avoir", "faire", "comme", "aussi", "tout", "tous", "toute", "toutes", "leur", "leurs",
        "notre", "votre", "plus", "très", "bien", "encore", "toujours", "jamais", "quand", "depuis", "pendant"
    )

    override fun analyze(text: String, tokens: List<FrenchToken>): List<CorrectionIssue> {
        val issues = mutableListOf<CorrectionIssue>()

        // 1. Expressions pléonastiques et style
        for (rule in phraseRules) {
            for (match in rule.regex.findAll(text)) {
                val matched = match.value
                val rawReplacement = if (rule.replacement.contains("$1") && match.groupValues.size > 1) {
                    rule.replacement.replace("$1", match.groupValues[1].lowercase())
                } else {
                    rule.replacement
                }
                val suggestion = FrenchNormalizer.preserveCase(matched, rawReplacement)
                issues += CorrectionIssue(
                    id = "${rule.ruleCode}_${match.range.first}",
                    originalFragment = matched,
                    suggestion = suggestion,
                    category = rule.category,
                    ruleCode = rule.ruleCode,
                    explanation = rule.explanation,
                    example = rule.example,
                    confidence = rule.confidence,
                    startIndex = match.range.first,
                    endIndex = match.range.last + 1,
                    isAmbiguous = rule.isAmbiguous
                )
            }
        }

        // 2. Répétition immédiate de mots (ex: "le le")
        for (i in 0 until tokens.size - 1) {
            val a = tokens[i]
            val b = tokens[i + 1]
            if (a.type == TokenType.WORD && b.type == TokenType.WORD &&
                a.lower == b.lower && a.lower !in allowedConsecutiveDuplicates
            ) {
                issues += CorrectionIssue(
                    id = "dup_${a.start}_${b.end}",
                    originalFragment = text.substring(a.start, b.end),
                    suggestion = a.text,
                    category = CorrectionCategory.SYNTAXE,
                    ruleCode = "SYN_DOUBLON",
                    explanation = "Le mot « ${a.text} » est répété deux fois de suite.",
                    example = "Supprimez le doublon pour conserver uniquement « ${a.text} ».",
                    confidence = 0.99f,
                    startIndex = a.start,
                    endIndex = b.end
                )
            }
        }

        // 3. Ponctuation : double espace & espace avant virgule/point
        Regex("""  +""").findAll(text).forEach { match ->
            issues += CorrectionIssue(
                id = "punct_space_${match.range.first}",
                originalFragment = match.value,
                suggestion = " ",
                category = CorrectionCategory.PONCTUATION,
                ruleCode = "PONCT_DOUBLE_ESPACE",
                explanation = "Espaces multiples consécutifs : un seul espace suffit entre deux mots.",
                example = "Utilisez un espace simple.",
                confidence = 0.95f,
                startIndex = match.range.first,
                endIndex = match.range.last + 1
            )
        }

        Regex("""\s+([,.])""").findAll(text).forEach { match ->
            val mark = match.groupValues[1]
            issues += CorrectionIssue(
                id = "punct_before_${match.range.first}",
                originalFragment = match.value,
                suggestion = mark,
                category = CorrectionCategory.PONCTUATION,
                ruleCode = "PONCT_ESPACE_AVANT",
                explanation = "En typographie française, il n'y a jamais d'espace avant une virgule ou un point.",
                example = "Bonjour, voici le rapport.",
                confidence = 0.96f,
                startIndex = match.range.first,
                endIndex = match.range.last + 1
            )
        }

        // 4. Majuscule en début de phrase (uniquement pour les phrases de plus d'un mot)
        val sentences = tokens.groupBy { it.sentenceIndex }.values
        for (sentence in sentences) {
            val firstWord = sentence.firstOrNull { it.type == TokenType.WORD || it.type == TokenType.ELISION_PREFIX } ?: continue
            if (sentence.count { it.type == TokenType.WORD } >= 2 && firstWord.text.first().isLowerCase()) {
                val cap = firstWord.text.replaceFirstChar { it.titlecase() }
                issues += CorrectionIssue(
                    id = "cap_${firstWord.start}",
                    originalFragment = firstWord.text,
                    suggestion = cap,
                    category = CorrectionCategory.PONCTUATION,
                    ruleCode = "PONCT_MAJUSCULE",
                    explanation = "Une phrase doit commencer par une lettre majuscule.",
                    example = "$cap ...",
                    confidence = 0.94f,
                    startIndex = firstWord.start,
                    endIndex = firstWord.end
                )
            }
        }

        // 5. Répétition lexicale rapprochée (>= 6 lettres dans une fenêtre de 14 mots) — conseil de style non intrusif
        val wordTokens = tokens.filter { it.type == TokenType.WORD }
        val lastSeen = mutableMapOf<String, Int>()
        for ((idx, tok) in wordTokens.withIndex()) {
            if (tok.lower.length < 6 || tok.lower in lexicalStopWords) continue
            val prevIdx = lastSeen[tok.lower]
            if (prevIdx != null && idx - prevIdx in 2..14) {
                issues += CorrectionIssue(
                    id = "style_rep_${tok.start}",
                    originalFragment = tok.text,
                    suggestion = tok.text,
                    category = CorrectionCategory.STYLE,
                    ruleCode = "STYLE_REPETITION",
                    explanation = "Répétition rapprochée du mot « ${tok.text} ». Envisagez un synonyme ou un pronom pour alléger le style.",
                    example = "Variez le vocabulaire à l'aide du dictionnaire de synonymes.",
                    confidence = 0.65f,
                    startIndex = tok.start,
                    endIndex = tok.end,
                    isAmbiguous = true
                )
            }
            lastSeen[tok.lower] = idx
        }

        return issues
    }
}
