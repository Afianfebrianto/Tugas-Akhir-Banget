package com.afian.tugasakhir.Controller

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.LoginRequest
import com.afian.tugasakhir.Model.User
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
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