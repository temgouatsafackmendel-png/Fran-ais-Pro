package com.example

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.core.database.AcademyPracticeDay
import com.example.core.database.DatabaseSeeder
import com.example.core.database.FrancaisProDatabase
import com.example.core.database.StreakState
import com.example.core.work.DailyReminderWorker
import com.example.core.work.ReminderDecision
import com.example.core.work.ReminderSkipReason
import com.example.learning.progression.AcademyStreakEngine
import com.example.linguistics.conjugation.FrenchConjugatorEngine
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WorkManagerReminderTest {

    private lateinit var app: Application
    private lateinit var db: FrancaisProDatabase
    private val dayMs = AcademyStreakEngine.DAY_MS

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(app, Configuration.Builder().build())
        }
        db = Room.inMemoryDatabaseBuilder(app, FrancaisProDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        kotlinx.coroutines.runBlocking {
            DatabaseSeeder(db, FrenchConjugatorEngine()).seedIfNeeded()
        }
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun millisForEpochDay(epochDay: Long): Long = epochDay * dayMs + 19 * 3_600_000L

    @Test
    fun `evaluateReminderDecision skips when permission denied, disabled, or daily streak already met`() {
        val today = 20_300L
        val now = millisForEpochDay(today)
        val rewards = AcademyStreakEngine.defaultRewards()

        val noPerm = DailyReminderWorker.evaluateReminderDecision(
            streakState = StreakState(currentStreak = 2, lastPracticeEpochDay = today - 1),
            todayPractice = null,
            rewards = rewards,
            reminderEnabled = true,
            hasNotificationPermission = false,
            nowMillis = now
        )
        assertEquals(ReminderDecision.Skip(ReminderSkipReason.PERMISSION_DENIED), noPerm)

        val disabled = DailyReminderWorker.evaluateReminderDecision(
            streakState = StreakState(currentStreak = 2, lastPracticeEpochDay = today - 1),
            todayPractice = null,
            rewards = rewards,
            reminderEnabled = false,
            hasNotificationPermission = true,
            nowMillis = now
        )
        assertEquals(ReminderDecision.Skip(ReminderSkipReason.REMINDER_DISABLED), disabled)

        val alreadyPracticed = DailyReminderWorker.evaluateReminderDecision(
            streakState = StreakState(currentStreak = 3, lastPracticeEpochDay = today),
            todayPractice = AcademyPracticeDay(epochDay = today, lessonsCompletedCount = 1, streakQualifies = true),
            rewards = rewards,
            reminderEnabled = true,
            hasNotificationPermission = true,
            nowMillis = now
        )
        assertEquals(ReminderDecision.Skip(ReminderSkipReason.DAILY_STREAK_ALREADY_MET), alreadyPracticed)
    }

    @Test
    fun `evaluateReminderDecision tailors copy for at-risk streak and freeze-shield status`() {
        val today = 20_310L
        val now = millisForEpochDay(today)
        val rewards = AcademyStreakEngine.defaultRewards()

        val atRisk = DailyReminderWorker.evaluateReminderDecision(
            streakState = StreakState(currentStreak = 2, lastPracticeEpochDay = today - 1, freezeTokensAvailable = 1),
            todayPractice = null,
            rewards = rewards,
            reminderEnabled = true,
            hasNotificationPermission = true,
            nowMillis = now
        ) as ReminderDecision.ShowNotification
        assertTrue(atRisk.content.title.contains("2 jours"))
        assertTrue(atRisk.content.bigText.contains("Élan de 3 jours"))

        val freezeReady = DailyReminderWorker.evaluateReminderDecision(
            streakState = StreakState(currentStreak = 5, lastPracticeEpochDay = today - 2, freezeTokensAvailable = 1),
            todayPractice = null,
            rewards = rewards,
            reminderEnabled = true,
            hasNotificationPermission = true,
            nowMillis = now
        ) as ReminderDecision.ShowNotification
        assertTrue(freezeReady.content.title.contains("Bouclier prêt"))
    }

    @Test
    fun `schedule enqueues unique periodic WorkManager job and cancel removes it cleanly`() {
        DailyReminderWorker.schedule(app, enabled = true, reminderHour = 20)
        val wm = WorkManager.getInstance(app)
        val activeInfos = wm.getWorkInfosForUniqueWork(DailyReminderWorker.WORK_NAME).get()
        assertTrue(activeInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING })
        assertTrue(activeInfos.first().tags.contains(DailyReminderWorker.WORK_TAG))

        DailyReminderWorker.schedule(app, enabled = false, reminderHour = 20)
        val cancelledInfos = wm.getWorkInfosForUniqueWork(DailyReminderWorker.WORK_NAME).get()
        assertTrue(cancelledInfos.isEmpty() || cancelledInfos.all { it.state == WorkInfo.State.CANCELLED })
    }

    @Test
    fun `executeReminderWork posts notification when streak unmet and skips when practiced today`() = runTest {
        val today = 20_400L
        val now = millisForEpochDay(today)
        val notificationManager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNm = Shadows.shadowOf(notificationManager)

        // Cas 1 : série d'hier non encore validée aujourd'hui -> notification envoyée avec cible Académie
        db.academyStreakDao().upsertStreakState(
            StreakState(currentStreak = 4, bestStreak = 4, lastPracticeEpochDay = today - 1, freezeTokensAvailable = 1)
        )
        val shownDecision = DailyReminderWorker.executeReminderWork(
            context = app,
            db = db,
            reminderEnabled = true,
            nowMillis = now
        )
        assertTrue(shownDecision is ReminderDecision.ShowNotification)
        val posted = shadowNm.getNotification(DailyReminderWorker.NOTIFICATION_ID)
        assertTrue("La notification de rappel de série doit être publiée", posted != null)
        val extras = posted.extras
        assertTrue(extras.getString("android.title").orEmpty().contains("4 jours"))

        // Cas 2 : pratique enregistrée aujourd'hui -> WorkManager ne notifie plus l'utilisateur
        notificationManager.cancelAll()
        db.academyStreakDao().upsertPracticeDay(
            AcademyPracticeDay(epochDay = today, exercisesCompletedCount = 1, streakQualifies = true)
        )
        db.academyStreakDao().upsertStreakState(
            StreakState(currentStreak = 5, bestStreak = 5, lastPracticeEpochDay = today, freezeTokensAvailable = 1)
        )
        val skippedDecision = DailyReminderWorker.executeReminderWork(
            context = app,
            db = db,
            reminderEnabled = true,
            nowMillis = now
        )
        assertEquals(ReminderDecision.Skip(ReminderSkipReason.DAILY_STREAK_ALREADY_MET), skippedDecision)
        assertEquals(0, shadowNm.size())
    }
}
