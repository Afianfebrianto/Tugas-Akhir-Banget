package com.afian.tugasakhir.Service

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

object GeofenceHelper {

    private const val TAG = "GeofenceHelper"

    // Radius default untuk setiap geofence target kecil (dalam meter)
    private const val TARGET_GEOFENCE_RADIUS_METERS = 20f // Coba 20 meter, sesuaikan jika perlu
    private const val DWELL_DELAY_MILLISECONDS = 1000 * 10 // 10 detik (jika DWELL diaktifkan)

    // Daftar koordinat target baru
    private val TARGET_LOCATIONS = mapOf(
        "Belakang_R" to LatLng(-5.1358115, 119.4489161),
        "Corner_Samping_Belakang" to LatLng(-5.1358267, 119.4491116),
        "Corner_Samping_Depan" to LatLng(-5.1359155, 119.4491038),
        "Corner_Depan_R" to LatLng(-5.1361354, 119.4488866),
        "Corner_Depan_L" to LatLng(-5.1361372, 119.4489829),
//        "Corner_Belakang_L" to LatLng (-5.1359406,119.4490079),
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

    // PendingIntent (logika tetap sama, flag MUTABLE disarankan dari tes sebelumnya)
    private fun getGeofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context.applicationContext, GeofenceBroadcastReceiver::class.java)
        // Disarankan menggunakan FLAG_MUTABLE atau FLAG_IMMUTABLE secara eksplisit untuk Android S+
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE // Coba MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context.applicationContext, 0, intent, flags)
    }

    // Membuat LIST objek Geofence dari data target
    private fun buildGeofencesList(): List<Geofence> {
        val geofenceList = mutableListOf<Geofence>()
        TARGET_LOCATIONS.forEach { (id, latLng) ->
            geofenceList.add(
                Geofence.Builder()
                    .setRequestId(id) // Gunakan key map sebagai ID unik
                    .setCircularRegion(
                        latLng.latitude,
                        latLng.longitude,
                        TARGET_GEOFENCE_RADIUS_METERS
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    // Tentukan transisi (ENTER & EXIT direkomendasikan untuk awal)
                    .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER or
                                Geofence.GEOFENCE_TRANSITION_EXIT
                        // | Geofence.GEOFENCE_TRANSITION_DWELL // Aktifkan DWELL jika perlu
                    )
                    // .setLoiteringDelay(DWELL_DELAY_MILLISECONDS) // Aktifkan jika DWELL aktif
                    .build()
            )
        }
        return geofenceList
    }

    // Membuat request untuk menambahkan LIST Geofence
    private fun buildGeofencingRequest(geofences: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            // setInitialTrigger(0) direkomendasikan dari tes sebelumnya (tidak ada trigger awal)
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            // setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER) // Alternatif jika ingin trigger ENTER awal
            addGeofences(geofences) // Tambahkan list geofence
        }.build()
    }

    // Fungsi utama untuk menambahkan SEMUA geofence target
    @SuppressLint("MissingPermission")
    fun addGeofences(context: Context) { // Nama fungsi diubah sedikit
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Fine location permission not granted.")
            return
        }

        val geofencesToAdd = buildGeofencesList() // Buat list geofence

        if (geofencesToAdd.isEmpty()) {
            Log.w(TAG, "Geofence list to add is empty.")
            return
        }

        val geofencingClient = LocationServices.getGeofencingClient(context)
        val geofencingRequest = buildGeofencingRequest(geofencesToAdd) // Buat request dari list
        val pendingIntent = getGeofencePendingIntent(context)

        Log.d(TAG, "Attempting to add ${geofencesToAdd.size} geofences...")

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)?.run {
            addOnSuccessListener {
                Log.i(TAG, "${geofencesToAdd.size} Geofences added successfully.")
                // Log ID geofence yang ditambahkan jika perlu
                // Log.d(TAG, "Added IDs: ${geofencesToAdd.joinToString { it.requestId }}")
            }
            addOnFailureListener { exception ->
                Log.e(TAG, "Failed to add ${geofencesToAdd.size} geofences.", exception)
                // Tambahan: Cek status code spesifik jika perlu
                if ((exception as? ApiException)?.statusCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                    Log.e(
                        TAG,
                        "Error: Geofence service not available (1000). Check location settings/Play Services."
                    )
                } else if ((exception as? ApiException)?.statusCode == GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES) {
                    Log.e(TAG, "Error: Exceeded maximum geofence limit (100).")
                }
            }
        }
    }

    // Fungsi untuk menghapus SEMUA geofence yang terkait dengan PendingIntent ini
    fun removeGeofences(context: Context) {
        val geofencingClient = LocationServices.getGeofencingClient(context)
        val pendingIntent = getGeofencePendingIntent(context) // Dapatkan PendingIntent yang sama

        Log.d(
            TAG,
            "Attempting to remove all geofences associated with PendingIntent: ${pendingIntent.hashCode()}"
        )

        geofencingClient.removeGeofences(pendingIntent)?.run { // Hapus berdasarkan PendingIntent
            addOnSuccessListener {
                Log.i(TAG, "All geofences associated with the PendingIntent removed successfully.")
            }
            addOnFailureListener { exception ->
                Log.e(TAG, "Failed to remove geofences by PendingIntent.", exception)
            }
        }
        // Jika Anda ingin menghapus berdasarkan ID secara spesifik di masa depan:
        // val geofenceIds = TARGET_LOCATIONS.keys.toList()
        // geofencingClient.removeGeofences(geofenceIds).addOnCompleteListener { ... }
    }
}

