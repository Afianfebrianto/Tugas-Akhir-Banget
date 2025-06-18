package com.afian.tugasakhir.Service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.afian.tugasakhir.API.ApiService
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.AddLocationRequest
import com.afian.tugasakhir.Model.UpdateLocationRequest
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "GeofenceReceiver"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val apiService: ApiService = RetrofitClient.apiService

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "<<< onReceive CALLED! >>> Intent action: ${intent?.action}")

        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent is null, cannot process.")
            return
        }

        // Langsung gunakan applicationContext agar aman jika context Activity sudah hancur
        val appContext = context.applicationContext

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        Log.d(TAG, "GeofencingEvent extracted. Is null: ${geofencingEvent == null}")

        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null.")
            return
        }

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofence Error Code ${geofencingEvent.errorCode}: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.d(TAG, "Geofence Transition detected: $geofenceTransition")

        // --- Mulai Logika Aplikasi Anda ---
        // Dapatkan user_id dari SharedPreferences LoginViewModel
        val userPrefs = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = userPrefs.getInt("user_id", -1) // Ambil user_id, default -1 jika tidak ada

        // =====> LOG UNTUK MEMERIKSA USER ID <=====
        Log.d(TAG, "Attempting to retrieve user_id. Value found: $userId")

        if (userId == -1) {
            Log.e(TAG, "User ID not found in SharedPreferences. Cannot process geofence event.")
            return // Tidak bisa lanjut tanpa user_id
        }

        // Dapatkan SharedPreferences untuk state lokasi
        val locationPrefs = appContext.getSharedPreferences(
            LocationPrefsKeys.PREFS_NAME,
            Context.MODE_PRIVATE
        )

        // Proses transisi
        synchronized (this) {
            when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    // Panggil fungsi untuk menangani ENTER
                    handleEnterTransition(userId, locationPrefs, geofencingEvent)
                }

                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    // Panggil fungsi untuk menangani EXIT
                    handleExitTransition(userId, locationPrefs, geofencingEvent)
                }

                Geofence.GEOFENCE_TRANSITION_DWELL -> {
                    // Log DWELL (atau hapus jika tidak diperlukan)
                    geofencingEvent.triggeringGeofences?.forEach { geofence: Geofence -> // Tipe eksplisit
                        Log.d(TAG, ">>> LOG: Dwelling inside Geofence - ID: ${geofence.requestId}")
                    }
                }

                else -> {
                    Log.e(TAG, "Unknown Geofence Transition type: $geofenceTransition")
                }
            }
        }
    }

    private fun handleEnterTransition(userId: Int, prefs: SharedPreferences, event: GeofencingEvent) {
        val triggers = event.triggeringGeofences ?: return // Keluar jika tidak ada trigger
        Log.d(TAG, "ENTER event triggered by: ${triggers.joinToString { it.requestId }}")

        // 1. Dapatkan counter saat ini
        val oldCount = prefs.getInt(LocationPrefsKeys.KEY_ACTIVE_GEOFENCE_COUNT, 0)
        // 2. Hitung counter baru
        val newCount = oldCount + triggers.size // Tambah counter sebanyak geofence yang terpicu

        Log.i(TAG, "ENTER: Active geofence count changed from $oldCount to $newCount.")

        // 3. Simpan counter yang baru
        prefs.edit().putInt(LocationPrefsKeys.KEY_ACTIVE_GEOFENCE_COUNT, newCount).apply()

        // 4. HANYA panggil API jika ini adalah geofence PERTAMA yang dimasuki (counter dari 0 -> >0)
        if (oldCount == 0 && newCount > 0) {
            Log.i(TAG, "First entry detected! Calling addLocation API...")
            scope.launch {
                try {
                    val now = LocalDateTime.now()
                    val today = LocalDate.now()
                    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                    val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
                    val requestBody = AddLocationRequest(userId, today.format(dateFormatter), now.format(dateTimeFormatter))

                    val response = apiService.addLocation(requestBody)

                    if (response.status && response.id_lokasi != null) {
                        val newLokasiId = response.id_lokasi
                        Log.i(TAG, "addLocation API success. Received id_lokasi: $newLokasiId")
                        // Simpan id_lokasi untuk digunakan saat EXIT nanti
                        prefs.edit().putInt(LocationPrefsKeys.KEY_ID_LOKASI, newLokasiId).apply()
                    } else {
                        Log.e(TAG, "addLocation API failed: ${response.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception in addLocation API call", e)
                }
            }
        } else {
            Log.d(TAG, "Already inside another geofence. Skipping addLocation API call.")
        }
    }

    private fun handleExitTransition(userId: Int, prefs: SharedPreferences, event: GeofencingEvent) {
        val triggers = event.triggeringGeofences ?: return
        Log.d(TAG, "EXIT event triggered by: ${triggers.joinToString { it.requestId }}")

        // 1. Dapatkan counter saat ini
        val oldCount = prefs.getInt(LocationPrefsKeys.KEY_ACTIVE_GEOFENCE_COUNT, 0)
        // 2. Hitung counter baru (pastikan tidak negatif)
        val newCount = maxOf(0, oldCount - triggers.size)

        Log.i(TAG, "EXIT: Active geofence count changed from $oldCount to $newCount.")

        // 3. Simpan counter yang baru
        prefs.edit().putInt(LocationPrefsKeys.KEY_ACTIVE_GEOFENCE_COUNT, newCount).apply()

        // 4. HANYA panggil API jika ini adalah geofence TERAKHIR yang ditinggalkan (counter dari >0 -> 0)
        if (newCount == 0 && oldCount > 0) {
            val lokasiIdToUpdate = prefs.getInt(LocationPrefsKeys.KEY_ID_LOKASI, LocationPrefsKeys.INVALID_LOKASI_ID)

            if (lokasiIdToUpdate != LocationPrefsKeys.INVALID_LOKASI_ID) {
                Log.i(TAG, "Final exit detected! Calling updateLocation API for lokasiId: $lokasiIdToUpdate")
                scope.launch {
                    try {
                        val now = LocalDateTime.now()
                        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                        val requestBody = UpdateLocationRequest(lokasiIdToUpdate, userId, now.format(dateTimeFormatter))

                        val response = apiService.updateLocation(requestBody)

                        if (response.status) {
                            Log.i(TAG, "updateLocation API success. Clearing location state.")
                            // Hapus id_lokasi dan reset counter (walaupun sudah 0, untuk kebersihan)
                            prefs.edit()
                                .remove(LocationPrefsKeys.KEY_ID_LOKASI)
                                .remove(LocationPrefsKeys.KEY_ACTIVE_GEOFENCE_COUNT)
                                .apply()
                        } else {
                            Log.e(TAG, "updateLocation API failed: ${response.message}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception in updateLocation API call", e)
                    }
                }
            } else {
                Log.w(TAG, "Final exit detected, but no valid lokasiId found to update.")
            }
        } else {
            Log.d(TAG, "Still inside other geofences. Skipping updateLocation API call.")
        }
    }
}
