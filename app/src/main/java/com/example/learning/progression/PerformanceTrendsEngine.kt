package com.example.learning.progression

import com.example.core.database.CorrectionHistory
import com.example.core.database.DictationAttempt
import com.example.core.database.ExerciseAttempt
import com.example.core.database.Lesson
import com.example.core.database.LessonProgress
import com.example.core.database.VocabularyProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Trois grands domaines suivis sur l'aperçu des performances sur 30 jours.
 */
enum class SkillDomain(
    val labelFr: String,
    val sourcesHintFr: String
) {
    GRAMMAR(
        labelFr = "Grammaire",
        sourcesHintFr = "Exercices, orthographe, conjugaison et cours"
    ),
    VOCABULARY(
        labelFr = "Vocabulaire",
        sourcesHintFr = "Révisions SM-2, lexique et compréhension"
    ),
    WRITING(
        labelFr = "Écriture",
        sourcesHintFr = "Correcteur, rédactions et dictées"
    )
}

/**
 * Fenêtre d'analyse sélectionnable sur la page Aperçu des performances.
 */
enum class PerformanceTimeframe(val days: Int, val labelFr: String) {
    DAYS_7(7, "7 j"),
    DAYS_14(14, "14 j"),
    DAYS_30(30, "30 j")
}

/**
 * Point quotidien d'évolution pour les 3 compétences.
 * Un score `null` indique qu'aucune activité n'a encore été enregistrée pour ce domaine jusqu'à ce jour.
 */
data class DailySkillPoint(
    val epochDay: Long,
    val shortDateLabel: String,
    val fullDateLabel: String,
    val grammarScore: Int?,
    val vocabularyScore: Int?,
    val writingScore: Int?,
    val grammarEvents: Int,
    val vocabularyEvents: Int,
    val writingEvents: Int
) {
    val totalEvents: Int get() = grammarEvents + vocabularyEvents + writingEvents

    fun scoreFor(domain: SkillDomain): Int? = when (domain) {
        SkillDomain.GRAMMAR -> grammarScore
        SkillDomain.VOCABULARY -> vocabularyScore
        SkillDomain.WRITING -> writingScore
    }

    fun eventsFor(domain: SkillDomain): Int = when (domain) {
        SkillDomain.GRAMMAR -> grammarEvents
        SkillDomain.VOCABULARY -> vocabularyEvents
        SkillDomain.WRITING -> writingEvents
    }
}

/**
 * Synthèse de tendance d'une compétence sur la période active.
 */
data class SkillTrendSummary(
    val domain: SkillDomain,
    val currentScore: Int?,
    val startScore: Int?,
    val deltaPoints: Int,
    val totalSessions: Int,
    val activeDays: Int
) {
    val hasData: Boolean get() = currentScore != null
}

/**
 * Snapshot complet pour la page Aperçu des performances (7 / 14 / 30 jours).
 */
data class PerformanceOverviewData(
    val timeframe: PerformanceTimeframe,
    val points: List<DailySkillPoint>,
    val grammarSummary: SkillTrendSummary,
    val vocabularySummary: SkillTrendSummary,
    val writingSummary: SkillTrendSummary,
    val overallScore: Int?,
    val overallDelta: Int,
    val activeDaysCount: Int,
    val totalSessionsCount: Int,
    val hasAnyActivity: Boolean
) {
    val summaries: List<SkillTrendSummary>
        get() = listOf(grammarSummary, vocabularySummary, writingSummary)

    fun summaryFor(domain: SkillDomain): SkillTrendSummary = when (domain) {
        SkillDomain.GRAMMAR -> grammarSummary
        SkillDomain.VOCABULARY -> vocabularySummary
        SkillDomain.WRITING -> writingSummary
    }

    val strongestDomain: SkillTrendSummary?
        get() = summaries.filter { it.hasData }.maxByOrNull { it.currentScore ?: -1 }

    val focusDomain: SkillTrendSummary?
        get() = summaries.filter { it.hasData }.minByOrNull { it.currentScore ?: 101 }
}

/**
 * Moteur analytique pur Kotlin qui agrège les données réelles Room des 30 derniers jours
 * pour calculer les courbes de progression en Grammaire, Vocabulaire et Écriture.
 * Aucune donnée fictive n'est générée.
 */
object PerformanceTrendsEngine {

    const val MILLIS_PER_DAY: Long = 86_400_000L

    private data class SkillEvent(
        val domain: SkillDomain,
        val epochDay: Long,
        val scorePercent: Int
    )

