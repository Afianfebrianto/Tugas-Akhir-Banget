package com.afian.tugasakhir.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: NotificationHistoryItem)


    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    fun getAllNotificationsSorted(): Flow<List<NotificationHistoryItem>>


    @Query("DELETE FROM notification_history")
    suspend fun clearAll()


}