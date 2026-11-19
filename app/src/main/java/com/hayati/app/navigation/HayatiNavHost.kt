package com.hayati.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hayati.app.domain.adhkar.model.AdhkarCategory
import com.hayati.app.feature.adhkar.AdhkarScreen
import com.hayati.app.feature.home.HomeScreen
import com.hayati.app.feature.prayer.PrayerHistoryScreen
import com.hayati.app.feature.prayer.PrayerScreen
import com.hayati.app.feature.prayer.PrayerSettingsScreen
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
                onOpenPrayer = { navController.navigateSingleTop(WorshipSection.Prayer.route) },
                onOpenAdhkar = { category -> navController.navigateSingleTop(HayatiRoutes.adhkar(category?.key)) },
            )
        }
        composable(HayatiRoutes.WORSHIP) {
            WorshipScreen(onOpenSection = { navController.navigateSingleTop(it.route) })
        }
        composable(HayatiRoutes.TASKS) { TasksScreen() }
        composable(HayatiRoutes.STATISTICS) { StatisticsScreen() }
        composable(HayatiRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenPrayerSettings = { navController.navigateSingleTop(HayatiRoutes.PRAYER_SETTINGS) },
            )
        }
        composable(WorshipSection.Prayer.route) {
            PrayerScreen(
                onBack = { navController.popBackStack() },
                onOpenSettings = { navController.navigateSingleTop(HayatiRoutes.PRAYER_SETTINGS) },
                onOpenHistory = { navController.navigateSingleTop(HayatiRoutes.PRAYER_HISTORY) },
            )
        }
        composable(HayatiRoutes.PRAYER_SETTINGS) {
            PrayerSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(HayatiRoutes.PRAYER_HISTORY) {
            PrayerHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = HayatiRoutes.ADHKAR_PATTERN,
            arguments = listOf(
                navArgument(HayatiRoutes.ADHKAR_CATEGORY_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            val category = entry.arguments?.getString(HayatiRoutes.ADHKAR_CATEGORY_ARG)
                ?.let { AdhkarCategory.fromKey(it) }
            AdhkarScreen(onBack = { navController.popBackStack() }, initialCategory = category)
        }
        // Prayer and Adhkar are real screens above; the other sections are still honest placeholders.
        WorshipSection.entries.filter { it != WorshipSection.Prayer && it != WorshipSection.Adhkar }.forEach { section ->
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
