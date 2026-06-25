package com.newroutes.app.domain.model

import java.util.UUID

/**
 * Representa uma praça de pedágio encontrada ao longo de uma rota.
 * Inclui identificação, localização, valor cobrado e categoria do veículo.
 */
enum class TollCategory {
    MOTORCYCLE,
    CAR,
    CAR_WITH_TRAILER,
    TRUCK_2_AXLES,
    TRUCK_3_AXLES,
    TRUCK_4_AXLES,
    TRUCK_5_AXLES,
    TRUCK_6_AXLES,
    BUS
}

data class TollPlaza(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val highway: String,
    val latitude: Double,
    val longitude: Double,
    val cost: Double,
    val category: TollCategory
)
