package com.afian.tugasakhir.View.Screen.HelperScreen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afian.tugasakhir.Component.MahasiswaItemPanggil
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Controller.LoginViewModel
import androidx.compose.runtime.getValue // <-- TAMBAHKAN IMPORT INI
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.afian.tugasakhir.Component.MyCustomTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DosenPanggilMahasiswaScreen(
    navController: NavController,
    dosenViewModel: DosenViewModel = viewModel(),
    loginViewModel: LoginViewModel // Teruskan LoginViewModel untuk dapat ID dosen
) {
    val context = LocalContext.current
    val dosenUserId = loginViewModel.getUserData()?.user_id ?: -1 // Ambil ID dosen yg login

    // State dari ViewModel
    val allMahasiswa by dosenViewModel.filteredAllMahasiswaList.collectAsState()
    val searchQuery by dosenViewModel.searchQueryMahasiswa.collectAsState()
    val isLoading by dosenViewModel.isLoadingMahasiswa
    val errorMessage by dosenViewModel.errorMahasiswa
    val panggilStatus by dosenViewModel.panggilStatus // Pair<Int?, String?>

    // Tampilkan pesan status panggilan (misal: Toast) saat berubah
    LaunchedEffect(panggilStatus) {
        panggilStatus.second?.let { message ->
            if (message.isNotEmpty() && message != "Memanggil...") { // Tampilkan saat selesai/error
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                // Anda bisa reset status di ViewModel setelah ditampilkan jika mau
            }
        }
    }


    Scaffold(
        topBar = {
            MyCustomTopAppBar(title = "Panggil Mahasiswa", navController)
//            TopAppBar(
//                title = { Text("Panggil Mahasiswa") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.navigateUp() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
//                    }
//                }
//            )

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp), // Padding di luar Card
                shape = RoundedCornerShape(20.dp), // Bentuk Card
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Elevation untuk Card
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface // Warna latar Card, bisa diganti
                )
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        dosenViewModel.onMahasiswaSearchQueryChanged(it) // Panggil lambda dari ViewModel
                        // Jika menggunakan state lokal:
                        // searchQuery = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), // Padding di dalam Card, antara tepi Card dan TextField
                    placeholder = {
                        Text(
                            "Cari Dosen (Nama/NIDN)",
                            // fontFamily = poppinsFamily
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search Icon")
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors( // Warna TextField diatur transparan agar warna Card terlihat
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.onSurface, // Sesuaikan warna kursor dengan tema
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface, // Warna teks
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant // Warna teks placeholder
                    )
                )
            }

            // Tampilkan Loading atau Error
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Text(
                    "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            // Tampilkan List Mahasiswa
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp), // Padding bawah list
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (allMahasiswa.isEmpty()) {
                        item {
                            Text(
                                if (searchQuery.isBlank()) "Tidak ada data mahasiswa." else "Mahasiswa \"$searchQuery\" tidak ditemukan.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(
                            count = allMahasiswa.size, // <-- Berikan JUMLAH item
                            key = { index ->
                                // Key berdasarkan identifier item pada indeks itu (pakai getOrNull)
                                allMahasiswa.getOrNull(index)?.identifier ?: index
                            }
                        ) { index -> // <-- Lambda ini sekarang menerima INDEKS (Int)
                            // Ambil objek mahasiswa asli dari list menggunakan indeks
                            val mahasiswa = allMahasiswa.getOrNull(index)
                            // Pastikan objek tidak null sebelum digunakan
                            if (mahasiswa != null) {
                                // Sekarang teruskan objek 'mahasiswa' ke item Anda
                                MahasiswaItemPanggil(
                                    mahasiswa = mahasiswa, // Tipe data di sini sudah benar (Dosen)
                                    isCalling = panggilStatus.first == mahasiswa.user_id, // Akses properti dari objek 'mahasiswa'
                                    onPanggilClick = {
                                        if (dosenUserId != -1) {
                                            dosenViewModel.requestPanggilMahasiswa(dosenUserId, mahasiswa.user_id) // Akses properti dari objek 'mahasiswa'
                                        } else {
                                            Toast.makeText(context, "ID Dosen tidak valid.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                                // Divider bisa ditambahkan di sini jika mau
                                // Divider()
                            } else {
                                // Log jika item tiba-tiba null (jarang terjadi)
                                Log.w("DosenPanggilMhsScreen", "Item mahasiswa null di index $index")
                            }
                        }
                    }
                } // Akhir LazyColumn
            } // Akhir Else
        } // Akhir Column
    } // Akhir Scaffold
}