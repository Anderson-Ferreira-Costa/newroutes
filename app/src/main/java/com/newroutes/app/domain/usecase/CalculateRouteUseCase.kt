package com.newroutes.app.domain.usecase

import com.newroutes.app.domain.repository.IRouteRepository

// TODO: UseCase para calcular rota entre waypoints
// Deve chamar IRouteRepository.calculateRoute com a lista de waypoints
class CalculateRouteUseCase(
    private val routeRepository: IRouteRepository
) {
    suspend fun execute(waypoints: List<Pair<Double, Double>>): Result<Any> {
        // TODO: Implementar lógica de chamada ao repository
        return Result.success(Unit)
    }
}
