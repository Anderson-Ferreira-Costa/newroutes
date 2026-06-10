package com.newroutes.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.newroutes.app.ui.map.MapScreen
import com.newroutes.app.ui.route.RouteScreen
import com.newroutes.app.ui.summary.SummaryScreen

sealed class Screen(val route: String) {
    object Map : Screen("map")
    object Route : Screen("route")
    object Summary : Screen("summary")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Map.route,
        modifier = modifier
    ) {
        composable(Screen.Map.route) {
            MapScreen(
                onRouteSelected = {
                    navController.navigate(Screen.Route.route)
                }
            )
        }
        composable(Screen.Route.route) {
            RouteScreen(
                onConfirm = {
                    navController.navigate(Screen.Summary.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Summary.route) {
            SummaryScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
