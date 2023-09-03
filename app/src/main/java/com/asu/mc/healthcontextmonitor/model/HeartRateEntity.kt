package com.asu.mc.healthcontextmonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HeartRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val time: Long,
    val rating: Float
)
