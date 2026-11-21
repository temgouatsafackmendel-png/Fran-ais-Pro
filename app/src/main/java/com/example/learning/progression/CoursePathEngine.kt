package com.example.learning.progression

import com.example.core.database.Lesson
import com.example.core.database.LessonProgress

/**
 * Statut d'accès progressif d'un chapitre dans le parcours CECRL.
 */
enum class LessonUnlockStatus {
    COMPLETED,
    CURRENT,
    LOCKED
}

/**
 * État structuré d'un chapitre dans son niveau.
 */
data class LessonStepState(
    val lesson: Lesson,
    val chapterNumber: Int,
    val totalInLevel: Int,
    val status: LessonUnlockStatus,
    val scorePercent: Int?,
    val lockReason: String?
)

/**
 * État d'un niveau CECRL complet (A1 → C2) avec condition de déblocage explicite.
 */
data class LevelRoadmapState(
    val level: String,
    val title: String,
    val subtitle: String,
    val difficultyLabel: String,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val completedCount: Int,
    val totalCount: Int,
    val progressRatio: Float,
    val xpEarned: Int,
    val maxXp: Int,
    val lockReason: String?,
    val nextUnlockedLesson: Lesson?,
    val skillsPreview: List<String>,
    val steps: List<LessonStepState>
)

/**
 * Vue consolidée du parcours progressif A1 → C2 de l'apprenant.
 */
data class CurriculumRoadmap(
    val activeLevel: String,
    val unlockedLevelsCount: Int,
    val totalLevelsCount: Int,
    val completedLessonsCount: Int,
    val totalLessonsCount: Int,
    val totalXp: Int,
    val overallProgressRatio: Float,
    val nextRecommendedLesson: Lesson?,
    val levels: List<LevelRoadmapState>
)

/**
 * Moteur déterministe de déblocage progressif des niveaux CECRL (A1 → C2)
 * et des chapitres séquentiels au sein de chaque niveau.
 */
object CoursePathEngine {

    const val QUIZ_PASS_THRESHOLD_PERCENT = 60
    const val XP_PER_LESSON = 50
    const val XP_EXCELLENCE_BONUS = 20
    const val XP_LEVEL_MILESTONE = 100

    private data class LevelMeta(
        val code: String,
        val title: String,
        val subtitle: String,
        val difficultyLabel: String
    )

    private val levelCatalog = listOf(
        LevelMeta("A1", "Débutant", "Accents, déterminants, présent et phrase simple", "Fondamentaux"),
        LevelMeta("A2", "Élémentaire", "Homophones, passé composé, imparfait et ponctuation", "Consolidation"),
        LevelMeta("B1", "Intermédiaire", "Participe passé, subjonctif, connecteurs et discours rapporté", "Autonomie"),
        LevelMeta("B2", "Intermédiaire avancé", "Hypothèses, argumentation, français pro et voix passive", "Maîtrise avancée"),
        LevelMeta("C1", "Avancé", "Relatifs composés, paronymes, nuance et note de synthèse", "Supérieur"),
        LevelMeta("C2", "Expert", "Accords complexes, liaisons, rhétorique et élégance éditoriale", "Excellence")
    )

    val cefrOrder: List<String> = levelCatalog.map { it.code }

