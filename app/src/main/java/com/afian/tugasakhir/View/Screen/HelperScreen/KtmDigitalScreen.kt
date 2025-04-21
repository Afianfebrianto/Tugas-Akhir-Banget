package com.afian.tugasakhir.View.Screen.HelperScreen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.R
import com.afian.tugasakhir.utils.BarcodeUtils
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KtmScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel() // Ambil dari NavGraph jika di-scope ke Activity
) {
    // Ambil data user mahasiswa yang sedang login
    val userData = loginViewModel.getUserData() // Asumsi fungsi ini mengembalikan User?

    // State untuk menampung Bitmap barcode
    var barcodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Generate barcode saat identifier berubah (dan tidak null/kosong)
    // LaunchedEffect dijalankan saat komposisi awal dan saat key berubah
    LaunchedEffect(userData?.identifier) {
        val nim = userData?.identifier
        if (!nim.isNullOrBlank()) {
            // Generate di background thread
            barcodeBitmap = withContext(Dispatchers.Default) {
                // Sesuaikan ukuran barcode jika perlu
                BarcodeUtils.generateBarcodeBitmap(nim, BarcodeFormat.CODE_128, 600, 150)
            }
        } else {
            barcodeBitmap = null // Reset jika NIM tidak ada
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KTM Digital") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp), // Padding sekeliling
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (userData == null) {
                // Tampilkan pesan jika data user tidak ditemukan
                Text("Gagal memuat data KTM. Silakan coba lagi.")
            } else {
                // --- Tampilan Kartu KTM ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Tinggi bisa dibuat fixed atau wrap content
                        .height(230.dp), // Sesuaikan tinggi kartu
                    shape = RoundedCornerShape(12.dp), // Sudut agak bulat
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00695C)) // Warna Hijau UMI (perkiraan)
                ) {
                    Column { // Column utama dalam Card
                        // Baris Atas: Logo & Judul Universitas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.umi_logo), // Ganti dengan logo UMI Anda
                                contentDescription = "Logo Universitas",
                                modifier = Modifier.size(40.dp) // Ukuran logo
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "KARTU TANDA MAHASISWA",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall, // Ukuran kecil
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "UNIVERSITAS MUSLIM INDONESIA",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium, // Ukuran label
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } // Akhir Row Atas

                        Divider(color = Color.White.copy(alpha = 0.5f)) // Garis pemisah

                        // Baris Tengah: Foto & Data Teks
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.Top // Rata atas
                        ) {
                            // Foto Mahasiswa
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = userData.foto_profile,
                                    placeholder = painterResource(id = R.drawable.placeholder_image), // Placeholder Anda
                                    error = painterResource(id = R.drawable.placeholder_image)
                                ),
                                contentDescription = "Foto Profil ${userData.user_name}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(width = 80.dp, height = 100.dp) // Ukuran foto KTM
                                    .clip(RoundedCornerShape(4.dp)) // Sedikit rounded
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Kolom Data Teks
                            Column(modifier = Modifier.weight(1f)) {
                                // NIM
                                Text(userData.identifier, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                // Nama
                                Text(userData.user_name.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) // Nama huruf besar
                                // Email (derivasi dari NIM)
                                val email = "${userData.identifier}@umi.ac.id" // Asumsi format email
                                Text(email, color = Color.White, fontSize = 12.sp)
                                // Barcode (di bawahnya)
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp) // Tinggi area barcode
                                    .background(Color.White) // Background putih untuk barcode
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ){
                                    if (barcodeBitmap != null) {
                                        Image(
                                            bitmap = barcodeBitmap!!.asImageBitmap(),
                                            contentDescription = "Barcode NIM ${userData.identifier}",
                                            modifier = Modifier.fillMaxSize(), // Isi Box
                                            contentScale = ContentScale.FillBounds // Penuhi area barcode
                                        )
                                    } else {
                                        // Tampilkan loading atau placeholder jika barcode belum siap/gagal
                                        Text("...", color = Color.Gray, fontSize = 10.sp)
                                    }
                                }
                                // NIM di bawah barcode
                                Text(
                                    userData.identifier,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 8.sp, // Ukuran sangat kecil
                                    modifier = Modifier.align(Alignment.End) // Rata kanan
                                )
                                // Fakultas
                                Text(
                                    "Fakultas Ilmu Komputer", // Ambil dari data user
                                    color = Color.White,
                                    fontSize = 10.sp, // Ukuran kecil
                                    modifier = Modifier.padding(top = 2.dp)
                                )


                            }
                        } // Akhir Row Tengah
                    } // Akhir Column Card
                } // Akhir Card KTM
            } // Akhir Else (jika user data ada)
        } // Akhir Column Utama
    } // Akhir Scaffold
}