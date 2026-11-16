package com.hayati.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hayati.app.feature.home.HomeScreen
import com.hayati.app.feature.settings.SettingsScreen
import com.hayati.app.feature.statistics.StatisticsScreen
import com.hayati.app.feature.tasks.TasksScreen
import com.hayati.app.feature.worship.WorshipScreen
import com.hayati.app.feature.worship.WorshipSection
import com.hayati.app.feature.worship.WorshipSectionPlaceholderScreen

private const val FADE_MILLIS = 180

@Composable
fun HayatiNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HayatiRoutes.HOME,
        modifier = modifier,
        enterTransition = { fadeIn(tween(FADE_MILLIS)) },
        exitTransition = { fadeOut(tween(FADE_MILLIS)) },
        popEnterTransition = { fadeIn(tween(FADE_MILLIS)) },
        popExitTransition = { fadeOut(tween(FADE_MILLIS)) },
    ) {
        composable(HayatiRoutes.HOME) {
            HomeScreen(
                onOpenSettings = { navController.navigateSingleTop(HayatiRoutes.SETTINGS) },
                onOpenSection = { navController.navigateSingleTop(it.route) },
                onOpenTasks = { navController.navigateTopLevel(TopLevelDestination.Tasks) },
            )
        }
        composable(HayatiRoutes.WORSHIP) {
            WorshipScreen(onOpenSection = { navController.navigateSingleTop(it.route) })
        }
        composable(HayatiRoutes.TASKS) { TasksScreen() }
        composable(HayatiRoutes.STATISTICS) { StatisticsScreen() }
        composable(HayatiRoutes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        WorshipSection.entries.forEach { section ->
            composable(section.route) {
                WorshipSectionPlaceholderScreen(
                    section = section,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

/** Opens a secondary screen, ignoring repeated taps so it is never created twice. */
private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) { launchSingleTop = true }
}
