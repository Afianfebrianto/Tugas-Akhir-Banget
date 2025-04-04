package com.afian.tugasakhir.Controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object GeofenceHelper {

    private const val TAG = "GeofenceHelper"
    const val GEOFENCE_ID = "AREA_TARGET_1" // ID unik untuk geofence Anda
    private const val GEOFENCE_RADIUS_METERS = 50f // Radius dalam meter (perkiraan)
    private const val DWELL_DELAY_MILLISECONDS = 1000 * 10 // 10 detik

    // Koordinat tengah
    private const val CENTER_LAT = -5.1359355261683906
    private const val CENTER_LNG = 119.44895924980513

    // --- Koordinat sudut (untuk perhitungan radius jika diperlukan) ---
    // private const val TL_LAT = -5.136154640871624
    // private const val TL_LNG = 119.44884939358585
    // private const val TR_LAT = -5.136182976030469
    // private const val TR_LNG = 119.44912618581905
    // private const val BR_LAT = -5.135779171177262
    // private const val BR_LNG = 119.44915940198575
    // private const val BL_LAT = -5.135758737671867
    // private const val BL_LNG = 119.44887608762276

    // Fungsi untuk menghitung jarak (Haversine) - jika ingin radius lebih akurat
    // fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    //     val R = 6371e3 // Radius bumi dalam meter
    //     val phi1 = Math.toRadians(lat1)
    //     val phi2 = Math.toRadians(lat2)
    //     val deltaPhi = Math.toRadians(lat2 - lat1)
    //     val deltaLambda = Math.toRadians(lon2 - lon1)
    //
    //     val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
    //             cos(phi1) * cos(phi2) *
    //             sin(deltaLambda / 2) * sin(deltaLambda / 2)
    //     val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    //
    //     return (R * c).toFloat()
    // }


    // PendingIntent untuk BroadcastReceiver
    private fun getGeofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context.applicationContext, com.afian.tugasakhir.Controller.GeofenceBroadcastReceiver::class.java) // Bisa pakai application context juga
        return PendingIntent.getBroadcast(
            context.applicationContext, // Bisa pakai application context juga
            0, // Request code (0 biasanya OK)
            intent,
            // V V V Ubah bagian ini V V V
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                // Untuk Android < S (12), UPDATE_CURRENT biasanya cukup
                // atau bisa coba FLAG_CANCEL_CURRENT seperti contoh
                PendingIntent.FLAG_UPDATE_CURRENT // atau coba PendingIntent.FLAG_CANCEL_CURRENT
            } else {
                // Untuk Android S (12) ke atas, coba MUTABLE
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // <--- COBA INI
            }
        )
    }

    // Membuat objek Geofence
    private fun buildGeofence(): Geofence {
        // Hitung radius jika perlu, misal dari tengah ke pojok atas kiri
        // val radius = calculateDistance(CENTER_LAT, CENTER_LNG, TL_LAT, TL_LNG)
        // Log.d(TAG, "Calculated Radius: $radius meters") // Hasilnya sekitar 25-30m

        return Geofence.Builder()
            // Set ID unik untuk geofence ini
            .setRequestId(GEOFENCE_ID)
            // Tentukan area geofence (lingkaran)
            .setCircularRegion(
                CENTER_LAT,
                CENTER_LNG,
                GEOFENCE_RADIUS_METERS // Gunakan radius yang fix atau hasil perhitungan
            )
            // Tentukan durasi geofence (NEVER_EXPIRE berarti tak terbatas)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            // Tentukan tipe transisi yang ingin dipantau
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT or
                        Geofence.GEOFENCE_TRANSITION_DWELL // Tambahkan DWELL
            )
            // Tentukan berapa lama (ms) pengguna harus berada di dalam sebelum DWELL terpicu
            .setLoiteringDelay(DWELL_DELAY_MILLISECONDS)
            .build()
    }
    // Fungsi utama untuk menambahkan geofence
    @SuppressLint("MissingPermission") // Izin diperiksa sebelum pemanggilan
    fun addGeofence(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Fine location permission not granted.")
            return // Jangan lanjutkan jika izin belum diberikan
        }

        val geofencingClient = LocationServices.getGeofencingClient(context)
        val geofence = buildGeofence()
        val geofencingRequest = buildGeofencingRequest(geofence)
        val pendingIntent = getGeofencePendingIntent(context)

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)?.run {
            addOnSuccessListener {
                Log.d(TAG, "Geofence added successfully: ${geofence.requestId}")
            }
            addOnFailureListener { exception ->
                Log.e(TAG, "Failed to add geofence: ${geofence.requestId}", exception)
                // Tambahan: Cek status code spesifik jika perlu
                // if ((exception as? ApiException)?.statusCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) { ... }
            }
        }
    }

    // Membuat request untuk menambahkan geofence
    private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            // INITIAL_TRIGGER_ENTER akan memicu GEOFENCE_TRANSITION_ENTER jika perangkat
            // sudah berada di dalam geofence saat ditambahkan.
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL)
            addGeofence(geofence)
        }.build()
    }



    // Fungsi untuk menghapus geofence (opsional)
    fun removeGeofences(context: Context) {
        val geofencingClient = LocationServices.getGeofencingClient(context)
        val pendingIntent = getGeofencePendingIntent(context)

        geofencingClient.removeGeofences(pendingIntent)?.run {
            addOnSuccessListener {
                Log.d(TAG, "Geofences removed successfully.")
            }
            addOnFailureListener { exception ->
                Log.e(TAG, "Failed to remove geofences.", exception)
            }
        }
        // Atau hapus berdasarkan ID:
        // geofencingClient.removeGeofences(listOf(GEOFENCE_ID))
    }
}