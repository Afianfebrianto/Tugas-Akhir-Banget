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

//// <--- PASTIKAN nama kelas ini SAMA PERSIS dengan yang di Manifest dan Intent Helper
//class GeofenceBroadcastReceiver : BroadcastReceiver() {
//
//    private val TAG = "GeofenceReceiver"
//
//    override fun onReceive(context: Context?, intent: Intent?) {
//        // =====> TAMBAHKAN LOG INI <=====
//        Log.d(TAG, "<<< onReceive CALLED! >>> Intent action: ${intent?.action}")
//
//        if (context == null || intent == null) {
//            Log.e(TAG, "Context or Intent is null, cannot process.")
//            return
//        }
//
//        // Coba ekstrak event
//        val geofencingEvent = GeofencingEvent.fromIntent(intent)
//
//        // =====> TAMBAHKAN LOG INI <=====
//        Log.d(TAG, "GeofencingEvent extracted. Is null: ${geofencingEvent == null}")
//
//        if (geofencingEvent == null) {
//            Log.e(TAG,"Error receiving geofence event: event object is null after extraction.")
//            // Log detail intent jika event null, mungkin ini bukan intent geofence?
//            Log.w(TAG, "Intent details: action=${intent.action}, extras=${intent.extras}")
//            return
//        }
//
//
//        if (geofencingEvent.hasError()) {
//            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
//            Log.e(TAG, "Geofence Error Code ${geofencingEvent.errorCode}: $errorMessage")
//            return
//        }
//
//        // Proses transisi jika tidak ada error dan event valid
//        val geofenceTransition = geofencingEvent.geofenceTransition
//        Log.d(TAG, "Geofence Transition detected: $geofenceTransition") // Log tipe transisi
//
//        val triggeringGeofences = geofencingEvent.triggeringGeofences
//        Log.d(TAG, "Triggering Geofences count: ${triggeringGeofences?.size ?: 0}") // Log jumlah geofence
//
//        // Log spesifik untuk Enter/Dwell/Exit (seperti sebelumnya)
//        when (geofenceTransition) {
//            Geofence.GEOFENCE_TRANSITION_ENTER -> {
//                triggeringGeofences?.forEach { geofence ->
//                    Log.d(TAG, ">>> LOG: Entering Geofence - ID: ${geofence.requestId}")
//                }
//            }
//            Geofence.GEOFENCE_TRANSITION_DWELL -> {
//                triggeringGeofences?.forEach { geofence ->
//                    Log.d(TAG, ">>> LOG: Dwelling inside Geofence - ID: ${geofence.requestId}")
//                }
//            }
//            Geofence.GEOFENCE_TRANSITION_EXIT -> {
//                triggeringGeofences?.forEach { geofence ->
//                    Log.d(TAG, ">>> LOG: Exiting Geofence - ID: ${geofence.requestId}")
//                }
//            }
//            else -> {
//                Log.e(TAG, "Unknown Geofence Transition type: $geofenceTransition")
//            }
//        }
//    }
//}

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

    // Fungsi untuk menangani transisi ENTER
    private fun handleEnterTransition(
        userId: Int,
        prefs: SharedPreferences,
        event: GeofencingEvent
    ) {
        val currentLokasiId = prefs.getInt(LocationPrefsKeys.KEY_ID_LOKASI, LocationPrefsKeys.INVALID_LOKASI_ID)

        // Hanya proses jika belum ada id_lokasi yang valid
        if (currentLokasiId == LocationPrefsKeys.INVALID_LOKASI_ID) {
            Log.d(TAG, "ENTER transition: No valid location ID found. Proceeding to add location.")

            event.triggeringGeofences?.forEach { geofence: Geofence -> // Tipe eksplisit
                Log.d(TAG, ">>> LOG: Entering Geofence - ID: ${geofence.requestId}")

                scope.launch {
                    try {
                        val now = LocalDateTime.now()
                        val today = LocalDate.now()
                        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // YYYY-MM-DD
                        val jamMasukStr = now.format(dateTimeFormatter)
                        val tanggalStr = today.format(dateFormatter)
                        val requestBody = AddLocationRequest(userId, tanggalStr, jamMasukStr)

                        Log.d(TAG, "Calling addLocation API with: $requestBody")
                        val response = apiService.addLocation(requestBody) // Gunakan instance apiService

                        if (response.status && response.id_lokasi != null) {
                            val newLokasiId = response.id_lokasi
                            Log.d(TAG, "addLocation API success. Received id_lokasi: $newLokasiId")
                            prefs.edit().putInt(LocationPrefsKeys.KEY_ID_LOKASI, newLokasiId).apply()
                            Log.d(TAG, "Saved id_lokasi: $newLokasiId to SharedPreferences.")
                        } else {
                            Log.e(TAG, "addLocation API failed or id_lokasi null. Message: ${response.message}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception during addLocation API call: ${e.message}", e)
                    }
                }
            }
        } else {
            Log.d(TAG, "ENTER transition: Valid location ID ($currentLokasiId) already exists. Skipping add location API call.")
        }
    }

    // Fungsi untuk menangani transisi EXIT
    private fun handleExitTransition(
        userId: Int,
        prefs: SharedPreferences,
        event: GeofencingEvent
    ) {
        val currentLokasiId = prefs.getInt(LocationPrefsKeys.KEY_ID_LOKASI, LocationPrefsKeys.INVALID_LOKASI_ID)

        // Hanya proses jika ADA id_lokasi yang valid
        if (currentLokasiId != LocationPrefsKeys.INVALID_LOKASI_ID) {
            Log.d(TAG, "EXIT transition: Found valid location ID ($currentLokasiId). Proceeding to update location.")

            event.triggeringGeofences?.forEach { geofence: Geofence -> // Tipe eksplisit
                Log.d(TAG, ">>> LOG: Exiting Geofence - ID: ${geofence.requestId}")

                scope.launch {
                    try {
                        val now = LocalDateTime.now()
                        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                        val jamKeluarStr = now.format(dateTimeFormatter)
                        val requestBody = UpdateLocationRequest(currentLokasiId, userId, jamKeluarStr)

                        Log.d(TAG, "Calling updateLocation API with: $requestBody")
                        val response = apiService.updateLocation(requestBody) // Gunakan instance apiService

                        if (response.status) {
                            Log.d(TAG, "updateLocation API success. Message: ${response.message}")
                            prefs.edit().remove(LocationPrefsKeys.KEY_ID_LOKASI).apply()
                            Log.d(TAG, "Cleared id_lokasi from SharedPreferences.")
                        } else {
                            Log.e(TAG, "updateLocation API failed. Message: ${response.message}")
                            // Pertimbangkan untuk tidak menghapus id_lokasi jika API gagal agar bisa dicoba lagi
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception during updateLocation API call: ${e.message}", e)
                    }
                }
            }
        } else {
            Log.d(TAG, "EXIT transition: No valid location ID found. Skipping update location API call.")
        }
    }
}
