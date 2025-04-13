package com.afian.tugasakhir.Service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.afian.tugasakhir.MainActivity
import com.afian.tugasakhir.R
import com.afian.tugasakhir.data.AppDatabase
import com.afian.tugasakhir.data.NotificationDao
import com.afian.tugasakhir.data.NotificationHistoryItem
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Dapatkan DAO secara manual menggunakan lazy delegate dan AppDatabase singleton
    private val notificationDao: NotificationDao by lazy {
        AppDatabase.getDatabase(applicationContext).notificationDao()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token) // Kirim token ke backend
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message data payload: ${remoteMessage.data}") // Selalu log data payload

        // Gunakan WHEN untuk penanganan tipe yang lebih rapi
        when (remoteMessage.data["type"]) {
            "panggilan_respon" -> { // <-- PASTIKAN CASE INI ADA DAN BENAR
                Log.i(TAG, "Handling 'panggilan_respon' notification...")
                val title = remoteMessage.data["title"] ?: "Respons Panggilan"
                val body = remoteMessage.data["body"] ?: "Mahasiswa telah merespons."
                val panggilanId = remoteMessage.data["panggilan_id"]
                val mahasiswaName = remoteMessage.data["mahasiswa_name"]
                val statusRespon = remoteMessage.data["status_respon"]

                // 1. Simpan ke riwayat notifikasi umum (jika perlu)
                saveNotificationToHistory(title, body) // Panggil helper jika ada

                // 2. Tampilkan notifikasi sistem ke Dosen
                // Intent bisa diarahkan ke layar riwayat panggilan Dosen
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("navigateTo", "riwayat_panggilan_dosen") // Sesuaikan route
                    putExtra("panggilan_id", panggilanId)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                val requestCode = panggilanId?.toIntOrNull() ?: Random().nextInt()
                val pendingIntent = PendingIntent.getActivity(this, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                sendLocalNotification(title, body, pendingIntent, notificationId = requestCode)

                // 3. TODO (Opsional): Trigger refresh data di DosenViewModel jika layar riwayat sedang aktif
                //    (Membutuhkan cara komunikasi antar komponen, misal SharedFlow/EventBus)

            }

            "panggilan_dosen" -> { // <-- PASTIKAN EJAAN INI BENAR!
                Log.i(TAG, "Handling 'panggilan_dosen' notification...")
                val title = remoteMessage.data["title"] ?: "Panggilan Dosen"
                val body = remoteMessage.data["body"] ?: "Seorang dosen mencari Anda."
                val panggilanId = remoteMessage.data["panggilan_id"]

                saveNotificationToHistory(title, body) // Simpan ke history umum

                // Tampilkan notifikasi sistem (arahkan ke riwayat panggilan)
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("navigateTo", "riwayat_panggilan") // Route mahasiswa
                    putExtra("panggilan_id", panggilanId)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                val requestCode = panggilanId?.toIntOrNull() ?: Random().nextInt()
                val pendingIntent = PendingIntent.getActivity(this, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                sendLocalNotification(title, body, pendingIntent, notificationId = requestCode)

                // TODO: Trigger refresh data di NotificationViewModel jika perlu
            }


            "geofence_masuk" -> { // <-- TAMBAHKAN CASE INI
                // --- Logika Handle Notifikasi Dosen Masuk Geofence ---
                val title = remoteMessage.data["title"] ?: "Dosen Tiba"
                val body = remoteMessage.data["body"] ?: "Seorang dosen telah memasuki area."
                val dosenName = remoteMessage.data["dosenName"] ?: "Dosen" // Ambil nama jika ada
                Log.i(TAG,"Received 'geofence_masuk': Dosen=$dosenName")

                saveNotificationToHistory(title, body) // Simpan ke history umum

                // Tampilkan Notifikasi Sistem (arahkan ke layar daftar dosen di kampus?)
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("navigateTo", "dosen_di_kampus") // Contoh route, sesuaikan
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                val requestCode = Random().nextInt() // ID notif unik
                val pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                sendLocalNotification(title, body, pendingIntent, notificationId = requestCode)
            }

            // --- Handle Tipe Lain atau Notifikasi Standar ---
            else -> {
                // Cek jika ada payload 'notification' (untuk kompatibilitas atau foreground)
                remoteMessage.notification?.let {
                    Log.d(TAG, "Handling standard notification payload: Title=${it.title}, Body=${it.body}")
                    saveNotificationToHistory(it.title, it.body)
                    val intent = Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                    val requestCode = Random().nextInt()
                    val pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
                    sendLocalNotification(it.title, it.body, pendingIntent, notificationId = requestCode)
                } ?: run {
                    // Jika tidak ada payload notifikasi dan tipe data tidak dikenal
                    Log.w(TAG, "Received unknown data message type or notification-only message in background: ${remoteMessage.data}")
                }
            }
        }
    }

    // Fungsi kirim token ke backend (memanggil FcmRepository atau langsung API call)
    private fun sendRegistrationToServer(token: String?) {
        if (token == null) return
        val userPrefs = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = userPrefs.getInt("user_id", -1)
        val isLoggedIn = userPrefs.getBoolean("is_logged_in", false)
        val userRole = userPrefs.getString("user_role", null)

        // --- 👇 MODIFIKASI: Kirim token jika Dosen ATAU Mahasiswa sedang login 👇 ---
        if (isLoggedIn && userId != -1 && (userRole == "mhs" || userRole == "dosen")) {
            Log.d(TAG, "User logged in as ($userRole) with ID ($userId). Sending refreshed token via Repository...")
            serviceScope.launch {
                FcmRepository.sendTokenToServer(userId, token) // Panggil Repository
            }
        } else {
            Log.d(TAG, "No eligible user (Dosen/Mhs) logged in. Refreshed token not sent to server yet. Role: $userRole, LoggedIn: $isLoggedIn, UserID: $userId")
        }
        // --- 👆 AKHIR MODIFIKASI 👆 ---
    }

    // Modifikasi sendLocalNotification untuk menerima PendingIntent & ID
    private fun sendLocalNotification(
        title: String?,
        messageBody: String?,
        pendingIntent: PendingIntent, // Terima PendingIntent
        notificationId: Int = Random().nextInt() // Terima ID unik
    ) {
        if (title.isNullOrBlank() || messageBody.isNullOrBlank()) return
        val channelId = getString(R.string.default_notification_channel_id)
        // Buat Notifikasi
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Tinggikan prioritas untuk panggilan?
            .setContentIntent(pendingIntent) // Gunakan PendingIntent yang diberikan

        val notificationManager = NotificationManagerCompat.from(this)
        createNotificationChannel(notificationManager, channelId)

        // Tampilkan Notifikasi (cek izin)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted.")
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build()) // Gunakan ID unik
    }

    // Fungsi buat channel notifikasi
    private fun createNotificationChannel(notificationManager: NotificationManagerCompat, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.default_notification_channel_name)
            val channelDescription = getString(R.string.default_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun saveNotificationToHistory(title: String?, body: String?) {
        // Hanya simpan jika ada isi pesan (body)
        if (body.isNullOrBlank()) {
            Log.w(TAG, "saveNotificationToHistory skipped: Notification body is null or blank.")
            return
        }

        val receivedTimestamp = System.currentTimeMillis() // Waktu saat ini

        // Buat objek Entity untuk disimpan
        val historyItem = NotificationHistoryItem(
            // id = 0, // Biarkan Room yang generate ID
            title = title, // Simpan title (bisa null)
            body = body,   // Simpan body
            timestamp = receivedTimestamp // Simpan waktu diterima
        )

        // Jalankan operasi database di background thread
        serviceScope.launch {
            try {
                notificationDao.insert(historyItem) // Panggil fungsi insert DAO
                Log.d(TAG, "Notification saved to local history DB: Title='${historyItem.title}', Body='${historyItem.body}'")
            } catch (e: Exception) {
                // Tangani jika ada error saat menyimpan ke DB
                Log.e(TAG, "Error saving notification to Room DB: ${e.message}", e)
            }
        }
    }
}