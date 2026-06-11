package com.newroutes.app.data.geocoding

import com.newroutes.app.domain.model.Waypoint

/**
 * Implementação concreta do repositório de geocoding usando a API Photon.
 *
 * Substitui o NominatimRepository — usa Photon (Komoot) ao invés de Nominatim (OSM).
 *
 * Recebe PhotonApi via construtor para injeção de dependência (Hilt).
 */
class PhotonRepository(
    private val api: PhotonApi
) {

    /**
     * Busca lugares/waypoints a partir de uma consulta de texto.
     *
     * Chama PhotonApi.search() e mapeia os resultados para List<Waypoint>.
     * Coordenadas do GeoJSON [lon, lat] são convertidas para Double.
     *
     * @param query Termo de busca (endereço, nome de lugar, etc.)
     * @return Result com lista de Waypoints em sucesso, ou failure em erro
     */
    suspend fun searchPlaces(query: String): Result<List<Waypoint>> = runCatching {
        api.search(query).map { feature ->
            feature.toWaypoint()
        }
    }

    /**
     * Realiza geocodificação reversa a partir de coordenadas.
     *
     * Chama PhotonApi.reverse() e mapeia o resultado para Waypoint.
     *
     * @param latitude Latitude do ponto
     * @param longitude Longitude do ponto
     * @return Result com Waypoint em sucesso, ou failure em erro
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<Waypoint> = runCatching {
        val results = api.reverse(latitude, longitude)
        results.firstOrNull()?.toWaypoint()
            ?: throw IllegalStateException("Nenhum resultado encontrado para as coordenadas fornecidas")
    }

    /**
     * Converte um PhotonFeature em Waypoint do domínio.
     *
     * - name: nome do local + cidade/estado, truncado em 60 caracteres
     * - address: nome do local + país/estado
     * - lat/lon: extraídos do array GeoJSON [lon, lat]
     */
    private fun PhotonFeature.toWaypoint(): Waypoint {
        val lon = geometry.coordinates.getOrElse(0) { 0.0 }
        val lat = geometry.coordinates.getOrElse(1) { 0.0 }

        val nameParts = listOfNotNull(properties.name, properties.locality, properties.state)
        val addressParts = listOfNotNull(properties.name, properties.postcode, properties.state, properties.country)

        return Waypoint(
            name = nameParts.joinToString(", ").take(60),
            latitude = lat,
            longitude = lon,
            address = addressParts.joinToString(", ")
        )
    }
}
