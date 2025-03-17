package com.afian.tugasakhir.API

import com.afian.tugasakhir.Model.LoginRequest
import com.afian.tugasakhir.Model.LoginResponse
import com.afian.tugasakhir.Model.User
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/users/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse
}

data class LoginRequest(val identifier: String, val password: String)
data class LoginResponse(val message: String, val status: Boolean, val user: User)

object RetrofitClient {
    private const val BASE_URL = "http://192.168.0.103:3000/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}