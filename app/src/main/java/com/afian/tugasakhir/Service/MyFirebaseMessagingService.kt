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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Random

//// Body untuk request token registration (buat data class jika perlu)
//data class RegisterTokenRequest(val user_id: Int, val token: String)

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Dipanggil ketika token FCM baru dibuat atau diperbarui.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // Kirim token ini ke backend Anda
        sendRegistrationToServer(token)
    }

    /**
     * Mengirim token ke backend.
     */
    private fun sendRegistrationToServer(token: String?) {
        if (token == null) return

        // Ambil user_id dari SharedPreferences (asumsi user sudah login)
        val userPrefs = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = userPrefs.getInt("user_id", -1)
        val userRole = userPrefs.getString("user_role", null)

        // Hanya kirim token jika user adalah mahasiswa dan user_id valid
        if (userId != -1 && userRole == "mhs") {
            Log.d(TAG, "Sending FCM token to server for user_id: $userId")
            serviceScope.launch {
                try {
                    // Buat endpoint baru di ApiService Anda untuk ini
                    // Contoh: suspend fun registerFcmToken(@Body body: RegisterTokenRequest): Response<Void> // atau Response Body lain
                    val requestBody = mapOf("user_id" to userId, "token" to token) // Atau gunakan data class
                    // Panggil API Retrofit Anda untuk mengirim token
                    // Pastikan Anda sudah menambahkan fungsi ini di ApiService & RetrofitClient
                    // Contoh pemanggilan (sesuaikan dengan implementasi Anda):
                    // val response = RetrofitClient.apiService.registerFcmToken(requestBody)
                    // if (response.isSuccessful) {
                    //     Log.i(TAG, "FCM Token registered successfully on backend.")
                    // } else {
                    //     Log.w(TAG, "Failed to register FCM token on backend. Code: ${response.code()}")
                    // }

                    // --- GANTI DENGAN PEMANGGILAN API ANDA YANG SEBENARNYA ---
                    Log.w(TAG, "TODO: Implement API call to send token to backend!")
                    // contoh (jika endpoint di ApiService ada):
                    // RetrofitClient.apiService.registerFcmToken(RegisterTokenRequest(userId, token))
                    // Log.i(TAG, "API call to register token initiated.")
                    // --- AKHIR BAGIAN YANG PERLU DIGANTI ---


                } catch (e: Exception) {
                    Log.e(TAG, "Error sending FCM token to server: ${e.message}", e)
                }
            }
        } else {
            Log.d(TAG, "User not logged in as student or invalid user ID ($userId). Token not sent.")
        }
    }

    /**
     * Dipanggil ketika pesan FCM diterima SAAT APLIKASI DI FOREGROUND.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Cek jika pesan berisi payload notifikasi (dikirim dari backend)
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Notification Message Title: ${notification.title}")
            Log.d(TAG, "Notification Message Body: ${notification.body}")

            // Tampilkan notifikasi lokal karena app di foreground
            sendLocalNotification(notification.title, notification.body)
        }

        // Cek jika pesan berisi data payload (jika Anda menambahkannya di backend)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            // Anda bisa proses data payload di sini jika perlu
        }
    }

    /**
     * Membuat dan menampilkan notifikasi lokal sederhana.
     */
    private fun sendLocalNotification(title: String?, messageBody: String?) {
        if (title.isNullOrBlank() || messageBody.isNullOrBlank()) return

        val channelId = getString(R.string.default_notification_channel_id) // Definisikan di strings.xml
        val notificationId = Random().nextInt() // ID unik untuk notifikasi

        // Intent untuk membuka MainActivity saat notifikasi diklik
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, notificationId /* Request code unik */, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Ganti dengan ikon notifikasi Anda
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)

        // Buat Channel Notifikasi (Penting untuk Android 8.0+)
        createNotificationChannel(notificationManager, channelId)

        // Tampilkan Notifikasi (Perlu Izin di Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(notificationId, notificationBuilder.build())
            } else {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification.")
                // Idealnya, beri tahu user bahwa izin diperlukan
            }
        } else {
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }

    /**
     * Membuat channel notifikasi jika belum ada (Wajib untuk Android 8.0+)
     */
    private fun createNotificationChannel(notificationManager: NotificationManagerCompat, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.default_notification_channel_name) // Definisikan di strings.xml
            val channelDescription = getString(R.string.default_notification_channel_description) // Definisikan di strings.xml
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            // Register channel ke sistem
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $channelId")
        }
    }
}