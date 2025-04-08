package com.afian.tugasakhir.Service

import android.util.Log
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.RegisterTokenRequest

object FcmRepository {
    private const val TAG = "FcmRepository"

    suspend fun sendTokenToServer(userId: Int, token: String) {
        if (token.isBlank()) {
            Log.w(TAG, "FCM token is blank. Cannot send.")
            return
        }
        try {
            Log.d(TAG, "Sending FCM token for user_id $userId to server...")
            val requestBody = RegisterTokenRequest(userId, token)
            // Asumsi apiService bisa diakses dari RetrofitClient
            val response = RetrofitClient.apiService.registerFcmToken(requestBody)
            if (response.isSuccessful) {
                Log.i(TAG, "FCM Token registered/updated successfully on backend for user_id $userId.")
            } else {
                // Log error dari server jika ada
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.w(TAG, "Failed to register FCM token on backend for user_id $userId. Code: ${response.code()}, Error: $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception sending FCM token to server for user_id $userId: ${e.message}", e)
        }
    }
}