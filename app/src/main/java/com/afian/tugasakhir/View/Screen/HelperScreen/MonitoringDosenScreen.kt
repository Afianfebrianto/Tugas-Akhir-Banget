package com.afian.tugasakhir.View.Screen.HelperScreen

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Component.MonthYearPickerDialog
import com.afian.tugasakhir.Component.PeringkatDosenItem
import com.afian.tugasakhir.Controller.PeringkatDosenViewModel
import com.afian.tugasakhir.R
import java.util.Calendar


// Fungsi helper nama bulan (jika belum ada / di file lain)
private fun getMonthName(monthNumber: Int): String {
    val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    return if (monthNumber in 1..12) months[monthNumber - 1] else "Bulan Invalid"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringDosenScreen(
    navController: NavController,
    viewModel: PeringkatDosenViewModel = viewModel()
) {
    val context = LocalContext.current

    // State dari ViewModel
    val peringkatList by viewModel.peringkatList.collectAsState()
    val selectedMonth by viewModel.selectedMonth
    val selectedYear by viewModel.selectedYear
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    // State BARU untuk kontrol visibilitas dialog
    var showPickerDialog by remember { mutableStateOf(false) }

    // Opsi tahun untuk dialog
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = remember { (currentYear - 5..currentYear + 1).toList() } // Contoh range tahun

    // Tampilkan Dialog jika showPickerDialog true
    if (showPickerDialog) {
        MonthYearPickerDialog(
            initialMonth = selectedMonth,
            initialYear = selectedYear,
            yearRange = years,
            onDismissRequest = { showPickerDialog = false }, // Tutup saat batal/klik luar
            onConfirm = { month, year ->
                // Panggil fungsi ViewModel yang sudah diupdate
                viewModel.updatePeriod(month, year)
                showPickerDialog = false // Tutup dialog setelah konfirmasi
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(elevation = 10.dp),
                title = { Text("Durasi Kehadiran Dosenn") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                // --- 👇 ACTIONS SEKARANG HANYA TOMBOL DOWNLOAD 👇 ---
                actions = {
                    IconButton(onClick = {
                        // 1. (Opsional) Set state loading UI = true (misal untuk tombol)
                        // viewModel.setIsDownloading(true)

                        val baseUrl = RetrofitClient.getBaseUrl // Dapatkan Base URL
                        val monthString = selectedMonth.toString().padStart(2, '0')
                        val fileName = "Laporan_Kehadiran_Dosen_${selectedYear}_${monthString}.xlsx"
                        val downloadUrl = "${baseUrl}api/users/reports/dosen-durasi-harian.xlsx?month=${selectedMonth}&year=${selectedYear}"

                        try {
                            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                                .setTitle(fileName) // Judul di notifikasi download
                                .setDescription("Mengunduh laporan durasi dosen")
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Tampilkan notif progress & selesai
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName) // Simpan ke folder Downloads publik
                                .setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                .setAllowedOverMetered(true) // Izinkan download via data seluler
                                .setAllowedOverRoaming(true)

                            // Tambahkan header jika API perlu otentikasi
                            // request.addRequestHeader("Authorization", "Bearer YOUR_TOKEN")

                            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            val downloadId = downloadManager.enqueue(request) // Mulai download & dapatkan ID

                            Log.i("MonitoringDosen", "Download enqueued with ID: $downloadId")
                            Toast.makeText(context, "Download dimulai...", Toast.LENGTH_SHORT).show()

                        } catch (e: Exception) {
                            Log.e("MonitoringDosen", "Error starting download", e)
                            Toast.makeText(context, "Gagal memulai download.", Toast.LENGTH_SHORT).show()
                        } finally {
                            // 2. Set state loading UI = false (proses enqueue selesai)
                            // viewModel.setIsDownloading(false)
                        }
//                        // Logika untuk download report Excel
//                        val baseUrl = try {
//                            // Cara mendapatkan Base URL bisa bervariasi
//                            // Ambil dari konstanta, BuildConfig, atau RetrofitClient
//                            RetrofitClient.getBaseUrl // Asumsi ada fungsi/konstanta ini
//                        } catch (e: Exception) {
//                            Log.e("MonitoringDosen", "Gagal mendapatkan Base URL, gunakan default", e)
//                            "http://192.168.1.8:3000" // <-- GANTI DENGAN DEFAULT YANG BENAR
//                        }
//
//                        // Pastikan endpoint dan nama file SAMA dengan di backend
//                        val downloadUrl = "${baseUrl}api/users/reports/dosen-durasi-harian.xlsx?month=${selectedMonth}&year=${selectedYear}"
//                        Log.i("MonitoringDosen", "Mencoba download report dari: $downloadUrl")
//
//                        // Buat Intent untuk membuka URL -> memicu download
//                        val intent = Intent(Intent.ACTION_VIEW).apply {
//                            data = Uri.parse(downloadUrl)
//                        }
//                        try {
//                            context.startActivity(intent)
//                        } catch (e: ActivityNotFoundException) {
//                            Log.e("MonitoringDosen", "Tidak ditemukan aplikasi browser/downloader.")
//                            Toast.makeText(context, "Tidak ada aplikasi untuk menangani download.", Toast.LENGTH_LONG).show()
//                        } catch (e: Exception) {
//                            Log.e("MonitoringDosen", "Error saat memulai intent download", e)
//                            Toast.makeText(context, "Gagal memulai download.", Toast.LENGTH_SHORT).show()
//                        }
                    }) {
                        Icon(painter = painterResource(R.drawable.ic_download_24), contentDescription = "Download Laporan Excel")
                    }
                }
                // --- 👆 AKHIR ACTIONS 👆 ---
            )
        },
        // --- 👇 FLOATING ACTION BUTTON (FAB) BARU 👇 ---
        floatingActionButton = {
            FloatingActionButton(onClick = { showPickerDialog = true }) { // Buka dialog
                Icon(painter = painterResource(R.drawable.ic_edit_calendar_24), contentDescription = "Pilih Periode")
            }
        }
        // --- 👆 AKHIR FAB 👆 ---

    ) { innerPadding -> // Content Scaffold

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) { // Column konten utama

            // Tampilkan Periode Terpilih di atas list
            Text(
                text = "Periode: ${getMonthName(selectedMonth)} $selectedYear",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp), // Padding
                textAlign = TextAlign.Center // Tengahkan
            )

            // Box untuk menampung Loading/Error/List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ambil sisa ruang setelah Text periode
                    .padding(horizontal = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (errorMessage != null) {
                    Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp), // Padding bawah saja
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (peringkatList.isEmpty()) {
                            item { Text("Tidak ada data peringkat untuk periode terpilih.", modifier = Modifier.padding(16.dp)) }
                        } else {
                            itemsIndexed(
                                items = peringkatList,
                                key = { _, item -> item.user_id }
                            ) { index, peringkatItem ->
                                PeringkatDosenItem(item = peringkatItem, rank = index + 1)
                            }
                        }
                    } // Akhir LazyColumn
                } // Akhir Else
            } // Akhir Box konten list
        } // Akhir Column konten utama
    } // Akhir Scaffold
}




