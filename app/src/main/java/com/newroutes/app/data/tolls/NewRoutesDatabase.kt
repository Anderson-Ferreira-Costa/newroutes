package com.newroutes.app.data.tolls

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Banco de dados principal do New Routes via Room.
 *
 * Contém as tabelas de praças de pedágio, veículos e rotas.
 * Todos os type converters necessários estão registrados em [Converters].
 */
@Database(
    entities = [
        TollPlazaEntity::class,
        VehicleEntity::class,
        RouteEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NewRoutesDatabase : RoomDatabase() {

    /**
     * Retorna o DAO para operações na tabela de praças de pedágio.
     */
    abstract fun tollPlazaDao(): TollPlazaDao

    /**
     * Retorna o DAO para operações na tabela de veículos.
     */
    abstract fun vehicleDao(): VehicleDao

    /**
     * Retorna o DAO para operações na tabela de rotas.
     */
    abstract fun routeDao(): RouteDao
}
