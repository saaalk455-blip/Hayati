package com.hayati.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.hayati.app.R
import com.hayati.app.core.designsystem.icons.HayatiIcons

/** Route names that do not depend on any UI classes. */
object HayatiRoutes {
    const val HOME = "home"
    const val WORSHIP = "worship"
    const val TASKS = "tasks"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val PRAYER_SETTINGS = "prayer/settings"
    const val PRAYER_HISTORY = "prayer/history"

    val topLevel: List<String> = listOf(HOME, WORSHIP, TASKS, STATISTICS)
}

/** Destinations shown in the bottom navigation bar, in display order (first = right side in RTL). */
enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home(HayatiRoutes.HOME, R.string.nav_home, Icons.Filled.Home),
    Worship(HayatiRoutes.WORSHIP, R.string.nav_worship, HayatiIcons.Worship),
    Tasks(HayatiRoutes.TASKS, R.string.nav_tasks, Icons.Filled.CheckCircle),
    Statistics(HayatiRoutes.STATISTICS, R.string.nav_statistics, HayatiIcons.Statistics),
}

/**
 * Switches to a bottom-navigation destination without stacking copies of it: pops back to the
 * start destination, keeps one instance on top and restores its saved state.
 */
fun NavController.navigateTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
