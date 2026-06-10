package com.newroutes.app.data.routing

import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.repository.IRouteRepository

// TODO: Implementar repositório de rotas usando OSRM via Retrofit
// Deve consumir os endpoints do OSRM público para cálculo de rotas
class OsrmRepository(
    private val osrmClient: OsrmClient
) : IRouteRepository {

    override fun getRoutes(): Flow<List<Route>> {
        TODO("Implementar listagem de rotas salvas localmente")
    }

    override suspend fun getRouteById(id: String): Route? {
        TODO("Implementar busca de rota por ID")
    }

    override suspend fun saveRoute(route: Route): String {
        TODO("Implementar salvamento de rota localmente")
    }

    override suspend fun deleteRoute(id: String) {
        TODO("Implementar deleção de rota")
    }

    override suspend fun calculateRoute(waypoints: List<Pair<Double, Double>>): Route {
        TODO("Implementar cálculo de rota via OSRM")
    }
}
