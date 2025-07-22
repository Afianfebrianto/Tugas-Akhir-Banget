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


object NewGeofenceTargets {
    const val FIXED_RADIUS_METERS_FLOAT = 20f

    val TARGET_LOCATIONS: Map<String, LatLng> = mapOf(
        "Belakang_R" to LatLng(-5.1358115, 119.4489161),
        "Corner_Samping_Belakang" to LatLng(-5.1358267, 119.4491116),
        "Corner_Samping_Depan" to LatLng(-5.1359155, 119.4491038),
        "Corner_Depan_R" to LatLng(-5.1361354, 119.4488866),
        "Corner_Depan_L" to LatLng(-5.1361372, 119.4489829),
//        "Corner_Belakang_L" to LatLng(-5.1359406, 119.4490079),
        "Mid_Belakang" to LatLng(-5.1358624, 119.4490039),
        "Mid_Belakang_R" to LatLng(-5.1359339, 119.4489137),
        "Tengah_Depan" to LatLng(-5.1360345, 119.4489478),

        "Parkiran_depan" to LatLng(-5.1362251, 119.4491960),
        "parkiran_belakang_A" to LatLng(-5.1356162, 119.4489378),
        "parkiran_belakang_B" to LatLng(-5.1355059, 119.4489591),
        "parkiran_belakang_C" to LatLng(-5.1354308, 119.4490255),
        "parkiran_belakang_D" to LatLng(-5.1354975, 119.4491218),
        "parkiran_belakang_E" to LatLng(-5.1356263, 119.4490986),
    )

    val APPROX_CENTER: LatLng by lazy {
        if (TARGET_LOCATIONS.isEmpty()) {
            LatLng(-5.1359, 119.4490)
        } else {
            val avgLat = TARGET_LOCATIONS.values.map { it.latitude }.average()
            val avgLng = TARGET_LOCATIONS.values.map { it.longitude }.average()
            LatLng(avgLat, avgLng)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofencingMap() {

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(NewGeofenceTargets.APPROX_CENTER, 19f)
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            myLocationButtonEnabled = true // Perlu izin lokasi
        )
    }
    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = true, // Perlu izin lokasi
            mapType = MapType.SATELLITE // Tetap satelit
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Peta Target Geofence (Radius 20m)") })
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
                NewGeofenceTargets.TARGET_LOCATIONS.forEach { (id, targetLatLng) ->

                    Circle(
                        center = targetLatLng,
                        radius = NewGeofenceTargets.FIXED_RADIUS_METERS_FLOAT.toDouble(),
                        strokeWidth = 2f,
                        strokeColor = Color.Cyan,
                        fillColor = Color.Cyan.copy(alpha = 0.3f)
                    )
                    Marker(
                        state = MarkerState(position = targetLatLng),
                        title = id
                    )
                }
            }
        }
    }
}
