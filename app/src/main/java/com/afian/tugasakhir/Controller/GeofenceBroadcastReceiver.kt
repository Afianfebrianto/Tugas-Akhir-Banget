package com.afian.tugasakhir.Controller

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
    // Membuat CoroutineScope untuk tugas background
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Dapatkan instance ApiService (asumsi RetrofitClient adalah object singleton)
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
        // --- Akhir Logika Aplikasi Anda ---


        // Proses transisi
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

    private fun handleEnterTransition(
        userId: Int,
        prefs: SharedPreferences,
        event: GeofencingEvent
    ) {
        val currentLokasiId = prefs.getInt(LocationPrefsKeys.KEY_ID_LOKASI, LocationPrefsKeys.INVALID_LOKASI_ID)

        // Hanya proses jika belum ada id_lokasi yang valid
        if (currentLokasiId == LocationPrefsKeys.INVALID_LOKASI_ID) {

            // Pastikan ada geofence yang terpicu sebelum lanjut
            val triggers = event.triggeringGeofences
            if (!triggers.isNullOrEmpty()) { // <--- Cek jika ada trigger
                Log.d(TAG, "ENTER transition: No valid location ID found. Proceeding to add location ONCE for this event.")
                val triggerIds = triggers.joinToString { it.requestId }
                Log.d(TAG, "Triggering Geofence IDs for this event: $triggerIds")

                // Launch SATU coroutine saja untuk event ini  <--- Kunci Perbaikan
                scope.launch {
                    try {
                        // ... (Persiapan data waktu dan requestBody)
                        val now = LocalDateTime.now()
                        val today = LocalDate.now()
                        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
                        val jamMasukStr = now.format(dateTimeFormatter)
                        val tanggalStr = today.format(dateFormatter)
                        val requestBody = AddLocationRequest(userId, tanggalStr, jamMasukStr)


                        Log.d(TAG, "Calling addLocation API ONCE with: $requestBody") // <--- Panggil API sekali
                        val response = apiService.addLocation(requestBody)

                        if (response.status && response.id_lokasi != null) {
                            // ... (Simpan id_lokasi sekali)
                            val newLokasiId = response.id_lokasi
                            Log.d(TAG, "addLocation API success. Received id_lokasi: $newLokasiId")
                            prefs.edit().putInt(LocationPrefsKeys.KEY_ID_LOKASI, newLokasiId).apply()
                            Log.d(TAG,"Saved id_lokasi: $newLokasiId to SharedPreferences.")
                        } else {
                            // ... (Log error)
                        }
                    } catch (e: Exception) {
                        // ... (Log exception)
                    }
                } // <--- Akhir scope.launch tunggal
            } else {
                Log.w(TAG, "ENTER transition detected, but triggeringGeofences list is null or empty. Skipping API call.")
            }
        } else {
            Log.d(TAG, "ENTER transition: Valid location ID ($currentLokasiId) already exists. Skipping add location API call.")
        }
    }

    // Pastikan handleExitTransition juga diperbaiki dengan cara yang sama
    private fun handleExitTransition(
        userId: Int,
        prefs: SharedPreferences,
        event: GeofencingEvent
    ) {
        val currentLokasiId = prefs.getInt(LocationPrefsKeys.KEY_ID_LOKASI, LocationPrefsKeys.INVALID_LOKASI_ID)

        // Hanya proses jika ADA id_lokasi yang valid
        if (currentLokasiId != LocationPrefsKeys.INVALID_LOKASI_ID) {

            val triggers = event.triggeringGeofences
            if (!triggers.isNullOrEmpty()) { // <-- Cek jika ada trigger
                Log.d(TAG, "EXIT transition: Found valid location ID ($currentLokasiId). Proceeding to update location ONCE for this event.")
                val triggerIds = triggers.joinToString { it.requestId }
                Log.d(TAG, "Triggering Geofence IDs for this event: $triggerIds")

                // Launch SATU coroutine saja  <--- Kunci Perbaikan
                scope.launch {
                    try {
                        // ... (Persiapan data waktu dan requestBody)
                        val now = LocalDateTime.now()
                        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                        val jamKeluarStr = now.format(dateTimeFormatter)
                        val requestBody = UpdateLocationRequest(currentLokasiId, userId, jamKeluarStr)

                        Log.d(TAG, "Calling updateLocation API ONCE with: $requestBody") // <-- Panggil API sekali
                        val response = apiService.updateLocation(requestBody)

                        if (response.status) {
                            // ... (Hapus id_lokasi sekali)
                            Log.d(TAG, "updateLocation API success. Message: ${response.message}")
                            prefs.edit().remove(LocationPrefsKeys.KEY_ID_LOKASI).apply()
                            Log.d(TAG,"Cleared id_lokasi from SharedPreferences.")
                        } else {
                            // ... (Log error)
                        }
                    } catch (e: Exception) {
                        // ... (Log exception)
                    }
                } // <--- Akhir scope.launch tunggal
            } else {
                Log.w(TAG, "EXIT transition detected, but triggeringGeofences list is null or empty. Skipping API call.")
            }
        } else {
            Log.d(TAG, "EXIT transition: No valid location ID found. Skipping update location API call.")
        }
    }

}
