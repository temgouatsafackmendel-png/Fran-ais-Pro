package com.example

import android.app.Application
import com.example.core.work.DailyReminderWorker
import com.example.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FrancaisProApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isSeedReady = MutableStateFlow(false)
    val isSeedReady: StateFlow<Boolean> = _isSeedReady.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch {
            try {
                container.seeder.seedIfNeeded()
                val prefs = container.preferencesRepository.preferencesFlow.first()
                DailyReminderWorker.schedule(
                    context = this@FrancaisProApplication,
                    enabled = prefs.dailyReminderEnabled,
                    reminderHour = prefs.reminderHour
                )
            } finally {
                _isSeedReady.value = true
            }
        }
    }
}
