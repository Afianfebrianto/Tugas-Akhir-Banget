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

        var notificationTitle: String? = null
        var notificationBody: String? = null

        // Prioritaskan ambil dari DATA payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            notificationTitle = remoteMessage.data["title"]
            notificationBody = remoteMessage.data["body"]
        }
        // Fallback ke NOTIFICATION payload (jarang terjadi jika backend kirim data)
        else {
            remoteMessage.notification?.let {
                notificationTitle = it.title
                notificationBody = it.body
            }
        }

        // Simpan ke DB dan tampilkan notifikasi jika ada data
        if (!notificationTitle.isNullOrBlank() && !notificationBody.isNullOrBlank()) {
            val receivedTimestamp = System.currentTimeMillis()
            val historyItem = NotificationHistoryItem(
                title = notificationTitle,
                body = notificationBody,
                timestamp = receivedTimestamp
            )

            // Simpan ke database
            serviceScope.launch {
                try {
                    notificationDao.insert(historyItem)
                    Log.d(TAG, "Notification saved to history database.")
                } catch(e: Exception) {
                    Log.e(TAG, "Error saving notification to DB: ${e.message}", e)
                }
            }

            // Tampilkan notifikasi lokal
            sendLocalNotification(notificationTitle, notificationBody)
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

    // Fungsi tampilkan notifikasi lokal
    private fun sendLocalNotification(title: String?, messageBody: String?) {
        if (title.isNullOrBlank() || messageBody.isNullOrBlank()) return
        val channelId = getString(R.string.default_notification_channel_id)
        val notificationId = Random().nextInt()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Ganti icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)
        createNotificationChannel(notificationManager, channelId) // Buat channel jika perlu

        // Tampilkan Notifikasi (cek izin Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification.")
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
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
}