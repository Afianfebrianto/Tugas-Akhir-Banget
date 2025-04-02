package com.afian.tugasakhir.Controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.jvm.java

//@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//fun createGeofence(context: Context, geofencingClient: GeofencingClient,geofenceId: String) {
//    val sharedPreferences: SharedPreferences = context.getSharedPreferences("GeofencePrefs", Context.MODE_PRIVATE)
//    val geofencePoints = listOf(
//        LatLng(-5.136154640871624, 119.44884939358585), // Pojok Atas Kiri
//        LatLng(-5.136182976030469, 119.44912618581905), // Pojok Atas Kanan
//        LatLng(-5.135779171177262, 119.44915940198575), // Pojok Bawah Kanan
//        LatLng(-5.135758737671867, 119.44887608762276), // Pojok Bawah Kiri
//        LatLng(-5.1359355261683906, 119.44895924980513) // Titik Tengah
//    )
//    if (sharedPreferences.getBoolean(geofenceId, false)) {
//        Log.d("Geofence", "Geofence already registered.")
//        return // Keluar jika geofence sudah terdaftar
//    }
//
//    // Buat geofence untuk setiap titik
//    val geofenceList = geofencePoints.map { point ->
//        Geofence.Builder()
//            .setRequestId("geofence_${point.latitude}_${point.longitude}")
//            .setCircularRegion(
//                point.latitude,
//                point.longitude,
//                40f // Radius dalam meter
//            )
//            .setExpirationDuration(Geofence.NEVER_EXPIRE)
//            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
//            .build()
//    }
//
//    // Buat GeofencingRequest
//    val geofencingRequest = GeofencingRequest.Builder()
//        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
//        .addGeofences(geofenceList)
//        .build()
//
//    // Buat pending intent untuk menerima notifikasi geofencing
//    val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
//    val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
//        context,
//        0,
//        intent,
//        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Tambahkan FLAG_IMMUTABLE
//    )
//    // Cek apakah geofence sudah terdaftar
//
//        // Tambahkan geofence
//        geofencingClient.addGeofences(geofencingRequest,  pendingIntent)
//            .addOnSuccessListener {
//                Log.d("Geofence", "Geofence added successfully")
//            }
//            .addOnFailureListener {
//                Log.e("Geofence", "Failed to add geofence: ${it.message}")
//            }
//
//
//}

enum class GeofenceStatus {
    UNKNOWN, // Status awal atau saat tidak ada info lokasi
    INSIDE,
    OUTSIDE
}

private const val TAG = "GeofenceMonitor"
private const val LOCATION_UPDATE_INTERVAL_MS = 10000L // Perbarui lokasi setiap 10 detik
private const val DWELL_THRESHOLD_MS = 30000L // Anggap "dwelling" jika di dalam selama 30 detik

