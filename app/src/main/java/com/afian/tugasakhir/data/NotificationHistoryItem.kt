package com.afian.tugasakhir.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_history")
data class NotificationHistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String?,
    val body: String,
    val timestamp: Long
)