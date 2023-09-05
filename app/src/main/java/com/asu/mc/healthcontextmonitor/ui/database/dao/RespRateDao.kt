package com.asu.mc.healthcontextmonitor.ui.database.dao

import androidx.room.Dao
import androidx.room.Insert
import com.asu.mc.healthcontextmonitor.model.RespRateEntity

@Dao
interface RespRateDao {
    @Insert
    fun insert(respRateEntity: RespRateEntity)
}
