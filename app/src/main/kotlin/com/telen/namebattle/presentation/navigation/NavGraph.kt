package com.telen.namebattle.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.telen.namebattle.presentation.auth.AuthScreen
import com.telen.namebattle.presentation.battle.BattleScreen
import com.telen.namebattle.presentation.home.HomeScreen
import com.telen.namebattle.presentation.launch.LaunchScreen
import com.telen.namebattle.presentation.results.ResultsScreen
import com.telen.namebattle.presentation.search.SearchScreen
import com.telen.namebattle.presentation.setup.SetupScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onCreateSession = { navController.navigate(Screen.Setup.route) },
                onManageLists = { sessionId ->
                    navController.navigate(Screen.Auth(sessionId).route)
                },
                onLaunchBattle = { sessionId ->
                    navController.navigate(Screen.Launch(sessionId).route)
                },
                onResumeBattle = { sessionId ->
                    navController.navigate(Screen.Battle(sessionId).route)
                },
                onViewResults = { sessionId ->
                    navController.navigate(Screen.Results(sessionId).route)
                },
            )
        }

        composable(Screen.Setup.route) {
            SetupScreen(
                onSessionCreated = { sessionId ->
                    navController.navigate(Screen.Auth(sessionId).route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AUTH_ROUTE,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { back ->
            val sessionId = back.arguments?.getLong("sessionId") ?: 0L
            AuthScreen(
                sessionId = sessionId,
                onAuthenticated = { parentIndex ->
                    navController.navigate(Screen.Search(sessionId, parentIndex).route)
                },
                onLaunchBattle = {
                    navController.navigate(Screen.Launch(sessionId).route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SEARCH_ROUTE,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType },
                navArgument("parentIndex") { type = NavType.IntType }
            )
        ) { back ->
            val sessionId = back.arguments?.getLong("sessionId") ?: 0L
            val idx = back.arguments?.getInt("parentIndex") ?: 0
            SearchScreen(
                sessionId = sessionId,
                parentIndex = idx,
                onListValidated = {
                    navController.navigate(Screen.Auth(sessionId).route) {
                        popUpTo(Screen.Auth(sessionId).route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LAUNCH_ROUTE,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { back ->
            val sessionId = back.arguments?.getLong("sessionId") ?: 0L
            LaunchScreen(
                sessionId = sessionId,
                onBattleStarted = {
                    navController.navigate(Screen.Battle(sessionId).route) {
                        popUpTo(Screen.LAUNCH_ROUTE) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BATTLE_ROUTE,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { back ->
            val sessionId = back.arguments?.getLong("sessionId") ?: 0L
            BattleScreen(
                sessionId = sessionId,
                onBattleComplete = {
                    navController.navigate(Screen.Results(sessionId).route) {
                        popUpTo(Screen.BATTLE_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.RESULTS_ROUTE,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { back ->
            val sessionId = back.arguments?.getLong("sessionId") ?: 0L
            ResultsScreen(
                sessionId = sessionId,
                onNewSession = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onReplay = {
                    navController.navigate(Screen.Launch(sessionId).route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
    }
}
