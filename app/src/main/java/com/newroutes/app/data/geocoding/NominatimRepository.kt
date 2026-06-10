package com.newroutes.app.data.geocoding

// TODO: Implementar repositório de geocoding usando Nominatim
class NominatimRepository(
    private val client: NominatimClient
) {
    suspend fun searchAddress(query: String): List<NominatimResponse> {
        TODO("Implementar busca de endereço via Nominatim")
    }

    suspend fun getAddressFromCoordinates(lat: Double, lon: Double): NominatimResponse? {
        TODO("Implementar busca de endereço a partir de coordenadas via Nominatim")
    }
}
