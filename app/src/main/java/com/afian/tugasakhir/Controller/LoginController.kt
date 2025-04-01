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
import kotlinx.coroutines.launch

class LoginViewModel1 : ViewModel() {
    var loginStatus = mutableStateOf("")

    fun login(username: String, password: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Attempting to login with username: $username")
                val response = RetrofitClient.apiService.login(LoginRequest(username, password))
                Log.d("LoginViewModel", "Response: $response")
                if (response.status) {
                    onSuccess(response.user)
                } else {
                    loginStatus.value = response.message
                }
            } catch (e: Exception) {
                loginStatus.value = "Login failed: ${e.message}"
                Log.e("LoginViewModel", "Error during login: ${e.message}")
            }
        }
    }
}

class LoginViewModel2 : ViewModel() {
    var loginStatus = mutableStateOf("")
    var currentUser = mutableStateOf<User?>(null) // Menyimpan data pengguna saat ini

    fun login(username: String, password: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Attempting to login with username: $username")
                val response = RetrofitClient.apiService.login(LoginRequest(username, password))
                Log.d("LoginViewModel", "Response: $response")
                if (response.status) {
                    currentUser.value = response.user // Simpan data pengguna
                    onSuccess(response.user)
                } else {
                    loginStatus.value = response.message
                }
            } catch (e: Exception) {
                loginStatus.value = "Login failed: ${e.message}"
                Log.e("LoginViewModel", "Error during login: ${e.message}")
            }
        }
    }
}

class LoginViewModel3(private val context: Context) : ViewModel() {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var loginStatus = mutableStateOf("")
    var currentUser = mutableStateOf<User?>(null)

    fun login(username: String, password: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(username, password))
                if (response.status) {
                    currentUser.value = response.user
                    saveLoginStatus(true, response.user) // Simpan status login dan informasi pengguna
                    onSuccess(response.user)
                } else {
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
                user_id = 0, // Anda bisa mengubah ini sesuai kebutuhan
                identifier = sharedPreferences.getString("user_identifier", "") ?: "",
                user_name = sharedPreferences.getString("user_name", "") ?: "",
                role = sharedPreferences.getString("user_role", "") ?: "",
                foto_profile = sharedPreferences.getString("user_photo", "") ?: "",
                update_password = 0 // Anda bisa mengubah ini sesuai kebutuhan
            )
        } else {
            null
        }
    }
}

class LoginViewModel(private val context: Context) : ViewModel() {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var loginStatus = mutableStateOf("")
    var currentUser = mutableStateOf<User?>(null)

    fun login(username: String, password: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(username, password))
                if (response.status) {
                    currentUser.value = response.user
                    saveLoginStatus(true,response.user) // Simpan semua data pengguna
                    onSuccess(response.user)
                } else {
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
    fun logout() {
        sharedPreferences.edit().clear().apply() // Menghapus semua data dari SharedPreferences
        currentUser.value = null // Mengatur currentUser menjadi null
    }

}