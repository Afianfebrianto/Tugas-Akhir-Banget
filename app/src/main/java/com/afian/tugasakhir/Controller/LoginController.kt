package com.afian.tugasakhir.Controller

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.LoginRequest
import com.afian.tugasakhir.Model.User
import com.afian.tugasakhir.Service.FcmRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


// Enum atau Sealed Class untuk representasi state login yg lebih jelas (opsional tapi bagus)
sealed class LoginUiState {
    object Idle : LoginUiState() // Kondisi awal, form ditampilkan
    object Loading : LoginUiState() // Sedang proses login
    data class Success(val user: User) : LoginUiState() // Login berhasil
    data class Error(val message: String) : LoginUiState() // Login gagal
}


class LoginViewModel(private val context: Context) : ViewModel() {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Gunakan delegasi untuk akses lebih mudah di Composable
    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()



    var currentUser by mutableStateOf<User?>(null)
        private set // Hanya bisa diubah dari dalam ViewModel

    init {
        // Muat data user saat init jika perlu (misal untuk cek auto login)
        currentUser = getUserData()
        // _loginUiState.value = if (isLoggedIn()) LoginUiState.Success(currentUser!!) else LoginUiState.Idle // Opsional: set state awal jika sudah login?
    }


    fun login(username: String, password: String) { // Hapus parameter onSuccess
        // Hindari login ganda jika sedang loading
        if (_loginUiState.value is LoginUiState.Loading) {
            Log.w("LoginViewModel", "Login attempt ignored, already loading.")
            return
        }

        _loginUiState.value = LoginUiState.Loading // 1. Set state Loading
        Log.d("LoginViewModel", "Attempting login...")

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(username, password))

                if (response.status && response.user != null) { // Pastikan user tidak null
                    val loggedInUser = response.user
                    Log.i("LoginViewModel", "Login API successful for ${loggedInUser.user_name}")
                    currentUser = loggedInUser // Tetap update currentUser jika perlu
                    saveLoginStatus(true, loggedInUser)

                    // Daftarkan FCM Token (tetap sama)
                    if (loggedInUser.user_id != -1 && loggedInUser.user_id != 0) {
                        registerAndSendFcmToken(loggedInUser.user_id)
                        // Tidak perlu delay di sini, biarkan UI yg delay navigasi
                    } else {
                        Log.w("LoginViewModel", "Invalid user_id (${loggedInUser.user_id}). Cannot register FCM token.")
                    }

                    // 2a. Set state Sukses dengan data user
                    _loginUiState.value = LoginUiState.Success(loggedInUser)

                } else {
                    // 2b. Set state Error dengan pesan dari API
                    val errorMsg = response.message ?: "Username atau password salah"
                    Log.w("LoginViewModel", "Login API failed: $errorMsg")
                    _loginUiState.value = LoginUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                // 2c. Set state Error dengan pesan exception
                val errorMsg = "Gagal terhubung: ${e.message}"
                Log.e("LoginViewModel", "Login exception", e)
                _loginUiState.value = LoginUiState.Error(errorMsg)
            }
            // Tidak perlu set loading false, karena state sudah jadi Success atau Error
        }
    }

    // --- 👇 Fungsi BARU untuk reset state dari UI 👇 ---
    /** Dipanggil dari UI (misal setelah animasi error) untuk kembali ke form login */
    fun resetLoginStateToIdle() {
        // Hanya reset jika state saat ini bukan Idle atau Loading
        if (_loginUiState.value !is LoginUiState.Idle && _loginUiState.value !is LoginUiState.Loading) {
            _loginUiState.value = LoginUiState.Idle
            Log.d("LoginViewModel", "Login state reset to Idle.")
        }
    }
    // --- 👆 ---

    private fun saveLoginStatus(isLoggedIn: Boolean, user: User) {
        with(sharedPreferences.edit()) {
            putBoolean("is_logged_in", isLoggedIn)
            putString("user_role", user.role)
            putString("user_name", user.user_name)
            putString("user_identifier", user.identifier)
            putString("user_photo", user.foto_profile)
            putInt("user_id", user.user_id) // Simpan user_id jika diperlukan
            putInt("update_password", user.update_password)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    fun getUserRole(): String? {
        return sharedPreferences.getString("user_role", null)
    }

    fun getUserData(): User? {
        return if (isLoggedIn()) {
            User(
                user_id = sharedPreferences.getInt("user_id", 0),
                identifier = sharedPreferences.getString("user_identifier", "") ?: "",
                user_name = sharedPreferences.getString("user_name", "") ?: "",
                role = sharedPreferences.getString("role", "") ?: "",
                foto_profile = sharedPreferences.getString("user_photo", "") ?: "",
                update_password = sharedPreferences.getInt("update_password", 0)
            )
        } else {
            null
        }
    }

    // --- 👇 FUNGSI BARU UNTUK TOKEN 👇 ---
    /**
     * Mendapatkan token FCM saat ini dan mengirimkannya ke server jika user_id valid.
     * Dilakukan di background thread.
     */
    private fun registerAndSendFcmToken(userId: Int) {
        // Jalankan di Dispatchers.IO karena ada network call (await() dan sendTokenToServer)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Dapatkan token FCM menggunakan await() dari kotlinx-coroutines-play-services
                val token = FirebaseMessaging.getInstance().token.await()
                Log.i("LoginViewModel", "FCM Token retrieved: $token")
                // Kirim ke server menggunakan repository
                FcmRepository.sendTokenToServer(userId, token)
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error getting or sending FCM token: ${e.message}", e)
                // Handle error jika perlu (misal: coba lagi nanti?)
            }
        }
    }
    fun logout() {
        Log.d("LoginViewModel", "Logging out user...")
        sharedPreferences.edit().clear().apply() // Menghapus semua data dari SharedPreferences
        currentUser = null // Mengatur currentUser menjadi null
        Log.d("LoginViewModel", "User logged out, currentUser set to null.")
    }

}