package com.afian.tugasakhir.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    // Insert satu item notifikasi
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Abaikan jika ada konflik (jarang terjadi dgn autoGenerate)
    suspend fun insert(notification: NotificationHistoryItem)

    // Ambil semua notifikasi, urutkan dari terbaru, dalam bentuk Flow
    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    fun getAllNotificationsSorted(): Flow<List<NotificationHistoryItem>>

    // (Opsional) Hapus semua riwayat
    @Query("DELETE FROM notification_history")
    suspend fun clearAll()

    // (Opsional) Bisa tambahkan fungsi lain seperti delete by ID, mark as read, dll.
}