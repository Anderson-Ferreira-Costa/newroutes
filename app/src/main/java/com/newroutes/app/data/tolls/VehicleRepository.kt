package com.newroutes.app.data.tolls

import com.newroutes.app.domain.model.Vehicle
import com.newroutes.app.domain.repository.IVehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Implementação de [IVehicleRepository] usando Room para persistência local.
 *
 * Recebe [VehicleDao] via injeção de dependência (Hilt) e traduz
 * entre domain models e entidades Room.
 */
class VehicleRepository(
    private val dao: VehicleDao
) : IVehicleRepository {

    /**
     * Retorna todos os veículos cadastrados como Flow de domain models.
     */
    override fun getAllVehicles(): Flow<List<Vehicle>> =
        dao.getAll().map { entities ->
            entities.map { it.toModel() }
        }

    /**
     * Retorna o veículo padrão do usuário como Flow, ou null.
     */
    override fun getDefaultVehicle(): Flow<Vehicle?> =
        dao.getDefault().map { entity ->
            entity?.toModel()
        }

    /**
     * Salva ou atualiza um veículo no armazenamento persistente.
     */
    override suspend fun saveVehicle(vehicle: Vehicle) {
        dao.upsert(VehicleEntity.fromModel(vehicle))
    }

    /**
     * Remove um veículo pelo identificador.
     */
    override suspend fun deleteVehicle(id: UUID) {
        dao.deleteById(id.toString())
    }

    /**
     * Marca um veículo como padrão, desmarcando todos os demais.
     */
    override suspend fun setDefault(id: UUID) {
        dao.clearDefault()
        dao.setDefault(id.toString())
    }
}
