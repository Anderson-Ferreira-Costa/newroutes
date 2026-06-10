package com.newroutes.app.domain.model

// TODO: Implementar modelo de ponto de passagem (waypoint)
// Deve conter: latitude, longitude, nome/endereço, ordem na rota
data class Waypoint(
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val name: String = "",
    val order: Int = 0
)
