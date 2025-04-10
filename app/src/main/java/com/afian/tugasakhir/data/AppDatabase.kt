package com.afian.tugasakhir.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NotificationHistoryItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Singleton pattern untuk mencegah multiple instance database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Nama file database
                )
                    // .fallbackToDestructiveMigration() // Hati-hati: hapus data lama saat migrasi versi
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}