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
import com.afian.tugasakhir.Component.TopDosenLeaderboard
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.Controller.PeringkatDosenViewModel

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
                DosenList(modifier = Modifier,navController,dosenViewModel)
            }
        }
    }
}