package com.newroutes.app.data.routing

import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.repository.IRouteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Implementação de [IRouteRepository] usando Room para persistência local.
 * Traduz entre domain models e entidades Room.
 */
class RouteRepository(
    private val dao: RouteDao
) : IRouteRepository {

    override fun getAllRoutes(): Flow<List<Route>> =
        dao.getAll().map { entities -> entities.map { it.toModel() } }

    override fun getRouteById(id: UUID): Flow<Route?> =
        dao.getById(id.toString()).map { entity -> entity?.toModel() }

    override suspend fun saveRoute(route: Route) {
        dao.upsert(RouteEntity.fromModel(route))
    }

    override suspend fun deleteRoute(id: UUID) {
        dao.deleteById(id.toString())
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}
