package com.telen.namebattle.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Setup : Screen("setup")
    object About : Screen("about")

    data class Auth(val sessionId: Long) : Screen("auth/${sessionId}")
    data class Search(val sessionId: Long, val parentIndex: Int) :
        Screen("search/${sessionId}/${parentIndex}")
    data class Launch(val sessionId: Long) : Screen("launch/${sessionId}")
    data class Battle(val sessionId: Long) : Screen("battle/${sessionId}")
    data class Results(val sessionId: Long) : Screen("results/${sessionId}")

    companion object {
        const val AUTH_ROUTE = "auth/{sessionId}"
        const val SEARCH_ROUTE = "search/{sessionId}/{parentIndex}"
        const val LAUNCH_ROUTE = "launch/{sessionId}"
        const val BATTLE_ROUTE = "battle/{sessionId}"
        const val RESULTS_ROUTE = "results/{sessionId}"
    }
}
