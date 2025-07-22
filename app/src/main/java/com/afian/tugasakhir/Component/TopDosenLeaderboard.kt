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


private fun getMonthNameLeaderboard(monthNumber: Int): String {
    val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    return if (monthNumber in 1..12) months[monthNumber - 1] else "..."
}


@Composable
fun TopDosenLeaderboard(
    modifier: Modifier = Modifier,
    peringkatList: List<PeringkatItem>,
    isLoading: Boolean,
    errorMessage: String?,
    selectedMonth: Int,
    selectedYear: Int
) {
    val top3Peringkat = peringkatList.take(3)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Top Kehadiran ( ${getMonthNameLeaderboard(selectedMonth)} $selectedYear )",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        when {
            isLoading && peringkatList.isEmpty() -> {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp))
                }
            }
            errorMessage != null -> {
                Text(
                    "Gagal memuat peringkat: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
            top3Peringkat.isEmpty() && !isLoading -> {
                Text(
                    "Belum ada data peringkat untuk periode ini.",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    top3Peringkat.forEachIndexed { index, peringkatItem ->
                        PeringkatDosenItem(
                            item = peringkatItem,
                            rank = index + 1
                        )
                    }
                }
            }
        }
    }
}
