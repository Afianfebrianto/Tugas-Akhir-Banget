package com.afian.tugasakhir.Component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Model.Dosen
import com.afian.tugasakhir.R

@Composable
fun DosenList( // Ini adalah versi PRATINJAU
    modifier: Modifier = Modifier,
    navController: NavController, // <-- Parameter NavController diperlukan
    viewModel: DosenViewModel = viewModel() // Bisa juga diteruskan dari parent
) {
    // Ambil list dosen di kampus yang sudah terfilter
    val dosenListFiltered by viewModel.filteredDosenList.collectAsState()
    val isLoading by viewModel.isLoadingDosen

    val dosenOnCampus by viewModel.filteredDosenList.collectAsState()
    val dosenNotInCampus by viewModel.filteredDosenNotInCampusList.collectAsState()

    // State untuk dialog detail dosen (tetap)
    var selectedDosen by remember { mutableStateOf<Dosen?>(null) }

    // Jumlah item yang ditampilkan di pratinjau
    val previewItemCount = 4

    // Ambil N item pertama untuk ditampilkan
    val displayedDosenList = dosenListFiltered.take(previewItemCount)
    val searchQuery by viewModel.searchQuery.collectAsState() // Ambil search query

    Column(modifier = modifier.fillMaxWidth()) {

        // --- Baris Judul dengan Refresh ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Saat Ini di Kampus (${dosenListFiltered.size})", // Tetap tampilkan jumlah total
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            // Tombol Refresh
            IconButton(
                onClick = { viewModel.refreshData() }, // Asumsi fungsi ini ada
                enabled = !isLoading
            ) {
                // Tampilkan loading hanya jika list kosong dan sedang fetch
                if (isLoading && dosenListFiltered.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Muat Ulang Daftar"
                    )
                }
            }
        } // --- Akhir Row Judul ---

        // --- Card untuk membungkus konten daftar dosen atau pesan kosong ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .padding(horizontal = 8.dp,) ,// Padding di luar card ini
                // Beri tinggi minimal agar pesan "kosong" bisa terlihat di tengah dengan baik
                // Sesuaikan nilai minHeight sesuai kebutuhan desain Anda
//                .defaultMinSize(minHeight = 50.dp),
            shape = RoundedCornerShape(8.dp), // Bentuk card, bisa disesuaikan
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Sedikit shadow
        ) {
            // Logika untuk menampilkan loading, pesan kosong, atau daftar item
            when {
                // 1. Jika sedang loading dan daftar yang akan ditampilkan (preview) masih kosong
                isLoading && displayedDosenList.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize() // Memenuhi ruang dalam Card
                            .padding(16.dp), // Beri padding agar tidak terlalu mepet tepi Card
                        contentAlignment = Alignment.Center // Konten di tengah Box
                    ) {
                        CircularProgressIndicator()
                    }
                }
                // 2. Jika tidak loading DAN daftar yang akan ditampilkan (preview) kosong
                !isLoading && displayedDosenList.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize() // Memenuhi ruang dalam Card
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            // Menggunakan teks sesuai permintaan Anda jika tidak ada query pencarian,
                            // atau menampilkan pesan hasil pencarian jika ada query.
                            text = if (searchQuery.isBlank()) "Tidak ada dosen di kampus"
                            else "Dosen \"$searchQuery\" tidak ditemukan di kampus.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center // Teks juga di tengah secara horizontal
                        )
                    }
                }
                // 3. Jika ada item untuk ditampilkan (baik sedang loading data baru atau tidak)
                else -> {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp), // Padding di dalam Card untuk list item
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Hanya tampilkan N item pertama
                        displayedDosenList.forEach { dosen ->
                            DosenItem(
                                dosen = dosen,
                                onClick = { selectedDosen = it }
                            )
                        }
                    }
                }
            }
        }
        // --- Akhir Card untuk konten daftar dosen ---


//        // --- Daftar Ringkasan Dosen (Max 4 item) ---
//        // Tampilkan pesan jika list kosong (setelah difilter)
//        if (displayedDosenList.isEmpty() && !isLoading) {
//            val searchQuery by viewModel.searchQuery.collectAsState()
////            val searchQuery by viewModel.searchQueryDosen.collectAsState()
//            Text(
//                text = if(searchQuery.isBlank()) "Tidak ada dosen di kampus saat ini." else "Dosen \"$searchQuery\" tidak ditemukan di kampus.",
//                modifier = Modifier.padding(start=16.dp, top=8.dp, bottom=8.dp),
//                style = MaterialTheme.typography.bodyMedium
//            )
//        } else {
//            // Gunakan Column biasa karena item terbatas
//            Column(
//                modifier = Modifier.padding(horizontal = 8.dp),
//                verticalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                // Hanya tampilkan N item pertama
//                displayedDosenList.forEach { dosen ->
//                    DosenItem(
//                        dosen = dosen,
////                        isOnCampus = true, // Selalu true untuk list ini
//                        onClick = { selectedDosen = it } // Tetap bisa buka dialog dari pratinjau
//                    )
//                }
//            }
//        }
//        // --- Akhir Daftar Ringkasan ---


        // --- Tombol "Lihat Semua" ---
        // Tampilkan hanya jika jumlah total > jumlah yang ditampilkan di pratinjau
        if (dosenListFiltered.size > previewItemCount && !isLoading) { // Sembunyikan juga jika sedang loading awal
            TextButton(
                onClick = { navController.navigate("informasi_dosen") },
                modifier = Modifier.align(Alignment.End).padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Lihat Semua")
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
        // --- Akhir Tombol "Lihat Semua" ---

    } // Akhir Column Utama

    // Tampilkan Dialog jika selectedDosen tidak null (tidak berubah)
    selectedDosen?.let { dosenData ->
        DosenDetailDialog(
            dosen = dosenData,
            onDismissRequest = { selectedDosen = null },
            dosenOnCampusList = dosenOnCampus,         // <-- Teruskan list dosen di kampus
            dosenNotInCampusList = dosenNotInCampus  // <-- Teruskan list dosen tidak di kampus
        )
    }
}


@Composable
fun DosenItem(
    dosen: Dosen,
    onClick: (Dosen) -> Unit // <-- Tambahkan parameter onClick lambda
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(dosen) } // <-- Tambahkan clickable di sini
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically // Ratakan vertikal
        ) {
            Image(
                // Gunakan rememberAsyncImagePainter untuk Coil versi baru
                painter = rememberAsyncImagePainter(
                    model = dosen.foto_profile, // URL dari Cloudinary
                    placeholder = painterResource(id = R.drawable.placeholder_image_24), // Fallback placeholder
                    error = painterResource(id = R.drawable.placeholder_image_24) // Gambar jika error load
                ),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp) // Sedikit lebih besar mungkin?
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = dosen.user_name, style = MaterialTheme.typography.titleMedium) // Gaya teks lebih besar
                Text(text = dosen.identifier, style = MaterialTheme.typography.bodyMedium, color = Color.Gray) // Gaya teks lebih kecil, warna abu
            }
        }
    }
}