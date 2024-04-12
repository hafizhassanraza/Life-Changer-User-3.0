package com.enfotrix.life_changer_user_2_0.api

import com.enfotrix.life_changer_user_2_0.Data.Repo
import com.enfotrix.life_changer_user_2_0.api.Requests.ReqAddAccount
import com.enfotrix.life_changer_user_2_0.api.Requests.ReqSignup
import com.enfotrix.life_changer_user_2_0.api.Responses.ResAddAccount
import com.enfotrix.life_changer_user_2_0.api.Responses.ResIsValidCNIC
import com.enfotrix.life_changer_user_2_0.api.Responses.ResSignup
import com.enfotrix.life_changer_user_2_0.api.Responses.ResUser
import com.enfotrix.life_changer_user_2_0.api.Responses.ResUserProfileReg
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


interface APIService {


    @Multipart
    @POST("add-photo")
    fun uploadImage(
        @Part part: MultipartBody.Part,
        @Part("type") type: String
    ): Call<RequestBody?>?


/*    @Multipart
    @POST("add-photo")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Query("type") param: String
    ): Response<ResSignup> // Define YourResponseModel according to your API response*/




    @POST("cnic-validator")
    fun isValidCNIC(@Body req: String): Call<ResIsValidCNIC>

    @GET("register")
    fun signup(@Body reqSignup: ReqSignup): Call<ResSignup>


    //fun isValidCNIC(@Path("cnic") cnic: String): Call<ResIsValidCNIC>




    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: String): Call<List<Repo>>


    @GET("users/{user}/repos")
    fun getUser(@Path("user") token: String): Call<ResUser>

    @GET("users/{user}/repos")
    fun addAccount(@Path("user") req: ReqAddAccount): Call<ResAddAccount> // return list of accounts
    @GET("users/{user}/repos")
    fun addFirstAccount(@Path("user") req: ReqAddAccount): Call<ResUserProfileReg>
    @GET("users/{user}/repos")
    fun uploadPhoto(@Path("user") req: String): Call<ResUserProfileReg>
    @GET("users/{user}/repos")
    fun checkProfileStatus(@Path("user") token: String): Call<ResUserProfileReg>


}