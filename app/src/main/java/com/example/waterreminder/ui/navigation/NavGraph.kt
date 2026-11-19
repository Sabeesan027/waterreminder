package com.example.waterreminder.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.waterreminder.WaterReminderApplication
import com.example.waterreminder.domain.model.ReminderSchedule
import com.example.waterreminder.domain.model.UserPreferences
import com.example.waterreminder.ui.history.HistoryViewModel
import com.example.waterreminder.ui.home.HomeViewModel
import com.example.waterreminder.ui.onboarding.OnboardingViewModel
import com.example.waterreminder.ui.reminders.RemindersViewModel
import com.example.waterreminder.ui.screens.AddEditReminderScreen
import com.example.waterreminder.ui.screens.DashboardScreen
import com.example.waterreminder.ui.screens.GoalSetupScreen
import com.example.waterreminder.ui.screens.HistoryAnalyticsScreen
import com.example.waterreminder.ui.screens.PermissionSetupScreen
import com.example.waterreminder.ui.screens.ProfileScreen
import com.example.waterreminder.ui.screens.ReminderSetupScreen
import com.example.waterreminder.ui.screens.RemindersManagementScreen
import com.example.waterreminder.ui.screens.SettingsMainScreen
import com.example.waterreminder.ui.screens.SetupSummaryScreen
import com.example.waterreminder.ui.screens.SplashScreen
import com.example.waterreminder.ui.screens.WelcomeScreen
import com.example.waterreminder.ui.settings.SettingsViewModel

