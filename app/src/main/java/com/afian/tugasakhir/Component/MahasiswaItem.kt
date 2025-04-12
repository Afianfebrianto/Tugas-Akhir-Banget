package com.afian.tugasakhir.Component

import android.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.afian.tugasakhir.Model.Dosen
import com.afian.tugasakhir.Model.PanggilanHistoryMahasiswaItem
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MahasiswaItem(
    mahasiswa: Dosen, // Asumsi pakai model Dosen untuk data mahasiswa
    isCalling: Boolean, // Apakah sedang proses memanggil mhs ini?
    onPanggilClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = mahasiswa.foto_profile,
                    placeholder = painterResource(R.drawable.ic_menu_report_image),
                    error = painterResource(id = R.drawable.ic_menu_report_image)
                ),
                contentDescription = "Foto Mahasiswa",
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(mahasiswa.user_name, style = MaterialTheme.typography.bodyLarge)
                Text(mahasiswa.identifier, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Button(onClick = onPanggilClick, enabled = !isCalling) { // Disable saat loading
                if (isCalling) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Panggil")
                }
            }
        }
    }
}

@Composable
fun PanggilanItem(
    item: PanggilanHistoryMahasiswaItem,
    isResponding: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    // --- Formatter untuk Tanggal & Waktu ---
    // Input: Sesuaikan format ini ("yyyy-MM-dd HH:mm:ss") dengan format String
    //        tanggal/waktu yang dikirim oleh backend Anda (dari kolom TIMESTAMP/DATETIME MySQL).
    val inputFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }
    // Output: Format tampilan tanggal dan waktu dalam Bahasa Indonesia
    val outputFormat = remember { SimpleDateFormat("E, dd MMM yyyy HH:mm", Locale("id", "ID")) }

    // Fungsi helper kecil untuk memformat tanggal dengan aman
    val formatTimestamp: (String?) -> String = remember {
        { timestampString ->
            timestampString?.let {
                try {
                    inputFormat.parse(it)?.let { date -> outputFormat.format(date) } ?: it
                } catch (e: ParseException) {
                    it // Kembalikan string asli jika format tidak cocok
                }
            } ?: "-" // Tampilkan "-" jika timestamp null
        }
    }

    val formattedWaktuPanggil = formatTimestamp(item.waktu_panggil)
    val formattedWaktuRespon = formatTimestamp(item.waktu_respon)
    // --- Akhir Formatter ---

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Padding antar card
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // Sedikit shadow
        shape = MaterialTheme.shapes.medium // Sudut sedikit membulat
    ) {
        Column(Modifier.padding(12.dp)) { // Padding di dalam card
            // Baris untuk Foto Dosen dan Detail Teks
            Row(
                verticalAlignment = Alignment.Top, // Align ke atas agar teks tidak aneh jika panjang
                modifier = Modifier.fillMaxWidth()
            ) {
                // Foto Profil Dosen
                Image(
                    painter = rememberAsyncImagePainter(
                        model = item.dosen_foto, // URL foto dosen
                        placeholder = painterResource(id = R.drawable.ic_menu_report_image), // Ganti placeholder
                        error = painterResource(id = R.drawable.ic_menu_report_image)
                    ),
                    contentDescription = "Foto Dosen ${item.dosen_name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp) // Ukuran foto
                        .clip(CircleShape)
                    // Opsional: Tambahkan border jika perlu
                    // .border(BorderStroke(1.dp, Color.LightGray), CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Kolom untuk Teks Detail
                Column(modifier = Modifier.weight(1f)) { // Ambil sisa ruang
                    Text(
                        // Tampilkan nama dosen, beri fallback jika null
                        text = item.dosen_name ?: "Nama Dosen Tidak Ada",
                        style = MaterialTheme.typography.titleMedium, // Ukuran judul medium
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp)) // Jarak kecil
                    Text(
                        text = "Waktu Panggil: $formattedWaktuPanggil",
                        style = MaterialTheme.typography.bodySmall, // Ukuran teks kecil
                        color = Color.Gray // Warna abu-abu
                    )

                    // Tampilkan Status dengan warna berbeda
                    val statusText = item.status.replaceFirstChar { // Huruf awal kapital
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                    val statusColor = when (item.status.lowercase()) {
                        "accepted" -> Color(0xFF4CAF50) // Hijau terang
                        "declined" -> MaterialTheme.colorScheme.error // Warna error tema
                        "pending" -> MaterialTheme.colorScheme.primary // Warna utama tema
                        else -> Color.Gray
                    }
                    Text(
                        text = "Status: $statusText",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (item.status == "pending") FontWeight.Bold else FontWeight.Normal, // Bold jika pending
                        color = statusColor
                    )

                    // Tampilkan waktu respon hanya jika status bukan 'pending'
                    if (item.status.lowercase() != "pending") {
                        Text(
                            text = "Waktu Respon: $formattedWaktuRespon",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                } // Akhir Column Teks Detail
            } // Akhir Row Foto & Detail

            // Tampilkan Tombol Aksi hanya jika status masih 'pending'
            if (item.status.lowercase() == "pending") {
                Spacer(modifier = Modifier.height(12.dp)) // Jarak ke tombol
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End, // Tombol di sisi kanan
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tampilkan Loading jika sedang proses respon untuk item INI
                    if (isResponding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end=8.dp), // Ukuran kecil + padding
                            strokeWidth = 2.dp // Garis tipis
                        )
                    }

                    // Tombol Tolak
                    Button(
                        onClick = onDecline,
                        enabled = !isResponding, // Nonaktifkan jika sedang proses
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error, // Warna background merah
                            contentColor = MaterialTheme.colorScheme.onError // Warna teks putih
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp) // Padding tombol
                    ) {
                        Text("Tolak")
                    }

                    Spacer(Modifier.width(8.dp)) // Jarak antar tombol

                    // Tombol Terima
                    Button(
                        onClick = onAccept,
                        enabled = !isResponding, // Nonaktifkan jika sedang proses
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Terima")
                    }
                } // Akhir Row Tombol
            } // Akhir if status pending
        } // Akhir Column dalam Card
    } // Akhir Card
}