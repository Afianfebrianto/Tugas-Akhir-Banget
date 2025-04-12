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

        // Cek jika ini adalah notifikasi panggilan dosen
        if (remoteMessage.data["type"] == "panggilan_dosen") {
            val title = remoteMessage.data["title"] ?: "Panggilan Dosen"
            val body = remoteMessage.data["body"] ?: "Seorang dosen mencari Anda."
            val panggilanId = remoteMessage.data["panggilan_id"]
            val dosenName = remoteMessage.data["dosen_name"]

            Log.i(TAG,"Panggilan Dosen received: ID=$panggilanId, From=$dosenName")

            // 1. Simpan ke Riwayat Lokal (Room DB)
            val historyItem = NotificationHistoryItem(
                // Anda bisa modifikasi entity atau simpan data panggilan ini secara khusus
                title = title,
                body = body,
                timestamp = System.currentTimeMillis() // Waktu terima notif
                // Mungkin tambahkan field: type="panggilan", relatedId=panggilanId.toIntOrNull()
            )
            serviceScope.launch {
                try {
                    notificationDao.insert(historyItem)
                    Log.d(TAG, "Panggilan notification saved to history database.")
                    // TODO: Mungkin trigger refresh di MahasiswaViewModel? (Use Flow/EventBus/etc.)
                } catch (e:Exception){ Log.e(TAG, "Error saving call notification to DB", e) }
            }

            // 2. Tampilkan Notifikasi Sistem
            // Intent saat notifikasi diklik -> buka layar riwayat panggilan
            val intent = Intent(this, MainActivity::class.java).apply {
                // Tambahkan extra untuk memberi tahu MainActivity/NavGraph tujuan navigasi
                putExtra("navigateTo", "riwayat_panggilan") // Definisikan konstanta
                putExtra("panggilan_id", panggilanId) // Opsional: bawa ID panggilan
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            // Buat PendingIntent yang unik untuk setiap notifikasi panggilan
            val requestCode = panggilanId?.toIntOrNull() ?: Random().nextInt()
            val pendingIntent = PendingIntent.getActivity(this, requestCode, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

            // Buat dan tampilkan notifikasi seperti di fungsi sendLocalNotification
            // Gunakan title dan body yang diterima dari data payload
            // Pastikan notificationId unik jika ada beberapa panggilan masuk
            sendLocalNotification(title, body, pendingIntent, notificationId = requestCode) // Modif sendLocalNotification

        }
        // Handle notifikasi tipe lain jika ada
        else {
            // ... Logika notifikasi sebelumnya (misal dari notif FCM biasa) ...
            Log.d(TAG, "Received standard FCM notification or unknown data message.")
        }
    }

    // Fungsi kirim token ke backend (memanggil FcmRepository atau langsung API call)
    private fun sendRegistrationToServer(token: String?) {
        if (token == null) return
        val userPrefs = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = userPrefs.getInt("user_id", -1)
        val isLoggedIn = userPrefs.getBoolean("is_logged_in", false)
        val userRole = userPrefs.getString("user_role", null)

        if (isLoggedIn && userId != -1 && userRole == "mhs") {
            Log.d(TAG, "User logged in as student ($userId). Sending refreshed token via Repository...")
            serviceScope.launch {
                // Asumsi Anda punya FcmRepository seperti contoh sebelumnya
                FcmRepository.sendTokenToServer(userId, token)
                // Jika tidak pakai Repository, panggil Retrofit langsung di sini
            }
        } else {
            Log.d(TAG, "No student user logged in. Refreshed token not sent.")
        }
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

//    // Fungsi tampilkan notifikasi lokal
//    private fun sendLocalNotification(title: String?, messageBody: String?) {
//        if (title.isNullOrBlank() || messageBody.isNullOrBlank()) return
//        val channelId = getString(R.string.default_notification_channel_id)
//        val notificationId = Random().nextInt()
//
//        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(
//            this, notificationId, intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notificationBuilder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.mipmap.ic_launcher) // Ganti icon
//            .setContentTitle(title)
//            .setContentText(messageBody)
//            .setAutoCancel(true)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setContentIntent(pendingIntent)
//
//        val notificationManager = NotificationManagerCompat.from(this)
//        createNotificationChannel(notificationManager, channelId) // Buat channel jika perlu
//
//        // Tampilkan Notifikasi (cek izin Android 13+)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
//            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification.")
//            return
//        }
//        notificationManager.notify(notificationId, notificationBuilder.build())
//    }

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
}