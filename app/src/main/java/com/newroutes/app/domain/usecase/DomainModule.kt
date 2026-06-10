package com.newroutes.app.domain.usecase

import com.newroutes.app.data.routing.OsrmRepository
import com.newroutes.app.domain.repository.IRouteRepository
import com.newroutes.app.domain.repository.ITollRepository
import com.newroutes.app.domain.repository.IVehicleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para prover os UseCases do domínio.
 *
 * Os UseCases dependem de interfaces de domínio (repository) e são
 * criados como singletons pelo Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideManageVehicleUseCase(vehicleRepository: IVehicleRepository): ManageVehicleUseCase {
        return ManageVehicleUseCase(vehicleRepository)
    }

    @Provides
    @Singleton
    fun provideGetRoutesUseCase(routeRepository: IRouteRepository): GetRoutesUseCase {
        return GetRoutesUseCase(routeRepository)
    }

    @Provides
    @Singleton
    fun provideCalculateRouteUseCase(
        tollRepository: ITollRepository,
        vehicleRepository: IVehicleRepository,
        osrmRepository: OsrmRepository
    ): CalculateRouteUseCase {
        return CalculateRouteUseCase(tollRepository, vehicleRepository, osrmRepository)
    }

    @Provides
    @Singleton
    fun provideSaveRouteUseCase(routeRepository: IRouteRepository): SaveRouteUseCase {
        return SaveRouteUseCase(routeRepository)
    }
}
