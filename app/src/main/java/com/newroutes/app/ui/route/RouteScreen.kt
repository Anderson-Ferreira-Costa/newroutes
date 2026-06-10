package com.newroutes.app.ui.route

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.newroutes.app.domain.model.TollCategory
import com.newroutes.app.domain.model.Vehicle
import com.newroutes.app.domain.model.Waypoint

@Composable
fun RouteScreen(
    onWaypointsConfirmed: (List<Waypoint>, Vehicle) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Configurar Rota") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .safeDrawingPadding()
            ) {
                Button(
                    text = "Calcular Rota",
                    enabled = uiState.selectedVehicle != null,
                    onClick = {
                        uiState.selectedVehicle?.let { vehicle ->
                            onWaypointsConfirmed(uiState.waypoints, vehicle)
                        }
                        keyboardController?.hide()
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            VehicleSection(
                vehicles = uiState.vehicles,
                selectedVehicle = uiState.selectedVehicle,
                onSelectVehicle = viewModel::selectVehicle
            )

            Spacer(Modifier.height(24.dp))

            WaypointsSection(
                waypoints = uiState.waypoints,
                searchQuery = uiState.searchQuery,
                searchResults = uiState.searchResults,
                isSearching = uiState.isSearching,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onSearch = { viewModel.searchPlaces(uiState.searchQuery) },
                onClearSearch = { viewModel.onSearchQueryChanged("") },
                onAddWaypoint = viewModel::addWaypoint,
                onRemoveWaypoint = viewModel::removeWaypoint,
                onReorderWaypoints = viewModel::reorderWaypoints
            )
        }
    }
}

@Composable
private fun VehicleSection(
    vehicles: List<Vehicle>,
    selectedVehicle: Vehicle?,
    onSelectVehicle: (Vehicle) -> Unit
) {
    Column {
        Text(
            text = "Veículo",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        if (vehicles.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Nenhum veículo cadastrado. Adicione um veículo.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(vehicles) { _, vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        isSelected = vehicle.id == selectedVehicle?.id,
                        onClick = { onSelectVehicle(vehicle) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .width(140.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                getVehicleIcon(vehicle.category),
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = vehicle.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "1/${vehicle.fuelConsumptionKmPerLiter} km/l",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun getVehicleIcon(category: TollCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        TollCategory.MOTORCYCLE -> Icons.Default.Motorcycle
        TollCategory.CAR -> Icons.Default.DirectionsCar
        TollCategory.CAR_WITH_TRAILER -> Icons.Default.DirectionsCar
        TollCategory.TRUCK_2_AXLES,
        TollCategory.TRUCK_3_AXLES,
        TollCategory.TRUCK_4_AXLES,
        TollCategory.TRUCK_5_AXLES,
        TollCategory.TRUCK_6_AXLES -> Icons.Default.DirectionsBus
        TollCategory.BUS -> Icons.Default.DirectionsBus
    }
}

@Composable
private fun WaypointsSection(
    waypoints: List<Waypoint>,
    searchQuery: String,
    searchResults: List<Waypoint>,
    isSearching: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onAddWaypoint: (Waypoint) -> Unit,
    onRemoveWaypoint: (Waypoint) -> Unit,
    onReorderWaypoints: (Int, Int) -> Unit
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    Column {
        Text(
            text = "Paradas intermediárias",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Adicionar parada...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(Icons.Default.Delete, contentDescription = "Limpar")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        onSearch()
                    }
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (isSearching && searchQuery.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        if (searchResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(searchResults, key = { it.id }) { waypoint ->
                    SearchResultItem(
                        waypoint = waypoint,
                        onClick = { onAddWaypoint(waypoint) }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (waypoints.isEmpty()) {
            Text(
                text = "Nenhuma parada intermediária adicionada",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(waypoints, key = { index, waypoint -> "${index}-${waypoint.id}" }) { index, waypoint ->
                    WaypointItem(
                        index = index + 1,
                        waypoint = waypoint,
                        totalWaypoints = waypoints.size,
                        onRemove = { onRemoveWaypoint(waypoint) },
                        onMoveUp = {
                            if (index > 0) {
                                onReorderWaypoints(index, index - 1)
                            }
                        },
                        onMoveDown = {
                            if (index < waypoints.size - 1) {
                                onReorderWaypoints(index, index + 1)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    waypoint: Waypoint,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(
                text = waypoint.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (waypoint.address.isNotBlank()) {
                Text(
                    text = waypoint.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WaypointItem(
    index: Int,
    waypoint: Waypoint,
    totalWaypoints: Int,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.DragHandle,
            contentDescription = "Arrastar",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$index",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(2.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = waypoint.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (waypoint.address.isNotBlank()) {
                        Text(
                            text = waypoint.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onMoveUp, enabled = index > 1) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Mover para cima")
            }
            IconButton(onClick = onMoveDown, enabled = index < totalWaypoints) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Mover para baixo")
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun Button(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
