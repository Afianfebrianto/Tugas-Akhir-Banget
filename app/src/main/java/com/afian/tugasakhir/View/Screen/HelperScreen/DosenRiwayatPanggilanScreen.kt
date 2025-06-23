package com.afian.tugasakhir.View.Screen.HelperScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh // Import ikon refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afian.tugasakhir.Component.DosenPanggilanHistoryItem
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Controller.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DosenRiwayatPanggilanScreen(
    navController: NavController,
    dosenViewModel: DosenViewModel = viewModel(), // Gunakan DosenViewModel yang sama
    loginViewModel: LoginViewModel
) {
    val dosenUserId = loginViewModel.getUserData()?.user_id ?: -1

    // Ambil state riwayat dari ViewModel
    val historyList by dosenViewModel.dosenPanggilanHistory.collectAsState()
    val isLoading by dosenViewModel.isLoadingHistory
    val errorMessage by dosenViewModel.errorHistory

    // Panggil fetch data saat layar pertama kali muncul/dibuat
    LaunchedEffect(key1 = dosenUserId) { // Trigger ulang jika dosenUserId berubah (meski jarang)
        if (dosenUserId != -1) {
            dosenViewModel.fetchDosenHistory(dosenUserId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(elevation = 10.dp),
                title = { Text("Riwayat Panggilan Keluar") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                actions = { // Tombol Refresh
                    IconButton(onClick = {
                        if (dosenUserId != -1) {
                            dosenViewModel.fetchDosenHistory(dosenUserId)
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (historyList.isEmpty()) {
                        item {
                            Text(
                                "Anda belum pernah melakukan panggilan.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        items(
                            items = historyList,
                            key = { item -> item.panggilan_id } // ID panggilan sebagai key
                        ) { historyItem ->
                            DosenPanggilanHistoryItem(item = historyItem)
                        }
                    }
                } // Akhir LazyColumn
            } // Akhir Else
        } // Akhir Box
    } // Akhir Scaffold
}