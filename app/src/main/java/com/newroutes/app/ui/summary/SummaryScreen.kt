package com.newroutes.app.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.model.TollCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    routeId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    androidx.compose.material3.Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Resumo da Rota") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null && uiState.route == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        TextButton(onClick = onNavigateBack) {
                            Text("Voltar")
                        }
                    }
                }
            }

            uiState.route != null -> {
                SummaryContent(
                    route = uiState.route!!,
                    isSaved = uiState.isSaved,
                    isSaving = uiState.isSaving,
                    onSaveRoute = viewModel::saveRoute,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun SummaryContent(
    route: Route,
    isSaved: Boolean,
    isSaving: Boolean,
    onSaveRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            MetricCards(route = route)
        }

        item {
            CostBreakdown(route = route)
        }

        item {
            VehicleCard(route = route)
        }

        item {
            ItineraryCard(route = route)
        }

        item {
            SaveButton(
                isSaved = isSaved,
                isSaving = isSaving,
                onSaveRoute = onSaveRoute
            )
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun MetricCards(route: Route) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricCard(
            icon = Icons.Default.Route,
            value = "%.1f km".format(route.distanceMeters / 1000.0),
            label = "Distância",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            icon = Icons.Default.Schedule,
            value = formatDuration(route.durationSeconds),
            label = "Tempo estimado",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            icon = Icons.Default.AttachMoney,
            value = "R$ %.2f".format(route.totalCost),
            label = "Custo total",
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color? = null
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = containerColor ?: MaterialTheme.colorScheme.surface
    )
    Card(
        modifier = modifier,
        colors = cardColors
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (containerColor != null) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> {
            if (minutes > 0) "${hours}h ${minutes}min" else "${hours}h"
        }
        else -> "${minutes} min"
    }
}

@Composable
private fun CostBreakdown(route: Route) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Detalhamento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))

            ListItem(
                headlineContent = { Text("Combustível") },
                supportingContent = { Text("R$ %.2f".format(route.totalFuelCost)) },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )
            Divider()

            ListItem(
                headlineContent = {
                    Text(
                        "Total",
                        fontWeight = FontWeight.Bold
                    )
                },
                supportingContent = {
                    Text(
                        "R$ %.2f".format(route.totalCost),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun VehicleCard(route: Route) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Veículo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            ListItem(
                headlineContent = { Text(route.vehicle.name) },
                supportingContent = {
                    Text(
                        "${route.vehicle.fuelConsumptionKmPerLiter} km/l · " +
                        "R$ ${"%.2f".format(route.vehicle.fuelPricePerLiter)}/l"
                    )
                },
                leadingContent = {
                    Icon(
                        getVehicleIcon(route.vehicle.category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun getVehicleIcon(category: TollCategory): ImageVector {
    return when (category) {
        TollCategory.MOTORCYCLE -> Icons.Default.TwoWheeler
        TollCategory.CAR -> Icons.Default.DirectionsCar
        TollCategory.CAR_WITH_TRAILER -> Icons.Default.DirectionsCar
        TollCategory.TRUCK_2_AXLES,
        TollCategory.TRUCK_3_AXLES,
        TollCategory.TRUCK_4_AXLES,
        TollCategory.TRUCK_5_AXLES,
        TollCategory.TRUCK_6_AXLES -> Icons.Default.LocalShipping
        TollCategory.BUS -> Icons.Default.DirectionsBus
    }
}

@Composable
private fun ItineraryCard(route: Route) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Itinerário",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            route.waypoints.forEachIndexed { index, waypoint ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = when (index) {
                                0 -> Icons.Default.RadioButtonChecked
                                route.waypoints.size - 1 -> Icons.Default.LocationOn
                                else -> Icons.Default.Circle
                            },
                            contentDescription = null,
                            tint = when (index) {
                                0 -> Color(0xFF4CAF50)
                                route.waypoints.size - 1 -> Color(0xFFE53935)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(
                                when (index) {
                                    0 -> 24.dp
                                    route.waypoints.size - 1 -> 24.dp
                                    else -> 12.dp
                                }
                            )
                        )
                        if (index < route.waypoints.size - 1) {
                            Spacer(Modifier.height(16.dp))
                            Divider(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }

                    Text(
                        text = waypoint.name.take(50),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveButton(
    isSaved: Boolean,
    isSaving: Boolean,
    onSaveRoute: () -> Unit
) {
    if (isSaved) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Rota salva",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4CAF50)
            )
        }
    } else {
        OutlinedButton(
            onClick = onSaveRoute,
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Salvando...")
            } else {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Salvar rota")
            }
        }
    }
}
