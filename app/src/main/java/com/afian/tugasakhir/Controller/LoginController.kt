package com.afian.tugasakhir.Controller

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.ApiService
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.LoginRequest
import com.afian.tugasakhir.Model.UpdateLocationRequest
import com.afian.tugasakhir.Model.User
import com.afian.tugasakhir.Model.UserProfileData
import com.afian.tugasakhir.Service.FcmRepository
import com.afian.tugasakhir.Service.LocationPrefsKeys
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


// Enum atau Sealed Class untuk representasi state login yg lebih jelas (opsional tapi bagus)
sealed class LoginUiState {
    object Idle : LoginUiState() // Kondisi awal, form ditampilkan
    object Loading : LoginUiState() // Sedang proses login
    data class Success(val user: User) : LoginUiState() // Login berhasil
    data class PasswordUpdateRequired(val user: User) : LoginUiState() // Login sukses TAPI password perlu diupdate
    data class Error(val message: String) : LoginUiState() // Login gagal
}

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}


class LoginViewModel(private val context: Context) : ViewModel() {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val TAG = "LoginViewModel"
    // Gunakan delegasi untuk akses lebih mudah di Composable
    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    // --- 👇 State BARU untuk proses LOGOUT 👇 ---
    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle) // Tipe data sukses bisa Unit
    val logoutState: StateFlow<UiState<Unit>> = _logoutState.asStateFlow()
    // --- 👆 ---
    private val locationPrefs: SharedPreferences = context.getSharedPreferences(
        LocationPrefsKeys.PREFS_NAME,
        Context.MODE_PRIVATE
    )
    // Dapatkan instance ApiService
    private val apiService: ApiService = RetrofitClient.apiService

    var currentUser by mutableStateOf<User?>(null)
        private set // Hanya bisa diubah dari dalam ViewModel

    init {
        // Muat data user saat init jika perlu (misal untuk cek auto login)
        currentUser = getUserData()
        Log.d(TAG, "init: currentUser set to $currentUser")
        // _loginUiState.value = if (isLoggedIn()) LoginUiState.Success(currentUser!!) else LoginUiState.Idle // Opsional: set state awal jika sudah login?
    }


    fun login(username: String, password: String) { // Hapus parameter onSuccess
        // Hindari login ganda jika sedang loading
        if (_loginUiState.value is LoginUiState.Loading) {
            Log.w(TAG, "Login attempt ignored, already loading.")
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
                    }
                    // === 👇 CEK STATUS UPDATE PASSWORD 👇 ===
                    if (loggedInUser.update_password == 0) { // Asumsi 0 = false = perlu update
                        Log.i("LoginViewModel", "Password update required for user.")
                        _loginUiState.value = LoginUiState.PasswordUpdateRequired(loggedInUser) // State baru
                    } else {
                        _loginUiState.value = LoginUiState.Success(loggedInUser)
                        Log.w("LoginViewModel", "Invalid user_id (${loggedInUser.user_id}). Cannot register FCM token.")
                    }

                    // 2a. Set state Sukses dengan data user
//                    _loginUiState.value = LoginUiState.Success(loggedInUser)

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

    // Fungsi ini mungkin perlu dipanggil setelah password berhasil diupdate
    fun markPasswordAsActionRequired(isRequired: Boolean) {
        val user = currentUser ?: return // Butuh user data saat ini
        val updatedUser = user.copy(update_password = if(isRequired) 0 else 1)
        saveLoginStatus(true, updatedUser) // Update SharedPreferences
        currentUser = updatedUser // Update state ViewModel
        Log.d("LoginViewModel", "Password update status manually set to: ${!isRequired}")
    }


    // --- 👇 Fungsi BARU untuk reset state dari UI 👇 ---
    /** Dipanggil dari UI (misal setelah animasi error) untuk kembali ke form login */
    fun resetLoginStateToIdle() {
        // Hanya reset jika state saat ini bukan Idle atau Loading
        if (_loginUiState.value !is LoginUiState.Idle && _loginUiState.value !is LoginUiState.Loading) {
            _loginUiState.value = LoginUiState.Idle
            Log.d("LoginViewModel", "Login state reset to Idle.")
        } else {
            Log.d(TAG, "resetLoginStateToIdle: Already Idle.")
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
            putString("no_hp", user.no_hp)
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
                update_password = sharedPreferences.getInt("update_password", 0),
                no_hp = sharedPreferences.getString("no_hp","") ?: ""
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
        if (_logoutState.value is UiState.Loading) return
        _logoutState.value = UiState.Loading
        Log.d("LoginViewModel", "Logging out user...")
        viewModelScope.launch(Dispatchers.IO) {
            val geofenceUpdateSuccess = handleLogoutGeofenceUpdate()

            // 2. Hanya lanjutkan proses logout jika update geofence berhasil (atau tidak diperlukan)
            if (geofenceUpdateSuccess) {
                Log.d(TAG, "Geofence check complete. Proceeding to clear user session.")
                // 3. Hapus data sesi pengguna dari SharedPreferences
                sharedPreferences.edit().clear() .remove(LocationPrefsKeys.KEY_ACTIVE_GEOFENCE_COUNT).apply()

                // 4. Update state di Main thread
                withContext(Dispatchers.Main) {
                    currentUser = null
                    _logoutState.value = UiState.Success(Unit)
                    _loginUiState.value = LoginUiState.Idle
                    Log.i(TAG, "User logged out successfully. State set to Success.")

                    // Reset state setelah jeda singkat
                    viewModelScope.launch {
                        delay(500L)
                        resetLogoutStateToIdle()
                    }
                }
            } else {
                // Jika update geofence GAGAL, batalkan proses logout dan tampilkan error
                Log.e(TAG, "Logout process aborted due to geofence update failure.")
                withContext(Dispatchers.Main) {
                    _logoutState.value = UiState.Error("Gagal mengakhiri sesi lokasi. Silakan coba lagi.")
                }
            }
        }
    }

    fun updateLocalUserData(updatedProfile: UserProfileData) {
        val currentUserData = getUserData()
        if (currentUserData != null) {
            // Buat objek User baru dengan data yang diperbarui
            val newlyUpdatedUser = currentUserData.copy(
                no_hp = updatedProfile.no_hp.toString(), // Ambil dari updatedProfile
                foto_profile = updatedProfile.foto_profile.toString() // Ambil dari updatedProfile
            )
            // Simpan kembali ke SharedPreferences menggunakan fungsi yang sudah ada
            saveLoginStatus(true, newlyUpdatedUser)
            Log.i(TAG, "Local user data updated in SharedPreferences: $newlyUpdatedUser")
        } else {
            Log.e(TAG, "Cannot update local user data: No user currently logged in according to SharedPreferences.")
            // Mungkin perlu tindakan lain? Misal logout paksa?
        }
    }

    // Fungsi ini tetap ada, bisa dipanggil UI jika perlu reset manual
    fun resetLogoutStateToIdle() {
        if (_logoutState.value !is UiState.Idle) {
            _logoutState.value = UiState.Idle
            Log.d("LoginViewModel", "Logout state reset to Idle by UI.")
        }
    }

    private suspend fun handleLogoutGeofenceUpdate(): Boolean {
        // 1. Ambil id_lokasi dan user_id
        val lokasiId = locationPrefs.getInt(LocationPrefsKeys.KEY_ID_LOKASI, LocationPrefsKeys.INVALID_LOKASI_ID)
        val userId = sharedPreferences.getInt("user_id", -1)

        // 2. Cek jika ada sesi lokasi yang aktif
        if (lokasiId != LocationPrefsKeys.INVALID_LOKASI_ID && userId != -1) {
            Log.d(TAG, "Active geofence session found (lokasiId: $lokasiId). Updating exit time before logout.")

            // 3. Siapkan dan panggil API updateLocation
            return try {
                val now = LocalDateTime.now()
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val jamKeluarStr = now.format(dateTimeFormatter)
                val requestBody = UpdateLocationRequest(lokasiId, userId, jamKeluarStr)

                Log.d(TAG, "Calling updateLocation API with: $requestBody")
                val response = apiService.updateLocation(requestBody)

                if (response.status) {
                    Log.i(TAG, "Successfully updated exit time for lokasiId: $lokasiId.")
                    // 4. Hapus id_lokasi dari SharedPreferences setelah berhasil
                    locationPrefs.edit()
                        .remove(LocationPrefsKeys.KEY_ID_LOKASI)
                        .remove(LocationPrefsKeys.KEY_ACTIVE_GEOFENCE_COUNT)
                        .apply()
                    true // Operasi berhasil
                } else {
                    Log.e(TAG, "Failed to update exit time. API Msg: ${response.message}")
                    false // Terjadi error di API
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during exit time update on logout.", e)
                false // Terjadi error koneksi/exception
            }
        } else {
            // Jika tidak ada sesi lokasi aktif, anggap proses berhasil dan lanjutkan logout
            Log.d(TAG, "No active geofence session found. Skipping exit time update.")
            return true
        }
    }

}