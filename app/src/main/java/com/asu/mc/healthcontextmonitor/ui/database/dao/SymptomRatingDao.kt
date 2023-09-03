package com.asu.mc.healthcontextmonitor.ui.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.asu.mc.healthcontextmonitor.model.SymptomRating

@Dao
interface SymptomRatingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(symptomRating: SymptomRating)

    @Query("SELECT * FROM symptom_ratings WHERE symptom = :symptom")
    suspend fun getRatingBySymptom(symptom: String): SymptomRating?
}
