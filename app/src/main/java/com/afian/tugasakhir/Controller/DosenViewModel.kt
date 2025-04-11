package com.afian.tugasakhir.Controller

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.Dosen
import com.afian.tugasakhir.Model.DosenResponse
import androidx.compose.runtime.State
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


    init {
        loadAllDosenData()
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