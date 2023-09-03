package com.asu.mc.healthcontextmonitor.ui.database.dao

import androidx.room.Dao
import androidx.room.Insert
import com.asu.mc.healthcontextmonitor.model.HeartRateEntity

@Dao
interface HeartRateDao {
    @Insert
    fun insert(heartRateEntity: HeartRateEntity)
}
