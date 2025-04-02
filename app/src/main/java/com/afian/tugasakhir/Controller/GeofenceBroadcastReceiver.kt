package com.afian.tugasakhir.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Log.e("BuatArea", "GeofencingEvent is null")
            return
        }

        // Periksa apakah geofencingEvent tidak null
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                Log.e("BuatArea", "Error code: ${geofencingEvent.errorCode}")
                return
            }

            // Dapatkan jenis transisi
            when (geofencingEvent.geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    Log.d("BuatArea", "Entering geofence")
                    // TODO: Tindakan saat memasuki geofence
                }
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    Log.d("BuatArea", "Exiting geofence")
                    // TODO: Tindakan saat keluar dari geofence
                }
                else -> {
                    Log.e("BuatArear", "Invalid transition type")
                }
            }
        } else {
            Log.e("BuatArear", "Geofencing event is null")
        }
    }
}