package com.afian.tugasakhir.Model

data class DosenResponse(
    val count: Int,
    val dosen: List<Dosen>
)

data class Dosen(
    val user_id: Int,
    val identifier: String,
    val user_name: String,
    val foto_profile: String?,
    val no_hp: String,
    val informasi: String
)
