package com.salgado.cookshare.api

import com.salgado.cookshare.model.LoginRequest
import com.salgado.cookshare.model.LoginResponse
import com.salgado.cookshare.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<String>

}

