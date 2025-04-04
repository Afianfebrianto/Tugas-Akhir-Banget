package com.afian.tugasakhir.Component

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color.parseColor
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


// --- Definisikan Koordinat Geofence (sebaiknya dari konstanta atau ViewModel) ---
object GeofenceData {
    val CENTER = LatLng(-5.1359355261683906, 119.44895924980513)
    const val RADIUS_METERS = 50.0 // Gunakan Double untuk Circle

    // Koordinat sudut asli (untuk visualisasi Polygon)
    val CORNERS = listOf(
        LatLng(-5.136154640871624, 119.44884939358585), // TL
        LatLng(-5.136182976030469, 119.44912618581905), // TR
        LatLng(-5.135779171177262, 119.44915940198575), // BR
        LatLng(-5.135758737671867, 119.44887608762276)  // BL
    )
}
// --- Akhir Definisi Koordinat ---


@OptIn(ExperimentalMaterial3Api::class) // Untuk TopAppBar Material 3
@Composable
fun GeofencingMap() {

    // State untuk mengontrol kamera map
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(GeofenceData.CENTER, 17f) // Zoom level 17f (bisa disesuaikan)
    }

    // Pengaturan UI Map (opsional)
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true, // Tampilkan tombol zoom +/-
            compassEnabled = true,
            myLocationButtonEnabled = true // Perlu izin lokasi untuk ini
        )
    }
    // Properti Map (opsional)
    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = true, // Tampilkan titik biru lokasi saat ini (perlu izin)
            mapType = MapType.NORMAL // Bisa ganti ke HYBRID, SATELLITE, TERRAIN
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Peta Area Geofence") })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Terapkan padding dari Scaffold
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings,
                properties = mapProperties
            ) {
                // 1. Tandai Titik Tengah Geofence
                Marker(
                    state = MarkerState(position = GeofenceData.CENTER),
                    title = "Pusat Geofence",
                    snippet = "Radius: ${GeofenceData.RADIUS_METERS}m"
                )

                // 2. Gambar Lingkaran Geofence (yang *sebenarnya* digunakan API)
                Circle(
                    center = GeofenceData.CENTER,
                    radius = GeofenceData.RADIUS_METERS, // Radius dalam meter
                    strokeColor = Color(parseColor("#FF4081")), // Warna garis tepi (misal: pink)
                    strokeWidth = 5f, // Lebar garis tepi
                    fillColor = Color(parseColor("#40FF4081")) // Warna isi (misal: pink transparan)
                )

                // 3. (Opsional) Gambar Polygon dari Sudut Asli yang Anda Berikan
                Polygon(
                    points = GeofenceData.CORNERS,
                    strokeColor = Color(parseColor("#3F51B5")), // Warna garis tepi (misal: indigo)
                    strokeWidth = 4f,
                    fillColor = Color(parseColor("#403F51B5")) // Warna isi (misal: indigo transparan)
                )
            }
        }
    }
}


//@Composable
//fun GeofencingMap() {
//    // Koordinat
//    val coordinates = listOf(
//        LatLng(-5.136154640871624, 119.44884939358585), // Pojok Atas Kiri
//        LatLng(-5.136182976030469, 119.44912618581905), // Pojok Atas Kanan
//        LatLng(-5.135779171177262, 119.44915940198575), // Pojok Bawah Kanan
//        LatLng(-5.135758737671867, 119.44887608762276), // Pojok Bawah Kiri
//        LatLng(-5.1359355261683906, 119.44895924980513) // Titik Tengah
//    )
//
//    // Tentukan radius untuk lingkaran
//    val radius = 40f // Radius dalam meter
//
//    // Tentukan posisi kamera
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(coordinates[4], 16f) // Pusatkan pada titik tengah
//    }
//
//    GoogleMap(
//        modifier = Modifier.fillMaxSize(),
//        cameraPositionState = cameraPositionState,
//        properties = MapProperties(isMyLocationEnabled = true)
//    ) {
//        // Gambar lingkaran di titik tengah
//        Circle(
//            center = coordinates[1], // Titik Tengah
//            radius = radius.toDouble(), // Radius dalam meter
//            strokeColor = Color.Red,
//            fillColor = Color(0x2200FF00) // Warna transparan hijau
//        )
//    }
//}
