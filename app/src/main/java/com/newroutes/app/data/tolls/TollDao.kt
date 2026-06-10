package com.newroutes.app.data.tolls

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.newroutes.app.data.tolls.entities.TollEntity
import kotlinx.coroutines.flow.Flow

// TODO: DAO do Room para tabela de pedágios
@Dao
interface TollDao {
    @Query("SELECT * FROM tolls ORDER BY `order` ASC")
    fun getAllTolls(): Flow<List<TollEntity>>

    @Query("SELECT * FROM tolls WHERE id = :id LIMIT 1")
    suspend fun getTollById(id: String): TollEntity?

    @Query("SELECT * FROM tolls WHERE highway = :highway ORDER BY `order` ASC")
    suspend fun searchByHighway(highway: String): List<TollEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTolls(tolls: List<TollEntity>)

    @Query("DELETE FROM tolls")
    suspend fun deleteAll()
}
