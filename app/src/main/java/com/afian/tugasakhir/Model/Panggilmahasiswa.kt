package com.afian.tugasakhir.Model

// Untuk response GET /api/users/mahasiswa
data class MahasiswaListResponse(
    val count: Int,
    // Gunakan data class Dosen atau buat MahasiswaSimple jika field berbeda
    val mahasiswa: List<Dosen> // Asumsi field sama dgn Dosen untuk simple display
    // Jika field beda, buat data class MahasiswaSimple(...)
)

// Untuk request body POST /api/panggilan/request
data class RequestPanggilanBody(
    val dosen_user_id: Int,
    val mahasiswa_user_id: Int
)

// Untuk response body POST /api/panggilan/request (opsional, sesuaikan dgn backend)
data class RequestPanggilanResponse(
    val status: Boolean,
    val message: String,
    val panggilan_id: Int?
)

// Untuk request body PUT /api/panggilan/respond
data class RespondPanggilanBody(
    val panggilan_id: Int,
    val mahasiswa_user_id: Int,
    val respon: String // "accepted" atau "declined"
)

// Untuk response body PUT /api/panggilan/respond (opsional)
data class RespondPanggilanResponse(
    val status: Boolean,
    val message: String
)

// Model untuk item riwayat panggilan Dosen
data class PanggilanHistoryDosenItem(
    val panggilan_id: Int,
    val mahasiswa_user_id: Int,
    val mahasiswa_name: String?,
    val mahasiswa_foto: String?,
    val waktu_panggil: String, // Terima sebagai String, format nanti di UI
    val status: String, // "pending", "accepted", "declined"
    val waktu_respon: String?
)

// Model untuk response riwayat panggilan Dosen
data class DosenHistoryResponse(
    val history: List<PanggilanHistoryDosenItem>
)

// Model untuk item riwayat panggilan Mahasiswa
data class PanggilanHistoryMahasiswaItem(
    val panggilan_id: Int,
    val dosen_user_id: Int,
    val dosen_name: String?,
    val dosen_foto: String?,
    val waktu_panggil: String,
    val status: String,
    val waktu_respon: String?
)

// Model untuk response riwayat panggilan Mahasiswa
data class MahasiswaHistoryResponse(
    val history: List<PanggilanHistoryMahasiswaItem>
)