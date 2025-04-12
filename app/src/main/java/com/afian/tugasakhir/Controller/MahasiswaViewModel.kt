package com.afian.tugasakhir.Controller

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.PanggilanHistoryMahasiswaItem
import com.afian.tugasakhir.Model.RespondPanggilanBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MahasiswaViewModel : ViewModel() { // Atau nama lain yang sesuai

    // State untuk riwayat panggilan yang diterima mahasiswa
    private val _panggilanHistory = MutableStateFlow<List<PanggilanHistoryMahasiswaItem>>(emptyList())
    val panggilanHistory: StateFlow<List<PanggilanHistoryMahasiswaItem>> = _panggilanHistory.asStateFlow()

    // State loading/error untuk riwayat & respons
    val isLoadingHistory = mutableStateOf(false)
    val responseMessage = mutableStateOf<String?>(null)
    val isResponding = mutableStateOf<Int?>(null) // ID panggilan yg sedang direspon

    // Panggil fetch history saat ViewModel dibuat (perlu user ID mahasiswa)
    // Cara mendapatkan user ID bisa dari SharedPreferences atau parameter constructor
    // init { fetchPanggilanHistory(mahasiswaUserId) }

    fun fetchPanggilanHistory(mahasiswaUserId: Int) {
        if (isLoadingHistory.value) return
        isLoadingHistory.value = true
        responseMessage.value = null
        viewModelScope.launch {
            try {
                Log.d("MahasiswaViewModel", "Fetching call history for user: $mahasiswaUserId")
                val response = RetrofitClient.apiService.getMahasiswaPanggilanHistory(mahasiswaUserId)
                _panggilanHistory.value = response.history ?: emptyList()
                Log.d("MahasiswaViewModel", "Fetched ${response.history?.size ?: 0} history items.")
            } catch (e: Exception) {
                Log.e("MahasiswaViewModel", "Failure fetching call history", e)
                responseMessage.value = "Gagal memuat riwayat: ${e.message}"
                _panggilanHistory.value = emptyList()
            } finally {
                isLoadingHistory.value = false
            }
        }
    }

    fun respondToPanggilan(panggilanId: Int, mahasiswaUserId: Int, respon: String) {
        if (isResponding.value != null) return // Hindari klik ganda
        isResponding.value = panggilanId
        responseMessage.value = "Mengirim respons..."

        viewModelScope.launch {
            try {
                val requestBody = RespondPanggilanBody(
                    panggilan_id = panggilanId,
                    mahasiswa_user_id = mahasiswaUserId,
                    respon = respon // "accepted" atau "declined"
                )
                Log.d("MahasiswaViewModel", "Responding to call $panggilanId with '$respon'")
                val response = RetrofitClient.apiService.respondPanggilan(requestBody)

                if (response.isSuccessful && response.body()?.status == true) {
                    responseMessage.value = response.body()?.message ?: "Respons terkirim"
                    Log.i("MahasiswaViewModel", "Response success: ${response.body()?.message}")
                    // Muat ulang riwayat untuk update status
                    fetchPanggilanHistory(mahasiswaUserId)
                } else {
                    val errorMsg = response.body()?.message ?: "Gagal mengirim respons (Error: ${response.code()})"
                    responseMessage.value = errorMsg
                    Log.e("MahasiswaViewModel", "Response failed: $errorMsg")
                }

            } catch (e: Exception) {
                responseMessage.value = "Error: ${e.message}"
                Log.e("MahasiswaViewModel", "Exception during respond call", e)
            } finally {
                isResponding.value = null
                // kotlinx.coroutines.delay(3000)
                // responseMessage.value = null // Hapus pesan setelah delay?
            }
        }
    }
}