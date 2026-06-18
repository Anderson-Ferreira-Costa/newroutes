package com.newroutes.app.domain.model

import java.util.UUID

/**
 * Representa uma rota completa calculada entre waypoints.
 * Contém distâncias, tempos e custos totais (combustível).
 */
data class Route(
    val id: UUID = UUID.randomUUID(),
    val waypoints: List<Waypoint>,
    val distanceMeters: Long,
    val durationSeconds: Long,
    val vehicle: Vehicle,
    val totalFuelCost: Double,
    val totalCost: Double,
    val encodedPolyline: String? = null
) {
    init {
        require(waypoints.size >= 2) { "Uma rota deve ter pelo menos 2 waypoints" }
        require(totalCost == totalFuelCost) {
            "totalCost deve ser igual a totalFuelCost"
        }
    }
}
