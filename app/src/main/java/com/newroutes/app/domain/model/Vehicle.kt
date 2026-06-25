package com.newroutes.app.domain.model

import java.util.UUID

/**
 * Representa um veículo para cálculo de custos de pedágio e combustível.
 * Contém a categoria compatível com a tabela de pedágios e parâmetros de consumo.
 */
data class Vehicle(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val category: TollCategory,
    val fuelConsumptionKmPerLiter: Double,
    val fuelPricePerLiter: Double,
    val isDefault: Boolean
)
