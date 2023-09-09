package com.asu.mc.healthcontextmonitor.model

data class HealthContext(
    val id: Int,
    val timestamp: Long,
    val heartRate: Int,
    val respRate: Int,
    val nausea: Float,
    val headache: Float,
    val diarrhea: Float,
    val soarThroat: Float,
    val fever: Float,
    val muscleAche: Float,
    val lossOfSmellOrTaste: Float,
    val cough: Float,
    val shortnessOfBreath: Float,
    val feelingTired: Float
)
