package com.afian.tugasakhir.Controller

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.PeringkatItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.runtime.State
import kotlin.coroutines.cancellation.CancellationException

class PeringkatDosenViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // State untuk bulan dan tahun yang dipilih
    private val _selectedYear = mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: State<Int> = _selectedYear

    private val _selectedMonth = mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) // Bulan 1-12
    val selectedMonth: State<Int> = _selectedMonth

    // State untuk daftar peringkat
    private val _peringkatList = MutableStateFlow<List<PeringkatItem>>(emptyList())
    val peringkatList: StateFlow<List<PeringkatItem>> = _peringkatList.asStateFlow()

    // State untuk loading dan error
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // Job untuk fetch data agar bisa dibatalkan jika user ganti bulan/tahun dengan cepat
    private var fetchJob: Job? = null

    init {
        // Ambil data untuk bulan dan tahun saat ini saat ViewModel dibuat
        fetchPeringkat()
    }

    /** Dipanggil UI saat tahun dipilih */
    fun onYearSelected(year: Int) {
        if (year != _selectedYear.intValue) {
            _selectedYear.intValue = year
            fetchPeringkat() // Ambil data baru
        }
    }

    /** Dipanggil UI saat bulan dipilih */
    fun onMonthSelected(month: Int) { // Terima bulan 1-12
        if (month != _selectedMonth.intValue) {
            _selectedMonth.intValue = month
            fetchPeringkat() // Ambil data baru
        }
    }

    /** Mengambil data peringkat dari API */
    fun fetchPeringkat() {
        // Batalkan job fetch sebelumnya jika sedang berjalan
        fetchJob?.cancel()

        _isLoading.value = true
        _errorMessage.value = null
        val month = _selectedMonth.intValue
        val year = _selectedYear.intValue
        Log.d("PeringkatVM", "Fetching peringkat for $month/$year")

        fetchJob = viewModelScope.launch {
            try {
                val response = apiService.getPeringkatDurasiDosen(month, year)
                if (response.status) {
                    _peringkatList.value = response.peringkat ?: emptyList()
                    Log.d("PeringkatVM", "Fetched ${response.peringkat?.size ?: 0} items.")
                } else {
                    throw Exception(response.message ?: "Gagal mengambil data peringkat")
                }
                _errorMessage.value = null // Hapus error jika sukses
            } catch (e: CancellationException) {
                Log.i("PeringkatVM", "Fetch cancelled for $month/$year")
                // Tidak set error jika dibatalkan
            } catch (e: Exception) {
                Log.e("PeringkatVM", "Error fetching peringkat for $month/$year", e)
                _errorMessage.value = "Gagal memuat: ${e.message}"
                _peringkatList.value = emptyList() // Kosongkan list jika error
            } finally {
                // Hanya set loading false jika job saat ini belum dibatalkan oleh job baru
                if (coroutineContext.isActive) {
                    _isLoading.value = false
                    Log.d("PeringkatVM", "Finished fetching attempt for $month/$year")
                }
            }
        }
    }
}