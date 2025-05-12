package com.afian.tugasakhir.View.Screen.dosen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afian.tugasakhir.Component.CardButtonBarDosen
import com.afian.tugasakhir.Component.DosenList
import com.afian.tugasakhir.Component.Header
import com.afian.tugasakhir.Component.PeringkatDosenItem
import com.afian.tugasakhir.Component.ProfilePromptDialog
import com.afian.tugasakhir.Component.TopDosenLeaderboard
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Service.GeofenceHelper
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.Controller.PeringkatDosenViewModel
import com.afian.tugasakhir.Controller.Screen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

// --- Helper check permission (bisa diletakkan di file utilitas) ---
private fun checkFineLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun checkBackgroundLocationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Tidak perlu izin terpisah sebelum Android Q
    }
}
// --- End Helper ---

// Opt-in tidak wajib untuk ActivityResultContracts, tapi tidak masalah jika ada
// @OptIn(ExperimentalPermissionsApi::class) // Hapus jika tidak pakai Accompanist
@Composable
fun HomeDosenScreen(loginViewModel: LoginViewModel, navController: NavController, dosenViewModel: DosenViewModel, peringkatViewModel: PeringkatDosenViewModel = viewModel()) {
    // Ambil data pengguna dari ViewModel
    val user = loginViewModel.getUserData()
    val username = user?.user_name ?: "Guest"
    val identifier = user?.identifier ?: "No Identifier"
    val fotoProfile = user?.foto_profile ?: ""

    // --- Integrasi Geofencing ---
    val context = LocalContext.current
    // State untuk melacak status izin (inisialisasi dengan kondisi saat ini)
    var fineLocationPermissionGranted by remember { mutableStateOf(checkFineLocationPermission(context)) }
    var backgroundLocationPermissionGranted by remember { mutableStateOf(checkBackgroundLocationPermission(context)) }
    // State untuk melacak apakah geofence sudah coba ditambahkan
    var geofenceSetupAttempted by remember { mutableStateOf(false) }

    // --- Ambil State Peringkat dari ViewModel Peringkat ---
    val peringkatList by peringkatViewModel.peringkatList.collectAsState()
    val isLoadingPeringkat by peringkatViewModel.isLoading
    val errorPeringkat by peringkatViewModel.errorMessage
    val selectedMonthPeringkat by peringkatViewModel.selectedMonth
    val selectedYearPeringkat by peringkatViewModel.selectedYear
    // Ambil 3 data teratas
    // --- Akhir State Peringkat ---

    // --- MULAI SETUP LOKASI AKTIF ---

    // 1. Dapatkan FusedLocationProviderClient
    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // 2. Definisikan LocationRequest (Akurasi Tinggi)
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L) // Interval 10 detik
            .setMinUpdateIntervalMillis(5000L) // Interval tercepat 5 detik
            .build()
    }
// --- 👇 State & Efek untuk Dialog Prompt Profil 👇 ---
    var showProfileDialog by remember { mutableStateOf(false) }
    // Gunakan Unit sebagai key agar hanya cek sekali saat user data pertama kali valid
    LaunchedEffect(key1 = user) {
        if (user != null) {
            // Cek apakah profil perlu dilengkapi
            val needsUpdate = user.foto_profile.isNullOrBlank() || user.no_hp.isNullOrBlank()
            if (needsUpdate) {
                Log.d("HomeDosenScreen", "Profile needs update (Photo: ${user.foto_profile}, HP: ${user.no_hp}). Showing prompt.")
                showProfileDialog = true // Tampilkan dialog jika perlu update
            } else {
                Log.d("HomeDosenScreen", "Profile seems complete. No prompt needed.")
            }
        }
    }
    // 3. Definisikan LocationCallback (Hanya untuk logging/konfirmasi)
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(
                        "HomeDosenScreen_GPS", // Tag log berbeda untuk GPS aktif
                        "GPS Update Received: Lat=${location.latitude}, Lng=${location.longitude}"
                    )
                    // Tidak perlu melakukan apa-apa lagi, tujuannya hanya agar GPS aktif
                }
            }
        }
    }
    // --- AKHIR SETUP LOKASI AKTIF ---


