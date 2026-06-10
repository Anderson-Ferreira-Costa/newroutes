package com.newroutes.app.data.tolls

import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.repository.IRouteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Implementação de [IRouteRepository] usando Room para persistência local.
 *
 * Recebe [RouteDao] via injeção de dependência (Hilt) e traduz
 * entre domain models e entidades Room.
 */
class RouteRepository(
    private val dao: RouteDao
) : IRouteRepository {

    /**
     * Retorna todas as rotas salvas como Flow de domain models.
     */
    override fun getAllRoutes(): Flow<List<Route>> =
        dao.getAll().map { entities ->
            entities.map { it.toModel() }
        }

    /**
     * Retorna a rota correspondente ao ID como Flow, ou null.
     */
    override fun getRouteById(id: UUID): Flow<Route?> =
        dao.getById(id.toString()).map { entity ->
            entity?.toModel()
        }

    /**
     * Salva ou atualiza uma rota no armazenamento persistente.
     */
    override suspend fun saveRoute(route: Route) {
        dao.upsert(RouteEntity.fromModel(route))
    }

    /**
     * Remove uma rota pelo identificador.
     */
    override suspend fun deleteRoute(id: UUID) {
        dao.deleteById(id.toString())
    }

    /**
     * Remove todas as rotas do armazenamento persistente.
     */
    override suspend fun clearAll() {
        dao.clearAll()
    }
}
