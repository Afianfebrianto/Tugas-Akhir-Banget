package com.afian.tugasakhir.Component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.afian.tugasakhir.Model.Dosen
import com.afian.tugasakhir.R

@Composable
fun MahasiswaItemPanggil(
    mahasiswa: Dosen, // Menggunakan model Dosen untuk data mahasiswa
    isCalling: Boolean, // True jika sedang memanggil mahasiswa ini
    onPanggilClick: () -> Unit,
    modifier: Modifier = Modifier // Tambahkan modifier
) {
    Card(modifier = modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), // Padding disesuaikan
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = mahasiswa.foto_profile,
                    placeholder = painterResource(id = R.drawable.placeholder_image),
                    error = painterResource(id = R.drawable.placeholder_image)
                ),
                contentDescription = "Foto ${mahasiswa.user_name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp) // Ukuran foto lebih kecil
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) { // Ambil sisa ruang
                Text(mahasiswa.user_name, style = MaterialTheme.typography.bodyLarge)
                Text(mahasiswa.identifier, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Tombol Panggil
            Button(
                onClick = onPanggilClick,
                enabled = !isCalling, // Disable tombol jika sedang proses panggil
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp) // Padding tombol
            ) {
                if (isCalling) {
                    // Tampilkan loading indicator kecil
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary // Warna kontras
                    )
                } else {
                    Text("Panggil")
                }
            }
        }
    }
}