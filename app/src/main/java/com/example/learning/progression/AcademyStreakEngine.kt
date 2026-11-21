package com.example.learning.progression

import com.example.core.database.AcademyPracticeDay
import com.example.core.database.StreakReward
import com.example.core.database.StreakState
import java.util.Calendar

enum class AcademyPracticeSource {
    LESSON,
    EXERCISE,
    VOCABULARY,
    DICTATION
}

enum class StreakHealthStatus {
    PRACTICED_TODAY,
    AT_RISK_TODAY,
    FREEZE_READY,
    INACTIVE_OR_NEW
}

data class StreakDayDot(
    val epochDay: Long,
    val dayInitial: String,
    val isToday: Boolean,
    val practiced: Boolean,
    val freezeUsed: Boolean,
    val sessionsCount: Int
)

data class AcademyStreakSummary(
    val currentStreak: Int,
    val bestStreak: Int,
    val freezeTokensAvailable: Int,
    val totalBonusXpEarned: Int,
    val totalAcademySessions: Int,
    val practicedToday: Boolean,
    val status: StreakHealthStatus,
    val statusMessage: String,
    val nextReward: StreakReward?,
    val daysToNextReward: Int,
    val nextRewardProgressRatio: Float,
    val claimableRewards: List<StreakReward>,
    val rewards: List<StreakReward>,
    val last7Days: List<StreakDayDot>
)

data class StreakUpdateOutcome(
    val updatedState: StreakState,
    val updatedDay: AcademyPracticeDay,
    val freezeBridgedDay: AcademyPracticeDay?,
    val newlyUnlockedRewards: List<StreakReward>
)

/**
 * Moteur déterministe 100 % hors ligne de suivi des séries quotidiennes et des récompenses
 * pour les sessions de pratique dans le module Académie (leçons, exercices, vocabulaire SM-2, dictées).
 */
object AcademyStreakEngine {

    const val DAY_MS = 86_400_000L

    fun defaultRewards(): List<StreakReward> = listOf(
        StreakReward(
            id = "streak_reward_3",
            requiredStreakDays = 3,
            title = "Élan de 3 jours",
            description = "Pratiquer 3 jours de suite à l'Académie débloque un bonus XP et un bouclier.",
            bonusXp = 50,
            freezeTokensGranted = 1
        ),
        StreakReward(
            id = "streak_reward_7",
            requiredStreakDays = 7,
            title = "Semaine d'or",
            description = "7 jours consécutifs d'entraînement récompensés par 120 XP et 1 bouclier.",
            bonusXp = 120,
            freezeTokensGranted = 1
        ),
        StreakReward(
            id = "streak_reward_14",
            requiredStreakDays = 14,
            title = "Quinzaine d'excellence",
            description = "14 jours de régularité constante pour ancrer durablement vos acquis.",
            bonusXp = 250,
            freezeTokensGranted = 2
        ),
        StreakReward(
            id = "streak_reward_30",
            requiredStreakDays = 30,
            title = "Maître de la régularité",
            description = "30 jours de pratique à l'Académie : votre discipline force l'admiration.",
            bonusXp = 500,
            freezeTokensGranted = 2
        )
    )

    fun recordAcademyPractice(
        currentState: StreakState,
        existingToday: AcademyPracticeDay?,
        existingRewards: List<StreakReward>,
        source: AcademyPracticeSource,
        xpDelta: Int,
        epochDay: Long,
        nowMillis: Long
    ): StreakUpdateOutcome {
        val baseDay = existingToday ?: AcademyPracticeDay(epochDay = epochDay)
        val updatedDay = when (source) {
            AcademyPracticeSource.LESSON -> baseDay.copy(
                lessonsCompletedCount = baseDay.lessonsCompletedCount + 1,
                xpEarned = baseDay.xpEarned + xpDelta,
                streakQualifies = true,
                updatedAt = nowMillis
            )
            AcademyPracticeSource.EXERCISE -> baseDay.copy(
                exercisesCompletedCount = baseDay.exercisesCompletedCount + 1,
                xpEarned = baseDay.xpEarned + xpDelta,
                streakQualifies = true,
                updatedAt = nowMillis
            )
            AcademyPracticeSource.VOCABULARY -> baseDay.copy(
                vocabularyReviewsCount = baseDay.vocabularyReviewsCount + 1,
                xpEarned = baseDay.xpEarned + xpDelta,
                streakQualifies = true,
                updatedAt = nowMillis
            )
            AcademyPracticeSource.DICTATION -> baseDay.copy(
                dictationsCompletedCount = baseDay.dictationsCompletedCount + 1,
                xpEarned = baseDay.xpEarned + xpDelta,
                streakQualifies = true,
                updatedAt = nowMillis
            )
        }

        val lastDay = currentState.lastPracticeEpochDay
        var freezeTokens = currentState.freezeTokensAvailable
        var freezeBridgedDay: AcademyPracticeDay? = null

        val newStreak = when {
            lastDay == epochDay -> currentState.currentStreak.coerceAtLeast(1)
            lastDay == epochDay - 1L && currentState.currentStreak > 0 -> currentState.currentStreak + 1
            lastDay == epochDay - 2L && currentState.currentStreak > 0 && freezeTokens > 0 -> {
                // Sauvegarde automatique de la série via un bouclier pour la journée manquée d'hier
                freezeTokens -= 1
                freezeBridgedDay = AcademyPracticeDay(
                    epochDay = epochDay - 1L,
                    streakQualifies = true,
                    isFreezeUsed = true,
                    updatedAt = nowMillis
                )
                currentState.currentStreak + 1
            }
            else -> 1
        }

        val updatedState = currentState.copy(
            currentStreak = newStreak,
            bestStreak = maxOf(currentState.bestStreak, newStreak),
            lastPracticeEpochDay = maxOf(currentState.lastPracticeEpochDay, epochDay),
            freezeTokensAvailable = freezeTokens,
            totalAcademySessions = currentState.totalAcademySessions + 1
        )

        val newlyUnlocked = existingRewards.mapNotNull { reward ->
            if (!reward.isUnlocked && newStreak >= reward.requiredStreakDays) {
                reward.copy(isUnlocked = true, unlockedAt = nowMillis)
            } else {
                null
            }
        }

        return StreakUpdateOutcome(
            updatedState = updatedState,
            updatedDay = updatedDay,
            freezeBridgedDay = freezeBridgedDay,
            newlyUnlockedRewards = newlyUnlocked
        )
    }

