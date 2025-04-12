package com.afian.tugasakhir.Controller

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.Dosen
import com.afian.tugasakhir.Model.DosenResponse
import androidx.compose.runtime.State
import com.afian.tugasakhir.Model.RequestPanggilanBody
import kotlinx.coroutines.async // Import async
import kotlinx.coroutines.awaitAll // Import awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class DosenViewModel : ViewModel() {

    // --- State Flow Internal untuk list asli (private) ---
    private val _dosenList = MutableStateFlow<List<Dosen>>(emptyList())
    private val _dosenNotInCampusList = MutableStateFlow<List<Dosen>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    // State Flow BARU untuk daftar SEMUA mahasiswa
    private val _allMahasiswaList = MutableStateFlow<List<Dosen>>(emptyList()) // Pakai model Dosen jika field mirip
    val allMahasiswaList: StateFlow<List<Dosen>> = _allMahasiswaList.asStateFlow()

    // State untuk status pemanggilan (misal: loading saat panggil, pesan sukses/gagal)
    val isCalling = mutableStateOf<Int?>(null) // Simpan ID mhs yg sedang dipanggil


    private val _isLoadingDosen = mutableStateOf(false)
    val isLoadingDosen: State<Boolean> = _isLoadingDosen
    private val _errorMessageDosen = mutableStateOf<String?>(null)
    // --- State Internal untuk loading dan error (private) ---
    private val _isLoading = mutableStateOf(false)
    private val _errorMessage = mutableStateOf<String?>(null)

    // === 👇 State Publik (Read-Only) untuk UI 👇 ===
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val isLoading: State<Boolean> = _isLoading           // <-- Diekspos sebagai State<Boolean>
    val errorMessage: State<String?> = _errorMessage     // <-- Diekspos sebagai State<String?>

    val filteredDosenList: StateFlow<List<Dosen>> =
        combine(_dosenList, searchQuery) { list, query -> filterList(list, query) }
            .stateInViewModel(emptyList())

    val filteredDosenNotInCampusList: StateFlow<List<Dosen>> =
        combine(_dosenNotInCampusList, searchQuery) { list, query -> filterList(list, query) }
            .stateInViewModel(emptyList())
    // === 👆 Akhir State Publik 👆 ===

    // --- State BARU untuk Daftar Semua Mahasiswa & Filtering ---
    private val _searchQueryMahasiswa = MutableStateFlow("")
    val searchQueryMahasiswa: StateFlow<String> = _searchQueryMahasiswa.asStateFlow()
    val filteredAllMahasiswaList: StateFlow<List<Dosen>> =
        combine(_allMahasiswaList, _searchQueryMahasiswa) { list, query ->
            filterList(list, query) // Gunakan helper filter yang sama
        }.stateInViewModel(emptyList())
    private val _isLoadingMahasiswa = mutableStateOf(false)
    val isLoadingMahasiswa: State<Boolean> = _isLoadingMahasiswa
    private val _errorMahasiswa = mutableStateOf<String?>(null)
    val errorMahasiswa: State<String?> = _errorMahasiswa
    // --- Akhir State Mahasiswa ---


    // --- State untuk Aksi Panggil ---
    private val _panggilStatus = mutableStateOf<Pair<Int?, String?>>(Pair(null, null)) // Pair(mahasiswaId, statusMsg)
    val panggilStatus: State<Pair<Int?, String?>> = _panggilStatus
    // --- Akhir State Panggil ---



    init {
        loadAllDosenData()
        fetchAllMahasiswa()
    }

    // --- Fungsi Refresh ---
    fun refreshAllData() {
        loadInitialDosenData()
        fetchAllMahasiswa()
    }

    // --- Logika Fetch Dosen On/Off Campus (sudah ada, disatukan di loadInitialDosenData) ---
    private fun loadInitialDosenData() {
        if (_isLoadingDosen.value) return
        _isLoadingDosen.value = true
        _errorMessageDosen.value = null
        viewModelScope.launch {
            val job1 = async { fetchDosenOnCampusInternal() }
            val job2 = async { fetchDosenNotInCampusInternal() }
            try { awaitAll(job1, job2) }
            catch (e: Exception) { _errorMessageDosen.value = "Gagal memuat data dosen: ${e.message}" }
            finally { _isLoadingDosen.value = false }
        }
    }

    // --- Fungsi Fetch Semua Mahasiswa ---
    private fun fetchAllMahasiswa() {
        if (_isLoadingMahasiswa.value) return
        _isLoadingMahasiswa.value = true
        _errorMahasiswa.value = null
        viewModelScope.launch {
            Log.d("DosenViewModel", "Fetching all mahasiswa list...")
            try {
                val response = RetrofitClient.apiService.getAllMahasiswa() // Panggil API
                _allMahasiswaList.value = response.mahasiswa ?: emptyList()
                Log.d("DosenViewModel", "Fetched ${response.count} mahasiswa.")
            } catch (e: CancellationException) {
                Log.i("DosenViewModel", "Fetch all mahasiswa cancelled.")
                _isLoadingMahasiswa.value = false // Tetap set false jika cancel
                throw e
            } catch (e: Exception) {
                Log.e("DosenViewModel", "Failure fetching all mahasiswa", e)
                _errorMahasiswa.value = "Gagal memuat mahasiswa: ${e.message}"
                _allMahasiswaList.value = emptyList()
            } finally {
                _isLoadingMahasiswa.value = false
            }
        }
    }

    // --- Fungsi Dosen Memanggil Mahasiswa ---
    fun requestPanggilMahasiswa(dosenUserId: Int, mahasiswaUserId: Int) {
        if (_panggilStatus.value.first != null) { // Cek jika sedang ada proses panggil lain
            Log.w("DosenViewModel","Panggilan sedang diproses untuk mahasiswa lain.")
            return
        }
        _panggilStatus.value = Pair(mahasiswaUserId, "Memanggil...") // Set status loading + target ID

        viewModelScope.launch {
            var success = false
            var message: String? = null
            try {
                val requestBody = RequestPanggilanBody(dosen_user_id = dosenUserId, mahasiswa_user_id = mahasiswaUserId)
                Log.d("DosenViewModel", "Requesting call: $requestBody")
                val response = RetrofitClient.apiService.requestPanggilan(requestBody)

                if (response.isSuccessful && response.body()?.status == true) {
                    success = true
                    message = response.body()?.message ?: "Panggilan berhasil dikirim"
                    Log.i("DosenViewModel", "Panggilan request success: $message")
                } else {
                    message = response.body()?.message ?: "Gagal mengirim panggilan (Error: ${response.code()})"
                    Log.e("DosenViewModel", "Panggilan request failed: $message")
                }
            } catch (e: Exception) {
                message = "Error jaringan/server: ${e.message}"
                Log.e("DosenViewModel", "Exception during panggil request", e)
            } finally {
                // Update status setelah selesai (berhasil atau gagal)
                // Tetap tampilkan ID mahasiswa agar UI tahu proses mana yg selesai
                _panggilStatus.value = Pair(mahasiswaUserId, message ?: "Selesai")
                // Tambahkan delay lalu hapus status agar pesan tidak tampil terus?
                kotlinx.coroutines.delay(3000) // Delay 3 detik
                // Hanya hapus jika ID nya masih sama (mencegah menghapus status dari panggil lain)
                if(_panggilStatus.value.first == mahasiswaUserId) {
                    _panggilStatus.value = Pair(null, null)
                }
            }
        }
    }


    // --- Fungsi Publik ---
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query // Update state private
        Log.d("DosenViewModel", "Search query updated: $query")
    }

    fun refreshData() {
        if (!_isLoading.value) { // Baca nilai dari state (_isLoading atau isLoading sama saja)
            loadAllDosenData()
        } else {
            Log.d("DosenViewModel", "Refresh skipped, already loading.")
        }
    }

    // --- Fungsi Internal ---
    private fun loadAllDosenData() {
        if (_isLoading.value) return // Baca state private

        _isLoading.value = true       // <-- SET state private pakai .value
        _errorMessage.value = null  // <-- SET state private pakai .value
        Log.i("DosenViewModel", "Starting loading all dosen data...")

        viewModelScope.launch {
            val onCampusJob = async { fetchDosenOnCampusInternal() }
            val notInCampusJob = async { fetchDosenNotInCampusInternal() }

            try {
                awaitAll(onCampusJob, notInCampusJob)
                _errorMessage.value = null // <-- SET state private pakai .value
                Log.i("DosenViewModel", "Successfully loaded both dosen lists.")
            } catch (e: CancellationException) {
                Log.i("DosenViewModel", "Data loading was cancelled.")
            } catch (e: Exception) {
                Log.e("DosenViewModel", "Error loading one or both dosen lists.", e)
                _errorMessage.value = "Gagal memuat data: ${e.message ?: "Terjadi kesalahan"}" // <-- SET state private pakai .value
            } finally {
                _isLoading.value = false // <-- SET state private pakai .value
                Log.i("DosenViewModel", "Finished loading all dosen data attempt.")
            }
        }
    }

    // Fungsi fetch internal (asumsi ApiService.getDosenOnCampus sudah suspend)
    private suspend fun fetchDosenOnCampusInternal() {
        Log.d("DosenViewModel", "Fetching on-campus dosen...")
        try {
            val response: DosenResponse = RetrofitClient.apiService.getDosenOnCampus()
            _dosenList.value = response.dosen ?: emptyList() // Update state private
            Log.d("DosenViewModel", "Fetched ${_dosenList.value.size} on-campus dosen.")
        } catch (e: Exception) {
            // Jika error di sini, akan ditangkap oleh catch di loadAllDosenData
            _dosenList.value = emptyList() // Kosongkan list jika error
            Log.e("DosenViewModel", "Failure fetching on-campus dosen during internal call", e)
            throw e // Lempar ulang agar loading di finally loadAllDosenData berjalan
        }
    }

    // === 👇 TAMBAHKAN FUNGSI INI UNTUK SEARCH MAHASISWA 👇 ===
    /**
     * Dipanggil oleh UI (TextField) saat query pencarian untuk MAHASISWA berubah.
     * @param query Teks pencarian baru.
     */
    fun onMahasiswaSearchQueryChanged(query: String) {
        _searchQueryMahasiswa.value = query // Update StateFlow query mahasiswa
        Log.d("DosenViewModel", "Mahasiswa search query updated: $query")
    }
    // === 👆 AKHIR FUNGSI BARU 👆 ===

    // Fungsi fetch internal
    private suspend fun fetchDosenNotInCampusInternal() {
        Log.d("DosenViewModel", "Fetching not-in-campus dosen...")
        try {
            val response = RetrofitClient.apiService.getDosenNotInCampus()
            _dosenNotInCampusList.value = response.dosen_not_in_campus ?: emptyList() // Update state private
            Log.d("DosenViewModel", "Fetched ${_dosenNotInCampusList.value.size} not-in-campus dosen. API Msg: ${response.message}")
        } catch (e: Exception) {
            // Jika error di sini, akan ditangkap oleh catch di loadAllDosenData
            _dosenNotInCampusList.value = emptyList() // Kosongkan list jika error
            Log.e("DosenViewModel", "Failure fetching not-in-campus dosen during internal call", e)
            throw e // Lempar ulang
        }
    }

    // Fungsi helper filter
    private fun filterList(list: List<Dosen>, query: String): List<Dosen> {
        // ... (logika filter seperti sebelumnya) ...
        return if (query.isBlank()) list else list.filter { dosen ->
            (dosen.user_name ?: "").contains(query, ignoreCase = true) ||
                    dosen.identifier.contains(query, ignoreCase = true)
        }
    }

    // Fungsi ekstensi stateIn
    private fun <T> Flow<T>.stateInViewModel(initialValue: T): StateFlow<T> =
        this.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), initialValue)

}