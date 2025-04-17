package com.afian.tugasakhir.Model

// Data class untuk satu item dalam peringkat
data class PeringkatItem(
    val user_id: Int,
    val user_name: String,
    val foto_profile: String?,
    val total_jam: Double // Terima sebagai Double
)

// Data class untuk response API peringkat
data class PeringkatResponse(
    val status: Boolean,
    val message: String,
    val bulan: Int?,
    val tahun: Int?,
    val peringkat: List<PeringkatItem>? // List bisa null dari API
)