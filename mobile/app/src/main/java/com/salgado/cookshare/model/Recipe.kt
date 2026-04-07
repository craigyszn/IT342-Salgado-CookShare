package com.salgado.cookshare.model

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val tags: List<String>,
    val rating: Double,
    val reviewCount: Int,
    val prepTime: String,
    val cookTime: String,
    val difficulty: String,
    val author: String,
    val servings: Int,
    val imageUrl: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val category: String = ""
)