package com.afian.tugasakhir.Model

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.ApiService
import com.afian.tugasakhir.API.RetrofitClient
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

// Nama ViewModel bisa diubah jika ingin lebih spesifik, misal MahasiswaPanggilanViewModel
class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    // Dapatkan instance ApiService
    private val apiService: ApiService = RetrofitClient.apiService

    // Simpan ID Mahasiswa yang login
    private var currentMahasiswaId: Int = -1

    // --- State untuk Riwayat PANGGILAN Dosen ---
    // StateFlow Internal (private)
    private val _panggilanHistory = MutableStateFlow<List<PanggilanHistoryMahasiswaItem>>(emptyList())
    // StateFlow Publik (read-only) untuk di-observe oleh UI
    val panggilanHistory: StateFlow<List<PanggilanHistoryMahasiswaItem>> = _panggilanHistory.asStateFlow()

    // State untuk status loading saat fetch riwayat
    private val _isLoadingHistory = mutableStateOf(false)
    val isLoadingHistory: State<Boolean> = _isLoadingHistory

    // State untuk menampilkan pesan hasil (sukses/gagal) dari aksi respond atau fetch error
    private val _responseMessage = mutableStateOf<String?>(null)
    val responseMessage: State<String?> = _responseMessage

    // State untuk menandai ID panggilan mana yang sedang diproses responsnya (untuk loading tombol)
    private val _isRespondingPanggilanId = mutableStateOf<Int?>(null)
    val isRespondingPanggilanId: State<Int?> = _isRespondingPanggilanId
    // --- Akhir State Panggilan ---


    // --- HAPUS State & DAO untuk Notifikasi Umum (jika tidak dipakai lagi) ---
    // private val notificationDao: NotificationDao = AppDatabase.getDatabase(application).notificationDao()
    // val notificationHistory: StateFlow<List<NotificationHistoryItem>> = ...
    // -------------------------------------------------------------------------

    init {
        // Ambil ID Mahasiswa dari SharedPreferences saat ViewModel dibuat
        val userPrefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val fetchedUserId = userPrefs.getInt("user_id", -1)
        val userRole = userPrefs.getString("user_role", null)

        // Hanya fetch riwayat jika user adalah mahasiswa yang valid
        if (fetchedUserId != -1 && userRole == "mhs") {
            currentMahasiswaId = fetchedUserId
            Log.d("NotificationViewModel", "ViewModel initialized for student ID: $currentMahasiswaId. Fetching history...")
            fetchPanggilanHistory() // Langsung panggil fetch riwayat
        } else {
            Log.w("NotificationViewModel", "ViewModel initialized, but cannot fetch history. UserID: $fetchedUserId, Role: $userRole")
            _responseMessage.value = "Gagal memuat riwayat (User tidak valid?)." // Beri pesan error awal jika perlu
        }
    }

    
    fun fetchPanggilanHistory() {
        // Cek ID Mahasiswa lagi untuk keamanan
        if (currentMahasiswaId == -1) {
            _responseMessage.value = "Gagal refresh: ID Mahasiswa tidak ditemukan."
            return
        }
        if (_isLoadingHistory.value) return // Jangan fetch jika sedang loading
        _isLoadingHistory.value = true
        _responseMessage.value = null // Hapus pesan lama
        Log.d("NotificationViewModel", "Fetching call history for user: $currentMahasiswaId")

        viewModelScope.launch {
            try {
                // Panggil endpoint API getMahasiswaPanggilanHistory
                val response = apiService.getMahasiswaPanggilanHistory(currentMahasiswaId)
                _panggilanHistory.value = response.history ?: emptyList() // Update state flow riwayat
                Log.d("NotificationViewModel", "Fetched ${response.history?.size ?: 0} call history items.")
            } catch (e: CancellationException) {
                Log.i("NotificationViewModel", "Fetch call history cancelled.")
                // Tidak set error untuk cancellation
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Failure fetching call history", e)
                _responseMessage.value = "Gagal memuat riwayat: ${e.message ?: "Error tidak diketahui"}"
                _panggilanHistory.value = emptyList() // Kosongkan list jika error
            } finally {
                _isLoadingHistory.value = false // Set loading false setelah selesai/gagal
            }
        }
    }

    
    fun respondToPanggilan(panggilanId: Int, respon: String) {
        if (currentMahasiswaId == -1) {
            _responseMessage.value = "Gagal merespon: ID Mahasiswa tidak valid."
            return
        }
        if (_isRespondingPanggilanId.value != null) { // Cek jika sedang merespon panggilan lain
            Log.w("NotificationViewModel","Sedang memproses respons lain untuk panggilan ID: ${_isRespondingPanggilanId.value}")
            // Beri feedback ke user mungkin?
            // _responseMessage.value = "Harap tunggu proses sebelumnya selesai."
            return
        }
        // Validasi input 'respon'
        if (respon != "accepted" && respon != "declined") {
            Log.e("NotificationViewModel", "Invalid response value provided: $respon")
            _responseMessage.value = "Tindakan tidak valid."
            return
        }

        _isRespondingPanggilanId.value = panggilanId // Tandai panggilan ini sedang diproses
        _responseMessage.value = "Mengirim respons..." // Pesan loading

        viewModelScope.launch {
            var shouldRefresh = false // Flag untuk menandai perlu refresh atau tidak
            try {
                val requestBody = RespondPanggilanBody(
                    panggilan_id = panggilanId,
                    mahasiswa_user_id = currentMahasiswaId,
                    respon = respon
                )
                Log.d("NotificationViewModel", "Responding to call $panggilanId with '$respon'")
                // Panggil endpoint API respondPanggilan
                val response = apiService.respondPanggilan(requestBody)

                if (response.isSuccessful && response.body()?.status == true) {
                    // Sukses mengirim respons
                    _responseMessage.value = response.body()?.message ?: "Respons berhasil dikirim"
                    Log.i("NotificationViewModel", "Response success: ${response.body()?.message}")
                    shouldRefresh = true // Data di server berubah, perlu refresh list
                } else {
                    // Gagal mengirim respons (error dari API)
                    val errorMsg = response.body()?.message ?: "Gagal mengirim respons (Error Code: ${response.code()})"
                    _responseMessage.value = errorMsg
                    Log.e("NotificationViewModel", "Response failed: $errorMsg - Body: ${response.errorBody()?.string()}")
                }

            } catch (e: CancellationException) {
                Log.i("NotificationViewModel", "Respond call cancelled.")
                _responseMessage.value = "Proses dibatalkan." // Beri pesan jika perlu
                // Tidak perlu refresh jika dibatalkan
            } catch (e: Exception) {
                // Error jaringan atau lainnya
                _responseMessage.value = "Gagal mengirim respons: ${e.message ?: "Error jaringan"}"
                Log.e("NotificationViewModel", "Exception during respond call", e)
            } finally {
                _isRespondingPanggilanId.value = null // Tandai proses selesai (baik sukses/gagal/cancel)
                if (shouldRefresh) {
                    // Jika sukses, panggil fetch untuk update data di layar
                    fetchPanggilanHistory()
                }
                // Pertimbangkan untuk clear pesan setelah beberapa detik
                // kotlinx.coroutines.delay(3000)
                // _responseMessage.value = null
            }
        }
    }

    // --- Hapus fungsi clearHistory() jika tidak dipakai lagi ---
    // fun clearHistory() { ... }

    // --- Helper extension function (opsional, tapi membantu) ---
    private fun <T> Flow<T>.stateInViewModel(initialValue: T): StateFlow<T> =
        this.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), initialValue)

}