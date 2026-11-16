package com.hayati.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hayati.app.core.designsystem.theme.LocalHayatiColors
import com.hayati.app.navigation.HayatiNavHost
import com.hayati.app.navigation.HayatiRoutes
import com.hayati.app.navigation.TopLevelDestination
import com.hayati.app.navigation.navigateTopLevel

/** Application root: scaffold with the bottom bar (only on the four main screens) and the nav host. */
@Composable
fun HayatiApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val showBottomBar = currentDestination?.hierarchy?.any { destination ->
        destination.route in HayatiRoutes.topLevel
    } == true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                ) {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) navController.navigateTopLevel(destination)
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = LocalHayatiColors.current.gold,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = LocalHayatiColors.current.textSecondary,
                                unselectedTextColor = LocalHayatiColors.current.textSecondary,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        HayatiNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
