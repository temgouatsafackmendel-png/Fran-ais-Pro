package com.example.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "francais_pro_prefs")

data class LocalUserPreferences(
    val themeMode: String = "SYSTEM",
    val dailyGoalMinutes: Int = 15,
    val ttsSpeechRate: Float = 0.92f,
    val autoAnalyze: Boolean = false,
    val includeHistoryInBackup: Boolean = true,
    val dailyReminderEnabled: Boolean = false,
    val reminderHour: Int = 19
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val TTS_SPEECH_RATE = floatPreferencesKey("tts_speech_rate")
        val AUTO_ANALYZE = booleanPreferencesKey("auto_analyze")
        val INCLUDE_HISTORY_BACKUP = booleanPreferencesKey("include_history_backup")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    }

    val preferencesFlow: Flow<LocalUserPreferences> = context.dataStore.data.map { prefs ->
        LocalUserPreferences(
            themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM",
            dailyGoalMinutes = prefs[Keys.DAILY_GOAL_MINUTES] ?: 15,
            ttsSpeechRate = prefs[Keys.TTS_SPEECH_RATE] ?: 0.92f,
            autoAnalyze = prefs[Keys.AUTO_ANALYZE] ?: false,
            includeHistoryInBackup = prefs[Keys.INCLUDE_HISTORY_BACKUP] ?: true,
            dailyReminderEnabled = prefs[Keys.DAILY_REMINDER_ENABLED] ?: false,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 19
        )
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun updateDailyGoal(minutes: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL_MINUTES] = minutes.coerceIn(5, 120) }
    }

    suspend fun updateSpeechRate(rate: Float) {
        context.dataStore.edit { it[Keys.TTS_SPEECH_RATE] = rate.coerceIn(0.6f, 1.5f) }
    }

    suspend fun updateAutoAnalyze(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_ANALYZE] = enabled }
    }

    suspend fun updateIncludeHistoryInBackup(enabled: Boolean) {
        context.dataStore.edit { it[Keys.INCLUDE_HISTORY_BACKUP] = enabled }
    }

    suspend fun updateDailyReminder(enabled: Boolean, hour: Int) {
        context.dataStore.edit {
            it[Keys.DAILY_REMINDER_ENABLED] = enabled
            it[Keys.REMINDER_HOUR] = hour.coerceIn(7, 22)
        }
    }
}
