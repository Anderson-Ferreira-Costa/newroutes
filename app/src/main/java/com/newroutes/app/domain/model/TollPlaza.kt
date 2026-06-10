package com.newroutes.app.domain.model

// TODO: Implementar modelo de praça de pedágio
// Deve conter: nome da praça, rodovia, latitude, longitude, custo, ordem
data class TollPlaza(
    val id: String = "",
    val name: String = "",
    val highway: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val cost: Double = 0.0,
    val order: Int = 0
)
