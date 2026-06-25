package com.newroutes.app.domain.repository

import com.newroutes.app.domain.model.TollPlaza
import com.newroutes.app.domain.model.Waypoint
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Contrato para acesso à base de praças de pedágio.
 * Implementações concretas vivem em data/tolls/.
 *
 * Reservado para implementação futura de pedágios.
 */
@Deprecated("Reservado para implementação futura de pedágios")
interface ITollRepository {

    /** Retorna uma Flow emitindo todas as praças de pedágio cadastradas. */
    fun getAllTollPlazas(): Flow<List<TollPlaza>>

    /** Retorna uma Flow emitindo as praças de pedágio de uma rodovia específica. */
    fun getTollPlazasByHighway(highway: String): Flow<List<TollPlaza>>

    /**
     * Retorna as praças de pedágio próximas a uma rota definida por waypoints,
     * dentro de um raio dado em metros. Operação única — não emite Flow.
     */
    suspend fun getTollPlazasNearRoute(waypoints: List<Waypoint>, radiusMeters: Double): List<TollPlaza>

    /** Insere ou atualiza uma praça de pedágio no armazenamento persistente. */
    suspend fun upsertTollPlaza(tollPlaza: TollPlaza): Unit

    /** Insere ou atualiza em lote uma lista de praças de pedágio. */
    suspend fun upsertAll(tollPlazas: List<TollPlaza>): Unit
}
