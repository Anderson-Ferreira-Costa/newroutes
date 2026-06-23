package com.newroutes.app.data.tolls

import android.content.Context
import androidx.room.Room
import com.newroutes.app.data.routing.RouteDao
import com.newroutes.app.data.routing.RouteRepository
import com.newroutes.app.domain.repository.IRouteRepository
import com.newroutes.app.domain.repository.IVehicleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para prover o banco de dados Room, DAOs, repositórios
 * concretos e bindings das interfaces de domínio.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NewRoutesDatabase {
        return Room.databaseBuilder(
            context,
            NewRoutesDatabase::class.java,
            "newroutes.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideVehicleDao(database: NewRoutesDatabase): VehicleDao =
        database.vehicleDao()

    @Provides
    @Singleton
    fun provideRouteDao(database: NewRoutesDatabase): RouteDao =
        database.routeDao()

    @Provides
    @Singleton
    fun provideVehicleRepository(dao: VehicleDao): VehicleRepository =
        VehicleRepository(dao)

    @Provides
    @Singleton
    fun provideRouteRepository(dao: RouteDao): RouteRepository =
        RouteRepository(dao)
}

/**
 * Liga as interfaces de domínio às implementações concretas via @Binds.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindings {

    @dagger.Binds
    @Singleton
    abstract fun bindVehicleRepository(repository: VehicleRepository): IVehicleRepository

    @dagger.Binds
    @Singleton
    abstract fun bindRouteRepository(repository: RouteRepository): IRouteRepository
}
