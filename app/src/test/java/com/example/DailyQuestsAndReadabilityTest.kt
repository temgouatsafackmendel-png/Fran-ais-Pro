package com.example

import com.example.core.database.DailyGoal
import com.example.core.database.ExerciseAttempt
import com.example.core.database.LessonProgress
import com.example.learning.progression.DailyQuestsEngine
import com.example.linguistics.analysis.TextReadabilityAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyQuestsAndReadabilityTest {

    @Test
    fun `daily quests engine computes progress strictly for current day and awards XP bonuses`() {
        val now = 20_500L * 86_400_000L + 36_000_000L // milieu de journée
        val todayEpoch = now / 86_400_000L
        val yesterdayMs = now - 86_400_000L

        // Jour vide (une leçon d'hier ne doit pas compter pour aujourd'hui)
        val emptySummary = DailyQuestsEngine.computeToday(
            lessonProgress = listOf(LessonProgress(lessonId = "a1_ort_01", isCompleted = true, quizScorePercent = 90, completedAt = yesterdayMs)),
            exerciseAttempts = emptyList(),
            dailyGoals = emptyList(),
            dailyTargetMinutes = 15,
            nowMillis = now
        )
        assertEquals(0, emptySummary.completedCount)
        assertEquals(3, emptySummary.totalCount)
        assertEquals(0, emptySummary.earnedBonusXp)
        assertEquals(100, emptySummary.totalBonusXp)
        assertFalse(emptySummary.allCompleted)

        // Journée complète : 1 cours, 3 exercices réussis, 15 minutes pratiquées
        val fullSummary = DailyQuestsEngine.computeToday(
            lessonProgress = listOf(LessonProgress(lessonId = "a1_ort_01", isCompleted = true, quizScorePercent = 95, completedAt = now)),
            exerciseAttempts = listOf(
                ExerciseAttempt(exerciseId = "ex_1", category = "Orthographe", userAnswer = "a", isCorrect = true, scoreEarned = 15, attemptedAt = now),
                ExerciseAttempt(exerciseId = "ex_2", category = "Grammaire", userAnswer = "b", isCorrect = true, scoreEarned = 15, attemptedAt = now),
                ExerciseAttempt(exerciseId = "ex_3", category = "Conjugaison", userAnswer = "c", isCorrect = true, scoreEarned = 15, attemptedAt = now)
            ),
            dailyGoals = listOf(DailyGoal(epochDay = todayEpoch, targetMinutes = 15, completedMinutes = 16)),
            dailyTargetMinutes = 15,
            nowMillis = now
        )
        assertEquals(3, fullSummary.completedCount)
        assertEquals(100, fullSummary.earnedBonusXp)
        assertTrue(fullSummary.allCompleted)
        assertTrue(fullSummary.quests.all { it.isCompleted })
    }

    @Test
    fun `text readability analyzer computes words sentences reading time and clarity labels`() {
        val empty = TextReadabilityAnalyzer.analyze("   ")
        assertEquals(0, empty.wordCount)
        assertEquals("En attente", empty.clarityLabel)

        val fluent = TextReadabilityAnalyzer.analyze(
            "Le français est une belle langue. Chaque jour, je progresse avec plaisir."
        )
        assertEquals(12, fluent.wordCount)
        assertEquals(2, fluent.sentenceCount)
        assertEquals("Très fluide", fluent.clarityLabel)
        assertTrue(fluent.estimatedReadingSeconds >= 3)

        val dense = TextReadabilityAnalyzer.analyze(
            "Dans le cadre de la préparation approfondie des examens officiels de maîtrise rédactionnelle, " +
                "il convient d'observer attentivement la structure syntaxique des propositions subordonnées " +
                "afin d'éviter toute lourdeur stylistique susceptible de nuire à la compréhension immédiate du lecteur."
        )
        assertEquals(1, dense.sentenceCount)
        assertTrue(dense.avgWordsPerSentence > 24)
        assertEquals("Dense · à aérer", dense.clarityLabel)
    }
}
