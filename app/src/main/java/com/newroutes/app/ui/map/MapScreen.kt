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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.text.style.TextOverflow
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
        viewModel.clearOriginSearchResults()
        viewModel.clearDestinationSearchResults()
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            Text(
                text = "Mapa",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
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
                    isSaved = uiState.isSaved
                )
            } else {
                BottomSheetSearchContent(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun BottomSheetSearchContent(
    uiState: MapUiState,
    viewModel: MapViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // === CAMPO ORIGEM ===
        if (uiState.selectedOrigin != null) {
            OriginChip(
                waypoint = uiState.selectedOrigin,
                onRemove = viewModel::clearOrigin
            )
        } else {
            OutlinedTextField(
                value = uiState.originQuery,
                onValueChange = viewModel::onOriginQueryChanged,
                label = { Text("Origem") },
                placeholder = { Text("De onde você está saindo?") },
                leadingIcon = {
                    Icon(
                        Icons.Default.RadioButtonChecked,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                },
                trailingIcon = {
                    if (uiState.originQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onOriginQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.searchOrigin(uiState.originQuery) }
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (uiState.originResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(uiState.originResults, key = { it.id }) { waypoint ->
                        ListItem(
                            headlineContent = { Text(waypoint.name) },
                            supportingContent = { Text(waypoint.address) },
                            leadingContent = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectOriginResult(waypoint)
                                }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // === CAMPO DESTINO ===
        if (uiState.selectedDestination != null) {
            DestinationChip(
                waypoint = uiState.selectedDestination,
                onRemove = viewModel::clearDestination
            )
        } else {
            OutlinedTextField(
                value = uiState.destinationQuery,
                onValueChange = viewModel::onDestinationQueryChanged,
                label = { Text("Destino") },
                placeholder = { Text("Para onde você vai?") },
                leadingIcon = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFE53935)
                    )
                },
                trailingIcon = {
                    if (uiState.destinationQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onDestinationQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.searchDestination(uiState.destinationQuery) }
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (uiState.destinationResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(uiState.destinationResults, key = { it.id }) { waypoint ->
                        ListItem(
                            headlineContent = { Text(waypoint.name) },
                            supportingContent = { Text(waypoint.address) },
                            leadingContent = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectDestinationResult(waypoint)
                                }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        // === VEÍCULO SELECIONADO ===
        uiState.selectedVehicle?.let { vehicle ->
            Spacer(Modifier.height(16.dp))
            VehicleCompactCard(vehicle = vehicle)
        }

        // === BOTÃO CALCULAR ===
        if (uiState.selectedOrigin != null && uiState.selectedDestination != null) {
            Spacer(Modifier.height(16.dp))
            CalculateRouteButton(
                enabled = true,
                isCalculating = uiState.isCalculatingRoute,
                onClick = { viewModel.calculateRoute() }
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun OriginChip(waypoint: Waypoint, onRemove: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.RadioButtonChecked,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "De: ${waypoint.name}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remover origem")
            }
        }
    }
}

@Composable
private fun DestinationChip(waypoint: Waypoint, onRemove: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFE53935)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Para: ${waypoint.name}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remover destino")
            }
        }
    }
}

@Composable
private fun RouteSummaryCompact(
    route: Route,
    onNavigateToSummary: (Route) -> Unit,
    isSaved: Boolean
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Button(
                onClick = { onNavigateToSummary(route) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ver detalhes")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isSaved) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Salva",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Não salva",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
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
