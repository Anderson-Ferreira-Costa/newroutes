package com.newroutes.app.data.geocoding

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface Retrofit para a API Photon (Komoot).
 *
 * Base URL: https://photon.komoot.io/
 *
 * Photon é uma alternativa open source ao Nominatim, baseada em dados OSM,
 * sem rate limit restritivo e sem política contra busca client-side.
 */
interface PhotonApi {

    /**
     * Busca lugares a partir de uma consulta de texto.
     *
     * - bbox limita resultados ao Brasil (wswng: -73.98,-33.75,-34.79,5.27)
     * - lang=default retorna nomes locais (Brasil → português)
     */
    @GET("/api")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("lang") lang: String = "default",
        @Query("bbox") bbox: String = "-73.98,-33.75,-34.79,5.27"
    ): PhotonResponse

    /**
     * Geocodificação reversa a partir de coordenadas.
     */
    @GET("/reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int = 1
    ): PhotonResponse
}
