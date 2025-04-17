package com.afian.tugasakhir.Controller

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.UploadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import androidx.compose.runtime.State
import okhttp3.RequestBody.Companion.toRequestBody

class UserAddViewModel : ViewModel() {
    private val TAG = "UserAddViewModel"
    private val apiService = RetrofitClient.apiService

    // State untuk URI file yang dipilih
    private val _selectedFileUri = mutableStateOf<Uri?>(null)
    // val selectedFileUri: State<Uri?> = _selectedFileUri // Tidak perlu diekspos jika hanya nama yg tampil

    // State untuk nama file yang dipilih (untuk ditampilkan di UI)
    private val _selectedFileName = mutableStateOf<String?>(null)
    val selectedFileName: State<String?> = _selectedFileName

    // State untuk status loading upload
    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    // State untuk hasil upload (sukses/gagal + pesan/error)
    private val _uploadResult = mutableStateOf<UploadResult?>(null)
    val uploadResult: State<UploadResult?> = _uploadResult

    // State untuk error umum (misal: jaringan)
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage


    /** Dipanggil saat user memilih file dari picker */
    fun onFileSelected(uri: Uri?, context: Context) {
        _selectedFileUri.value = uri
        _selectedFileName.value = if (uri != null) getFileName(context.contentResolver, uri) else null
        _uploadResult.value = null // Reset hasil sebelumnya saat file baru dipilih
        _errorMessage.value = null // Reset error
        Log.d(TAG, "File selected: $uri, Name: ${_selectedFileName.value}")
    }

    /** Fungsi utama untuk upload file Excel */
    fun uploadExcelFile(context: Context) {
        val fileUri = _selectedFileUri.value
        val fileName = _selectedFileName.value

        // Validasi awal
        if (fileUri == null || fileName == null) {
            _errorMessage.value = "Silakan pilih file Excel terlebih dahulu."
            return
        }
        if (_isUploading.value) {
            Log.w(TAG, "Upload already in progress.")
            return // Hindari upload ganda
        }

        _isUploading.value = true
        _errorMessage.value = null
        _uploadResult.value = null // Reset hasil

        viewModelScope.launch(Dispatchers.IO) { // Lakukan di background thread
            try {
                Log.d(TAG, "Starting upload process for: $fileName")
                // Dapatkan InputStream dari ContentResolver
                context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    // Baca semua byte dari stream
                    val fileBytes = inputStream.readBytes()

                    // Buat RequestBody dari byte array
                    // Dapatkan tipe MIME dari resolver atau hardcode jika pasti .xlsx
                    val mimeType = context.contentResolver.getType(fileUri) ?: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    val requestFileBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())

                    // Buat MultipartBody.Part
                    // Nama part ("fileExcel") harus SAMA dengan yang diharapkan backend (multer)
                    val bodyPart = MultipartBody.Part.createFormData("fileExcel", fileName, requestFileBody)

                    // Panggil API Service
                    Log.d(TAG, "Calling uploadUserExcel API...")
                    val response = apiService.uploadUserExcel(bodyPart)

                    // Kembali ke Main thread untuk update state UI
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val result = response.body()!!
                            Log.i(TAG, "Upload API success: ${result.message}")
                            _uploadResult.value = UploadResult(
                                success = result.status,
                                message = result.message,
                                errors = result.errors
                            )
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "Gagal upload (Error: ${response.code()})"
                            Log.e(TAG, "Upload API failed: $errorMsg")
                            _errorMessage.value = "Gagal upload: $errorMsg"
                        }
                    }
                } ?: run {
                    // Gagal membuka InputStream
                    Log.e(TAG, "Failed to open InputStream for URI: $fileUri")
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Gagal membaca file yang dipilih."
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during upload process", e)
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Terjadi kesalahan: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isUploading.value = false // Set loading false setelah selesai
                    Log.d(TAG, "Upload process finished.")
                }
            }
        }
    }

    /** Helper untuk mendapatkan nama file dari Uri */
    private fun getFileName(resolver: ContentResolver, uri: Uri): String? {
        var fileName: String? = null
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}