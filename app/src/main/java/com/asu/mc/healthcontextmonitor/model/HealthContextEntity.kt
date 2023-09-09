package com.asu.mc.healthcontextmonitor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_context")
data class HealthContextEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "heart_rate") var heartRate: Float,
    @ColumnInfo(name = "resp_rate") var respRate: Int,
    @ColumnInfo(name = "nausea") var nausea: Float,
    @ColumnInfo(name = "headache") var headache: Float,
    @ColumnInfo(name = "diarrhea") var diarrhea: Float,
    @ColumnInfo(name = "soar_throat") var soarThroat: Float,
    @ColumnInfo(name = "fever") var fever: Float,
    @ColumnInfo(name = "muscle_ache") var muscleAche: Float,
    @ColumnInfo(name = "loss_of_smell_or_taste") var lossOfSmellOrTaste: Float,
    @ColumnInfo(name = "cough") var cough: Float,
    @ColumnInfo(name = "shortness_of_breath") var shortnessOfBreath: Float,
    @ColumnInfo(name = "feeling_tired") var feelingTired: Float
)
