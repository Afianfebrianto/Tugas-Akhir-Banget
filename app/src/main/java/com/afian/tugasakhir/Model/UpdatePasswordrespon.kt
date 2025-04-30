package com.afian.tugasakhir.Model

// Data class untuk request body update password
data class UpdatePasswordRequest(
    val identifier: String,
    val newPassword: String
)