    fun compute(
        lessons: List<Lesson>,
        lessonProgress: List<LessonProgress>,
        exerciseAttempts: List<ExerciseAttempt>,
        vocabularyProgress: List<VocabularyProgress>,
        correctionHistory: List<CorrectionHistory>,
        dictationAttempts: List<DictationAttempt>,
        timeframe: PerformanceTimeframe = PerformanceTimeframe.DAYS_30,
        nowMillis: Long = System.currentTimeMillis()
    ): PerformanceOverviewData {
        val todayEpochDay = nowMillis / MILLIS_PER_DAY
        val windowDays = timeframe.days.coerceAtLeast(1)
        val startEpochDay = todayEpochDay - (windowDays - 1)

        val lessonById = lessons.associateBy { it.id }
        val events = buildList {
            // 1. Tentatives d'exercices
            exerciseAttempts.forEach { attempt ->
                if (attempt.attemptedAt > 0L) {
                    val domain = classifyExerciseCategory(attempt.category)
                    add(
                        SkillEvent(
                            domain = domain,
                            epochDay = attempt.attemptedAt / MILLIS_PER_DAY,
                            scorePercent = if (attempt.isCorrect) 100 else 0
                        )
                    )
                }
            }

            // 2. Cours validés (quiz de fin de chapitre)
            lessonProgress.forEach { lp ->
                if (lp.isCompleted && lp.completedAt > 0L) {
                    val theme = lessonById[lp.lessonId]?.theme.orEmpty()
                    val domain = classifyLessonTheme(theme)
                    add(
                        SkillEvent(
                            domain = domain,
                            epochDay = lp.completedAt / MILLIS_PER_DAY,
                            scorePercent = lp.quizScorePercent.coerceIn(0, 100)
                        )
                    )
                }
            }

            // 3. Révisions de vocabulaire (SM-2)
            vocabularyProgress.forEach { vp ->
                if (vp.repetitions > 0 && vp.nextReviewAt > 0L) {
                    val estimatedReviewMillis = (vp.nextReviewAt - vp.intervalDays.coerceAtLeast(1) * MILLIS_PER_DAY)
                        .coerceAtMost(nowMillis)
                    val score = when {
                        vp.lastQuality > 0 -> (vp.lastQuality.coerceIn(1, 5) * 20)
                        vp.mastered -> 90
                        else -> 65
                    }
                    add(
                        SkillEvent(
                            domain = SkillDomain.VOCABULARY,
                            epochDay = estimatedReviewMillis / MILLIS_PER_DAY,
                            scorePercent = score.coerceIn(0, 100)
                        )
                    )
                }
            }

            // 4. Analyses du correcteur & atelier de rédaction
            correctionHistory.forEach { item ->
                if (item.createdAt > 0L) {
                    add(
                        SkillEvent(
                            domain = SkillDomain.WRITING,
                            epochDay = item.createdAt / MILLIS_PER_DAY,
                            scorePercent = computeWritingQualityScore(item)
                        )
                    )
                }
            }

            // 5. Dictées évaluées
            dictationAttempts.forEach { dict ->
                if (dict.attemptedAt > 0L) {
                    val pct = ((dict.scoreOutOf20 / 20f) * 100f).roundToInt().coerceIn(0, 100)
                    add(
                        SkillEvent(
                            domain = SkillDomain.WRITING,
                            epochDay = dict.attemptedAt / MILLIS_PER_DAY,
                            scorePercent = pct
                        )
                    )
                }
            }
        }

        // Ligne de base antérieure à la fenêtre (pour une continuité réaliste si l'apprenant pratiquait avant)
        val runningScore = mutableMapOf<SkillDomain, Float?>()
        SkillDomain.entries.forEach { domain ->
            val priorEvents = events
                .filter { it.domain == domain && it.epochDay < startEpochDay }
                .sortedBy { it.epochDay }
            var acc: Float? = null
            priorEvents.groupBy { it.epochDay }.toSortedMap().values.forEach { dayEvents ->
                val dayAvg = dayEvents.map { it.scorePercent }.average().toFloat()
                val prevAcc = acc
                acc = if (prevAcc == null) dayAvg else (prevAcc * 0.45f + dayAvg * 0.55f)
            }
            runningScore[domain] = acc
        }

        val eventsByDayAndDomain = events
            .filter { it.epochDay in startEpochDay..todayEpochDay }
            .groupBy { it.epochDay to it.domain }

        val shortFmt = SimpleDateFormat("d MMM", Locale.FRANCE)
        val fullFmt = SimpleDateFormat("EEE d MMM", Locale.FRANCE)

        val points = (0 until windowDays).map { offset ->
            val epochDay = startEpochDay + offset
            val dayDate = Date(epochDay * MILLIS_PER_DAY + (MILLIS_PER_DAY / 2L))
            val shortLabel = shortFmt.format(dayDate)
            val fullLabel = fullFmt.format(dayDate).replaceFirstChar { it.titlecase(Locale.FRANCE) }

            val gEvents = eventsByDayAndDomain[epochDay to SkillDomain.GRAMMAR].orEmpty()
            val vEvents = eventsByDayAndDomain[epochDay to SkillDomain.VOCABULARY].orEmpty()
            val wEvents = eventsByDayAndDomain[epochDay to SkillDomain.WRITING].orEmpty()

            updateRunningScore(runningScore, SkillDomain.GRAMMAR, gEvents)
            updateRunningScore(runningScore, SkillDomain.VOCABULARY, vEvents)
            updateRunningScore(runningScore, SkillDomain.WRITING, wEvents)

            DailySkillPoint(
                epochDay = epochDay,
                shortDateLabel = shortLabel,
                fullDateLabel = fullLabel,
                grammarScore = runningScore[SkillDomain.GRAMMAR]?.roundToInt()?.coerceIn(0, 100),
                vocabularyScore = runningScore[SkillDomain.VOCABULARY]?.roundToInt()?.coerceIn(0, 100),
                writingScore = runningScore[SkillDomain.WRITING]?.roundToInt()?.coerceIn(0, 100),
                grammarEvents = gEvents.size,
                vocabularyEvents = vEvents.size,
                writingEvents = wEvents.size
            )
        }

        val grammarSummary = buildDomainSummary(SkillDomain.GRAMMAR, points)
        val vocabularySummary = buildDomainSummary(SkillDomain.VOCABULARY, points)
        val writingSummary = buildDomainSummary(SkillDomain.WRITING, points)

        val availableCurrent = listOfNotNull(
            grammarSummary.currentScore,
            vocabularySummary.currentScore,
            writingSummary.currentScore
        )
        val availableStart = listOfNotNull(
            grammarSummary.startScore,
            vocabularySummary.startScore,
            writingSummary.startScore
        )

        val overallScore = if (availableCurrent.isEmpty()) null else availableCurrent.average().roundToInt()
        val overallStart = if (availableStart.isEmpty()) null else availableStart.average().roundToInt()
        val overallDelta = if (overallScore != null && overallStart != null) overallScore - overallStart else 0
        val activeDaysCount = points.count { it.totalEvents > 0 }
        val totalSessionsCount = points.sumOf { it.totalEvents }
        val hasAnyActivity = totalSessionsCount > 0 || availableCurrent.isNotEmpty()

        return PerformanceOverviewData(
            timeframe = timeframe,
            points = points,
            grammarSummary = grammarSummary,
            vocabularySummary = vocabularySummary,
            writingSummary = writingSummary,
            overallScore = overallScore,
            overallDelta = overallDelta,
            activeDaysCount = activeDaysCount,
            totalSessionsCount = totalSessionsCount,
            hasAnyActivity = hasAnyActivity
        )
    }

