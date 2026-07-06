package se.w3footprint.korlog.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import se.w3footprint.korlog.R
import se.w3footprint.korlog.presentation.auth.ForgotPasswordScreen
import se.w3footprint.korlog.presentation.auth.LoginScreen
import se.w3footprint.korlog.presentation.auth.RegisterScreen
import se.w3footprint.korlog.presentation.dashboard.DashboardScreen
import se.w3footprint.korlog.presentation.history.HistoryScreen
import se.w3footprint.korlog.presentation.history.SessionDetailScreen
import se.w3footprint.korlog.presentation.session.ActiveSessionScreen
import se.w3footprint.korlog.presentation.session.SessionSummaryScreen
import se.w3footprint.korlog.presentation.settings.AboutScreen
import se.w3footprint.korlog.presentation.settings.ProUpgradeScreen
import se.w3footprint.korlog.presentation.settings.SettingsScreen
import se.w3footprint.korlog.presentation.stats.StatsScreen

private val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.History,
    Screen.Stats,
    Screen.Settings
)

private val screensWithoutBottomNav = setOf(
    Screen.Login.route,
    Screen.Register.route,
    Screen.ForgotPassword.route,
    Screen.ActiveSession.route
)

@Composable
fun KorLogNavGraph(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomNav = currentDestination?.route !in screensWithoutBottomNav

    val startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = when (screen) {
                                        Screen.Dashboard -> Icons.Default.Home
                                        Screen.History   -> Icons.Default.History
                                        Screen.Stats     -> Icons.Default.BarChart
                                        Screen.Settings  -> Icons.Default.Settings
                                        else             -> Icons.Default.Home
                                    },
                                    contentDescription = null
                                )
                            },
                            label = {
                                Text(
                                    text = when (screen) {
                                        Screen.Dashboard -> stringResource(R.string.nav_home)
                                        Screen.History   -> stringResource(R.string.nav_history)
                                        Screen.Stats     -> stringResource(R.string.nav_stats)
                                        Screen.Settings  -> stringResource(R.string.nav_settings)
                                        else             -> ""
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onRegisterClick = { navController.navigate(Screen.Register.route) },
                    onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onLoginClick = { navController.popBackStack() }
                )
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(onBack = { navController.popBackStack() })
            }

            // Main tabs
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onStartSession = { navController.navigate(Screen.ActiveSession.route) },
                    onSessionClick = { sessionId ->
                        navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                    }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                    }
                )
            }
            composable(Screen.Stats.route) {
                StatsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onProUpgradeClick = { navController.navigate(Screen.ProUpgrade.route) },
                    onAboutClick = { navController.navigate(Screen.About.route) },
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Session flow
            composable(Screen.ActiveSession.route) {
                ActiveSessionScreen(
                    onSessionSaved = { sessionId ->
                        navController.navigate(Screen.SessionSummary.createRoute(sessionId)) {
                            popUpTo(Screen.Dashboard.route)
                        }
                    }
                )
            }
            composable(
                route = Screen.SessionSummary.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
                SessionSummaryScreen(
                    sessionId = sessionId,
                    onDone = { navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }}
                )
            }

            // History detail
            composable(
                route = Screen.SessionDetail.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
                SessionDetailScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() }
                )
            }

            // Settings sub-screens
            composable(Screen.ProUpgrade.route) {
                ProUpgradeScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.About.route) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
