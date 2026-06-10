package com.newroutes.app.domain.repository

import com.newroutes.app.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Contrato para gerenciamento de veículos do usuário.
 * Implementações concretas vivem em data/tolls/.
 */
interface IVehicleRepository {

    /** Retorna uma Flow emitindo todos os veículos cadastrados. */
    fun getAllVehicles(): Flow<List<Vehicle>>

    /** Retorna uma Flow emitindo o veículo padrão do usuário, ou null. */
    fun getDefaultVehicle(): Flow<Vehicle?>

    /** Salva ou atualiza um veículo no armazenamento persistente. */
    suspend fun saveVehicle(vehicle: Vehicle): Unit

    /** Remove um veículo pelo identificador. */
    suspend fun deleteVehicle(id: UUID): Unit

    /** Marca um veículo como padrão (isDefault = true), desmarcando os demais. */
    suspend fun setDefault(id: UUID): Unit
}
