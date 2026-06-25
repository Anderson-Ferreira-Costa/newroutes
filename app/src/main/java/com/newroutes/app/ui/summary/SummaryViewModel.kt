package com.newroutes.app.ui.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.usecase.GetRoutesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SummaryUiState(
    val route: Route? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    getRoutesUseCase: GetRoutesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    private val routeIdString: String = savedStateHandle["routeId"] ?: ""

    init {
        viewModelScope.launch {
            getRoutesUseCase.invoke()
                .collect { routes ->
                    val found = routes.find { it.id.toString() == routeIdString }
                    if (found != null) {
                        _uiState.update {
                            it.copy(
                                route = found,
                                isLoading = false
                            )
                        }
                    } else if (!_uiState.value.isLoading) {
                        // Already collected, route not found
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                    }
                }
        }

        // Check if route was already found during collection
        viewModelScope.launch {
            _uiState.collect { state ->
                if (!state.isLoading && state.route == null && routeIdString.isNotEmpty()) {
                    _uiState.update {
                        it.copy(error = "Rota não encontrada")
                    }
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
}
