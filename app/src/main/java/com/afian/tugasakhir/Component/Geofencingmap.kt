package com.afian.tugasakhir.Component

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun GeofencingMap() {
    // Koordinat
    val coordinates = listOf(
        LatLng(-5.136154640871624, 119.44884939358585), // Pojok Atas Kiri
        LatLng(-5.136182976030469, 119.44912618581905), // Pojok Atas Kanan
        LatLng(-5.135779171177262, 119.44915940198575), // Pojok Bawah Kanan
        LatLng(-5.135758737671867, 119.44887608762276), // Pojok Bawah Kiri
        LatLng(-5.1359355261683906, 119.44895924980513) // Titik Tengah
    )

    // Tentukan radius untuk lingkaran
    val radius = 35f // Radius dalam meter

    // Tentukan posisi kamera
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(coordinates[4], 16f) // Pusatkan pada titik tengah
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true)
    ) {
        // Gambar lingkaran di titik tengah
        Circle(
            center = coordinates[4], // Titik Tengah
            radius = radius.toDouble(), // Radius dalam meter
            strokeColor = Color.Red,
            fillColor = Color(0x2200FF00) // Warna transparan hijau
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionRequest() {
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    if (permissionState.allPermissionsGranted) {
        // Izin diberikan, lakukan sesuatu dengan lokasi
        Text("Location permission granted!")
    } else {
        // Izin tidak diberikan
        Text("Location permission denied.")
    }
}