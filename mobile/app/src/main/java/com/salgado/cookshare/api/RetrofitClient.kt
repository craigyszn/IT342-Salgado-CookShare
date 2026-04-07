package com.salgado.cookshare.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // ── Auth API (your Spring Boot backend) ───────────────────────────────────
    // Use 10.0.2.2 for emulator (maps to localhost on your PC)
    private const val AUTH_BASE_URL = "http://10.0.2.2:8081/"

    // ── Spoonacular API ───────────────────────────────────────────────────────
    private const val SPOONACULAR_BASE_URL = "https://api.spoonacular.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Auth Retrofit instance
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Spoonacular Retrofit instance
    val spoonacular: SpoonacularApiService by lazy {
        Retrofit.Builder()
            .baseUrl(SPOONACULAR_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpoonacularApiService::class.java)
    }
}