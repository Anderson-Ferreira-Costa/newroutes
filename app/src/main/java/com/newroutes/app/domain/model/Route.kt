package com.newroutes.app.domain.model

import java.util.UUID

/**
 * Representa uma rota completa calculada entre waypoints.
 * Contém distâncias, tempos, pedágios e custos totais (pedágio + combustível).
 */
data class Route(
    val id: UUID = UUID.randomUUID(),
    val waypoints: List<Waypoint>,
    val distanceMeters: Long,
    val durationSeconds: Long,
    val tollPlazas: List<TollPlaza>,
    val vehicle: Vehicle,
    val totalTollCost: Double,
    val totalFuelCost: Double,
    val totalCost: Double,
    val encodedPolyline: String? = null
) {
    init {
        require(waypoints.size >= 2) { "Uma rota deve ter pelo menos 2 waypoints" }
        require(totalCost == (totalTollCost + totalFuelCost)) {
            "totalCost deve ser a soma de totalTollCost + totalFuelCost"
        }
    }
}
