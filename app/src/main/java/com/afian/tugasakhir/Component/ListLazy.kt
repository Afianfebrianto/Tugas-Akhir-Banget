package com.afian.tugasakhir.Component

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Model.Dosen
import com.afian.tugasakhir.R

@Composable
fun DosenList(viewModel: DosenViewModel = viewModel()) {
    val dosenListState = viewModel.filteredDosenList.collectAsState()
    val dosenList = dosenListState.value // Ambil list dari state

    // State untuk menyimpan dosen yang dipilih (null jika tidak ada yg dipilih)
    var selectedDosen by remember { mutableStateOf<Dosen?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp)) { // Padding di Column utama
         Text(text = "Dosen on Campus", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp)) // Judul jika perlu

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp) // Jarak antar card
        ) {
            items(
                items = dosenList,
                key = { dosen -> dosen.identifier } // Gunakan identifier sebagai key
            ) { dosen ->
                DosenItem(
                    dosen = dosen,
                    onClick = { clickedDosen ->
                        selectedDosen = clickedDosen // Update state saat item diklik
                    }
                )
            }
        }
    }

    // Tampilkan Dialog jika selectedDosen tidak null
    selectedDosen?.let { dosenData ->
        DosenDetailDialog(
            dosen = dosenData,
            onDismissRequest = {Log.d("PemanggilDialog", "onDismissRequest dipanggil, menutup dialog...")
                selectedDosen = null } // Set state ke null untuk menutup dialog
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

//@Composable
//fun DosenItem(
//    dosen: Dosen,
//    isOnCampus: Boolean, // <-- Parameter BARU untuk status di kampus
//    onClick: (Dosen) -> Unit
//) {
//    // Tentukan warna border berdasarkan status
//    val borderColor = if (isOnCampus) Color.Green else Color.Red
//    val borderWidth = 2.dp // Ketebalan border (bisa disesuaikan)
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable { onClick(dosen) }
//    ) {
//        Row(
//            modifier = Modifier.padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Image(
//                painter = rememberAsyncImagePainter(
//                    model = dosen.foto_profile,
//                    placeholder = painterResource(id = R.drawable.placeholder_image),
//                    error = painterResource(id = R.drawable.placeholder_image)
//                ),
//                contentDescription = "Profile Picture",
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .size(50.dp)
//                    // --- 👇 Tambahkan Border di sini 👇 ---
//                    .border(
//                        BorderStroke(borderWidth, borderColor), // Tentukan ketebalan & warna
//                        CircleShape // Bentuk border mengikuti bentuk clip
//                    )
//                    // --- 👆 Akhir Border 👆 ---
//                    .clip(CircleShape) // Clip gambar menjadi lingkaran
//            )
//            Spacer(modifier = Modifier.width(16.dp))
//            Column {
//                Text(text = dosen.user_name, style = MaterialTheme.typography.titleMedium)
//                Text(text = dosen.identifier, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
//                // (Opsional) Tambahkan indikator teks kecil
//                // Text(
//                //     text = if (isOnCampus) "Di Kampus" else "Tidak di Kampus",
//                //     style = MaterialTheme.typography.labelSmall,
//                //     color = borderColor,
//                //     modifier = Modifier.padding(top = 2.dp)
//                // )
//            }
//        }
//    }
//}
@Preview
@Composable
fun PreviewListDosen() {
    DosenList()
}