package com.example.waterreminder.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.waterreminder.domain.model.ActivityMode
import com.example.waterreminder.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_NAME = stringPreferencesKey("user_name")
        val UNIT = stringPreferencesKey("unit")
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val QUICK_ADD_ML = intPreferencesKey("quick_add_ml")
        val WORK_ALLOCATION_PERCENT = intPreferencesKey("work_allocation_percent")
        val WAKE_UP_TIME = stringPreferencesKey("wake_up_time")
        val SLEEP_TIME = stringPreferencesKey("sleep_time")
        val WORK_START_TIME = stringPreferencesKey("work_start_time")
        val WORK_END_TIME = stringPreferencesKey("work_end_time")
        val ACTIVITY_MODE = stringPreferencesKey("activity_mode")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATE_ENABLED = booleanPreferencesKey("vibrate_enabled")
        val DARK_THEME_MODE = stringPreferencesKey("dark_theme_mode")
        val RECENT_DRINK_SUPPRESSION_MINUTES = intPreferencesKey("recent_drink_suppression_minutes")
        val SNOOZED_UNTIL_MS = longPreferencesKey("snoozed_until_ms")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val activityModeString = prefs[Keys.ACTIVITY_MODE] ?: ActivityMode.OFFICE_AC.name
        val activityMode = try {
            ActivityMode.valueOf(activityModeString)
        } catch (e: Exception) {
            ActivityMode.OFFICE_AC
        }

        UserPreferences(
            isOnboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            userName = prefs[Keys.USER_NAME] ?: "Sabin",
            unit = prefs[Keys.UNIT] ?: "ml",
            dailyGoalMl = prefs[Keys.DAILY_GOAL_ML] ?: 3500,
            quickAddMl = prefs[Keys.QUICK_ADD_ML] ?: 250,
            workAllocationPercent = prefs[Keys.WORK_ALLOCATION_PERCENT] ?: 70,
            wakeUpTime = prefs[Keys.WAKE_UP_TIME] ?: "07:00",
            sleepTime = prefs[Keys.SLEEP_TIME] ?: "23:00",
            workStartTime = prefs[Keys.WORK_START_TIME] ?: "08:30",
            workEndTime = prefs[Keys.WORK_END_TIME] ?: "17:30",
            activityMode = activityMode,
            remindersEnabled = prefs[Keys.REMINDERS_ENABLED] ?: true,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            vibrateEnabled = prefs[Keys.VIBRATE_ENABLED] ?: true,
            darkThemeMode = prefs[Keys.DARK_THEME_MODE] ?: "DARK",
            recentDrinkSuppressionMinutes = prefs[Keys.RECENT_DRINK_SUPPRESSION_MINUTES] ?: 30
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun updatePreferences(preferences: UserPreferences) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = preferences.isOnboardingCompleted
            prefs[Keys.USER_NAME] = preferences.userName
            prefs[Keys.UNIT] = preferences.unit
            prefs[Keys.DAILY_GOAL_ML] = preferences.dailyGoalMl
            prefs[Keys.QUICK_ADD_ML] = preferences.quickAddMl
            prefs[Keys.WORK_ALLOCATION_PERCENT] = preferences.workAllocationPercent
            prefs[Keys.WAKE_UP_TIME] = preferences.wakeUpTime
            prefs[Keys.SLEEP_TIME] = preferences.sleepTime
            prefs[Keys.WORK_START_TIME] = preferences.workStartTime
            prefs[Keys.WORK_END_TIME] = preferences.workEndTime
            prefs[Keys.ACTIVITY_MODE] = preferences.activityMode.name
            prefs[Keys.REMINDERS_ENABLED] = preferences.remindersEnabled
            prefs[Keys.SOUND_ENABLED] = preferences.soundEnabled
            prefs[Keys.VIBRATE_ENABLED] = preferences.vibrateEnabled
            prefs[Keys.DARK_THEME_MODE] = preferences.darkThemeMode
            prefs[Keys.RECENT_DRINK_SUPPRESSION_MINUTES] = preferences.recentDrinkSuppressionMinutes
        }
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = name
        }
    }

    suspend fun updateDailyGoal(goalMl: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL_ML] = goalMl }
    }

    suspend fun updateQuickAddAmount(amountMl: Int) {
        context.dataStore.edit { it[Keys.QUICK_ADD_ML] = amountMl }
    }

    suspend fun updateActivityMode(mode: ActivityMode) {
        context.dataStore.edit { it[Keys.ACTIVITY_MODE] = mode.name }
    }

    suspend fun updateRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMINDERS_ENABLED] = enabled }
    }

    suspend fun getSnoozedUntilMs(): Long? =
        context.dataStore.data.first()[Keys.SNOOZED_UNTIL_MS]

    suspend fun setSnoozedUntilMs(timestampMs: Long?) {
        context.dataStore.edit { prefs ->
            if (timestampMs == null) {
                prefs.remove(Keys.SNOOZED_UNTIL_MS)
            } else {
                prefs[Keys.SNOOZED_UNTIL_MS] = timestampMs
            }
        }
    }

    suspend fun resetPreferences() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
