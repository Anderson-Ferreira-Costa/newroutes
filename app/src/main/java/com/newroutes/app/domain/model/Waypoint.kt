package com.newroutes.app.domain.model

import java.util.UUID

/**
 * Representa um ponto na rota, seja origem, destino ou parada intermediária.
 * Contém coordenadas geográficas e identificação textual opcional.
 */
data class Waypoint(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)
