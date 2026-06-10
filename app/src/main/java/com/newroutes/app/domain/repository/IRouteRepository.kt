package com.newroutes.app.domain.repository

import com.newroutes.app.domain.model.Route
import kotlinx.coroutines.flow.Flow

// TODO: Interface do repositório de rotas
// Deve definir operações de salvar, listar, buscar e deletar rotas
interface IRouteRepository {
    fun getRoutes(): Flow<List<Route>>
    suspend fun getRouteById(id: String): Route?
    suspend fun saveRoute(route: Route): String
    suspend fun deleteRoute(id: String)
    suspend fun calculateRoute(waypoints: List<Pair<Double, Double>>): Route
}
