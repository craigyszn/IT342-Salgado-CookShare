package com.salgado.cookshare.api

import com.salgado.cookshare.model.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<String>

    @POST("api/auth/refresh")
    fun refreshToken(@Body request: RefreshRequest): Call<LoginResponse>

    @POST("api/auth/logout")
    fun logout(@Body request: RefreshRequest): Call<String>

    // ── Recipes ───────────────────────────────────────────────────────────────
    @GET("api/recipes")
    fun getAllRecipes(): Call<List<DbRecipe>>

    @GET("api/recipes/user")
    fun getRecipesByUser(@Query("email") email: String): Call<List<DbRecipe>>

    @POST("api/recipes")
    fun createRecipe(
        @Header("Authorization") token: String,
        @Body recipe: CreateRecipeRequest
    ): Call<DbRecipe>

    @DELETE("api/recipes/{id}")
    fun deleteRecipe(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Call<String>

    @POST("api/recipes/{id}/rate")
    fun rateRecipe(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: RateRequest
    ): Call<DbRecipe>

    // ── Image Upload ──────────────────────────────────────────────────────────
    @Multipart
    @POST("api/recipes/upload-image")
    fun uploadRecipeImage(
        @Part image: MultipartBody.Part
    ): Call<ImageUploadResponse>

    @Multipart
    @POST("api/users/upload-profile-photo")
    fun uploadProfilePhoto(
        @Part photo: MultipartBody.Part,
        @Part("email") email: okhttp3.RequestBody
    ): Call<ProfilePhotoResponse>

    @GET("api/users/profile-photo")
    fun getProfilePhoto(@Query("email") email: String): Call<ProfilePhotoResponse>

    // ── Favorites ─────────────────────────────────────────────────────────────
    @GET("api/favorites")
    fun getFavorites(@Query("email") email: String): Call<List<FavoriteItem>>

    @GET("api/favorites/check")
    fun checkFavorite(
        @Query("email") email: String,
        @Query("recipeId") recipeId: String
    ): Call<FavoriteCheckResponse>

    @POST("api/favorites")
    fun addFavorite(
        @Header("Authorization") token: String,
        @Body body: FavoriteRequest
    ): Call<FavoriteItem>

    @DELETE("api/favorites")
    fun removeFavorite(
        @Header("Authorization") token: String,
        @Query("email") email: String,
        @Query("recipeId") recipeId: String
    ): Call<String>

    // ── Comments ──────────────────────────────────────────────────────────────
    @GET("api/comments")
    fun getComments(@Query("recipeId") recipeId: String): Call<List<CommentItem>>

    @POST("api/comments")
    fun postComment(
        @Header("Authorization") token: String,
        @Body body: CommentRequest
    ): Call<CommentItem>

    // ── User Stats ────────────────────────────────────────────────────────────
    @GET("api/users/stats")
    fun getUserStats(@Query("email") email: String): Call<UserStats>

    @GET("api/users/count")
    fun getUserCount(): Call<UserCountResponse>

    @GET("api/recipes/{id}/my-rating")
    fun getMyRating(
        @Path("id") id: String,
        @Query("email") email: String
    ): Call<MyRatingResponse>
}