package com.newroutes.app.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.newroutes.app.domain.model.Waypoint
import com.newroutes.app.domain.model.Route

data class SharedRouteConfig(
    var waypoints: List<Waypoint> = emptyList(),
    var vehicle: com.newroutes.app.domain.model.Vehicle? = null
)
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.Overlay
import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.core.content.ContextCompat

@Composable
fun MapScreen(
    onNavigateToSummary: (Route) -> Unit,
    sharedConfig: SharedRouteConfig,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
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
            update = { mapView ->
                mapView.overlays.clear()

                uiState.selectedOrigin?.let { waypoint ->
                    mapView.overlays.add(createMarker(mapView.context, waypoint, android.R.drawable.star_big_on))
                }

                uiState.selectedDestination?.let { waypoint ->
                    mapView.overlays.add(createMarker(mapView.context, waypoint, android.R.drawable.pin_dark))
                }

                uiState.encodedPolyline?.let { polyline ->
                    val points = decodePolyline(polyline)
                    if (points.isNotEmpty()) {
                        mapView.overlays.add(createPolylineOverlay(points))
                    }
                }

                mapView.invalidate()
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChanged = viewModel::onSearchQueryChanged,
                onSearch = viewModel::searchPlaces,
                onClear = { viewModel.onSearchQueryChanged("") }
            )

            if (uiState.searchResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.95f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(uiState.searchResults, key = { it.id }) { waypoint ->
                        SearchResultItem(
                            waypoint = waypoint,
                            hasOrigin = uiState.selectedOrigin != null,
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
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Directions, contentDescription = "Calcular rota")
                }
            }
        }

        uiState.currentRoute?.let { route ->
            RouteBottomSheet(
                route = route,
                onNavigateToSummary = onNavigateToSummary,
                onSaveRoute = viewModel::saveCurrentRoute
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        TextField(
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
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SearchResultItem(
    waypoint: Waypoint,
    hasOrigin: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = waypoint.name,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            if (waypoint.address.isNotBlank()) {
                Text(
                    text = waypoint.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun RouteBottomSheet(
    route: Route,
    onNavigateToSummary: (Route) -> Unit,
    onSaveRoute: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f km".format(route.distanceMeters / 1000.0),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Distância", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${route.durationSeconds / 60} min",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Duração", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "R$ %.2f".format(route.totalCost),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Custo total", style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    text = "Ver detalhes",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSummary(route) }
                )
                Button(
                    text = "Salvar rota",
                    modifier = Modifier.weight(1f),
                    onClick = onSaveRoute
                )
            }
        }
    }
}

@Composable
private fun Button(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text)
    }
}

private fun createMarker(context: Context, waypoint: Waypoint, drawableRes: Int): ItemizedIconOverlay<com.osmdroid.views.overlay.Marker> {
    val icon = ContextCompat.getDrawable(context, drawableRes)
    val marker = com.osmdroid.views.overlay.Marker(MapView(context)).apply {
        position = GeoPoint(waypoint.latitude, waypoint.longitude)
        setIcon(icon)
        title = waypoint.name
    }
    val list = listOf(marker)
    return ItemizedIconOverlay(list, marker) { /* no onclick needed */ }
}

private fun createPolylineOverlay(points: List<GeoPoint>): Overlay {
    return object : Overlay(points) {
        override fun onDraw(p0: android.graphics.Canvas?, p1: org.osmdroid.api.IGeoPoint?, p2: org.osmdroid.views.Projection?) {
            super.onDraw(p0, p1, p2)
        }
    }
}

private fun decodePolyline(encoded: String): List<GeoPoint> {
    val points = mutableListOf<GeoPoint>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        do {
            b = encoded[index++].code - 63
            lat = lat or (b and 0x1f shl shift)
            shift += 5
        } while (b and 0x20 != 0)

        shift = 0
        do {
            b = encoded[index++].code - 63
            lng = lng or (b and 0x1f shl shift)
            shift += 5
        } while (b and 0x20 != 0)

        val latitude = (lat shr 1).toDouble() * -0.0000001
        val longitude = (lng shr 1).toDouble() * -0.0000001
        points.add(GeoPoint(latitude, longitude))
        lat = 0
        lng = 0
    }

    return points
}
