package com.afian.tugasakhir.Controller

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.UserProfileData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.compose.runtime.State
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class UserProfileViewModel : ViewModel() {
    private val TAG = "UserProfileVM"
    private val apiService = RetrofitClient.apiService

    // State untuk data profil yang sedang ditampilkan/diedit
    private val _profileData = MutableStateFlow<UserProfileData?>(null)
    val profileData: StateFlow<UserProfileData?> = _profileData.asStateFlow()

    private val _updateSuccessEvent = MutableSharedFlow<UserProfileData>() // Gunakan SharedFlow
    val updateSuccessEvent: SharedFlow<UserProfileData> = _updateSuccessEvent.asSharedFlow()

    // State untuk input field (agar bisa diedit)
    val noHpInput = mutableStateOf("")
    val informasiInput = mutableStateOf("")

    // State untuk URI gambar yang baru dipilih
    private val _selectedImageUri = mutableStateOf<Uri?>(null)
    val selectedImageUri: State<Uri?> = _selectedImageUri

    // State loading dan status penyimpanan
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    private val _isSaving = mutableStateOf(false)
    val isSaving: State<Boolean> = _isSaving
    private val _saveResult = mutableStateOf<Pair<Boolean, String>?>(null) // Pair(sukses?, pesan)
    val saveResult: State<Pair<Boolean, String>?> = _saveResult


    /** Memuat profil user dari API */
    fun loadUserProfile(userId: Int) {
        if (_isLoading.value || userId <= 0) return
        _isLoading.value = true
        _errorMessage.value = null // Asumsi ada state _errorMessage

        Log.d(TAG, "Loading profile for user ID: $userId")
        viewModelScope.launch {
            try {
                val response = apiService.getUserProfile(userId)
                if (response.status && response.profile != null) {
                    _profileData.value = response.profile
                    // Set nilai awal untuk input field dari data yang didapat
                    noHpInput.value = response.profile.no_hp ?: ""
                    informasiInput.value = response.profile.informasi ?: ""
                    _selectedImageUri.value = null // Reset pilihan gambar
                    Log.d(TAG, "Profile loaded successfully.")
                } else {
                    // response tidak punya field 'message', buat pesan error sendiri
                    val errorMsg = "Gagal memuat profil (Status: ${response.status})" // Pesan error generik
                    Log.w(TAG, errorMsg)
                    throw Exception(errorMsg) // Lemparkan exception dengan pesan baru
                }
                _errorMessage.value = null
            } catch (e: CancellationException) {
                Log.i(TAG, "Profile loading cancelled.")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile", e)
                _errorMessage.value = "Gagal memuat profil: ${e.message}"
                _profileData.value = null // Kosongkan jika error
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Dipanggil saat input No HP berubah */
    fun onNoHpChanged(newText: String) {
        noHpInput.value = newText
    }

    /** Dipanggil saat input Informasi berubah */
    fun onInformasiChanged(newText: String) {
        informasiInput.value = newText
    }

    /** Dipanggil saat user memilih gambar baru */
    fun onImageSelected(uri: Uri?) {
        _selectedImageUri.value = uri
        Log.d(TAG, "New image URI selected: $uri")
    }

    /** Menyimpan perubahan profil ke backend */
    fun saveProfile(context: Context) {
        val currentProfile = _profileData.value
        val identifier = currentProfile?.identifier
        val imageUri = _selectedImageUri.value

        if (identifier == null) {
            _saveResult.value = Pair(false, "Identifier pengguna tidak ditemukan.")
            return
        }
        if (_isSaving.value) return // Hindari save ganda

        _isSaving.value = true
        _saveResult.value = null // Reset hasil lama
        Log.d(TAG, "Attempting to save profile for identifier: $identifier")

        viewModelScope.launch(Dispatchers.IO) { // Proses file & network di IO
            var photoPart: MultipartBody.Part? = null
            var saveSuccess = false
            var message = "Gagal menyimpan profil."
            var updatedDataResult: UserProfileData? = null

            try {
                // 1. Siapkan Part untuk Foto (jika ada URI baru)
                if (imageUri != null) {
                    Log.d(TAG, "Preparing image part from URI: $imageUri")
                    context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                        val fileBytes = inputStream.readBytes()
                        val fileName = getFileName(context.contentResolver, imageUri) ?: "profile.jpg" // Dapatkan nama file asli
                        val mimeType = context.contentResolver.getType(imageUri) ?: "image/*"
                        val requestFileBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
                        // Nama part "foto_profile" HARUS SAMA dengan di backend (multer)
                        photoPart = MultipartBody.Part.createFormData("foto_profile", fileName, requestFileBody)
                        Log.d(TAG, "Image part created: $fileName, size: ${fileBytes.size}")
                    } ?: run { throw Exception("Gagal membaca file gambar terpilih.") }
                }

                // 2. Siapkan Part untuk data teks (hanya jika berubah? opsional)
                //    Untuk simple, kita kirim saja nilai saat ini
                val noHpValue = noHpInput.value
                val infoValue = informasiInput.value
                // Buat RequestBody dari String (tipe "text/plain")
                val noHpPart = noHpValue.toRequestBody("text/plain".toMediaTypeOrNull())
                val infoPart = infoValue.toRequestBody("text/plain".toMediaTypeOrNull())

                // 3. Panggil API updateUserProfile
                Log.d(TAG, "Calling updateUserProfile API...")
                val response = apiService.updateUserProfile(
                    identifier = identifier,
                    photo = photoPart, // Bisa null
                    noHp = noHpPart,
                    informasi = infoPart
                )

                // 4. Proses Response
                if (response.isSuccessful && response.body()?.status == true) {
                    saveSuccess = true
                    message = response.body()?.message ?: "Profil berhasil diperbarui."
                    Log.i(TAG, "Profile update success: $message")
                    // Jika sukses & ada foto baru, update state foto profil di ViewModel
                    // dengan URL baru dari respons (jika backend mengirimnya)
//                    val newPhotoUrl = response.body()?.updated_photo_url
                    // --- 👇 Bangun objek UserProfileData yang terupdate 👇 ---
                    val newPhotoUrl = response.body()?.updated_photo_url ?: currentProfile?.foto_profile // Ambil URL baru jika ada, jika tidak pakai yg lama
                    // Buat objek baru berdasarkan currentProfile TAPI dengan nilai input terbaru & foto baru
                    updatedDataResult = currentProfile?.copy(
                        no_hp = noHpValue,
                        informasi = infoValue,
                        foto_profile = newPhotoUrl
                    )
                    if (newPhotoUrl != null) {
                        withContext(Dispatchers.Main) {
                            _profileData.update { it?.copy(foto_profile = newPhotoUrl) }
                            _selectedImageUri.value = null // Reset pilihan gambar setelah sukses upload
                        }
                    }
                    // Muat ulang seluruh profil setelah update? Atau cukup update field yg berubah?
                    // loadUserProfile(currentProfile.user_id) // Memuat ulang semua
                    // Atau update state lokal saja:
                    withContext(Dispatchers.Main) {
                        _profileData.update { it?.copy(no_hp = noHpValue, informasi = infoValue) }
                    }


                } else {
                    message = response.body()?.message ?: "Gagal update profil (Error: ${response.code()})"
                    Log.e(TAG, "Profile update API failed: $message - ErrorBody: ${response.errorBody()?.string()}")
                }

            } catch (e: CancellationException) {
                Log.i(TAG, "Profile save cancelled.")
                message = "Penyimpanan dibatalkan."
            } catch (e: Exception) {
                Log.e(TAG, "Exception during profile save", e)
                message = "Error: ${e.message}"
            } finally {
                withContext(Dispatchers.Main) {
                    _saveResult.value = Pair(saveSuccess, message) // Set hasil akhir
                    _isSaving.value = false // Set loading false
                    Log.d(TAG, "Profile save finished.")
                    // --- 👇 TAMBAHKAN LOGGING DI SINI 👇 ---
                    if (saveSuccess && updatedDataResult != null) {
                        Log.d(TAG, ">>> Kondisi terpenuhi untuk emit: saveSuccess=$saveSuccess, data=$updatedDataResult")
                        try {
                            Log.d(TAG, ">>> MEMANGGIL _updateSuccessEvent.emit()...")
                            _updateSuccessEvent.emit(updatedDataResult)
                            Log.i(TAG, ">>> BERHASIL emit updateSuccessEvent.") // Konfirmasi emit berhasil
                        } catch (e: Exception) {
                            Log.e(TAG, ">>> GAGAL emit updateSuccessEvent", e) // Log jika emit error
                        }
                    } else {
                        Log.w(TAG, ">>> TIDAK emit updateSuccessEvent. Kondisi: saveSuccess=$saveSuccess, updatedDataResult=$updatedDataResult")
                    }
                }
            }
        }
    }

    // Fungsi helper getFileName (pindahkan ke file utilitas jika dipakai di tempat lain)
    private fun getFileName(resolver: ContentResolver, uri: Uri): String? {
        var fileName: String? = null
        // Menggunakan query ke ContentResolver untuk mendapatkan metadata file
        val cursor: Cursor? = resolver.query(uri, null, null, null, null)
        cursor?.use { // 'use' akan otomatis menutup cursor setelah selesai
            if (it.moveToFirst()) {
                // Cari indeks kolom untuk nama tampilan file
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    // Jika kolom ditemukan, ambil nama filenya
                    fileName = it.getString(nameIndex)
                    Log.d(TAG, "getFileName: Found display name: $fileName")
                } else {
                    // Fallback jika kolom nama tidak ada (jarang terjadi untuk picker standar)
                    Log.w(TAG, "getFileName: Column DISPLAY_NAME not found.")
                    fileName = uri.lastPathSegment // Coba ambil segmen terakhir dari path URI
                    Log.d(TAG, "getFileName: Using lastPathSegment as fallback: $fileName")
                }
            } else {
                Log.w(TAG, "getFileName: Cursor is empty for URI: $uri")
            }
        } ?: Log.e(TAG, "getFileName: Cursor is null for URI: $uri") // Log jika cursor null
        return fileName
    }

    // Fungsi untuk clear pesan hasil (dipanggil dari UI setelah Toast/Snackbar)
    fun clearSaveResult() {
        _saveResult.value = null
    }

    // State error message generik (untuk load)
    private val _errorMessage = mutableStateOf<String?>(null)
    // val errorMessage: State<String?> = _errorMessage // Ekspos jika perlu
}
