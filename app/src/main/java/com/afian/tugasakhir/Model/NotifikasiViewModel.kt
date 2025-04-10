package com.afian.tugasakhir.Model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.data.AppDatabase
import com.afian.tugasakhir.data.NotificationDao
import com.afian.tugasakhir.data.NotificationHistoryItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    // Dapatkan DAO secara manual
    private val notificationDao: NotificationDao =
        AppDatabase.getDatabase(application).notificationDao()

    // StateFlow untuk riwayat (tidak berubah)
    val notificationHistory: StateFlow<List<NotificationHistoryItem>> =
        notificationDao.getAllNotificationsSorted()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Contoh fungsi tambahan
    fun clearHistory() {
        viewModelScope.launch {
            notificationDao.clearAll()
        }
    }
}