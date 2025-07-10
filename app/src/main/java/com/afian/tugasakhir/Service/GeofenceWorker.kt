package com.afian.tugasakhir.Service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.afian.tugasakhir.API.ApiService
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.AddLocationRequest
import com.afian.tugasakhir.Model.UpdateLocationRequest
import com.google.android.gms.location.Geofence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GeofenceWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val mutex = Mutex()

    private val TAG = "GeofenceWorker"
    private val apiService: ApiService = RetrofitClient.apiService

    companion object {
        // Keys untuk mengirim data ke Worker
        const val KEY_TRANSITION_TYPE = "key_transition_type"
        const val KEY_USER_ID = "key_user_id"
    }

    override suspend fun doWork(): Result {
        // Pindahkan semua logika ke background thread dengan Dispatchers.IO
        return withContext(Dispatchers.IO) {
            val transitionType = inputData.getInt(KEY_TRANSITION_TYPE, -1)
            val userId = inputData.getInt(KEY_USER_ID, -1)

            if (userId == -1 || transitionType == -1) {
                Log.e(TAG, "Invalid input data received. Cannot process work.")
                return@withContext Result.failure()
            }

            val locationPrefs = appContext.getSharedPreferences(
                LocationPrefsKeys.PREFS_NAME,
                Context.MODE_PRIVATE
            )

            // Gunakan synchronized untuk keamanan thread, sama seperti sebelumnya
            mutex.withLock {
                when (transitionType) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> handleEnter(userId, locationPrefs)
                    Geofence.GEOFENCE_TRANSITION_EXIT -> handleExit(userId, locationPrefs)
                    else -> {
                        Log.e(TAG, "Unknown transition type in worker: $transitionType")
                        Result.failure()
                    }
                }
            }
        }
    }

    // Logika dari handleEnterTransition dipindahkan ke sini
    private suspend fun handleEnter(userId: Int, prefs: SharedPreferences): Result {
        val oldCount = prefs.getInt(LocationPrefsKeys.KEY_ACTIVE_GEOFENCE_COUNT, 0)
        val newCount = oldCount + 1 // Asumsi 1 event per worker
        prefs.edit().putInt(LocationPrefsKeys.KEY_ACTIVE_GEOFENCE_COUNT, newCount).apply()
        Log.i(TAG, "WORKER-ENTER: Count changed $oldCount -> $newCount.")
        Log.d(TAG, "Geofence ENTER detected. oldCount=$oldCount, newCount=$newCount")


        if (oldCount == 0 && newCount > 0) {
            Log.i(TAG, "WORKER-ENTER: First entry detected! Calling API.")
            return try {
                val now = LocalDateTime.now()
                val today = LocalDate.now()
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
                val requestBody = AddLocationRequest(userId, today.format(dateFormatter), now.format(dateTimeFormatter))

                val response = apiService.addLocation(requestBody)

                if (response.status && response.id_lokasi != null) {
                    prefs.edit().putInt(LocationPrefsKeys.KEY_ID_LOKASI, response.id_lokasi).apply()
                    Log.i(TAG, "WORKER-ENTER: API Success, saved lokasiId: ${response.id_lokasi}")
                    Result.success()
                } else {
                    Log.e(TAG, "WORKER-ENTER: API returned an error: ${response.message}")
                    Result.retry() // API gagal, coba lagi nanti
                }
            } catch (e: Exception) {
                Log.e(TAG, "WORKER-ENTER: Network exception", e)
                Result.retry() // Gagal karena jaringan, coba lagi nanti
            }
        }
        Log.d(TAG, "WORKER-ENTER: Not first entry, work successful without API call.")
        return Result.success()
    }

    // Logika dari handleExitTransition dipindahkan ke sini
    private suspend fun handleExit(userId: Int, prefs: SharedPreferences): Result {
        val oldCount = prefs.getInt(LocationPrefsKeys.KEY_ACTIVE_GEOFENCE_COUNT, 0)
        val newCount = maxOf(0, oldCount - 1)
        prefs.edit().putInt(LocationPrefsKeys.KEY_ACTIVE_GEOFENCE_COUNT, newCount).apply()
        Log.i(TAG, "WORKER-EXIT: Count changed $oldCount -> $newCount.")

        if (newCount == 0 && oldCount > 0) {
            val lokasiId = prefs.getInt(LocationPrefsKeys.KEY_ID_LOKASI, LocationPrefsKeys.INVALID_LOKASI_ID)
            if (lokasiId == LocationPrefsKeys.INVALID_LOKASI_ID) {
                Log.e(TAG, "WORKER-EXIT: Final exit but no lokasiId found!")
                return Result.failure() // Ada yang salah dengan state, gagal permanen
            }
            Log.i(TAG, "WORKER-EXIT: Final exit detected! Calling API for lokasiId: $lokasiId.")
            return try {
                val now = LocalDateTime.now()
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val requestBody = UpdateLocationRequest(lokasiId, userId, now.format(dateTimeFormatter))

                val response = apiService.updateLocation(requestBody)

                if (response.status) {
                    prefs.edit()
                        .remove(LocationPrefsKeys.KEY_ID_LOKASI)
                        .remove(LocationPrefsKeys.KEY_ACTIVE_GEOFENCE_COUNT)
                        .apply()
                    Log.i(TAG, "WORKER-EXIT: API Success, cleared location state.")
                    Result.success()
                } else {
                    Log.e(TAG, "WORKER-EXIT: API returned an error: ${response.message}")
                    Result.retry()
                }
            } catch (e: Exception) {
                Log.e(TAG, "WORKER-EXIT: Network exception", e)
                Result.retry()
            }
        }
        Log.d(TAG, "WORKER-EXIT: Not final exit, work successful without API call.")
        return Result.success()
    }
}