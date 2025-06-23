package com.afian.tugasakhir.View.Screen.HelperScreen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afian.tugasakhir.Controller.UserAddViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items // Untuk menampilkan list error
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAddScreen(
    navController: NavController,
    viewModel: UserAddViewModel = viewModel()
) {
    val context = LocalContext.current

    // State dari ViewModel
    val selectedFileName by viewModel.selectedFileName
    val isUploading by viewModel.isUploading
    val uploadResult by viewModel.uploadResult
    val errorMessage by viewModel.errorMessage

    // Launcher untuk memilih file Excel
//    val filePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent() // Kontrak standar untuk pilih file
//    ) { uri: Uri? ->
//        viewModel.onFileSelected(uri, context) // Teruskan URI ke ViewModel
//    }

    // === 👇 UBAH KONTRAK DI SINI 👇 ===
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument() // <-- Gunakan OpenDocument
    ) { uri: Uri? ->
        // Callback tetap sama, menerima URI
        viewModel.onFileSelected(uri, context)
    }
// === 👆 AKHIR PERUBAHAN KONTRAK 👆 ===

    // Menampilkan pesan error umum (misal jaringan) dengan Toast
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            // Reset error di ViewModel mungkin? viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(elevation = 10.dp),
                title = { Text("Tambah User Massal (Excel)") },
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
                .padding(16.dp), // Padding sekeliling konten
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Jarak antar elemen
        ) {
            Text(
                "Pilih file Excel (.xlsx atau .xls) dengan kolom: no_identitas, nama, jenis_kelamin (L/P), no_hp (opsional), informasi (opsional), role (dosen/mhs)",
                style = MaterialTheme.typography.bodyMedium
            )

            // Tombol Pilih File
            Button(onClick = {
                // Luncurkan file picker, tentukan tipe MIME
//                filePickerLauncher.launch(
//                    arrayOf(
//                        "application/vnd.ms-excel", // .xls
//                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // .xlsx
//                    ).joinToString(",") // Beberapa tipe MIME dipisah koma
//                )
                filePickerLauncher.launch(
                    arrayOf( // <-- Kirim sebagai Array String
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                )
            }) {
                Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Pilih File Excel")
            }

            // Tampilkan nama file yang dipilih
            Text(
                text = selectedFileName ?: "Belum ada file dipilih",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol Upload & Proses
            Button(
                onClick = { viewModel.uploadExcelFile(context) },
                enabled = !isUploading && selectedFileName != null // Aktif jika file dipilih & tdk sdg upload
            ) {
                if (isUploading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Mengupload...")
                    }
                } else {
                    Text("Upload & Proses File")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Tampilkan Hasil Upload ---
            uploadResult?.let { result ->
                Divider()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Hasil Proses:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    Text(
                        text = result.message,
                        color = if (result.success && result.errors.isNullOrEmpty()) Color.Green else MaterialTheme.colorScheme.onSurface
                    )
                    // Tampilkan daftar error jika ada
                    if (!result.errors.isNullOrEmpty()) {
                        Text(
                            text = "Detail Error/Skipped:",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        // Gunakan LazyColumn jika error bisa sangat banyak
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) { // Batasi tinggi
                            items(result.errors) { errorMsg ->
                                Text(
                                    text = "- $errorMsg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            // --- Akhir Hasil Upload ---

        } // Akhir Column konten
    } // Akhir Scaffold
}