    fun buildSummary(
        streakState: StreakState?,
        practiceDays: List<AcademyPracticeDay>,
        rewards: List<StreakReward>,
        nowMillis: Long = System.currentTimeMillis()
    ): AcademyStreakSummary {
        val state = streakState ?: StreakState()
        val todayEpoch = nowMillis / DAY_MS
        val dayMap = practiceDays.associateBy { it.epochDay }
        val sortedRewards = (if (rewards.isEmpty()) defaultRewards() else rewards)
            .sortedBy { it.requiredStreakDays }

        val practicedToday = state.lastPracticeEpochDay == todayEpoch ||
            (dayMap[todayEpoch]?.let { it.streakQualifies && !it.isFreezeUsed } == true)

        val gapDays = if (state.lastPracticeEpochDay <= 0L) Long.MAX_VALUE
        else (todayEpoch - state.lastPracticeEpochDay).coerceAtLeast(0L)

        val effectiveStreak: Int
        val healthStatus: StreakHealthStatus
        val statusMsg: String

        when {
            practicedToday -> {
                effectiveStreak = state.currentStreak.coerceAtLeast(1)
                healthStatus = StreakHealthStatus.PRACTICED_TODAY
                statusMsg = "Session du jour validée · Série maintenue !"
            }
            state.currentStreak > 0 && gapDays == 1L -> {
                effectiveStreak = state.currentStreak
                healthStatus = StreakHealthStatus.AT_RISK_TODAY
                statusMsg = "Pratiquez aujourd'hui pour prolonger votre série à ${effectiveStreak + 1} jours."
            }
            state.currentStreak > 0 && gapDays == 2L && state.freezeTokensAvailable > 0 -> {
                effectiveStreak = state.currentStreak
                healthStatus = StreakHealthStatus.FREEZE_READY
                statusMsg = "1 bouclier prêt : pratiquez aujourd'hui pour sauver votre série de $effectiveStreak jours."
            }
            else -> {
                effectiveStreak = 0
                healthStatus = StreakHealthStatus.INACTIVE_OR_NEW
                statusMsg = "Terminez une leçon, un exercice ou 1 carte SM-2 pour lancer votre série."
            }
        }

        val nextReward = sortedRewards.firstOrNull { !it.isUnlocked }
            ?: sortedRewards.firstOrNull { !it.isClaimed }
        val daysRemaining = if (nextReward != null) {
            (nextReward.requiredStreakDays - effectiveStreak).coerceAtLeast(0)
        } else 0
        val ratio = if (nextReward != null && nextReward.requiredStreakDays > 0) {
            (effectiveStreak.toFloat() / nextReward.requiredStreakDays).coerceIn(0f, 1f)
        } else 1f

        val claimable = sortedRewards.filter { it.isUnlocked && !it.isClaimed }

        val last7Days = (6 downTo 0).map { daysAgo ->
            val epoch = todayEpoch - daysAgo
            val record = dayMap[epoch]
            val sessions = if (record == null) 0 else {
                record.lessonsCompletedCount +
                    record.exercisesCompletedCount +
                    record.vocabularyReviewsCount +
                    record.dictationsCompletedCount
            }
            StreakDayDot(
                epochDay = epoch,
                dayInitial = frenchDayInitial(epoch),
                isToday = daysAgo == 0,
                practiced = record?.let { it.streakQualifies && !it.isFreezeUsed } == true,
                freezeUsed = record?.isFreezeUsed == true,
                sessionsCount = sessions
            )
        }

        return AcademyStreakSummary(
            currentStreak = effectiveStreak,
            bestStreak = maxOf(state.bestStreak, effectiveStreak),
            freezeTokensAvailable = state.freezeTokensAvailable,
            totalBonusXpEarned = state.totalBonusXpEarned,
            totalAcademySessions = state.totalAcademySessions,
            practicedToday = practicedToday,
            status = healthStatus,
            statusMessage = statusMsg,
            nextReward = nextReward,
            daysToNextReward = daysRemaining,
            nextRewardProgressRatio = ratio,
            claimableRewards = claimable,
            rewards = sortedRewards,
            last7Days = last7Days
        )
    }

    private fun frenchDayInitial(epochDay: Long): String {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochDay * DAY_MS + 12 * 3_600_000L
        }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "L"
            Calendar.TUESDAY -> "M"
            Calendar.WEDNESDAY -> "M"
            Calendar.THURSDAY -> "J"
            Calendar.FRIDAY -> "V"
            Calendar.SATURDAY -> "S"
            Calendar.SUNDAY -> "D"
            else -> "·"
        }
    }
}
