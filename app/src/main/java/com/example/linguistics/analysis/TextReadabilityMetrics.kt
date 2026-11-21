package com.example.linguistics.analysis

import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Indicateurs de lisibilité et de structure rédactionnelle en temps réel
 * (inspirés des ateliers de correction avancés type Antidote / LanguageTool).
 */
data class TextReadabilityMetrics(
    val wordCount: Int,
    val characterCount: Int,
    val sentenceCount: Int,
    val avgWordsPerSentence: Int,
    val estimatedReadingSeconds: Int,
    val clarityLabel: String
) {
    val readingTimeLabel: String
        get() = when {
            wordCount == 0 -> "0 s"
            estimatedReadingSeconds < 60 -> "${estimatedReadingSeconds} s"
            else -> "${ceil(estimatedReadingSeconds / 60.0).toInt()} min"
        }
}

object TextReadabilityAnalyzer {

    private val wordRegex = Regex("[\\p{L}\\p{N}]+(?:['’-][\\p{L}\\p{N}]+)*")
    private val sentenceSplitRegex = Regex("[.!?…]+")

    fun analyze(text: String): TextReadabilityMetrics {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return TextReadabilityMetrics(
                wordCount = 0,
                characterCount = 0,
                sentenceCount = 0,
                avgWordsPerSentence = 0,
                estimatedReadingSeconds = 0,
                clarityLabel = "En attente"
            )
        }

        val words = wordRegex.findAll(trimmed).count()
        val sentences = sentenceSplitRegex.split(trimmed)
            .map { it.trim() }
            .count { it.isNotEmpty() }
            .coerceAtLeast(if (words > 0) 1 else 0)

        val avgWords = if (sentences == 0) 0 else (words.toDouble() / sentences).roundToInt()
        // Vitesse moyenne de lecture en français : ~200 mots / minute (~3,3 mots / seconde)
        val readingSec = if (words == 0) 0 else ((words * 60.0) / 200.0).roundToInt().coerceAtLeast(3)

        val clarity = when {
            words == 0 -> "En attente"
            avgWords <= 16 -> "Très fluide"
            avgWords <= 24 -> "Équilibré"
            else -> "Dense · à aérer"
        }

        return TextReadabilityMetrics(
            wordCount = words,
            characterCount = text.length,
            sentenceCount = sentences,
            avgWordsPerSentence = avgWords,
            estimatedReadingSeconds = readingSec,
            clarityLabel = clarity
        )
    }
}
