package com.afian.tugasakhir.Controller

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.Dosen
import com.afian.tugasakhir.Model.DosenResponse
import androidx.compose.runtime.State
import com.afian.tugasakhir.Model.PanggilanHistoryDosenItem
import com.afian.tugasakhir.Model.RequestPanggilanBody
import kotlinx.coroutines.async // Import async
import kotlinx.coroutines.awaitAll // Import awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Long.min
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.pow

class DosenViewModel : ViewModel() {

    private val _dosenList = MutableStateFlow<List<Dosen>>(emptyList())
    private val _dosenNotInCampusList = MutableStateFlow<List<Dosen>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _allMahasiswaList = MutableStateFlow<List<Dosen>>(emptyList())
    private val _isLoadingDosen = mutableStateOf(false)
    val isLoadingDosen: State<Boolean> = _isLoadingDosen
    private val _errorMessageDosen = mutableStateOf<String?>(null)
    private val _isLoading = mutableStateOf(false)
    private val _errorMessage = mutableStateOf<String?>(null)
    private val _dosenPanggilanHistory = MutableStateFlow<List<PanggilanHistoryDosenItem>>(emptyList())
    val dosenPanggilanHistory: StateFlow<List<PanggilanHistoryDosenItem>> = _dosenPanggilanHistory.asStateFlow()

    private val _isLoadingHistory = mutableStateOf(false) // Loading khusus riwayat
    val isLoadingHistory: State<Boolean> = _isLoadingHistory

    private val _errorHistory = mutableStateOf<String?>(null) // Error khusus riwayat
    val errorHistory: State<String?> = _errorHistory
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val isLoading: State<Boolean> = _isLoading
    val errorMessage: State<String?> = _errorMessage

    val filteredDosenList: StateFlow<List<Dosen>> =
        combine(_dosenList, searchQuery) { list, query -> filterList(list, query) }
            .stateInViewModel(emptyList())

    val filteredDosenNotInCampusList: StateFlow<List<Dosen>> =
        combine(_dosenNotInCampusList, searchQuery) { list, query -> filterList(list, query) }
            .stateInViewModel(emptyList())



    private var dosenRetryAttempt = 0
    private val maxRetryDelayMillis = 60000L
    private val initialRetryDelayMillis = 3000L
    private val MAX_DOSEN_RETRY_ATTEMPTS = 10


    private val _searchQueryMahasiswa = MutableStateFlow("")
    val searchQueryMahasiswa: StateFlow<String> = _searchQueryMahasiswa.asStateFlow()
    val filteredAllMahasiswaList: StateFlow<List<Dosen>> =
        combine(_allMahasiswaList, _searchQueryMahasiswa) { list, query ->
            filterList(list, query)
        }.stateInViewModel(emptyList())
    private val _isLoadingMahasiswa = mutableStateOf(false)
    val isLoadingMahasiswa: State<Boolean> = _isLoadingMahasiswa
    private val _errorMahasiswa = mutableStateOf<String?>(null)
    val errorMahasiswa: State<String?> = _errorMahasiswa

        private val _panggilStatus = mutableStateOf<Pair<Int?, String?>>(Pair(null, null))
    val panggilStatus: State<Pair<Int?, String?>> = _panggilStatus



    private fun executeDosenLoadWithRetry() {

        if (_isLoading.value && dosenRetryAttempt == 0) {
            Log.d("DosenViewModel", "executeDosenLoadWithRetry skipped, already loading initial data.")
            return
        }


        _isLoading.value = true

        if (dosenRetryAttempt == 0) {
            _errorMessage.value = null
        }

        Log.i("DosenViewModel", "Starting/Retrying load all dosen data (Attempt ${dosenRetryAttempt + 1})...")

        viewModelScope.launch {
            try {

                val onCampusJob = async { fetchDosenOnCampusInternal() }
                val notInCampusJob = async { fetchDosenNotInCampusInternal() }

                awaitAll(onCampusJob, notInCampusJob)


                Log.i("DosenViewModel", "Successfully loaded both dosen lists (Attempt ${dosenRetryAttempt + 1}).")
                _isLoading.value = false
                _errorMessage.value = null
                dosenRetryAttempt = 0

            } catch (e: CancellationException) {

                _isLoading.value = false
                dosenRetryAttempt = 0
                Log.i("DosenViewModel", "Dosen data loading was cancelled.")


            } catch (e: Exception) {
                Log.e("DosenViewModel", "Error loading dosen lists (Attempt ${dosenRetryAttempt + 1}): ${e.javaClass.simpleName} - ${e.message}")

                dosenRetryAttempt++

                if (dosenRetryAttempt > MAX_DOSEN_RETRY_ATTEMPTS) {
                    Log.e("DosenViewModel", "Max retry attempts ($MAX_DOSEN_RETRY_ATTEMPTS) reached. Stopping retries.")
                    _errorMessage.value = "Gagal memuat data setelah $MAX_DOSEN_RETRY_ATTEMPTS percobaan. Periksa koneksi internet."
                    _isLoading.value = false
                } else {
                    val delayMillis = min(
                        initialRetryDelayMillis * (2.0.pow(dosenRetryAttempt - 1).toLong()),
                        maxRetryDelayMillis
                    )
                    Log.w("DosenViewModel", "Scheduling retry attempt ${dosenRetryAttempt + 1} after ${delayMillis}ms...")
                    _errorMessage.value = "Gagal terhubung. Mencoba lagi (${dosenRetryAttempt}/${MAX_DOSEN_RETRY_ATTEMPTS})..."
                    _isLoading.value = true
                    delay(delayMillis)
                    executeDosenLoadWithRetry()
                }
            }
        }
    }
    fun loadDosenDataWithRetry() {
        if (_isLoading.value && dosenRetryAttempt > 0) {
            Log.d("DosenViewModel", "Load Dosen Data with Retry skipped, a retry sequence is already in progress.")
            return
        }
        if (_isLoading.value && dosenRetryAttempt == 0 && _errorMessage.value == null) { // Kasus load awal masih berjalan
            Log.d("DosenViewModel", "Load Dosen Data with Retry skipped, initial load in progress.")
            return
        }

        Log.d("DosenViewModel", "Manual trigger for loadDosenDataWithRetry.")
        dosenRetryAttempt = 0
        _errorMessage.value = null
        executeDosenLoadWithRetry()
    }

