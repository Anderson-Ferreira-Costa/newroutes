package com.newroutes.app.data.geocoding

import com.newroutes.app.domain.model.Waypoint

/**
 * Implementação concreta do repositório de geocoding usando a API Nominatim.
 *
 * Não implementa nenhuma interface de domain/ — geocoding sem contrato definido
 * na camada de domínio nesta fase do projeto.
 *
 * Recebe NominatimApi via construtor para injeção de dependência (Hilt).
 */
class NominatimRepository(
    private val api: NominatimApi
) {

    /**
     * Busca lugares/waypoints a partir de uma consulta de texto.
     *
     * Chama NominatimApi.search() e mapeia os resultados para List<Waypoint>.
     * Lat/lon (String na API) são convertidos para Double via toDoubleOrNull().
     *
     * @param query Termo de busca (endereço, nome de lugar, etc.)
     * @return Result com lista de Waypoints em sucesso, ou failure em erro
     */
    suspend fun searchPlaces(query: String): Result<List<Waypoint>> = runCatching {
        api.search(query).map { place ->
            place.toWaypoint()
        }
    }

    /**
     * Realiza geocodificação reversa a partir de coordenadas.
     *
     * Chama NominatimApi.reverse() e mapeia o resultado para Waypoint.
     *
     * @param latitude Latitude do ponto
     * @param longitude Longitude do ponto
     * @return Result com Waypoint em sucesso, ou failure em erro
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<Waypoint> = runCatching {
        api.reverse(latitude, longitude).toWaypoint()
    }

    /**
     * Converte um NominatimPlace em Waypoint do domínio.
     *
     * - name: displayName truncado em 60 caracteres
     * - address: displayName completo
     * - lat/lon: convertidos de String para Double (fallback 0.0)
     */
    private fun NominatimPlace.toWaypoint(): Waypoint {
        return Waypoint(
            name = displayName.take(60),
            latitude = lat.toDoubleOrNull() ?: 0.0,
            longitude = lon.toDoubleOrNull() ?: 0.0,
            address = displayName
        )
    }
}
