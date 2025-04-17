package com.afian.tugasakhir.Service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider // Import FileProvider jika menggunakannya nanti
import java.io.File // Import File jika perlu

class DownloadCompleteReceiver : BroadcastReceiver() {

    private val TAG = "DownloadCompleteRcvr"

    // Helper untuk menjalankan di Main Thread (untuk Toast)
    private fun runOnMainThread(action: () -> Unit) {
        Handler(Looper.getMainLooper()).post(action)
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, ">>> DownloadCompleteReceiver.onReceive TRIGGERED <<<")
        if (context == null || intent == null || DownloadManager.ACTION_DOWNLOAD_COMPLETE != intent.action) {
            Log.w(TAG, "Invalid context, intent, or action: ${intent?.action}")
            return
        }

        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        Log.d(TAG, "ACTION_DOWNLOAD_COMPLETE received for download ID: $id")
        if (id == -1L) { Log.e(TAG, "Invalid download ID (-1)"); return }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(id)
        var cursor: Cursor? = null
        try {
            cursor = downloadManager.query(query)
            Log.d(TAG, "Query executed for ID: $id. Cursor is null: ${cursor == null}")

            if (cursor != null && cursor.moveToFirst()) {
                Log.d(TAG, "Cursor moved to first for ID: $id")

                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI) // Tetap ambil URI ini
                val mimeTypeIndex = cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)
                val titleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)

                val title = if(titleIndex != -1) cursor.getString(titleIndex) ?: "N/A" else "N/A"
                Log.d(TAG, "Processing download: '$title' (ID: $id)")

                if (statusIndex != -1) {
                    val status = cursor.getInt(statusIndex)
                    val reason = if (reasonIndex != -1) cursor.getInt(reasonIndex) else 0
                    Log.d(TAG, "Download '$title' Status: $status, Reason Code: $reason")

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        // --- 👇 LOGIKA BARU UNTUK SUKSES 👇 ---
                        Log.i(TAG,"Download $id SUCCESSFUL")
                        if (uriIndex != -1 && mimeTypeIndex != -1) {
                            val downloadedUriString = cursor.getString(uriIndex)
                            val mimeType = cursor.getString(mimeTypeIndex)
                            Log.i(TAG, "Attempting to open file. Original URI String: $downloadedUriString, MIME: $mimeType")

                            if (!downloadedUriString.isNullOrBlank() && !mimeType.isNullOrBlank()) {
                                try {
                                    val originalUri = Uri.parse(downloadedUriString)
                                    // Panggil fungsi yang menggunakan FileProvider
                                    openFileWithFileProvider(context, originalUri, mimeType)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing URI or opening file", e)
                                    runOnMainThread { Toast.makeText(context, "Gagal membuka file: Format URI tidak valid.", Toast.LENGTH_LONG).show() }
                                }
                            } else { /* Handle null URI/MIME */ }
                        } else { /* Handle kolom tidak ditemukan */ }
                        // --- 👆 AKHIR LOGIKA BARU 👆 ---
                    } else { /* Handle FAILED status (tidak berubah) */ }
                } else { /* Handle kolom status tidak ditemukan */ }
            } else { /* Handle cursor null/kosong */ }
        } catch (e: Exception) { /* Handle exception query */ }
        finally { cursor?.close() }
        Log.d(TAG, "Finished processing broadcast for ID: $id")
    }

    // --- 👇 FUNGSI OPEN FILE MENGGUNAKAN FILEPROVIDER 👇 ---
    private fun openFileWithFileProvider(context: Context, originalUri: Uri, mimeType: String) {
        try {
            val contentUri: Uri

            // Cek skema URI asli dari DownloadManager
            if ("file".equals(originalUri.scheme, ignoreCase = true)) {
                // Jika skemanya file://, konversi ke File lalu ke content:// via FileProvider
                Log.d(TAG, "Original URI is file://. Converting using FileProvider.")
                val filePath = originalUri.path // Ambil path dari file:// URI
                if (filePath == null) throw IllegalArgumentException("File path is null for file URI")
                val file = File(filePath)

                if (!file.exists()) {
                    Log.e(TAG, "File does not exist at path: $filePath derived from $originalUri")
                    runOnMainThread { Toast.makeText(context, "File tidak ditemukan ($filePath).", Toast.LENGTH_LONG).show() }
                    return
                }

                // Bentuk authority (HARUS SAMA dengan di Manifest)
                val authority = "${context.packageName}.provider"
                contentUri = FileProvider.getUriForFile(context, authority, file)
                Log.i(TAG, "Converted file:// to content:// URI: $contentUri")
            } else if ("content".equals(originalUri.scheme, ignoreCase = true)) {
                // Jika sudah content:// (misal dari DownloadManager di Android versi baru),
                // kita tetap gunakan itu, karena FileProvider mungkin tidak bisa dibuat darinya.
                // Flag GRANT_READ_URI_PERMISSION akan tetap penting.
                Log.d(TAG, "Original URI is content:// ($originalUri). Using it directly.")
                contentUri = originalUri
            } else {
                // Skema URI tidak dikenal
                throw IllegalArgumentException("Unsupported URI scheme: ${originalUri.scheme}")
            }

            // Buat Intent ACTION_VIEW dengan URI final (hasil konversi atau asli)
            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Beri izin baca
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Wajib dari Receiver
            }

            context.startActivity(openIntent)
            Log.i(TAG,"Intent ACTION_VIEW sent for final URI: $contentUri")

        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error generating/validating URI: ${e.message}", e)
            runOnMainThread { Toast.makeText(context, "Gagal memproses URI file.", Toast.LENGTH_LONG).show() }
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "No activity found to handle MIME type: $mimeType", e)
            runOnMainThread { Toast.makeText(context, "Tidak ada aplikasi untuk membuka file ($mimeType).", Toast.LENGTH_LONG).show() }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening file with final URI $originalUri", e)
            runOnMainThread { Toast.makeText(context, "Gagal membuka file.", Toast.LENGTH_SHORT).show() }
        }
    }
    // --- 👆 AKHIR FUNGSI OPEN FILE 👆 ---

    // Fungsi getDownloadErrorReason (tidak berubah)
    private fun getDownloadErrorReason(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "Tidak Bisa Melanjutkan"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Device Tidak Ditemukan"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File Sudah Ada"
            DownloadManager.ERROR_FILE_ERROR -> "Kesalahan File"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "Kesalahan Data HTTP ($reason)" // Tambahkan kode asli
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Ruang Tidak Cukup"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Terlalu Banyak Redirect"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Kode HTTP Tidak Diketahui ($reason)" // Tambahkan kode asli
            DownloadManager.ERROR_UNKNOWN -> "Kesalahan Tidak Diketahui"
            else -> "Kode Alasan: $reason"
        }
    }
}