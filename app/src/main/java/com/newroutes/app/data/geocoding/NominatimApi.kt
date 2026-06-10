package com.newroutes.app.data.geocoding

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface Retrofit para os endpoints do Nominatim (OpenStreetMap Geocoding).
 *
 * Base URL: https://nominatim.openstreetmap.org/
 *
 * TODO: Implementar rate limiting de 1 requisição/segundo conforme exigido
 * pela política de uso público da API Nominatim.
 */
interface NominatimApi {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("countrycodes") countryCodes: String = "br",
        @Query("limit") limit: Int = 5
    ): List<NominatimPlace>

    @GET("reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "jsonv2",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 1
    ): NominatimPlace
}