// --- Koordinat Geofence Anda ---
val cornerTopLeft = LatLng(-5.136154640871624, 119.44884939358585)
val cornerTopRight = LatLng(-5.136182976030469, 119.44912618581905)
val cornerBottomRight = LatLng(-5.135779171177262, 119.44915940198575)
val cornerBottomLeft = LatLng(-5.135758737671867, 119.44887608762276)
val geofencePolygon: List<LatLng> = listOf(
    cornerTopLeft, cornerTopRight, cornerBottomRight, cornerBottomLeft
    // Pastikan urutan searah jarum jam atau berlawanan arah jarum jam
)
// --- Akhir Koordinat Geofence ---


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GeofenceMonitorEffect(
    polygon: List<LatLng> = geofencePolygon, // Gunakan poligon default
    onStatusChange: (status: GeofenceStatus, isDwelling: Boolean) -> Unit = { status, dwelling ->
        // Callback default jika tidak disediakan
        Log.d(TAG, "Default Callback: Status = $status, Dwelling = $dwelling")
    }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var previousStatus by remember { mutableStateOf(GeofenceStatus.UNKNOWN) }
    var insideStartTime by remember { mutableStateOf<Long?>(null) }
    var isDwelling by remember { mutableStateOf(false) }
    var hasLoggedEntering by remember { mutableStateOf(false) }
    var hasLoggedDwelling by remember { mutableStateOf(false) }
    var hasLoggedExiting by remember { mutableStateOf(false) }

    // State untuk izin lokasi
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    // Efek untuk mengamati siklus hidup Composable
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Anda bisa menambahkan logika start/stop update berdasarkan lifecycle jika perlu
            if (event == Lifecycle.Event.ON_START) {
                Log.d(TAG,"Lifecycle ON_START")
                // Mungkin mulai update di sini jika belum
            } else if (event == Lifecycle.Event.ON_STOP) {
                Log.d(TAG,"Lifecycle ON_STOP")
                // Hentikan update di sini untuk hemat baterai saat app tidak visible
                // (Perlu modifikasi flow di bawah untuk bisa di-cancel)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            Log.d(TAG,"Composable Disposed")
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Pembersihan lainnya jika perlu
        }
    }


    // Minta izin saat Composable pertama kali dijalankan
    LaunchedEffect(key1 = locationPermissionsState.allPermissionsGranted) {
        if (!locationPermissionsState.allPermissionsGranted) {
            Log.d(TAG, "Meminta izin lokasi...")
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    // LaunchedEffect untuk memulai pembaruan lokasi setelah izin diberikan
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            Log.d(TAG, "Izin lokasi diberikan. Memulai pemantauan lokasi...")

            // Menggunakan callbackFlow untuk mendapatkan update lokasi
            locationFlow(context, fusedLocationClient).collectLatest { location ->
                val currentLatLng = LatLng(location.latitude, location.longitude)
                val isInside = isPointInPolygon(currentLatLng, polygon)
                val currentStatus = if (isInside) GeofenceStatus.INSIDE else GeofenceStatus.OUTSIDE
                val now = System.currentTimeMillis()

                // --- Logika Transisi dan Dwell ---

                // Transisi dari Luar ke Dalam (Entering)
                if (currentStatus == GeofenceStatus.INSIDE && previousStatus != GeofenceStatus.INSIDE) {
                    if (!hasLoggedEntering) { // Hanya log sekali saat masuk
                        Log.i(TAG, "EVENT: Entering Geofence at $currentLatLng")
                        hasLoggedEntering = true
                        hasLoggedExiting = false // Reset flag exiting
                        hasLoggedDwelling = false // Reset flag dwelling
                    }
                    previousStatus = GeofenceStatus.INSIDE
                    insideStartTime = now // Mulai timer dwell
                    isDwelling = false // Reset status dwelling
                    onStatusChange(currentStatus, isDwelling)
                }
                // Transisi dari Dalam ke Luar (Exiting)
                else if (currentStatus == GeofenceStatus.OUTSIDE && previousStatus == GeofenceStatus.INSIDE) {
                    if (!hasLoggedExiting) { // Hanya log sekali saat keluar
                        Log.w(TAG, "EVENT: Exiting Geofence from $currentLatLng")
                        hasLoggedExiting = true
                        hasLoggedEntering = false // Reset flag entering
                    }
                    previousStatus = GeofenceStatus.OUTSIDE
                    insideStartTime = null // Hentikan timer dwell
                    isDwelling = false    // Tidak lagi dwelling
                    onStatusChange(currentStatus, isDwelling)
                }
                // Tetap di Dalam (Checking for Dwell)
                else if (currentStatus == GeofenceStatus.INSIDE) {
                    // Reset flag entering jika sudah log sebelumnya
                    // (agar bisa log entering lagi jika keluar lalu masuk lagi)
                    if (previousStatus != GeofenceStatus.INSIDE) hasLoggedEntering = false

                    val startTime = insideStartTime
                    if (startTime != null && !isDwelling) { // Cek jika sudah cukup lama di dalam
                        if (now - startTime >= DWELL_THRESHOLD_MS) {
                            if(!hasLoggedDwelling) { // Hanya log dwell sekali
                                Log.d(TAG, "EVENT: Dwelling inside Geofence detected.")
                                hasLoggedDwelling = true
                            }
                            isDwelling = true
                            onStatusChange(currentStatus, isDwelling)
                        }
                    }
                    // Jika sudah dwelling, tidak perlu cek waktu lagi, cukup panggil callback jika perlu
                    else if (isDwelling) {
                        onStatusChange(currentStatus, isDwelling)
                    }
                }
                // Tetap di Luar
                else if (currentStatus == GeofenceStatus.OUTSIDE) {
                    // Reset flag exiting jika sudah log sebelumnya
                    // (agar bisa log exiting lagi jika masuk lalu keluar lagi)
                    if (previousStatus != GeofenceStatus.OUTSIDE) hasLoggedExiting = false
                    previousStatus = GeofenceStatus.OUTSIDE // Pastikan status tetap OUTSIDE
                    insideStartTime = null
                    isDwelling = false
                    onStatusChange(currentStatus, isDwelling)
                }
            }
        } else {
            Log.w(TAG, "Izin lokasi tidak diberikan.")
            // Handle kasus tidak ada izin (misal: tampilkan pesan ke user)
            onStatusChange(GeofenceStatus.UNKNOWN, false) // Update status ke Unknown
            previousStatus = GeofenceStatus.UNKNOWN
            insideStartTime = null
            isDwelling = false
            // Reset semua flag log
            hasLoggedEntering = false
            hasLoggedDwelling = false
            hasLoggedExiting = false
        }
    }
}

