package com.afian.tugasakhir.Model

data class LoginRequest(
    val identifier: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val status: Boolean,
    val user: User
)

data class User(
    val user_id: Int,
    val identifier: String,
    val user_name: String,
    val role: String,
    val foto_profile: String,
    val update_password: Int
)