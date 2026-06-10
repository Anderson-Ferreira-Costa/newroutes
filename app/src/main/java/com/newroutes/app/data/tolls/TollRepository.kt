package com.newroutes.app.data.tolls

import com.newroutes.app.data.tolls.entities.TollEntity
import com.newroutes.app.domain.model.TollPlaza
import com.newroutes.app.domain.repository.ITollRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// TODO: Implementar repositório de pedágios com Room
class TollDatabaseRepository(
    private val tollDao: TollDao
) : ITollRepository {

    override fun getAllTolls(): Flow<List<TollPlaza>> {
        return tollDao.getAllTolls().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTollById(id: String): TollPlaza? {
        return tollDao.getTollById(id)?.toDomain()
    }

    override suspend fun searchTollsByHighway(highway: String): List<TollPlaza> {
        return tollDao.searchByHighway(highway).map { it.toDomain() }
    }

    override suspend fun importTollsFromCsv(csvContent: String): Int {
        TODO("Implementar importação de pedágios a partir de CSV da ANTT")
    }

    private fun TollEntity.toDomain(): TollPlaza {
        return TollPlaza(
            id = id,
            name = name,
            highway = highway,
            latitude = latitude,
            longitude = longitude,
            cost = cost,
            order = order
        )
    }
}
