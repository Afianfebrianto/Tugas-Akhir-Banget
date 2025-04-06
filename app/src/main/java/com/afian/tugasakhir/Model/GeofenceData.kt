package com.afian.tugasakhir.Model

// Data class untuk request & response (buat file terpisah atau di bawah interface)
data class AddLocationRequest(
    val user_id: Int,
    val tanggal: String, // Format: "YYYY-MM-DD"
    val jam_masuk: String // Format: "YYYY-MM-DDTHH:mm:ss"
)

data class AddLocationResponse(
    val id_lokasi: Int?, // Jadikan nullable untuk keamanan
    val status: Boolean,
    val message: String
)

data class UpdateLocationRequest(
    val id_lokasi: Int,
    val user_id: Int,
    val jam_keluar: String // Format: "YYYY-MM-DDTHH:mm:ss"
)

data class UpdateLocationResponse(
    val status: Boolean,
    val message: String
)