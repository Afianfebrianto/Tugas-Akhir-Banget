package com.afian.tugasakhir.Controller

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afian.tugasakhir.API.RetrofitClient
import com.afian.tugasakhir.Model.Dosen
import com.afian.tugasakhir.Model.DosenResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DosenViewModel : ViewModel() {
    private val _dosenList = MutableStateFlow<List<Dosen>>(emptyList())
    val dosenList: StateFlow<List<Dosen>> = _dosenList

    init {
        fetchDosen()
    }

    private fun fetchDosen() {
        viewModelScope.launch {
            RetrofitClient.apiService.getDosen().enqueue(object : Callback<DosenResponse> {
                override fun onResponse(call: Call<DosenResponse>, response: Response<DosenResponse>) {
                    if (response.isSuccessful) {
                        Log.d("DosenViewModel", "Response: ${response.body()}")
                        response.body()?.let {
                            _dosenList.value = it.dosen
                        }
                    }
                }

                override fun onFailure(call: Call<DosenResponse>, t: Throwable) {
                    // Handle failure
                    Log.d("DosenViewModel", "Gagal")
                }
            })
        }
    }
}