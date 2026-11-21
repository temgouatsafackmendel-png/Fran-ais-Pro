package com.example

import com.example.core.database.CorrectionHistory
import com.example.core.database.DictationAttempt
import com.example.core.database.ExerciseAttempt
import com.example.core.database.Lesson
import com.example.core.database.LessonProgress
import com.example.core.database.VocabularyProgress
import com.example.learning.progression.PerformanceTimeframe
import com.example.learning.progression.PerformanceTrendsEngine
import com.example.learning.progression.SkillDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceTrendsEngineTest {

    private val dayMs = PerformanceTrendsEngine.MILLIS_PER_DAY
    private val fixedNow = 20_500L * dayMs + 12 * 3_600_000L // Midi fixe pour éviter tout souci de fuseau

    private fun sampleLessons(): List<Lesson> = listOf(
        Lesson(
            id = "l_gram",
            level = "A2",
            moduleTitle = "Module Grammaire",
            chapterOrder = 1,
            theme = "Grammaire",
            title = "Accords du participe passé",
            summary = "Règles de base",
            ruleContent = "Contenu",
            examplesJson = "Ex1",
            pitfallsContent = "Pièges"
        ),
        Lesson(
            id = "l_vocab",
            level = "B1",
            moduleTitle = "Module Lexique",
            chapterOrder = 2,
            theme = "Vocabulaire",
            title = "Nuances et synonymes",
            summary = "Enrichir son lexique",
            ruleContent = "Contenu",
            examplesJson = "Ex1",
            pitfallsContent = "Pièges"
        ),
        Lesson(
            id = "l_write",
            level = "B2",
            moduleTitle = "Module Rédaction",
            chapterOrder = 3,
            theme = "Expression écrite",
            title = "Structurer un essai",
            summary = "Cohérence rédactionnelle",
            ruleContent = "Contenu",
            examplesJson = "Ex1",
            pitfallsContent = "Pièges"
        )
    )

    @Test
    fun `empty datasets produce 30 empty buckets without fabricating scores`() {
        val overview = PerformanceTrendsEngine.compute(
            lessons = sampleLessons(),
            lessonProgress = emptyList(),
            exerciseAttempts = emptyList(),
            vocabularyProgress = emptyList(),
            correctionHistory = emptyList(),
            dictationAttempts = emptyList(),
            timeframe = PerformanceTimeframe.DAYS_30,
            nowMillis = fixedNow
        )

        assertEquals(30, overview.points.size)
        assertFalse(overview.hasAnyActivity)
        assertNull(overview.overallScore)
        assertEquals(0, overview.activeDaysCount)
        assertEquals(0, overview.totalSessionsCount)
        assertNull(overview.grammarSummary.currentScore)
        assertNull(overview.vocabularySummary.currentScore)
        assertNull(overview.writingSummary.currentScore)
    }

    @Test
    fun `aggregates grammar vocabulary and writing trends across 30 days with positive deltas`() {
        val lessons = sampleLessons()

        // Grammaire : J-20 à 50 % (1 bon, 1 faux), J-5 leçon validée à 90 %, J-0 à 100 %
        val attempts = listOf(
            ExerciseAttempt(exerciseId = "e1", category = "Grammaire", userAnswer = "a", isCorrect = true, scoreEarned = 10, attemptedAt = fixedNow - 20 * dayMs),
            ExerciseAttempt(exerciseId = "e2", category = "Orthographe", userAnswer = "b", isCorrect = false, scoreEarned = 0, attemptedAt = fixedNow - 20 * dayMs),
            ExerciseAttempt(exerciseId = "e3", category = "Conjugaison", userAnswer = "c", isCorrect = true, scoreEarned = 10, attemptedAt = fixedNow)
        )
        val lessonProgress = listOf(
            LessonProgress(lessonId = "l_gram", isCompleted = true, quizScorePercent = 90, completedAt = fixedNow - 5 * dayMs),
            LessonProgress(lessonId = "l_vocab", isCompleted = true, quizScorePercent = 85, completedAt = fixedNow - 2 * dayMs)
        )

        // Vocabulaire : révision SM-2 il y a 15 jours (qualité 3 -> 60 %) et hier (qualité 5 -> 100 %)
        val vocabProgress = listOf(
            VocabularyProgress(wordId = "v1", repetitions = 2, intervalDays = 3, nextReviewAt = (fixedNow - 15 * dayMs) + 3 * dayMs, lastQuality = 3),
            VocabularyProgress(wordId = "v2", repetitions = 3, intervalDays = 6, nextReviewAt = (fixedNow - 1 * dayMs) + 6 * dayMs, lastQuality = 5, mastered = true)
        )

        // Écriture : dictée il y a 18 jours (12/20 = 60 %), puis correction sans faute aujourd'hui (100 %)
        val dictations = listOf(
            DictationAttempt(dictationId = "d1", userText = "texte", scoreOutOf20 = 12f, mistakesCount = 4, summaryFeedback = "Bien", attemptedAt = fixedNow - 18 * dayMs)
        )
        val corrections = listOf(
            CorrectionHistory(
                originalText = "Ce texte est parfaitement écrit et structuré avec clarté.",
                correctedText = "Ce texte est parfaitement écrit et structuré avec clarté.",
                errorsFoundCount = 0,
                errorsAppliedCount = 0,
                createdAt = fixedNow
            )
        )

        val overview = PerformanceTrendsEngine.compute(
            lessons = lessons,
            lessonProgress = lessonProgress,
            exerciseAttempts = attempts,
            vocabularyProgress = vocabProgress,
            correctionHistory = corrections,
            dictationAttempts = dictations,
            timeframe = PerformanceTimeframe.DAYS_30,
            nowMillis = fixedNow
        )

        assertTrue(overview.hasAnyActivity)
        assertEquals(30, overview.points.size)
        assertNotNull(overview.overallScore)
        assertTrue("Global delta should be positive", overview.overallDelta > 0)

        assertTrue("Grammar delta should be positive", overview.grammarSummary.deltaPoints > 0)
        assertEquals(4, overview.grammarSummary.totalSessions)
        assertEquals(3, overview.grammarSummary.activeDays)

        assertTrue("Vocabulary delta should be positive", overview.vocabularySummary.deltaPoints > 0)
        assertEquals(3, overview.vocabularySummary.totalSessions)

        assertTrue("Writing delta should be positive", overview.writingSummary.deltaPoints > 0)
        assertEquals(2, overview.writingSummary.totalSessions)
        assertEquals(2, overview.writingSummary.activeDays)
    }

    @Test
    fun `timeframe slicing filters session counts while preserving pre-window baseline continuity`() {
        val attempts = listOf(
            // Il y a 20 jours (hors fenêtre 7 j, mais constitue la ligne de base)
            ExerciseAttempt(exerciseId = "e_old", category = "Grammaire", userAnswer = "ok", isCorrect = true, scoreEarned = 10, attemptedAt = fixedNow - 20 * dayMs),
            // Il y a 2 jours (dans la fenêtre 7 j)
            ExerciseAttempt(exerciseId = "e_new", category = "Vocabulaire", userAnswer = "ok", isCorrect = true, scoreEarned = 10, attemptedAt = fixedNow - 2 * dayMs)
        )

        val overview7d = PerformanceTrendsEngine.compute(
            lessons = emptyList(),
            lessonProgress = emptyList(),
            exerciseAttempts = attempts,
            vocabularyProgress = emptyList(),
            correctionHistory = emptyList(),
            dictationAttempts = emptyList(),
            timeframe = PerformanceTimeframe.DAYS_7,
            nowMillis = fixedNow
        )

        assertEquals(7, overview7d.points.size)
        assertEquals(0, overview7d.grammarSummary.totalSessions)
        assertEquals(100, overview7d.grammarSummary.currentScore)
        assertEquals(1, overview7d.vocabularySummary.totalSessions)
        assertEquals(100, overview7d.vocabularySummary.currentScore)
        assertEquals(1, overview7d.totalSessionsCount)
    }

    @Test
    fun `writing quality score rewards fewer errors and applied corrections`() {
        val clean = CorrectionHistory(
            originalText = "Bonjour monsieur, je vous remercie chaleureusement pour votre réponse rapide.",
            correctedText = "Bonjour monsieur, je vous remercie chaleureusement pour votre réponse rapide.",
            errorsFoundCount = 0,
            errorsAppliedCount = 0
        )
        val noisyUnfixed = CorrectionHistory(
            originalText = "Je suis aller au marcher hier soir",
            correctedText = "Je suis aller au marcher hier soir",
            errorsFoundCount = 2,
            errorsAppliedCount = 0
        )
        val noisyFixed = noisyUnfixed.copy(errorsAppliedCount = 2)

        val cleanScore = PerformanceTrendsEngine.computeWritingQualityScore(clean)
        val unfixedScore = PerformanceTrendsEngine.computeWritingQualityScore(noisyUnfixed)
        val fixedScore = PerformanceTrendsEngine.computeWritingQualityScore(noisyFixed)

        assertEquals(100, cleanScore)
        assertTrue(fixedScore > unfixedScore)
        assertTrue(cleanScore > fixedScore)
    }

    @Test
    fun `domain classification maps French curriculum categories accurately`() {
        assertEquals(SkillDomain.GRAMMAR, PerformanceTrendsEngine.classifyExerciseCategory("Orthographe"))
        assertEquals(SkillDomain.GRAMMAR, PerformanceTrendsEngine.classifyExerciseCategory("Conjugaison"))
        assertEquals(SkillDomain.VOCABULARY, PerformanceTrendsEngine.classifyExerciseCategory("Vocabulaire"))
        assertEquals(SkillDomain.WRITING, PerformanceTrendsEngine.classifyExerciseCategory("Réécriture"))
        assertEquals(SkillDomain.WRITING, PerformanceTrendsEngine.classifyLessonTheme("Expression écrite"))
    }
}
