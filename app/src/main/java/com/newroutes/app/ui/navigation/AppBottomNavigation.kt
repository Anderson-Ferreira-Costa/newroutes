package com.newroutes.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class NavDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Map : NavDestination("map", "Mapa", Icons.Default.Map)
    object Vehicle : NavDestination("vehicle", "Veículos", Icons.Default.DirectionsCar)
    object Routes : NavDestination("routes", "Rotas", Icons.Default.Route)
}

@Composable
fun AppBottomNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val destinations = listOf(NavDestination.Map, NavDestination.Vehicle, NavDestination.Routes)

    BottomAppBar(
        modifier = modifier
    ) {
        destinations.forEach { destination ->
            val currentRoute = currentDestination?.route
            val isSelected = when (destination.route) {
                "map" -> currentRoute == "map"
                "vehicle" -> currentRoute == "vehicle"
                "routes" -> currentRoute == "routes" || currentRoute == "route" || currentRoute?.startsWith("summary/") == true
                else -> false
            }

            NavigationBarItem(
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(destination.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    }
                }
            )
        }
    }
}
