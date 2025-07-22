package com.afian.tugasakhir.Component

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.afian.tugasakhir.R
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DosenDetailDialog(
    dosen: Dosen,
    onDismissRequest: () -> Unit,
    dosenOnCampusList: List<Dosen>,     // <-- Parameter BARU
    dosenNotInCampusList: List<Dosen> // <-- Parameter BARU
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val namaDosen = dosen.user_name

    // Tentukan apakah dosen ada di kampus berdasarkan statusnya
    // Anda perlu menyesuaikan logika ini dengan data 'status_kehadiran' dari objek Dosen Anda
    // Tentukan status kehadiran berdasarkan keberadaan dosen di list yang diterima
    val isActuallyOnCampus = dosenOnCampusList.any { it.identifier == dosen.identifier }
    // Untuk debug, Anda bisa log ini:
    // LaunchedEffect(dosen, dosenOnCampusList) {
    //     Log.d("DosenDetailDialog", "Dosen: ${dosen.user_name}, isOnCampusList: $isActuallyOnCampus")
    // }

    val indicatorColor = if (isActuallyOnCampus) {
        Color(0xFF4CAF50) // Hijau jika ada di daftar dosen di kampus
    } else {
        // Jika tidak ada di daftar 'dosenOnCampusList', kita asumsikan tidak di kampus (merah)
        // Anda bisa menambahkan pengecekan ke dosenNotInCampusList jika perlu logika lebih spesifik
        Color(0xFFF44336) // Merah jika tidak di kampus
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3E62)), // Background biru gelap
            modifier = Modifier
                .fillMaxWidth()
            // .padding(16.dp) // Padding ini bisa membuat dialog tampak lebih kecil dari lebar penuh,
            // jika ingin dialog full-width di dalam Dialog wrapper, padding bisa dihilangkan atau dikurangi
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { // Column utama untuk menampung konten dan status bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        Log.d("DosenDetailDialog", "Tombol Close diklik!")
                        onDismissRequest()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Dialog",
                            tint = Color.White
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 8.dp)) { // Box untuk konten utama dan tombol close
                    // Tombol Close (X) di kanan atas
//                    IconButton(
//                        onClick = {
//                            Log.d("DosenDetailDialog", "Tombol Close (X) diklik!")
//                            onDismissRequest()
//                        },
//                        modifier = Modifier
//                            .align(Alignment.TopEnd)
//                            .padding(4.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Filled.Close,
//                            contentDescription = "Close Dialog",
//                            tint = Color.White
//                        )
//                    }

                    // Konten utama dialog
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 32.dp), // Padding atas lebih besar untuk memberi ruang dari tombol close
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Foto Profil
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = dosen.foto_profile,
                                placeholder = painterResource(id = R.drawable.placeholder_image), // Pastikan placeholder_image ada
                                error = painterResource(id = R.drawable.placeholder_image)
                            ),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fakultas (Asumsi ada field dosen.fakultas atau Anda hardcode)
                        Text(
                            text = "Fakultas Ilmu Komputer", // Ganti dengan dosen.fakultas jika ada
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Username
                        Text(
                            text = dosen.user_name,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        // Identifier (NIDN)
                        Text(
                            text = dosen.identifier,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Informasi/Deskripsi Dosen
                        if (!dosen.informasi.isNullOrBlank()) { // Tampilkan Card hanya jika ada informasi
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Text(
                                    text = dosen.informasi, // Tidak perlu fallback jika sudah dicek null/blank
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Justify
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }


                        // Tombol Chat WhatsApp (sudah ada, bentuk dan warna seperti di gambar)
                        Button(
                            onClick = {
                                val nomorHp = dosen.no_hp
                                if (!nomorHp.isNullOrBlank()) {
                                    try {
                                        var formattedNumber = nomorHp.filter { it.isDigit() }
                                        if (formattedNumber.startsWith("0")) {
                                            formattedNumber = "62" + formattedNumber.substring(1)
                                        } else if (!formattedNumber.startsWith("62")) {
                                            formattedNumber = "62$formattedNumber"
                                        }
                                        val templatePesan = "Assalamualaikum Pak/Bu ${namaDosen}, saya ingin bertanya terkait..."
                                        val encodedPesan = URLEncoder.encode(templatePesan, "UTF-8")
                                        val url = "https://wa.me/$formattedNumber?text=$encodedPesan"
                                        uriHandler.openUri(url)
                                    } catch (e: Exception) {
                                        Log.e("DosenDetailDialog", "Failed to open WhatsApp: ${e.message}")
                                        Toast.makeText(context, "Gagal membuka WhatsApp.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Log.w("DosenDetailDialog", "Nomor HP Dosen kosong.")
                                    Toast.makeText(context, "Nomor HP dosen tidak tersedia.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp), // Sesuaikan rounded corner seperti di gambar
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(35.dp) // Atur tinggi tombol jika perlu
                        ) {
                            Text("Chat On WhatsApp", color = Color.Black, style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.whatsapp), // GANTI DENGAN ICON WHATSAPP YANG BENAR
                                contentDescription = "WhatsApp Icon",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(60.dp) // Ukuran ikon disesuaikan
                            )


                        }
                        // Spacer di bawah tombol WhatsApp, sebelum status bar hijau
                        Spacer(modifier = Modifier.height(16.dp))
                    } // Akhir Column konten utama
                } // Akhir Box konten utama
                // --- Indikator Status ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(indicatorColor) // Gunakan warna yang sudah ditentukan
                )
            } // Akhir Column utama (yang membungkus konten dan status bar)
        } // Akhir Card utama
    } // Akhir Dialog
}

//@Composable
//fun DosenDetailDialog(
//    dosen: Dosen,
//    onDismissRequest: () -> Unit
//) {
//    val context = LocalContext.current
//    val uriHandler = LocalUriHandler.current
//    val namaDosen = dosen.user_name
//
//    Dialog(onDismissRequest = onDismissRequest) {
//        Card(
//            shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3E62)), // Background biru gelap
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp) // Beri padding agar tidak terlalu mepet layar dialog
//        ) {
//            Box(modifier = Modifier.fillMaxWidth()) {
//                // Tombol Close (X) di kanan atas
//                IconButton(
//                    onClick = {Log.d("DosenDetailDialog", "Tombol Close (X) diklik!")
//                        onDismissRequest() },
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .padding(4.dp) // Padding kecil untuk tombol close
//                ) {
//                    Icon(
//                        imageVector = Icons.Filled.Close,
//                        contentDescription = "Close Dialog",
//                        tint = Color.White // Warna ikon putih
//                    )
//                }
//
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .verticalScroll(rememberScrollState()) // Aktifkan scroll jika konten panjang
//                        .padding(horizontal = 16.dp, vertical = 24.dp), // Padding konten utama
//                    horizontalAlignment = Alignment.CenterHorizontally // Pusatkan item di Column
//                ) {
//                    // Foto Profil
//                    Image(
//                        painter = rememberAsyncImagePainter(
//                            model = dosen.foto_profile,
//                            placeholder = painterResource(id = R.drawable.placeholder_image),
//                            error = painterResource(id = R.drawable.placeholder_image)
//                        ),
//                        contentDescription = "Profile Picture",
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier
//                            .size(100.dp) // Ukuran gambar lebih besar
//                            .clip(CircleShape)
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Fakultas (Ganti dosen.fakultas jika nama field berbeda)
//                    Text(
//                        text =  "Fakultas Ilmu Komputer", // Fallback text
//                        style = MaterialTheme.typography.headlineSmall, // Gaya lebih besar
//                        color = Color.White,
//                        textAlign = TextAlign.Center
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Username
//                    Text(
//                        text = dosen.user_name,
//                        style = MaterialTheme.typography.titleLarge, // Gaya judul
//                        color = Color.White,
//                        textAlign = TextAlign.Center
//                    )
//
//                    // Identifier (NIDN)
//                    Text(
//                        text = dosen.identifier,
//                        style = MaterialTheme.typography.bodyMedium, // Gaya body
//                        color = Color.LightGray, // Warna abu terang
//                        textAlign = TextAlign.Center
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Informasi/Deskripsi Dosen (Ganti dosen.informasi jika nama field berbeda)
//                    Card( // Beri background putih untuk kontras teks deskripsi
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color.White)
//                    ) {
//                        Text(
//                            text = dosen.informasi ?: "Tidak ada informasi tambahan.", // Fallback text
//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.padding(16.dp),
//                            textAlign = TextAlign.Justify // Ratakan teks
//                        )
//                    }
//
//
//                    Spacer(modifier = Modifier.height(24.dp))
//
//                    // Baris Tombol
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceEvenly // Beri jarak merata
//                    ) {
//                        // Tombol Status "On Campus" (contoh, belum dinamis)
//                        Button(
//                            onClick = {  },
//                            shape = RoundedCornerShape(50), // Sangat bulat
//                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Warna hijau
//                        ) {
//                            Text("On Campus", color = Color.White)
//                        }
//
//                        // Tombol Chat WhatsApp
//                        Button(
//                            onClick = {
//                                val nomorHp = dosen.no_hp
//                                // Pastikan nomor HP ada dan formatnya benar (awali dgn kode negara tanpa +/0)
//                                if (!nomorHp.isNullOrBlank()) {
//                                    try {
//                                        // Hapus karakter non-digit, ganti 0 di depan dgn 62
//                                        var formattedNumber = nomorHp.filter { it.isDigit() }
//                                        if (formattedNumber.startsWith("0")) {
//                                            formattedNumber = "62" + formattedNumber.substring(1)
//                                        } else if (!formattedNumber.startsWith("62")) {
//                                            // Tambah 62 jika belum ada (asumsi nomor Indonesia)
//                                            // Anda mungkin perlu logika lebih baik untuk kode negara lain
//                                            formattedNumber = "62$formattedNumber"
//                                        }
//
//                                        // --- 👇 TAMBAHKAN TEMPLATE PESAN DI SINI 👇 ---
//
//                                        // 1. Buat template pesan awal yang Anda inginkan
//                                        //    Anda bisa menyertakan nama dosen jika mau.
//                                        //    (Jika Anda bisa mendapatkan nama mahasiswa yang login, bisa dimasukkan juga)
//                                        val templatePesan = "Assalamualaikum Pak/Bu ${namaDosen}, saya ingin bertanya terkait..."
//
//                                        // 2. URL Encode template pesan agar aman digunakan dalam URL
//                                        //    Spasi akan menjadi %20, dll.
//                                        val encodedPesan = URLEncoder.encode(templatePesan, "UTF-8")
//
//                                        // 3. Buat URL lengkap dengan parameter 'text'
//                                        val url = "https://wa.me/$formattedNumber?text=$encodedPesan"
//
//                                        // --- 👆 AKHIR TAMBAHAN TEMPLATE PESAN 👆 ---
//
//                                        uriHandler.openUri(url) // Buka link WhatsApp
//                                    } catch (e: Exception) {
//                                        Log.e("DosenDetailDialog", "Failed to open WhatsApp: ${e.message}")
//                                        // Tampilkan pesan error ke user jika perlu
//                                    }
//                                } else {
//                                    Log.w("DosenDetailDialog", "Nomor HP Dosen kosong.")
//                                    // Tampilkan pesan bahwa nomor tidak ada
//                                }
//                            },
//                            shape = RoundedCornerShape(50), // Sangat bulat
//                            colors = ButtonDefaults.buttonColors(containerColor = Color.White) // Warna putih
//                        ) {
//                            // Ganti R.drawable.ic_whatsapp dengan resource icon WhatsApp Anda
//                            Icon(
//                                painter = painterResource(id = R.drawable.ic_settings), // GANTI DENGAN ICON WHATSAPP
//                                contentDescription = "WhatsApp Icon",
//                                tint = Color(0xFF25D366), // Warna hijau WhatsApp
//                                modifier = Modifier.size(18.dp) // Ukuran ikon kecil
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text("Chat On WhatsApp", color = Color.Black) // Teks hitam
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(16.dp)) // Jarak bawah
//                }
//            }
//        }
//    }
//}