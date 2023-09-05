package com.asu.mc.healthcontextmonitor.ui.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.asu.mc.healthcontextmonitor.model.HeartRateEntity
import com.asu.mc.healthcontextmonitor.model.RespRateEntity
import com.asu.mc.healthcontextmonitor.model.SymptomRating
import com.asu.mc.healthcontextmonitor.ui.database.dao.HeartRateDao
import com.asu.mc.healthcontextmonitor.ui.database.dao.RespRateDao
import com.asu.mc.healthcontextmonitor.ui.database.dao.SymptomRatingDao

@Database(entities = [SymptomRating::class, HeartRateEntity::class, RespRateEntity::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun symptomRatingDao(): SymptomRatingDao
    abstract fun heartRateDao(): HeartRateDao
    abstract fun respRateDao(): RespRateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app-database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
