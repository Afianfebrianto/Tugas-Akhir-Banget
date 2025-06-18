package com.afian.tugasakhir.Service

// Di suatu tempat, misal file Constants.kt atau di dalam Receiver/ViewModel
object LocationPrefsKeys {
    const val PREFS_NAME = "location_state_prefs"
    const val KEY_ID_LOKASI = "id_lokasi"
    const val INVALID_LOKASI_ID = -1 // Nilai default jika id_lokasi tidak ada/valid

    const val KEY_ACTIVE_GEOFENCE_COUNT = "key_active_geofence_count"
}