package com.newroutes.app.data.tolls

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.model.TollPlaza
import com.newroutes.app.domain.model.Vehicle
import com.newroutes.app.domain.model.Waypoint
import java.util.UUID

/**
 * Entidade Room para persistência de rotas calculadas.
 *
 * Waypoints, pedágios e veículo são serializados como JSON String
 * via [Converters], permitindo que o Room armazene estruturas
 * complexas em colunas individuais.
 */
@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey
    val id: String,
    val distanceMeters: Long,
    val durationSeconds: Long,
    val totalTollCost: Double,
    val totalFuelCost: Double,
    val totalCost: Double,
    val waypointsJson: String,
    val tollPlazasJson: String,
    val vehicleJson: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Converte esta entidade para o domain model [Route].
     */
    fun toModel(): Route = Route(
        id = UUID.fromString(id),
        distanceMeters = distanceMeters,
        durationSeconds = durationSeconds,
        totalTollCost = totalTollCost,
        totalFuelCost = totalFuelCost,
        totalCost = totalCost,
        waypoints = convertToWaypoints(waypointsJson),
        tollPlazas = convertToTollPlazas(tollPlazasJson),
        vehicle = convertToVehicle(vehicleJson)
    )

    companion object {
        /**
         * Cria uma [RouteEntity] a partir de um domain model [Route].
         */
        fun fromModel(route: Route): RouteEntity = RouteEntity(
            id = route.id.toString(),
            distanceMeters = route.distanceMeters,
            durationSeconds = route.durationSeconds,
            totalTollCost = route.totalTollCost,
            totalFuelCost = route.totalFuelCost,
            totalCost = route.totalCost,
            waypointsJson = convertFromWaypoints(route.waypoints),
            tollPlazasJson = convertFromTollPlazas(route.tollPlazas),
            vehicleJson = convertFromVehicle(route.vehicle)
        )

        private fun convertFromWaypoints(list: List<Waypoint>): String {
            val converters = Converters()
            return converters.fromWaypointList(list)
        }

        private fun convertToWaypoints(value: String): List<Waypoint> {
            val converters = Converters()
            return converters.toWaypointList(value)
        }

        private fun convertFromTollPlazas(list: List<TollPlaza>): String {
            val converters = Converters()
            return converters.fromTollPlazaList(list)
        }

        private fun convertToTollPlazas(value: String): List<TollPlaza> {
            val converters = Converters()
            return converters.toTollPlazaList(value)
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
