package com.newroutes.app.domain.usecase

import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.repository.IRouteRepository

/**
 * UseCase para persistir uma rota calculada no banco de dados local.
 */
class SaveRouteUseCase(
    private val routeRepository: IRouteRepository
) {

    /**
     * Salva uma rota calculada.
     *
     * @param route rota a ser persistida
     * @return Result.success(Unit) em caso de sucesso ou Result.failure com a exceção
     */
    suspend fun invoke(route: Route): Result<Unit> {
        if (route.waypoints.size < 2) {
            return Result.failure(IllegalArgumentException("Route must have at least 2 waypoints"))
        }

        return try {
            routeRepository.saveRoute(route)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