// --- Pastikan backgroundLocationPermissionLauncher sudah didefinisikan ---
// (Kode ini harus ada SEBELUM fineLocationPermissionLauncher atau di scope yang sama)
    val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("HomeDosenScreen", "ACCESS_BACKGROUND_LOCATION Result: $isGranted")
        backgroundLocationPermissionGranted = isGranted // Update state
        if (isGranted) {
            // Izin Background DIBERIKAN
            // Pastikan Fine Location masih ada (seharusnya masih ada jika flow normal)
            if (fineLocationPermissionGranted) {
                Log.d("HomeDosenScreen", "Background location granted. Attempting geofence setup.")
                GeofenceHelper.addGeofences(context)
                geofenceSetupAttempted = true
            } else {
                // Kasus aneh jika fine permission dicabut antara permintaan fine dan background
                Log.w("HomeDosenScreen", "Background granted, but fine location was lost? Geofence setup skipped.")
                geofenceSetupAttempted = true // Tandai sudah dicoba
            }
        } else {
            // Izin Background DITOLAK
            Log.w("HomeDosenScreen", "ACCESS_BACKGROUND_LOCATION Denied.")
            // Geofence mungkin masih bisa berjalan di foreground jika fine permission ada
            if (fineLocationPermissionGranted) {
                Log.d("HomeDosenScreen", "Background denied, attempting geofence setup for foreground use.")
                GeofenceHelper.addGeofences(context) // Tetap coba setup untuk foreground
            }
            geofenceSetupAttempted = true // Tandai sudah dicoba
        }
    }
    // Launcher untuk meminta izin FINE_LOCATION
    val fineLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("HomeDosenScreen", "ACCESS_FINE_LOCATION Result: $isGranted")
        fineLocationPermissionGranted = isGranted // Update state

        if (isGranted) {
            // Izin Fine Location DIBERIKAN
            Log.d("HomeDosenScreen", "ACCESS_FINE_LOCATION Granted.")

            // Cek apakah perlu & belum ada izin Background Location (hanya untuk Android Q/API 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !checkBackgroundLocationPermission(context)) {
                // Perlu izin background dan belum diberikan
                Log.d("HomeDosenScreen", "Requesting ACCESS_BACKGROUND_LOCATION...")
                // Langsung minta izin background
                backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                // Setup Geofence akan ditangani di callback backgroundLocationPermissionLauncher jika berhasil
                // Tidak perlu setup di sini lagi untuk kasus ini
            } else {
                // Izin Background SUDAH ADA atau TIDAK DIPERLUKAN (versi Android < Q)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    Log.d("HomeDosenScreen", "Background location permission not required for this Android version.")
                } else {
                    Log.d("HomeDosenScreen", "Background location permission already granted.")
                }
                backgroundLocationPermissionGranted = true // Pastikan state sesuai
                // Langsung setup Geofence karena semua izin yang diperlukan sudah terpenuhi
                Log.d("HomeDosenScreen", "All necessary permissions granted. Attempting geofence setup.")
                GeofenceHelper.addGeofences(context)
                geofenceSetupAttempted = true // Tandai sudah dicoba
            }
        } else {
            // Izin Fine Location DITOLAK
            Log.w("HomeDosenScreen", "ACCESS_FINE_LOCATION Denied.")
            // Handle penolakan (misalnya tampilkan Snackbar atau pesan)
            // Anda mungkin ingin menonaktifkan fitur terkait geofencing
            geofenceSetupAttempted = true // Tetap tandai sudah dicoba (meski gagal)
        }
    }



    // LaunchedEffect untuk memicu pemeriksaan izin saat composable pertama kali dibuat
    LaunchedEffect(Unit) { // Unit berarti hanya berjalan sekali saat masuk komposisi
        if (!geofenceSetupAttempted) { // Hanya jalankan jika belum pernah dicoba
            Log.d("HomeDosenScreen", "LaunchedEffect: Checking permissions...")
            if (!fineLocationPermissionGranted) {
                Log.d("HomeDosenScreen", "Fine location not granted. Requesting...")
                fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !backgroundLocationPermissionGranted) {
                Log.d("HomeDosenScreen", "Fine location granted, but background not. Requesting background...")
                // TODO: Idealnya tampilkan dialog penjelasan di sini
                backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                // Semua izin sudah ada (atau tidak diperlukan)
                Log.d("HomeDosenScreen", "All permissions already granted. Attempting geofence setup.")
                GeofenceHelper.addGeofences(context)
                geofenceSetupAttempted = true
            }
        } else {
            Log.d("HomeDosenScreen", "LaunchedEffect: Geofence setup already attempted, skipping check.")
        }
    }

    // --- Akhir Integrasi Geofencing ---

    // --- MULAI EFEK UNTUK START/STOP LOKASI AKTIF ---
    // DisposableEffect akan memulai saat Composable masuk & izin ada,
    // dan membersihkan (menghentikan) saat Composable keluar atau izin dicabut.
    DisposableEffect(fineLocationPermissionGranted) { // Key: agar dievaluasi ulang jika izin berubah
        if (fineLocationPermissionGranted) {
            Log.i("HomeDosenScreen_GPS", "Izin lokasi ada. Memulai pembaruan lokasi aktif...")
            // Memulai pembaruan lokasi
            // Anotasi diperlukan karena kita cek izin secara manual di dalam effect
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper() // Callback di main thread (aman untuk log)
            )
        } else {
            Log.w("HomeDosenScreen_GPS", "Izin lokasi tidak ada. Tidak memulai pembaruan lokasi aktif.")
        }

        // onDispose akan dipanggil saat HomeDosenScreen meninggalkan komposisi
        // atau saat fineLocationPermissionGranted berubah.
        onDispose {
            Log.i("HomeDosenScreen_GPS", "Menghentikan pembaruan lokasi aktif (onDispose)...")
            // Sangat penting untuk menghentikan pembaruan agar hemat baterai!
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    // --- AKHIR EFEK UNTUK START/STOP LOKASI AKTIF ---

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
                    Log.e("HomeDosenScreen", "Cannot navigate to edit profile, user identifier is null")
                    // Tampilkan Toast error?
                }
            }
        )
    }
    // --- Layout UI Asli Anda ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF1E3E62))
        // .padding(top = 16.dp) // Padding mungkin lebih baik di dalam Column spesifik
    ) {
        // Tambahkan padding di sini jika diperlukan untuk Header
        Box(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
            Header(username, identifier, fotoProfile) // Panggil Header dengan data pengguna
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Membuat Card mengisi sisa ruang vertikal
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            CardButtonBarDosen(navController)
            Column(modifier = Modifier.fillMaxSize()) { // Pastikan Column di dalam Card mengisi Card
                TopDosenLeaderboard(
                    modifier = Modifier.padding(horizontal = 8.dp), // Beri padding section
                    // Teruskan state dari peringkatViewModel sebagai parameter
                    peringkatList = peringkatList,
                    isLoading = isLoadingPeringkat,
                    errorMessage = errorPeringkat,
                    selectedMonth = selectedMonthPeringkat,
                    selectedYear = selectedYearPeringkat
                )
                // DosenList mungkin perlu weight jika ingin scrollable dan mengisi sisa ruang Card
                Box(modifier = Modifier.weight(1f)) { // Beri DosenList sisa ruang
                    DosenList(modifier = Modifier .padding(bottom = 10.dp) ,navController,dosenViewModel)
                }
            }
        }
    }
    // --- Akhir Layout UI Asli Anda ---
}
