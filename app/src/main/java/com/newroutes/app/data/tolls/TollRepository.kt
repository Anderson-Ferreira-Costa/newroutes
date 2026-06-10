package com.newroutes.app.data.tolls

import com.newroutes.app.domain.model.TollPlaza
import com.newroutes.app.domain.model.Waypoint
import com.newroutes.app.domain.repository.ITollRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementação de [ITollRepository] usando Room para persistência local.
 *
 * Recebe [TollPlazaDao] via injeção de dependência (Hilt) e traduz
 * entre domain models e entidades Room.
 */
class TollRepository(
    private val dao: TollPlazaDao
) : ITollRepository {

    private companion object {
        /**
         * Fator de conversão: 1 grau de latitude/longitude ≈ 111.000 metros.
         */
        const val DEG_TO_METERS = 111_000.0
    }

    /**
     * Retorna todas as praças de pedágio como Flow de domain models.
     */
    override fun getAllTollPlazas(): Flow<List<TollPlaza>> =
        dao.getAll().map { entities ->
            entities.map { it.toModel() }
        }

    /**
     * Retorna as praças de pedágio de uma rodovia específica como Flow.
     */
    override fun getTollPlazasByHighway(highway: String): Flow<List<TollPlaza>> =
        dao.getByHighway(highway).map { entities ->
            entities.map { it.toModel() }
        }

    /**
     * Encontra praças de pedágio próximas a uma rota definida por waypoints,
     * dentro de um raio dado em metros.
     *
     * Para cada segmento entre waypoints consecutivos, calcula um bounding box
     * com padding baseado no raio, consulta o DAO e agrega os resultados
     * removendo duplicatas pelo ID.
     */
    override suspend fun getTollPlazasNearRoute(
        waypoints: List<Waypoint>,
        radiusMeters: Double
    ): List<TollPlaza> {
        val results = mutableListOf<TollPlazaEntity>()

        for (i in waypoints.indices.dropLast(1)) {
            val current = waypoints[i]
            val next = waypoints[i + 1]

            val minLat = minOf(current.latitude, next.latitude) - radiusMeters / DEG_TO_METERS
            val maxLat = maxOf(current.latitude, next.latitude) + radiusMeters / DEG_TO_METERS
            val minLon = minOf(current.longitude, next.longitude) - radiusMeters / DEG_TO_METERS
            val maxLon = maxOf(current.longitude, next.longitude) + radiusMeters / DEG_TO_METERS

            val nearby = dao.getNearby(minLat, maxLat, minLon, maxLon)
            results.addAll(nearby)
        }

        return results.distBy { it.id }.map { it.toModel() }
    }

    /**
     * Insere ou atualiza uma praça de pedágio.
     */
    override suspend fun upsertTollPlaza(tollPlaza: TollPlaza) {
        dao.upsert(TollPlazaEntity.fromModel(tollPlaza))
    }

    /**
     * Insere ou atualiza em lote uma lista de praças de pedágio.
     */
    override suspend fun upsertAll(tollPlazas: List<TollPlaza>) {
        val entities = tollPlazas.map { TollPlazaEntity.fromModel(it) }
        dao.upsertAll(entities)
    }

    /**
     * Remove duplicatas de uma lista mantendo a primeira ocorrência de cada ID.
     */
    private fun <T : Any> List<T>.distBy(selector: (T) -> Any): List<T> {
        val seen = mutableSetOf<Any>()
        return filter { seen.add(selector(it)) }
    }
}
