package com.newroutes.app.data.routing

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.newroutes.app.data.tolls.Converters
import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.model.Vehicle
import com.newroutes.app.domain.model.Waypoint
import java.util.UUID

/**
 * Entidade Room para persistência de rotas calculadas.
 *
 * Waypoints e veículo são serializados como JSON String
 * via [Converters], permitindo que o Room armazene estruturas
 * complexas em colunas individuais.
 */
@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey
    val id: String,
    val distanceMeters: Long,
    val durationSeconds: Long,
    val totalFuelCost: Double,
    val totalCost: Double,
    val waypointsJson: String,
    val vehicleJson: String,
    val encodedPolyline: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Converte esta entidade para o domain model [Route].
     */
    fun toModel(): Route = Route(
        id = UUID.fromString(id),
        distanceMeters = distanceMeters,
        durationSeconds = durationSeconds,
        totalFuelCost = totalFuelCost,
        totalCost = totalCost,
        waypoints = convertToWaypoints(waypointsJson),
        vehicle = convertToVehicle(vehicleJson),
        encodedPolyline = encodedPolyline
    )

    companion object {
        /**
         * Cria uma [RouteEntity] a partir de um domain model [Route].
         */
        fun fromModel(route: Route): RouteEntity = RouteEntity(
            id = route.id.toString(),
            distanceMeters = route.distanceMeters,
            durationSeconds = route.durationSeconds,
            totalFuelCost = route.totalFuelCost,
            totalCost = route.totalCost,
            waypointsJson = convertFromWaypoints(route.waypoints),
            vehicleJson = convertFromVehicle(route.vehicle),
            encodedPolyline = route.encodedPolyline
        )

        private fun convertFromWaypoints(list: List<Waypoint>): String {
            val converters = Converters()
            return converters.fromWaypointList(list)
        }

        private fun convertToWaypoints(value: String): List<Waypoint> {
            val converters = Converters()
            return converters.toWaypointList(value)
        }

        private fun convertFromVehicle(vehicle: Vehicle): String {
            val converters = Converters()
            return converters.fromVehicle(vehicle)
        }

        private fun convertToVehicle(value: String): Vehicle {
            val converters = Converters()
            return converters.toVehicle(value)
        }
    }
}
