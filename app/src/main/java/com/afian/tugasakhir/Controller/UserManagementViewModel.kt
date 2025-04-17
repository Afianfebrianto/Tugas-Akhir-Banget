package com.afian.tugasakhir.Controller

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.RecoveryResult
import com.afian.tugasakhir.Model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import kotlin.coroutines.cancellation.CancellationException

class UserManagementViewModel : ViewModel() {
    private val TAG = "UserManagementVM"
    private val apiService = RetrofitClient.apiService

    // State untuk daftar semua user
    private val _allUserList = MutableStateFlow<List<User>>(emptyList())
    val allUserList: StateFlow<List<User>> = _allUserList.asStateFlow()

    // State untuk filter/pencarian (jika mau ditambahkan)
    // private val _searchQuery = MutableStateFlow("")
    // val filteredUserList: StateFlow<List<User>> = combine(...)

    // State loading dan error untuk list user
    private val _isLoadingUsers = mutableStateOf(false)
    val isLoadingUsers: State<Boolean> = _isLoadingUsers
    private val _errorUsers = mutableStateOf<String?>(null)
    val errorUsers: State<String?> = _errorUsers

    // State untuk proses recovery
    private val _recoveringUserId = mutableStateOf<Int?>(null) // ID user yg sedang direcover
    val recoveringUserId: State<Int?> = _recoveringUserId
    private val _recoveryResult = mutableStateOf<RecoveryResult?>(null) // Hasil recovery terakhir
    val recoveryResult: State<RecoveryResult?> = _recoveryResult

    init {
        fetchAllUsers() // Muat daftar user saat init
    }

    fun fetchAllUsers() {
        if (_isLoadingUsers.value) return
        _isLoadingUsers.value = true
        _errorUsers.value = null
        Log.d(TAG, "Fetching all users...")

        viewModelScope.launch {
            try {
                // TODO: Tambahkan header otentikasi Admin jika diperlukan
                val response = apiService.getAllUsers()
                if (response.status) {
                    _allUserList.value = response.users ?: emptyList()
                    Log.d(TAG, "Fetched ${response.users?.size ?: 0} users.")
                } else {
                    throw Exception("API returned status false fetching users.")
                }
                _errorUsers.value = null // Hapus error jika sukses
            } catch (e: CancellationException) {
                Log.i(TAG, "Fetch all users cancelled.")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching all users", e)
                _errorUsers.value = "Gagal memuat daftar pengguna: ${e.message}"
                _allUserList.value = emptyList()
            } finally {
                _isLoadingUsers.value = false
            }
        }
    }

    fun recoverUserPassword(userId: Int) {
        if (_recoveringUserId.value != null) {
            Log.w(TAG, "Recovery already in progress for user ${_recoveringUserId.value}")
            return // Hindari klik ganda
        }
        _recoveringUserId.value = userId // Tandai user ini sedang diproses
        _recoveryResult.value = null // Hapus hasil lama
        Log.d(TAG, "Attempting password recovery for user ID: $userId")

        viewModelScope.launch {
            var success = false
            var message = "Gagal melakukan recovery." // Default message
            try {
                // TODO: Tambahkan header otentikasi Admin jika diperlukan
                val response = apiService.recoverUserPassword(userId)

                if (response.isSuccessful && response.body()?.status == true) {
                    success = true
                    message = response.body()?.message ?: "Password berhasil direset."
                    Log.i(TAG, "Password recovery success for user $userId: $message")
                    // Opsional: refresh daftar user jika ada perubahan (misal flag update_password)
                    // fetchAllUsers()
                } else {
                    message = response.body()?.message ?: "Recovery gagal (Error: ${response.code()})"
                    Log.e(TAG, "Password recovery failed for user $userId: $message")
                }

            } catch (e: CancellationException){
                Log.i(TAG, "Password recovery cancelled for user $userId.")
                message = "Proses dibatalkan." // Atau jangan tampilkan pesan
            } catch (e: Exception) {
                Log.e(TAG, "Exception during password recovery for user $userId", e)
                message = "Error: ${e.message}"
            } finally {
                // Simpan hasil untuk ditampilkan di UI
                _recoveryResult.value = RecoveryResult(userId, success, message)
                _recoveringUserId.value = null // Tandai proses selesai
                // Hapus pesan setelah delay?
                // kotlinx.coroutines.delay(4000)
                // _recoveryResult.value = null
            }
        }
    }

    // Fungsi untuk clear pesan hasil recovery (dipanggil dari UI setelah Toast/Snackbar)
    fun clearRecoveryResult() {
        _recoveryResult.value = null
    }

    // Fungsi helper stateIn (jika pakai combine untuk filter)
    // private fun <T> Flow<T>.stateInViewModel(...) { ... }
}