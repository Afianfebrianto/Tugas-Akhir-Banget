package com.afian.tugasakhir.Model

data class DosenNotInCampusResponse(
    val message: String,
    val dosen_not_in_campus: List<Dosen> // Menggunakan data class Dosen yang sudah ada
)