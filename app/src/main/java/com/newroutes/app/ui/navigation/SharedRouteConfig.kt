package com.newroutes.app.ui.navigation

import com.newroutes.app.domain.model.Vehicle
import com.newroutes.app.domain.model.Waypoint

/**
 * Configuração compartilhada de rota para comunicação entre telas
 * via Navigation Compose (que não passa parâmetros complexos por intent).
 */
data class SharedRouteConfig(
    var waypoints: List<Waypoint> = emptyList(),
    var vehicle: Vehicle? = null
)
