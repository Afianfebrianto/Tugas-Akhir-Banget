package com.afian.tugasakhir.View.Screen.mahasiswa

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afian.tugasakhir.Component.CardButtonBarMhs
import com.afian.tugasakhir.Component.DosenList
import com.afian.tugasakhir.Component.Header
import com.afian.tugasakhir.Component.ProfilePromptDialog
import com.afian.tugasakhir.Component.TopDosenLeaderboard
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.Controller.PeringkatDosenViewModel
import com.afian.tugasakhir.Controller.Screen
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

@Composable
fun ScreenMhs() {
    Text("Mahasiswa")
}

@Composable
fun HomeMhsScreen(loginViewModel: LoginViewModel,navController: NavController, dosenViewModel : DosenViewModel, peringkatViewModel: PeringkatDosenViewModel = viewModel()) {
    // Ambil data pengguna dari ViewModel
    val user = loginViewModel.getUserData()

    // Jika user tidak ada, tampilkan pesan atau tampilan default
    val username = user?.user_name ?: "Guest"
    val identifier = user?.identifier ?: "No Identifier"
    val fotoProfile = user?.foto_profile ?: ""
    val context = LocalContext.current

    // --- Ambil State Peringkat dari ViewModel Peringkat ---
    val peringkatList by peringkatViewModel.peringkatList.collectAsState()
    val isLoadingPeringkat by peringkatViewModel.isLoading
    val errorPeringkat by peringkatViewModel.errorMessage
    val selectedMonthPeringkat by peringkatViewModel.selectedMonth
    val selectedYearPeringkat by peringkatViewModel.selectedYear

    // Tambahkan log untuk mencetak informasi pengguna
    Log.d("HomeMhsScreen", "Username: $username")
    Log.d("HomeMhsScreen", "Identifier: $identifier")
    Log.d("HomeMhsScreen", "Foto Profile: $fotoProfile")

    // Minta izin notifikasi di Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    Log.i("HomeMhsScreen", "Notification permission granted.")
                } else {
                    Log.w("HomeMhsScreen", "Notification permission denied.")
                    // Beri tahu user mengapa izin ini berguna (opsional)
                }
            }
        )

        LaunchedEffect(Unit) {
            // Cek izin saat layar pertama kali muncul
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("HomeMhsScreen", "Requesting notification permission...")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // --- 👇 State & Efek untuk Dialog Prompt Profil 👇 ---
    var showProfileDialog by remember { mutableStateOf(false) }
    // Gunakan Unit sebagai key agar hanya cek sekali saat user data pertama kali valid
    LaunchedEffect(key1 = user) {
        if (user != null) {
            // Cek apakah profil perlu dilengkapi
            val needsUpdate = user.foto_profile.isNullOrBlank() || user.no_hp.isNullOrBlank()
            if (needsUpdate) {
                Log.d("HomeMahasiswaScreen", "Profile needs update (Photo: ${user.foto_profile}, HP: ${user.no_hp}). Showing prompt.")
                showProfileDialog = true // Tampilkan dialog jika perlu update
            } else {
                Log.d("HomeMahasiswaScreen", "Profile seems complete. No prompt needed.")
            }
        }
    }

    // --- Tampilkan Dialog Secara Kondisional ---
    if (showProfileDialog) {
        ProfilePromptDialog(
            onDismissRequest = { showProfileDialog = false }, // Tutup dialog
            onConfirm = {
                showProfileDialog = false // Tutup dialog
                // Navigasi ke layar edit profil
                // Pastikan route dan cara passing argumen sesuai definisi NavGraph Anda
                if (user?.identifier != null) {
                    // Asumsi route = "profile_edit/{identifier}/{role}"
                    val route = Screen.UserProfileEdit.route // Ambil route dari sealed class
                        .replace("{identifier}", user.identifier)
                        .replace("{role}", user.role) // Kirim role juga
                    navController.navigate(route)
                } else {
                    Log.e("HomeMahasiswaScreen", "Cannot navigate to edit profile, user identifier is null")
                    // Tampilkan Toast error?
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF1E3E62))
            .padding(top = 16.dp)
    ) {
        Header(username, identifier, fotoProfile) // Panggil Header dengan data pengguna
        Card(modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                CardButtonBarMhs(navController)
                TopDosenLeaderboard(
                    modifier = Modifier.padding(horizontal = 8.dp), // Beri padding section
                    // Teruskan state dari peringkatViewModel sebagai parameter
                    peringkatList = peringkatList,
                    isLoading = isLoadingPeringkat,
                    errorMessage = errorPeringkat,
                    selectedMonth = selectedMonthPeringkat,
                    selectedYear = selectedYearPeringkat
                )
                DosenList(modifier = Modifier .padding(bottom = 10.dp) ,navController,dosenViewModel)
            }
        }
    }
}