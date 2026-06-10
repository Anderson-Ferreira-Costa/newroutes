package com.newroutes.app.domain.repository

import com.newroutes.app.domain.model.Route
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Contrato para persistência e recuperação de rotas calculadas.
 * Implementações concretas vivem em data/routing/.
 */
interface IRouteRepository {

    /** Salva uma rota calculada no armazenamento persistente. */
    suspend fun saveRoute(route: Route): Unit

    /** Remove uma rota pelo identificador. */
    suspend fun deleteRoute(id: UUID): Unit

    /** Retorna uma Flow emitindo a rota correspondente ao ID, ou null. */
    fun getRouteById(id: UUID): Flow<Route?>

    /** Retorna uma Flow emitindo todas as rotas salvas. */
    fun getAllRoutes(): Flow<List<Route>>

    /** Remove todas as rotas do armazenamento persistente. */
    suspend fun clearAll(): Unit
}