//@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // Untuk Dropdown dan FlowRow
//@Composable
//fun MonitoringDosenScreen(
//    navController: NavController,
//    viewModel: PeringkatDosenViewModel = viewModel()
//) {
//
//    val peringkatList by viewModel.peringkatList.collectAsState()
//    val selectedMonth by viewModel.selectedMonth
//    val selectedYear by viewModel.selectedYear
//    val isLoading by viewModel.isLoading
//    val errorMessage by viewModel.errorMessage
//
//    // Opsi untuk dropdown bulan dan tahun
//    val months = remember { (1..12).map { Pair(it, getMonthName(it)) } } // List Pair(angka, namaBulan)
//    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
//    val years = remember { (currentYear - 5..currentYear + 1).toList() } // Contoh: 5 tahun ke belakang + 1 ke depan
//
//    var monthMenuExpanded by remember { mutableStateOf(false) }
//    var yearMenuExpanded by remember { mutableStateOf(false) }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Peringkat Durasi Dosen") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.navigateUp() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
//                    }
//                },
//                actions = { // Filter bulan dan tahun di sini
//                    // Dropdown Bulan
//                    ExposedDropdownMenuBox(
//                        expanded = monthMenuExpanded,
//                        onExpandedChange = { monthMenuExpanded = !monthMenuExpanded },
//                        modifier = Modifier.width(150.dp) // Atur lebar dropdown
//                    ) {
//                        // Tampilkan bulan terpilih (konversi angka ke nama)
//                        OutlinedTextField(
//                            value = getMonthName(selectedMonth),
//                            onValueChange = {}, // Tidak bisa diedit langsung
//                            readOnly = true,
//                            label = { Text("Bulan") },
//                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthMenuExpanded) },
//                            modifier = Modifier.menuAnchor(), // Anchor untuk menu
//                            textStyle = MaterialTheme.typography.bodySmall // Ukuran teks lebih kecil
//                        )
//                        ExposedDropdownMenu(
//                            expanded = monthMenuExpanded,
//                            onDismissRequest = { monthMenuExpanded = false }
//                        ) {
//                            months.forEach { (monthNumber, monthName) ->
//                                DropdownMenuItem(
//                                    text = { Text(monthName) },
//                                    onClick = {
//                                        viewModel.onMonthSelected(monthNumber)
//                                        monthMenuExpanded = false // Tutup menu
//                                    }
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    // Dropdown Tahun
//                    ExposedDropdownMenuBox(
//                        expanded = yearMenuExpanded,
//                        onExpandedChange = { yearMenuExpanded = !yearMenuExpanded },
//                        modifier = Modifier.width(120.dp)
//                    ) {
//                        OutlinedTextField(
//                            value = selectedYear.toString(),
//                            onValueChange = {},
//                            readOnly = true,
//                            label = { Text("Tahun") },
//                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearMenuExpanded) },
//                            modifier = Modifier.menuAnchor(),
//                            textStyle = MaterialTheme.typography.bodySmall
//                        )
//                        ExposedDropdownMenu(
//                            expanded = yearMenuExpanded,
//                            onDismissRequest = { yearMenuExpanded = false }
//                        ) {
//                            years.forEach { yearValue ->
//                                DropdownMenuItem(
//                                    text = { Text(yearValue.toString()) },
//                                    onClick = {
//                                        viewModel.onYearSelected(yearValue)
//                                        yearMenuExpanded = false
//                                    }
//                                )
//                            }
//                        }
//                    }
//                    Spacer(modifier = Modifier.width(8.dp)) // Sedikit padding di ujung
//                }
//            )
//        }
//    ) { innerPadding ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(horizontal = 8.dp) // Padding konten
//        ) {
//            if (isLoading) {
//                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//            } else if (errorMessage != null) {
//                Text(
//                    text = "Error: $errorMessage",
//                    color = MaterialTheme.colorScheme.error,
//                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
//                )
//            } else {
//                LazyColumn(
//                    modifier = Modifier.fillMaxSize(),
//                    contentPadding = PaddingValues(vertical = 8.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    if (peringkatList.isEmpty()) {
//                        item {
//                            Text(
//                                "Tidak ada data peringkat untuk periode ${getMonthName(selectedMonth)} $selectedYear.",
//                                modifier = Modifier.padding(16.dp)
//                            )
//                        }
//                    } else {
//                        itemsIndexed( // Gunakan itemsIndexed untuk mendapatkan index sebagai rank
//                            items = peringkatList,
//                            key = { _, item -> item.user_id } // Key unik
//                        ) { index, peringkatItem ->
//                            PeringkatDosenItem(
//                                item = peringkatItem,
//                                rank = index + 1 // Rank dimulai dari 1
//                            )
//                        }
//                    }
//                } // Akhir LazyColumn
//            } // Akhir Else
//        } // Akhir Box
//    } // Akhir Scaffold
//}
//
//// Fungsi helper nama bulan (bisa ditaruh di file utilitas)
//private fun getMonthName(monthNumber: Int): String {
//    val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
//    return if (monthNumber in 1..12) months[monthNumber - 1] else "Invalid Month"
//}