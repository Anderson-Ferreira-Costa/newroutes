package com.newroutes.app.domain.model

// TODO: Implementar modelo de veículo (para cálculo de pedágio)
// Deve conter: tipo de veículo, peso, categoria (conforme tabela ANTT)
data class Vehicle(
    val type: String = "",
    val weightKg: Long = 0,
    val category: Int = 1
) {
    companion object {
        val Default = Vehicle(
            type = "Carro",
            weightKg = 1500,
            category = 1
        )
    }
}
