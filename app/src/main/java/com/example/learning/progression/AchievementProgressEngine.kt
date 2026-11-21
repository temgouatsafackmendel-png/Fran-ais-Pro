package com.example.learning.progression

import com.example.core.database.Achievement
import com.example.core.database.CorrectionHistory
import com.example.core.database.DictationAttempt
import com.example.core.database.ExerciseAttempt
import com.example.core.database.Lesson
import com.example.core.database.LessonProgress
import com.example.core.database.StreakState
import com.example.core.database.UserProfile
import com.example.core.database.VocabularyProgress
import com.example.navigation.Routes

/**
 * État enrichi d'un badge de l'Académie combinant l'entité Room et la progression en direct.
 */
data class AchievementProgressItem(
    val achievement: Achievement,
    val domainLabel: String,
    val current: Int,
    val target: Int,
    val progressRatio: Float,
    val progressText: String,
    val actionHint: String,
    val exerciseCategoryFilter: String? = null,
    val targetRoute: String? = null
)

/**
 * Moteur déterministe 100 % hors ligne d'évaluation des succès & badges de l'Académie.
 * Garantit une cohérence exacte entre les conditions de déblocage Room et les barres
 * de progression affichées dans l'interface.
 */
object AchievementProgressEngine {

    fun qualifyingIds(
        lessons: List<Lesson>,
        lessonProgress: List<LessonProgress>,
        exerciseAttempts: List<ExerciseAttempt>,
        vocabularyProgress: List<VocabularyProgress>,
        correctionHistory: List<CorrectionHistory>,
        dictationAttempts: List<DictationAttempt>,
        profile: UserProfile?,
        streakState: StreakState? = null
    ): Set<String> {
        val unlocked = mutableSetOf<String>()
        val completedLessons = lessonProgress.filter { it.isCompleted }
        val correctAttempts = exerciseAttempts.filter { it.isCorrect }

        if (correctionHistory.isNotEmpty()) {
            unlocked += "ach_first_correction"
        }
        if (completedLessons.isNotEmpty()) {
            unlocked += "ach_first_lesson"
        }
        if (completedLessons.any { it.quizScorePercent >= 100 }) {
            unlocked += "ach_perfect_quiz"
        }
        val roadmap = if (lessons.isNotEmpty()) {
            CoursePathEngine.buildRoadmap(lessons, lessonProgress)
        } else null
        if (roadmap?.levels?.any { it.isCompleted } == true) {
            unlocked += "ach_level_master"
        }

        val grammarCorrect = correctAttempts.count { it.category.equals("Grammaire", ignoreCase = true) }
        if (grammarCorrect >= 3) {
            unlocked += "ach_grammar_guru"
        }

        val vocabOrStyleCorrect = correctAttempts.count {
            it.category.equals("Vocabulaire", ignoreCase = true) ||
                it.category.equals("Style", ignoreCase = true)
        }
        if (vocabularyProgress.size >= 5 || vocabOrStyleCorrect >= 3) {
            unlocked += "ach_vocab_master"
        }

        val orthoCorrect = correctAttempts.count { it.category.equals("Orthographe", ignoreCase = true) }
        val goodDictation = dictationAttempts.any { it.scoreOutOf20 >= 14f }
        if (orthoCorrect >= 2 || goodDictation) {
            unlocked += "ach_orthographe_ace"
        }

        val conjCorrect = correctAttempts.count { it.category.equals("Conjugaison", ignoreCase = true) }
        if (conjCorrect >= 3) {
            unlocked += "ach_conjugation_pro"
        }

        if (correctAttempts.size >= 5) {
            unlocked += "ach_5_exercises"
        }
        if (correctAttempts.size >= 10) {
            unlocked += "ach_10_exercises"
        }

        if (dictationAttempts.isNotEmpty()) {
            unlocked += "ach_first_dictation"
        }
        val activeStreak = maxOf(profile?.streakDays ?: 0, streakState?.currentStreak ?: 0)
        if (activeStreak >= 3) {
            unlocked += "ach_streak_3"
        }
        if (activeStreak >= 7) {
            unlocked += "ach_streak_7"
        }
        return unlocked
    }

