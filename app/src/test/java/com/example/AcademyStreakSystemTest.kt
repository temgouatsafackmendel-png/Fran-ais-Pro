package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.backup.BackupManager
import com.example.core.backup.BackupResult
import com.example.core.database.DatabaseSeeder
import com.example.core.database.FrancaisProDatabase
import com.example.core.database.StreakReward
import com.example.core.datastore.PreferencesRepository
import com.example.data.FrancaisProRepository
import com.example.learning.progression.AcademyPracticeSource
import com.example.learning.progression.AcademyStreakEngine
import com.example.learning.progression.StreakHealthStatus
import com.example.linguistics.analysis.HybridCorrectionEngine
import com.example.linguistics.conjugation.FrenchConjugatorEngine
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AcademyStreakSystemTest {

    private lateinit var context: Context
    private lateinit var db: FrancaisProDatabase
    private lateinit var repository: FrancaisProRepository
    private val dayMs = AcademyStreakEngine.DAY_MS

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, FrancaisProDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val conjugator = FrenchConjugatorEngine()
        val seeder = DatabaseSeeder(db, conjugator)
        kotlinx.coroutines.runBlocking {
            seeder.seedIfNeeded()
        }
        repository = FrancaisProRepository(
            db = db,
            prefs = PreferencesRepository(context),
            correctionEngine = HybridCorrectionEngine(),
            conjugatorEngine = conjugator,
            backupManager = BackupManager(db)
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun millisForEpochDay(epochDay: Long): Long = epochDay * dayMs + 10 * 3_600_000L

    @Test
    fun `consecutive academy practice increments streak unlocks milestone reward and emits event`() = runTest {
        val unlockedRewards = mutableListOf<StreakReward>()
        val job = launch {
            repository.unlockedStreakRewardEvents.collect { unlockedRewards += it }
        }

        // Jour 100 -> 101 -> 102
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_000L))
        repository.recordAcademyPracticeSession(AcademyPracticeSource.EXERCISE, 15, millisForEpochDay(20_001L))
        val day3Reward = repository.recordAcademyPracticeSession(AcademyPracticeSource.VOCABULARY, 15, millisForEpochDay(20_002L))

        val state = db.academyStreakDao().getStreakState()!!
        assertEquals(3, state.currentStreak)
        assertEquals(3, state.bestStreak)
        assertEquals(3, state.totalAcademySessions)
        assertEquals(20_002L, state.lastPracticeEpochDay)

        assertNotNull(day3Reward)
        assertEquals("streak_reward_3", day3Reward!!.id)
        assertTrue(unlockedRewards.any { it.id == "streak_reward_3" })

        val rewardsSnapshot = db.academyStreakDao().getStreakRewardsSnapshot()
        val reward3 = rewardsSnapshot.first { it.id == "streak_reward_3" }
        assertTrue(reward3.isUnlocked)
        assertFalse(reward3.isClaimed)

        // Déblocage du badge ach_streak_3 lié à la série Académie
        repository.evaluateAndUnlockAchievements()
        val badges = db.statsAndMetaDao().getAchievementsSnapshot()
        assertTrue(badges.first { it.id == "ach_streak_3" }.isUnlocked)

        job.cancel()
    }

    @Test
    fun `multiple sessions on the same day accumulate xp and counters without double counting streak`() = runTest {
        val dayMillis = millisForEpochDay(20_010L)
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, dayMillis)
        repository.recordAcademyPracticeSession(AcademyPracticeSource.EXERCISE, 15, dayMillis + 1_000L)
        repository.recordAcademyPracticeSession(AcademyPracticeSource.VOCABULARY, 15, dayMillis + 2_000L)
        repository.recordAcademyPracticeSession(AcademyPracticeSource.DICTATION, 30, dayMillis + 3_000L)

        val state = db.academyStreakDao().getStreakState()!!
        assertEquals(1, state.currentStreak)
        assertEquals(4, state.totalAcademySessions)

        val dayRecord = db.academyStreakDao().getPracticeDay(20_010L)!!
        assertEquals(1, dayRecord.lessonsCompletedCount)
        assertEquals(1, dayRecord.exercisesCompletedCount)
        assertEquals(1, dayRecord.vocabularyReviewsCount)
        assertEquals(1, dayRecord.dictationsCompletedCount)
        assertEquals(85, dayRecord.xpEarned)
    }

    @Test
    fun `missing one day automatically spends freeze shield and bridges gap cleanly`() = runTest {
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_020L))
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_021L))
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_022L))

        val beforeFreeze = db.academyStreakDao().getStreakState()!!
        assertEquals(1, beforeFreeze.freezeTokensAvailable)
        assertEquals(3, beforeFreeze.currentStreak)

        // Saut du jour 20_023, reprise le jour 20_024 -> le bouclier protège la série
        repository.recordAcademyPracticeSession(AcademyPracticeSource.EXERCISE, 15, millisForEpochDay(20_024L))

        val afterFreeze = db.academyStreakDao().getStreakState()!!
        assertEquals(4, afterFreeze.currentStreak)
        assertEquals(4, afterFreeze.bestStreak)
        assertEquals(0, afterFreeze.freezeTokensAvailable)

        val bridgedDay = db.academyStreakDao().getPracticeDay(20_023L)!!
        assertTrue(bridgedDay.isFreezeUsed)
        assertTrue(bridgedDay.streakQualifies)
    }

    @Test
    fun `missing multiple days or lacking shield resets current streak while preserving best streak`() = runTest {
        // S'assurer que 0 bouclier reste pour le second saut
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_030L))
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_031L))
        // Saut de 2 jours complets (20_032 et 20_033)
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_034L))

        val state = db.academyStreakDao().getStreakState()!!
        assertEquals(1, state.currentStreak)
        assertEquals(2, state.bestStreak)
    }

    @Test
    fun `claiming unlocked streak reward grants bonus xp and freeze token once`() = runTest {
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_040L))
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_041L))
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_042L))

        val claimed = repository.claimStreakReward("streak_reward_3", millisForEpochDay(20_042L))
        assertNotNull(claimed)
        assertTrue(claimed!!.isClaimed)

        val state = db.academyStreakDao().getStreakState()!!
        assertEquals(50, state.totalBonusXpEarned)
        assertEquals(2, state.freezeTokensAvailable) // 1 initial + 1 accordé

        // Impossible de réclamer deux fois la même récompense
        val duplicateClaim = repository.claimStreakReward("streak_reward_3", millisForEpochDay(20_042L))
        assertNull(duplicateClaim)
    }

    @Test
    fun `seven consecutive academy sessions unlock seven day streak reward and achievement`() = runTest {
        for (offset in 0L..6L) {
            repository.recordAcademyPracticeSession(
                source = AcademyPracticeSource.VOCABULARY,
                xpDelta = 15,
                nowMillis = millisForEpochDay(20_050L + offset)
            )
        }
        repository.evaluateAndUnlockAchievements()

        val state = db.academyStreakDao().getStreakState()!!
        assertEquals(7, state.currentStreak)

        val reward7 = db.academyStreakDao().getStreakRewardsSnapshot().first { it.id == "streak_reward_7" }
        assertTrue(reward7.isUnlocked)

        val ach7 = db.statsAndMetaDao().getAchievementsSnapshot().first { it.id == "ach_streak_7" }
        assertTrue(ach7.isUnlocked)
    }

    @Test
    fun `buildSummary exposes accurate health status and 7-day strip`() {
        val today = 20_100L
        val days = listOf(
            com.example.core.database.AcademyPracticeDay(epochDay = today - 2, lessonsCompletedCount = 1, streakQualifies = true),
            com.example.core.database.AcademyPracticeDay(epochDay = today - 1, streakQualifies = true, isFreezeUsed = true)
        )
        val state = com.example.core.database.StreakState(
            currentStreak = 2,
            bestStreak = 5,
            lastPracticeEpochDay = today - 1,
            freezeTokensAvailable = 1
        )
        val summary = AcademyStreakEngine.buildSummary(
            streakState = state,
            practiceDays = days,
            rewards = AcademyStreakEngine.defaultRewards(),
            nowMillis = millisForEpochDay(today)
        )

        assertEquals(StreakHealthStatus.AT_RISK_TODAY, summary.status)
        assertEquals(2, summary.currentStreak)
        assertEquals(5, summary.bestStreak)
        assertEquals(7, summary.last7Days.size)
        assertTrue(summary.last7Days[4].practiced)
        assertTrue(summary.last7Days[5].freezeUsed)
        assertTrue(summary.last7Days[6].isToday)
        assertFalse(summary.last7Days[6].practiced)
    }

    @Test
    fun `backup export and import preserve academy streak state practice days and rewards`() = runTest {
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_200L))
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_201L))
        repository.recordAcademyPracticeSession(AcademyPracticeSource.LESSON, 25, millisForEpochDay(20_202L))
        repository.claimStreakReward("streak_reward_3", millisForEpochDay(20_202L))

        val export = repository.exportBackupJson(includeHistory = true) as BackupResult.ExportSuccess

        // Réinitialiser les données série avant import
        db.academyStreakDao().clearPracticeDays()
        db.academyStreakDao().upsertStreakState(com.example.core.database.StreakState())
        db.academyStreakDao().insertStreakRewards(AcademyStreakEngine.defaultRewards())

        val importResult = repository.importBackupJson(export.json)
        assertTrue(importResult is BackupResult.ImportSuccess)

        val restoredState = db.academyStreakDao().getStreakState()!!
        assertEquals(3, restoredState.currentStreak)
        assertEquals(50, restoredState.totalBonusXpEarned)
        assertEquals(2, restoredState.freezeTokensAvailable)

        val restoredDays = db.academyStreakDao().getAllPracticeDaysSnapshot()
        assertEquals(3, restoredDays.size)

        val restoredReward3 = db.academyStreakDao().getStreakRewardsSnapshot().first { it.id == "streak_reward_3" }
        assertTrue(restoredReward3.isUnlocked)
        assertTrue(restoredReward3.isClaimed)
    }
}
