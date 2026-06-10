package com.newroutes.app.data.tolls

import androidx.room.TypeConverter
import com.newroutes.app.domain.model.TollCategory
import com.newroutes.app.domain.model.TollPlaza
import com.newroutes.app.domain.model.Vehicle
import com.newroutes.app.domain.model.Waypoint
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.util.UUID

/**
 * Conversores de tipo para o Room, permitindo armazenar no banco dados
 * tipos não suportados nativamente (UUID, enums customizados, listas de domain models)
 * serializando-os como JSON via Moshi.
 */
class Converters {

    private val moshi: Moshi = Moshi.Builder().build()

    // ── UUID ──────────────────────────────────────────────────────────

    /**
     * Converte um UUID para String para armazenamento no Room.
     */
    @TypeConverter
    fun fromUuid(uuid: UUID): String = uuid.toString()

    /**
     * Converte uma String armazenada no Room de volta para UUID.
     */
    @TypeConverter
    fun toUuid(value: String): UUID = UUID.fromString(value)

    // ── TollCategory ──────────────────────────────────────────────────

    /**
     * Converte um TollCategory para String (nome do enum) para armazenamento.
     */
    @TypeConverter
    fun fromTollCategory(category: TollCategory): String = category.name

    /**
     * Converte uma String armazenada de volta para TollCategory.
     */
    @TypeConverter
    fun toTollCategory(value: String): TollCategory = TollCategory.valueOf(value)

    // ── List<TollPlaza> ───────────────────────────────────────────────

    /**
     * Serializa uma lista de TollPlaza em JSON String via Moshi.
     */
    @TypeConverter
    fun fromTollPlazaList(list: List<TollPlaza>): String {
        val type = Types.newParameterizedType(List::class.java, TollPlaza::class.java)
        val adapter: JsonAdapter<List<TollPlaza>> = moshi.adapter(type)
        return adapter.toJson(list)
    }

    /**
     * Desserializa uma String JSON de volta para List<TollPlaza>.
     */
    @TypeConverter
    fun toTollPlazaList(value: String): List<TollPlaza> {
        val type = Types.newParameterizedType(List::class.java, TollPlaza::class.java)
        val adapter: JsonAdapter<List<TollPlaza>> = moshi.adapter(type)
        return adapter.fromJson(value) ?: emptyList()
    }

    // ── List<Waypoint> ────────────────────────────────────────────────

    /**
     * Serializa uma lista de Waypoints em JSON String via Moshi.
     */
    @TypeConverter
    fun fromWaypointList(list: List<Waypoint>): String {
        val type = Types.newParameterizedType(List::class.java, Waypoint::class.java)
        val adapter: JsonAdapter<List<Waypoint>> = moshi.adapter(type)
        return adapter.toJson(list)
    }

    /**
     * Desserializa uma String JSON de volta para List<Waypoint>.
     */
    @TypeConverter
    fun toWaypointList(value: String): List<Waypoint> {
        val type = Types.newParameterizedType(List::class.java, Waypoint::class.java)
        val adapter: JsonAdapter<List<Waypoint>> = moshi.adapter(type)
        return adapter.fromJson(value) ?: emptyList()
    }

    // ── Vehicle ───────────────────────────────────────────────────────

    /**
     * Serializa um Vehicle para JSON String via Moshi.
     */
    @TypeConverter
    fun fromVehicle(vehicle: Vehicle): String {
        val adapter: JsonAdapter<Vehicle> = moshi.adapter(Vehicle::class.java)
        return adapter.toJson(vehicle)
    }

    /**
     * Desserializa uma String JSON de volta para Vehicle.
     */
    @TypeConverter
    fun toVehicle(value: String): Vehicle {
        val adapter: JsonAdapter<Vehicle> = moshi.adapter(Vehicle::class.java)
        return adapter.fromJson(value) ?: throw IllegalArgumentException("Cannot deserialize Vehicle from: $value")
    }
}
