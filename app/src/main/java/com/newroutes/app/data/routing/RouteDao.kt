package com.newroutes.app.data.routing

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room para a tabela de rotas.
 */
@Dao
interface RouteDao {

    @Query("SELECT * FROM routes ORDER BY createdAt DESC")
    fun getAll(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE id = :id")
    fun getById(id: String): Flow<RouteEntity?>

    @Upsert
    suspend fun upsert(entity: RouteEntity)

    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM routes")
    suspend fun clearAll()
}
