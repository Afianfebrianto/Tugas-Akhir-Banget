package com.afian.tugasakhir.API

import com.afian.tugasakhir.Model.DosenResponse
import com.afian.tugasakhir.Model.LoginRequest
import com.afian.tugasakhir.Model.LoginResponse
import com.afian.tugasakhir.Model.User
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/users/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/users/dosen-di-kampus")
    fun getDosen(): Call<DosenResponse>
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.8.103:3000/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}