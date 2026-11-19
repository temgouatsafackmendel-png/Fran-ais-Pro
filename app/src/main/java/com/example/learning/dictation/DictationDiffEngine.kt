package com.example.learning.dictation

import com.example.linguistics.model.FrenchNormalizer

enum class DictationMistakeType(val labelFr: String) {
    OK("Exact"),
    ACCENT_OR_CASE("Accent / Majuscule"),
    SPELLING("Orthographe / Accord"),
    OMISSION("Mot manquant"),
    ADDITION("Mot ajouté")
}

data class DictationWordDiff(
    val expected: String?,
    val actual: String?,
    val type: DictationMistakeType,
    val note: String
)

data class DictationEvaluation(
    val scoreOutOf20: Float,
    val mistakesCount: Int,
    val exactWordsCount: Int,
    val totalReferenceWords: Int,
    val diffs: List<DictationWordDiff>,
    val summaryFeedback: String
)

object DictationDiffEngine {

    private val wordSplitter = Regex("""\s+""")

    fun evaluate(referenceText: String, userText: String): DictationEvaluation {
        val refWords = referenceText.trim().split(wordSplitter).filter { it.isNotEmpty() }
        val userWords = userText.trim().split(wordSplitter).filter { it.isNotEmpty() }

        if (refWords.isEmpty()) {
            return DictationEvaluation(20f, 0, 0, 0, emptyList(), "Dictée vide.")
        }

        // LCS sur les mots normalisés (insensible aux accents pour aligner les erreurs d'accents proprement)
        val n = refWords.size
        val m = userWords.size
        val dp = Array(n + 1) { IntArray(m + 1) }

        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                dp[i][j] = if (fuzzyMatch(refWords[i], userWords[j])) {
                    dp[i + 1][j + 1] + 1
                } else {
                    maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }

        val diffs = mutableListOf<DictationWordDiff>()
        var i = 0
        var j = 0
        var penaltyPoints = 0f
        var exactCount = 0
        var mistakesCount = 0

        while (i < n && j < m) {
            val r = refWords[i]
            val u = userWords[j]
            when {
                r == u -> {
                    diffs += DictationWordDiff(r, u, DictationMistakeType.OK, "Mot exact")
                    exactCount++
                    i++
                    j++
                }
                fuzzyMatch(r, u) -> {
                    val isAccentOrCaseOnly = FrenchNormalizer.stripAccents(cleanPunct(r)) ==
                        FrenchNormalizer.stripAccents(cleanPunct(u))
                    val type = if (isAccentOrCaseOnly) DictationMistakeType.ACCENT_OR_CASE else DictationMistakeType.SPELLING
                    val penalty = if (isAccentOrCaseOnly) 0.5f else 1.0f
                    penaltyPoints += penalty
                    mistakesCount++
                    val note = if (isAccentOrCaseOnly) {
                        "Vérifiez l'accent, la majuscule ou la ponctuation : attendu « $r »."
                    } else {
                        "Orthographe ou accord incorrect : attendu « $r » au lieu de « $u »."
                    }
                    diffs += DictationWordDiff(r, u, type, note)
                    i++
                    j++
                }
                dp[i + 1][j] >= dp[i][j + 1] -> {
                    penaltyPoints += 1.0f
                    mistakesCount++
                    diffs += DictationWordDiff(r, null, DictationMistakeType.OMISSION, "Mot oublié : « $r ».")
                    i++
                }
                else -> {
                    penaltyPoints += 0.5f
                    mistakesCount++
                    diffs += DictationWordDiff(null, u, DictationMistakeType.ADDITION, "Mot en trop : « $u ».")
                    j++
                }
            }
        }

        while (i < n) {
            penaltyPoints += 1.0f
            mistakesCount++
            diffs += DictationWordDiff(refWords[i], null, DictationMistakeType.OMISSION, "Mot oublié : « ${refWords[i]} ».")
            i++
        }
        while (j < m) {
            penaltyPoints += 0.5f
            mistakesCount++
            diffs += DictationWordDiff(null, userWords[j], DictationMistakeType.ADDITION, "Mot en trop : « ${userWords[j]} ».")
            j++
        }

        val score = (20f - (penaltyPoints * (20f / refWords.size.coerceAtLeast(10)) * 2f))
            .coerceIn(0f, 20f)
        val roundedScore = (score * 2f).toInt() / 2f

        val feedback = when {
            mistakesCount == 0 -> "Excellent travail ! Aucune faute détectée sur l'ensemble de la dictée."
            roundedScore >= 16f -> "Très bonne maîtrise. Relisez attentivement les rares écarts d'accents ou d'accords."
            roundedScore >= 12f -> "Bon ensemble. Travaillez les points signalés en rouge pour gagner en précision."
            else -> "Poursuivez l'entraînement : révisez les règles clés de cette dictée puis réessayez."
        }

        return DictationEvaluation(
            scoreOutOf20 = roundedScore,
            mistakesCount = mistakesCount,
            exactWordsCount = exactCount,
            totalReferenceWords = refWords.size,
            diffs = diffs,
            summaryFeedback = feedback
        )
    }

    private fun cleanPunct(w: String): String = w.trim('.', ',', ';', ':', '!', '?', '«', '»', '"', '(', ')')

    private fun fuzzyMatch(a: String, b: String): Boolean {
        val ca = FrenchNormalizer.stripAccents(cleanPunct(a))
        val cb = FrenchNormalizer.stripAccents(cleanPunct(b))
        if (ca == cb) return true
        if (ca.length >= 4 && cb.length >= 4) {
            val commonPrefix = ca.zip(cb).takeWhile { it.first == it.second }.count()
            if (commonPrefix >= maxOf(ca.length, cb.length) - 2) return true
        }
        return false
    }
}
