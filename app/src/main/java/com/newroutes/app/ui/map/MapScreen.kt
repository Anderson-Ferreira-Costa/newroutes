package com.newroutes.app.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

// TODO: Tela de mapa com OSMDroid
// Deve exibir: MapView com tiles OpenStreetMap, markers de waypoints, botões de ação
@Composable
fun MapScreen(
    onRouteSelected: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    // TODO: Collect uiState e renderizar:
    // - MapView do OSMDroid
    // - Waypoints como markers
    // - FAB para adicionar waypoint
    // - Botão para calcular rota
}
