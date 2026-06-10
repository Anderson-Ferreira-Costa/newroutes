package com.newroutes.app.domain.repository

import com.newroutes.app.domain.model.TollPlaza
import kotlinx.coroutines.flow.Flow

// TODO: Interface do repositório de pedágios
// Deve definir operações de listar, buscar e atualizar praças de pedágio
interface ITollRepository {
    fun getAllTolls(): Flow<List<TollPlaza>>
    suspend fun getTollById(id: String): TollPlaza?
    suspend fun searchTollsByHighway(highway: String): List<TollPlaza>
    suspend fun importTollsFromCsv(csvContent: String): Int
}
