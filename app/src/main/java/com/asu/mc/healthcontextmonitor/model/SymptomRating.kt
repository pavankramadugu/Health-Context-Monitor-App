package com.asu.mc.healthcontextmonitor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptom_ratings")
data class SymptomRating(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Auto-generated primary key
    @ColumnInfo(name = "timestamp") val timestamp: Long,  // Timestamp for the group of symptoms
    @ColumnInfo(name = "symptom") val symptom: String,  // Symptom description
    @ColumnInfo(name = "rating") var rating: Float  // Symptom rating
)
