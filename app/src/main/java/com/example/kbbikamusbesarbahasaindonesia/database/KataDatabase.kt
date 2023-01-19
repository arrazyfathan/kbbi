package com.example.kbbikamusbesarbahasaindonesia.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kbbikamusbesarbahasaindonesia.model.History
import com.example.kbbikamusbesarbahasaindonesia.model.Kata

@Database(
    entities = [Kata::class, History::class],
    version = 3
)
@TypeConverters(Converters::class)
abstract class KataDatabase : RoomDatabase() {

    abstract fun kataDao(): KataDao

    companion object {

        @Volatile
        private var INSTANCE: KataDatabase? = null

        fun getInstance(context: Context): KataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KataDatabase::class.java,
                    "kata_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}