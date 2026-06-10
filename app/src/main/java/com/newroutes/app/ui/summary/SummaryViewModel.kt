package com.newroutes.app.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SummaryUiState(
    val routeName: String = "",
    val distanceText: String = "",
    val durationText: String = "",
    val totalCost: Double = 0.0,
    val tollCost: Double = 0.0,
    val fuelCost: Double = 0.0,
    val waypoints: List<SummaryWaypoint> = emptyList(),
    val tolls: List<SummaryToll> = emptyList(),
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SummaryWaypoint(
    val name: String = "",
    val address: String = "",
    val order: Int = 0
)

data class SummaryToll(
    val name: String = "",
    val highway: String = "",
    val cost: Double = 0.0
)

@HiltViewModel
class SummaryViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    // TODO: Carregar dados do resumo da rota
    fun loadRouteSummary(routeId: String) {
        viewModelScope.launch {
            // TODO: Carregar dados da rota para exibição
        }
    }

    // TODO: Salvar rota calculada
    fun saveRoute() {
        viewModelScope.launch {
            // TODO: Salvar rota no repository
        }
    }
}
