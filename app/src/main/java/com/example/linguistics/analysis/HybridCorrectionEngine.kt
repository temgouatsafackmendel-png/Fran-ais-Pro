package com.example.linguistics.analysis

import com.example.linguistics.grammar.FrenchGrammarRules
import com.example.linguistics.lexicon.FrenchLexicon
import com.example.linguistics.model.CorrectionCategory
import com.example.linguistics.model.CorrectionIssue
import com.example.linguistics.model.CorrectionReport
import com.example.linguistics.model.CorrectionRuleModule
import com.example.linguistics.model.TokenType
import com.example.linguistics.spelling.FrenchSpellingEngine
import com.example.linguistics.syntax.FrenchSyntaxAndStyleEngine
import com.example.linguistics.tokenizer.FrenchTokenizer

data class DiffSegment(
    val text: String,
    val type: DiffType
)

enum class DiffType { UNCHANGED, REMOVED, ADDED }

/**
 * Orchestrateur du moteur de correction hybride 100 % hors ligne.
 * Chaque module linguistique est découplé et remplaçable indépendamment.
 */
class HybridCorrectionEngine(
    private val tokenizer: FrenchTokenizer = FrenchTokenizer(),
    val lexicon: FrenchLexicon = FrenchLexicon(),
    private val modules: List<CorrectionRuleModule> = listOf(
        FrenchGrammarRules(lexicon),
        FrenchSpellingEngine(lexicon),
        FrenchSyntaxAndStyleEngine()
    )
) {

    fun analyze(text: String): CorrectionReport {
        val startNs = System.nanoTime()
        if (text.isBlank()) {
            return CorrectionReport(
                originalText = text,
                issues = emptyList(),
                wordCount = 0,
                sentenceCount = 0,
                analysisDurationMs = 0L,
                qualityScore = 100
            )
        }

        val tokens = tokenizer.tokenize(text)
        val rawIssues = modules.flatMap { it.analyze(text, tokens) }
        val resolvedIssues = resolveOverlaps(rawIssues)

        val wordCount = tokens.count { it.type == TokenType.WORD }
        val sentenceCount = (tokens.maxOfOrNull { it.sentenceIndex } ?: 0) + if (wordCount > 0) 1 else 0
        val blockingErrors = resolvedIssues.count { it.category != CorrectionCategory.STYLE }
        val quality = if (wordCount == 0) {
            100
        } else {
            val penalty = (blockingErrors * 14) + ((resolvedIssues.size - blockingErrors) * 4)
            (100 - penalty).coerceIn(15, 100)
        }

        val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
        return CorrectionReport(
            originalText = text,
            issues = resolvedIssues,
            wordCount = wordCount,
            sentenceCount = sentenceCount,
            analysisDurationMs = elapsedMs,
            qualityScore = quality
        )
    }

    /**
     * Applique une correction unique tout en préservant intégralement le reste du texte.
     */
    fun applySingleIssue(text: String, issue: CorrectionIssue): String {
        if (issue.startIndex < 0 || issue.endIndex > text.length || issue.startIndex >= issue.endIndex) {
            return text
        }
        return text.substring(0, issue.startIndex) + issue.suggestion + text.substring(issue.endIndex)
    }

    /**
     * Applique toutes les corrections fiables non ambiguës (ne modifie jamais silencieusement
     * les suggestions marquées ambiguës ou de style pur).
     */
    fun applySafeCorrections(text: String, issues: List<CorrectionIssue>): String {
        val safeIssues = issues
            .filter { !it.isAmbiguous && it.suggestion != it.originalFragment }
            .sortedByDescending { it.startIndex }

        var result = text
        for (issue in safeIssues) {
            if (issue.startIndex >= 0 && issue.endIndex <= result.length && issue.startIndex < issue.endIndex) {
                result = result.substring(0, issue.startIndex) + issue.suggestion + result.substring(issue.endIndex)
            }
        }
        return result
    }

    fun computeWordDiff(before: String, after: String): List<DiffSegment> {
        val a = before.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val b = after.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val dp = Array(a.size + 1) { IntArray(b.size + 1) }

        for (i in a.size - 1 downTo 0) {
            for (j in b.size - 1 downTo 0) {
                dp[i][j] = if (a[i] == b[j]) {
                    dp[i + 1][j + 1] + 1
                } else {
                    maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }

        val segments = mutableListOf<DiffSegment>()
        var i = 0
        var j = 0
        while (i < a.size && j < b.size) {
            when {
                a[i] == b[j] -> {
                    segments += DiffSegment(a[i], DiffType.UNCHANGED)
                    i++
                    j++
                }
                dp[i + 1][j] >= dp[i][j + 1] -> {
                    segments += DiffSegment(a[i], DiffType.REMOVED)
                    i++
                }
                else -> {
                    segments += DiffSegment(b[j], DiffType.ADDED)
                    j++
                }
            }
        }
        while (i < a.size) {
            segments += DiffSegment(a[i++], DiffType.REMOVED)
        }
        while (j < b.size) {
            segments += DiffSegment(b[j++], DiffType.ADDED)
        }
        return segments
    }

    private fun resolveOverlaps(raw: List<CorrectionIssue>): List<CorrectionIssue> {
        // Prioriser les règles grammaticales/conjugaison et la plus haute confiance sur un même intervalle
        val sorted = raw.sortedWith(
            compareByDescending<CorrectionIssue> { it.category != CorrectionCategory.STYLE }
                .thenByDescending { it.confidence }
                .thenBy { it.startIndex }
        )
        val accepted = mutableListOf<CorrectionIssue>()
        for (candidate in sorted) {
            val overlaps = accepted.any { existing ->
                candidate.startIndex < existing.endIndex && candidate.endIndex > existing.startIndex
            }
            if (!overlaps) {
                accepted += candidate
            }
        }
        return accepted.sortedBy { it.startIndex }
    }
}
