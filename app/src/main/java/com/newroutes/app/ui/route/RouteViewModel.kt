package com.newroutes.app.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: ViewModel da tela de rota
data class RouteUiState(
    val waypoints: List<WaypointUi> = emptyList(),
    val distanceText: String = "",
    val durationText: String = "",
    val estimatedCost: Double = 0.0,
    val tolls: List<TollPlazaUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class WaypointUi(
    val id: String = "",
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val order: Int = 0
)

data class TollPlazaUi(
    val name: String = "",
    val highway: String = "",
    val cost: Double = 0.0,
    val order: Int = 0
)

@HiltViewModel
class RouteViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState

    // TODO: Calcular detalhes da rota
    fun calculateRouteDetails() {
        // TODO: Calcular distância, tempo, pedágios
    }

    // TODO: Confirmar rota e navegar para summary
    fun confirmRoute() {
        // TODO: Salvar rota e navegar
    }
}
