package com.afian.tugasakhir.View.Screen.HelperScreen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue // <--- Tambahkan ini
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.afian.tugasakhir.Component.NotificationItem
import com.afian.tugasakhir.Component.PanggilanItem
import com.afian.tugasakhir.Model.NotificationViewModel




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationMhsScreen( // Layar ini sekarang menampilkan Riwayat Panggilan untuk Mahasiswa
    viewModel: NotificationViewModel = viewModel()
) {
    // Ambil state yang BERKAITAN DENGAN PANGGILAN dari ViewModel
    // StateFlow untuk daftar riwayat panggilan
    val panggilanHistory by viewModel.panggilanHistory.collectAsState()
    // State untuk status loading saat mengambil riwayat
    val isLoading by viewModel.isLoadingHistory
    // State untuk menampilkan pesan hasil (misal setelah accept/decline) atau error
    val responseMessage by viewModel.responseMessage
    // State untuk mengetahui ID panggilan mana yang sedang diproses responsnya (untuk loading di tombol)
    val respondingPanggilanId by viewModel.isRespondingPanggilanId

    val context = LocalContext.current

    // Tampilkan Toast saat responseMessage (hasil accept/decline/error) berubah
    LaunchedEffect(responseMessage) {
        responseMessage?.let { message ->
            // Tampilkan pesan jika tidak kosong dan bukan pesan "loading" awal
            if (message.isNotEmpty() && message != "Mengirim respons...") {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                // Idealnya, tambahkan fungsi di ViewModel untuk me-reset pesan ini setelah ditampilkan
                // viewModel.clearResponseMessage()
            }
        }
    }

    // Anda bisa menambahkan tombol refresh atau memanggil fetch saat layar pertama kali visible
    // LaunchedEffect(Unit) { viewModel.fetchPanggilanHistory() } // ViewModel sudah fetch di init

    Scaffold(
        topBar = {
            // Ganti judul AppBar sesuai konten
            TopAppBar(title = { Text("Riwayat Panggilan Dosen") })
            // Tambahkan tombol refresh jika diinginkan
            // actions = {
            //    IconButton(onClick = { viewModel.fetchPanggilanHistory() }) {
            //        Icon(Icons.Default.Refresh, contentDescription = "Refresh Riwayat")
            //    }
            // }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) { // Box utama

            // Tampilkan Loading Indicator di tengah jika isLoading true
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // Tampilkan konten list jika tidak sedang loading
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp), // Padding kiri/kanan untuk list
                    contentPadding = PaddingValues(vertical = 8.dp), // Padding atas/bawah list
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Jarak antar Card PanggilanItem
                ) {
                    // Tampilkan pesan jika riwayat panggilan kosong
                    if (panggilanHistory.isEmpty()) {
                        item {
                            Text(
                                text = "Belum ada riwayat panggilan.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center // Opsional tengahkan
                            )
                        }
                    }
                    // Tampilkan item-item riwayat panggilan jika ada
                    else {
                        // Menggunakan items(count=...) sebagai workaround dari error sebelumnya
                        items(
                            count = panggilanHistory.size,
                            key = { index ->
                                // Gunakan ID panggilan sebagai key unik
                                panggilanHistory.getOrNull(index)?.panggilan_id ?: index
                            }
                        ) { index ->
                            // Ambil data panggilan untuk indeks saat ini
                            val itemPanggilan = panggilanHistory.getOrNull(index)
                            if (itemPanggilan != null) {
                                // Panggil Composable PanggilanItem
                                PanggilanItem(
                                    item = itemPanggilan,
                                    // Tandai jika item ini sedang direspon
                                    isResponding = respondingPanggilanId == itemPanggilan.panggilan_id,
                                    // Lambda untuk tombol Terima
                                    onAccept = {
                                        viewModel.respondToPanggilan(
                                            panggilanId = itemPanggilan.panggilan_id,
                                            respon = "accepted" // Kirim "accepted"
                                        )
                                    },
                                    // Lambda untuk tombol Tolak
                                    onDecline = {
                                        viewModel.respondToPanggilan(
                                            panggilanId = itemPanggilan.panggilan_id,
                                            respon = "declined" // Kirim "declined"
                                        )
                                    }
                                )
                                // Divider tidak perlu karena PanggilanItem sudah Card
                            } else {
                                Log.w("NotificationScreen", "Panggilan item null at index $index")
                            }
                        }
                    }
                } // Akhir LazyColumn
            } // Akhir Else (tidak loading)
        } // Akhir Box utama
    } // Akhir Scaffold
}
