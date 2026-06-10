package com.newroutes.app.data.routing

import com.squareup.moshi.Json

/**
 * DTO que representa a resposta da API OSRM de roteamento.
 *
 * DTO — Data Transfer Object, sem lógica de negócio.
 * Fica em data/routing/, nunca em domain/.
 */
data class OsrmResponse(
    @Json(name = "code") val code: String,
    @Json(name = "routes") val routes: List<OsrmRoute>
)

/**
 * DTO que representa uma rota individual na resposta do OSRM.
 */
data class OsrmRoute(
    @Json(name = "distance") val distance: Double,
    @Json(name = "duration") val duration: Double,
    @Json(name = "geometry") val geometry: String
)