    init {
        loadAllDosenDataWithRetry()
        fetchAllMahasiswa()
    }
    private fun loadAllDosenDataWithRetry() {
        if (_isLoading.value && dosenRetryAttempt == 0) {
            Log.d("DosenViewModel", "loadAllDosenDataWithRetry skipped, already loading initial data.")
            return
        }


        _isLoading.value = true

        if (dosenRetryAttempt == 0) {
            _errorMessage.value = null
        }

        Log.i("DosenViewModel", "Starting/Retrying load all dosen data (Attempt ${dosenRetryAttempt + 1})...")

        viewModelScope.launch {
            try {

                val onCampusJob = async { fetchDosenOnCampusInternal() }
                val notInCampusJob = async { fetchDosenNotInCampusInternal() }

                awaitAll(onCampusJob, notInCampusJob)


                Log.i("DosenViewModel", "Successfully loaded both dosen lists (Attempt ${dosenRetryAttempt + 1}).")
                _isLoading.value = false
                _errorMessage.value = null
                dosenRetryAttempt = 0

            } catch (e: CancellationException) {

                _isLoading.value = false
                dosenRetryAttempt = 0
                Log.i("DosenViewModel", "Dosen data loading was cancelled.")


            } catch (e: Exception) {

                Log.e("DosenViewModel", "Error loading dosen lists (Attempt ${dosenRetryAttempt + 1}): ${e.javaClass.simpleName} - ${e.message}")

                dosenRetryAttempt++


                if (dosenRetryAttempt > MAX_DOSEN_RETRY_ATTEMPTS) {
                    Log.e("DosenViewModel", "Max retry attempts ($MAX_DOSEN_RETRY_ATTEMPTS) reached. Stopping retries.")
                    _errorMessage.value = "Gagal memuat data setelah $MAX_DOSEN_RETRY_ATTEMPTS percobaan. Periksa koneksi internet."
                    _isLoading.value = false
                } else {

                    val delayMillis = min(
                        initialRetryDelayMillis * (2.0.pow(dosenRetryAttempt - 1).toLong()),
                        maxRetryDelayMillis
                    )
                    Log.w("DosenViewModel", "Scheduling retry attempt ${dosenRetryAttempt + 1} after ${delayMillis}ms...")

                    _errorMessage.value = "Gagal terhubung. Mencoba lagi (${dosenRetryAttempt}/${MAX_DOSEN_RETRY_ATTEMPTS})..."


                    _isLoading.value = true
                    delay(delayMillis)


                    loadAllDosenDataWithRetry()
                }
            }
        }
    }


    fun fetchDosenHistory(dosenUserId: Int) {
        if (dosenUserId == -1) {
            _errorHistory.value = "ID Dosen tidak valid untuk memuat riwayat."
            return
        }
        if (_isLoadingHistory.value) return

        _isLoadingHistory.value = true
        _errorHistory.value = null
        Log.d("DosenViewModel", "Fetching call history for Dosen ID: $dosenUserId")

        viewModelScope.launch {
            try {

                val response = RetrofitClient.apiService.getDosenPanggilanHistory(dosenUserId)
                _dosenPanggilanHistory.value = response.history ?: emptyList()
                Log.d("DosenViewModel", "Fetched ${response.history?.size ?: 0} history items for Dosen.")
            } catch (e: CancellationException) {
                Log.i("DosenViewModel", "Fetch Dosen history cancelled.")

            } catch (e: Exception) {
                Log.e("DosenViewModel", "Failure fetching Dosen history", e)
                _errorHistory.value = "Gagal memuat riwayat: ${e.message ?: "Error tidak diketahui"}"
                _dosenPanggilanHistory.value = emptyList()
            } finally {
                _isLoadingHistory.value = false
            }
        }
    }


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


