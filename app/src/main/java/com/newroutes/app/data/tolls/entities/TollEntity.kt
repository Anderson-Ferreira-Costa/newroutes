package com.newroutes.app.data.tolls.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// TODO: Entity Room para praça de pedágio
@Entity(tableName = "tolls")
data class TollEntity(
    @PrimaryKey(val = true)
    val id: String = "",
    val name: String = "",
    val highway: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val cost: Double = 0.0,
    val order: Int = 0
)
