package com.newroutes.app.data.routing

import com.newroutes.app.domain.model.Waypoint

/**
 * Implementação concreta do repositório de roteamento usando a API OSRM.
 *
 * Não implementa interface de domain/ — IRouteRepository será implementada
 * por RouteRepository em Session 6 (junto com Room para persistência local).
 *
 * Recebe OsrmApi via construtor para injeção de dependência (Hilt).
 */
class OsrmRepository(
    private val osrmApi: OsrmApi
) {

    /**
     * Calcula uma rota via OSRM dado uma lista de waypoints.
     *
     * Monta a string de coordenadas no formato OSRM (lon,lat;lon,lat;...),
     * chama a API e valida a resposta.
     *
     * @param waypoints Lista de waypoints (mínimo 2)
     * @return Result com OsrmRouteResult em sucesso, ou failure em erro
     */
    suspend fun getRoute(waypoints: List<Waypoint>): Result<OsrmRouteResult> = runCatching {
        if (waypoints.size < 2) {
            throw IllegalArgumentException("Waypoints deve conter pelo menos 2 itens")
        }

        val coordinates = waypoints.joinToString(";") { wp ->
            "${wp.longitude},${wp.latitude}"
        }

        val response = osrmApi.getRoute(coordinates)

        if (response.code != "Ok") {
            throw IllegalStateException("OSRM returned non-OK status: ${response.code}")
        }

        if (response.routes.isEmpty()) {
            throw IllegalStateException("OSRM returned no routes")
        }

        val route = response.routes[0]
        OsrmRouteResult(
            distanceMeters = route.distance.toLong(),
            durationSeconds = route.duration.toLong(),
            encodedPolyline = route.geometry
        )
    }
}
