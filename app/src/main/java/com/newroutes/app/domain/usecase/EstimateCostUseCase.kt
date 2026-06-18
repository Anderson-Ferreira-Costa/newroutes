package com.newroutes.app.domain.usecase

import com.newroutes.app.domain.model.Route

/**
 * Recalcula os custos de uma rota já existente com um veículo diferente.
 * 
 * Útil quando o usuário altera o veículo selecionado e precisa
 * reavaliar imediatamente os custos de combustível sem
 * recalcular a rota completa.
 */
class EstimateCostUseCase {

    /**
     * Recalcula os custos de uma rota com base em um novo veículo.
     *
     * @param route rota existente cujos custos serão recalculados
     * @param vehicle novo veículo para recálculo dos custos
     * @return Result com Route copiada e custos atualizados, ou falha
     */
    suspend fun invoke(route: Route, vehicle: com.newroutes.app.domain.model.Vehicle): Result<Route> {
        return try {
            val distanceMeters = route.distanceMeters

            val totalFuelCost = calculateFuelCost(
                distanceMeters = distanceMeters,
                consumptionKmPerLiter = vehicle.fuelConsumptionKmPerLiter,
                fuelPricePerLiter = vehicle.fuelPricePerLiter
            )

            Result.success(
                route.copy(
                    vehicle = vehicle,
                    totalFuelCost = totalFuelCost,
                    totalCost = totalFuelCost
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
