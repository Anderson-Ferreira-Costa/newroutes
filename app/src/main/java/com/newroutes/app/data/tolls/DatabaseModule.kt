package com.newroutes.app.data.tolls

import android.content.Context
import androidx.room.Room
import com.newroutes.app.domain.repository.IRouteRepository
import com.newroutes.app.domain.repository.IVehicleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para prover o banco de dados Room e todos os DAOs,
 * repositórios concretos e binds das interfaces de domínio.
 *
 * Configura o database como singleton com fallback para migração destrutiva
 * (aceitável para MVP — TODO: migrations formais em produção).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provê a instância singleton do banco de dados.
     *
     * Cria o banco em modo destrutivo em caso de incompatibilidade de schema
     * (MVP). Em produção, substituir por migrations explícitas.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NewRoutesDatabase {
        return Room.databaseBuilder(
            context,
            NewRoutesDatabase::class.java,
            "newroutes.db"
        )
            .fallbackToDestructiveMigration()
            // TODO: Implementar migrations formais (Migration objects) antes de liberar em produção
            .build()
    }

    /**
     * Provê o DAO de praças de pedágio a partir do database.
     */
    @Provides
    @Singleton
    fun provideTollPlazaDao(database: NewRoutesDatabase): TollPlazaDao =
        database.tollPlazaDao()

    /**
     * Provê o DAO de veículos a partir do database.
     */
    @Provides
    @Singleton
    fun provideVehicleDao(database: NewRoutesDatabase): VehicleDao =
        database.vehicleDao()

    /**
     * Provê o DAO de rotas a partir do database.
     */
    @Provides
    @Singleton
    fun provideRouteDao(database: NewRoutesDatabase): RouteDao =
        database.routeDao()

    /**
     * Provê o repositório de praças de pedágio com o DAO injetado.
     * Preservado para uso futuro quando a feature de pedágios for reintegrada.
     */
    @Provides
    @Singleton
    fun provideTollRepository(dao: TollPlazaDao): TollRepository =
        TollRepository(dao)

    /**
     * Provê o repositório de veículos com o DAO injetado.
     */
    @Provides
    @Singleton
    fun provideVehicleRepository(dao: VehicleDao): VehicleRepository =
        VehicleRepository(dao)

    /**
     * Provê o repositório de rotas com o DAO injetado.
     */
    @Provides
    @Singleton
    fun provideRouteRepository(dao: RouteDao): RouteRepository =
        RouteRepository(dao)
}

/**
 * Liga as interfaces de domínio às implementações concretas via @Binds.
 *
 * Permite injetar IVehicleRepository ou IRouteRepository
 * em qualquer classe e receber automaticamente a implementação Room.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindings {

    /**
     * Liga IVehicleRepository à implementação VehicleRepository.
     */
    @dagger.Binds
    @Singleton
    abstract fun bindVehicleRepository(repository: VehicleRepository): IVehicleRepository

    /**
     * Liga IRouteRepository à implementação RouteRepository.
     */
    @dagger.Binds
    @Singleton
    abstract fun bindRouteRepository(repository: RouteRepository): IRouteRepository
}