    fun buildRoadmap(
        lessons: List<Lesson>,
        progress: List<LessonProgress>
    ): CurriculumRoadmap {
        val progressByLessonId = progress.associateBy { it.lessonId }
        val lessonsByLevel = lessons
            .groupBy { it.level }
            .mapValues { (_, list) -> list.sortedBy { it.chapterOrder } }

        val levelStates = mutableListOf<LevelRoadmapState>()
        var previousLevelCompleted = true
        var previousLevelCode = ""
        var previousLevelRemaining = 0

        for ((index, meta) in levelCatalog.withIndex()) {
            val levelLessons = lessonsByLevel[meta.code].orEmpty()
            val completedCount = levelLessons.count { progressByLessonId[it.id]?.isCompleted == true }
            val totalCount = levelLessons.size
            val isCompleted = totalCount > 0 && completedCount == totalCount

            // Un niveau est débloqué si c'est A1, ou si le niveau précédent est validé,
            // ou si l'apprenant a déjà validé au moins une leçon dans ce niveau ou un niveau supérieur.
            val hasAnyProgressAtOrAbove = levelCatalog.drop(index).any { higher ->
                lessonsByLevel[higher.code].orEmpty().any { progressByLessonId[it.id]?.isCompleted == true }
            }
            val isUnlocked = index == 0 || previousLevelCompleted || hasAnyProgressAtOrAbove

            val lockReason = if (isUnlocked) {
                null
            } else {
                val plural = if (previousLevelRemaining > 1) "cours" else "cours"
                "Terminez encore $previousLevelRemaining $plural du niveau $previousLevelCode pour débloquer ${meta.code}."
            }

            var currentMarked = false
            val steps = levelLessons.mapIndexed { stepIdx, lesson ->
                val lp = progressByLessonId[lesson.id]
                val lessonDone = lp?.isCompleted == true
                val prevLesson = if (stepIdx > 0) levelLessons[stepIdx - 1] else null
                val prevLessonDone = prevLesson == null || progressByLessonId[prevLesson.id]?.isCompleted == true

                val status = when {
                    lessonDone -> LessonUnlockStatus.COMPLETED
                    isUnlocked && prevLessonDone && !currentMarked -> {
                        currentMarked = true
                        LessonUnlockStatus.CURRENT
                    }
                    else -> LessonUnlockStatus.LOCKED
                }

                val stepLockReason = when (status) {
                    LessonUnlockStatus.LOCKED -> {
                        if (!isUnlocked) {
                            lockReason
                        } else {
                            "Validez d'abord le chapitre $stepIdx (« ${prevLesson?.title.orEmpty()} ») pour débloquer ce cours."
                        }
                    }
                    else -> null
                }

                LessonStepState(
                    lesson = lesson,
                    chapterNumber = stepIdx + 1,
                    totalInLevel = totalCount,
                    status = status,
                    scorePercent = lp?.quizScorePercent,
                    lockReason = stepLockReason
                )
            }

            val xpFromLessons = levelLessons.sumOf { lesson ->
                val lp = progressByLessonId[lesson.id]
                if (lp?.isCompleted == true) {
                    XP_PER_LESSON + if (lp.quizScorePercent >= 90) XP_EXCELLENCE_BONUS else 0
                } else 0
            }
            val xpEarned = xpFromLessons + if (isCompleted) XP_LEVEL_MILESTONE else 0
            val maxXp = (totalCount * (XP_PER_LESSON + XP_EXCELLENCE_BONUS)) + XP_LEVEL_MILESTONE
            val ratio = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount

            val nextUnlocked = steps.firstOrNull { it.status == LessonUnlockStatus.CURRENT }?.lesson
                ?: if (isUnlocked) levelLessons.firstOrNull() else null

            levelStates += LevelRoadmapState(
                level = meta.code,
                title = meta.title,
                subtitle = meta.subtitle,
                difficultyLabel = meta.difficultyLabel,
                isUnlocked = isUnlocked,
                isCompleted = isCompleted,
                completedCount = completedCount,
                totalCount = totalCount,
                progressRatio = ratio,
                xpEarned = xpEarned,
                maxXp = maxXp,
                lockReason = lockReason,
                nextUnlockedLesson = nextUnlocked,
                skillsPreview = levelLessons.map { it.theme }.distinct().take(4),
                steps = steps
            )

            previousLevelCompleted = isCompleted
            previousLevelCode = meta.code
            previousLevelRemaining = (totalCount - completedCount).coerceAtLeast(1)
        }

        val activeLevelState = levelStates.firstOrNull { it.isUnlocked && !it.isCompleted }
            ?: levelStates.lastOrNull { it.isUnlocked }
            ?: levelStates.first()

        val totalLessons = levelStates.sumOf { it.totalCount }
        val completedLessons = levelStates.sumOf { it.completedCount }
        val totalXp = levelStates.sumOf { it.xpEarned }
        val overallRatio = if (totalLessons == 0) 0f else completedLessons.toFloat() / totalLessons
        val nextRecommended = levelStates
            .flatMap { it.steps }
            .firstOrNull { it.status == LessonUnlockStatus.CURRENT }
            ?.lesson
            ?: activeLevelState.nextUnlockedLesson

        return CurriculumRoadmap(
            activeLevel = activeLevelState.level,
            unlockedLevelsCount = levelStates.count { it.isUnlocked },
            totalLevelsCount = levelStates.size,
            completedLessonsCount = completedLessons,
            totalLessonsCount = totalLessons,
            totalXp = totalXp,
            overallProgressRatio = overallRatio,
            nextRecommendedLesson = nextRecommended,
            levels = levelStates
        )
    }

    fun isLevelUnlocked(
        level: String,
        lessons: List<Lesson>,
        progress: List<LessonProgress>
    ): Boolean {
        if (level == "ALL") return true
        return buildRoadmap(lessons, progress)
            .levels
            .firstOrNull { it.level == level }
            ?.isUnlocked ?: false
    }

    fun getLessonStepState(
        lessonId: String,
        lessons: List<Lesson>,
        progress: List<LessonProgress>
    ): LessonStepState? = buildRoadmap(lessons, progress)
        .levels
        .flatMap { it.steps }
        .firstOrNull { it.lesson.id == lessonId }

    fun nextLevelCode(currentLevel: String): String? {
        val idx = cefrOrder.indexOf(currentLevel)
        return if (idx >= 0 && idx + 1 < cefrOrder.size) cefrOrder[idx + 1] else null
    }
}
