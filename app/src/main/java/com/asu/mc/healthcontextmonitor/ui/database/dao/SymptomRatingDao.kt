package com.asu.mc.healthcontextmonitor.ui.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.asu.mc.healthcontextmonitor.model.SymptomRating

@Dao
interface SymptomRatingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(symptomRating: SymptomRating)

    @Update
    suspend fun update(symptomRating: SymptomRating)

    @Query("SELECT * FROM symptom_ratings WHERE symptom = :symptom ORDER BY timestamp DESC LIMIT 1")
    suspend fun getRatingBySymptom(symptom: String): SymptomRating?

    @Query("SELECT * FROM symptom_ratings WHERE symptom = :symptom AND timestamp = :timestamp")
    suspend fun getRatingBySymptomAndTimestamp(symptom: String, timestamp: Long): SymptomRating?

    @Query("SELECT MAX(timestamp) FROM symptom_ratings")
    suspend fun getLatestTimestamp(): Long?
}
