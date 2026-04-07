package com.salgado.cookshare.api

import com.salgado.cookshare.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<String>

    // ── Recipes ───────────────────────────────────────────────────────────────
    @GET("api/recipes")
    fun getAllRecipes(): Call<List<DbRecipe>>

    @GET("api/recipes/user")
    fun getRecipesByUser(@Query("email") email: String): Call<List<DbRecipe>>

    @POST("api/recipes")
    fun createRecipe(@Body recipe: CreateRecipeRequest): Call<DbRecipe>

    @DELETE("api/recipes/{id}")
    fun deleteRecipe(@Path("id") id: String): Call<String>

    @POST("api/recipes/{id}/rate")
    fun rateRecipe(@Path("id") id: String, @Body body: RateRequest): Call<DbRecipe>

    // ── Favorites ─────────────────────────────────────────────────────────────
    @GET("api/favorites")
    fun getFavorites(@Query("email") email: String): Call<List<FavoriteItem>>

    @GET("api/favorites/check")
    fun checkFavorite(
        @Query("email") email: String,
        @Query("recipeId") recipeId: String
    ): Call<FavoriteCheckResponse>

    @POST("api/favorites")
    fun addFavorite(@Body body: FavoriteRequest): Call<FavoriteItem>

    @DELETE("api/favorites")
    fun removeFavorite(
        @Query("email") email: String,
        @Query("recipeId") recipeId: String
    ): Call<String>

    // ── Comments ──────────────────────────────────────────────────────────────
    @GET("api/comments")
    fun getComments(@Query("recipeId") recipeId: String): Call<List<CommentItem>>

    @POST("api/comments")
    fun postComment(@Body body: CommentRequest): Call<CommentItem>

    // ── User Stats ────────────────────────────────────────────────────────────
    @GET("api/users/stats")
    fun getUserStats(@Query("email") email: String): Call<UserStats>

    @GET("api/users/count")
    fun getUserCount(): Call<UserCountResponse>
}