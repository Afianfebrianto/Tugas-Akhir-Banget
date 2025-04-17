package com.afian.tugasakhir.API

import com.afian.tugasakhir.Model.AddLocationRequest
import com.afian.tugasakhir.Model.AddLocationResponse
import com.afian.tugasakhir.Model.DosenHistoryResponse
import com.afian.tugasakhir.Model.DosenNotInCampusResponse
import com.afian.tugasakhir.Model.DosenResponse
import com.afian.tugasakhir.Model.LoginRequest
import com.afian.tugasakhir.Model.LoginResponse
import com.afian.tugasakhir.Model.MahasiswaHistoryResponse
import com.afian.tugasakhir.Model.MahasiswaListResponse
import com.afian.tugasakhir.Model.PeringkatResponse
import com.afian.tugasakhir.Model.RegisterTokenRequest
import com.afian.tugasakhir.Model.RequestPanggilanBody
import com.afian.tugasakhir.Model.RequestPanggilanResponse
import com.afian.tugasakhir.Model.RespondPanggilanBody
import com.afian.tugasakhir.Model.RespondPanggilanResponse
import com.afian.tugasakhir.Model.UpdateLocationRequest
import com.afian.tugasakhir.Model.UpdateLocationResponse
import com.afian.tugasakhir.Model.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/users/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/users/dosen-di-kampus")
    suspend fun getDosenOnCampus(): DosenResponse

    @POST("api/users/add-location")
    suspend fun addLocation(@Body body: AddLocationRequest): AddLocationResponse // Gunakan suspend

    @PUT("api/users/update-location")
    suspend fun updateLocation(@Body body: UpdateLocationRequest): UpdateLocationResponse // Gunakan suspend

    @POST("api/users/register-fcm-token") // Sesuaikan path jika berbeda
    suspend fun registerFcmToken(@Body body: RegisterTokenRequest): retrofit2.Response<Void> // Contoh Response, sesuaikan
    // Atau bisa juga: suspend fun registerFcmToken(@Body body: Map<String, Any>): retrofit2.Response<Void>

    @GET("api/users/dosen-not-in-campus")
    suspend fun getDosenNotInCampus(): DosenNotInCampusResponse //

    // Endpoint baru untuk ambil semua mahasiswa
    @GET("api/users/mahasiswa")
    suspend fun getAllMahasiswa(): MahasiswaListResponse

    // Endpoint baru untuk dosen memanggil mahasiswa
    @POST("api/panggilan/request")
    suspend fun requestPanggilan(@Body body: RequestPanggilanBody): Response<RequestPanggilanResponse> // Sesuaikan Response jika perlu

    // Endpoint baru untuk mahasiswa merespons panggilan
    @PUT("api/panggilan/respond")
    suspend fun respondPanggilan(@Body body: RespondPanggilanBody): Response<RespondPanggilanResponse> // Sesuaikan Response

    // Endpoint baru untuk riwayat panggilan dosen
    @GET("api/panggilan/history/dosen/{dosen_user_id}") // Gunakan Path parameter
    suspend fun getDosenPanggilanHistory(@Path("dosen_user_id") dosenUserId: Int): DosenHistoryResponse

    // Endpoint baru untuk riwayat panggilan mahasiswa
    @GET("api/panggilan/history/mahasiswa/{mahasiswa_user_id}") // Gunakan Path parameter
    suspend fun getMahasiswaPanggilanHistory(@Path("mahasiswa_user_id") mahasiswaUserId: Int): MahasiswaHistoryResponse

    @GET("api/users/peringkat-dosen-durasi") // Sesuaikan path jika berbeda
    suspend fun getPeringkatDurasiDosen(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): PeringkatResponse

}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.40:3000/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}