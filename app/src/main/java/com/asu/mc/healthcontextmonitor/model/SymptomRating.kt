package com.asu.mc.healthcontextmonitor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptom_ratings")
data class SymptomRating(
    @PrimaryKey @ColumnInfo(name = "symptom") val symptom: String,
    @ColumnInfo(name = "rating") val rating: Float
)

