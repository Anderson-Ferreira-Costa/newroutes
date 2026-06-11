package com.newroutes.app.domain.usecase

import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.model.TollPlaza
import com.newroutes.app.domain.model.Vehicle
import com.newroutes.app.domain.model.Waypoint
import com.newroutes.app.domain.repository.ITollRepository
import com.newroutes.app.domain.repository.IVehicleRepository
import com.newroutes.app.data.routing.OsrmRepository
import kotlinx.coroutines.flow.first

/**
 * Orquestra o cálculo de uma rota completa dado origem, destino e waypoints intermediários.
 *
 * Responsabilidades:
 * - Validar número mínimo de waypoints
 * - Resolver o veículo a ser usado (argumento ou padrão)
 * - Buscar e filtrar praças de pedágio compatíveis com a categoria do veículo
 * - Obter distância e duração reais via OSRM
 * - Calcular custos de pedágio e combustível
 */
class CalculateRouteUseCase(
    private val tollRepository: ITollRepository,
    private val vehicleRepository: IVehicleRepository,
    private val osrmRepository: OsrmRepository
) {

    /**
     * Calcula uma rota completa com custos de pedágio e combustível.
     *
     * @param waypoints lista de pontos da rota (mínimo 2)
     * @param vehicle veículo usado no cálculo; se null, usa o veículo padrão
     * @return Result com Route calculada ou falha com exceção adequada
     */
    suspend fun invoke(waypoints: List<Waypoint>, vehicle: Vehicle? = null): Result<Route> {
        if (waypoints.size < 2) {
            return Result.failure(IllegalArgumentException("Waypoints deve conter pelo menos 2 itens"))
        }

        val resolvedVehicle = resolveVehicle(vehicle)
            .mapCatching { resolved ->
                if (resolved == null) {
                    throw IllegalStateException("No default vehicle configured")
                }
                resolved
            }

        return resolvedVehicle.mapCatching { v ->
            val tollPlazas = fetchAndFilterTollPlazas(waypoints, v.category)
            val totalTollCost = tollPlazas.sumOf { it.cost }

            val osrmResult = osrmRepository.getRoute(waypoints)
                .getOrElse {
                    throw IllegalStateException("Falha ao calcular rota com OSRM: ${it.message}")
                }

            val distanceMeters = osrmResult.distanceMeters
            val durationSeconds = osrmResult.durationSeconds
            val polyline = osrmResult.encodedPolyline

            val totalFuelCost = calculateFuelCost(
                distanceMeters = distanceMeters,
                consumptionKmPerLiter = v.fuelConsumptionKmPerLiter,
                fuelPricePerLiter = v.fuelPricePerLiter
            )

            Route(
                waypoints = waypoints,
                distanceMeters = distanceMeters,
                durationSeconds = durationSeconds,
                tollPlazas = tollPlazas,
                vehicle = v,
                totalTollCost = totalTollCost,
                totalFuelCost = totalFuelCost,
                totalCost = totalTollCost + totalFuelCost,
                encodedPolyline = polyline
            )
        }
    }

    /**
     * Resolve o veículo a ser usado: retorna o argumento se fornecido,
     * caso contrário busca o veículo padrão via repository.
     */
    private suspend fun resolveVehicle(vehicle: Vehicle?): Result<Vehicle?> {
        return if (vehicle != null) {
            Result.success(vehicle)
        } else {
            try {
                Result.success(vehicleRepository.getDefaultVehicle().first())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Busca praças de pedágio próximas à rota e filtra pela categoria do veículo.
     * Raio de busca: 500 metros.
     */
    private suspend fun fetchAndFilterTollPlazas(
        waypoints: List<Waypoint>,
        category: com.newroutes.app.domain.model.TollCategory
    ): List<TollPlaza> {
        return try {
            tollRepository.getTollPlazasNearRoute(waypoints, radiusMeters = 500.0)
                .filter { it.category == category }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Calcula o custo de combustível com base na distância e nos parâmetros do veículo.
     *
     * Fórmula: (distanceMeters / 1000) / consumoKmPorLitro * precoPorLitro
     */
    private fun calculateFuelCost(
        distanceMeters: Long,
        consumptionKmPerLiter: Double,
        fuelPricePerLiter: Double
    ): Double {
        val distanceKm = distanceMeters / 1000.0
        if (consumptionKmPerLiter <= 0) return 0.0
        return (distanceKm / consumptionKmPerLiter) * fuelPricePerLiter
    }
}
