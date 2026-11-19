package com.example.waterreminder.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash", "Splash")
    object Welcome : Screen("welcome", "Welcome")
    object PermissionSetup : Screen("permission", "Permission")
    object GoalSetup : Screen("goal_setup", "Goal Setup")
    object EditGoal : Screen("edit_goal", "Edit Daily Goal")
    object ReminderSetup : Screen("reminder_setup", "Reminder Setup")
    object SetupSummary : Screen("setup_summary", "Setup Summary")
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object History : Screen("history", "History", Icons.Default.DateRange)
    object Reminders : Screen("reminders", "Reminders", Icons.Default.Notifications)
    object AddEditReminder : Screen("add_edit_reminder?scheduleId={scheduleId}", "Add Reminder") {
        fun createRoute(scheduleId: Long? = null): String =
            "add_edit_reminder?scheduleId=${scheduleId ?: 0L}"
    }
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Profile : Screen("profile", "Profile")

    companion object {
        val bottomNavItems = listOf(Dashboard, History, Reminders, Settings)
    }
}
