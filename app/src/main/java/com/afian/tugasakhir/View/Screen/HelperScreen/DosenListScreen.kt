package com.afian.tugasakhir.View.Screen.HelperScreen

// Di file Composable Screen Anda (misal: HomeDosenScreen.kt atau DosenListScreen.kt)

// ... (Import lain)
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn // Import LazyColumn
import androidx.compose.foundation.lazy.items // Import items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator // Untuk loading indicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afian.tugasakhir.Component.DosenDetailDialog
import com.afian.tugasakhir.Component.DosenItem // Import DosenItem
import com.afian.tugasakhir.Component.MyCustomTopAppBar
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Model.Dosen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombinedDosenListScreen(
    viewModel: DosenViewModel = viewModel(),
    navController: NavController // <-- Tambahkan NavController sebagai parameter
) {
    // Ambil state list yang SUDAH DIFILTER
    val dosenOnCampus by viewModel.filteredDosenList.collectAsState()
    val dosenNotInCampus by viewModel.filteredDosenNotInCampusList.collectAsState()

    // Ambil state query pencarian
    val searchQuery by viewModel.searchQuery.collectAsState()
//    var searchQuery by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    // State untuk dialog detail dosen
    var selectedDosen by remember { mutableStateOf<Dosen?>(null) }

    Scaffold(
        topBar = {
            MyCustomTopAppBar(title = "Informasi Dosen", navController = navController)
        }
    ) { innerPadding -> // Ambil padding dari Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Terapkan padding Scaffold ke Column
                .padding(horizontal = 8.dp) // Padding kiri kanan utama
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
                        viewModel.onSearchQueryChanged(it) // Panggil lambda dari ViewModel
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
            // --- Akhir TextField Pencarian ---

            // Tampilkan loading atau error jika ada
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { // Center loading
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            // Tampilkan list jika tidak loading dan tidak error
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(), // LazyColumn mengisi sisa ruang
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // --- Section Dosen Di Kampus ---
                    item {
                        Text(
                            text = "Dosen di Kampus (${dosenOnCampus.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                        )
                    }
                    // Tampilkan pesan jika list filter kosong tapi query tidak kosong
                    if (dosenOnCampus.isEmpty() && searchQuery.isNotEmpty()) {
                        item { Text("Dosen \"${searchQuery}\" tidak ditemukan di kampus.", modifier = Modifier.padding(horizontal=8.dp, vertical = 4.dp)) }
                    } else if (dosenOnCampus.isEmpty() && searchQuery.isEmpty()) {
                        item { Text("Tidak ada dosen di kampus saat ini.", modifier = Modifier.padding(horizontal=8.dp, vertical = 4.dp)) }
                    } else {
                        items(
                            items = dosenOnCampus,
                            key = { dosen -> "on_${dosen.identifier}" }
                        ) { dosen ->
                            DosenItem(
                                dosen = dosen,
                                onClick = { selectedDosen = it }
                            )
                        }
                    }


                    // --- Section Dosen Tidak Di Kampus ---
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Dosen Tidak di Kampus (${dosenNotInCampus.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                        )
                    }
                    // Tampilkan pesan jika list filter kosong tapi query tidak kosong
                    if (dosenNotInCampus.isEmpty() && searchQuery.isNotEmpty()) {
                        item { Text("Dosen \"${searchQuery}\" tidak ditemukan (tidak di kampus).", modifier = Modifier.padding(horizontal=8.dp, vertical = 4.dp)) }
                    } else if (dosenNotInCampus.isEmpty() && searchQuery.isEmpty()) {
                        item { Text("Semua dosen berada di kampus.", modifier = Modifier.padding(horizontal=8.dp, vertical = 4.dp)) }
                    } else {
                        items(
                            items = dosenNotInCampus,
                            key = { dosen -> "off_${dosen.identifier}" }
                        ) { dosen ->
                            DosenItem(
                                dosen = dosen,
                                onClick = { selectedDosen = it }
                            )
                        }
                    }
                } // Akhir LazyColumn
            } // Akhir else (jika tidak loading/error)
        } // Akhir Column utama
    } // Akhir Scaffold

    // Tampilkan Dialog (logika tetap sama)
    selectedDosen?.let { dosenData ->
        DosenDetailDialog(
            dosen = dosenData,
            onDismissRequest = { selectedDosen = null },
            dosenOnCampusList = dosenOnCampus,         // <-- Teruskan list dosen di kampus
            dosenNotInCampusList = dosenNotInCampus  // <-- Teruskan list dosen tidak di kampus
        )
    }
} // Akhir Composable Screen