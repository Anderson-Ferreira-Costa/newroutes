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
    val originQuery: String = "",
    val originResults: List<Waypoint> = emptyList(),
    val isSearchingOrigin: Boolean = false,
    val destinationQuery: String = "",
    val destinationResults: List<Waypoint> = emptyList(),
    val isSearchingDestination: Boolean = false,
    val selectedOrigin: Waypoint? = null,
    val selectedDestination: Waypoint? = null,
    val intermediateWaypoints: List<Waypoint> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val currentRoute: Route? = null,
    val encodedPolyline: String? = null,
    val isCalculatingRoute: Boolean = false,
    val isSaved: Boolean = false,
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

    fun onOriginQueryChanged(query: String) {
        _uiState.update { it.copy(originQuery = query) }
    }

    fun onDestinationQueryChanged(query: String) {
        _uiState.update { it.copy(destinationQuery = query) }
    }

    fun clearOrigin() {
        _uiState.update {
            it.copy(
                selectedOrigin = null,
                originQuery = "",
                originResults = emptyList()
            )
        }
    }

    fun clearDestination() {
        _uiState.update {
            it.copy(
                selectedDestination = null,
                destinationQuery = "",
                destinationResults = emptyList()
            )
        }
    }

    fun searchOrigin(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingOrigin = true) }
            photonRepository.searchPlaces(query)
                .onSuccess { results ->
                    _uiState.update {
                        it.copy(
                            originResults = results,
                            isSearchingOrigin = false
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            error = exception.message,
                            isSearchingOrigin = false
                        )
                    }
                }
        }
    }

    fun searchDestination(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingDestination = true) }
            photonRepository.searchPlaces(query)
                .onSuccess { results ->
                    _uiState.update {
                        it.copy(
                            destinationResults = results,
                            isSearchingDestination = false
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            error = exception.message,
                            isSearchingDestination = false
                        )
                    }
                }
        }
    }

    fun selectOriginResult(waypoint: Waypoint) {
        _uiState.update {
            it.copy(
                selectedOrigin = waypoint,
                originQuery = "",
                originResults = emptyList()
            )
        }
    }

    fun selectDestinationResult(waypoint: Waypoint) {
        _uiState.update {
            it.copy(
                selectedDestination = waypoint,
                destinationQuery = "",
                destinationResults = emptyList()
            )
        }
    }

    fun clearOriginSearchResults() {
        _uiState.update {
            it.copy(
                originResults = emptyList()
            )
        }
    }

    fun clearDestinationSearchResults() {
        _uiState.update {
            it.copy(
                destinationResults = emptyList()
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
                    saveRouteUseCase.invoke(route)
                        .onSuccess {
                            _uiState.update {
                                it.copy(
                                    currentRoute = route,
                                    encodedPolyline = route.encodedPolyline,
                                    isCalculatingRoute = false,
                                    isSaved = true
                                )
                            }
                        }
                        .onFailure { saveError ->
                            _uiState.update {
                                it.copy(
                                    currentRoute = route,
                                    encodedPolyline = route.encodedPolyline,
                                    isCalculatingRoute = false,
                                    isSaved = false,
                                    error = "Rota calculada mas não foi possível salvar: ${saveError.message}"
                                )
                            }
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
            it.copy(
                selectedOrigin = null,
                selectedDestination = null,
                intermediateWaypoints = emptyList(),
                currentRoute = null,
                encodedPolyline = null,
                isSaved = false,
                originQuery = "",
                destinationQuery = "",
                originResults = emptyList(),
                destinationResults = emptyList()
            )
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
