package com.newroutes.app.ui.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.model.TollCategory
import com.newroutes.app.domain.model.Vehicle
import com.newroutes.app.domain.model.Waypoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

data class SharedRouteConfig(
    var waypoints: List<Waypoint> = emptyList(),
    var vehicle: Vehicle? = null
)

@Composable
fun MapScreen(
    onNavigateToSummary: (Route) -> Unit,
    onNavigateToVehicle: () -> Unit,
    sharedConfig: SharedRouteConfig,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var sheetExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(sharedConfig) {
        sharedConfig.waypoints.forEach { waypoint ->
            if (waypoint !in uiState.intermediateWaypoints) {
                viewModel.addIntermediateWaypoint(waypoint)
            }
        }
        if (sharedConfig.waypoints.isNotEmpty()) {
            sharedConfig.waypoints = emptyList()
        }
        sharedConfig.vehicle?.let { vehicle ->
            viewModel.setSelectedVehicle(vehicle)
            sharedConfig.vehicle = null
        }
    }

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

    val mapView = remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(uiState.selectedOrigin, uiState.selectedDestination) {
        val waypoint = uiState.selectedOrigin ?: uiState.selectedDestination
        val map = mapView.value ?: return@LaunchedEffect
        waypoint?.let { wp ->
            map.controller.animateTo(GeoPoint(wp.latitude, wp.longitude))
            map.controller.setZoom(13.0)
        }
        viewModel.onSearchSelectionMade()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { context ->
                Configuration.getInstance().load(context, android.preference.PreferenceManager.getDefaultSharedPreferences(context))
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(5.0)
                    controller.setCenter(GeoPoint(-15.0, -47.0))
                }
            },
            update = { map ->
                mapView.value = map
                map.overlays.clear()

                val waypointsWithIcons = mutableListOf<Pair<Waypoint, Int>>()
                uiState.selectedOrigin?.let { waypoint ->
                    waypointsWithIcons.add(waypoint to android.R.drawable.star_big_on)
                }
                uiState.selectedDestination?.let { waypoint ->
                    waypointsWithIcons.add(waypoint to android.R.drawable.sym_def_app_icon)
                }
                if (waypointsWithIcons.isNotEmpty()) {
                    map.overlays.add(createMarkersOverlay(map.context, waypointsWithIcons))
                }

                uiState.encodedPolyline?.let { polyline ->
                    val points = decodePolyline(polyline)
                    if (points.isNotEmpty()) {
                        map.overlays.add(createPolylineOverlay(points))
                    }
                }

                map.invalidate()
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mapa",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onNavigateToVehicle) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = "Veículos",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp)
                    )
                }
            }

            if (uiState.selectedOrigin == null || uiState.selectedDestination == null) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged,
                    onSearch = viewModel::searchPlaces,
                    onClear = { viewModel.onSearchQueryChanged("") }
                )

                SelectionChips(
                    selectedOrigin = uiState.selectedOrigin,
                    selectedDestination = uiState.selectedDestination,
                    onClearOrigin = viewModel::clearOrigin,
                    onClearDestination = viewModel::clearDestination
                )

                if (uiState.searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(uiState.searchResults, key = { it.id }) { waypoint ->
                            SearchResultItem(
                                waypoint = waypoint,
                                isOrigin = uiState.selectedOrigin?.id == waypoint.id,
                                isDestination = uiState.selectedDestination?.id == waypoint.id,
                                onClick = {
                                    if (uiState.selectedOrigin == null) {
                                        viewModel.selectOrigin(waypoint)
                                    } else if (uiState.selectedDestination == null) {
                                        viewModel.selectDestination(waypoint)
                                    } else {
                                        viewModel.addIntermediateWaypoint(waypoint)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.selectedOrigin != null && uiState.selectedDestination != null && uiState.currentRoute == null) {
            FloatingActionButton(
                onClick = { viewModel.calculateRoute() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (uiState.isCalculatingRoute) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Directions, contentDescription = "Calcular rota", tint = Color.White)
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        MapBottomSheet(
            uiState = uiState,
            sheetExpanded = sheetExpanded,
            onSheetExpandedChanged = { sheetExpanded = it },
            viewModel = viewModel,
            onNavigateToSummary = onNavigateToSummary,
            onNavigateToVehicle = onNavigateToVehicle,
            onNavigateToRoutes = { navController.navigate("routes") { launchSingleTop = true } }
        )
    }
}

@Composable
private fun MapBottomSheet(
    uiState: MapUiState,
    sheetExpanded: Boolean,
    onSheetExpandedChanged: (Boolean) -> Unit,
    viewModel: MapViewModel,
    onNavigateToSummary: (Route) -> Unit,
    onNavigateToVehicle: () -> Unit,
    onNavigateToRoutes: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            if (uiState.currentRoute != null) {
                RouteSummaryCompact(
                    route = uiState.currentRoute,
                    onNavigateToSummary = onNavigateToSummary,
                    onSaveRoute = viewModel::saveCurrentRoute,
                    onExpandChanged = onSheetExpandedChanged
                )
            } else {
           BottomSheetSearchContent(
                uiState = uiState,
                sheetExpanded = sheetExpanded,
                onSheetExpandedChanged = onSheetExpandedChanged,
                viewModel = viewModel,
                onNavigateToSummary = onNavigateToSummary,
                onNavigateToVehicle = onNavigateToVehicle,
                onNavigateToRoutes = onNavigateToRoutes
            )
            }
        }
    }
}

@Composable
private fun BottomSheetSearchContent(
    uiState: MapUiState,
    sheetExpanded: Boolean,
    onSheetExpandedChanged: (Boolean) -> Unit,
    viewModel: MapViewModel,
    onNavigateToSummary: (Route) -> Unit,
    onNavigateToVehicle: () -> Unit,
    onNavigateToRoutes: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var expandedOrigin by remember { mutableStateOf(false) }
    var expandedDestination by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (!sheetExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = { Text("Buscar origem ou destino...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { if (uiState.searchQuery.isNotBlank()) viewModel.searchPlaces(uiState.searchQuery) }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            expandedOrigin = true
                            expandedDestination = false
                            onSheetExpandedChanged(true)
                        },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true,
                    readOnly = true
                )
            }

            SelectionChipsCompact(
                selectedOrigin = uiState.selectedOrigin,
                selectedDestination = uiState.selectedDestination,
                onClearOrigin = viewModel::clearOrigin,
                onClearDestination = viewModel::clearDestination
            )

            uiState.currentRoute?.let { route ->
                Spacer(Modifier.height(8.dp))
                RouteSummaryCompact(
                    route = route,
                    onNavigateToSummary = onNavigateToSummary,
                    onSaveRoute = viewModel::saveCurrentRoute,
                    onExpandChanged = null
                )
            }

            uiState.selectedVehicle?.let { vehicle ->
                Spacer(Modifier.height(8.dp))
                VehicleCompactCard(vehicle = vehicle)
            }

            if (uiState.selectedOrigin != null && uiState.selectedDestination != null) {
                Spacer(Modifier.height(8.dp))
                CalculateRouteButton(
                    enabled = true,
                    isCalculating = uiState.isCalculatingRoute,
                    onClick = { viewModel.calculateRoute() }
                )
                Spacer(Modifier.height(8.dp))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selecionar Local",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = {
                    onSheetExpandedChanged(false)
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }) {
                    Icon(Icons.Default.Clear, contentDescription = "Recolher")
                }
            }

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Buscar localização...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.onSearchQueryChanged("")
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { if (uiState.searchQuery.isNotBlank()) viewModel.searchPlaces(uiState.searchQuery) }
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            if (uiState.isSearching && uiState.searchQuery.isNotBlank()) {
                Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center).size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            if (uiState.searchResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(uiState.searchResults, key = { it.id }) { waypoint ->
                        SearchResultItemCompact(
                            waypoint = waypoint,
                            isSelected = false,
                            onClick = {
                                if (uiState.selectedOrigin == null) {
                                    viewModel.selectOrigin(waypoint)
                                } else if (uiState.selectedDestination == null) {
                                    viewModel.selectDestination(waypoint)
                                } else {
                                    viewModel.addIntermediateWaypoint(waypoint)
                                }
                            }
                        )
                    }
                }
            }

            uiState.selectedOrigin?.let { wp ->
                SelectableChip(
                    label = "De: ${wp.name}",
                    onClear = viewModel::clearOrigin
                )
            }
            uiState.selectedDestination?.let { wp ->
                SelectableChip(
                    label = "Para: ${wp.name}",
                    onClear = viewModel::clearDestination
                )
            }

            uiState.selectedVehicle?.let { vehicle ->
                Spacer(Modifier.height(8.dp))
                VehicleCompactCard(vehicle = vehicle)
            }

            uiState.currentRoute?.let { route ->
                Spacer(Modifier.height(8.dp))
                RouteSummaryCompact(
                    route = route,
                    onNavigateToSummary = onNavigateToSummary,
                    onSaveRoute = viewModel::saveCurrentRoute,
                    onExpandChanged = null
                )
            }

            if (uiState.selectedOrigin != null && uiState.selectedDestination != null) {
                Spacer(Modifier.height(8.dp))
                CalculateRouteButton(
                    enabled = true,
                    isCalculating = uiState.isCalculatingRoute,
                    onClick = { viewModel.calculateRoute() }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RouteSummaryCompact(
    route: Route,
    onNavigateToSummary: (Route) -> Unit,
    onSaveRoute: () -> Unit,
    onExpandChanged: ((Boolean) -> Unit)? = null
) {
    val durationText = formatDuration(route.durationSeconds)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%.1f km".format(route.distanceMeters / 1000.0),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Distância", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Duração", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "R$ %.2f".format(route.totalCost),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Custo", style = MaterialTheme.typography.labelSmall)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.Button(
                onClick = { onNavigateToSummary(route) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ver detalhes")
            }
            androidx.compose.material3.Button(
                onClick = onSaveRoute,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Salvar")
            }
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    onClear: () -> Unit
) {
    FilterChip(
        selected = true,
        onClick = onClear,
        label = { Text(label, maxLines = 1) },
        leadingIcon = {
            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    )
}

@Composable
private fun VehicleCompactCard(vehicle: Vehicle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    getVehicleIcon(vehicle.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "%.1f km/l".format(vehicle.fuelConsumptionKmPerLiter),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "R$ ${"%.2f".format(vehicle.fuelPricePerLiter)}/l",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculateRouteButton(
    enabled: Boolean,
    isCalculating: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled && !isCalculating,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isCalculating) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(
                "Calcular Rota",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SelectionChipsCompact(
    selectedOrigin: Waypoint?,
    selectedDestination: Waypoint?,
    onClearOrigin: () -> Unit,
    onClearDestination: () -> Unit
) {
    if (selectedOrigin == null && selectedDestination == null) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        selectedOrigin?.let { wp ->
            FilterChip(
                selected = true,
                onClick = onClearOrigin,
                label = {
                    Text("De: ${wp.name.take(25)}", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                },
                leadingIcon = {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                },
                modifier = Modifier.weight(1f)
            )
        }
        selectedDestination?.let { wp ->
            FilterChip(
                selected = true,
                onClick = onClearDestination,
                label = {
                    Text("Para: ${wp.name.take(25)}", maxLines = 1, style = MaterialTheme.typography.labelSmall)
                },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClear: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        androidx.compose.material3.TextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = {
                Text("Buscar localização...")
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        onClear()
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (query.isNotBlank()) {
                        onSearch(query)
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SelectionChips(
    selectedOrigin: Waypoint?,
    selectedDestination: Waypoint?,
    onClearOrigin: () -> Unit,
    onClearDestination: () -> Unit
) {
    if (selectedOrigin == null && selectedDestination == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        selectedOrigin?.let { wp ->
            FilterChip(
                selected = true,
                onClick = onClearOrigin,
                label = {
                    Text("De: ${wp.name.take(30)}", maxLines = 1)
                },
                leadingIcon = {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier.weight(1f)
            )
        }
        selectedDestination?.let { wp ->
            FilterChip(
                selected = true,
                onClick = onClearDestination,
                label = {
                    Text("Para: ${wp.name.take(30)}", maxLines = 1)
                },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SearchResultItem(
    waypoint: Waypoint,
    isOrigin: Boolean,
    isDestination: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isOrigin) {
                    Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                } else if (isDestination) {
                    Modifier.background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = when {
                isOrigin -> MaterialTheme.colorScheme.primary
                isDestination -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = waypoint.name,
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isOrigin -> MaterialTheme.colorScheme.onPrimaryContainer
                    isDestination -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> Color.Black
                }
            )
            if (waypoint.address.isNotBlank()) {
                Text(
                    text = waypoint.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        when {
            isOrigin -> Icon(Icons.Default.Star, contentDescription = "Origem", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            isDestination -> Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Destino", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SearchResultItemCompact(
    waypoint: Waypoint,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = waypoint.name,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            if (waypoint.address.isNotBlank()) {
                Text(
                    text = waypoint.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
private fun getVehicleIcon(category: TollCategory): ImageVector {
    return when (category) {
        TollCategory.MOTORCYCLE -> Icons.Default.Motorcycle
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

private fun createMarkersOverlay(context: Context, waypointsWithIcons: List<Pair<Waypoint, Int>>): Overlay {
    return object : Overlay() {
        override fun draw(c: Canvas?, mapView: MapView?, shadow: Boolean) {
            if (shadow || c == null || mapView == null) return
            val projection = mapView.projection
            if (projection == null) return
            for ((waypoint, drawableRes) in waypointsWithIcons) {
                val geoPoint = GeoPoint(waypoint.latitude, waypoint.longitude)
                val pixel = projection.toPixels(geoPoint, null) ?: continue
                val icon = ContextCompat.getDrawable(context, drawableRes) ?: continue
                val iconWidth = icon.intrinsicWidth.takeIf { it > 0 } ?: 32
                val iconHeight = icon.intrinsicHeight.takeIf { it > 0 } ?: 48
                icon.setBounds(
                    pixel.x - iconWidth / 2,
                    pixel.y - iconHeight,
                    pixel.x + iconWidth / 2,
                    pixel.y
                )
                icon.draw(c)
                val textPaint = Paint().apply {
                    color = AndroidColor.BLACK
                    textSize = 32f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                val bounds = Rect()
                textPaint.getTextBounds(waypoint.name, 0, waypoint.name.length, bounds)
                val textX = pixel.x.toFloat()
                val textY = pixel.y.toFloat() - iconHeight - 8f
                c.drawText(waypoint.name, textX, textY, textPaint)
            }
        }
    }
}

private fun createPolylineOverlay(points: List<GeoPoint>): Overlay {
    return object : Overlay() {
        override fun draw(c: android.graphics.Canvas?, mapView: MapView?, shadow: Boolean) {
            super.draw(c, mapView, shadow)
            if (shadow || c == null || points.size < 2) return
            val projection = mapView?.projection
            if (projection == null) return
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                strokeWidth = 8f
                color = AndroidColor.parseColor("#3F51B5")
                style = android.graphics.Paint.Style.STROKE
            }
            for (i in 0 until points.size - 1) {
                val from = projection.toPixels(points[i], null)
                val to = projection.toPixels(points[i + 1], null)
                if (from != null && to != null) {
                    c.drawLine(from.x.toFloat(), from.y.toFloat(), to.x.toFloat(), to.y.toFloat(), paint)
                }
            }
        }
    }
}

private fun decodePolyline(encoded: String): List<GeoPoint> {
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0
    val poly = mutableListOf<GeoPoint>()

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            if (index >= len) return poly
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dLat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dLat

        shift = 0
        result = 0
        do {
            if (index >= len) return poly
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dLng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dLng

        poly.add(GeoPoint(lat / 1e5, lng / 1e5))
    }
    return poly
}