    private fun fetchAllMahasiswa() {
        if (_isLoadingMahasiswa.value) return
        _isLoadingMahasiswa.value = true
        _errorMahasiswa.value = null
        viewModelScope.launch {
            Log.d("DosenViewModel", "Fetching all mahasiswa list...")
            try {
                val response = RetrofitClient.apiService.getAllMahasiswa()
                _allMahasiswaList.value = response.mahasiswa ?: emptyList()
                Log.d("DosenViewModel", "Fetched ${response.count} mahasiswa.")
            } catch (e: CancellationException) {
                Log.i("DosenViewModel", "Fetch all mahasiswa cancelled.")
                _isLoadingMahasiswa.value = false
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


    fun requestPanggilMahasiswa(dosenUserId: Int, mahasiswaUserId: Int) {
        if (_panggilStatus.value.first != null) {
            Log.w("DosenViewModel","Panggilan sedang diproses untuk mahasiswa lain.")
            return
        }
        _panggilStatus.value = Pair(mahasiswaUserId, "Memanggil...")

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


                _panggilStatus.value = Pair(mahasiswaUserId, message ?: "Selesai")
                kotlinx.coroutines.delay(3000)
                if(_panggilStatus.value.first == mahasiswaUserId) {
                    _panggilStatus.value = Pair(null, null)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        Log.d("DosenViewModel", "Search query updated: $query")
    }

    fun refreshData() {
        if (!_isLoading.value) {
            loadInitialDosenData()
        } else {
            Log.d("DosenViewModel", "Refresh skipped, already loading.")
        }
    }


    private fun loadAllDosenData() {
        if (_isLoading.value) return

        _isLoading.value = true
        _errorMessage.value = null
        Log.i("DosenViewModel", "Starting loading all dosen data...")

        viewModelScope.launch {
            val onCampusJob = async { fetchDosenOnCampusInternal() }
            val notInCampusJob = async { fetchDosenNotInCampusInternal() }

            try {
                awaitAll(onCampusJob, notInCampusJob)
                _errorMessage.value = null
                Log.i("DosenViewModel", "Successfully loaded both dosen lists.")
            } catch (e: CancellationException) {
                Log.i("DosenViewModel", "Data loading was cancelled.")
            } catch (e: Exception) {
                Log.e("DosenViewModel", "Error loading one or both dosen lists.", e)
                _errorMessage.value = "Gagal memuat data: ${e.message ?: "Terjadi kesalahan"}"
            } finally {
                _isLoading.value = false
                Log.i("DosenViewModel", "Finished loading all dosen data attempt.")
            }
        }
    }


    private suspend fun fetchDosenOnCampusInternal() {
        Log.d("DosenViewModel", "Fetching on-campus dosen...")
        try {
            val response: DosenResponse = RetrofitClient.apiService.getDosenOnCampus()
            _dosenList.value = response.dosen ?: emptyList()
            Log.d("DosenViewModel", "Fetched ${_dosenList.value.size} on-campus dosen.")
        } catch (e: Exception) {

            _dosenList.value = emptyList() // Kosongkan list jika error
            Log.e("DosenViewModel", "Failure fetching on-campus dosen during internal call", e)
            throw e
        }
    }
    
    fun onMahasiswaSearchQueryChanged(query: String) {
        _searchQueryMahasiswa.value = query
        Log.d("DosenViewModel", "Mahasiswa search query updated: $query")
    }

    private suspend fun fetchDosenNotInCampusInternal() {
        Log.d("DosenViewModel", "Fetching not-in-campus dosen...")
        try {
            val response = RetrofitClient.apiService.getDosenNotInCampus()
            _dosenNotInCampusList.value = response.dosen_not_in_campus ?: emptyList()
            Log.d("DosenViewModel", "Fetched ${_dosenNotInCampusList.value.size} not-in-campus dosen. API Msg: ${response.message}")
        } catch (e: Exception) {
            _dosenNotInCampusList.value = emptyList()
            Log.e("DosenViewModel", "Failure fetching not-in-campus dosen during internal call", e)
            throw e
        }
    }


    private fun filterList(list: List<Dosen>, query: String): List<Dosen> {
        return if (query.isBlank()) list else list.filter { dosen ->
            (dosen.user_name ?: "").contains(query, ignoreCase = true) ||
                    dosen.identifier.contains(query, ignoreCase = true)
        }
    }

    private fun <T> Flow<T>.stateInViewModel(initialValue: T): StateFlow<T> =
        this.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), initialValue)

}