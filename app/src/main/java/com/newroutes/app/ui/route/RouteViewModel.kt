package com.newroutes.app.ui.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newroutes.app.data.geocoding.NominatimRepository
import com.newroutes.app.domain.model.Vehicle
import com.newroutes.app.domain.model.Waypoint
import com.newroutes.app.domain.usecase.ManageVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouteUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val searchQuery: String = "",
    val searchResults: List<Waypoint> = emptyList(),
    val isSearching: Boolean = false,
    val waypoints: List<Waypoint> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val manageVehicleUseCase: ManageVehicleUseCase,
    private val nominatimRepository: NominatimRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            manageVehicleUseCase.getAll().collect { vehicles ->
                _uiState.update { it.copy(vehicles = vehicles) }
            }
        }
    }

    /**
     * Seleciona um veículo para uso no cálculo da rota.
     */
    fun selectVehicle(vehicle: Vehicle) {
        _uiState.update { it.copy(selectedVehicle = vehicle) }
    }

    /**
     * Busca localidades usando Nominatim e exibe os resultados para seleção.
     * O usuário deve clicar no resultado para adicionar como waypoint.
     */
    fun searchPlaces(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            nominatimRepository.searchPlaces(query)
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
     * Adiciona um waypoint intermediário à lista.
     */
    fun addWaypoint(waypoint: Waypoint) {
        _uiState.update {
            it.copy(waypoints = it.waypoints + waypoint)
        }
    }

    /**
     * Remove um waypoint intermediário da lista.
     */
    fun removeWaypoint(waypoint: Waypoint) {
        _uiState.update {
            it.copy(waypoints = it.waypoints - waypoint)
        }
    }

    /**
     * Reordena a lista de waypoints trocando as posições from e to.
     */
    fun reorderWaypoints(from: Int, to: Int) {
        _uiState.update { state ->
            val newWaypoints = state.waypoints.toMutableList()
            if (from in newWaypoints.indices && to in newWaypoints.indices) {
                val temp = newWaypoints[from]
                newWaypoints[from] = newWaypoints[to]
                newWaypoints[to] = temp
            }
            it.copy(waypoints = newWaypoints)
        }
    }

    /**
     * Limpa o campo de erro atual.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
