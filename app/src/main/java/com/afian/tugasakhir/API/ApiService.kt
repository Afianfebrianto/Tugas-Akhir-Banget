package com.afian.tugasakhir.API

import com.afian.tugasakhir.Model.AddLocationRequest
import com.afian.tugasakhir.Model.AddLocationResponse
import com.afian.tugasakhir.Model.DosenResponse
import com.afian.tugasakhir.Model.LoginRequest
import com.afian.tugasakhir.Model.LoginResponse
import com.afian.tugasakhir.Model.UpdateLocationRequest
import com.afian.tugasakhir.Model.UpdateLocationResponse
import com.afian.tugasakhir.Model.User
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
    @POST("api/users/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/users/dosen-di-kampus")
    fun getDosen(): Call<DosenResponse>

    @POST("api/users/add-location")
    suspend fun addLocation(@Body body: AddLocationRequest): AddLocationResponse // Gunakan suspend

    @PUT("api/users/update-location")
    suspend fun updateLocation(@Body body: UpdateLocationRequest): UpdateLocationResponse // Gunakan suspend
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.138:3000/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}