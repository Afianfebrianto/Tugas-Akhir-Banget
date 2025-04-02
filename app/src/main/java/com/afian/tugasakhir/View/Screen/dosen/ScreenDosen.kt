package com.afian.tugasakhir.View.Screen.dosen

import android.Manifest
import android.util.Log
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.afian.tugasakhir.Component.CardButtonBar
import com.afian.tugasakhir.Component.DosenList
import com.afian.tugasakhir.Component.Header
import com.afian.tugasakhir.Controller.GeofenceMonitorEffect
import com.afian.tugasakhir.Controller.LoginViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices

@Composable
fun ScreenDosen() {
    Text("Dosen")
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeDosenScreen(loginViewModel: LoginViewModel, navController: NavController) {
    // Ambil data pengguna dari ViewModel
    val user = loginViewModel.getUserData()

    // Jika user tidak ada, tampilkan pesan atau tampilan default
    val username = user?.user_name ?: "Guest"
    val identifier = user?.identifier ?: "No Identifier"
    val fotoProfile = user?.foto_profile ?: ""

    // --- Integrasi Geofence ---
    // Panggil GeofenceMonitorEffect di sini. Ia akan berjalan selama HomeDosenScreen ada dalam komposisi.
    // Poligon default dan callback logging internal akan digunakan.
    GeofenceMonitorEffect(
        // Anda bisa override polygon atau onStatusChange di sini jika perlu
        // polygon = customPolygon, // Jika punya poligon lain
        onStatusChange = { status, isDwelling ->
            // Callback ini akan dipanggil saat status geofence berubah.
            // Saat ini, GeofenceMonitorEffect sudah melakukan logging internal.
            // Anda bisa tambahkan logika di sini jika ingin HomeDosenScreen
            // bereaksi terhadap perubahan status (misal: update UI).
            // Contoh:
            // currentGeofenceStatusState.value = status
            // isUserDwellingState.value = isDwelling
            println("HomeDosenScreen Callback: Status = $status, Dwelling = $isDwelling") // Contoh print dari screen
        }
    )
    // --- Akhir Integrasi Geofence ---
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