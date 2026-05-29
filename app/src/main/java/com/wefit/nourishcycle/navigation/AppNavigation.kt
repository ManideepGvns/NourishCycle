package com.wefit.nourishcycle.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wefit.nourishcycle.ui.home.HomeScreen
import com.wefit.nourishcycle.ui.insights.InsightsScreen
import com.wefit.nourishcycle.ui.splash.SplashScreen

private object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val INSIGHTS = "insights"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        // ── Splash ───────────────────────────────────────────────────
        composable(
            route = Routes.SPLASH,
            exitTransition = { fadeOut(tween(300)) }
        ) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        // Remove splash from back stack — infiniteTransition disposed on exit
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ─────────────────────────────────────────────────────
        composable(
            route = Routes.HOME,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = {
                // Home exits upward when Insights opens
                slideOutVertically(
                    targetOffsetY = { -it / 4 },
                    animationSpec = tween(350)
                ) + fadeOut(tween(350))
            },
            popEnterTransition = {
                // Home re-enters when popping back from Insights
                slideInVertically(
                    initialOffsetY = { -it / 4 },
                    animationSpec = tween(350)
                ) + fadeIn(tween(350))
            }
        ) {
            HomeScreen(
                onNavigateToInsights = {
                    navController.navigate(Routes.INSIGHTS)
                }
            )
        }

        // ── Insights ─────────────────────────────────────────────────
        composable(
            route = Routes.INSIGHTS,
            enterTransition = {
                // Insights slides up from below
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(400)
                ) + fadeIn(tween(400))
            },
            exitTransition = {
                // Insights slides down on back
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(350)
                ) + fadeOut(tween(350))
            }
        ) {
            InsightsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