    fun evaluateProgress(
        achievements: List<Achievement>,
        lessons: List<Lesson>,
        lessonProgress: List<LessonProgress>,
        exerciseAttempts: List<ExerciseAttempt>,
        vocabularyProgress: List<VocabularyProgress>,
        correctionHistory: List<CorrectionHistory>,
        dictationAttempts: List<DictationAttempt>,
        profile: UserProfile?,
        streakState: StreakState? = null
    ): List<AchievementProgressItem> {
        val completedLessonsCount = lessonProgress.count { it.isCompleted }
        val hasPerfectQuiz = if (lessonProgress.any { it.isCompleted && it.quizScorePercent >= 100 }) 1 else 0
        val roadmap = if (lessons.isNotEmpty()) {
            CoursePathEngine.buildRoadmap(lessons, lessonProgress)
        } else null
        val completedLevelsCount = roadmap?.levels?.count { it.isCompleted } ?: 0

        val correctAttempts = exerciseAttempts.filter { it.isCorrect }
        val totalCorrect = correctAttempts.size
        val grammarCorrect = correctAttempts.count { it.category.equals("Grammaire", ignoreCase = true) }
        val orthoCorrect = correctAttempts.count { it.category.equals("Orthographe", ignoreCase = true) }
        val goodDictationCount = if (dictationAttempts.any { it.scoreOutOf20 >= 14f }) 1 else 0
        val conjCorrect = correctAttempts.count { it.category.equals("Conjugaison", ignoreCase = true) }
        val vocabReviews = vocabularyProgress.size
        val vocabStyleExercises = correctAttempts.count {
            it.category.equals("Vocabulaire", ignoreCase = true) ||
                it.category.equals("Style", ignoreCase = true)
        }
        val streakDays = maxOf(profile?.streakDays ?: 0, streakState?.currentStreak ?: 0).coerceAtLeast(0)

        return achievements.map { ach ->
            val raw: AchievementProgressItem = when (ach.id) {
                "ach_first_lesson" -> item(
                    ach = ach,
                    domainLabel = "Académie",
                    current = completedLessonsCount.coerceAtMost(1),
                    target = 1,
                    unit = "cours",
                    actionHint = "Ouvrir le prochain cours",
                    targetRoute = roadmap?.nextRecommendedLesson?.let { Routes.lessonDetail(it.id) }
                        ?: Routes.lessonList("A1")
                )
                "ach_grammar_guru" -> item(
                    ach = ach,
                    domainLabel = "Grammaire",
                    current = grammarCorrect,
                    target = 3,
                    unit = "exercices",
                    actionHint = "S'entraîner en grammaire",
                    exerciseCategoryFilter = "Grammaire",
                    targetRoute = Routes.EXERCISES
                )
                "ach_vocab_master" -> {
                    val bySm2 = vocabReviews >= 5
                    val byEx = vocabStyleExercises >= 3
                    val cur = when {
                        bySm2 -> 5
                        byEx -> 5
                        vocabReviews * 3 >= vocabStyleExercises * 5 -> vocabReviews.coerceAtMost(5)
                        else -> ((vocabStyleExercises * 5) / 3).coerceAtMost(5)
                    }
                    val tgt = if (vocabStyleExercises > 0 && vocabReviews == 0) 3 else 5
                    val displayCurrent = if (tgt == 3) vocabStyleExercises else vocabReviews
                    AchievementProgressItem(
                        achievement = ach,
                        domainLabel = "Vocabulaire",
                        current = if (ach.isUnlocked) tgt else displayCurrent.coerceAtMost(tgt),
                        target = tgt,
                        progressRatio = if (ach.isUnlocked) 1f else (cur.toFloat() / 5f).coerceIn(0f, 1f),
                        progressText = if (ach.isUnlocked) "Débloqué" else if (tgt == 3) "${displayCurrent.coerceAtMost(3)} / 3 exercices" else "${displayCurrent.coerceAtMost(5)} / 5 mots SM-2",
                        actionHint = "Réviser le vocabulaire",
                        targetRoute = Routes.VOCABULARY
                    )
                }
                "ach_orthographe_ace" -> {
                    val achieved = ach.isUnlocked || orthoCorrect >= 2 || goodDictationCount >= 1
                    val cur = if (achieved) 2 else orthoCorrect.coerceAtMost(2)
                    AchievementProgressItem(
                        achievement = ach,
                        domainLabel = "Orthographe",
                        current = cur,
                        target = 2,
                        progressRatio = if (achieved) 1f else (cur / 2f).coerceIn(0f, 1f),
                        progressText = if (achieved) "Débloqué" else "$cur / 2 exercices",
                        actionHint = "Pratiquer l'orthographe",
                        exerciseCategoryFilter = "Orthographe",
                        targetRoute = Routes.EXERCISES
                    )
                }
                "ach_conjugation_pro" -> item(
                    ach = ach,
                    domainLabel = "Conjugaison",
                    current = conjCorrect,
                    target = 3,
                    unit = "exercices",
                    actionHint = "S'entraîner en conjugaison",
                    exerciseCategoryFilter = "Conjugaison",
                    targetRoute = Routes.EXERCISES
                )
                "ach_5_exercises" -> item(
                    ach = ach,
                    domainLabel = "Exercices",
                    current = totalCorrect,
                    target = 5,
                    unit = "réussis",
                    actionHint = "Lancer un exercice",
                    targetRoute = Routes.EXERCISES
                )
                "ach_10_exercises" -> item(
                    ach = ach,
                    domainLabel = "Exercices",
                    current = totalCorrect,
                    target = 10,
                    unit = "réussis",
                    actionHint = "Continuer les exercices",
                    targetRoute = Routes.EXERCISES
                )
                "ach_perfect_quiz" -> item(
                    ach = ach,
                    domainLabel = "Académie",
                    current = hasPerfectQuiz,
                    target = 1,
                    unit = "quiz 100 %",
                    actionHint = "Viser 100 % au quiz",
                    targetRoute = roadmap?.nextRecommendedLesson?.let { Routes.lessonDetail(it.id) }
                        ?: Routes.lessonList("A1")
                )
                "ach_level_master" -> {
                    val active = roadmap?.levels?.firstOrNull { it.isUnlocked && !it.isCompleted }
                    val lvlCurrent = if (completedLevelsCount > 0 || ach.isUnlocked) {
                        active?.totalCount ?: 4
                    } else {
                        active?.completedCount ?: completedLessonsCount
                    }
                    val lvlTarget = (active?.totalCount ?: 4).coerceAtLeast(1)
                    AchievementProgressItem(
                        achievement = ach,
                        domainLabel = "Parcours CECRL",
                        current = if (ach.isUnlocked || completedLevelsCount > 0) lvlTarget else lvlCurrent.coerceAtMost(lvlTarget),
                        target = lvlTarget,
                        progressRatio = if (ach.isUnlocked || completedLevelsCount > 0) 1f else (lvlCurrent.toFloat() / lvlTarget).coerceIn(0f, 1f),
                        progressText = if (ach.isUnlocked || completedLevelsCount > 0) "Débloqué" else "${lvlCurrent.coerceAtMost(lvlTarget)} / $lvlTarget cours (${active?.level ?: "A1"})",
                        actionHint = "Continuer le palier ${active?.level ?: "A1"}",
                        targetRoute = Routes.lessonList(active?.level ?: "A1")
                    )
                }
                "ach_first_correction" -> item(
                    ach = ach,
                    domainLabel = "Correcteur",
                    current = correctionHistory.size.coerceAtMost(1),
                    target = 1,
                    unit = "analyse",
                    actionHint = "Ouvrir le correcteur",
                    targetRoute = Routes.CORRECTOR
                )
                "ach_first_dictation" -> item(
                    ach = ach,
                    domainLabel = "Dictée",
                    current = dictationAttempts.size.coerceAtMost(1),
                    target = 1,
                    unit = "dictée",
                    actionHint = "Lancer une dictée",
                    targetRoute = Routes.DICTATION
                )
                "ach_streak_3" -> item(
                    ach = ach,
                    domainLabel = "Régularité",
                    current = streakDays,
                    target = 3,
                    unit = "jours",
                    actionHint = "Pratiquer aujourd'hui",
                    targetRoute = Routes.EXERCISES
                )
                "ach_streak_7" -> item(
                    ach = ach,
                    domainLabel = "Régularité",
                    current = streakDays,
                    target = 7,
                    unit = "jours",
                    actionHint = "Maintenir la série",
                    targetRoute = Routes.EXERCISES
                )
                else -> item(
                    ach = ach,
                    domainLabel = "Académie",
                    current = if (ach.isUnlocked) 1 else 0,
                    target = 1,
                    unit = "objectif",
                    actionHint = "Découvrir",
                    targetRoute = Routes.EXERCISES
                )
            }
            raw
        }.sortedWith(
            compareByDescending<AchievementProgressItem> { it.achievement.isUnlocked }
                .thenByDescending { it.progressRatio }
                .thenBy { it.achievement.title }
        )
    }

    private fun item(
        ach: Achievement,
        domainLabel: String,
        current: Int,
        target: Int,
        unit: String,
        actionHint: String,
        exerciseCategoryFilter: String? = null,
        targetRoute: String? = null
    ): AchievementProgressItem {
        val safeTarget = target.coerceAtLeast(1)
        val clamped = if (ach.isUnlocked) safeTarget else current.coerceIn(0, safeTarget)
        val ratio = if (ach.isUnlocked) 1f else (clamped.toFloat() / safeTarget).coerceIn(0f, 1f)
        val text = if (ach.isUnlocked) "Débloqué" else "$clamped / $safeTarget $unit"
        return AchievementProgressItem(
            achievement = ach,
            domainLabel = domainLabel,
            current = clamped,
            target = safeTarget,
            progressRatio = ratio,
            progressText = text,
            actionHint = actionHint,
            exerciseCategoryFilter = exerciseCategoryFilter,
            targetRoute = targetRoute
        )
    }
}
