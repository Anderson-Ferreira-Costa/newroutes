package com.newroutes.app.domain.usecase

import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.repository.IRouteRepository

import kotlinx.coroutines.flow.Flow

/**
 * Recupera todas as rotas salvas como stream observável.
 *
 * Delegação direta ao repository — sem transformação ou filtragem adicional.
 */
class GetRoutesUseCase(
    private val routeRepository: IRouteRepository
) {

    /**
     * Retorna todas as rotas salvas como Flow observável.
     *
     * @return Flow emitindo lista de rotas
     */
    fun invoke(): Flow<List<Route>> {
        return routeRepository.getAllRoutes()
    }
}