    private fun updateRunningScore(
        running: MutableMap<SkillDomain, Float?>,
        domain: SkillDomain,
        dayEvents: List<SkillEvent>
    ) {
        if (dayEvents.isEmpty()) return
        val dayAvg = dayEvents.map { it.scorePercent }.average().toFloat()
        val prev = running[domain]
        running[domain] = if (prev == null) dayAvg else (prev * 0.45f + dayAvg * 0.55f)
    }

    private fun buildDomainSummary(
        domain: SkillDomain,
        points: List<DailySkillPoint>
    ): SkillTrendSummary {
        val nonNullScores = points.mapNotNull { it.scoreFor(domain) }
        val startScore = nonNullScores.firstOrNull()
        val currentScore = nonNullScores.lastOrNull()
        val delta = if (startScore != null && currentScore != null) currentScore - startScore else 0
        val totalSessions = points.sumOf { it.eventsFor(domain) }
        val activeDays = points.count { it.eventsFor(domain) > 0 }
        return SkillTrendSummary(
            domain = domain,
            currentScore = currentScore,
            startScore = startScore,
            deltaPoints = delta,
            totalSessions = totalSessions,
            activeDays = activeDays
        )
    }

    internal fun classifyExerciseCategory(category: String): SkillDomain = when {
        category.contains("Vocab", ignoreCase = true) ||
            category.contains("Lexiq", ignoreCase = true) ||
            category.contains("Compréh", ignoreCase = true) -> SkillDomain.VOCABULARY

        category.contains("Rédac", ignoreCase = true) ||
            category.contains("Écrit", ignoreCase = true) ||
            category.contains("Style", ignoreCase = true) ||
            category.contains("Dictée", ignoreCase = true) ||
            category.contains("Réécriture", ignoreCase = true) -> SkillDomain.WRITING

        else -> SkillDomain.GRAMMAR
    }

    internal fun classifyLessonTheme(theme: String): SkillDomain = when {
        theme.contains("Vocab", ignoreCase = true) ||
            theme.contains("Compréh", ignoreCase = true) -> SkillDomain.VOCABULARY

        theme.contains("Expression", ignoreCase = true) ||
            theme.contains("Écrit", ignoreCase = true) ||
            theme.contains("Style", ignoreCase = true) ||
            theme.contains("professionnel", ignoreCase = true) -> SkillDomain.WRITING

        else -> SkillDomain.GRAMMAR
    }

    internal fun computeWritingQualityScore(history: CorrectionHistory): Int {
        val wordCount = history.originalText
            .trim()
            .split(Regex("\\s+"))
            .count { it.isNotEmpty() }
            .coerceAtLeast(1)
        if (history.errorsFoundCount <= 0) return 100
        val errorsPer100Words = (history.errorsFoundCount.toFloat() / wordCount) * 100f
        val baseScore = (100f - errorsPer100Words * 3.0f).coerceIn(25f, 96f)
        val appliedRatio = (history.errorsAppliedCount.toFloat() / history.errorsFoundCount.coerceAtLeast(1))
            .coerceIn(0f, 1f)
        return (baseScore + appliedRatio * 8f).roundToInt().coerceIn(25, 100)
    }
}
