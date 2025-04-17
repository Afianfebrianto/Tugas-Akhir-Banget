package com.afian.tugasakhir.View.Screen.HelperScreen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afian.tugasakhir.Component.UserRecoveryItem
import com.afian.tugasakhir.Controller.UserManagementViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRecoveryScreen(
    navController: NavController,
    viewModel: UserManagementViewModel = viewModel() // Gunakan ViewModel baru
) {
    val context = LocalContext.current

    // State dari ViewModel
    val userList by viewModel.allUserList.collectAsState()
    val isLoading by viewModel.isLoadingUsers
    val errorMessage by viewModel.errorUsers
    val recoveringUserId by viewModel.recoveringUserId
    val recoveryResult by viewModel.recoveryResult

    // Tampilkan Toast saat hasil recovery berubah
    LaunchedEffect(recoveryResult) {
        recoveryResult?.let { result ->
            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            // Reset state hasil agar toast tidak muncul lagi saat recompose
            viewModel.clearRecoveryResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recovery Akun Pengguna") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                actions = { // Tombol Refresh
                    IconButton(onClick = { viewModel.fetchAllUsers() }, enabled = !isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang Daftar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading && userList.isEmpty()) { // Tampilkan loading awal saja
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(
                    "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp), // Padding sekeliling list
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Jarak antar item
                ) {
                    if (userList.isEmpty() && !isLoading) { // Pastikan tidak loading saat tampil pesan kosong
                        item {
                            Text("Tidak ada data pengguna.", modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(
                            items = userList,
                            key = { user -> user.user_id } // ID user sebagai key
                        ) { user ->
                            UserRecoveryItem(
                                user = user,
                                isRecovering = recoveringUserId == user.user_id,
                                onRecoverClick = { viewModel.recoverUserPassword(user.user_id) }
                            )
                        }
                    }
                } // Akhir LazyColumn
            } // Akhir Else
        } // Akhir Box
    } // Akhir Scaffold
}