package se.w3footprint.korlog.presentation.navigation

sealed class Screen(val route: String) {

    // Auth
    data object Login : Screen("auth/login")
    data object Register : Screen("auth/register")

    // Main
    data object Dashboard : Screen("dashboard")
    data object ActiveSession : Screen("session/active")
    data object History : Screen("history")
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
    data object ProUpgrade : Screen("pro")
    data object About : Screen("settings/about")

    // Detail screens with arguments
    data object SessionDetail : Screen("history/{sessionId}") {
        fun createRoute(sessionId: Long) = "history/$sessionId"
    }
    data object SessionSummary : Screen("session/summary/{sessionId}") {
        fun createRoute(sessionId: Long) = "session/summary/$sessionId"
    }
}
