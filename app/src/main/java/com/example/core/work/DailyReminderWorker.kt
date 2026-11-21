package com.example.core.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.core.database.AcademyPracticeDay
import com.example.core.database.FrancaisProDatabase
import com.example.core.database.StreakReward
import com.example.core.database.StreakState
import com.example.core.datastore.PreferencesRepository
import com.example.learning.progression.AcademyStreakEngine
import com.example.learning.progression.StreakHealthStatus
import com.example.navigation.Routes
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class StreakReminderContent(
    val title: String,
    val body: String,
    val bigText: String,
    val currentStreak: Int
)

enum class ReminderSkipReason {
    PERMISSION_DENIED,
    REMINDER_DISABLED,
    DAILY_STREAK_ALREADY_MET
}

sealed interface ReminderDecision {
    data class ShowNotification(val content: StreakReminderContent) : ReminderDecision
    data class Skip(val reason: ReminderSkipReason) : ReminderDecision
}

/**
 * Worker WorkManager 100 % hors ligne chargé de vérifier si la série quotidienne de l'Académie
 * a déjà été validée aujourd'hui et d'envoyer une notification locale adaptée dans le cas contraire.
 */
class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = PreferencesRepository(applicationContext).preferencesFlow.first()
        val forcePreview = inputData.getBoolean(KEY_FORCE_PREVIEW, false)
        val db = FrancaisProDatabase.getInstance(applicationContext)
        val dbSettings = db.statsAndMetaDao().getSettings()
        val reminderEnabled = forcePreview || prefs.dailyReminderEnabled || (dbSettings?.dailyReminderEnabled == true)

        executeReminderWork(
            context = applicationContext,
            db = db,
            reminderEnabled = reminderEnabled,
            nowMillis = System.currentTimeMillis()
        )
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "francais_pro_daily_reminder"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "francais_pro_daily_study_reminder"
        const val WORK_TAG = "francais_pro_streak_reminder"
        const val IMMEDIATE_WORK_NAME = "francais_pro_daily_reminder_preview"
        const val EXTRA_TARGET_ROUTE = "extra_target_route"
        private const val KEY_FORCE_PREVIEW = "key_force_preview"

        @VisibleForTesting
        internal suspend fun executeReminderWork(
            context: Context,
            db: FrancaisProDatabase,
            reminderEnabled: Boolean,
            nowMillis: Long = System.currentTimeMillis()
        ): ReminderDecision {
            val hasPermission = hasPostNotificationsPermission(context)
            val todayEpoch = nowMillis / AcademyStreakEngine.DAY_MS
            val streakState = db.academyStreakDao().getStreakState()
            val todayPractice = db.academyStreakDao().getPracticeDay(todayEpoch)
            val rewards = db.academyStreakDao().getStreakRewardsSnapshot()
                .ifEmpty { AcademyStreakEngine.defaultRewards() }

            val decision = evaluateReminderDecision(
                streakState = streakState,
                todayPractice = todayPractice,
                rewards = rewards,
                reminderEnabled = reminderEnabled,
                hasNotificationPermission = hasPermission,
                nowMillis = nowMillis
            )
            if (decision is ReminderDecision.ShowNotification) {
                showLocalNotification(context, decision.content)
            }
            return decision
        }

        private fun showLocalNotification(context: Context, content: StreakReminderContent) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Rappels de série Académie",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Rappels locaux quotidiens lorsque la session du jour à l'Académie n'est pas encore validée."
                }
                manager.createNotificationChannel(channel)
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_TARGET_ROUTE, Routes.ACADEMY)
            }
            val contentIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_reminder)
                .setContentTitle(content.title)
                .setContentText(content.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content.bigText))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()

            manager.notify(NOTIFICATION_ID, notification)
        }

        fun hasPostNotificationsPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

        @VisibleForTesting
        fun evaluateReminderDecision(
            streakState: StreakState?,
            todayPractice: AcademyPracticeDay?,
            rewards: List<StreakReward>,
            reminderEnabled: Boolean,
            hasNotificationPermission: Boolean,
            nowMillis: Long = System.currentTimeMillis()
        ): ReminderDecision {
            if (!hasNotificationPermission) {
                return ReminderDecision.Skip(ReminderSkipReason.PERMISSION_DENIED)
            }
            if (!reminderEnabled) {
                return ReminderDecision.Skip(ReminderSkipReason.REMINDER_DISABLED)
            }

            val summary = AcademyStreakEngine.buildSummary(
                streakState = streakState,
                practiceDays = listOfNotNull(todayPractice),
                rewards = rewards,
                nowMillis = nowMillis
            )

            if (summary.practicedToday) {
                return ReminderDecision.Skip(ReminderSkipReason.DAILY_STREAK_ALREADY_MET)
            }

            val (title, body) = when (summary.status) {
                StreakHealthStatus.AT_RISK_TODAY -> {
                    val unit = if (summary.currentStreak <= 1) "jour" else "jours"
                    "Protégez votre série de ${summary.currentStreak} $unit !" to
                        "Validez 1 leçon, 1 exercice ou 1 carte SM-2 à l'Académie pour atteindre ${summary.currentStreak + 1} jours."
                }
                StreakHealthStatus.FREEZE_READY -> {
                    "Bouclier prêt · Sauvez votre série (${summary.currentStreak} j)" to
                        "Votre bouclier protège votre série : pratiquez à l'Académie aujourd'hui pour la conserver."
                }
                else -> {
                    "Français Pro — Pratique quotidienne" to
                        "Terminez une leçon, un exercice ou 1 mot SM-2 à l'Académie pour lancer votre série du jour."
                }
            }

            val rewardSuffix = summary.nextReward
                ?.takeIf { !it.isUnlocked && summary.daysToNextReward in 1..2 }
                ?.let { " Prochain palier « ${it.title} » dans ${summary.daysToNextReward} j (+${it.bonusXp} XP)." }
                .orEmpty()

            return ReminderDecision.ShowNotification(
                StreakReminderContent(
                    title = title,
                    body = body,
                    bigText = body + rewardSuffix,
                    currentStreak = summary.currentStreak
                )
            )
        }

        internal fun computeInitialDelayMillis(
            targetHour: Int,
            nowMillis: Long = System.currentTimeMillis()
        ): Long {
            val safeHour = targetHour.coerceIn(0, 23)
            val target = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, safeHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= nowMillis) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            return (target.timeInMillis - nowMillis).coerceAtLeast(TimeUnit.MINUTES.toMillis(1))
        }

        fun schedule(
            context: Context,
            enabled: Boolean,
            reminderHour: Int = 19,
            nowMillis: Long = System.currentTimeMillis()
        ) {
            val wm = try {
                WorkManager.getInstance(context)
            } catch (_: IllegalStateException) {
                return
            }
            if (!enabled) {
                wm.cancelUniqueWork(WORK_NAME)
                return
            }
            val initialDelayMs = computeInitialDelayMillis(reminderHour, nowMillis)
            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG)
                .build()
            wm.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun triggerImmediateCheck(context: Context) {
            val wm = try {
                WorkManager.getInstance(context)
            } catch (_: IllegalStateException) {
                return
            }
            val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
                .addTag(WORK_TAG)
                .setInputData(androidx.work.workDataOf(KEY_FORCE_PREVIEW to true))
                .build()
            wm.enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
