package com.newroutes.app.ui.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newroutes.app.data.geocoding.PhotonRepository
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

/**
 * ViewModel da tela de configuração de rota — seleção de veículo e waypoints intermediários.
 */
@HiltViewModel
class RouteViewModel @Inject constructor(
    private val manageVehicleUseCase: ManageVehicleUseCase,
    private val photonRepository: PhotonRepository
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

    fun selectVehicle(vehicle: Vehicle) {
        _uiState.update { it.copy(selectedVehicle = vehicle) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

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

    fun addWaypoint(waypoint: Waypoint) {
        _uiState.update {
            it.copy(waypoints = it.waypoints + waypoint)
        }
    }

    fun removeWaypoint(waypoint: Waypoint) {
        _uiState.update {
            it.copy(waypoints = it.waypoints - waypoint)
        }
    }

    fun reorderWaypoints(from: Int, to: Int) {
        _uiState.update { state ->
            val newWaypoints = state.waypoints.toMutableList()
            if (from in newWaypoints.indices && to in newWaypoints.indices) {
                val temp = newWaypoints[from]
                newWaypoints[from] = newWaypoints[to]
                newWaypoints[to] = temp
            }
            state.copy(waypoints = newWaypoints)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class RouteUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val searchQuery: String = "",
    val searchResults: List<Waypoint> = emptyList(),
    val isSearching: Boolean = false,
    val waypoints: List<Waypoint> = emptyList(),
    val error: String? = null
)
