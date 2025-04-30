package com.afian.tugasakhir.Controller

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.UpdatePasswordRequest
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import androidx.compose.runtime.State

//// State generik (jika belum ada)
//sealed interface UiState<out T> {
//    object Idle : UiState<Nothing>
//    object Loading : UiState<Nothing>
//    data class Success<T>(val data: T) : UiState<T>
//    data class Error(val message: String) : UiState<Nothing>
//}

class UpdatePasswordViewModel(
    savedStateHandle: SavedStateHandle // Inject SavedStateHandle untuk ambil argumen
) : ViewModel() {
    private val TAG = "UpdatePasswordVM"
    private val apiService = RetrofitClient.apiService

    // Ambil identifier dari argumen navigasi
    val identifier: String = savedStateHandle.get<String>("identifier") ?: ""

    // State untuk input password
    var newPassword = mutableStateOf("")
    var confirmPassword = mutableStateOf("")
    var passwordVisible = mutableStateOf(false)

    // State untuk proses update
    private val _updateState = mutableStateOf<UiState<Unit>>(UiState.Idle)
    val updateState: State<UiState<Unit>> = _updateState

    var validationError = mutableStateOf<String?>(null) // Pesan error validasi

    init {
        Log.d(TAG, "ViewModel initialized for identifier: $identifier")
        if (identifier.isBlank()) {
            _updateState.value = UiState.Error("Identifier pengguna tidak valid.")
        }
    }

    fun updatePassword() {
        val newPass = newPassword.value
        val confirmPass = confirmPassword.value

        // Validasi input
        if (newPass.length < 6) { // Contoh: minimal 6 karakter
            validationError.value = "Password baru minimal 6 karakter."
            return
        }
        if (newPass != confirmPass) {
            validationError.value = "Konfirmasi password tidak cocok."
            return
        }
        // Reset error validasi jika lolos
        validationError.value = null

        if (identifier.isBlank()) {
            _updateState.value = UiState.Error("Identifier pengguna tidak valid.")
            return
        }
        if (_updateState.value is UiState.Loading) return // Hindari klik ganda

        _updateState.value = UiState.Loading
        Log.d(TAG, "Attempting password update for identifier: $identifier")

        viewModelScope.launch {
            try {
                val requestBody = UpdatePasswordRequest(identifier = identifier, newPassword = newPass)
                // Panggil API update password
                val response = apiService.updatePassword(requestBody)

                if (response.isSuccessful && response.body() !=null ) {
                    Log.i(TAG, "Password update successful: ${response.body()?.message}")
                    _updateState.value = UiState.Success(Unit) // Set state sukses
                    // Tidak perlu update SharedPreferences di sini,
                    // karena backend sudah set flag 'update_password' ke true.
                    // Navigasi akan ditangani oleh UI.
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    val errorMsg = try {
                        if (!errorBodyString.isNullOrBlank()) {
                            org.json.JSONObject(errorBodyString).optString("message", "Gagal update (${response.code()})")
                        } else {
                            "Gagal update (${response.code()} - ${response.message()})" // Fallback http
                        }
                    } catch (e: Exception) {
                        "Gagal update (${response.code()}) - Format error tidak dikenal."
                    }
                    Log.w(TAG, "Password update failed: $errorMsg")
                    _updateState.value = UiState.Error(errorMsg)
                }
            } catch (e: CancellationException) {
                Log.i(TAG, "Password update cancelled.")
                _updateState.value = UiState.Idle // Kembali ke Idle jika dibatalkan
            } catch (e: Exception) {
                Log.e(TAG, "Password update exception", e)
                _updateState.value = UiState.Error("Gagal update: ${e.message}")
            }
            // Tidak perlu set loading false, state sudah jadi Success/Error/Idle
        }
    }
}