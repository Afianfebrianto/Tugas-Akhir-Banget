package com.afian.tugasakhir.View.Screen.HelperScreen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue // <--- Tambahkan ini
import com.afian.tugasakhir.Component.NotificationItem
import com.afian.tugasakhir.Model.NotificationViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = viewModel()
) {
    // Gunakan 'by' delegate - sekarang seharusnya berfungsi
    val notifications by viewModel.notificationHistory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Riwayat Notifikasi") })
            // actions = { ... tombol hapus ... }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Kondisi jika list notifikasi kosong
            if (notifications.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada notifikasi.",
                        modifier = Modifier.padding(vertical = 16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            // Kondisi jika list notifikasi ada isinya
            else {
                // === 👇 MENCOBA OVERLOAD items() YANG BERBEDA (BERBASIS JUMLAH/INDEKS) 👇 ===
                // Ini cara alternatif jika 'items(items = notifications...)' terus error
                items(
                    count = notifications.size, // Berikan jumlah item
                    key = { index -> // Key sekarang berbasis indeks, gunakan ID item
                        // Ambil ID dari item pada indeks ini sebagai key unik
                        // Tambahkan null check untuk keamanan jika list bisa berubah cepat
                        notifications.getOrNull(index)?.id
                            ?: index // Fallback ke index jika item hilang
                    }
                    // contentType = { index -> NotificationHistoryItem::class } // Opsional: bantu performa
                ) { index -> // Lambda content sekarang menerima indeks
                    // Ambil item notifikasi berdasarkan indeks
                    val notificationItem = notifications.getOrNull(index)
                    // Pastikan item tidak null sebelum ditampilkan
                    if (notificationItem != null) {
                        NotificationItem(notification = notificationItem)
                        Divider(modifier = Modifier.padding(top = 8.dp))
                    } else {
                        // Handle kasus (jarang) item hilang antara perhitungan count dan render
                        Log.w(
                            "NotificationScreen",
                            "Item at index $index became null during composition."
                        )
                    }
                }
                // === 👆 AKHIR ALTERNATIF items() 👆 ===
            }
        }
    }
}
