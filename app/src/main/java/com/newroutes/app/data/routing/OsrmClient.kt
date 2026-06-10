package com.newroutes.app.data.routing

// TODO: Implementar Retrofit client para API do OSRM
// Deve configurar endpoints: /route/v1/driving/ para roteamento
// e /match/v1/driving/ para matching de轨迹
interface OsrmClient {
    // TODO: Endpoint de roteamento
    // GET /route/v1/driving/{lon},{lat};{lon},{lat}?overview=full&steps=true
    suspend fun getRoute(
        coordinates: String,
        steps: Boolean = true
    ): Any

    // TODO: Endpoint de geocoding inverso
    // GET /reverse/v1/driving/{lon},{lat}
    suspend fun reverseGeocode(
        longitude: Double,
        latitude: Double
    ): Any
}
