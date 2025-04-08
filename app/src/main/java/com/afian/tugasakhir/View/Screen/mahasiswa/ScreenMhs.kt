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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.afian.tugasakhir.Component.CardButtonBar
import com.afian.tugasakhir.Component.DosenList
import com.afian.tugasakhir.Component.Header
import com.afian.tugasakhir.Controller.LoginViewModel

@Composable
fun ScreenMhs() {
    Text("Mahasiswa")
}

@Composable
fun HomeMhsScreen(loginViewModel: LoginViewModel,navController: NavController) {
    // Ambil data pengguna dari ViewModel
    val user = loginViewModel.getUserData()

    // Jika user tidak ada, tampilkan pesan atau tampilan default
    val username = user?.user_name ?: "Guest"
    val identifier = user?.identifier ?: "No Identifier"
    val fotoProfile = user?.foto_profile ?: ""
    val context = LocalContext.current

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
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                CardButtonBar(navController)
                DosenList()
            }
        }
    }
}