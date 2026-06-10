package com.newroutes.app.data.routing

/**
 * Resultado interno da camada data para uma rota calculada pelo OSRM.
 *
 * Não é um modelo de domínio — fica em data/routing/ e não é exposto
 * para a camada domain/ ou ui/.
 *
 * @property distanceMeters Distância em metros (inteiro)
 * @property durationSeconds Duração em segundos (inteiro)
 * @property encodedPolyline Geometria da rota codificada em Google Polyline
 */
data class OsrmRouteResult(
    val distanceMeters: Long,
    val durationSeconds: Long,
    val encodedPolyline: String
)
