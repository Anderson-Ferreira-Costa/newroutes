package com.newroutes.app.data.tolls

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room para a tabela de rotas.
 *
 * Fornece consultas para listar rotas ordenadas por data de criação,
 * buscar por ID e operações de inserção/atualização/exclusão.
 */
@Dao
interface RouteDao {

    /**
     * Retorna todas as rotas salvas, ordenadas pela data de criação decrescente.
     */
    @Query("SELECT * FROM routes ORDER BY createdAt DESC")
    fun getAll(): Flow<List<RouteEntity>>

    /**
     * Retorna a rota correspondente ao ID, ou null se não existir.
     */
    @Query("SELECT * FROM routes WHERE id = :id")
    fun getById(id: String): Flow<RouteEntity?>

    /**
     * Insere ou atualiza uma rota (UPSERT).
     *
     * @param entity entidade a ser inserida/atualizada
     */
    @Upsert
    suspend fun upsert(entity: RouteEntity)

    /**
     * Remove uma rota pelo ID.
     *
     * @param id identificador da rota a ser removida
     */
    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Remove todas as rotas do banco.
     */
    @Query("DELETE FROM routes")
    suspend fun clearAll()
}
