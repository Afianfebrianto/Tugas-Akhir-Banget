package com.afian.tugasakhir.Component

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color.parseColor
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState


// --- Definisikan Koordinat Target Baru & Radius Tetap ---
object NewGeofenceTargets {
    // Radius tetap 20 meter
    const val FIXED_RADIUS_METERS_DOUBLE = 20.0 // Untuk Map Circle (Double)
    const val FIXED_RADIUS_METERS_FLOAT = 20f  // Untuk GeofenceHelper (Float)

    // Daftar koordinat target baru (Map: ID -> LatLng)
    val TARGET_LOCATIONS: Map<String, LatLng> = mapOf(
        "Belakang_R" to LatLng(-5.1358115, 119.4489161),
        "Corner_Samping_Belakang" to LatLng(-5.1358267, 119.4491116),
        "Corner_Samping_Depan" to LatLng(-5.1359155, 119.4491038),
        "Corner_Depan_R" to LatLng(-5.1361354, 119.4488866),
        "Corner_Depan_L" to LatLng(-5.1361372, 119.4489829),
//        "Corner_Belakang_L" to LatLng(-5.1359406, 119.4490079),
        "Mid_Belakang" to LatLng(-5.1358624, 119.4490039),
        "Mid_Belakang_R" to LatLng(-5.1359339, 119.4489137),
        "Tengah_Depan" to LatLng(-5.1360345, 119.4489478)
    )

    // Pusat perkiraan (tidak berubah)
    val APPROX_CENTER: LatLng by lazy {
        if (TARGET_LOCATIONS.isEmpty()) {
            LatLng(-5.1359, 119.4490)
        } else {
            val avgLat = TARGET_LOCATIONS.values.map { it.latitude }.average()
            val avgLng = TARGET_LOCATIONS.values.map { it.longitude }.average()
            LatLng(avgLat, avgLng)
        }
    }
    // Tidak perlu logika radius dinamis lagi di sini
}
// --- Akhir Definisi Koordinat Baru ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofencingMap() { // Nama fungsi mungkin perlu disesuaikan

    // State kamera, atur zoom agar semua titik terlihat (misal 19f)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(NewGeofenceTargets.APPROX_CENTER, 19f)
    }

    // Pengaturan UI Map
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = true // Perlu izin lokasi
        )
    }
    // Properti Map
    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = true, // Perlu izin lokasi
            mapType = MapType.SATELLITE // Tetap satelit
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Peta Target Geofence (Radius 20m)") }) // Update judul
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings,
                properties = mapProperties
            ) { // <-- Mulai blok konten GoogleMap

                // === 👇 IMPLEMENTASI GAMBAR LINGKARAN & MARKER 👇 ===
                // Loop melalui Map lokasi target BARU
                NewGeofenceTargets.TARGET_LOCATIONS.forEach { (id, targetLatLng) ->

                    // Gambar Lingkaran dengan radius tetap 20.0
                    Circle(
                        center = targetLatLng,
                        radius = NewGeofenceTargets.FIXED_RADIUS_METERS_DOUBLE, // <-- Gunakan radius tetap (Double)
                        strokeWidth = 2f, // Border tipis
                        strokeColor = Color.Cyan, // Warna border (sesuaikan agar kontras)
                        fillColor = Color.Cyan.copy(alpha = 0.3f) // Isi transparan
                    )

                    // Gambar Marker di pusat setiap lingkaran
                    Marker(
                        state = MarkerState(position = targetLatLng),
                        title = id // Judul marker = ID dari map (misal: "Belakang_R")
                    )
                }
                // === 👆 AKHIR IMPLEMENTASI 👆 ===

            } // <-- Akhir blok konten GoogleMap
        }
    }
} // <-- Akhir composable GeofencingMap
