package com.salgado.cookshare.api

import com.salgado.cookshare.model.SpoonacularResponse
import com.salgado.cookshare.model.SpoonacularRandomResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SpoonacularApiService {

    @GET("recipes/random")
    fun getRandomRecipes(
        @Query("number") number: Int = 6,
        @Query("addRecipeInformation") addInfo: Boolean = true,
        @Query("fillIngredients") fillIngredients: Boolean = true,
        @Query("addRecipeInstructions") addInstructions: Boolean = true,
        @Query("apiKey") apiKey: String
    ): Call<SpoonacularRandomResponse>

    @GET("recipes/complexSearch")
    fun searchRecipes(
        @Query("query") query: String,
        @Query("number") number: Int = 9,
        @Query("addRecipeInformation") addInfo: Boolean = true,
        @Query("fillIngredients") fillIngredients: Boolean = true,
        @Query("addRecipeInstructions") addInstructions: Boolean = true,
        @Query("instructionsRequired") instructionsRequired: Boolean = true,
        @Query("apiKey") apiKey: String
    ): Call<SpoonacularResponse>
}

