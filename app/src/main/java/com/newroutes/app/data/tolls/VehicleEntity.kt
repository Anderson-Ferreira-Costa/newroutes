package com.newroutes.app.data.tolls

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.newroutes.app.domain.model.TollCategory
import com.newroutes.app.domain.model.Vehicle
import java.util.UUID

/**
 * Entidade Room para persistência de veículos do usuário.
 *
 * Armazena a categoria do veículo como String (conversion feito pelo [Converters]).
 */
@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    val fuelConsumptionKmPerLiter: Double,
    val fuelPricePerLiter: Double,
    val isDefault: Boolean
) {
    /**
     * Converte esta entidade para o domain model [Vehicle].
     */
    fun toModel(): Vehicle = Vehicle(
        id = UUID.fromString(id),
        name = name,
        category = TollCategory.valueOf(category),
        fuelConsumptionKmPerLiter = fuelConsumptionKmPerLiter,
        fuelPricePerLiter = fuelPricePerLiter,
        isDefault = isDefault
    )

    companion object {
        /**
         * Cria uma [VehicleEntity] a partir de um domain model [Vehicle].
         */
        fun fromModel(vehicle: Vehicle): VehicleEntity = VehicleEntity(
            id = vehicle.id.toString(),
            name = vehicle.name,
            category = vehicle.category.name,
            fuelConsumptionKmPerLiter = vehicle.fuelConsumptionKmPerLiter,
            fuelPricePerLiter = vehicle.fuelPricePerLiter,
            isDefault = vehicle.isDefault
        )
    }
}
