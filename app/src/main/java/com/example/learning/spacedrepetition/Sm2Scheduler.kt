package com.example.learning.spacedrepetition

import com.example.core.database.VocabularyProgress
import kotlin.math.roundToInt

/**
 * Implémentation locale de l'algorithme de répétition espacée SM-2.
 * Qualité de rappel :
 *  - 1 : À revoir immédiatement (oubli)
 *  - 3 : Difficile (rappel avec effort)
 *  - 4 : Bien (rappel correct)
 *  - 5 : Facile (maîtrise fluide)
 */
object Sm2Scheduler {

    private const val DAY_MS = 86_400_000L

    fun schedule(
        current: VocabularyProgress?,
        wordId: String,
        quality: Int,
        nowMs: Long = System.currentTimeMillis()
    ): VocabularyProgress {
        val q = quality.coerceIn(0, 5)
        val prevReps = current?.repetitions ?: 0
        val prevInterval = current?.intervalDays ?: 1
        val prevEase = current?.easeFactor ?: 2.5f
        val prevLapses = current?.lapsesCount ?: 0

        val newEase = (prevEase + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f)))
            .coerceAtLeast(1.3f)

        val (newReps, newInterval, newLapses) = if (q < 3) {
            Triple(0, 1, prevLapses + 1)
        } else {
            val reps = prevReps + 1
            val interval = when (reps) {
                1 -> 1
                2 -> 6
                else -> (prevInterval * newEase).roundToInt().coerceAtLeast(1)
            }
            Triple(reps, interval, prevLapses)
        }

        return VocabularyProgress(
            id = current?.id ?: 0L,
            wordId = wordId,
            repetitions = newReps,
            intervalDays = newInterval,
            easeFactor = newEase,
            nextReviewAt = nowMs + (newInterval * DAY_MS),
            lastQuality = q,
            lapsesCount = newLapses,
            mastered = newReps >= 3 && newInterval >= 12
        )
    }
}
