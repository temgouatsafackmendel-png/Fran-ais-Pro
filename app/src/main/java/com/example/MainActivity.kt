package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.core.work.DailyReminderWorker
import com.example.navigation.FrancaisProApp

class MainActivity : ComponentActivity() {
    private var pendingNotificationRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingNotificationRoute = extractTargetRoute(intent)
        val app = application as FrancaisProApplication
        setContent {
            FrancaisProApp(
                container = app.container,
                pendingNotificationRoute = pendingNotificationRoute,
                onPendingRouteConsumed = { pendingNotificationRoute = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractTargetRoute(intent)?.let { route ->
            pendingNotificationRoute = route
        }
    }

    private fun extractTargetRoute(intent: Intent?): String? =
        intent?.getStringExtra(DailyReminderWorker.EXTRA_TARGET_ROUTE)
}
