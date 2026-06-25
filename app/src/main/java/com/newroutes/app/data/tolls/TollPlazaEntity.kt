package com.newroutes.app.data.tolls

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.newroutes.app.domain.model.TollCategory
import com.newroutes.app.domain.model.TollPlaza
import java.util.UUID

/**
 * Entidade Room para persistência de praças de pedágio.
 *
 * Espelha a estrutura do domain model [TollPlaza], com o UUID
 * armazenado como String (conversion feito pelo [Converters]).
 */
@Entity(tableName = "toll_plazas")
data class TollPlazaEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val highway: String,
    val latitude: Double,
    val longitude: Double,
    val cost: Double,
    val category: String
) {
    /**
     * Converte esta entidade para o domain model [TollPlaza].
     */
    fun toModel(): TollPlaza = TollPlaza(
        id = UUID.fromString(id),
        name = name,
        highway = highway,
        latitude = latitude,
        longitude = longitude,
        cost = cost,
        category = TollCategory.valueOf(category)
    )

    companion object {
        /**
         * Cria uma [TollPlazaEntity] a partir de um domain model [TollPlaza].
         */
        fun fromModel(tollPlaza: TollPlaza): TollPlazaEntity = TollPlazaEntity(
            id = tollPlaza.id.toString(),
            name = tollPlaza.name,
            highway = tollPlaza.highway,
            latitude = tollPlaza.latitude,
            longitude = tollPlaza.longitude,
            cost = tollPlaza.cost,
            category = tollPlaza.category.name
        )
    }
}
