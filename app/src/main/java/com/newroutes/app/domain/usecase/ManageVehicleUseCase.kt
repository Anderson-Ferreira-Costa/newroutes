package com.newroutes.app.domain.usecase

import com.newroutes.app.domain.model.Vehicle
import com.newroutes.app.domain.repository.IVehicleRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Agrupa operações de gerenciamento de veículos.
 *
 * Fornece interface unificada para salvar, excluir, listar e definir
 * veículo padrão, centralizando operações que pertencem ao domínio
 * mas são orquestradas por esta classe.
 */
class ManageVehicleUseCase(
    private val vehicleRepository: IVehicleRepository
) {

    /**
     * Salva ou atualiza um veículo no armazenamento persistente.
     *
     * @param vehicle veículo a ser salvo
     * @return Result.success(Unit) em caso de sucesso ou Result.failure com a exceção
     */
    suspend fun save(vehicle: Vehicle): Result<Unit> {
        return try {
            vehicleRepository.saveVehicle(vehicle)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remove um veículo pelo identificador.
     *
     * @param id identificador do veículo a ser removido
     * @return Result.success(Unit) em caso de sucesso ou Result.failure com a exceção
     */
    suspend fun delete(id: UUID): Result<Unit> {
        return try {
            vehicleRepository.deleteVehicle(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marca um veículo como padrão, desmarcando todos os demais.
     *
     * @param id identificador do veículo a ser marcado como padrão
     * @return Result.success(Unit) em caso de sucesso ou Result.failure com a exceção
     */
    suspend fun setDefault(id: UUID): Result<Unit> {
        return try {
            vehicleRepository.setDefault(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retorna todos os veículos cadastrados como stream observável.
     *
     * @return Flow emitindo lista de veículos
     */
    fun getAll(): Flow<List<Vehicle>> {
        return vehicleRepository.getAllVehicles()
    }

    /**
     * Retorna o veículo padrão do usuário como stream observável, ou null.
     *
     * @return Flow emitindo veículo padrão ou null
     */
    fun getDefault(): Flow<Vehicle?> {
        return vehicleRepository.getDefaultVehicle()
    }
}
