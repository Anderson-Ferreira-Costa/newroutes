package com.newroutes.app.domain.model

// TODO: Implementar modelo de dados da rota
// Deve conter: lista de waypoints, distância total, tempo estimado, pedágios, custo total
data class Route(
    val id: String = "",
    val name: String = "",
    val waypoints: List<Waypoint> = emptyList(),
    val distanceMeters: Long = 0,
    val durationSeconds: Long = 0,
    val tolls: List<TollPlaza> = emptyList(),
    val totalCost: Double = 0.0,
    val vehicle: Vehicle = Vehicle.Default,
    val createdAt: Long = System.currentTimeMillis()
)
