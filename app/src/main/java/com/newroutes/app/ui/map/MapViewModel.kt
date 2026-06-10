package com.newroutes.app.ui.map

// TODO: ViewModel da tela de mapa
// Deve gerenciar: posição do mapa, markers de waypoints, estado de loading
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val centerLat: Double = -14.235,
    val centerLng: Double = -51.9253,
    val zoom: Float = 4f,
    val waypoints: List<WaypointUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class WaypointUi(
    val id: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val name: String = "",
    val order: Int = 0
)

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    // TODO: Centralizar mapa em posição
    fun animateTo(lat: Double, lng: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(centerLat = lat, centerLng = lng)
        }
    }

    // TODO: Adicionar waypoint ao mapa
    fun addWaypoint(lat: Double, lng: Double, name: String) {
        viewModelScope.launch {
            // TODO: Adicionar waypoint à lista
        }
    }

    // TODO: Remover waypoint do mapa
    fun removeWaypoint(id: String) {
        viewModelScope.launch {
            // TODO: Remover waypoint da lista
        }
    }
}
