package com.newroutes.app.domain.usecase

import com.newroutes.app.domain.repository.IRouteRepository
import java.util.UUID

/**
 * Remove uma rota do armazenamento persistente pelo seu identificador.
 */
class DeleteRouteUseCase(
    private val routeRepository: IRouteRepository
) {

    /**
     * Deleta uma rota pelo ID.
     *
     * @param id identificador da rota a ser removida
     * @return Result.success(Unit) em caso de sucesso ou Result.failure com a exceção
     */
    suspend fun invoke(id: UUID): Result<Unit> {
        return try {
            routeRepository.deleteRoute(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
