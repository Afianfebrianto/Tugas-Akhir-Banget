package com.afian.tugasakhir.API

import com.afian.tugasakhir.Model.AddLocationRequest
import com.afian.tugasakhir.Model.AddLocationResponse
import com.afian.tugasakhir.Model.AllUsersResponse
import com.afian.tugasakhir.Model.BulkUploadResponse
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
import com.afian.tugasakhir.Model.SimpleStatusResponse
import com.afian.tugasakhir.Model.UpdateLocationRequest
import com.afian.tugasakhir.Model.UpdateLocationResponse
import com.afian.tugasakhir.Model.UpdatePasswordRequest
import com.afian.tugasakhir.Model.UpdateProfileResponse
import com.afian.tugasakhir.Model.User
import com.afian.tugasakhir.Model.UserProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {
    @POST("api/users/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/users/dosen-di-kampus")
    suspend fun getDosenOnCampus(): DosenResponse

    @POST("api/users/add-location")
    suspend fun addLocation(@Body body: AddLocationRequest): AddLocationResponse

    @PUT("api/users/update-location")
    suspend fun updateLocation(@Body body: UpdateLocationRequest): UpdateLocationResponse

    @POST("api/users/register-fcm-token")
    suspend fun registerFcmToken(@Body body: RegisterTokenRequest): retrofit2.Response<Void>

    @GET("api/users/dosen-not-in-campus")
    suspend fun getDosenNotInCampus(): DosenNotInCampusResponse

    @GET("api/users/mahasiswa")
    suspend fun getAllMahasiswa(): MahasiswaListResponse

    @POST("api/panggilan/request")
    suspend fun requestPanggilan(@Body body: RequestPanggilanBody): Response<RequestPanggilanResponse>

    @PUT("api/panggilan/respond")
    suspend fun respondPanggilan(@Body body: RespondPanggilanBody): Response<RespondPanggilanResponse>


    @GET("api/panggilan/history/dosen/{dosen_user_id}")
    suspend fun getDosenPanggilanHistory(@Path("dosen_user_id") dosenUserId: Int): DosenHistoryResponse


    @GET("api/panggilan/history/mahasiswa/{mahasiswa_user_id}")
    suspend fun getMahasiswaPanggilanHistory(@Path("mahasiswa_user_id") mahasiswaUserId: Int): MahasiswaHistoryResponse

    @GET("api/users/peringkat-dosen-durasi")
    suspend fun getPeringkatDurasiDosen(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): PeringkatResponse

    @Multipart
    @POST("api/users/generate/bulk-excel")
    suspend fun uploadUserExcel(
        @Part file: MultipartBody.Part
    ): Response<BulkUploadResponse>

    @GET("api/users/all")
    suspend fun getAllUsers(): AllUsersResponse

    @PUT("api/users/recover-password/{user_id}")
    suspend fun recoverUserPassword(
        @Path("user_id") userId: Int
    ): Response<SimpleStatusResponse>

    @Streaming
    @GET("api/reports/dosen-durasi-harian.xlsx")
    suspend fun downloadDosenHarianReport(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<ResponseBody>

    @GET("api/users/profile/{user_id}")
    suspend fun getUserProfile(@Path("user_id") userId: Int): UserProfileResponse

    @Multipart
    @PUT("api/users/update-profile/{identifier}")
    suspend fun updateUserProfile(
        @Path("identifier") identifier: String,
        @Part photo: MultipartBody.Part?,
        @Part("no_hp") noHp: RequestBody?,
        @Part("informasi") informasi: RequestBody?
    ): Response<UpdateProfileResponse>
    @PUT("api/users/update-password")
    suspend fun updatePassword(@Body body: UpdatePasswordRequest): Response<SimpleStatusResponse>
}

object RetrofitClient {

    private const val BASE_URL = "https://dosentracker.kebunrayabundahayati.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
    val getBaseUrl = BASE_URL
}