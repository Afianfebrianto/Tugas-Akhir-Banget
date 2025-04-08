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


// --- Definisikan Koordinat Target Baru & Logika Radius ---
object NewGeofenceTargets {
    private const val TAG = "NewGeofenceTargets" // Tag Log

    // Konstanta untuk perhitungan radius
    internal const val DEFAULT_RADIUS_METERS = 30.0 // Gunakan Double
    private const val OVERLAP_FACTOR = 1.1 // Double
    private const val MINIMUM_RADIUS_METERS = 15.0 // Double

    // Daftar koordinat target (sekarang Map agar ada ID)
    val TARGET_LOCATIONS: Map<String, LatLng> = mapOf(
        "TARGET_1" to LatLng(-5.1358417, 119.4489412),
        "TARGET_2" to LatLng(-5.1358403, 119.4490958),
        "TARGET_4" to LatLng(-5.1360277, 119.4489348),
        "TARGET_5" to LatLng(-5.1360271, 119.4490799),
        "TARGET_6" to LatLng(-5.1361475, 119.4489108),
        "TARGET_7" to LatLng(-5.1361669, 119.4490859)
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

    // Fungsi hitung jarak (pindah ke sini)
    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        try {
            Location.distanceBetween(lat1, lon1, lat2, lon2, results)
            // Kembalikan sebagai Double untuk konsistensi radius
            return results[0].toDouble()
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error calculating distance: Invalid coordinates?", e)
            return Double.MAX_VALUE
        }
    }

    // Properti lazy untuk menyimpan hasil perhitungan radius dinamis
    // Ini akan dihitung sekali saat pertama kali diakses
    val dynamicRadii: Map<String, Double> by lazy {
        Log.d(TAG, "Calculating dynamic radii...")
        val radii = mutableMapOf<String, Double>()
        val targetIds = TARGET_LOCATIONS.keys.toList()

        if (targetIds.size <= 1) {
            targetIds.forEach { id -> radii[id] = DEFAULT_RADIUS_METERS }
            Log.d(TAG, "Only ${targetIds.size} target(s). Using default radius: $DEFAULT_RADIUS_METERS")
            return@lazy radii // Kembali dari lazy block
        }

        for (i in targetIds.indices) {
            val currentId = targetIds[i]
            val currentLatLng = TARGET_LOCATIONS[currentId] ?: continue
            var minDistance = Double.MAX_VALUE

            for (j in targetIds.indices) {
                if (i == j) continue
                val otherId = targetIds[j]
                val otherLatLng = TARGET_LOCATIONS[otherId] ?: continue
                val distance = calculateDistanceMeters(
                    currentLatLng.latitude, currentLatLng.longitude,
                    otherLatLng.latitude, otherLatLng.longitude
                )
                minDistance = minOf(minDistance, distance)
            }

            var calculatedRadius = (minDistance / 2.0) * OVERLAP_FACTOR
            calculatedRadius = maxOf(MINIMUM_RADIUS_METERS, calculatedRadius)
            radii[currentId] = calculatedRadius
            Log.d(TAG, "Radius for $currentId = $calculatedRadius meters (based on min neighbor distance: $minDistance)")
        }
        radii // Kembalikan map radius dari lazy block
    }
}
// --- Akhir Definisi Koordinat Baru ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofencingMap() { // Nama fungsi mungkin perlu disesuaikan jika sudah ada

    // State untuk mengontrol kamera map, arahkan ke pusat baru
    val cameraPositionState = rememberCameraPositionState {
        // Gunakan pusat perkiraan dari data baru
        // Mungkin perlu zoom lebih dekat (misal 18f atau 19f) karena titiknya berdekatan
        position = CameraPosition.fromLatLngZoom(NewGeofenceTargets.APPROX_CENTER, 18.5f)
    }

    // Pengaturan UI Map (tetap sama atau sesuaikan)
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = true // Perlu izin lokasi
        )
    }
    // Properti Map (tetap sama atau sesuaikan)
    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = true, // Perlu izin lokasi
            mapType = MapType.SATELLITE
        )
    }
    val targetRadii = NewGeofenceTargets.dynamicRadii
    Scaffold(
        topBar = {
            // Judul bisa disesuaikan
            TopAppBar(title = { Text("Peta Target Geofence") })
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
            ) {
                // Loop melalui Map lokasi target
                NewGeofenceTargets.TARGET_LOCATIONS.forEach { (id, targetLatLng) ->
                    // Ambil radius dinamis untuk ID ini, atau gunakan default
                    val radius = targetRadii[id] ?: NewGeofenceTargets.DEFAULT_RADIUS_METERS // Ambil radius dari map

                    Circle(
                        center = targetLatLng,
                        radius = radius, // <-- GUNAKAN RADIUS DINAMIS DARI MAP
                        strokeWidth = 2f,
                        strokeColor = Color.Cyan, // Warna disesuaikan agar kontras
                        fillColor = Color.Cyan.copy(alpha = 0.3f)
                    )

                    Marker(
                        state = MarkerState(position = targetLatLng),
                        title = id // Gunakan ID dari map sebagai judul
                    )
                }
            }
        }
    }
}
