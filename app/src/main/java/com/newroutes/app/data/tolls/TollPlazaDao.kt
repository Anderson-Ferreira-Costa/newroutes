package com.newroutes.app.data.tolls

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room para a tabela de praças de pedágio.
 *
 * Fornece consultas para listar todas as praças, buscar por rodovia,
 * encontrar praças próximas a um bounding box e inserir/atualizar registros.
 */
@Dao
interface TollPlazaDao {

    /**
     * Retorna todas as praças de pedágio como Flow.
     */
    @Query("SELECT * FROM toll_plazas")
    fun getAll(): Flow<List<TollPlazaEntity>>

    /**
     * Retorna as praças de pedágio de uma rodovia específica como Flow.
     */
    @Query("SELECT * FROM toll_plazas WHERE highway = :highway")
    fun getByHighway(highway: String): Flow<List<TollPlazaEntity>>

    /**
     * Retorna as praças de pedágio dentro de um bounding box.
     *
     * @param minLat Latitude mínima do bounding box
     * @param maxLat Latitude máxima do bounding box
     * @param minLon Longitude mínima do bounding box
     * @param maxLon Longitude máxima do bounding box
     * @return Lista de entidades encontradas
     */
    @Query("SELECT * FROM toll_plazas WHERE " +
            "(latitude BETWEEN :minLat AND :maxLat) AND " +
            "(longitude BETWEEN :minLon AND :maxLon)")
    suspend fun getNearby(
        minLat: Double, maxLat: Double,
        minLon: Double, maxLon: Double
    ): List<TollPlazaEntity>

    /**
     * Insere ou atualiza uma praça de pedágio (UPSERT).
     *
     * @param entity entidade a ser inserida/atualizada
     */
    @Upsert
    suspend fun upsert(entity: TollPlazaEntity)

    /**
     * Insere ou atualiza em lote uma lista de praças de pedágio.
     *
     * @param entities lista de entidades a serem inseridas/atualizadas
     */
    @Upsert
    suspend fun upsertAll(entities: List<TollPlazaEntity>)
}
