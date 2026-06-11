package com.newroutes.app.data.geocoding

import com.squareup.moshi.Json

/**
 * DTO que representa a resposta completa da API Photon (FeatureCollection).
 *
 * O Photon retorna JSON no formato GeoJSON FeatureCollection:
 * {
 *   "type": "FeatureCollection",
 *   "features": [{
 *     "geometry": { "coordinates": [lon, lat] },
 *     "properties": { "name": "...", "country": "...", "state": "..." }
 *   }]
 * }
 */
data class PhotonResponse(
    val features: List<PhotonFeature>
)

/**
 * DTO que representa uma feature GeoJSON da API Photon.
 */
data class PhotonFeature(
    val geometry: PhotonGeometry,
    val properties: PhotonProperties
)

data class PhotonGeometry(
    @Json(name = "coordinates") val coordinates: List<Double> // [longitude, latitude]
)

data class PhotonProperties(
    val name: String? = null,
    val country: String? = null,
    val state: String? = null,
    @Json(name = "locality") val locality: String? = null,
    @Json(name = "postcode") val postcode: String? = null,
    @Json(name = "city") val city: String? = null
)
