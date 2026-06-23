package com.newroutes.app.domain.model

import java.util.UUID

/**
 * Representa um veículo cadastrado pelo usuário para cálculo de custos de combustível.
 */
data class Vehicle(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val category: TollCategory,
    val fuelConsumptionKmPerLiter: Double,
    val fuelPricePerLiter: Double,
    val isDefault: Boolean
)
