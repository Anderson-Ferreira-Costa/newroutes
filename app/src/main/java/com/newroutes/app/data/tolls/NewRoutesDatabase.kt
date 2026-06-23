package com.newroutes.app.data.tolls

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.newroutes.app.data.routing.RouteDao
import com.newroutes.app.data.routing.RouteEntity

/**
 * Banco de dados principal do New Routes via Room.
 *
 * Contém as tabelas de veículos e rotas.
 * Todos os type converters necessários estão registrados em [Converters].
 */
@Database(
    entities = [
        VehicleEntity::class,
        RouteEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NewRoutesDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
    abstract fun routeDao(): RouteDao
}
