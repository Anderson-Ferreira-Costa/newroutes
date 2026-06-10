package com.newroutes.app.domain.usecase

import com.newroutes.app.domain.repository.IRouteRepository

// TODO: UseCase para estimar custo de uma rota (pedágios + combustível)
// Deve analisar os pedágios da rota e aplicar a tarifa do veículo
class EstimateCostUseCase(
    private val routeRepository: IRouteRepository
) {
    suspend fun execute(routeId: String): Result<Double> {
        // TODO: Implementar lógica de cálculo de custo
        return Result.success(0.0)
    }
}
