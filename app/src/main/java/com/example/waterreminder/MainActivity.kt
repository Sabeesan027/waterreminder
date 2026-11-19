package com.example.waterreminder

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.waterreminder.domain.model.LogSource
import com.example.waterreminder.domain.model.WaterLog
import com.example.waterreminder.notification.NotificationHelper
import com.example.waterreminder.ui.components.HydrationReminderBot
import com.example.waterreminder.ui.navigation.MainNavGraph
import com.example.waterreminder.ui.theme.WaterReminderTheme
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WaterReminderApplication
        showReminderBotFromIntent(intent, app)

        setContent {
            val userPrefs by app.userPreferencesRepository.userPreferencesFlow.collectAsState(
                initial = com.example.waterreminder.domain.model.UserPreferences()
            )

            val isDarkTheme = when (userPrefs.darkThemeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            val reminderBotVisible by app.reminderBotVisible.collectAsState()
            val scope = rememberCoroutineScope()

            WaterReminderTheme(darkTheme = isDarkTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MainNavGraph(
                        app = app,
                        isOnboardingCompleted = userPrefs.isOnboardingCompleted
                    )

                    HydrationReminderBot(
                        visible = reminderBotVisible,
                        quickAddAmountMl = userPrefs.quickAddMl,
                        onDrink = {
                            app.dismissReminderBot()
                            scope.launch {
                                val date = LocalDate.now().toString()
                                app.waterRepository.addWaterLog(
                                    WaterLog(
                                        amountMl = userPrefs.quickAddMl,
                                        timestamp = System.currentTimeMillis(),
                                        source = LogSource.NOTIFICATION
                                    ),
                                    date
                                )
                                app.reminderScheduler.scheduleNextReminder()
                            }
                        },
                        onDismiss = app::dismissReminderBot
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        showReminderBotFromIntent(intent, application as WaterReminderApplication)
    }

    private fun showReminderBotFromIntent(
        intent: Intent?,
        app: WaterReminderApplication
    ) {
        if (intent?.getBooleanExtra(
                NotificationHelper.EXTRA_SHOW_REMINDER_BOT,
                false
            ) == true
        ) {
            app.showReminderBot()
            intent.removeExtra(NotificationHelper.EXTRA_SHOW_REMINDER_BOT)
        }
    }
}
