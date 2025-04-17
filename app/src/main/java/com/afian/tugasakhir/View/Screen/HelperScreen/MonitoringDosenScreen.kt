package com.afian.tugasakhir.View.Screen.HelperScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afian.tugasakhir.Component.PeringkatDosenItem
import com.afian.tugasakhir.Controller.PeringkatDosenViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // Untuk Dropdown dan FlowRow
@Composable
fun MonitoringDosenScreen(
    navController: NavController,
    viewModel: PeringkatDosenViewModel = viewModel()
) {
    val peringkatList by viewModel.peringkatList.collectAsState()
    val selectedMonth by viewModel.selectedMonth
    val selectedYear by viewModel.selectedYear
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    // Opsi untuk dropdown bulan dan tahun
    val months = remember { (1..12).map { Pair(it, getMonthName(it)) } } // List Pair(angka, namaBulan)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = remember { (currentYear - 5..currentYear + 1).toList() } // Contoh: 5 tahun ke belakang + 1 ke depan

    var monthMenuExpanded by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Peringkat Durasi Dosen") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                actions = { // Filter bulan dan tahun di sini
                    // Dropdown Bulan
                    ExposedDropdownMenuBox(
                        expanded = monthMenuExpanded,
                        onExpandedChange = { monthMenuExpanded = !monthMenuExpanded },
                        modifier = Modifier.width(150.dp) // Atur lebar dropdown
                    ) {
                        // Tampilkan bulan terpilih (konversi angka ke nama)
                        OutlinedTextField(
                            value = getMonthName(selectedMonth),
                            onValueChange = {}, // Tidak bisa diedit langsung
                            readOnly = true,
                            label = { Text("Bulan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthMenuExpanded) },
                            modifier = Modifier.menuAnchor(), // Anchor untuk menu
                            textStyle = MaterialTheme.typography.bodySmall // Ukuran teks lebih kecil
                        )
                        ExposedDropdownMenu(
                            expanded = monthMenuExpanded,
                            onDismissRequest = { monthMenuExpanded = false }
                        ) {
                            months.forEach { (monthNumber, monthName) ->
                                DropdownMenuItem(
                                    text = { Text(monthName) },
                                    onClick = {
                                        viewModel.onMonthSelected(monthNumber)
                                        monthMenuExpanded = false // Tutup menu
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Dropdown Tahun
                    ExposedDropdownMenuBox(
                        expanded = yearMenuExpanded,
                        onExpandedChange = { yearMenuExpanded = !yearMenuExpanded },
                        modifier = Modifier.width(120.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedYear.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tahun") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearMenuExpanded) },
                            modifier = Modifier.menuAnchor(),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        ExposedDropdownMenu(
                            expanded = yearMenuExpanded,
                            onDismissRequest = { yearMenuExpanded = false }
                        ) {
                            years.forEach { yearValue ->
                                DropdownMenuItem(
                                    text = { Text(yearValue.toString()) },
                                    onClick = {
                                        viewModel.onYearSelected(yearValue)
                                        yearMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp)) // Sedikit padding di ujung
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp) // Padding konten
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
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (peringkatList.isEmpty()) {
                        item {
                            Text(
                                "Tidak ada data peringkat untuk periode ${getMonthName(selectedMonth)} $selectedYear.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        itemsIndexed( // Gunakan itemsIndexed untuk mendapatkan index sebagai rank
                            items = peringkatList,
                            key = { _, item -> item.user_id } // Key unik
                        ) { index, peringkatItem ->
                            PeringkatDosenItem(
                                item = peringkatItem,
                                rank = index + 1 // Rank dimulai dari 1
                            )
                        }
                    }
                } // Akhir LazyColumn
            } // Akhir Else
        } // Akhir Box
    } // Akhir Scaffold
}

// Fungsi helper nama bulan (bisa ditaruh di file utilitas)
private fun getMonthName(monthNumber: Int): String {
    val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    return if (monthNumber in 1..12) months[monthNumber - 1] else "Invalid Month"
}