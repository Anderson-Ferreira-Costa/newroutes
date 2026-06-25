package com.newroutes.app.ui.vehicle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newroutes.app.domain.model.TollCategory
import com.newroutes.app.domain.model.Vehicle
import com.newroutes.app.domain.usecase.ManageVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class VehicleUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val name: String = "",
    val category: TollCategory = TollCategory.CAR,
    val fuelConsumption: String = "",
    val fuelPrice: String = "",
    val isDefault: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val manageVehicleUseCase: ManageVehicleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleUiState())
    val uiState: StateFlow<VehicleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            manageVehicleUseCase.getAll().collect { vehicles ->
                _uiState.update { it.copy(vehicles = vehicles) }
            }
        }
    }

    /**
     * Atualiza o nome do veículo no formulário.
     */
    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    /**
     * Atualiza a categoria do veículo no formulário.
     */
    fun onCategoryChanged(category: TollCategory) {
        _uiState.update { it.copy(category = category) }
    }

    /**
     * Atualiza o consumo de combustível — aceita apenas dígitos e ponto.
     */
    fun onFuelConsumptionChanged(value: String) {
        _uiState.update { it.copy(fuelConsumption = value.filter { it.isDigit() || it == '.' }) }
    }

    /**
     * Atualiza o preço do combustível — aceita apenas dígitos e ponto.
     */
    fun onFuelPriceChanged(value: String) {
        _uiState.update { it.copy(fuelPrice = value.filter { it.isDigit() || it == '.' }) }
    }

    /**
     * Atualiza o flag de veículo padrão.
     */
    fun onIsDefaultChanged(value: Boolean) {
        _uiState.update { it.copy(isDefault = value) }
    }

    /**
     * Salva o veículo atual.
     *
     * Valida campos obrigatórios, constrói o Vehicle e chama ManageVehicleUseCase.save().
     * Se isDefault == true ou vehicles estiver vazio, chama setDefault() após o save.
     * Reseta o formulário após sucesso (isSaved = true).
     */
    fun saveVehicle() {
        val state = _uiState.value
        Log.d("VehicleViewModel", "saveVehicle chamado — name=${state.name}, consumption=${state.fuelConsumption}, price=${state.fuelPrice}")

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Nome é obrigatório") }
            return
        }

        val consumption = state.fuelConsumption.toDoubleOrNull()
        if (consumption == null || consumption <= 0) {
            _uiState.update { it.copy(error = "Consumo inválido") }
            return
        }

        val price = state.fuelPrice.toDoubleOrNull()
        if (price == null || price <= 0) {
            _uiState.update { it.copy(error = "Preço do combustível inválido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val vehicle = Vehicle(
                name = state.name.trim(),
                category = state.category,
                fuelConsumptionKmPerLiter = consumption,
                fuelPricePerLiter = price,
                isDefault = state.isDefault
            )

            manageVehicleUseCase.save(vehicle)
                .onSuccess {
                    if (state.isDefault || state.vehicles.isEmpty()) {
                        manageVehicleUseCase.setDefault(vehicle.id)
                    }
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = true,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = exception.message ?: "Erro ao salvar veículo"
                        )
                    }
                }
        }
    }

    /**
     * Deleta um veículo pelo ID.
     */
    fun deleteVehicle(id: UUID) {
        viewModelScope.launch {
            manageVehicleUseCase.delete(id)
        }
    }

    /**
     * Define um veículo como padrão.
     */
    fun setDefault(id: UUID) {
        viewModelScope.launch {
            manageVehicleUseCase.setDefault(id)
        }
    }

    /**
     * Limpa o campo de erro atual.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Limpa todos os campos do formulário.
     */
    fun resetForm() {
        _uiState.update {
            VehicleUiState(
                category = TollCategory.CAR,
                isDefault = it.vehicles.isEmpty()
            )
        }
    }
}
