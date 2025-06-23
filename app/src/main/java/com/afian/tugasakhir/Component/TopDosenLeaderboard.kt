package com.afian.tugasakhir.Component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afian.tugasakhir.Model.PeringkatItem
import kotlin.collections.take

// Fungsi helper nama bulan bisa ditaruh di sini atau di file Utils terpisah
// Pastikan fungsi ini ada dan bisa diakses
private fun getMonthNameLeaderboard(monthNumber: Int): String {
    val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    return if (monthNumber in 1..12) months[monthNumber - 1] else "..."
}

/**
 * Composable reusable untuk menampilkan bagian leaderboard Top 3 Dosen.
 * Menerima data state sebagai parameter.
 *
 * @param modifier Modifier untuk mengatur tata letak Column utama dari luar.
 * @param peringkatList Daftar lengkap peringkat dosen (akan diambil top 3).
 * @param isLoading Status loading untuk data peringkat.
 * @param errorMessage Pesan error jika gagal memuat peringkat.
 * @param selectedMonth Bulan yang sedang ditampilkan peringkatnya (1-12).
 * @param selectedYear Tahun yang sedang ditampilkan peringkatnya.
 */
@Composable
fun TopDosenLeaderboard(
    modifier: Modifier = Modifier, // Modifier untuk Column luar
    peringkatList: List<PeringkatItem>,
    isLoading: Boolean,
    errorMessage: String?,
    selectedMonth: Int,
    selectedYear: Int
) {
    // Ambil hanya 3 data teratas dari list peringkat yang diberikan
    val top3Peringkat = peringkatList.take(3)

    Column(modifier = modifier.fillMaxWidth()) { // Gunakan modifier dari parameter
        // Judul Section Leaderboard
        Text(
            text = "Top Kehadiran ( ${getMonthNameLeaderboard(selectedMonth)} $selectedYear )",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp) // Padding judul
        )

        // Konten Leaderboard (Loading / Error / List Top 3)
        when {
            // Tampilkan loading hanya jika list peringkat belum ada isinya
            isLoading && peringkatList.isEmpty() -> {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp))
                }
            }
            // Tampilkan error jika ada
            errorMessage != null -> {
                Text(
                    "Gagal memuat peringkat: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
            // Tampilkan pesan jika list top 3 kosong (setelah selesai loading dan tidak ada error)
            top3Peringkat.isEmpty() && !isLoading -> {
                Text(
                    "Belum ada data peringkat untuk periode ini.",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // Tampilkan list Top 3 jika ada data
            else -> {
                // Gunakan Column biasa untuk menampilkan 1-3 item saja
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    top3Peringkat.forEachIndexed { index, peringkatItem ->
                        // Panggil Composable PeringkatDosenItem yang sudah ada
                        PeringkatDosenItem(
                            item = peringkatItem,
                            rank = index + 1 // Rank 1, 2, 3
                        )
                    }
                }
            }
        } // Akhir when
    } // Akhir Column utama komponen
}
