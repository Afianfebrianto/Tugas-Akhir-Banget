package com.afian.tugasakhir.Model

// Data Class untuk response GET /api/users/profile/:user_id
data class UserProfileResponse(
    val status: Boolean,
    val profile: UserProfileData? // Buat data class gabungan
)

// Data Class gabungan untuk profil
data class UserProfileData(
    val user_id: Int,
    val identifier: String,
    val user_name: String,
    val role: String,
    val foto_profile: String?,
    val no_hp: String?,
    val informasi: String?
    // Tambahkan field lain jika perlu
)

// Data Class untuk response PUT /api/users/update-profile/:identifier
data class UpdateProfileResponse(
    val status: Boolean,
    val message: String,
    val updated_photo_url: String? // Opsional: URL foto baru jika berubah
)