package com.newroutes.app.data.geocoding

import com.squareup.moshi.Json

/**
 * DTO que representa uma feature GeoJSON da API Photon.
 *
 * Format GeoJSON: {
 *   "features": [{
 *     "geometry": { "coordinates": [lon, lat] },
 *     "properties": { "name": "...", "country": "...", "state": "..." }
 *   }]
 * }
 */
data class PhotonFeature(
    val geometry: Geometry,
    val properties: Properties
) {
    data class Geometry(
        @Json(name = "coordinates") val coordinates: List<Double>
    )

    data class Properties(
        val name: String? = null,
        val country: String? = null,
        val state: String? = null,
        @Json(name = "locality") val locality: String? = null,
        @Json(name = "postcode") val postcode: String? = null
    )
}

/**
 * DTO que representa a resposta completa da API Photon (array de features).
 */
typealias PhotonResponse = List<PhotonFeature>
