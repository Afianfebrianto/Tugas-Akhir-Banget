package com.afian.tugasakhir.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

// <--- PASTIKAN nama kelas ini SAMA PERSIS dengan yang di Manifest dan Intent Helper
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "GeofenceReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        // =====> TAMBAHKAN LOG INI <=====
        Log.d(TAG, "<<< onReceive CALLED! >>> Intent action: ${intent?.action}")

        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent is null, cannot process.")
            return
        }

        // Coba ekstrak event
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // =====> TAMBAHKAN LOG INI <=====
        Log.d(TAG, "GeofencingEvent extracted. Is null: ${geofencingEvent == null}")

        if (geofencingEvent == null) {
            Log.e(TAG,"Error receiving geofence event: event object is null after extraction.")
            // Log detail intent jika event null, mungkin ini bukan intent geofence?
            Log.w(TAG, "Intent details: action=${intent.action}, extras=${intent.extras}")
            return
        }


        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofence Error Code ${geofencingEvent.errorCode}: $errorMessage")
            return
        }

        // Proses transisi jika tidak ada error dan event valid
        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.d(TAG, "Geofence Transition detected: $geofenceTransition") // Log tipe transisi

        val triggeringGeofences = geofencingEvent.triggeringGeofences
        Log.d(TAG, "Triggering Geofences count: ${triggeringGeofences?.size ?: 0}") // Log jumlah geofence

        // Log spesifik untuk Enter/Dwell/Exit (seperti sebelumnya)
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                triggeringGeofences?.forEach { geofence ->
                    Log.d(TAG, ">>> LOG: Entering Geofence - ID: ${geofence.requestId}")
                }
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                triggeringGeofences?.forEach { geofence ->
                    Log.d(TAG, ">>> LOG: Dwelling inside Geofence - ID: ${geofence.requestId}")
                }
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                triggeringGeofences?.forEach { geofence ->
                    Log.d(TAG, ">>> LOG: Exiting Geofence - ID: ${geofence.requestId}")
                }
            }
            else -> {
                Log.e(TAG, "Unknown Geofence Transition type: $geofenceTransition")
            }
        }
    }
}