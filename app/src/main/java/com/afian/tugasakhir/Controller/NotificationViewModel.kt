package com.afian.tugasakhir.Controller

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.PanggilanHistoryMahasiswaItem
import com.afian.tugasakhir.data.AppDatabase
import com.afian.tugasakhir.data.NotificationDao
import com.afian.tugasakhir.data.NotificationHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.*

// Ganti nama agar lebih jelas? Misal: MahasiswaNotificationViewModel
class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    // DAO untuk Notifikasi Umum (jika masih dipakai)
    private val notificationDao: NotificationDao = AppDatabase.getDatabase(application).notificationDao()

    // StateFlow untuk Notifikasi Umum (jika masih dipakai)
    val notificationHistory: StateFlow<List<NotificationHistoryItem>> =
        notificationDao.getAllNotificationsSorted()
            .stateInViewModel(emptyList())

    // --- State BARU untuk Riwayat PANGGILAN ---
    private val _panggilanHistory = MutableStateFlow<List<PanggilanHistoryMahasiswaItem>>(emptyList())
    val panggilanHistory: StateFlow<List<PanggilanHistoryMahasiswaItem>> = _panggilanHistory.asStateFlow()

    private val _isLoadingHistory = mutableStateOf(false)
//    val isLoadingHistory: State<Boolean> = _isLoadingHistory
    private val _responseMessage = mutableStateOf<String?>(null) // Untuk pesan accept/decline/error
//    val responseMessage: State<String?> = _responseMessage
    private val _isRespondingPanggilanId = mutableStateOf<Int?>(null) // ID panggilan yg sedang direspon
//    val isRespondingPanggilanId: State<Int?> = _isRespondingPanggilanId
    // --- Akhir State Panggilan ---


    // ID Mahasiswa yang sedang login (ambil dari SharedPreferences)
    private var currentMahasiswaId: Int = -1

    init {
        // Ambil ID Mahasiswa saat ViewModel dibuat
        val userPrefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentMahasiswaId = userPrefs.getInt("user_id", -1)
        val userRole = userPrefs.getString("user_role", null)

        // Hanya fetch riwayat jika user adalah mahasiswa
        if (currentMahasiswaId != -1 && userRole == "mhs") {
            fetchPanggilanHistory()
        } else {
            Log.w("NotificationViewModel", "Current user is not a student or ID not found.")
        }
    }

    // Fungsi fetch riwayat PANGGILAN
    fun fetchPanggilanHistory() {
        if (currentMahasiswaId == -1) {
            _responseMessage.value = "ID Mahasiswa tidak ditemukan."
            return
        }
        if (_isLoadingHistory.value) return
        _isLoadingHistory.value = true
        _responseMessage.value = null
        viewModelScope.launch {
            try {
                Log.d("NotificationViewModel", "Fetching call history for user: $currentMahasiswaId")
                // Panggil API getMahasiswaPanggilanHistory
                val response = RetrofitClient.apiService.getMahasiswaPanggilanHistory(currentMahasiswaId)
                _panggilanHistory.value = response.history ?: emptyList()
                Log.d("NotificationViewModel", "Fetched ${response.history?.size ?: 0} call history items.")
            } catch (e: CancellationException) {
                Log.i("NotificationViewModel", "Fetch call history cancelled.")
                throw e
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Failure fetching call history", e)
                _responseMessage.value = "Gagal memuat riwayat panggilan: ${e.message}"
                _panggilanHistory.value = emptyList()
            } finally {
                _isLoadingHistory.value = false
            }
        }
    }

    // Fungsi mahasiswa merespons panggilan
    fun respondToPanggilan(panggilanId: Int, respon: String) {
        if (currentMahasiswaId == -1) {
            _responseMessage.value = "ID Mahasiswa tidak valid."
            return
        }
        if (_isRespondingPanggilanId.value != null) return // Hindari klik ganda
        _isRespondingPanggilanId.value = panggilanId // Tandai sedang proses
        _responseMessage.value = "Mengirim respons..."

        viewModelScope.launch {
            try {
                val requestBody = com.afian.tugasakhir.Model.RespondPanggilanBody( // Gunakan data class dari Model
                    panggilan_id = panggilanId,
                    mahasiswa_user_id = currentMahasiswaId,
                    respon = respon
                )
                Log.d("NotificationViewModel", "Responding to call $panggilanId with '$respon'")
                val response = RetrofitClient.apiService.respondPanggilan(requestBody) // Panggil API

                if (response.isSuccessful && response.body()?.status == true) {
                    _responseMessage.value = response.body()?.message ?: "Respons terkirim"
                    Log.i("NotificationViewModel", "Response success: ${response.body()?.message}")
                    // Muat ulang riwayat untuk update status
                    fetchPanggilanHistory() // Langsung refresh
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal mengirim respons (Error: ${response.code()})"
                    _responseMessage.value = errorMsg
                    Log.e("NotificationViewModel", "Response failed: $errorMsg")
                }

            } catch (e: Exception) {
                _responseMessage.value = "Error: ${e.message}"
                Log.e("NotificationViewModel", "Exception during respond call", e)
            } finally {
                _isRespondingPanggilanId.value = null // Selesai proses
                // Tambahkan delay sebelum hapus pesan?
                // kotlinx.coroutines.delay(3000)
                // _responseMessage.value = null
            }
        }
    }

    // ... (Fungsi stateInViewModel helper jika diperlukan) ...
    private fun <T> Flow<T>.stateInViewModel(initialValue: T): StateFlow<T> =
        this.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), initialValue)
}