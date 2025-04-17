package com.afian.tugasakhir.Model

// Response untuk get all users
data class AllUsersResponse(
    val status: Boolean,
    val users: List<User>? // List user, gunakan model User yang ada
)

// Response sederhana untuk sukses/gagal
data class SimpleStatusResponse(
    val status: Boolean,
    val message: String
)

// Data class untuk hasil recovery
data class RecoveryResult(
    val userId: Int,
    val success: Boolean,
    val message: String
)
