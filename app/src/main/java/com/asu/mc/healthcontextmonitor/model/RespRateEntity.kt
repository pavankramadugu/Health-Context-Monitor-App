package com.asu.mc.healthcontextmonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resp_ratings")
data class RespRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val time: Long,
    val rating: Int
)
