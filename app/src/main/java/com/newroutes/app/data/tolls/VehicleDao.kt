package com.newroutes.app.data.tolls

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room para a tabela de veículos.
 *
 * Fornece consultas para listar todos os veículos, encontrar o veículo
 * padrão e operações de inserção/atualização/exclusão.
 */
@Dao
interface VehicleDao {

    /**
     * Retorna todos os veículos cadastrados como Flow.
     */
    @Query("SELECT * FROM vehicles")
    fun getAll(): Flow<List<VehicleEntity>>

    /**
     * Retorna o veículo padrão do usuário como Flow, ou null se não houver.
     */
    @Query("SELECT * FROM vehicles WHERE isDefault = 1 LIMIT 1")
    fun getDefault(): Flow<VehicleEntity?>

    /**
     * Insere ou atualiza um veículo (UPSERT).
     *
     * @param entity entidade a ser inserida/atualizada
     */
    @Upsert
    suspend fun upsert(entity: VehicleEntity)

    /**
     * Remove um veículo pelo ID.
     *
     * @param id identificador do veículo a ser removido
     */
    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Desmarca todos os veículos como padrão (isDefault = 0).
     *
     * Chamado antes de definir um novo veículo como padrão.
     */
    @Query("UPDATE vehicles SET isDefault = 0")
    suspend fun clearDefault()

    /**
     * Marca um veículo como padrão (isDefault = 1).
     *
     * @param id identificador do veículo a ser marcado como padrão
     */
    @Query("UPDATE vehicles SET isDefault = 1 WHERE id = :id")
    suspend fun setDefault(id: String)
}
