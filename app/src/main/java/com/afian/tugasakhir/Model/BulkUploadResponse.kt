package com.afian.tugasakhir.Model

// Data class untuk response dari API bulk upload (sesuaikan dengan backend Anda)
data class BulkUploadResponse(
    val status: Boolean,
    val message: String,
    val errors: List<String>? // Daftar error per baris (nullable)
)

// Data class untuk menampung hasil upload di UI state
data class UploadResult(
    val success: Boolean,
    val message: String,
    val errors: List<String>? = null
)
