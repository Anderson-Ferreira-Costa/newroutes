package com.newroutes.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newroutes.app.data.geocoding.PhotonRepository
import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.model.Vehicle
import com.newroutes.app.domain.model.Waypoint
import com.newroutes.app.domain.usecase.CalculateRouteUseCase
import com.newroutes.app.domain.usecase.SaveRouteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val searchQuery: String = "",
    val searchResults: List<Waypoint> = emptyList(),
    val isSearching: Boolean = false,
    val selectedOrigin: Waypoint? = null,
    val selectedDestination: Waypoint? = null,
    val intermediateWaypoints: List<Waypoint> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val currentRoute: Route? = null,
    val encodedPolyline: String? = null,
    val isCalculatingRoute: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val photonRepository: PhotonRepository,
    private val calculateRouteUseCase: CalculateRouteUseCase,
    private val saveRouteUseCase: SaveRouteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Busca localidades usando Photon a partir de uma consulta de texto.
     */
    fun searchPlaces(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            photonRepository.searchPlaces(query)
                .onSuccess { results ->
                    _uiState.update {
                        it.copy(
                            searchResults = results,
                            isSearching = false
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            error = exception.message,
                            isSearching = false
                        )
                    }
                }
        }
    }

    /**
     * Seleciona um waypoint como origem da rota e limpa os resultados da busca.
     */
    fun selectOrigin(waypoint: Waypoint) {
        _uiState.update {
            it.copy(
                selectedOrigin = waypoint,
                searchResults = emptyList()
            )
        }
    }

    /**
     * Seleciona um waypoint como destino da rota e limpa os resultados da busca.
     */
    fun selectDestination(waypoint: Waypoint) {
        _uiState.update {
            it.copy(
                selectedDestination = waypoint,
                searchResults = emptyList()
            )
        }
    }

    /**
     * Adiciona um waypoint intermediário à lista de paradas.
     */
    fun addIntermediateWaypoint(waypoint: Waypoint) {
        _uiState.update {
            it.copy(
                intermediateWaypoints = it.intermediateWaypoints + waypoint
            )
        }
    }

    /**
     * Remove um waypoint intermediário da lista de paradas.
     */
    fun removeIntermediateWaypoint(waypoint: Waypoint) {
        _uiState.update {
            it.copy(
                intermediateWaypoints = it.intermediateWaypoints - waypoint
            )
        }
    }

    /**
     * Calcula a rota entre origem e destino (com paradas intermediárias),
     * usando CalculateRouteUseCase para orquestrar o cálculo.
     */
    fun calculateRoute() {
        val state = _uiState.value
        if (state.selectedOrigin == null || state.selectedDestination == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCalculatingRoute = true, error = null) }

            val waypoints = buildList {
                add(state.selectedOrigin!!)
                addAll(state.intermediateWaypoints)
                add(state.selectedDestination!!)
            }

            calculateRouteUseCase.invoke(waypoints, state.selectedVehicle)
                .onSuccess { route ->
                    _uiState.update {
                        it.copy(
                            currentRoute = route,
                            encodedPolyline = route.waypoints.getOrNull(route.waypoints.size - 1)?.let { wp ->
                                wp.latitude.toString() + "," + wp.longitude.toString()
                            },
                            isCalculatingRoute = false
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            error = exception.message,
                            isCalculatingRoute = false
                        )
                    }
                }
        }
    }

    /**
     * Salva a rota atualmente calculada no estado.
     */
    fun saveCurrentRoute() {
        val route = _uiState.value.currentRoute ?: return
        viewModelScope.launch {
            saveRouteUseCase.invoke(route)
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(error = exception.message)
                    }
                }
        }
    }

    /**
     * Limpa o campo de erro atual.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Reseta todos os dados da rota: origem, destino, paradas intermediárias,
     * rota atual e polyline codificada.
     */
    fun clearRoute() {
        _uiState.update {
            MapUiState()
        }
    }

    /**
     * Define o veículo selecionado a partir de uma configuração compartilhada.
     */
    fun setSelectedVehicle(vehicle: Vehicle) {
        _uiState.update { it.copy(selectedVehicle = vehicle) }
    }

    /**
     * Adiciona múltiplos waypoints intermediários de uma vez.
     */
    fun addIntermediateWaypoints(waypoints: List<Waypoint>) {
        _uiState.update {
            it.copy(
                intermediateWaypoints = it.intermediateWaypoints + waypoints
            )
        }
    }
}
