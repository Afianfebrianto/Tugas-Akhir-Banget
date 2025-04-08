package com.afian.tugasakhir.Controller

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.LoginRequest
import com.afian.tugasakhir.Model.User
import com.afian.tugasakhir.Service.FcmRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(private val context: Context) : ViewModel() {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var loginStatus = mutableStateOf("")
    var currentUser = mutableStateOf<User?>(null)

    fun login(username: String, password: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(username, password))
                val loggedInUser = response.user
                if (response.status) {
                    currentUser.value = response.user
                    saveLoginStatus(true,response.user) // Simpan semua data pengguna
                    onSuccess(response.user)
                }
                if (loggedInUser.role == "mhs") {
                    Log.d("LoginViewModel", "User is a student. Attempting to register FCM token...")
                    registerAndSendFcmToken(loggedInUser.user_id) // Panggil fungsi pendaftaran token
                }
                else {
                    loginStatus.value = response.message
                }
            } catch (e: Exception) {
                loginStatus.value = "Login failed: ${e.message}"
            }
        }
    }

    private fun saveLoginStatus(isLoggedIn: Boolean, user: User) {
        with(sharedPreferences.edit()) {
            putBoolean("is_logged_in", isLoggedIn)
            putString("user_role", user.role)
            putString("user_name", user.user_name)
            putString("user_identifier", user.identifier)
            putString("user_photo", user.foto_profile)
            putInt("user_id", user.user_id) // Simpan user_id jika diperlukan
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
    // --- 👆 AKHIR FUNGSI BARU 👆 ---
    fun logout() {
        sharedPreferences.edit().clear().apply() // Menghapus semua data dari SharedPreferences
        currentUser.value = null // Mengatur currentUser menjadi null
    }

}