@Composable
fun MainNavGraph(
    app: WaterReminderApplication,
    isOnboardingCompleted: Boolean,
    navController: NavHostController = rememberNavController()
) {
    val onboardingVm: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory(
            app.userPreferencesRepository,
            app.scheduleRepository,
            app.reminderScheduler
        )
    )
    val onboardingState by onboardingVm.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            fadeIn(tween(280)) + slideInHorizontally(
                animationSpec = tween(360, easing = FastOutSlowInEasing),
                initialOffsetX = { it / 10 }
            )
        },
        exitTransition = {
            fadeOut(tween(180)) + slideOutHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                targetOffsetX = { -it / 12 }
            )
        },
        popEnterTransition = {
            fadeIn(tween(260)) + slideInHorizontally(
                animationSpec = tween(340, easing = FastOutSlowInEasing),
                initialOffsetX = { -it / 10 }
            )
        },
        popExitTransition = {
            fadeOut(tween(180)) + slideOutHorizontally(
                animationSpec = tween(280, easing = FastOutSlowInEasing),
                targetOffsetX = { it / 12 }
            )
        }
    ) {
        // Screen 1: Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                isOnboardingCompleted = isOnboardingCompleted,
                onNavigateNext = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Screen 2: Welcome / Onboarding
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNextClick = { navController.navigate(Screen.PermissionSetup.route) },
                onSkipClick = {
                    onboardingVm.skipOnboarding {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Screen 3: Permission Setup
        composable(Screen.PermissionSetup.route) {
            PermissionSetupScreen(
                onContinue = { navController.navigate(Screen.GoalSetup.route) },
                onNotNow = { navController.navigate(Screen.GoalSetup.route) }
            )
        }

        // Screen 4: Daily Water Goal Setup
        composable(Screen.GoalSetup.route) {
            GoalSetupScreen(
                initialGoalMl = onboardingState.dailyGoalMl,
                isOnboarding = true,
                onGoalSet = { goalMl ->
                    onboardingVm.updateGoal(goalMl)
                    navController.navigate(Screen.ReminderSetup.route)
                }
            )
        }

        composable(Screen.EditGoal.route) {
            val settingsVm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(
                    app.userPreferencesRepository,
                    app.waterRepository,
                    app.reminderScheduler
                )
            )
            val prefs by settingsVm.userPreferences.collectAsState()
            GoalSetupScreen(
                initialGoalMl = prefs.dailyGoalMl,
                isOnboarding = false,
                onBack = { navController.popBackStack() },
                onGoalSet = { goalMl ->
                    settingsVm.updateDailyGoal(goalMl)
                    navController.popBackStack()
                }
            )
        }

        // Screen 5: Reminder Setup
        composable(Screen.ReminderSetup.route) {
            ReminderSetupScreen(
                initialRemindersEnabled = onboardingState.remindersEnabled,
                initialWorkingDays = onboardingState.workingDays,
                initialIntervalMinutes = onboardingState.reminderIntervalMinutes,
                initialSnoozeMinutes = onboardingState.snoozeMinutes,
                onFinishSetup = { enabled, days, intervalMins, snoozeMins, suppressBreaks ->
                    onboardingVm.updateRemindersEnabled(enabled)
                    onboardingVm.updateWorkingDays(days)
                    onboardingVm.updateInterval(intervalMins)
                    onboardingVm.updateSnooze(snoozeMins)
                    onboardingVm.updateSuppressDuringBreaks(suppressBreaks)
                    onboardingVm.completeOnboarding {
                        navController.navigate(Screen.SetupSummary.route)
                    }
                }
            )
        }

        // Screen 6: Setup Summary
        composable(Screen.SetupSummary.route) {
            val prefs by app.userPreferencesRepository.userPreferencesFlow.collectAsState(
                initial = UserPreferences()
            )
            val schedule by produceState<ReminderSchedule?>(initialValue = null) {
                value = app.scheduleRepository.getAllSchedulesSync().firstOrNull()
            }
            SetupSummaryScreen(
                userPreferences = prefs,
                schedule = schedule,
                onGoToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onEditSetup = { navController.navigate(Screen.GoalSetup.route) }
            )
        }

        // Screen 7: Home Dashboard
        composable(Screen.Dashboard.route) {
            val homeVm: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(
                    app.waterRepository,
                    app.scheduleRepository,
                    app.userPreferencesRepository,
                    app.reminderScheduler
                )
            )
            DashboardScreen(
                viewModel = homeVm,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // Screen 8: History & Analytics
        composable(Screen.History.route) {
            val historyVm: HistoryViewModel = viewModel(
                factory = HistoryViewModel.Factory(
                    app.waterRepository,
                    app.userPreferencesRepository
                )
            )
            HistoryAnalyticsScreen(
                viewModel = historyVm,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // Screen 9: Reminders Management
        composable(Screen.Reminders.route) {
            val remindersVm: RemindersViewModel = viewModel(
                factory = RemindersViewModel.Factory(
                    app.scheduleRepository,
                    app.userPreferencesRepository,
                    app.reminderScheduler
                )
            )
            RemindersManagementScreen(
                viewModel = remindersVm,
                onAddReminder = { navController.navigate(Screen.AddEditReminder.createRoute()) },
                onEditReminder = { schedule ->
                    navController.navigate(Screen.AddEditReminder.createRoute(schedule.id))
                },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // Screen 10: Add / Edit Reminder
        composable(
            route = Screen.AddEditReminder.route,
            arguments = listOf(
                navArgument("scheduleId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val remindersVm: RemindersViewModel = viewModel(
                factory = RemindersViewModel.Factory(
                    app.scheduleRepository,
                    app.userPreferencesRepository,
                    app.reminderScheduler
                )
            )
            val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: 0L
            val schedule by produceState<ReminderSchedule?>(
                initialValue = null,
                key1 = scheduleId
            ) {
                value = if (scheduleId == 0L) null else app.scheduleRepository.getScheduleById(scheduleId)
            }

            if (scheduleId == 0L || schedule != null) {
                AddEditReminderScreen(
                    schedule = schedule,
                    onSave = { updatedSchedule ->
                        remindersVm.updateSchedule(updatedSchedule)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Screen 11: Settings
        composable(Screen.Settings.route) {
            val settingsVm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(
                    app.userPreferencesRepository,
                    app.waterRepository,
                    app.reminderScheduler
                )
            )
            SettingsMainScreen(
                viewModel = settingsVm,
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToGoalSetup = { navController.navigate(Screen.EditGoal.route) },
                onResetCompleted = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // Screen 12: Profile
        composable(Screen.Profile.route) {
            val settingsVm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(
                    app.userPreferencesRepository,
                    app.waterRepository,
                    app.reminderScheduler
                )
            )
            ProfileScreen(
                viewModel = settingsVm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
