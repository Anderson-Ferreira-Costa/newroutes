package com.newroutes.app.data.geocoding

import retrofit2.http.GET
import retrofit2.http.Query

// TODO: Implementar Retrofit client para Nominatim (OpenStreetMap Geocoding)
// Base URL: https://nominatim.openstreetmap.org
interface NominatimClient {

    // TODO: Geocoding forward - busca endereço por coordenadas
    // GET /search?format=json&q={query}&limit={limit}&countrycodes={country}
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("countrycodes") countryCodes: String = "br"
    ): List<NominatimResponse>

    // TODO: Geocoding reverse - busca coordenadas por endereço
    // GET /reverse?format=json&lat={lat}&lon={lon}&zoom={zoom}
    @GET("reverse")
    suspend fun reverse(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("zoom") zoom: Int = 18
    ): NominatimResponse?
}

data class NominatimResponse(
    val place_id: String = "",
    val lat: String = "",
    val lon: String = "",
    val display_name: String = "",
    val type: String = "",
    val importance: Double = 0.0
)
