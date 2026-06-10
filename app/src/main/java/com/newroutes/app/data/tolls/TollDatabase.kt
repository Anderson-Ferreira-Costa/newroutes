package com.newroutes.app.data.tolls

import androidx.room.Database
import androidx.room.RoomDatabase
import com.newroutes.app.data.tolls.entities.TollEntity

// TODO: Database Room para persistência de pedágios
@Database(entities = [TollEntity::class], version = 1, exportSchema = true)
abstract class TollDatabase : RoomDatabase() {
    abstract fun tollDao(): TollDao

    companion object {
        const val DATABASE_NAME = "tolls_database"
    }
}
