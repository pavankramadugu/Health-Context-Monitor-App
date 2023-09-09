package com.asu.mc.healthcontextmonitor.ui.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.asu.mc.healthcontextmonitor.model.HealthContextEntity

@Dao
interface HealthContextDao {

    @Query("SELECT * FROM health_context WHERE timestamp = :timestamp")
    fun getRecordByTimestamp(timestamp: Long): HealthContextEntity?

    @Insert
    fun insert(record: HealthContextEntity)

    @Update
    fun update(record: HealthContextEntity)

    @Query("SELECT * FROM health_context ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRecord(): HealthContextEntity?
}
