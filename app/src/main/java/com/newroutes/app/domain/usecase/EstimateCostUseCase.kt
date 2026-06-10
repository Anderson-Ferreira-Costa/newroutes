package com.newroutes.app.domain.usecase

import com.newroutes.app.domain.model.Route
import com.newroutes.app.domain.repository.ITollRepository

/**
 * Recalcula os custos de uma rota já existente com um veículo diferente.
 *
 * Útil quando o usuário altera o veículo selecionado e precisa
 * reavaliar imediatamente os custos (pedágio + combustível) sem
 * recalculadar a rota completa.
 */
class EstimateCostUseCase(
    private val tollRepository: ITollRepository
) {

    /**
     * Recalcula os custos de uma rota com base em um novo veículo.
     *
     * @param route rota existente cujos custos serão recalculados
     * @param vehicle novo veículo para recálculo dos custos
     * @return Result com Route copiada e custos atualizados, ou falha
     */
    suspend fun invoke(route: Route, vehicle: com.newroutes.app.domain.model.Vehicle): Result<Route> {
        return try {
            val tollPlazas = tollRepository
                .getTollPlazasNearRoute(route.waypoints, radiusMeters = 500.0)
                .filter { it.category == vehicle.category }

            val totalTollCost = tollPlazas.sumOf { it.cost }

            // TODO: distanceMeters e durationSeconds são placeholders (0L).
            // Valores reais virão do OsrmClient (camada data/) em sessão futura.
            val distanceMeters: Long = 0L

            val totalFuelCost = calculateFuelCost(
                distanceMeters = distanceMeters,
                consumptionKmPerLiter = vehicle.fuelConsumptionKmPerLiter,
                fuelPricePerLiter = vehicle.fuelPricePerLiter
            )

            Result.success(
                route.copy(
                    vehicle = vehicle,
                    tollPlazas = tollPlazas,
                    totalTollCost = totalTollCost,
                    totalFuelCost = totalFuelCost,
                    totalCost = totalTollCost + totalFuelCost
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
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
