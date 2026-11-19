package com.example.linguistics.model

import java.text.Normalizer

enum class CorrectionCategory(val labelFr: String) {
    ORTHOGRAPHE("Orthographe"),
    GRAMMAIRE("Grammaire"),
    CONJUGAISON("Conjugaison"),
    SYNTAXE("Syntaxe"),
    PONCTUATION("Ponctuation"),
    STYLE("Style")
}

enum class TokenType {
    WORD,
    PUNCTUATION,
    NUMBER,
    ELISION_PREFIX
}

data class FrenchToken(
    val text: String,
    val lower: String,
    val normalized: String,
    val start: Int,
    val end: Int,
    val type: TokenType,
    val sentenceIndex: Int,
    val indexInSentence: Int
)

data class CorrectionIssue(
    val id: String,
    val originalFragment: String,
    val suggestion: String,
    val alternativeSuggestions: List<String> = emptyList(),
    val category: CorrectionCategory,
    val ruleCode: String,
    val explanation: String,
    val example: String,
    val confidence: Float, // 0.0f..1.0f
    val startIndex: Int,
    val endIndex: Int,
    val isAmbiguous: Boolean = false
)

data class CorrectionReport(
    val originalText: String,
    val issues: List<CorrectionIssue>,
    val wordCount: Int,
    val sentenceCount: Int,
    val analysisDurationMs: Long,
    val qualityScore: Int // 0..100
)

/**
 * Interface modulaire permettant de remplacer ou d'enrichir chaque sous-moteur
 * linguistique indépendamment (règles, dictionnaire, morphologie, NLP local).
 */
interface CorrectionRuleModule {
    val moduleId: String
    fun analyze(text: String, tokens: List<FrenchToken>): List<CorrectionIssue>
}

object FrenchNormalizer {
    private val COMBINING_DIACRITICS = Regex("\\p{InCombiningDiacriticalMarks}+")

    fun stripAccents(input: String): String {
        val decomposed = Normalizer.normalize(input.lowercase(), Normalizer.Form.NFD)
        return COMBINING_DIACRITICS.replace(decomposed, "")
            .replace("œ", "oe")
            .replace("æ", "ae")
    }

    fun preserveCase(template: String, replacement: String): String {
        if (template.isEmpty() || replacement.isEmpty()) return replacement
        return when {
            template.length > 1 && template.all { !it.isLetter() || it.isUpperCase() } -> replacement.uppercase()
            template.first().isUpperCase() -> replacement.replaceFirstChar { it.titlecase() }
            else -> replacement
        }
    }
}
