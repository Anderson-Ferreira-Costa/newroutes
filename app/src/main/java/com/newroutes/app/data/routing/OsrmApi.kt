package com.newroutes.app.data.routing

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface Retrofit para o endpoint de roteamento do OSRM.
 *
 * Base URL: https://router.project-osrm.org/
 * Endpoint: /route/v1/driving/{coordinates}
 *
 * TODO: Migrar para instância self-hosted do OSRM para garantir
 * disponibilidade e controle de rate limiting em produção.
 */
interface OsrmApi {

    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "polyline",
        @Query("steps") steps: Boolean = false
    ): OsrmResponse
}
