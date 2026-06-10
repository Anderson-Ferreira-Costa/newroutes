package com.newroutes.app.data.geocoding

import com.squareup.moshi.Json

/**
 * DTO que representa um resultado da API Nominatim.
 *
 * Esta classe é um Data Transfer Object — não contém lógica de negócio.
 * Fica na camada data e não deve ser movida para domain.
 *
 * Campos mapeados diretamente do JSON retornado pelo Nominatim.
 */
data class NominatimPlace(
    @Json(name = "place_id") val placeId: Long,
    @Json(name = "display_name") val displayName: String,
    val lat: String,
    val lon: String,
    val type: String,
    val importance: Double
)
