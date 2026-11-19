package com.example.learning.progression

import com.example.core.database.LearningStatistics

data class AdaptiveRecommendation(
    val title: String,
    val reason: String,
    val targetCategory: String,
    val targetRoute: String
)

data class LinguisticProfileSummary(
    val estimatedLevel: String,
    val levelLabelFr: String,
    val levelDisclaimer: String,
    val globalScore: Int,
    val weakSkills: List<LearningStatistics>,
    val masteredSkills: List<LearningStatistics>,
    val recommendations: List<AdaptiveRecommendation>
)

object AdaptiveProgressionEngine {

    val cefrLabels: Map<String, String> = mapOf(
        "A1" to "Débutant",
        "A2" to "Élémentaire",
        "B1" to "Intermédiaire",
        "B2" to "Intermédiaire avancé",
        "C1" to "Avancé",
        "C2" to "Expert"
    )

    fun estimateLevelFromPlacement(correctCount: Int, totalCount: Int): String {
        val ratio = if (totalCount == 0) 0f else correctCount.toFloat() / totalCount
        return when {
            ratio >= 0.92f -> "C2"
            ratio >= 0.78f -> "C1"
            ratio >= 0.64f -> "B2"
            ratio >= 0.48f -> "B1"
            ratio >= 0.30f -> "A2"
            else -> "A1"
        }
    }

    fun computeProfileSummary(
        currentEstimatedLevel: String,
        completedLessonsCount: Int,
        stats: List<LearningStatistics>
    ): LinguisticProfileSummary {
        val activeStats = stats.filter { it.totalAttempts > 0 }
        val avgMastery = if (activeStats.isEmpty()) 0 else activeStats.map { it.masteryPercent }.average().toInt()
        val globalScore = ((avgMastery * 0.75) + (completedLessonsCount.coerceAtMost(25) * 1.0)).toInt().coerceIn(0, 100)

        val weakSkills = activeStats
            .filter { it.masteryPercent < 70 || it.errorCount >= 2 }
            .sortedBy { it.masteryPercent }
            .take(4)

        val masteredSkills = activeStats
            .filter { it.masteryPercent >= 80 && it.totalAttempts >= 2 }
            .sortedByDescending { it.masteryPercent }

        val recommendations = buildList {
            weakSkills.firstOrNull()?.let { weak ->
                add(
                    AdaptiveRecommendation(
                        title = "Renforcer : ${weak.skillCategory}",
                        reason = "Taux de maîtrise actuel de ${weak.masteryPercent} % (${weak.errorCount} erreur(s) récente(s)).",
                        targetCategory = weak.skillCategory,
                        targetRoute = "exercises"
                    )
                )
            }
            if (completedLessonsCount < 3) {
                add(
                    AdaptiveRecommendation(
                        title = "Poursuivre l'Académie ($currentEstimatedLevel)",
                        reason = "Complétez les leçons fondamentales de votre niveau estimé pour consolider vos bases.",
                        targetCategory = "Académie",
                        targetRoute = "academy"
                    )
                )
            }
            add(
                AdaptiveRecommendation(
                    title = "Session de dictée ciblée",
                    reason = "Ancrez l'orthographe d'usage et les accords en situation réelle hors ligne.",
                    targetCategory = "Dictée",
                    targetRoute = "dictation"
                )
            )
            add(
                AdaptiveRecommendation(
                    title = "Révision du vocabulaire (SM-2)",
                    reason = "Consolidez durablement les mots précis grâce à la répétition espacée.",
                    targetCategory = "Vocabulaire",
                    targetRoute = "vocabulary"
                )
            )
        }.take(3)

        val effectiveLevel = when {
            activeStats.size >= 4 && avgMastery >= 92 && completedLessonsCount >= 10 -> "C2"
            activeStats.size >= 4 && avgMastery >= 84 && completedLessonsCount >= 7 -> "C1"
            activeStats.size >= 3 && avgMastery >= 75 && completedLessonsCount >= 5 -> "B2"
            else -> currentEstimatedLevel
        }

        return LinguisticProfileSummary(
            estimatedLevel = effectiveLevel,
            levelLabelFr = cefrLabels[effectiveLevel] ?: "Débutant",
            levelDisclaimer = "Estimation pédagogique interne inspirée du CECRL (ne constitue pas une certification officielle).",
            globalScore = globalScore,
            weakSkills = weakSkills,
            masteredSkills = masteredSkills,
            recommendations = recommendations
        )
    }
}
