package com.example.learning.progression

import com.example.core.database.DailyGoal
import com.example.core.database.ExerciseAttempt
import com.example.core.database.LessonProgress
import com.example.navigation.Routes

/**
 * Une mission quotidienne concrète calculée à partir des données locales Room.
 */
data class DailyQuest(
    val id: String,
    val title: String,
    val subtitle: String,
    val current: Int,
    val target: Int,
    val xpReward: Int,
    val targetRoute: String
) {
    val isCompleted: Boolean get() = current >= target
    val progressRatio: Float get() = if (target <= 0) 1f else (current.toFloat() / target).coerceIn(0f, 1f)
}

/**
 * Synthèse des 3 missions quotidiennes inspirées des meilleures applications d'apprentissage.
 */
data class DailyQuestsSummary(
    val quests: List<DailyQuest>,
    val completedCount: Int,
    val totalCount: Int,
    val earnedBonusXp: Int,
    val totalBonusXp: Int
) {
    val completionRatio: Float get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
    val allCompleted: Boolean get() = totalCount > 0 && completedCount == totalCount
}

/**
 * Moteur déterministe des missions quotidiennes (100 % local, alimenté par Room).
 */
object DailyQuestsEngine {

    private const val DAY_MS = 86_400_000L

    fun computeToday(
        lessonProgress: List<LessonProgress>,
        exerciseAttempts: List<ExerciseAttempt>,
        dailyGoals: List<DailyGoal>,
        dailyTargetMinutes: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): DailyQuestsSummary {
        val todayEpoch = nowMillis / DAY_MS
        val startOfDayMs = todayEpoch * DAY_MS
        val endOfDayMs = startOfDayMs + DAY_MS

        val todayGoal = dailyGoals.firstOrNull { it.epochDay == todayEpoch }
        val todayMinutes = todayGoal?.completedMinutes ?: 0
        val safeTargetMinutes = dailyTargetMinutes.coerceAtLeast(5)

        val lessonsCompletedToday = lessonProgress.count { lp ->
            lp.isCompleted && lp.completedAt in startOfDayMs until endOfDayMs
        }
        val exercisesSucceededToday = exerciseAttempts.count { att ->
            att.isCorrect && att.attemptedAt in startOfDayMs until endOfDayMs
        }

        val quests = listOf(
            DailyQuest(
                id = "quest_lesson",
                title = "Valider 1 chapitre",
                subtitle = "Avancez dans votre parcours CECRL",
                current = lessonsCompletedToday.coerceAtMost(1),
                target = 1,
                xpReward = 40,
                targetRoute = Routes.ACADEMY
            ),
            DailyQuest(
                id = "quest_exercises",
                title = "Réussir 3 exercices",
                subtitle = "Renforcez vos automatismes",
                current = exercisesSucceededToday.coerceAtMost(3),
                target = 3,
                xpReward = 30,
                targetRoute = Routes.EXERCISES
            ),
            DailyQuest(
                id = "quest_minutes",
                title = "Pratiquer $safeTargetMinutes min",
                subtitle = "Maintenez votre régularité du jour",
                current = todayMinutes.coerceAtMost(safeTargetMinutes),
                target = safeTargetMinutes,
                xpReward = 30,
                targetRoute = Routes.CORRECTOR
            )
        )

        val completedCount = quests.count { it.isCompleted }
        val earnedXp = quests.filter { it.isCompleted }.sumOf { it.xpReward }
        val totalXp = quests.sumOf { it.xpReward }

        return DailyQuestsSummary(
            quests = quests,
            completedCount = completedCount,
            totalCount = quests.size,
            earnedBonusXp = earnedXp,
            totalBonusXp = totalXp
        )
    }
}
