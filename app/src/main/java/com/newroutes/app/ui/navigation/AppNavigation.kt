package com.newroutes.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.newroutes.app.ui.map.MapScreen
import com.newroutes.app.ui.navigation.SharedRouteConfig
import com.newroutes.app.ui.route.RouteScreen
import com.newroutes.app.ui.routes.SavedRoutesScreen
import com.newroutes.app.ui.summary.SummaryScreen
import com.newroutes.app.ui.vehicle.VehicleScreen

sealed class Screen(val route: String) {
    object Map : Screen("map")
    object Route : Screen("route")
    object Summary : Screen("summary/{routeId}")
    object Vehicle : Screen("vehicle")
    object Routes : Screen("routes")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    sharedConfig: SharedRouteConfig,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Map.route,
        modifier = modifier
    ) {
        composable(Screen.Map.route) {
            MapScreen(
                onNavigateToSummary = { route ->
                    navController.navigate("summary/${route.id}")
                },
                onNavigateToVehicle = {
                    navController.navigate(Screen.Vehicle.route)
                },
                sharedConfig = sharedConfig,
                navController = navController
            )
        }
        composable(Screen.Route.route) {
            RouteScreen(
                onWaypointsConfirmed = { waypoints, vehicle ->
                    sharedConfig.waypoints = waypoints
                    sharedConfig.vehicle = vehicle
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("summary/{routeId}") { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
            SummaryScreen(
                routeId = routeId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Vehicle.route) {
            VehicleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Routes.route) {
            SavedRoutesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRouteClick = { routeId ->
                    navController.navigate("summary/$routeId")
                }
            )
        }
    }
}
