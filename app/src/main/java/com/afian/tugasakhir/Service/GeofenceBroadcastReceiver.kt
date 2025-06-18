package com.afian.tugasakhir.Service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
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

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "<<< Geofence Event Received by Receiver >>>")

        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent is null.")
            return
        }

        val appContext = context.applicationContext
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null || geofencingEvent.hasError()) {
            val errorMessage = if (geofencingEvent != null) GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode) else "Event is null"
            Log.e(TAG, "Geofence Error or Null Event: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val userPrefs = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = userPrefs.getInt("user_id", -1)

        if (userId == -1) {
            Log.e(TAG, "User ID not found. Cannot schedule work.")
            return
        }

        // Jadwalkan pekerjaan menggunakan WorkManager
        scheduleGeofenceWork(appContext, geofenceTransition, userId)
    }

    private fun scheduleGeofenceWork(context: Context, transitionType: Int, userId: Int) {
        // 1. Buat batasan: hanya jalan jika ada jaringan
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 2. Siapkan data untuk dikirim ke Worker
        val inputData = workDataOf(
            GeofenceWorker.KEY_TRANSITION_TYPE to transitionType,
            GeofenceWorker.KEY_USER_ID to userId
        )

        // 3. Buat Work Request
        val geofenceWorkRequest = OneTimeWorkRequestBuilder<GeofenceWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        // 4. Jalankan Work Request
        WorkManager.getInstance(context).enqueue(geofenceWorkRequest)

        val transitionName = if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) "ENTER" else "EXIT"
        Log.i(TAG, "Work scheduled for user $userId with transition: $transitionName. WorkManager will handle execution.")
    }
}