// Fungsi Flow untuk mendapatkan update lokasi
@SuppressLint("MissingPermission")
private fun locationFlow(context: Context, client: FusedLocationProviderClient) = callbackFlow {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL_MS)
        .setWaitForAccurateLocation(false) // Bisa disesuaikan
        .setMinUpdateIntervalMillis(LOCATION_UPDATE_INTERVAL_MS / 2) // Batas minimal
        .setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL_MS * 2) // Batas maksimal delay
        .build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let {
                Log.v(TAG, "Lokasi baru diterima: Lat=${it.latitude}, Lon=${it.longitude}, Acc=${it.accuracy}")
                trySend(it).isSuccess // Kirim lokasi ke flow
            } ?: Log.w(TAG, "Hasil lokasi null")
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            Log.d(TAG, "Location Availability: ${availability.isLocationAvailable}")
            if (!availability.isLocationAvailable) {
                Log.e(TAG, "Penyedia lokasi tidak tersedia!")
                // Mungkin perlu handle error di sini
            }
        }
    }

    Log.d(TAG, "Memulai request pembaruan lokasi...")
    client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        .addOnFailureListener { e ->
            Log.e(TAG, "Gagal memulai pembaruan lokasi", e)
            close(e) // Tutup flow jika gagal
        }
        .addOnSuccessListener {
            Log.d(TAG, "Request pembaruan lokasi berhasil dimulai.")
        }


    // Dipanggil saat flow di-cancel/ditutup (misal: Composable di-dispose)
    awaitClose {
        Log.d(TAG, "Menghentikan pembaruan lokasi...")
        client.removeLocationUpdates(locationCallback)
    }
}


// --- Algoritma Point-In-Polygon (Ray Casting) ---
fun isPointInPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
    if (polygon.size < 3) return false // Polygon tidak valid

    var isInside = false
    var i = 0
    var j = polygon.size - 1
    while (i < polygon.size) {
        val pi = polygon[i]
        val pj = polygon[j]

        // Cek apakah titik berada pada garis horizontal yang sama dengan vertex (abaikan)
        // Atau jika garis polygon horizontal (bisa diabaikan atau ditangani khusus)

        // Kondisi utama Ray Casting
        val intersect = ((pi.latitude > point.latitude) != (pj.latitude > point.latitude)) &&
                (point.longitude < (pj.longitude - pi.longitude) * (point.latitude - pi.latitude) / (pj.latitude - pi.latitude) + pi.longitude)

        if (intersect) {
            isInside = !isInside
        }
        j = i++
    }
    return isInside
}

//private fun getPendingIntent(context: Context): PendingIntent {
//    val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
//    return PendingIntent.getBroadcast(
//        context,
//        0,
//        intent,
//        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//    )
//}
