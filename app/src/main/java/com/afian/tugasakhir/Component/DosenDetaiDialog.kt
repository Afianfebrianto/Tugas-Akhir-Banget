package com.afian.tugasakhir.Component

import android.R
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.afian.tugasakhir.Model.Dosen
import java.net.URLEncoder

@Composable
fun DosenDetailDialog(
    dosen: Dosen,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val namaDosen = dosen.user_name

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3E62)), // Background biru gelap
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Beri padding agar tidak terlalu mepet layar dialog
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Tombol Close (X) di kanan atas
                IconButton(
                    onClick = {Log.d("DosenDetailDialog", "Tombol Close (X) diklik!")
                        onDismissRequest() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp) // Padding kecil untuk tombol close
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Dialog",
                        tint = Color.White // Warna ikon putih
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()) // Aktifkan scroll jika konten panjang
                        .padding(horizontal = 16.dp, vertical = 24.dp), // Padding konten utama
                    horizontalAlignment = Alignment.CenterHorizontally // Pusatkan item di Column
                ) {
                    // Foto Profil
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = dosen.foto_profile,
                            placeholder = painterResource(id = R.drawable.ic_dialog_info),
                            error = painterResource(id = R.drawable.ic_dialog_email)
                        ),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp) // Ukuran gambar lebih besar
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fakultas (Ganti dosen.fakultas jika nama field berbeda)
                    Text(
                        text =  "Fakultas Ilmu Komputer", // Fallback text
                        style = MaterialTheme.typography.headlineSmall, // Gaya lebih besar
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Username
                    Text(
                        text = dosen.user_name,
                        style = MaterialTheme.typography.titleLarge, // Gaya judul
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // Identifier (NIDN)
                    Text(
                        text = dosen.identifier,
                        style = MaterialTheme.typography.bodyMedium, // Gaya body
                        color = Color.LightGray, // Warna abu terang
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Informasi/Deskripsi Dosen (Ganti dosen.informasi jika nama field berbeda)
                    Card( // Beri background putih untuk kontras teks deskripsi
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = dosen.informasi ?: "Tidak ada informasi tambahan.", // Fallback text
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Justify // Ratakan teks
                        )
                    }


                    Spacer(modifier = Modifier.height(24.dp))

                    // Baris Tombol
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly // Beri jarak merata
                    ) {
                        // Tombol Status "On Campus" (contoh, belum dinamis)
                        Button(
                            onClick = { /* Aksi jika tombol status diklik? */ },
                            shape = RoundedCornerShape(50), // Sangat bulat
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Warna hijau
                        ) {
                            Text("On Campus", color = Color.White)
                        }

                        // Tombol Chat WhatsApp
                        Button(
                            onClick = {
                                val nomorHp = dosen.no_hp
                                // Pastikan nomor HP ada dan formatnya benar (awali dgn kode negara tanpa +/0)
                                if (!nomorHp.isNullOrBlank()) {
                                    try {
                                        // Hapus karakter non-digit, ganti 0 di depan dgn 62
                                        var formattedNumber = nomorHp.filter { it.isDigit() }
                                        if (formattedNumber.startsWith("0")) {
                                            formattedNumber = "62" + formattedNumber.substring(1)
                                        } else if (!formattedNumber.startsWith("62")) {
                                            // Tambah 62 jika belum ada (asumsi nomor Indonesia)
                                            // Anda mungkin perlu logika lebih baik untuk kode negara lain
                                            formattedNumber = "62$formattedNumber"
                                        }

                                        // --- 👇 TAMBAHKAN TEMPLATE PESAN DI SINI 👇 ---

                                        // 1. Buat template pesan awal yang Anda inginkan
                                        //    Anda bisa menyertakan nama dosen jika mau.
                                        //    (Jika Anda bisa mendapatkan nama mahasiswa yang login, bisa dimasukkan juga)
                                        val templatePesan = "Assalamualaikum Pak/Bu ${namaDosen}, saya ingin bertanya terkait..."

                                        // 2. URL Encode template pesan agar aman digunakan dalam URL
                                        //    Spasi akan menjadi %20, dll.
                                        val encodedPesan = URLEncoder.encode(templatePesan, "UTF-8")

                                        // 3. Buat URL lengkap dengan parameter 'text'
                                        val url = "https://wa.me/$formattedNumber?text=$encodedPesan"

                                        // --- 👆 AKHIR TAMBAHAN TEMPLATE PESAN 👆 ---

                                        uriHandler.openUri(url) // Buka link WhatsApp
                                    } catch (e: Exception) {
                                        Log.e("DosenDetailDialog", "Failed to open WhatsApp: ${e.message}")
                                        // Tampilkan pesan error ke user jika perlu
                                    }
                                } else {
                                    Log.w("DosenDetailDialog", "Nomor HP Dosen kosong.")
                                    // Tampilkan pesan bahwa nomor tidak ada
                                }
                            },
                            shape = RoundedCornerShape(50), // Sangat bulat
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White) // Warna putih
                        ) {
                            // Ganti R.drawable.ic_whatsapp dengan resource icon WhatsApp Anda
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dialog_map), // GANTI DENGAN ICON WHATSAPP
                                contentDescription = "WhatsApp Icon",
                                tint = Color(0xFF25D366), // Warna hijau WhatsApp
                                modifier = Modifier.size(18.dp) // Ukuran ikon kecil
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chat On WhatsApp", color = Color.Black) // Teks hitam
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // Jarak bawah
                }
            }
        }
    }
}